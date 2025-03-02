package com.example.expensetrackerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseViewHolder> {
    private List<Expense> expenseList;
    Date startingDate, endingDate; // all the expenses in the list are in that range
    // used to return the item clicked position in the list
    private OnItemClickListener itemClickListener;
    // onAmountListener.onAmountChanged() is triggered each time the total amount changes
    private onAmountListener onAmountListener;
    private double totalExpensesAmount;

    public ExpenseAdapter() {
        this(new ArrayList<>(), new Date(), new Date(), null, null);
    }

    public ExpenseAdapter(@NonNull List<Expense> expenseList, @NonNull Date startingDate,
                          @NonNull Date endingDate, OnItemClickListener itemClickListener,
                          onAmountListener onAmountListener) {
        this.expenseList = expenseList;
        totalExpensesAmount = getTotalExpensesAmount(expenseList);
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.itemClickListener = itemClickListener;
        this.onAmountListener = onAmountListener;
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

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Retrieve necessary fields
        String description = expense.getDescription();
        Date date = expense.getTransactionDate();
        int categoryImg = expense.getCategory().getImageResourceId();
        double amount = expense.getAmount();

        // Convert Date to appropriate text for textView
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String formattedDate = dateFormat.format(date);

        // Convert amount to String with two numbers after decimal point
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String formattedAmount = decimalFormat.format(amount);

        // Apply texts and img
        holder.getExpenseDescriptionTV().setText(description);
        holder.getDateTV().setText(formattedDate);
        holder.getAmountTV().setText(formattedAmount);
        holder.getCategoryImg().setImageResource(categoryImg);
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
        if (index < 0) return; // The expense is out of the date range
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
        if (expenseList.get(index).getTransactionDate().equals(expense.getTransactionDate())) {
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
     * based on the transaction date.
     * if the expense transaction date is lower than startingDate or higherThan endingDate
     * than -1 is returned indicating that the expense should not be in the list.
     * @param expense the expense to calculate where it should sit at in the list
     * @return the index of where the expense should sit at, if it shouldn't be in the list, return
     * -1.
     */
    private int getInsertIndex(Expense expense) {
        // Determine if the expense should be in the list
        if (!isExpenseInRange(expense)) {
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
        return  (expense.getTransactionDate().compareTo(startingDate) >= 0 &&
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
}
