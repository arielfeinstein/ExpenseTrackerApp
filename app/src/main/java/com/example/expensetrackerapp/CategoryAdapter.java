package com.example.expensetrackerapp;

import static com.example.expensetrackerapp.Helper.getImageResourceId;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {

    private List<Category> categories;
    private Context context;


    public CategoryAdapter(List<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
    }

    public Category getCategory(int position) {
        return categories.get(position);
    }

    public void addCategory(@NonNull Category category) {
        categories.add(category);
        notifyDataSetChanged();
    }

    public void removeCategory(int position) {
        categories.remove(position);
        notifyDataSetChanged();
    }

    public void editCategory(int position, Category modifiedCategory) {
        categories.set(position, modifiedCategory);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.category_grid_item, parent, false);
        }

        ImageView imgCategoryIcon = convertView.findViewById(R.id.imgCategoryIcon);
        TextView txtCategoryName = convertView.findViewById(R.id.txtCategoryName);

        int imgIndex = categories.get(position).getImgIndexInsideArraysXml();
        int imgResource = Helper.getImageResourceId(imgIndex, context);

        imgCategoryIcon.setImageResource(imgResource);
        txtCategoryName.setText(categories.get(position).getName());

        return convertView;
    }

}
