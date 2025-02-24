package com.example.expensetrackerapp;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.Locale;
import java.util.Objects;

public class CurrencyEditText extends androidx.appcompat.widget.AppCompatEditText {
    private static final String SHEKEL_SYMBOL = " ₪";
    private boolean isUpdating = false;

    public CurrencyEditText(Context context) {
        super(context);
        init();
    }

    public CurrencyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurrencyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Set input type to decimal numbers
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Add text change listener
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    isUpdating = true;

                    String value = s.toString();

                    // Remove the shekel symbol if it exists
                    if (value.endsWith(SHEKEL_SYMBOL)) {
                        value = value.substring(0, value.length() - SHEKEL_SYMBOL.length());
                    }

                    // Handle empty or invalid input
                    if (value.isEmpty() || value.equals(".")) {
                        s.clear();
                        isUpdating = false;
                        return;
                    }

                    try {
                        // Parse and validate the number
                        double amount = Double.parseDouble(value);
                        if (amount < 0) {
                            s.clear();
                        } else {
                            // Format the number with two decimal places
                            String formatted = String.format(Locale.US, "%.2f", amount);
                            // Add the shekel symbol
                            formatted += SHEKEL_SYMBOL;

                            if (!formatted.equals(s.toString())) {
                                setText(formatted);
                                setSelection(formatted.length() - SHEKEL_SYMBOL.length());
                            }
                        }
                    } catch (NumberFormatException e) {
                        s.clear();
                    }

                    isUpdating = false;
                }
            }
        });
    }

    // Method to get the current amount without the currency symbol
    public double getAmount() {
        String value = Objects.requireNonNull(getText()).toString();
        if (value.endsWith(SHEKEL_SYMBOL)) {
            value = value.substring(0, value.length() - SHEKEL_SYMBOL.length());
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
