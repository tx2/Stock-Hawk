package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StockDetailActivity extends AppCompatActivity {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.price)
    TextView mPriceTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.price_change)
    TextView mPriceChangeTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.percentage_change)
    TextView mPercentageChangeTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock_history_chart)
    LineChart mStockHistoryChart;

    private String[] mHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ButterKnife.bind(this);

        Intent intent = this.getIntent();
        if (intent != null && intent.hasExtra(Contract.Quote.COLUMN_SYMBOL)
                && intent.hasExtra(Contract.Quote.COLUMN_PRICE)
                && intent.hasExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE)
                && intent.hasExtra(Contract.Quote.COLUMN_PERCENTAGE_CHANGE)
                && intent.hasExtra(Contract.Quote.COLUMN_HISTORY)) {

            final String symbol = intent.getStringExtra(Contract.Quote.COLUMN_SYMBOL);
            final Float price = intent.getFloatExtra(Contract.Quote.COLUMN_PRICE, 0);
            final Float absoluteChange = intent.getFloatExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, 0);
            final Float percentageChange = intent.getFloatExtra(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, 0);
            final String history = intent.getStringExtra(Contract.Quote.COLUMN_HISTORY);
            mHistory = history.split("\n");

            getSupportActionBar().setTitle(symbol.toUpperCase());

            final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            mPriceTextView.setText(dollarFormat.format(price));

            final DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            mPriceChangeTextView.setText(dollarFormatWithPlus.format(absoluteChange));

            final DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
            mPercentageChangeTextView.setText(percentageFormat.format(percentageChange / 100));

            if (absoluteChange > 0) {
                mPriceChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
                mPercentageChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                mPriceChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
                mPercentageChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            LineDataSet dataSet = new LineDataSet(parseStockHistoryData(mHistory), getString(R.string.stock_chart_subtitle));
            dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorAccent));
            dataSet.setCircleColorHole(ContextCompat.getColor(this, R.color.colorAccent));
            dataSet.setFillColor(ContextCompat.getColor(this, R.color.colorAccent));
            dataSet.setColor(ContextCompat.getColor(this, R.color.colorAccent));
            dataSet.setDrawFilled(true);

            mStockHistoryChart.setData(new LineData(dataSet));
            mStockHistoryChart.setNoDataText(getString(R.string.stock_chart_no_data));
            mStockHistoryChart.setContentDescription(getString(R.string.stock_chart, symbol));
            mStockHistoryChart.invalidate();

            mStockHistoryChart.setTouchEnabled(false);
            mStockHistoryChart.getAxisRight().setEnabled(false);
            mStockHistoryChart.getDescription().setEnabled(false);
            mStockHistoryChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.chart_borders));
            mStockHistoryChart.setGridBackgroundColor(ContextCompat.getColor(this, R.color.chart_borders));
            mStockHistoryChart.setBorderColor(ContextCompat.getColor(this, R.color.chart_borders));

            mStockHistoryChart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.chart_borders));
            mStockHistoryChart.getXAxis().setLabelRotationAngle(-45);
            mStockHistoryChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mStockHistoryChart.getXAxis().setTextColor(ContextCompat.getColor(this, R.color.chart_borders));
            mStockHistoryChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    String[] values = mHistory[(int) value].split(",");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(values[0]));
                    calendar.add(Calendar.MILLISECOND, TimeZone.getDefault().getOffset(calendar.getTimeInMillis()));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
                    return dateFormat.format(calendar.getTime());
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<Entry> parseStockHistoryData(String[] history) {
        List<Entry> stockHistory = new ArrayList<>();

        try {
            int i = 0;
            for (String h : history) {
                String[] stock = h.split(",");
                stockHistory.add(new Entry(i++, Float.parseFloat(stock[1])));
            }
        } catch (Exception ex) {
            Timber.e("Error parsing stock history: " + ex.getMessage());
        }

        return stockHistory;
    }
}
