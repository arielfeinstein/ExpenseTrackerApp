package com.example.expensetrackerapp;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class HomeFragment extends Fragment {
    /*
    expensesLiveData holds the expensesList. This wrapper is used in order to know when the
    expenses list is retrieved from the database.
     */
    private MutableLiveData<List<Category>> categoriesLiveData;
    private MutableLiveData<ExpenseAdapter> expenseAdapterLiveData;

    private String userEmail;
    private Date startingDate, endingDate;
    private Context context; // todo: consider removing and using requireContext where needed


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init class fields
        //live data
        expenseAdapterLiveData = new MutableLiveData<>();
        categoriesLiveData = new MutableLiveData<>();
        // userEmail
        userEmail = FirebaseAuthManager.getUserEmail();

        // expenses - by default get the expenses for the current month
        Date[] firstAndLastDaysOfCurrentMonth = getFirstAndLastDayOfTheMonth();
        updateRecyclerList(firstAndLastDaysOfCurrentMonth[0], firstAndLastDaysOfCurrentMonth[1],
                new ExpenseAdapter(), true);

        // categories
        FirestoreManager.getCategories(userEmail, new FirestoreManager.FirestoreListCallback<Category>() {
            @Override
            public void onComplete(List<Category> items) {
                categoriesLiveData.postValue(items);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("HomeFragment", "Failed to get categories");
            }
        });

        this.context = getContext();
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

        // Access views
        FloatingActionButton floatingAddButton = view.findViewById(R.id.fragment_home_floating_add_btn);
        LinearLayout selectCustomDateLinearLayout = view.findViewById(R.id.fragment_home_select_custom_date);
        TextView selectedDatesTextView = view.findViewById(R.id.fragment_home_tv_dates);
        RecyclerView recyclerView = view.findViewById(R.id.fragment_home_recycler_view);
        TextView totalAmountTV = view.findViewById(R.id.fragment_home_amount_tv);
        View rootView = requireActivity().findViewById(android.R.id.content); // The view of the entire activity
        
        // Set selectedDatesTextView to the current range
        updateDateRangeTextView(startingDate, endingDate, selectedDatesTextView);

        // This will only be called once the data is available
        expenseAdapterLiveData.observe(getViewLifecycleOwner(), expenseAdapter -> {
            // expenseAdapter can't be null
            if (expenseAdapter == null) throw new NullPointerException("expenseAdapter can't be null");

            // Set recycler List
            // Set layout manager
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            // Set adapter
            recyclerView.setAdapter(expenseAdapter);

            // Set total expense amount listener
            expenseAdapter.setOnAmountListener(totalAmount -> {
                String amountFormattedString = String.format(Locale.US,"%.2f", totalAmount) + "₪";
                totalAmountTV.setText(amountFormattedString);
            });

            // select custom date listener
            selectCustomDateLinearLayout.setOnClickListener(v -> {
                showDateRangePicker(selectedDatesTextView, expenseAdapter);
            });

            // observe category list
            categoriesLiveData.observe(getViewLifecycleOwner(), categories -> {
                // add expense listener
                floatingAddButton.setOnClickListener(v -> {
                    // position -1 to indicate add expense popup window
                    showPopupWindow(rootView, expenseAdapter, categories, -1);
                });

                // Set click listener for an item inside the recycler list
                expenseAdapter.setItemClickListener(itemPosition -> {
                    // edit expense listener
                    showPopupWindow(rootView, expenseAdapter, categories, itemPosition);
                });
            });

        });
    }

    private void showDateRangePicker(TextView selectedDateRangeTextView, ExpenseAdapter expenseAdapter) {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select dates");

        final MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(androidx.core.util.Pair<Long, Long> selection) {
                // Get start and end dates
                Date startDate = new Date(selection.first);
                Date endDate = new Date(selection.second);

                // Update the expenses list
                updateRecyclerList(startDate, endDate, expenseAdapter, false);

                // update the selectDateRangeTextView with the new date range
                updateDateRangeTextView(startDate, endDate, selectedDateRangeTextView);
            }
        });

        picker.show(getParentFragmentManager(), picker.toString());
    }

    // update the selectDateRangeTextView with the new date range
    private void updateDateRangeTextView(Date startDate, Date endDate, TextView selectedDateRangeTextView) {
        String formattedDateRange = formatDateRange(startDate, endDate);
        selectedDateRangeTextView.setText(formattedDateRange);
    }

    /**
     * set the date range fields to be the startingDate and endingDate and pull
     * from the database the expenses and set it into the expense adapter.
     * @param startingDate will be assigned to instance field
     * @param endingDate will be assigned to instance field
     * @param postLiveData if true will postValue for the expenseAdapterLiveData
     */
    private void updateRecyclerList(Date startingDate, Date endingDate, ExpenseAdapter expenseAdapter,
                                     boolean postLiveData) {
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        FirestoreManager.getExpenses(userEmail, startingDate, endingDate, new FirestoreManager.FirestoreListCallback<Expense>() {
            @Override
            public void onComplete(List<Expense> expenses) {
                expenseAdapter.replaceExpenseList(expenses, startingDate, endingDate);
                if(postLiveData) expenseAdapterLiveData.postValue(expenseAdapter);
            }
            @Override
            public void onFailure(Exception e) {
                Log.d("HomeFragment", "Failed to get list of expenses");
            }
        });
    }

    // Return a string dd/MM/yyyy - dd/MM/yyyy starting date - ending date string from long millis
    private String formatDateRange(Date startingDate, Date endingDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); //Important for consistent date representation

        String formattedStartingDate = sdf.format(startingDate);
        String formattedEndingDate = sdf.format(endingDate);
        return formattedStartingDate + " - " + formattedEndingDate;
    }

    private void showPopupWindow(@NonNull View anchorView, @NonNull ExpenseAdapter expenseAdapter,
                                 List<Category> categories, int position) {
        // Determine if we're adding or editing
        boolean isEditing = position != -1;

        // Inflate the layout of the popup window
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_expense, null);

        // Create the popup window with fixed width for better appearance
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Get the current expense if editing
        final Expense currentExpense = isEditing ? expenseAdapter.getExpense(position) : null;

        // Set up popup content
        handleExpensePopup(popupView, popupWindow, categories, expenseAdapter, position, currentExpense, isEditing);

        // Add visual enhancements to make the popup stand out

        // 1. Set a higher elevation for stronger shadow
        popupWindow.setElevation(24f);

        // 2. Add a background with rounded corners
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        shape.setCornerRadius(16f);
        shape.setStroke(2, ContextCompat.getColor(getContext(), R.color.primaryColor));
        popupWindow.setBackgroundDrawable(shape);

        // 3. Add enter/exit animations
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        // 4. Add dim effect to background
        View rootView = getActivity().getWindow().getDecorView().getRootView();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) rootView.getLayoutParams();
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;
        getActivity().getWindow().setAttributes(params);

        // 5. When popup is dismissed, remove dim effect
        popupWindow.setOnDismissListener(() -> {
            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            getActivity().getWindow().setAttributes(params);
        });

        // Make outside touchable to dismiss
        popupWindow.setOutsideTouchable(true);

        // Center the popup with slight offset to avoid it feeling static
        int yOffset = -50; // Slight upward shift for better visual appeal
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, yOffset);
    }

    private void handleExpensePopup(@NonNull View popupView, final PopupWindow popupWindow,
                                    List<Category> categories, @NonNull ExpenseAdapter expenseAdapter,
                                    int position, Expense currentExpense, boolean isEditing) {
        // Access views
        TextView titleTextView = popupView.findViewById(R.id.popup_add_expense_title_tv);
        DatePicker datePicker = popupView.findViewById(R.id.popup_add_expense_date_picker);
        EditText descriptionEditText = popupView.findViewById(R.id.popup_add_expense_description_et);
        TextView categoryEditText = popupView.findViewById(R.id.popup_add_expense_category_et);
        CurrencyEditText amountEditText = popupView.findViewById(R.id.popup_add_expense_amount_et);
        Button actionButton = popupView.findViewById(R.id.popup_add_expense_btn);
        Button removeExpenseBtn = popupView.findViewById(R.id.popup_add_expense_remove_btn);

        Category currentCategory = null; // Used in category popupmenu to know which category should be displayed in editing mode

        // Setup UI based on mode (add or edit)
        if (isEditing) {
            // Setup for editing
            titleTextView.setText("Edit / Remove Expense");
            actionButton.setText("Edit Expense");
            removeExpenseBtn.setVisibility(VISIBLE);

            // if in editing mode the current expense can't be null.
            if (currentExpense == null) throw new NullPointerException("current expense can't be null");

            // Set existing expense data to fields

            // Set date
            Date currentExpenseDate = currentExpense.getTransactionDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentExpenseDate);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            datePicker.updateDate(year, month, day);

            // Set description
            descriptionEditText.setText(currentExpense.getDescription());
            // Set amount
            String amountFormattedString = String.format(Locale.US,"%.2f", currentExpense.getAmount()) + "₪"; // amount with 2 point precision and ₪
            amountEditText.setText(amountFormattedString);
            //Set category
            currentCategory = currentExpense.getCategory(); // will be displayed as the default category inside the category popupmenu.
            // Note: Category setting would be handled in handleCategoryPopupMenu


            // Set remove button listener
            removeExpenseBtn.setOnClickListener(view -> {
                expenseAdapter.removeExpense(position);
                popupWindow.dismiss();
            });
        } else {
            // Setup for adding
            titleTextView.setText("New Expense");
            actionButton.setText("Add Expense");
            removeExpenseBtn.setVisibility(View.GONE);

            // Set initial date
            Calendar calendar = Calendar.getInstance();
            datePicker.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            // Note: Category setting would be handled in handleCategoryPopupMenu
        }

        // Category selection - common for both modes
        final int[] selectedCategoryIndex = {-1};
        handleCategoryPopupMenu(categoryEditText, selectedCategoryIndex, categories, currentCategory);

        // Action button (Add or Edit)
        actionButton.setOnClickListener(btn -> {
            // Fetch input data
            Date expenseDate = getDateFromDatePicker(datePicker);
            String description = descriptionEditText.getText().toString();
            Category selectedCategory = selectedCategoryIndex[0] != -1 ?
                    categories.get(selectedCategoryIndex[0]) : null;
            double amount = amountEditText.getAmount();

            // Validate input
            if (!description.isEmpty() && selectedCategory != null) {
                Expense expense = new Expense(amount, selectedCategory, expenseDate, description);
                if (isEditing) {
                    // Edit existing expense
                    String existingExpenseId = currentExpense.getId();
                    FirestoreManager.editExpense(userEmail, existingExpenseId, expense,
                            new FirestoreManager.FirestoreIdCallback() {
                                @Override
                                public void onComplete(String id) {
                                    Log.d("HomeFragment", "edited expense " + id + " successfully");
                                    expenseAdapter.editExpense(position, expense);
                                    popupWindow.dismiss();
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Log.d("HomeFragment", "Failed to edit expense");
                                    popupWindow.dismiss();
                                }
                            });
                } else {
                    // Add new expense
                    FirestoreManager.addExpense(userEmail, expense,
                            new FirestoreManager.FirestoreIdCallback() {
                                @Override
                                public void onComplete(String id) {
                                    Log.d("HomeFragment", "added new expense " + id + " successfully");
                                    expenseAdapter.addExpense(expense);
                                    popupWindow.dismiss();
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Log.d("HomeFragment", "Failed to add expense");
                                }
                            });
                }
            } else {
                Toast.makeText(context, "One or more fields are invalid", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCategoryPopupMenu(TextView categoryEditText, @NonNull final int[] selectedCategoryIndex,
                                         List<Category> categories, Category category) {

        //Can't do anything
        if(categories == null || categories.isEmpty()) return;

        // there is a category - in editing mode - setting the editText to be of that category
        if (category != null) {
            selectedCategoryIndex[0] = findCategoryIndex(categories, category); // where the category sit in the list
            if (selectedCategoryIndex[0] != -1) {
                categoryEditText.setText(category.getName());
            }
            else {
                throw new CategoryNotFoundException("Shouldn't be possible");
            }
        }

        // There isn't a category - in add category mode
        else {
            categoryEditText.setText(categories.get(0).getName()); // default category is the first in the list
            selectedCategoryIndex[0] = 0; // default selected category is the first in the list

        }

        // showing category popup menu when clicked
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

    private static Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth(); // 0-11
        int year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // return the index of where the category sits at inside categories, otherwise return -1.
    private static int findCategoryIndex(List<Category> categories, Category category) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(category.getId())) {
                return i;
            }
        }
        return -1;
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