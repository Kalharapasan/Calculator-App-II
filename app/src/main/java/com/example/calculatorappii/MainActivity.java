package com.example.calculatorappii;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingInflatedId")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDisplay, tvHistory, tvMemoryIndicator;
    private String currentInput = "0";
    private String operator = "";
    private String firstOperand = "";
    private boolean isNewInput = true;
    private boolean isDecimalAdded = false;
    private boolean isResultDisplayed = false;
    private double memoryValue = 0.0;
    private boolean hasMemory = false;
    private List<String> calculationHistory = new ArrayList<>();
    private String lastOperation = "";

    // Advanced calculation support
    private MathContext mathContext = new MathContext(15, RoundingMode.HALF_UP);
    private DecimalFormat displayFormatter = new DecimalFormat("#,###.##########");
    private DecimalFormat scientificFormatter = new DecimalFormat("0.#####E0");

    // Preferences for settings
    private SharedPreferences preferences;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);
        tvMemoryIndicator = findViewById(R.id.tvMemoryIndicator);

        // Initialize preferences and vibrator
        preferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Load saved memory value
        memoryValue = Double.longBitsToDouble(preferences.getLong("memory_value", 0));
        hasMemory = preferences.getBoolean("has_memory", false);

        // Initialize all buttons and set click listeners
        initializeButtons();
        updateDisplay();
        updateMemoryIndicator();

        // Add long click listeners for advanced features
        addLongClickListeners();
    }

    private void initializeButtons() {
        // Number buttons
        findViewById(R.id.btn0).setOnClickListener(this);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
        findViewById(R.id.btn6).setOnClickListener(this);
        findViewById(R.id.btn7).setOnClickListener(this);
        findViewById(R.id.btn8).setOnClickListener(this);
        findViewById(R.id.btn9).setOnClickListener(this);

        // Operation buttons
        findViewById(R.id.btnPlus).setOnClickListener(this);
        findViewById(R.id.btnMinus).setOnClickListener(this);
        findViewById(R.id.btnMultiply).setOnClickListener(this);
        findViewById(R.id.btnDivide).setOnClickListener(this);
        findViewById(R.id.btnEquals).setOnClickListener(this);

        // Function buttons
        findViewById(R.id.btnC).setOnClickListener(this);
        findViewById(R.id.btnCE).setOnClickListener(this);
        findViewById(R.id.btnDot).setOnClickListener(this);
        findViewById(R.id.btnPlusMinus).setOnClickListener(this);
        findViewById(R.id.btnPercent).setOnClickListener(this);
        findViewById(R.id.btnSquare).setOnClickListener(this);
        findViewById(R.id.btnSqrt).setOnClickListener(this);
        findViewById(R.id.btnOneOverX).setOnClickListener(this);

        // Memory buttons
        findViewById(R.id.btnMC).setOnClickListener(this);
        findViewById(R.id.btnMR).setOnClickListener(this);
        findViewById(R.id.btnMPlus).setOnClickListener(this);
        findViewById(R.id.btnMMinus).setOnClickListener(this);
        findViewById(R.id.btnMS).setOnClickListener(this);
        findViewById(R.id.btnMTilde).setOnClickListener(this);
    }

    private void addLongClickListeners() {
        // Long press C for advanced clear (clear history)
        findViewById(R.id.btnC).setOnLongClickListener(v -> {
            clearAll();
            calculationHistory.clear();
            tvHistory.setText("");
            performHapticFeedback();
            return true;
        });

        // Long press display to copy result
        tvDisplay.setOnLongClickListener(v -> {
            copyToClipboard(currentInput);
            performHapticFeedback();
            return true;
        });

        // Long press equals for repeat last operation
        findViewById(R.id.btnEquals).setOnLongClickListener(v -> {
            repeatLastOperation();
            performHapticFeedback();
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save memory value
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("memory_value", Double.doubleToLongBits(memoryValue));
        editor.putBoolean("has_memory", hasMemory);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // Add haptic feedback for button presses
        performHapticFeedback();

        // Animate button press
        animateButtonPress(v);

        if (id == R.id.btn0) numberPressed("0");
        else if (id == R.id.btn1) numberPressed("1");
        else if (id == R.id.btn2) numberPressed("2");
        else if (id == R.id.btn3) numberPressed("3");
        else if (id == R.id.btn4) numberPressed("4");
        else if (id == R.id.btn5) numberPressed("5");
        else if (id == R.id.btn6) numberPressed("6");
        else if (id == R.id.btn7) numberPressed("7");
        else if (id == R.id.btn8) numberPressed("8");
        else if (id == R.id.btn9) numberPressed("9");
        else if (id == R.id.btnDot) dotPressed();
        else if (id == R.id.btnPlus) operatorPressed("+");
        else if (id == R.id.btnMinus) operatorPressed("-");
        else if (id == R.id.btnMultiply) operatorPressed("*");
        else if (id == R.id.btnDivide) operatorPressed("/");
        else if (id == R.id.btnEquals) equalsPressed();
        else if (id == R.id.btnC) clearAll();
        else if (id == R.id.btnCE) clearEntry();
        else if (id == R.id.btnPlusMinus) plusMinusPressed();
        else if (id == R.id.btnPercent) percentPressed();
        else if (id == R.id.btnSquare) squarePressed();
        else if (id == R.id.btnSqrt) sqrtPressed();
        else if (id == R.id.btnOneOverX) oneOverXPressed();
        else if (id == R.id.btnMC) memoryClear();
        else if (id == R.id.btnMR) memoryRecall();
        else if (id == R.id.btnMPlus) memoryAdd();
        else if (id == R.id.btnMMinus) memorySubtract();
        else if (id == R.id.btnMS) memoryStore();
        else if (id == R.id.btnMTilde) memoryToggle();

        updateDisplay();
        updateMemoryIndicator();
    }

    private void performHapticFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(10); // Short vibration
            }
        } catch (Exception e) {
            // Fallback to system haptic feedback
            try {
                View view = findViewById(android.R.id.content);
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            } catch (Exception ignored) {}
        }
    }

    private void animateButtonPress(View button) {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        scaleDown.setDuration(50);
        scaleDown.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
        scaleUp.setDuration(50);
        scaleUp.setInterpolator(new DecelerateInterpolator());

        scaleDown.start();
        scaleUp.setStartDelay(50);
        scaleUp.start();

        // Same for Y axis
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
        scaleDownY.setDuration(50);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);
        scaleUpY.setDuration(50);
        scaleUpY.setStartDelay(50);

        scaleDownY.start();
        scaleUpY.start();
    }

    private void numberPressed(String number) {
        if (isNewInput || currentInput.equals("0") || currentInput.equals("Error") || isResultDisplayed) {
            currentInput = number;
            isNewInput = false;
            isResultDisplayed = false;
            isDecimalAdded = false;
        } else {
            if (currentInput.length() < 15) { // Limit input length
                currentInput += number;
            }
        }
    }

    private void dotPressed() {
        if (isNewInput || isResultDisplayed) {
            currentInput = "0.";
            isNewInput = false;
            isResultDisplayed = false;
            isDecimalAdded = true;
        } else if (!isDecimalAdded && !currentInput.contains(".")) {
            currentInput += ".";
            isDecimalAdded = true;
        }
    }

    private void operatorPressed(String op) {
        if (!operator.isEmpty() && !isNewInput && !isResultDisplayed) {
            calculate();
        }

        // Add to history
        String historyEntry = currentInput + " " + getOperatorSymbol(op);
        updateHistory(historyEntry);

        firstOperand = currentInput;
        operator = op;
        lastOperation = op;
        isNewInput = true;
        isResultDisplayed = false;
        isDecimalAdded = false;
    }

    private void equalsPressed() {
        if (!operator.isEmpty() && !isNewInput) {
            String fullExpression = firstOperand + " " + getOperatorSymbol(operator) + " " + currentInput;
            calculate();

            // Add complete calculation to history
            calculationHistory.add(0, fullExpression + " = " + currentInput);
            if (calculationHistory.size() > 10) {
                calculationHistory.remove(calculationHistory.size() - 1);
            }

            updateHistory(fullExpression + " =");

            operator = "";
            firstOperand = "";
            isNewInput = true;
            isResultDisplayed = true;
            isDecimalAdded = false;
        }
    }

    private void calculate() {
        try {
            BigDecimal first = new BigDecimal(firstOperand);
            BigDecimal second = new BigDecimal(currentInput);
            BigDecimal result;

            switch (operator) {
                case "+":
                    result = first.add(second, mathContext);
                    break;
                case "-":
                    result = first.subtract(second, mathContext);
                    break;
                case "*":
                    result = first.multiply(second, mathContext);
                    break;
                case "/":
                    if (second.compareTo(BigDecimal.ZERO) != 0) {
                        result = first.divide(second, mathContext);
                    } else {
                        currentInput = "Cannot divide by zero";
                        return;
                    }
                    break;
                default:
                    return;
            }

            // Format result
            currentInput = formatResult(result);

        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private String formatResult(BigDecimal result) {
        // Handle very large or very small numbers with scientific notation
        double doubleResult = result.doubleValue();

        if (Math.abs(doubleResult) >= 1e12 || (Math.abs(doubleResult) < 1e-6 && doubleResult != 0)) {
            return scientificFormatter.format(doubleResult);
        }

        // Remove unnecessary trailing zeros
        result = result.stripTrailingZeros();

        // Use plain notation for normal numbers
        String formatted = result.toPlainString();

        // Limit decimal places for display
        if (formatted.contains(".") && formatted.length() > 15) {
            int decimalIndex = formatted.indexOf(".");
            if (decimalIndex > 10) {
                return scientificFormatter.format(doubleResult);
            }
            formatted = formatted.substring(0, Math.min(15, formatted.length()));
        }

        return formatted;
    }

    private String getOperatorSymbol(String op) {
        switch (op) {
            case "+": return "+";
            case "-": return "−";
            case "*": return "×";
            case "/": return "÷";
            default: return op;
        }
    }

    private void updateHistory(String entry) {
        if (tvHistory != null) {
            tvHistory.setText(entry);
        }
    }

    private void clearAll() {
        currentInput = "0";
        firstOperand = "";
        operator = "";
        lastOperation = "";
        isNewInput = true;
        isResultDisplayed = false;
        isDecimalAdded = false;
        if (tvHistory != null) {
            tvHistory.setText("");
        }
    }

    private void clearEntry() {
        currentInput = "0";
        isNewInput = true;
        isResultDisplayed = false;
        isDecimalAdded = false;
    }

    private void plusMinusPressed() {
        try {
            if (!currentInput.equals("0") && !currentInput.equals("Error")) {
                if (currentInput.startsWith("-")) {
                    currentInput = currentInput.substring(1);
                } else {
                    currentInput = "-" + currentInput;
                }
                isResultDisplayed = false;
            }
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void percentPressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            BigDecimal result = value.divide(new BigDecimal("100"), mathContext);
            currentInput = formatResult(result);
            isNewInput = true;
            isResultDisplayed = true;
            isDecimalAdded = false;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void squarePressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            BigDecimal result = value.multiply(value, mathContext);

            String historyEntry = currentInput + "²";
            updateHistory(historyEntry);

            currentInput = formatResult(result);
            isNewInput = true;
            isResultDisplayed = true;
            isDecimalAdded = false;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void sqrtPressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            if (value.compareTo(BigDecimal.ZERO) >= 0) {
                double result = Math.sqrt(value.doubleValue());

                String historyEntry = "√(" + currentInput + ")";
                updateHistory(historyEntry);

                currentInput = formatResult(new BigDecimal(result));
            } else {
                currentInput = "Invalid input";
            }
            isNewInput = true;
            isResultDisplayed = true;
            isDecimalAdded = false;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void oneOverXPressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            if (value.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal result = BigDecimal.ONE.divide(value, mathContext);

                String historyEntry = "1/(" + currentInput + ")";
                updateHistory(historyEntry);

                currentInput = formatResult(result);
            } else {
                currentInput = "Cannot divide by zero";
            }
            isNewInput = true;
            isResultDisplayed = true;
            isDecimalAdded = false;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    // Enhanced Memory functions
    private void memoryClear() {
        memoryValue = 0.0;
        hasMemory = false;
    }

    private void memoryRecall() {
        if (hasMemory) {
            currentInput = formatResult(new BigDecimal(memoryValue));
            isNewInput = true;
            isResultDisplayed = true;
            isDecimalAdded = false;
        }
    }

    private void memoryStore() {
        try {
            memoryValue = Double.parseDouble(currentInput);
            hasMemory = true;
        } catch (Exception e) {
            // Handle error silently
        }
    }

    private void memoryAdd() {
        try {
            memoryValue += Double.parseDouble(currentInput);
            hasMemory = true;
        } catch (Exception e) {
            // Handle error silently
        }
    }

    private void memorySubtract() {
        try {
            memoryValue -= Double.parseDouble(currentInput);
            hasMemory = true;
        } catch (Exception e) {
            // Handle error silently
        }
    }

    private void memoryToggle() {
        if (hasMemory) {
            memoryValue = -memoryValue;
            currentInput = formatResult(new BigDecimal(memoryValue));
            isNewInput = true;
            isResultDisplayed = true;
            isDecimalAdded = false;
        }
    }

    private void updateMemoryIndicator() {
        if (tvMemoryIndicator != null) {
            if (hasMemory && memoryValue != 0.0) {
                tvMemoryIndicator.setText("M");
                tvMemoryIndicator.setVisibility(View.VISIBLE);
            } else {
                tvMemoryIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void repeatLastOperation() {
        if (!lastOperation.isEmpty() && !firstOperand.isEmpty()) {
            operatorPressed(lastOperation);
        }
    }

    private void copyToClipboard(String text) {
        try {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Calculator Result", text);
            clipboard.setPrimaryClip(clip);

            // Show feedback (you can add a Toast message here if desired)
        } catch (Exception e) {
            // Handle error silently
        }
    }

    private void updateDisplay() {
        if (tvDisplay != null) {
            String displayText = currentInput;

            // Add thousand separators for large numbers (if not in scientific notation)
            if (!displayText.contains("E") && !displayText.contains("Error") &&
                    !displayText.contains("Cannot") && !displayText.contains("Invalid")) {
                try {
                    if (displayText.contains(".")) {
                        String[] parts = displayText.split("\\.");
                        if (parts.length == 2) {
                            String integerPart = parts[0].replace("-", "");
                            if (integerPart.length() > 3) {
                                String formattedInteger = String.format("%,d", Long.parseLong(integerPart));
                                displayText = (parts[0].startsWith("-") ? "-" : "") +
                                        formattedInteger + "." + parts[1];
                            }
                        }
                    } else {
                        long number = Long.parseLong(displayText);
                        if (Math.abs(number) > 999) {
                            displayText = String.format("%,d", number);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Keep original displayText
                }
            }

            // Limit display length
            if (displayText.length() > 12) {
                // Try scientific notation for very long numbers
                try {
                    double value = Double.parseDouble(currentInput);
                    displayText = scientificFormatter.format(value);
                } catch (Exception e) {
                    displayText = displayText.substring(0, 12) + "...";
                }
            }

            tvDisplay.setText(displayText);
        }
    }
}