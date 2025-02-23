package com.example.expensetrackerapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class ButtonSelectionManager {
    private Button selectedButton;
    private final List<OnSelectionChangeListener> listeners;
    private final Context context;

    public ButtonSelectionManager(Context context) {
        this.context = context;
        this.listeners = new ArrayList<>();
    }

    /**
     * Sets up a group of buttons to act as a selection group.
     * Only one button can be selected at a time.
     * Notifies the button clicked to the OnSelectionChangeListener.onSelectionChanged(int selectedOption)
     *
     * @param buttons The buttons to include in the selection group.
     */
    public void setupButtonGroup(Button[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedButton != null) {
                        setUnselectedButton(selectedButton);
                    }
                    selectedButton = (Button) v;
                    setSelectedButton(selectedButton);

                    // Notify listeners of the new selection
                    notifyListeners(index);
                }
            });
        }

        // Set the default selected button (optional)
        if (buttons.length > 0) {
            selectedButton = buttons[0];
            setSelectedButton(selectedButton);
        }
    }

    private void setSelectedButton(Button button) {
        Drawable selectedBackground = ContextCompat.getDrawable(context, R.drawable.button_selected);
        button.setBackground(selectedBackground);
        button.setTextColor(ContextCompat.getColor(context, android.R.color.white));
    }

    private void setUnselectedButton(Button button) {
        Drawable unselectedBackground = ContextCompat.getDrawable(context, R.drawable.button_unselected);
        button.setBackground(unselectedBackground);
        button.setTextColor(ContextCompat.getColor(context, android.R.color.black));
    }

    /**
     * @param listener will be notified with the index of the button that is currently selected
     */
    public void addSelectionChangeListeners(@NonNull OnSelectionChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(int selectedOption) {
        for (OnSelectionChangeListener listener : listeners) {
            listener.onSelectionChanged(selectedOption);
        }
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedOption);
    }
}
