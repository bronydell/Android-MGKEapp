package ru.equestriadev.netwerking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import ru.equestriadev.arch.Day;
import ru.equestriadev.mgke.DatabaseHelper;
import ru.equestriadev.mgke.Pupil;
import ru.equestriadev.mgke.Teacher;

/**
 * Created by Bronydell on 6/14/16.
 */
public class RequestPairs extends AsyncTask<String, Void, Void> {


    private Teacher teacher;
    private boolean isNeedToSave = true;
    private Pupil pupil;
    private Context context;
    private Day nowDay;
    private DatabaseHelper helper;

    String baseURL = "http://s1.al3xable.me/method/";

    private boolean isPupil;

    public void setTeacherFragment(Teacher teacher)
    {
        this.teacher = teacher;
        context = teacher.getContext();
        baseURL+="getTeacher";
        isPupil = false;
    }

    public void setPupilFragment(Pupil pupil)
    {
        this.pupil = pupil;
        context = pupil.getContext();
        baseURL+="getStudent";
        isPupil = true;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        nowDay = new Day();
        helper = new DatabaseHelper(context);
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length > 0) {
            baseURL += "&date=" + params[0];
            isNeedToSave = false;
        }
        if (isOnline()) {
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

        Log.d("Debug", baseURL);
        if(isPupil)
            pupil.setAdapter(nowDay);
        else
            teacher.setAdapter(nowDay);
    }

    private Day getOnline() {
        Day day = new Day();
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
        return day;
    }

    private Day getOffline() {
        Day day = new Day();
        String dd;
        if(!isPupil)
            dd = helper.getTeacherByDate("current");
        else
            dd = helper.getPupilByDate("current");
        //Log.i("Offline", dd);
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
        //Log.i("Offline", dd);
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

}