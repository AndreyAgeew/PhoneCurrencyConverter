package com.practicum.currencyconverter.presentation.converter;

import com.practicum.currencyconverter.data.models.Currency;
import com.practicum.currencyconverter.data.models.PairConversation;
import com.practicum.currencyconverter.data.network.ExchangerRateService;
import com.practicum.currencyconverter.di.DI;
import com.practicum.currencyconverter.presentation.base.BaseViewModel;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConverterViewModel extends BaseViewModel {

    private final ExchangerRateService exchangerRateService = DI.exchangerRateService();

    private final MutableLiveData<ConverterState> converterStateLiveData = new MutableLiveData<>(ConverterState.DEFAULT);

    public LiveData<ConverterState> getConverterStateLiveData() {
        return converterStateLiveData;
    }

    public void getCurrencyRate() {
        exchangerRateService.getPairConversation(currentState().getFromCurrency().getCode(), currentState().getToCurrency().getCode())
                .enqueue(new Callback<PairConversation>() {
                    @Override
                    public void onResponse(@NonNull final Call<PairConversation> call, @NonNull final Response<PairConversation> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            final ConverterState resultState = new ConverterState.Builder(currentState())
                                    .setCurrencyCourse(response.body().getConversionRate())
                                    .copy();

                            converterStateLiveData.postValue(resultState);
                        } else {
                            // TODO handle error
                        }
                    }

                    @Override
                    public void onFailure(@NonNull final Call<PairConversation> call, @NonNull final Throwable error) {
                        // TODO handle error
                    }
                });
    }

    public void setFromCurrency(final Currency currency) {
        final ConverterState resultState = new ConverterState.Builder(currentState())
                .setFromCurrency(currency)
                .copy();

        converterStateLiveData.postValue(resultState);
        getCurrencyRate();
    }

    public void setToCurrency(final Currency currency) {
        final ConverterState resultState = new ConverterState.Builder(currentState())
                .setToCurrency(currency)
                .copy();

        converterStateLiveData.postValue(resultState);
        getCurrencyRate();
    }

    public void swapCurrencies() {
        final ConverterState resultState = new ConverterState.Builder(currentState())
                .setFromCurrency(currentState().getToCurrency())
                .setToCurrency(currentState().getFromCurrency())
                .setToCurrencyInput(currentState().getFromCurrencyInput())
                .setFromCurrencyInput(currentState().getToCurrencyInput())
                .copy();

        converterStateLiveData.postValue(resultState);
        getCurrencyRate();
    }

    @NonNull
    private ConverterState currentState() {
        return Objects.requireNonNull(converterStateLiveData.getValue());
    }

    public void convertUserInput(final String fromCurrencyInput) {
        final DecimalFormat decimalFormat = new DecimalFormat();
        try {
            final double result = Objects.requireNonNull(decimalFormat.parse(fromCurrencyInput)).doubleValue();
            final double toCurrencyInput = result * currentState().getCurrencyCourse();

            final ConverterState resultState = new ConverterState.Builder(currentState())
                    .setFromCurrencyInput(result)
                    .setToCurrencyInput(toCurrencyInput)
                    .copy();

            converterStateLiveData.postValue(resultState);
        } catch (ParseException e) {
            // TODO handle error
        }

    }
}
