package ru.equestriadev.netwerking;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Date;

import ru.equestriadev.arch.Day;
import ru.equestriadev.mgke.BaseActivity;
import ru.equestriadev.mgke.DatabaseHelper;

/**
 * Created by Bronydell on 6/14/16.
 */
public class RequestPairs extends AsyncTask<String, Void, Void> {


    private String baseURL = "https://msce.bronydell.xyz/method/";
    private BaseActivity teacher;
    private boolean isNeedToSave = true;
    private BaseActivity pupil;
    private Context context;
    private Day nowDay;
    private DatabaseHelper helper;
    private boolean isPupil;
    private boolean isForced = true;

    public void setTeacherFragment(BaseActivity teacher)
    {
        this.teacher = teacher;
        context = teacher.getContext();
        baseURL+="getTeacher";
        isPupil = false;
    }

    public void setPupilFragment(BaseActivity pupil)
    {
        this.pupil = pupil;
        context = pupil.getContext();
        baseURL+="getStudent";
        isPupil = true;
    }

    public void setForced(boolean forced)
    {
        isForced=forced;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        nowDay = new Day();
        helper = DatabaseHelper.getInstance(context);
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length > 0) {
            baseURL += "&date=" + params[0];
            isNeedToSave = false;
        }
        //If last update was 1 hours ago or later
        String off = helper.getPupilByDate("current");
        if (!isPupil)
                off = helper.getTeacherByDate("current");

        if ((off == null && isOnline()) || (off.equals("") && isOnline()) ||
                (isForced && isOnline()) || (isOnline() && getUpdate() + 1000 * 60 * 60 < getCurrent())) {
            nowDay = getOnline();
        } else {
            if (params.length > 0)
                nowDay = getOffline(params[0]);
            else
                nowDay = getOffline();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if(isPupil)
            pupil.setAdapter(nowDay);
        else
            teacher.setAdapter(nowDay);
        updateWidget();
    }

    private Day getOnline() {
        Day day = new Day();
        Log.i("Netwerking", "Online. API URL: " + baseURL);
        try {
            Gson gson = new Gson();
            String feedback;
            feedback = NetworkMethods.readUrl(baseURL);

            JSONObject obj = new JSONObject(feedback);
            if (obj.getInt("code") == 0) {
                day = gson.fromJson(obj.getJSONObject("data").toString(), Day.class);
                saveByDate(day.getDate(), day);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        UpdateTime();
        return day;
    }

    public Day getOffline() {
        Log.i("Netwerking" , "Offline");
        Day day = new Day();
        String dd;
        if(!isPupil)
            dd = helper.getTeacherByDate("current");
        else
            dd = helper.getPupilByDate("current");
        Gson gson = new Gson();
        if (dd != null)
            day = gson.fromJson(dd, Day.class);
        return day;
    }

    private Day getOffline(String date) {
        Day day = new Day();
        String dd;
        if(!isPupil)
            dd = helper.getTeacherByDate(date);
        else
            dd = helper.getPupilByDate(date);
        Gson gson = new Gson();
        if (dd != null)
            day = gson.fromJson(dd, Day.class);
        return day;
    }

    private void saveByDate(String date, Day day) {
        Gson gson = new Gson();
        if(!isPupil) {
            helper.putTeacher(date, gson.toJson(day, Day.class));
            if(isNeedToSave)
                helper.putTeacher("current", gson.toJson(day, Day.class));
        }
        else {
            helper.putPupil(date, gson.toJson(day, Day.class));
            if(isNeedToSave)
                helper.putPupil("current", gson.toJson(day, Day.class));
        }
    }

    private boolean isOnline()
    {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    public long getCurrent()
    {
        //getting the current time in milliseconds, and creating a Date object from it:
        Date date = new Date(System.currentTimeMillis()); //or simply new Date();
        //converting it back to a milliseconds representation:
        return date.getTime();
    }

    public void UpdateTime()
    {
        //getting the current time in milliseconds, and creating a Date object from it:
        Date date = new Date(System.currentTimeMillis()); //or simply new Date();
        //converting it back to a milliseconds representation:
        long millis = date.getTime();
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(isPupil)
            editor.putLong("time_teacher", date.getTime());
        else
            editor.putLong("time_student", date.getTime());
        editor.commit();
    }

    public long getUpdate()
    {
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if(isPupil)
            return prefs.getLong("time_teacher", 0);
        else
            return prefs.getLong("time_student", 0);
    }

    public void updateWidget()
    {
        if(pupil!=null)
            pupil.updateWidgets(pupil.getActivity());
        else if(teacher!=null)
            teacher.updateWidgets(teacher.getActivity());
    }
}