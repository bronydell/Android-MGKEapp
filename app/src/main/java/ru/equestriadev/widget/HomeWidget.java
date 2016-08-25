package ru.equestriadev.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.equestriadev.arch.Day;
import ru.equestriadev.arch.Month;
import ru.equestriadev.mgke.DatabaseHelper;
import ru.equestriadev.mgke.R;

/**
 * Implementation of App Widget functionality.
 */
public class HomeWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("widgets", 0);
        CharSequence widgetText = prefs.getString("Title_" + appWidgetId, "Error");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_widget);
        views.setTextColor(R.id.appwidget_text, Color.WHITE);
        views.setTextColor(R.id.appwidget_date, Color.WHITE);
        if (prefs.getBoolean("Type_" + appWidgetId, false))
            views.setTextViewText(R.id.appwidget_text, "Группа " + widgetText);
        else
            views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextViewText(R.id.appwidget_date, getDate(context, appWidgetId));
        Log.i("WoW", "Much Doge");
        Intent adapter = new Intent(context, FactoryService.class);
        adapter.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));

        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setRemoteAdapter(R.id.listLessons, adapter);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static String getDate(Context context, int widgetID) {
        SharedPreferences prefs = context.getSharedPreferences("widgets", 0);
        Day day;
        Gson gson = new Gson();
        String json;
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        if (prefs.getBoolean("Type_" + widgetID, false))
            json = helper.getPupilByDate("current");
        else
            json = helper.getTeacherByDate("current");
        day = gson.fromJson(json, Day.class);
        if (day != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Calendar calendar = Calendar.getInstance();
                Date date = format.parse(day.getDate());
                calendar.setTime(date);
                return Month.getMouthNyNumber(calendar.get(Calendar.MONTH) + 1) + " " + calendar.get(Calendar.DAY_OF_MONTH) + " (" + Month.getDatNyNumberMon(calendar.get(Calendar.DAY_OF_WEEK) - 1) + ")";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (helper != null)
            helper.close();
        return "";
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }


        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

