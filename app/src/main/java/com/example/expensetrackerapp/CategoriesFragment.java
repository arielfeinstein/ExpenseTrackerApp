package com.example.expensetrackerapp;

import static android.view.View.VISIBLE;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoriesFragment extends Fragment {
    private MutableLiveData<CategoryAdapter> categoryAdapterMutableLiveData;
    private String userEmail;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userEmail = FirebaseAuthManager.getUserEmail();
        this.categoryAdapterMutableLiveData = new MutableLiveData<>();
        FirestoreManager.getCategories(userEmail, new FirestoreManager.FirestoreListCallback<Category>() {
            @Override
            public void onComplete(List<Category> categories) {
                categoryAdapterMutableLiveData.postValue(new CategoryAdapter(categories, requireContext()));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to retrieve categories, try again later",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int GRID_COLUMN_AMOUNT = 2;

        // Access views
        GridView gridView = view.findViewById(R.id.gridViewCategories);
        FloatingActionButton addCategoryBtn = view.findViewById(R.id.btnAddCategory);
        View rootView = requireActivity().findViewById(android.R.id.content); // The view of the entire activity

        //Setting recyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext().getApplicationContext(), GRID_COLUMN_AMOUNT);

        categoryAdapterMutableLiveData.observe(getViewLifecycleOwner(), categoryAdapter -> {
            // retrieving category process is complete.

            // attaching adapter to gridView
            gridView.setAdapter(categoryAdapter);

            // Set category item click listener
            gridView.setOnItemClickListener((parent, itemView, position, id) -> {
                showPopupWindow(rootView,categoryAdapter,position);
            });

            // Set add category button click listener
            addCategoryBtn.setOnClickListener(btn -> {
                showPopupWindow(rootView, categoryAdapter, -1); // -1 indicating adding new category
            });
        });

    }

    public void showIconSelectionBottomSheet(@NonNull ImageView categoryIconIV,
                                             @NonNull Category category,
                                             final int[] imgIndexArr) {
        Context context = requireContext();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_icon_selection, null);
        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.bottom_sheet_icon_selection_recycler_view);

        // Setup grid layout for icons
        final int COLUMN_AMOUNT = 4;
        GridLayoutManager layoutManager = new GridLayoutManager(context, COLUMN_AMOUNT);
        recyclerView.setLayoutManager(layoutManager);

        // Setup Adapter
        CategoryIconAdapter categoryIconAdapter = new CategoryIconAdapter(context);

        // Setup icon listener
        categoryIconAdapter.setOnItemClickListener(position -> {
            // Set img index to category instance
            int imgIndex = categoryIconAdapter.getImgIndex(position);
            category.setImgIndexInsideArraysXml(imgIndex);

            // Set imgIndexArr[0] value
            imgIndexArr[0] = imgIndex;

            // Set image view
            int imgResourceId = Helper.getImageResourceId(imgIndex, context);
            categoryIconIV.setImageResource(imgResourceId);

            // Done - dismiss
            bottomSheetDialog.dismiss();
        });

        // Attach adapter
        recyclerView.setAdapter(categoryIconAdapter);

        // Showing dialog
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void showPopupWindow(@NonNull View anchorView, @NonNull CategoryAdapter categoryAdapter,
                                 int position) {
        // Determine if we're adding or editing
        boolean isEditing = position != -1;

        // Inflate the layout of the popup window
        View popupView = getLayoutInflater().inflate(R.layout.popup_category, null);
        FrameLayout outerFrame = popupView.findViewById(R.id.popup_add_expense_outer_frame); //todo: maybe refactor outer_frame to be general
        LinearLayout content = popupView.findViewById(R.id.popup_category_content);

        // Create the popup window with fixed width for better appearance
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        // Get the current expense if editing
        final Category currentCategory = isEditing ? categoryAdapter.getCategory(position) : null;

        //DEBUG
        final String TAG = "debug";
        if (currentCategory == null) {
            Log.d(TAG, "currentCategory: null");
        }
        else {
            Log.d(TAG, "currentCategory: " + currentCategory);
        }
        Log.d(TAG, "position: " + position);

        // Set up popup content
        handleCategoryPopup(categoryAdapter, currentCategory, position, popupView, popupWindow);

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
        outerFrame.setOnClickListener(v -> {
            content.animate()
                    .scaleX(0.8f).scaleY(0.8f)  // Shrink
                    .alpha(0f)                  // Fade out
                    .setDuration(300)           // Duration 200ms
                    .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                    .withEndAction(popupWindow::dismiss) // Dismiss after animation
                    .start();
        });
        content.setOnClickListener(v -> {}); // prevent dismiss when clicking inside the content

        // Center the popup with slight offset to avoid it feeling static
        int yOffset = -50; // Slight upward shift for better visual appeal
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, yOffset);
    }

    private void handleCategoryPopup(@NonNull CategoryAdapter categoryAdapter,
                                     Category currentCategory, int position, @NonNull View popupView,
                                     @NonNull PopupWindow popupWindow) {
        boolean isEditing = currentCategory != null;

        if (isEditing && position == -1) {
            Log.d("CategoriesFragment", "handleCategoryPopup: editing an expense but position is -1");
            return;
        }

        // Access to views
        TextView titleTV = popupView.findViewById(R.id.popup_category_title_tv);
        ImageView iconIV = popupView.findViewById(R.id.popup_category_icon_iv);
        EditText nameET = popupView.findViewById(R.id.popup_category_name_et);
        Button okBtn = popupView.findViewById(R.id.popup_category_ok_btn); // add/edit button
        Button removeBtn = popupView.findViewById(R.id.popup_category_remove_btn);

        // The index of the currentCategory icon inside arrays.xml
        final int[] imgIndexArr = {-1}; // if remains -1 something went wrong

        // Set views
        if (isEditing) {
            // Editing currentCategory
            // Set title
            titleTV.setText("Edit Category");
            // Set currentCategory icon
            int imgResourceId = Helper.getImageResourceId(currentCategory.getImgIndexInsideArraysXml(),
                    requireContext());
            iconIV.setImageResource(imgResourceId);
            // Set currentCategory name
            nameET.setText(currentCategory.getName());
            // Set ok button text
            okBtn.setText("Edit");
            // Show remove button
            removeBtn.setVisibility(VISIBLE);
            // Set the icon index of the currentCategory
            imgIndexArr[0] = currentCategory.getImgIndexInsideArraysXml();

            // Listener for remove button
            removeBtn.setOnClickListener(btn -> {
                FirestoreManager.removeCategory(userEmail, currentCategory.getId(), new FirestoreManager.FirestoreIdCallback() {
                    @Override
                    public void onComplete(String id) {
                        // Successfully removed currentCategory from firestore
                        categoryAdapter.removeCategory(position);
                        dismissPopupWithAnimation(popupView, popupWindow);

                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof ExpenseWithCategoryExistsException) {
                            // There are expenses that have this currentCategory
                            String toastMsg = "Failed to remove, There are expenses that have this currentCategory";
                            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // Other unexpected error
                            String toastMsg = "Failed to remove currentCategory, try again later";
                            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        }
        else {
            // Adding currentCategory
            // Set title
            titleTV.setText("Add Category");
            // Set currentCategory icon
            int defaultImgIndex = Helper.getDefaultImgIndex(requireContext());
            int imgResourceId = Helper.getImageResourceId(defaultImgIndex,
                    requireContext());
            iconIV.setImageResource(imgResourceId);
            // Set the index of the currentCategory
            imgIndexArr[0] = defaultImgIndex;
        }

        // Will be used for adding / modifying
        Category newCategory = new Category();
        newCategory.setImgIndexInsideArraysXml(imgIndexArr[0]);

        // Icon ImageView listener
        iconIV.setOnClickListener(iconView -> {
            showIconSelectionBottomSheet(iconIV, newCategory, imgIndexArr);
        });

        // Ok button listener (Add currentCategory or edit currentCategory)
        okBtn.setOnClickListener(btn -> {
            // Validate imgIndexArr
            if (imgIndexArr[0] == -1) {
                Log.d("CategoriesFragment", "handleCategoryPopup: Failed to get imgIndexArr");
            }

            //Extract entered name
            String newCategoryName = nameET.getText().toString();

            // Validate input
            if (newCategoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            }
            else {
                // All is valid
                newCategory.setName(newCategoryName);
                if (isEditing) {
                    newCategory.setId(currentCategory.getId()); // Need to store the firestore id in the new category
                    if (newCategoryName.equals(currentCategory.getName()) && imgIndexArr[0] == currentCategory.getImgIndexInsideArraysXml()) {
                        // Nothing is changed, dismiss
                        dismissPopupWithAnimation(popupView, popupWindow);
                    }
                    else if (newCategoryName.equals(currentCategory.getName())) {
                        // only icon changed
                        FirestoreManager.editCategoryIconIndex(userEmail, currentCategory.getId(), imgIndexArr[0], new FirestoreManager.FirestoreIdCallback() {
                            @Override
                            public void onComplete(String id) {
                                // Successfully edited icon
                                categoryAdapter.editCategory(position,newCategory);
                                dismissPopupWithAnimation(popupView, popupWindow);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Failed to edit icon
                                String toastMsg = "Failed to edit icon, try again later";
                                Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        // Editing both icon and name
                        FirestoreManager.editCategory(userEmail, currentCategory.getId(), newCategory,
                                new FirestoreManager.FirestoreIdCallback() {
                                    @Override
                                    public void onComplete(String id) {
                                        // Successfully edited category
                                        categoryAdapter.editCategory(position, newCategory);
                                        dismissPopupWithAnimation(popupView, popupWindow);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Failed to edit category
                                        if (e instanceof CategoryAlreadyExistsException) {
                                            String toastMsg = "The name for the category is already taken";
                                            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Unexpected error while editing
                                            String toastMsg = "Failed to edit category, try again later";
                                            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }

                else {
                    // Adding new expense
                    FirestoreManager.addCategories(userEmail, List.of(newCategory),
                            new FirestoreManager.FirestoreListCallback<String>() {
                                @Override
                                public void onComplete(List<String> idList) {
                                    // Successfully added category
                                    newCategory.setId(idList.get(0)); // Set the id firestore allocated
                                    categoryAdapter.addCategory(newCategory);
                                    dismissPopupWithAnimation(popupView, popupWindow);

                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // Failed to add category
                                    if (e instanceof CategoryAlreadyExistsException) {
                                        String toastMsg = "The selected name already exist, choose a different name";
                                        Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        // Unexpected error
                                        String toastMsg = " Failed to add category, try again later";
                                        Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private static void dismissPopupWithAnimation(@NonNull View popupView, @NonNull PopupWindow popupWindow) {
        popupView.animate()
                .scaleX(0.8f).scaleY(0.8f)  // Shrink
                .alpha(0f)                  // Fade out
                .setDuration(300)           // Duration 200ms
                .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                .withEndAction(popupWindow::dismiss) // Dismiss after animation
                .start();
    }
}
