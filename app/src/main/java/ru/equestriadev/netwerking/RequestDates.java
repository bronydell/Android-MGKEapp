package ru.equestriadev.netwerking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.equestriadev.arch.Day;
import ru.equestriadev.datetimepicker.date.DatePickerDialog;
import ru.equestriadev.mgke.BaseActivity;
import ru.equestriadev.mgke.DatabaseHelper;

/**
 * Created by Bronydell on 6/14/16.
 */
public class RequestDates extends AsyncTask<Void, Void, Void> implements DatePickerDialog.OnDateSetListener {

    static DatePickerDialog datePickerDialog;
    private String baseURL = "https://msce.bronydell.xyz/method/";
    private List<Calendar> days = new ArrayList<Calendar>();
    private Context context;
    private BaseActivity teacher;
    private BaseActivity pupil;
    private boolean isPupil;

    public void setTeacherFragment(BaseActivity teacher) {
        this.teacher = teacher;
        context = teacher.getContext();
        baseURL += "getTeacherDates";
        isPupil = false;
    }


    public void setPupilFragment(BaseActivity pupil) {
        this.pupil = pupil;
        context = pupil.getContext();
        baseURL += "getStudentDates";
        isPupil = true;
    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPreExecute() {

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
        Day dd = null;
        if (!isPupil)
            dd = teacher.getAdapterDay();
        else
            dd = pupil.getAdapterDay();
        Calendar calendar = Calendar.getInstance();
        if (dd != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                calendar.setTime(sdf.parse(dd.getDate()));
            } catch (ParseException e) {
                Log.e("Error", "Error parse current day");
            }
        }

        if (days.size() > 0) {
            if (datePickerDialog != null && !datePickerDialog.isVisible())
                datePickerDialog = null;
            if (datePickerDialog == null) {
                datePickerDialog = DatePickerDialog.newInstance(null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false, days);

                datePickerDialog.setFirstDayOfWeek(2);
                datePickerDialog.setCloseOnSingleTapDay(false);
                if (pupil != null)
                    datePickerDialog.show(pupil.getFragmentManager(), "DATE_MANAGER");
                else if (teacher != null)
                    datePickerDialog.show(teacher.getFragmentManager(), "DATE_MANAGER");
                datePickerDialog.setOnDateSetListener(this);
            }
        } else {
            Toast.makeText(context, "Нет доступных дат. Включите интернет!", Toast.LENGTH_LONG).show();
        }
    }

    public List<Calendar> getOffline() {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);

        List<Calendar> objs = helper.getAllDates(isPupil);

        return objs;
    }


    public List<Calendar> getOnline() {
        try {
            String response = NetworkMethods.readUrl(baseURL);
            JSONObject obj = new JSONObject(response);
            JSONArray arr = obj.getJSONObject("data").getJSONArray("dates");
            List<Calendar> objs = new ArrayList<Calendar>();
            for (int i = 0; i < arr.length(); i++) {
                Calendar cal = Calendar.getInstance();
                Date date;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                date = format.parse(arr.getString(i));
                cal.setTime(date);
                objs.add(cal);
            }
            return objs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        RequestPairs pairs = new RequestPairs();

        if (!isPupil)
            pairs.setTeacherFragment(teacher);
        else
            pairs.setPupilFragment(pupil);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        date.setDate(day);
        date.setMonth(month);
        date.setYear(year - 1900);
        RequestDates.datePickerDialog = null;
        pairs.execute(format.format(date));
    }


}
