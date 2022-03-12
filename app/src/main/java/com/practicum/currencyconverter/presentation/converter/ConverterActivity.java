package com.practicum.currencyconverter.presentation.converter;

import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;

import com.practicum.currencyconverter.R;
import com.practicum.currencyconverter.data.models.Currency;
import com.practicum.currencyconverter.databinding.ActivityConverterBinding;
import com.practicum.currencyconverter.presentation.base.BaseActivity;
import com.practicum.currencyconverter.presentation.currencies.CurrenciesActivity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Objects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

public class ConverterActivity extends BaseActivity<ConverterViewModel> {

    private final ActivityResultLauncher<CurrencyInput> currencyScreenLauncher =
            registerForActivityResult(CurrenciesActivity.getContract(this), this::handleCurrencyResult);

    private ActivityConverterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConverterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        initViewModel();
        openKeyBoard();
        viewModel.getCurrencyRate();
    }

    @Override
    protected ConverterViewModel createViewModel() {
        return new ViewModelProvider(this).get(ConverterViewModel.class);
    }

    @Override
    protected void showLoader(final boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.currencyCourseTextView.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.currencyCourseTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void handleError(final boolean isShown) {
        binding.fromInputEditText.setEnabled(!isShown);
        if (isShown) {
            binding.currencyCourseTextView.setText(R.string.converter_currency_cource_error);
            binding.currencyCourseTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
            binding.fromBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.red_10));
        } else {
            binding.currencyCourseTextView.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            binding.fromBackground.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void initView() {
        setFromInput();

        binding.fromCurrencyIconImageView.setOnClickListener(v -> openCurrencies(CurrencyInput.FROM));
        binding.toCurrencyIconImageView.setOnClickListener(v -> openCurrencies(CurrencyInput.TO));
        binding.refreshButton.setOnClickListener(v -> clearFields());
        binding.convertButton.setOnClickListener(v -> viewModel.convertUserInput(binding.fromInputEditText.getText().toString()));
        binding.swapFab.setOnClickListener(v -> viewModel.swapCurrencies());
    }

    private void setFromInput() {
        binding.fromInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // nothing to do
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                // nothing to do
            }

            @Override
            public void afterTextChanged(final Editable s) {
                setConvertButtonState(s);
            }
        });
    }

    private void initViewModel() {
        viewModel.getConverterStateLiveData().observe(this, this::render);
    }

    private void render(final ConverterState state) {
        setFromCurrency(state.getFromCurrency());
        setToCurrency(state.getToCurrency());
        setCurrencyCourse(state);
        setFromCurrencyInput(state);
        setToCurrencyInput(state);
    }

    private void openKeyBoard() {
        binding.fromInputEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void openCurrencies(final CurrencyInput currencyInput) {
        currencyScreenLauncher.launch(currencyInput);
    }

    private void handleCurrencyResult(final Pair<Currency, CurrencyInput> currencyResult) {
        final CurrencyInput currencyInput = currencyResult.second;
        final Currency currency = currencyResult.first;

        switch (currencyInput) {
            case FROM:
                viewModel.setFromCurrency(currency);
                break;
            case TO:
                viewModel.setToCurrency(currency);
                break;
        }
    }

    private void setFromCurrency(final Currency currency) {
        binding.fromCurrencyIconImageView.setImageDrawable(ContextCompat.getDrawable(this, currency.getIcon()));
        binding.fromCurrencyCodeTextView.setText(currency.getCode());
    }

    private void setToCurrency(final Currency currency) {
        binding.toCurrencyIconImageView.setImageDrawable(ContextCompat.getDrawable(this, currency.getIcon()));
        binding.toCurrencyCodeTextView.setText(currency.getCode());
    }

    private void setCurrencyCourse(final ConverterState state) {
        if (state.getCurrencyCourse() != 0.0) {
            final String currencyCourseText = getString(
                    R.string.converter_currency_cource,
                    state.getFromCurrency().getCode(),
                    state.getCurrencyCourse(),
                    state.getToCurrency().getCode()
            );

            binding.currencyCourseTextView.setVisibility(View.VISIBLE);
            binding.currencyCourseTextView.setText(currencyCourseText);
        } else {
            binding.currencyCourseTextView.setVisibility(View.GONE);
            binding.currencyCourseTextView.setText("");
        }
    }

    private void setFromCurrencyInput(final ConverterState state) {
        final double fromCurrencyInput = state.getFromCurrencyInput();

        if (fromCurrencyInput == 0.0) {
            binding.fromInputEditText.setText("");
        } else {
            final DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setMaximumFractionDigits(2);
            binding.fromInputEditText.setText(decimalFormat.format(fromCurrencyInput));
        }
    }

    private void setToCurrencyInput(final ConverterState state) {
        final double toCurrencyInput = state.getToCurrencyInput();

        if (toCurrencyInput == 0.0) {
            binding.toResultTextView.setText("");
        } else {
            final DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setMaximumFractionDigits(2);
            binding.toResultTextView.setText(decimalFormat.format(toCurrencyInput));
        }
    }

    private void clearFields() {
        binding.fromInputEditText.setText("");
        binding.toResultTextView.setText("");
    }

    private void setConvertButtonState(final Editable s) {
        binding.convertButton.setEnabled(s.length() > 0);
    }
}