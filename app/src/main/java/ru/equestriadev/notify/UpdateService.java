package ru.equestriadev.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Bronydell on 6/17/16.
 */
public class UpdateService extends Service {

    Calendar cur_cal = Calendar.getInstance();

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Intent intent = new Intent(this, UpdateService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(),
                0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                60 * 60 * 1000 * 3, pintent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        SharedPreferences myPrefs = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        Log.i("Service", "Service is starting...");
        if(myPrefs.getBoolean("Auto", false)) {
            NetworkFetch fetch = new NetworkFetch();
            fetch.setContext(getApplicationContext());
            fetch.execute();
        }
    }
}