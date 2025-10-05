package com.example.calculatorappii;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
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
    private String lastOperation = "";
    private String lastOperand = "";

    private MathContext mathContext = new MathContext(15, RoundingMode.HALF_UP);
    private DecimalFormat scientificFormatter = new DecimalFormat("0.#####E0");
    private SharedPreferences preferences;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);
        tvMemoryIndicator = findViewById(R.id.tvMemoryIndicator);

        preferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        memoryValue = Double.longBitsToDouble(preferences.getLong("memory_value", 0));
        hasMemory = preferences.getBoolean("has_memory", false);

        initializeButtons();
        updateDisplay();
        updateMemoryIndicator();
        addLongClickListeners();
    }

    private void initializeButtons() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide,
                R.id.btnEquals, R.id.btnC, R.id.btnCE, R.id.btnDot,
                R.id.btnPlusMinus, R.id.btnPercent, R.id.btnSquare,
                R.id.btnSqrt, R.id.btnOneOverX, R.id.btnMC, R.id.btnMR,
                R.id.btnMPlus, R.id.btnMMinus, R.id.btnMS, R.id.btnMTilde
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this);
        }
    }

    private void addLongClickListeners() {
        findViewById(R.id.btnC).setOnLongClickListener(v -> {
            clearAll();
            performHapticFeedback();
            showToast("All cleared");
            return true;
        });

        tvDisplay.setOnLongClickListener(v -> {
            copyToClipboard(currentInput);
            performHapticFeedback();
            showToast("Copied to clipboard");
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("memory_value", Double.doubleToLongBits(memoryValue));
        editor.putBoolean("has_memory", hasMemory);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        performHapticFeedback();
        animateButtonPress(v);

        int id = v.getId();

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
                vibrator.vibrate(10);
            }
        } catch (Exception ignored) {}
    }

    private void animateButtonPress(View button) {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        scaleDown.setDuration(50);
        scaleDown.setInterpolator(new DecelerateInterpolator());
        scaleDown.start();

        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
        scaleUp.setDuration(50);
        scaleUp.setStartDelay(50);
        scaleUp.start();

        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
        scaleDownY.setDuration(50);
        scaleDownY.start();

        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);
        scaleUpY.setDuration(50);
        scaleUpY.setStartDelay(50);
        scaleUpY.start();
    }

    private void numberPressed(String number) {
        if (isNewInput || currentInput.equals("0") || currentInput.equals("Error") || isResultDisplayed) {
            currentInput = number;
            isNewInput = false;
            isResultDisplayed = false;
            isDecimalAdded = false;
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
        } else if (!isDecimalAdded && !currentInput.contains(".")) {
            currentInput += ".";
            isDecimalAdded = true;
        }
    }

    private void operatorPressed(String op) {
        if (!operator.isEmpty() && !isNewInput && !isResultDisplayed) {
            calculate();
        }

        updateHistory(currentInput + " " + getOperatorSymbol(op));
        firstOperand = currentInput;
        operator = op;
        lastOperation = op;
        lastOperand = currentInput;
        isNewInput = true;
        isResultDisplayed = false;
        isDecimalAdded = false;
    }

    private void equalsPressed() {
        if (!operator.isEmpty() && !isNewInput) {
            String expression = firstOperand + " " + getOperatorSymbol(operator) + " " + currentInput;
            lastOperand = currentInput;
            calculate();
            updateHistory(expression + " =");
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
                        currentInput = "Error";
                        showToast("Cannot divide by zero");
                        return;
                    }
                    break;
                default:
                    return;
            }
            currentInput = formatResult(result);
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private String formatResult(BigDecimal result) {
        double doubleResult = result.doubleValue();

        if (Double.isInfinite(doubleResult) || Double.isNaN(doubleResult)) {
            return "Error";
        }

        if (Math.abs(doubleResult) >= 1e12 || (Math.abs(doubleResult) < 1e-6 && doubleResult != 0)) {
            return scientificFormatter.format(doubleResult);
        }

        result = result.stripTrailingZeros();
        String formatted = result.toPlainString();

        if (formatted.contains(".") && formatted.length() > 15) {
            formatted = formatted.substring(0, 15);
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
        isNewInput = true;
        isResultDisplayed = false;
        isDecimalAdded = false;
        if (tvHistory != null) tvHistory.setText("");
    }

    private void clearEntry() {
        currentInput = "0";
        isNewInput = true;
        isResultDisplayed = false;
        isDecimalAdded = false;
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

    private void percentPressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            BigDecimal result = value.divide(new BigDecimal("100"), mathContext);
            currentInput = formatResult(result);
            isNewInput = true;
            isResultDisplayed = true;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void squarePressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            BigDecimal result = value.multiply(value, mathContext);
            updateHistory(currentInput + "²");
            currentInput = formatResult(result);
            isNewInput = true;
            isResultDisplayed = true;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void sqrtPressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            if (value.compareTo(BigDecimal.ZERO) >= 0) {
                double result = Math.sqrt(value.doubleValue());
                updateHistory("√(" + currentInput + ")");
                currentInput = formatResult(new BigDecimal(result));
            } else {
                currentInput = "Error";
                showToast("Invalid input");
            }
            isNewInput = true;
            isResultDisplayed = true;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void oneOverXPressed() {
        try {
            BigDecimal value = new BigDecimal(currentInput);
            if (value.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal result = BigDecimal.ONE.divide(value, mathContext);
                updateHistory("1/(" + currentInput + ")");
                currentInput = formatResult(result);
            } else {
                currentInput = "Error";
                showToast("Cannot divide by zero");
            }
            isNewInput = true;
            isResultDisplayed = true;
        } catch (Exception e) {
            currentInput = "Error";
        }
    }

    private void memoryClear() {
        memoryValue = 0.0;
        hasMemory = false;
        showToast("Memory cleared");
    }

    private void memoryRecall() {
        if (hasMemory) {
            currentInput = formatResult(new BigDecimal(memoryValue));
            isNewInput = true;
            isResultDisplayed = true;
        }
    }

    private void memoryStore() {
        try {
            memoryValue = Double.parseDouble(currentInput);
            hasMemory = true;
            showToast("Stored");
        } catch (Exception ignored) {}
    }

    private void memoryAdd() {
        try {
            memoryValue += Double.parseDouble(currentInput);
            hasMemory = true;
            showToast("Added to memory");
        } catch (Exception ignored) {}
    }

    private void memorySubtract() {
        try {
            memoryValue -= Double.parseDouble(currentInput);
            hasMemory = true;
            showToast("Subtracted");
        } catch (Exception ignored) {}
    }

    private void memoryToggle() {
        if (hasMemory) {
            memoryValue = -memoryValue;
            currentInput = formatResult(new BigDecimal(memoryValue));
            isNewInput = true;
            isResultDisplayed = true;
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

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("result", text);
        clipboard.setPrimaryClip(clip);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateDisplay() {
        if (tvDisplay != null) {
            tvDisplay.setText(currentInput);
        }
    }
}