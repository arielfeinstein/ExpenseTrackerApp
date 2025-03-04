package com.example.expensetrackerapp;

import static com.example.expensetrackerapp.Helper.getImageResourceId;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Context context;
    private OnItemClickListener onItemClickListener;


    public CategoryAdapter(List<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
        this.onItemClickListener = null;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTV;
        ImageView categoryImgIV;

        public CategoryViewHolder(View view) {
            super(view);
            // Set views data
            this.categoryNameTV = view.findViewById(R.id.txtCategoryName);
            this.categoryImgIV = view.findViewById(R.id.imgCategoryIcon);

            // Listener for when the item is clicked - return the position
            view.setOnClickListener(v -> {
                if(onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_grid_item, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        int imgResourceId = getImageResourceId(category.getImgIndexInsideArraysXml(), context);
        String name = category.getName();

        holder.categoryImgIV.setImageResource(imgResourceId);
        holder.categoryNameTV.setText(name);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public Category getCategory(int position) {
        return categories.get(position);
    }

    public void addCategory(@NonNull Category category) {
        categories.add(category);
        notifyItemInserted(categories.size()-1);
    }

    public void removeCategory(int position) {
        categories.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, categories.size() - position);
    }

    public void editCategory(int position, Category modifiedCategory) {
        categories.set(position, modifiedCategory);
        notifyItemChanged(position);
    }
}
