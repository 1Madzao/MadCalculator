package com.madstudio.madcalculator.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.madstudio.madcalculator.CalculatorEngine;
import com.madstudio.madcalculator.data.HistoryManager;
import com.madstudio.madcalculator.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class CalculatorViewModel extends ViewModel {

    private final CalculatorEngine engine;
    private HistoryManager historyManager;

    private final StringBuilder currentExpression = new StringBuilder();
    private boolean isScientificVisible = false;
    private boolean isDegrees = true;
    private boolean lastActionWasEquals = false;

    private final MutableLiveData<String> _expressionLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> _resultLiveData = new MutableLiveData<>("0");
    private final MutableLiveData<String> _previewLiveData = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> _scientificVisibleLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _degreesLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<List<HistoryItem>> _historyLiveData = new MutableLiveData<>(new ArrayList<>());

    public final LiveData<String> expressionLiveData = _expressionLiveData;
    public final LiveData<String> resultLiveData = _resultLiveData;
    public final LiveData<String> previewLiveData = _previewLiveData;
    public final LiveData<Boolean> scientificVisibleLiveData = _scientificVisibleLiveData;
    public final LiveData<Boolean> degreesLiveData = _degreesLiveData;
    public final LiveData<List<HistoryItem>> historyLiveData = _historyLiveData;

    public CalculatorViewModel() {
        this(new CalculatorEngine(), HistoryManager.getInstance());
    }

    public CalculatorViewModel(HistoryManager historyManager) {
        this(new CalculatorEngine(), historyManager);
    }

    public CalculatorViewModel(CalculatorEngine engine, HistoryManager historyManager) {
        this.engine = engine != null ? engine : new CalculatorEngine();
        this.historyManager = historyManager != null ? historyManager : HistoryManager.getInstance();
        this.isDegrees = this.engine.isDegrees();

        _scientificVisibleLiveData.setValue(isScientificVisible);
        _degreesLiveData.setValue(isDegrees);
        _resultLiveData.setValue("0");
        _expressionLiveData.setValue("");
        _previewLiveData.setValue("");

        refreshHistory();
    }

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
        refreshHistory();
    }

    public void refreshHistory() {
        if (historyManager != null) {
            _historyLiveData.setValue(historyManager.getAll());
        }
    }

    public void appendToken(String token) {
        if (token == null || token.isEmpty()) return;

        if (lastActionWasEquals) {
            currentExpression.setLength(0);
            lastActionWasEquals = false;
        }

        currentExpression.append(token);
        updateDisplay();
    }

    public void appendFunction(String func) {
        if (func == null || func.isEmpty()) return;

        if (lastActionWasEquals) {
            currentExpression.setLength(0);
            lastActionWasEquals = false;
        }

        currentExpression.append(func);
        updateDisplay();
    }

    public void appendDot() {
        if (lastActionWasEquals) {
            currentExpression.setLength(0);
            lastActionWasEquals = false;
        }

        if (canAppendComma()) {
            if (currentExpression.length() == 0 || isOperator(lastChar())) {
                currentExpression.append("0,");
            } else {
                currentExpression.append(",");
            }
            updateDisplay();
        }
    }

    public void appendOperator(String op) {
        if (op == null || op.isEmpty()) return;

        lastActionWasEquals = false;

        if (currentExpression.length() == 0) {
            if (op.equals("\u2212") || op.equals("-")) {
                currentExpression.append("\u2212");
                updateDisplay();
            }
            return;
        }

        char last = lastChar();
        if (isBasicOperator(last) && !op.equals("%")) {
            currentExpression.setCharAt(currentExpression.length() - 1, op.charAt(0));
            updateDisplay();
        } else {
            currentExpression.append(op);
            updateDisplay();
        }
    }

    public void backspace() {
        if (currentExpression.length() > 0) {
            lastActionWasEquals = false;
            String expr = currentExpression.toString();
            String[] funcs = {"sin(", "cos(", "tan(", "log(", "ln(", "\u221A("};
            for (String f : funcs) {
                if (expr.endsWith(f)) {
                    currentExpression.setLength(currentExpression.length() - f.length());
                    updateDisplay();
                    return;
                }
            }
            currentExpression.deleteCharAt(currentExpression.length() - 1);
            updateDisplay();
        }
    }

    public void clear() {
        currentExpression.setLength(0);
        lastActionWasEquals = false;
        _expressionLiveData.setValue("");
        _resultLiveData.setValue("0");
        _previewLiveData.setValue("");
    }

    public void evaluate() {
        if (currentExpression.length() == 0) return;

        try {
            String rawExpr = currentExpression.toString();
            String evaluated = engine.evaluate(rawExpr);
            if (evaluated != null && !evaluated.isEmpty()) {
                if (historyManager != null) {
                    historyManager.add(rawExpr, evaluated);
                    _historyLiveData.setValue(historyManager.getAll());
                }

                _expressionLiveData.setValue(rawExpr + " =");
                _resultLiveData.setValue(evaluated);
                _previewLiveData.setValue("");

                currentExpression.setLength(0);
                currentExpression.append(evaluated.replace(".", ""));
                lastActionWasEquals = true;
            }
        } catch (ArithmeticException e) {
            _previewLiveData.setValue(e.getMessage() != null ? e.getMessage() : "Erro matem\u00E1tico");
        } catch (Exception e) {
            _previewLiveData.setValue("Express\u00E3o inv\u00E1lida");
        }
    }

    public void toggleScientific() {
        isScientificVisible = !isScientificVisible;
        _scientificVisibleLiveData.setValue(isScientificVisible);
    }

    public void toggleAngleMode() {
        isDegrees = !engine.isDegrees();
        engine.setDegrees(isDegrees);
        _degreesLiveData.setValue(isDegrees);
        updateDisplay();
    }

    public void clearHistory() {
        if (historyManager != null) {
            historyManager.clear();
            _historyLiveData.setValue(historyManager.getAll());
        }
    }

    public void loadFromHistory(HistoryItem item) {
        if (item == null || item.getResult() == null) return;

        currentExpression.setLength(0);
        currentExpression.append(item.getResult().replace(".", ""));
        lastActionWasEquals = true;

        _expressionLiveData.setValue(item.getExpression() + " =");
        _resultLiveData.setValue(item.getResult());
        _previewLiveData.setValue("");
    }

    private void updateDisplay() {
        String expr = currentExpression.toString();
        _expressionLiveData.setValue(expr);

        if (expr.isEmpty()) {
            _resultLiveData.setValue("0");
            _previewLiveData.setValue("");
            return;
        }

        try {
            String preview = engine.evaluate(expr);
            _resultLiveData.setValue(preview.isEmpty() ? "0" : preview);
            _previewLiveData.setValue("");
        } catch (ArithmeticException e) {
            _previewLiveData.setValue(e.getMessage());
        } catch (Exception e) {
            _previewLiveData.setValue("");
        }
    }

    private boolean canAppendComma() {
        for (int i = currentExpression.length() - 1; i >= 0; i--) {
            char c = currentExpression.charAt(i);
            if (c == ',') return false;
            if (!Character.isDigit(c)) break;
        }
        return true;
    }

    private char lastChar() {
        return currentExpression.charAt(currentExpression.length() - 1);
    }

    private boolean isBasicOperator(char c) {
        return c == '+' || c == '\u2212' || c == '-' || c == '\u00D7' || c == '\u00F7' || c == '^';
    }

    private boolean isOperator(char c) {
        return isBasicOperator(c) || c == '(';
    }

    public String getCurrentExpression() {
        return currentExpression.toString();
    }

    public boolean isScientificVisible() {
        return isScientificVisible;
    }

    public boolean isDegrees() {
        return isDegrees;
    }

    public boolean isLastActionWasEquals() {
        return lastActionWasEquals;
    }

    public LiveData<String> getExpressionLiveData() {
        return expressionLiveData;
    }

    public LiveData<String> getResultLiveData() {
        return resultLiveData;
    }

    public LiveData<String> getPreviewLiveData() {
        return previewLiveData;
    }

    public LiveData<Boolean> getScientificVisibleLiveData() {
        return scientificVisibleLiveData;
    }

    public LiveData<Boolean> getDegreesLiveData() {
        return degreesLiveData;
    }

    public LiveData<List<HistoryItem>> getHistoryLiveData() {
        return historyLiveData;
    }
}
