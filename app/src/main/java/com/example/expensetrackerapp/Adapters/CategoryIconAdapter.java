package com.example.expensetrackerapp.Adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetrackerapp.Helper;
import com.example.expensetrackerapp.OnItemClickListener;
import com.example.expensetrackerapp.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryIconAdapter extends RecyclerView.Adapter<CategoryIconAdapter.CategoryIconViewHolder> {
    private OnItemClickListener onItemClickListener;
    private final Context context;
    private final List<Integer> categoryIconIndexes;

    public CategoryIconAdapter(Context context) {
        this.context = context;
        this.categoryIconIndexes = getCategoryIconIndexes(context);
        this.onItemClickListener = null;
    }

    @NonNull
    @Override
    public CategoryIconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_icon_item, parent, false);
        return new CategoryIconAdapter.CategoryIconViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryIconViewHolder holder, int position) {
        int imgIndex = categoryIconIndexes.get(position);
        int imgResourceId = Helper.getImageResourceId(position, context);
        holder.icon.setImageResource(imgResourceId);
    }

    @Override
    public int getItemCount() {
        return categoryIconIndexes.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public int getImgIndex(int position) {
        return categoryIconIndexes.get(position);
    }

    public class CategoryIconViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        CategoryIconViewHolder(@NonNull View view) {
            super(view);
            icon = view.findViewById(R.id.category_icon_item_iv);

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

    /**
     * Retrieves a list of integer indexes corresponding to the images defined in the R.array.image_array resource.
     * This method is designed to provide a sequential list of indexes that can be used to access the images
     * within the TypedArray obtained from the image_array resource.
     *
     * @param context The Context used to access resources.
     * @return A List of Integer representing the indexes of the images, or an empty List if an error occurs.
     */
    private static List<Integer> getCategoryIconIndexes(Context context) {
        // Initialize an empty ArrayList to store the image indexes.
        List<Integer> categoryIconIndexesList = new ArrayList<>();

        // Use a try-with-resources block to automatically close the TypedArray after use.
        try (TypedArray images = context.getResources().obtainTypedArray(R.array.image_array)) {
            // Create an Integer array with the same length as the TypedArray.
            Integer[] indexesArr = new Integer[images.length()];

            // Populate the indexesArr with sequential integers, representing the indexes of the images.
            for (int i = 0; i < images.length(); i++) {
                indexesArr[i] = i;
            }

            // Convert the Integer array to a List and assign it to categoryIconIndexesList.
            categoryIconIndexesList = Arrays.asList(indexesArr);
        }
        // Catch any exceptions that occur while accessing the TypedArray or creating the List.
        catch (Exception e) {
            // Log an error message if the image_array resource cannot be accessed.
            Log.e("CategoryIconAdapter", "getCategoryIconIndexes: failed to get image_array", e);
        }

        // Return the List of image indexes. If an exception occurred, it will be an empty List.
        return categoryIconIndexesList;
    }
}

