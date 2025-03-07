package com.example.expensetrackerapp;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.VISIBLE;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class HomeFragment extends Fragment {
    /*
    expensesLiveData holds the expensesList. This wrapper is used in order to know when the
    expenses list is retrieved from the database.
     */
    private MutableLiveData<List<Category>> categoriesLiveData;
    private MutableLiveData<ExpenseAdapter> expenseAdapterLiveData;
    private ExpenseAdapter expenseAdapter;

    private final char currencySymbol = '₪';

    private String userEmail;
    private Date startingDate, endingDate;
    private Context context;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getContext();

        // clear all previous saved filters
        clearAllFilters();

        // init class fields
        //live data
        expenseAdapterLiveData = new MutableLiveData<>();
        categoriesLiveData = new MutableLiveData<>();
        // userEmail
        userEmail = FirebaseAuthManager.getUserEmail();

        // expenses - by default get the expenses for the current month
        Date[] firstAndLastDaysOfCurrentMonth = getFirstAndLastDayOfTheMonth();
        expenseAdapter =  new ExpenseAdapter(requireContext(), currencySymbol);
        updateRecyclerList(firstAndLastDaysOfCurrentMonth[0], firstAndLastDaysOfCurrentMonth[1],
                expenseAdapter, true);

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
        View btnFilter = view.findViewById(R.id.btnFilter);

        // set filter button onClick
        btnFilter.setOnClickListener(v -> showFilterPopup(rootView));

        // add custom item divider to the recycler view
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.recycler_item_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        
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

            // Set total expenses amount for the first time
            setTotalAmountToTV(expenseAdapter.getTotalExpensesAmount(), totalAmountTV);

            // Set total expense amount listener
            expenseAdapter.setOnAmountListener(totalAmount ->
                setTotalAmountToTV(totalAmount, totalAmountTV));

            // select custom date listener
            selectCustomDateLinearLayout.setOnClickListener(v ->
                showDateRangePicker(selectedDatesTextView, expenseAdapter));

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

    private void clearAllFilters() {
        // clear all filters from shared preferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void showFilterPopup(View view) {
        // Inflate the layout of the popup window
        @SuppressLint("InflateParams")
        View popupView = getLayoutInflater().inflate(R.layout.popup_filter, null);
        FrameLayout outerFrame = popupView.findViewById(R.id.popup_filter_outer_frame);
        LinearLayout content = popupView.findViewById(R.id.popup_filter_content);

        // Create the popup window
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        // Add enter animations
        content.setScaleX(0.8f);
        content.setScaleY(0.8f);
        content.setAlpha(0f);
        content.animate()
                .scaleX(1f).scaleY(1f)  // Scale to normal
                .alpha(1f)               // Fade in
                .setDuration(400)        // Duration 300ms
                .setInterpolator(new DecelerateInterpolator()) // Smooth effect
                .start();

        // Make outside touchable to dismiss
        outerFrame.setOnClickListener(v ->
                content.animate()
                        .scaleX(0.8f).scaleY(0.8f)  // Shrink
                        .alpha(0f)                  // Fade out
                        .setDuration(300)           // Duration 200ms
                        .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                        .withEndAction(popupWindow::dismiss) // Dismiss after animation
                        .start());
        content.setOnClickListener(v -> {}); // prevent dismiss when clicking inside the content

        // load all categories and initialize the ChipGroup
        initChipGroup(popupWindow, popupView, content);

        // Center the popup with slight offset to avoid it feeling static
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private void initChipGroup(PopupWindow popupWindow, View popupView, LinearLayout content) {
        ChipGroup chipGroup = popupView.findViewById(R.id.chipGroup);
        Button btnShowResults = popupView.findViewById(R.id.btnShowResults);

        // fetch categories
        String userEmail = FirebaseAuthManager.getUserEmail();
        FirestoreManager.getCategories(userEmail, new FirestoreManager.FirestoreListCallback<Category>() { //todo: use categories from onViewCreated
            @Override
            public void onComplete(List<Category> items) {
                Set<String> filteredCategoriesIds = getFilteredCategories();

                // add chips to chipGroup
                for (Category category : items) {
                    Chip chip = new Chip(context);
                    chip.setText(category.getName());
                    chip.setCheckable(true);
                    chip.setChipBackgroundColorResource(R.color.chip_selector);

                    // restore selection state
                    if (filteredCategoriesIds.contains(category.getId())) {
                        chip.setChecked(true);
                    }

                    // update selection state when chip is checked
                    chip.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                        Set<String> updatedFilteredCategoriesIds = getFilteredCategories();
                        if (isChecked) {
                            updatedFilteredCategoriesIds.add(category.getId());
                        } else {
                            updatedFilteredCategoriesIds.remove(category.getId());
                        }
                        saveFilteredCategories(updatedFilteredCategoriesIds);
                    }));

                    chipGroup.addView(chip);
                }


                // handle show results button click
                btnShowResults.setOnClickListener(v -> {
                    Set<String> categoriesIds = getFilteredCategories();
                    String userEmail = FirebaseAuthManager.getUserEmail();
                    if (!categoriesIds.isEmpty()) {
                        FirestoreManager.getExpenses(userEmail, new ArrayList<>(categoriesIds), startingDate, endingDate, new FirestoreManager.FirestoreListCallback<Expense>() {
                            @Override
                            public void onComplete(List<Expense> items) {
                                expenseAdapter.replaceExpenseList(items, startingDate, endingDate);
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    } else {
                        // no categories to filter by - fetch all expenses
                        FirestoreManager.getExpenses(userEmail, startingDate, endingDate, new FirestoreManager.FirestoreListCallback<Expense>() {
                            @Override
                            public void onComplete(List<Expense> items) {
                                expenseAdapter.replaceExpenseList(items, startingDate, endingDate);
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    }
                    // dismiss the popup
                    content.animate()
                            .scaleX(0.8f).scaleY(0.8f)  // Shrink
                            .alpha(0f)                  // Fade out
                            .setDuration(300)           // Duration 200ms
                            .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                            .withEndAction(popupWindow::dismiss) // Dismiss after animation
                            .start();
                });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void saveFilteredCategories(Set<String> filteredCategoriesIds) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("filteredCategoriesIds", filteredCategoriesIds);
        editor.apply();
    }

    private Set<String> getFilteredCategories() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE);
        return sharedPreferences.getStringSet("filteredCategoriesIds", new HashSet<>());
    }

    private void showDateRangePicker(TextView selectedDateRangeTextView, ExpenseAdapter expenseAdapter) {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select dates");

        final MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            // Get start and end dates
            Date startDate = new Date(selection.first);
            Date endDate = new Date(selection.second);
            Calendar startCal = Calendar.getInstance(Locale.US);
            startCal.setTime(startDate);
            setStartOfDay(startCal);
            startDate = startCal.getTime(); // startDate is now the start of the day

            Calendar endCal = Calendar.getInstance(Locale.US);
            endCal.setTime(endDate);
            setEndOfDay(endCal);
            endDate = endCal.getTime(); // endDate is now the end of the day.

            // save dates
            this.startingDate = startDate;
            this.endingDate = endDate;

            // Update the expenses list
            updateRecyclerList(startDate, endDate, expenseAdapter, false);

            // update the selectDateRangeTextView with the new date range
            updateDateRangeTextView(startDate, endDate, selectedDateRangeTextView);
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
        Set<String> filteredCategoriesIds = getFilteredCategories();

        if (!filteredCategoriesIds.isEmpty()) {
            FirestoreManager.getExpenses(userEmail, new ArrayList<>(filteredCategoriesIds), startingDate, endingDate, new FirestoreManager.FirestoreListCallback<Expense>() {
                @Override
                public void onComplete(List<Expense> items) {
                    expenseAdapter.replaceExpenseList(items, startingDate, endingDate);
                    if(postLiveData) expenseAdapterLiveData.postValue(expenseAdapter);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        } else {
            // no categories to filter by - fetch all expenses
            FirestoreManager.getExpenses(userEmail, startingDate, endingDate, new FirestoreManager.FirestoreListCallback<Expense>() {
                @Override
                public void onComplete(List<Expense> items) {
                    expenseAdapter.replaceExpenseList(items, startingDate, endingDate);
                    if(postLiveData) expenseAdapterLiveData.postValue(expenseAdapter);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("HomeFragment", "Failed to get list of expenses");
                }
            });
        }
    }

    // Return a string dd/MM/yyyy - dd/MM/yyyy starting date - ending date string from long millis
    private String formatDateRange(Date startingDate, Date endingDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedStartingDate = sdf.format(startingDate);
        String formattedEndingDate = sdf.format(endingDate);
        return formattedStartingDate + " - " + formattedEndingDate;
    }

    private void showPopupWindow(@NonNull View anchorView, @NonNull ExpenseAdapter expenseAdapter,
                                 List<Category> categories, int position) {
        // Determine if we're adding or editing
        boolean isEditing = position != -1;

        // Inflate the layout of the popup window
        @SuppressLint("InflateParams")
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_expense, null);
        FrameLayout outerFrame = popupView.findViewById(R.id.popup_add_expense_outer_frame);
        LinearLayout content = popupView.findViewById(R.id.popup_add_expense_content);

        // Create the popup window with fixed width for better appearance
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        // Get the current expense if editing
        final Expense currentExpense = isEditing ? expenseAdapter.getExpense(position) : null;

        // Set up popup content
        handleExpensePopup(popupView, popupWindow, categories, expenseAdapter, position, currentExpense, isEditing);

        // Add visual enhancements to make the popup stand out

        // 1. Set a higher elevation for stronger shadow
        popupWindow.setElevation(24f);

        // 2. Add enter/exit animations
        content.setScaleX(0.8f);
        content.setScaleY(0.8f);
        content.setAlpha(0f);
        content.animate()
                .scaleX(1f).scaleY(1f)  // Scale to normal
                .alpha(1f)               // Fade in
                .setDuration(400)        // Duration 300ms
                .setInterpolator(new DecelerateInterpolator()) // Smooth effect
                .start();

        // 3. Add dim effect to background
        View rootView = getActivity().getWindow().getDecorView().getRootView();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) rootView.getLayoutParams();
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;
        getActivity().getWindow().setAttributes(params);

        // 4. When popup is dismissed, remove dim effect
        popupWindow.setOnDismissListener(() -> {
            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            getActivity().getWindow().setAttributes(params);
        });

        // Make outside touchable to dismiss
        outerFrame.setOnClickListener(v ->
            content.animate()
                    .scaleX(0.8f).scaleY(0.8f)  // Shrink
                    .alpha(0f)                  // Fade out
                    .setDuration(300)           // Duration 200ms
                    .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                    .withEndAction(popupWindow::dismiss) // Dismiss after animation
                    .start());
        content.setOnClickListener(v -> {}); // prevent dismiss when clicking inside the content

        // Center the popup with slight offset to avoid it feeling static
        int yOffset = -50; // Slight upward shift for better visual appeal
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, yOffset);
    }

    @SuppressLint("SetTextI18n")
    private void handleExpensePopup(@NonNull View popupView, final PopupWindow popupWindow,
                                    List<Category> categories, @NonNull ExpenseAdapter expenseAdapter,
                                    int position, Expense currentExpense, boolean isEditing) {
        // Access views
        TextView titleTextView = popupView.findViewById(R.id.popup_add_expense_title_tv);
        DatePicker datePicker = popupView.findViewById(R.id.popup_add_expense_date_picker);
        EditText descriptionEditText = popupView.findViewById(R.id.popup_add_expense_description_et);
        TextView categoryEditText = popupView.findViewById(R.id.popup_add_expense_category_et);
        EditText amountEditText = popupView.findViewById(R.id.popup_add_expense_amount_et);
        Button actionButton = popupView.findViewById(R.id.popup_add_expense_btn);
        Button removeExpenseBtn = popupView.findViewById(R.id.popup_add_expense_remove_btn);
        LinearLayout content = popupView.findViewById(R.id.popup_add_expense_content);

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
            Calendar calendar = Calendar.getInstance(Locale.US);
            calendar.setTime(currentExpenseDate);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            datePicker.updateDate(year, month, day);

            // Set description
            descriptionEditText.setText(currentExpense.getDescription());
            // Set amount
            String amountFormattedString = String.format(Locale.US,"%.2f", currentExpense.getAmount()); // amount with 2 point precision and ₪
            amountEditText.setText(amountFormattedString);
            //Set category
            currentCategory = currentExpense.getCategory(); // will be displayed as the default category inside the category popupmenu.
            // Note: Category setting would be handled in handleCategoryPopupMenu


            // Set remove button listener
            removeExpenseBtn.setOnClickListener(view ->
                FirestoreManager.removeExpense(userEmail, currentExpense.getId(), new FirestoreManager.FirestoreIdCallback() {
                    @Override
                    public void onComplete(String id) {
                        expenseAdapter.removeExpense(position);
                        content.animate()
                                .scaleX(0.8f).scaleY(0.8f)  // Shrink
                                .alpha(0f)                  // Fade out
                                .setDuration(300)           // Duration 200ms
                                .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                                .withEndAction(popupWindow::dismiss) // Dismiss after animation
                                .start();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Failed to remove expense, try again later", Toast.LENGTH_SHORT).show();
                    }
                }));
        } else {
            // Setup for adding
            titleTextView.setText("New Expense");
            actionButton.setText("Add Expense");
            removeExpenseBtn.setVisibility(View.GONE);

            // Set initial date
            Calendar calendar = Calendar.getInstance(Locale.US);
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
            String amountString = amountEditText.getText().toString();
            // Fetch amount
            double amountDouble; // will have positive value if amountString is valid
            try {
                amountDouble = Double.parseDouble(amountString);
            }
            catch (Exception e) {
                amountDouble = -1;
            }

            // Validate input
            if (!description.isEmpty() && selectedCategory != null && amountDouble > 0) {
                Expense expense = new Expense(amountDouble, selectedCategory, expenseDate, description);
                if (isEditing) {
                    // Edit existing expense
                    String existingExpenseId = currentExpense.getId();
                    expense.setId(existingExpenseId); // Set the expense id of firestore so that it is up to date in the expense adapter
                    FirestoreManager.editExpense(userEmail, existingExpenseId, expense,
                            new FirestoreManager.FirestoreIdCallback() {
                                @Override
                                public void onComplete(String id) {
                                    Log.d("HomeFragment", "edited expense " + id + " successfully");
                                    expenseAdapter.editExpense(position, expense);
                                    content.animate()
                                            .scaleX(0.8f).scaleY(0.8f)  // Shrink
                                            .alpha(0f)                  // Fade out
                                            .setDuration(300)           // Duration 200ms
                                            .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                                            .withEndAction(popupWindow::dismiss) // Dismiss after animation
                                            .start();
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Log.d("HomeFragment", "Failed to edit expense");
                                    content.animate()
                                            .scaleX(0.8f).scaleY(0.8f)  // Shrink
                                            .alpha(0f)                  // Fade out
                                            .setDuration(300)           // Duration 200ms
                                            .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                                            .withEndAction(popupWindow::dismiss) // Dismiss after animation
                                            .start();
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
                                    content.animate()
                                            .scaleX(0.8f).scaleY(0.8f)  // Shrink
                                            .alpha(0f)                  // Fade out
                                            .setDuration(300)           // Duration 200ms
                                            .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                                            .withEndAction(popupWindow::dismiss) // Dismiss after animation
                                            .start();
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
        categoryEditText.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, categoryEditText);

            // Dynamically add menu items
            for (int i = 0; i < categories.size(); i++) {
                popupMenu.getMenu().add(0, i, 0, categories.get(i).getName()); // (groupId, itemId, order, title)
            }

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int categoryIndex = menuItem.getItemId(); // Get the dynamically assigned categoryIndex
                String selectedItem = categories.get(categoryIndex).getName();
                categoryEditText.setText(selectedItem);
                selectedCategoryIndex[0] = categoryIndex;
                return true;
            });
            popupMenu.show();
        });
    }

    private static Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth(); // 0-11
        int year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance(Locale.US);
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

    // Return a Date array of size two: [first day, last day] for the current month,
    // with start and end of day times.
    public static Date[] getFirstAndLastDayOfTheMonth() {
        Calendar cal = Calendar.getInstance(Locale.US);

        // Get the first day of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        setStartOfDay(cal);
        Date firstDayOfMonth = cal.getTime();

        // Get the last day of the month
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        setEndOfDay(cal);
        Date lastDayOfMonth = cal.getTime();

        return new Date[] { firstDayOfMonth, lastDayOfMonth };
    }

    private static void setStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static void setEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    // Set a formatted amount "0.00 <currency_symbol>" inside the textView
    private void setTotalAmountToTV(double amount, @NonNull TextView amountTV) {
        String amountFormattedString = String.format(Locale.US,"%.2f", amount) + " " + currencySymbol;
        amountTV.setText(amountFormattedString);
    }
}