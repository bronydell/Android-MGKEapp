package ru.equestriadev.mgke;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ru.equestriadev.adapter.ExpAdapter;
import ru.equestriadev.arch.Day;
import ru.equestriadev.arch.Group;
import ru.equestriadev.arch.Month;


public class Teacher extends Fragment {

    ExpAdapter adapter;
    ExpandableListView listView;
    DatabaseHelper helper;




    public static Teacher newInstance() {
        Teacher fragment = new Teacher();
        return fragment;
    }

    public Teacher() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Init ListView
        listView = (ExpandableListView) getView().findViewById(R.id.teacherList);
        helper = new DatabaseHelper(getContext());
        //Cool feature
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int offset = position - listView.getFirstVisiblePosition();
                if (listView.getFirstVisiblePosition() > 0)
                    offset -= 1;

                listView.smoothScrollByOffset(offset);
            }
        });

        getActivity().setTitle("Преподаватели");
        //Update content!
        if (savedInstanceState != null) {
            Gson gson = new Gson();
            Day day = gson.fromJson(savedInstanceState.getString("content"), Day.class);
            if (day != null) {
                setAdapter(day);
                listView.onRestoreInstanceState(savedInstanceState.getParcelable("list"));
            } else
                new TeacherExecute().execute();
        } else {
            new TeacherExecute().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teacher, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        Gson gson = new Gson();
        if (adapter != null) {
            saveState.putString("content", gson.toJson(adapter.getDay()));
            saveState.putParcelable("list", listView.onSaveInstanceState());
        }
    }

    class TeacherExecute extends AsyncTask<String, Void, Void> {

        String baseURL = "http://s1.al3xable.me/api/?method=getTeacher";
        Day nowDay;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nowDay = new Day();
        }

        @Override
        protected Void doInBackground(String... params) {
            if(params.length>0)
                baseURL+="&date="+params[0];
            if (isOnline()) {
                nowDay = getOnline();
            } else {
                nowDay = getOffline();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Log.d("Debug", baseURL);
            setAdapter(nowDay);
        }

        private Day getOnline() {
            Day day = new Day();
            try {
                Gson gson = new Gson();
                String feedback;
                feedback = readUrl(baseURL);

                JSONObject obj = new JSONObject(feedback);
                if(obj.getInt("code")==0) {
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
            String dd = helper.getTeacherByDate("current");
            Gson gson = new Gson();
            if (dd != null)
                day = gson.fromJson(dd, Day.class);
            return day;
        }

        private void saveByDate(String date, Day day) {
            Gson gson = new Gson();
            helper.putTeacher(date, gson.toJson(day, Day.class));
            helper.putTeacher("current", gson.toJson(day, Day.class));
        }


    }

    public String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
    public void onTop() {
        listView.smoothScrollToPosition(0);
    }
    public void setAdapter(Day day) {
        SharedPreferences myPrefs = getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if (day.getGroups() != null) {
            for (int i = 0; i < day.getGroups().size(); i++) {
                day.getGroups().get(i).setIsFavorite(myPrefs.getBoolean(day.getGroups().get(i).getTitle(), false));
            }

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateStr = formatter.parse(day.getDate());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateStr);
                getActivity().setTitle("Преподаватели " + Month.getMouthNyNumber(calendar.get(Calendar.MONTH) + 1) + " " + calendar.get(Calendar.DAY_OF_MONTH));
            } catch (ParseException e) {
                getActivity().setTitle("Преподаватели ???");
                e.printStackTrace();
            }

            Collections.sort(day.getGroups(), new Comparator<Group>() {
                @Override
                public int compare(final Group object1, final Group object2) {
                    boolean b1 = object1.isFavorite();
                    boolean b2 = object2.isFavorite();
                    if (b1 && !b2) {
                        return -1;
                    }
                    if (!b1 && b2) {
                        return +1;
                    }

                    return 0;
                }
            });
            adapter = new ExpAdapter(getContext(), day);
        }
        listView.setAdapter(adapter);

    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.event:
                new TeacherDates().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class TeacherDates extends AsyncTask<Void, Void, Void> {

        String baseURL = "http://s1.al3xable.me/api/?method=getTeacherDates";
        List<String> days = new ArrayList<String>();

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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Выберите нужную дату");
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    new TeacherExecute().execute(days.get(id));
                }
            };

            builder.setItems(days.toArray(new CharSequence[days.size()]), listener);
            AlertDialog alert = builder.create();

            alert.show();
        }

        public List<String> getOffline()
        {
            DatabaseHelper helper = new DatabaseHelper(getContext());
            return helper.getAllDates();
        }

        public List<String> getOnline()
        {
            try {
                String responce = readUrl(baseURL);
                JSONObject obj = new JSONObject(responce);
                JSONArray arr = obj.getJSONObject("data").getJSONArray("dates");
                List<String> objs = new ArrayList<String>();
                for(int i=0;i<arr.length();i++)
                    objs.add(arr.getString(i));
                return objs;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
