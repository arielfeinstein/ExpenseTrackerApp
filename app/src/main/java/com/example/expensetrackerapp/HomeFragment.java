package com.example.expensetrackerapp;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    private List<Expense> expenses;
    private List<Category> categories;
    private String userEmail;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init class fields
        // userEmail
        userEmail = FirebaseAuthManager.getUserEmail();

        // expenses - by default get the expenses for the current month
        Date[] firstAndLastDaysOfCurrentMonth = getFirstAndLastDayOfTheMonth();
        Date startingDate = firstAndLastDaysOfCurrentMonth[0];
        Date endingDate = firstAndLastDaysOfCurrentMonth[1];
        FirestoreManager.getExpenses(userEmail, startingDate, endingDate, new FirestoreManager.FirestoreListCallback<Expense>() {
            @Override
            public void onComplete(List<Expense> items) {
                expenses = items;
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("HomeFragment", "Failed to get list of expenses");
            }
        });

        // categories
        FirestoreManager.getCategories(userEmail, new FirestoreManager.FirestoreListCallback<Category>() {
            @Override
            public void onComplete(List<Category> items) {
                categories = items;
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("HomeFragment", "Failed to get categories");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton floatingAddButton = view.findViewById(R.id.fragment_home_floating_add_btn);

        floatingAddButton.setOnClickListener(v -> {
            showPopupWindow(view);
        });
    }

    private void showPopupWindow(@NonNull View view) {
        Context context = requireContext();

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupAddExpense = inflater.inflate(R.layout.popup_add_expense, null); // Create popup_layout.xml

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupAddExpense, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Set an elevation value for the popup window.
        popupWindow.setElevation(10);

        // Set a background drawable with a semi-transparent color.
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.argb(200, 255, 255, 255)));

        // dismiss the popup window when touched outside
        popupAddExpense.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.dismiss();
                return true;
            }
        });

        handleViewsInsideAddExpensePopup(view, context);

    }

    private void handleViewsInsideAddExpensePopup(@NonNull View view, Context context) {
        // Access views
        DatePicker datePicker = view.findViewById(R.id.popup_add_expense_date_picker);
        EditText descriptionEditText = view.findViewById(R.id.popup_add_expense_description_et);
        TextView categoryEditText = view.findViewById(R.id.popup_add_expense_category_et);
        CurrencyEditText amountEditText = view.findViewById(R.id.popup_add_expense_amount_et);

        //Date
        // Set initial date
        Calendar calendar = Calendar.getInstance();
        datePicker.updateDate(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);

        //Category
        final int[] selectedCategoryIndex = {-1}; // the index of the category inside categories list
        handleCategoryPopupMenu(context, categories, categoryEditText, selectedCategoryIndex);

        // ok button (submit)
        Button addExpenseOkBtn = view.findViewById(R.id.popup_add_expense_btn);
        addExpenseOkBtn.setOnClickListener(btn -> {
            // Fetch input data
            Date expenseDate = getDate(datePicker);
            String description = descriptionEditText.getText().toString();
            Category selectedCategory = selectedCategoryIndex[0]!=-1 ? categories.get(selectedCategoryIndex[0]) : null; // category is null if there are not categories at all
            double amount = amountEditText.getAmount();

            // If input data is valid create a new Expense and add it to the list
            // else Toast about an error
            if (!description.isEmpty() && selectedCategory != null) {
                Expense expense = new Expense(amount, selectedCategory, expenseDate, description);
                // todo: add expense to firestore and list
                FirestoreManager.addExpense(userEmail, expense, new FirestoreManager.FirestoreIdCallback() {
                    @Override
                    public void onComplete(String id) {
                        Log.d("HomeFragment", "added new expense " + id + " successfully");
                        expenses.add(expense);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d("HomeFragment", "Failed to add expense");
                    }
                });
            }
            else {
                Toast.makeText(context, "One or more fields are invalid", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static Date getDate(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth(); // 0-11
        int year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static void handleCategoryPopupMenu(Context context, List<Category> categories, TextView categoryEditText, @NonNull final int[] selectedCategoryIndex) {
        if (categories != null && (!categories.isEmpty())) {
            categoryEditText.setText(categories.get(0).getName());
            selectedCategoryIndex[0] = 0;
            categoryEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, categoryEditText);

                    // Dynamically add menu items
                    for (int i = 0; i < categories.size(); i++) {
                        popupMenu.getMenu().add(0, i, 0, categories.get(i).getName()); // (groupId, itemId, order, title)
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int categoryIndex = menuItem.getItemId(); // Get the dynamically assigned categoryIndex
                            String selectedItem = categories.get(categoryIndex).getName();
                            categoryEditText.setText(selectedItem);
                            selectedCategoryIndex[0] = categoryIndex;
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }

    // Return a Date array of size two: [first day, last day] for the current month
    private static Date[] getFirstAndLastDayOfTheMonth() {
        Calendar cal = Calendar.getInstance();

        // Get the first day of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = cal.getTime();

        // Get the last day of the month
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDayOfMonth = cal.getTime();

        return new Date[] { firstDayOfMonth, lastDayOfMonth };
    }
}