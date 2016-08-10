package ru.equestriadev.mgke;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.idunnololz.widgets.AnimatedExpandableListView;

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
import ru.equestriadev.widget.HomeWidget;


public class Teacher extends Fragment {

    ExpAdapter adapter;
    AnimatedExpandableListView listView;
    SwipeRefreshLayout refresher;


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
        listView = (AnimatedExpandableListView) getView().findViewById(R.id.teacherList);


        refresher = (SwipeRefreshLayout) getView().findViewById(R.id.refreshTeacher);
        refresher.setColorSchemeColors(R.color.colorPrimary);
        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresher.setRefreshing(true);
                executeNetworking(true);
            }
        });
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
                executeNetworking(false);
        } else {
            executeNetworking(false);
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


    public void executeNetworking(boolean isForced)
    {
        RequestPairs requestPairs = new RequestPairs();
        requestPairs.setTeacherFragment(this);
        requestPairs.setForced(isForced);
        requestPairs.execute();
    }

    public void onTop() {
        listView.smoothScrollToPosition(0);
    }

    public void setAdapter(Day day) {
        SharedPreferences myPrefs = getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if(refresher!=null)
            refresher.setRefreshing(false);
        if (day.getGroups() != null) {
            for (int i = 0; i < day.getGroups().size(); i++) {
                day.getGroups().get(i).setIsFavorite(myPrefs.getBoolean(day.getGroups().get(i).getTitle(), false));
            }
            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateStr = formatter.parse(day.getDate());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateStr);
                if(actionBar!=null) {
                    getActivity().setTitle("Преподаватели");
                    actionBar.setSubtitle(Month.getMouthNyNumber(calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.DAY_OF_MONTH) + " (" + Month.getDatNyNumberMon(calendar.get(Calendar.DAY_OF_WEEK) - 1) + ")");
                }
                } catch (ParseException e) {
                if(actionBar!=null)
                    actionBar.setSubtitle("???");
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
            if(listView!=null) {
                listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                        if (listView.isGroupExpanded(groupPosition)) {
                            listView.collapseGroupWithAnimation(groupPosition);
                        } else {
                            listView.expandGroupWithAnimation(groupPosition);
                        }
                        return true;
                    }
                });
                    listView.setAdapter(adapter);
            }
        }
        else
            Toast.makeText(getActivity().getApplicationContext(), "Включите интернет", Toast.LENGTH_SHORT).show();

    }


    public void updateWidgets(){
        Intent intent = new Intent(getContext(), HomeWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getActivity().getApplication()).getAppWidgetIds(new ComponentName(getActivity().getApplication(), HomeWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        getContext().sendBroadcast(intent);
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
