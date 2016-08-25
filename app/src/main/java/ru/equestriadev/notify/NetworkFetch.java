package ru.equestriadev.notify;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;

import org.json.JSONObject;

import ru.equestriadev.arch.Day;
import ru.equestriadev.mgke.DatabaseHelper;
import ru.equestriadev.mgke.MainActivity;
import ru.equestriadev.mgke.R;
import ru.equestriadev.netwerking.NetworkMethods;

/**
 * Created by Bronydell on 6/17/16.
 */
public class NetworkFetch extends AsyncTask<String, Void, Void> {


    String baseURL = "http://s1.al3xable.me/method/";
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
        }
        if (helper != null)
            helper.close();
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
                        .setContentText("Загружено свежее расписание!");

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(228, builder.build());
    }


}