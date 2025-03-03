package com.example.expensetrackerapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ExpenseViewHolder extends RecyclerView.ViewHolder {
    private final TextView expenseDescriptionTV, dateTV, amountTV;
    private ImageView categoryImg;
    
    // used to return the position of the view in the list when clicked
    private ExpenseAdapter.OnItemClickListener itemClickListener;

    public ExpenseViewHolder(View view, ExpenseAdapter.OnItemClickListener itemClickListener) {
        super(view);
        expenseDescriptionTV = view.findViewById(R.id.expense_item_expense_description_tv);
        dateTV = view.findViewById(R.id.expense_item_date_tv);
        amountTV = view.findViewById(R.id.expense_item_amount_tv);
        categoryImg = view.findViewById(R.id.expense_item_iv);
        this.itemClickListener = itemClickListener;

        // Listener for when the item is clicked - return the position
        view.setOnClickListener(v -> {
            if(itemClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position);
                }
            }
        });
    }

    public TextView getExpenseDescriptionTV() {
        return expenseDescriptionTV;
    }

    public TextView getDateTV() {
        return dateTV;
    }

    public TextView getAmountTV() {
        return amountTV;
    }

    public ImageView getCategoryImg() {
        return categoryImg;
    }
}
