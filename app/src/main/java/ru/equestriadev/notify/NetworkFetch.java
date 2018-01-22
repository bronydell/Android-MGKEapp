package ru.equestriadev.notify;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import ru.equestriadev.arch.Day;
import ru.equestriadev.mgke.DatabaseHelper;
import ru.equestriadev.mgke.MainActivity;
import ru.equestriadev.mgke.R;
import ru.equestriadev.netwerking.NetworkMethods;
import ru.equestriadev.widget.HomeWidget;

/**
 * Created by Bronydell on 6/17/16.
 */
public class NetworkFetch extends AsyncTask<String, Void, Void> {


    String baseURL = "https://msce.bronydell.xyz/method/";
    private Context context;
    private DatabaseHelper helper;
    private boolean isUpdated = false;

    public void setContext(Context context)
    {
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        helper = DatabaseHelper.getInstance(context);

    }

    @Override
    protected Void doInBackground(String ... params) {
        if (isOnline()){
            getOnline(true);
            getOnline(false);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if(isUpdated)
        {
            addNotification();
            try {
                updateWidgets();
            } catch (Exception ex) {
                Log.e("MSCE", ex.toString());
            }
        }
    }

    private Day getOnline(boolean isPupil) {
        Day day = new Day();
        try {
            Gson gson = new Gson();
            String feedback;
            if(isPupil)
                feedback = NetworkMethods.readUrl(baseURL+"getStudent");
            else
                feedback = NetworkMethods.readUrl(baseURL+"getTeacher");
            JSONObject obj = new JSONObject(feedback);
            if (obj.getInt("code") == 0) {
                day = gson.fromJson(obj.getJSONObject("data").toString(), Day.class);
                saveByDate(day.getDate(), day, isPupil);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return day;
    }


    private void saveByDate(String date, Day day, boolean isPupil) {
        Gson gson = new Gson();
        if (!isPupil && helper.getTeacherByDate(date)==null) {
            helper.putTeacher(date, gson.toJson(day, Day.class));
            helper.putTeacher("current", gson.toJson(day, Day.class));
            isUpdated = true;
        } else if (isPupil && helper.getPupilByDate(date)==null){
            helper.putPupil(date, gson.toJson(day, Day.class));
            helper.putPupil("current", gson.toJson(day, Day.class));
            isUpdated = true;
        }
    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    private void addNotification() {



        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_event)
                        .setContentTitle("Расписание обновлено")
                        .setAutoCancel(true)
                        .setContentText("Загружено свежее расписание!");


        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(228, builder.build());
    }

    public void updateWidgets() {
        Intent intent = new Intent(context, HomeWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(context).
                getAppWidgetIds(new ComponentName(context, HomeWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }


}