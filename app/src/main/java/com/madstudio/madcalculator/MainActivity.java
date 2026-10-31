package com.madstudio.madcalculator;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.madstudio.madcalculator.adapter.HistoryAdapter;
import com.madstudio.madcalculator.data.HistoryManager;
import com.madstudio.madcalculator.databinding.ActivityMainBinding;
import com.madstudio.madcalculator.databinding.DialogHistoryBinding;
import com.madstudio.madcalculator.model.HistoryItem;
import com.madstudio.madcalculator.viewmodel.CalculatorViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CalculatorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        HistoryManager.init(getApplicationContext());
        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);
        viewModel.setHistoryManager(HistoryManager.getInstance(getApplicationContext()));

        setupObservers();
        setupKeypadButtons();
        setupScientificButtons();
        setupActionButtons();
    }

    private void setupObservers() {
        viewModel.expressionLiveData.observe(this, expr -> {
            binding.tvExpression.setText(expr != null ? expr : "");
            binding.expressionScroll.post(() -> binding.expressionScroll.fullScroll(View.FOCUS_RIGHT));
        });

        viewModel.resultLiveData.observe(this, res -> {
            binding.tvResult.setText(res != null && !res.isEmpty() ? res : "0");
        });

        viewModel.previewLiveData.observe(this, prev -> {
            binding.tvPreview.setText(prev != null ? prev : "");
        });

        viewModel.scientificVisibleLiveData.observe(this, visible -> {
            boolean isVisible = visible != null && visible;
            binding.scientificPanel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            binding.btnToggleScientific.setText(isVisible ? "B\u00E1sica \u25B4" : "Cient\u00EDfica \u25BE");
        });

        viewModel.degreesLiveData.observe(this, deg -> {
            boolean isDeg = deg != null && deg;
            binding.btnAngleMode.setText(isDeg ? "DEG" : "RAD");
        });
    }

    private void setupKeypadButtons() {
        MaterialButton[] numberButtons = {
                binding.btn0, binding.btn00, binding.btn1, binding.btn2, binding.btn3,
                binding.btn4, binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        };

        View.OnClickListener numberListener = v -> {
            performHaptic(v);
            MaterialButton btn = (MaterialButton) v;
            viewModel.appendToken(btn.getText().toString());
        };

        for (MaterialButton btn : numberButtons) {
            btn.setOnClickListener(numberListener);
        }

        binding.btnDot.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendDot();
        });

        MaterialButton[] operatorButtons = {
                binding.btnAdd, binding.btnSubtract, binding.btnMultiply, binding.btnDivide, binding.btnPercent
        };

        View.OnClickListener operatorListener = v -> {
            performHaptic(v);
            MaterialButton btn = (MaterialButton) v;
            viewModel.appendOperator(btn.getText().toString());
        };

        for (MaterialButton btn : operatorButtons) {
            btn.setOnClickListener(operatorListener);
        }
    }

    private void setupScientificButtons() {
        binding.btnSin.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("sin(");
        });

        binding.btnCos.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("cos(");
        });

        binding.btnTan.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("tan(");
        });

        binding.btnLn.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("ln(");
        });

        binding.btnLog.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("log(");
        });

        binding.btnSqrt.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("\u221A(");
        });

        binding.btnOpenParen.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("(");
        });

        binding.btnCloseParen.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendToken(")");
        });

        binding.btnPi.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendFunction("\u03C0");
        });

        binding.btnPower.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.appendOperator("^");
        });
    }

    private void setupActionButtons() {
        binding.btnClear.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.clear();
        });

        binding.btnBackspace.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.backspace();
        });

        binding.btnEquals.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.evaluate();
        });

        binding.btnToggleScientific.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.toggleScientific();
        });

        binding.btnAngleMode.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.toggleAngleMode();
        });

        binding.btnHistory.setOnClickListener(v -> {
            performHaptic(v);
            showHistoryBottomSheet();
        });
    }

    private void showHistoryBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogHistoryBinding sheetBinding = DialogHistoryBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        HistoryAdapter adapter = new HistoryAdapter(item -> {
            viewModel.loadFromHistory(item);
            dialog.dismiss();
        });

        sheetBinding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        sheetBinding.rvHistory.setAdapter(adapter);

        Observer<List<HistoryItem>> historyObserver = items -> {
            adapter.setItems(items);
            boolean isEmpty = items == null || items.isEmpty();
            sheetBinding.tvEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            sheetBinding.rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        };

        viewModel.historyLiveData.observe(this, historyObserver);
        dialog.setOnDismissListener(d -> viewModel.historyLiveData.removeObserver(historyObserver));

        sheetBinding.btnClearHistory.setOnClickListener(v -> {
            performHaptic(v);
            viewModel.clearHistory();
        });

        dialog.show();
    }

    private void performHaptic(View v) {
        if (v != null) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }
}
