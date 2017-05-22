package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.StockDetailActivity;

import static com.udacity.stockhawk.sync.QuoteSyncJob.ACTION_DATA_UPDATED;

/**
 * Created by jorgemendes on 21/05/17.
 */

public class StockWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
        views.setTextViewText(R.id.title, context.getString(R.string.widget_title));

        views.setRemoteAdapter(R.id.stocks, new Intent(context, StockWidgetRemoteViewsService.class));

        Intent startMainActivityIntent = new Intent(context, StockDetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startMainActivityIntent, 0);
        views.setPendingIntentTemplate(R.id.stocks, pendingIntent);

        for (int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_DATA_UPDATED.equals(intent.getAction())) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
            views.setRemoteAdapter(R.id.stocks, new Intent(context, StockWidgetRemoteViewsService.class));
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, StockWidget.class);
            manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(componentName), R.id.stocks);
        }
    }
}
