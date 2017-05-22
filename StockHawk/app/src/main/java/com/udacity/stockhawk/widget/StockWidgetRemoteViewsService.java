package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by jorgemendes on 21/05/17.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {}

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI, new String[]{}, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return data != null ? data.getCount() : 0;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_item);
                if (data != null && data.moveToPosition(position)) {
                    final String symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                    final Float price = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                    final Float absoluteChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                    final Float percentageChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
                    final String history = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));

                    views.setTextViewText(R.id.symbol, symbol);

                    final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                    views.setTextViewText(R.id.price, dollarFormat.format(price));

                    final DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                    dollarFormatWithPlus.setPositivePrefix("+$");
                    views.setTextViewText(R.id.change, dollarFormatWithPlus.format(absoluteChange));

                    if (absoluteChange > 0) {
                        views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                    } else {
                        views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                    }

                    Intent fillInIntent = new Intent()
                            .putExtra(Contract.Quote.COLUMN_SYMBOL, symbol)
                            .putExtra(Contract.Quote.COLUMN_PRICE, price)
                            .putExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, absoluteChange)
                            .putExtra(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentageChange)
                            .putExtra(Contract.Quote.COLUMN_HISTORY, history);
                    fillInIntent.putExtra("", position);
                    views.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
                }
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data != null && data.moveToPosition(position)) {
                    return data.getLong(Contract.Quote.POSITION_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
