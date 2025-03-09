package com.example.expensetrackerapp.Adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetrackerapp.R;

public class ExpenseViewHolder extends RecyclerView.ViewHolder {
    private final TextView expenseDescriptionTV, dateTV, amountTV;
    private final ImageView categoryImg;

    public ExpenseViewHolder(View view, ExpenseAdapter.OnItemClickListener itemClickListener) {
        super(view);
        expenseDescriptionTV = view.findViewById(R.id.expense_item_expense_description_tv);
        dateTV = view.findViewById(R.id.expense_item_date_tv);
        amountTV = view.findViewById(R.id.expense_item_amount_tv);
        categoryImg = view.findViewById(R.id.expense_item_iv);
        // used to return the position of the view in the list when clicked

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
