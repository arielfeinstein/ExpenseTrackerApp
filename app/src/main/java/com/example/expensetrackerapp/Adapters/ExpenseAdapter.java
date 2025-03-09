package com.example.expensetrackerapp.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetrackerapp.Models.Expense;
import com.example.expensetrackerapp.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseViewHolder> {
    private List<Expense> expenseList;
    Date startingDate, endingDate; // all the expenses in the list are in that range
    // used to return the item clicked position in the list
    private OnItemClickListener itemClickListener;
    // onAmountListener.onAmountChanged() is triggered each time the total amount changes
    private onAmountListener onAmountListener;
    private double totalExpensesAmount;
    private final Context context;
    private final char currencySymbol;

    public ExpenseAdapter(Context context, char currencySymbol) {
        this(new ArrayList<>(), new Date(), new Date(), null, null, context, currencySymbol);
    }

    public ExpenseAdapter(@NonNull List<Expense> expenseList, @NonNull Date startingDate,
                          @NonNull Date endingDate, OnItemClickListener itemClickListener,
                          onAmountListener onAmountListener, Context context,
                          char currencySymbol) {
        this.expenseList = expenseList;
        totalExpensesAmount = getTotalExpensesAmount(expenseList);
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.itemClickListener = itemClickListener;
        this.onAmountListener = onAmountListener;
        this.context = context;
        this.currencySymbol = currencySymbol;
    }

    private static double getTotalExpensesAmount(@NonNull List<Expense> expenses) {
        double sum = 0;
        for (Expense expense : expenses) {
            sum += expense.getAmount();
        }
        return sum;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView, itemClickListener);
    }

    public double getTotalExpensesAmount() {
        return totalExpensesAmount;
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Retrieve necessary fields
        String description = expense.getDescription();
        Date date = expense.getTransactionDate();
        int categoryImgResourceId = getImageResourceId(expense.getCategory().getImgIndexInsideArraysXml());
        double amount = expense.getAmount();

        // Convert Date to appropriate text for textView
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String formattedDate = dateFormat.format(date);

        // Convert amount to String with two numbers after decimal point
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String formattedAmount = decimalFormat.format(amount) + " " + currencySymbol;

        // Apply texts and img
        holder.getExpenseDescriptionTV().setText(description);
        holder.getDateTV().setText(formattedDate);
        holder.getAmountTV().setText(formattedAmount);
        holder.getCategoryImg().setImageResource(categoryImgResourceId);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    /**
     * Add the expense to the list and maintain descending order. doesn't add the expense
     * if it is out of the date range.
     * @param expense the expense to be added
     */
    public void addExpense(Expense expense) {
        int index = getInsertIndex(expense);
        if (index < 0) return; // The expense is out of the date range or out of category filter
        addExpense(index, expense);
    }

    /**
     * private method to add an expense in at specified index to the list and notify
     * the insertion
     * @param index the index to insert the expense in the list
     * @param expense the expense to add
     */
    private void addExpense(int index, Expense expense) {
        expenseList.add(index, expense);
        totalExpensesAmount += expense.getAmount();
        onAmountListener.onAmountChanged(totalExpensesAmount);
        notifyItemInserted(index);
        notifyItemRangeChanged(index+1, expenseList.size() - (index+1));
    }

    /**
     * removing the expense at the corresponding index
     * @param index used as the index for removal
     */
    public void removeExpense(int index) {
        totalExpensesAmount -= expenseList.get(index).getAmount();
        expenseList.remove(index);
        onAmountListener.onAmountChanged(totalExpensesAmount);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, expenseList.size() - index);
    }

    /**
     * edits the expense at the given index. makes sure to move the expense to a different
     * location in the list depending on if the transactionDate has changed to maintain
     * descending order. Removes the expense from the list if it is out of the date range.
     * @param index the old expense in the list to be edited
     * @param expense the new edited expense to replace the one in the list
     */
    public void editExpense(int index, Expense expense) {
        // Check if the transaction date has not changed.
        if (expenseList.get(index).getTransactionDate().equals(expense.getTransactionDate()) && shouldExpenseBeInTheList(expense)) {
            // Update total expenses
            totalExpensesAmount -= expenseList.get(index).getAmount();
            expenseList.set(index, expense);
            totalExpensesAmount += expense.getAmount();
            onAmountListener.onAmountChanged(totalExpensesAmount);
            notifyItemChanged(index);
        }
        else {
            // Transaction date is changed. remove old expense and add new expense (if in range)
            removeExpense(index);
            addExpense(expense);
        }
    }

    /**
     * Replace the current Expense list with a new expense list, update the date range
     * @param expenseList the new expense list to set
     * @param startingDate the new starting date
     * @param endingDate the new ending date
     */
    @SuppressLint("NotifyDataSetChanged")
    public void replaceExpenseList(List<Expense> expenseList, Date startingDate, Date endingDate) {
        this.expenseList = expenseList;
        totalExpensesAmount = getTotalExpensesAmount(expenseList);
        if (onAmountListener != null) {
            onAmountListener.onAmountChanged(totalExpensesAmount);
        }
        notifyDataSetChanged();
        this.startingDate = startingDate;
        this.endingDate = endingDate;
    }

    public Expense getExpense(int position) {
        return this.expenseList.get(position);
    }

    /**
     * This method will calculate at what index should the expense sit in inside the expenseList
     * based on the transaction date and the category
     * if the expense transaction date is lower than startingDate or higherThan endingDate or is not
     * in the categories set of the shared preferences than -1 is returned indicating that the
     * expense should not be in the list.
     * @param expense the expense to calculate where it should sit at in the list
     * @return the index of where the expense should sit at, if it shouldn't be in the list, return
     * -1.
     */
    private int getInsertIndex(Expense expense) {
        // Determine if the expense should be in the list
        if (!shouldExpenseBeInTheList(expense)) {
            return -1;
        }

        // Determine where the expense should be in the list
        int index = Collections.binarySearch(expenseList, expense, Collections.reverseOrder());
        if (index >= 0) {
            /* There is already an expense in the list with the exact transaction date,
               it should sit right beside it */
            return index;
        }
        else {
            /* There isn't an expense in the list with the exact transaction date,
               calculate the index it should be in */
            index = -(index) - 1;
        }
        return index;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setOnAmountListener(onAmountListener onAmountListener) {
        this.onAmountListener = onAmountListener;
    }

    // Return true if the expense's transaction date is in the date range.
    private boolean isExpenseInRange(Expense expense) {
        return (expense.getTransactionDate().compareTo(startingDate) >= 0 &&
                expense.getTransactionDate().compareTo(endingDate) <= 0);
    }

    // Interface for getting the position of the item clicked on the recycler list
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Interface for getting the current total amount of the expense list
    public interface onAmountListener {
        void onAmountChanged(double amount);
    }

    // Get image resource from arrays.xml using the index
    private int getImageResourceId(int arraysXmlIndex) {
        int defaultResourceId = R.drawable.default_category; // if category img not found
        // Get access to arrays.xml
        try (TypedArray images = context.getResources().obtainTypedArray(R.array.image_array)) {
            int resourceId = images.getResourceId(arraysXmlIndex, -1); // -1 is default if out of bounds
            if (resourceId >= 0) {
                return resourceId;
            }
            else {
                return defaultResourceId;
            }
        } catch (Exception e) {
            Log.e("ExpenseAdapter", "getImageResourceId: failed to get images array", e);
        }
        return defaultResourceId;
    }

    /**
     * Determine whether an expense should be in the list based on the date range and the current
     * category filter settings.
     * @param expense check to see if the expense should be in the list
     * @return true if the expense should be in the list, else return false.
     */
    private boolean shouldExpenseBeInTheList(Expense expense) {
        // Get filtered categories' ids
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE);
        Set<String> filteredCategoriesIds =  sharedPreferences.getStringSet("filteredCategoriesIds", new HashSet<>());

        // Get expense's category's id
        String categoryId = expense.getCategory().getId();

        // DEBUG
        String TAG = "shouldExpenseBeInTheList";
        Log.d(TAG, "expense category id = " + categoryId);
        Log.d(TAG, "category set = " + filteredCategoriesIds);

        // Determine if the expense should be in the list
        if (filteredCategoriesIds.isEmpty()) {
            return isExpenseInRange(expense);
        }
        return isExpenseInRange(expense) && filteredCategoriesIds.contains(categoryId);

    }
}
