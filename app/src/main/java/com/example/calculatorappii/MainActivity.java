// 1. Corrected the package name to match the file path
package com.example.calculatorappii;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Using the correct button IDs from your activity_main.xml
    private TextView tvDisplay, tvHistory;
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
    private String lastOperandForRepeat = "";

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
        // The other TextViews (tvMemoryIndicator, tvSecondaryDisplay) were not in the final XML

        // Initialize preferences and vibrator
        preferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Load saved memory value
        memoryValue = Double.longBitsToDouble(preferences.getLong("memory_value", 0));
        hasMemory = preferences.getBoolean("has_memory", false);

        // Initialize all buttons and set click listeners
        initializeButtons();
        updateDisplay();

        // Add long click listeners for advanced features
        addLongClickListeners();
    }

    private void initializeButtons() {
        // Number buttons
        setButtonListener(R.id.btn0);
        setButtonListener(R.id.btn1);
        setButtonListener(R.id.btn2);
        setButtonListener(R.id.btn3);
        setButtonListener(R.id.btn4);
        setButtonListener(R.id.btn5);
        setButtonListener(R.id.btn6);
        setButtonListener(R.id.btn7);
        setButtonListener(R.id.btn8);
        setButtonListener(R.id.btn9);

        // Operation buttons
        setButtonListener(R.id.btnPlus);
        setButtonListener(R.id.btnMinus);
        setButtonListener(R.id.btnMultiply);
        setButtonListener(R.id.btnDivide);
        setButtonListener(R.id.btnEquals);

        // 2. Using correct button IDs from your layout file
        setButtonListener(R.id.btnAC); // Changed from btnC
        setButtonListener(R.id.btnDot);
        setButtonListener(R.id.btnPlusMinus);
        setButtonListener(R.id.btnPercent);

        // The following buttons were removed as they don't exist in the layout:
        // btnCE, btnSquare, btnSqrt, btnOneOverX, btnMC, btnMR, btnMPlus, btnMMinus, btnMS, btnMTilde
    }

    private void setButtonListener(int id) {
        View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(this);
        }
    }

    private void addLongClickListeners() {
        // Long press C for advanced clear (clear history)
        View btnAC = findViewById(R.id.btnAC);
        if (btnAC != null) {
            btnAC.setOnLongClickListener(v -> {
                clearAll();
                calculationHistory.clear();
                tvHistory.setText("");
                performHapticFeedback();
                Toast.makeText(MainActivity.this, "History Cleared", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // Long press display to copy result
        if (tvDisplay != null) {
            tvDisplay.setOnLongClickListener(v -> {
                copyToClipboard(currentInput);
                performHapticFeedback();
                return true;
            });
        }

        // Long press equals for repeat last operation
        View btnEquals = findViewById(R.id.btnEquals);
        if (btnEquals != null) {
            btnEquals.setOnLongClickListener(v -> {
                repeatLastOperation();
                performHapticFeedback();
                return true;
            });
        }
    }

    // 3. Added the missing copyToClipboard method
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("CalculatorResult", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    // 4. Added the missing repeatLastOperation method
    private void repeatLastOperation() {
        if (!lastOperation.isEmpty() && !lastOperandForRepeat.isEmpty()) {
            firstOperand = currentInput;
            currentInput = lastOperandForRepeat;
            operator = lastOperation;
            isNewInput = false;
            equalsPressed();
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        performHapticFeedback();
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
        else if (id == R.id.btnAC) clearAll(); // Using the correct ID from layout
        else if (id == R.id.btnPlusMinus) plusMinusPressed();
        else if (id == R.id.btnPercent) percentPressed();
        // Removed handlers for non-existent buttons

        updateDisplay();
    }

    private void numberPressed(String number) {
        if (isNewInput || currentInput.equals("0") || currentInput.equals("Error") || isResultDisplayed) {
            currentInput = number;
            isNewInput = false;
            isResultDisplayed = false;
        } else {
            if (currentInput.length() < 15) {
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
        } else if (!isDecimalAdded) {
            currentInput += ".";
            isDecimalAdded = true;
        }
    }

    private void operatorPressed(String op) {
        if (!operator.isEmpty() && !isNewInput) {
            calculate();
        }
        firstOperand = currentInput;
        operator = op;
        lastOperation = op;
        isNewInput = true;
        isDecimalAdded = false;
        tvHistory.setText(firstOperand + " " + getOperatorSymbol(op));
    }

    private void equalsPressed() {
        if (!operator.isEmpty() && !isNewInput) {
            lastOperandForRepeat = currentInput; // Store for repeat operation
            String fullExpression = firstOperand + " " + getOperatorSymbol(operator) + " " + currentInput;
            calculate();
            tvHistory.setText(fullExpression + " =");
            operator = "";
            isResultDisplayed = true;
        }
    }

    private void calculate() {
        if (firstOperand.isEmpty() || operator.isEmpty()) return;
        try {
            BigDecimal first = new BigDecimal(firstOperand);
            BigDecimal second = new BigDecimal(currentInput);
            BigDecimal result = BigDecimal.ZERO;

            switch (operator) {
                case "+": result = first.add(second, mathContext); break;
                case "-": result = first.subtract(second, mathContext); break;
                case "*": result = first.multiply(second, mathContext); break;
                case "/":
                    if (second.compareTo(BigDecimal.ZERO) == 0) {
                        currentInput = "Error";
                        updateDisplay();
                        return;
                    }
                    result = first.divide(second, mathContext);
                    break;
            }
            currentInput = formatResult(result);
        } catch (Exception e) {
            currentInput = "Error";
        }
        isNewInput = true;
        isDecimalAdded = false;
    }

    // 5. Completed the percentPressed method and the rest of the file
    private void percentPressed() {
        if (firstOperand.isEmpty()) {
            // Simple percentage
            BigDecimal value = new BigDecimal(currentInput);
            currentInput = formatResult(value.divide(new BigDecimal(100), mathContext));
        } else {
            // Percentage of the first operand
            BigDecimal first = new BigDecimal(firstOperand);
            BigDecimal second = new BigDecimal(currentInput);
            BigDecimal percentValue = first.multiply(second, mathContext).divide(new BigDecimal(100), mathContext);
            currentInput = formatResult(percentValue);
        }
        isResultDisplayed = true;
        updateDisplay();
    }

    private void clearAll() {
        currentInput = "0";
        firstOperand = "";
        operator = "";
        lastOperation = "";
        lastOperandForRepeat = "";
        isNewInput = true;
        isResultDisplayed = false;
        isDecimalAdded = false;
        tvHistory.setText("");
    }

    private void plusMinusPressed() {
        if (!currentInput.equals("0") && !currentInput.equals("Error")) {
            if (currentInput.startsWith("-")) {
                currentInput = currentInput.substring(1);
            } else {
                currentInput = "-" + currentInput;
            }
        }
    }

    private void updateDisplay() {
        tvDisplay.setText(currentInput);
    }

    private String formatResult(BigDecimal result) {
        String plainString = result.stripTrailingZeros().toPlainString();
        if (plainString.length() > 15) {
            return scientificFormatter.format(result);
        }
        return plainString;
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

    private void performHapticFeedback() {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }

    private void animateButtonPress(View button) {
        if (button == null) return;
        button.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50).withEndAction(() -> {
            button.animate().scaleX(1f).scaleY(1f).setDuration(50).start();
        }).start();
    }
}
