package ru.equestriadev.netwerking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.equestriadev.mgke.DatabaseHelper;
import ru.equestriadev.mgke.Pupil;
import ru.equestriadev.mgke.Teacher;

/**
 * Created by Bronydell on 6/14/16.
 */
public class RequestDates extends AsyncTask<Void, Void, Void> {

    String baseURL = "http://s1.al3xable.me/method/";
    private List<String> days = new ArrayList<String>();

    private Context context;
    private Teacher teacher;
    private Pupil pupil;

    private boolean isPupil;
    public void setTeacherFragment(Teacher teacher)
    {
        this.teacher = teacher;
        context = teacher.getContext();
        baseURL+="getTeacherDates";
        isPupil = false;
    }


    public void setPupilFragment(Pupil pupil)
    {
        this.pupil = pupil;
        context = pupil.getContext();
        baseURL+="getStudentDates";
        isPupil = true;
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

    @Override
    protected void onPreExecute()
    {

        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (isOnline()) {
            days = getOnline();
        } else {
            days = getOffline();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Выберите нужную дату");
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                RequestPairs pairs = new RequestPairs();
                if(!isPupil)
                    pairs.setTeacherFragment(teacher);
                else
                    pairs.setPupilFragment(pupil);
                try {
                    Date dt = new SimpleDateFormat("dd.MM.yyyy").parse(days.get(id));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    pairs.execute(dateFormat.format(dt));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        builder.setItems(days.toArray(new CharSequence[days.size()]), listener);
        AlertDialog alert = builder.create();

        alert.show();
    }

    public List<String> getOffline()
    {
        DatabaseHelper helper = new DatabaseHelper(context);

        List<String> objs = helper.getAllDates();
        for (int i = 0; i < objs.size(); i++) {
            Date dt = null;
            try {
                dt = new SimpleDateFormat("yyyy-MM-dd").parse(objs.get(i));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                objs.remove(i);
                objs.add(dateFormat.format(dt));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (objs.size() > 0) {
            objs.remove(objs.size() - 1);
            Collections.sort(objs);
        }

        return objs;
    }

    public List<String> getOnline()
    {
        try {
            String response = NetworkMethods.readUrl(baseURL);
            JSONObject obj = new JSONObject(response);
            JSONArray arr = obj.getJSONObject("data").getJSONArray("dates");
            List<String> objs = new ArrayList<String>();
            for (int i = 0; i < arr.length(); i++) {
                Date dt = new SimpleDateFormat("yyyy-MM-dd").parse(arr.getString(i));
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                objs.add(dateFormat.format(dt));
            }
            Collections.sort(objs);
            return objs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
