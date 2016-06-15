package ru.equestriadev.mgke;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import ru.equestriadev.netwerking.RequestDates;
import ru.equestriadev.netwerking.RequestPairs;
import ru.equestriadev.adapter.ExpAdapter;
import ru.equestriadev.arch.Day;
import ru.equestriadev.arch.Group;
import ru.equestriadev.arch.Month;


public class Teacher extends Fragment {

    ExpAdapter adapter;
    ExpandableListView listView;


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
                executeNetworking();
        } else {
            executeNetworking();
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


    public void executeNetworking()
    {
        RequestPairs requestPairs = new RequestPairs();
        requestPairs.setTeacherFragment(this);
        requestPairs.execute();
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
                getActivity().setTitle("Преподаватели");
                ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
                actionBar.setSubtitle(Month.getMouthNyNumber(calendar.get(Calendar.MONTH) + 1) + " " + calendar.get(Calendar.DAY_OF_MONTH) + " (" + Month.getDatNyNumber(calendar.get(Calendar.DAY_OF_WEEK) - 1) + ")");
            } catch (ParseException e) {
                getActivity().setTitle("Преподаватели. ???");
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
            adapter = new ExpAdapter(getContext(), day, false);
        }
        listView.setAdapter(adapter);

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.event:
                RequestDates requestDates = new RequestDates();
                requestDates.setTeacherFragment(this);
                requestDates.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
