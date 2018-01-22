package ru.equestriadev.mgke;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.idunnololz.widgets.AnimatedExpandableListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import ru.equestriadev.adapter.ExpAdapter;
import ru.equestriadev.arch.Day;
import ru.equestriadev.arch.Group;
import ru.equestriadev.arch.Lesson;
import ru.equestriadev.arch.Month;
import ru.equestriadev.arch.PairDate;
import ru.equestriadev.netwerking.RequestDates;
import ru.equestriadev.netwerking.RequestPairs;
import ru.equestriadev.parsing.Experiments;


public class Pupil extends BaseActivity {


    public static Pupil newInstance() {
        return new Pupil();
    }

    public void setAdapter(final Day day) {

        final Calendar calendar = Calendar.getInstance();
        if (refresher != null)
            refresher.setRefreshing(false);
        SharedPreferences myPrefs = null;
        if (getActivity() != null)
            myPrefs = getActivity().
                    getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if (day != null && day.getGroups() != null && myPrefs != null) {
            for (int i = 0; i < day.getGroups().size(); i++) {
                day.getGroups().get(i).
                        setIsFavorite(myPrefs.getBoolean(day.getGroups().get(i).getTitle(), false));
            }
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateStr = formatter.parse(day.getDate());
                calendar.setTime(dateStr);
                if (actionBar != null) {
                    getActivity().setTitle(activityTitle);
                    actionBar.setSubtitle(Month.getMouthNyNumber(calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.DAY_OF_MONTH) + " (" + Month.getDatNyNumberMon(calendar.get(Calendar.DAY_OF_WEEK) - 1) + ")");
                }
            } catch (ParseException e) {
                if (actionBar != null)
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
            if (getView() != null)
                listView = (AnimatedExpandableListView) getView().findViewById(R.id.pupilList);
            if (listView != null) {
                adapter = new ExpAdapter(getContext(), day, true);
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
                listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        PairDate dater = new PairDate();
                        Lesson lesson = day.getGroups().get(groupPosition).getLessons().get(childPosition);
                        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            Toast.makeText(getContext(),
                                    dater.getSaturday(lesson.getNumber(), Experiments.isKn(lesson.getAudience())),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    dater.getAnotherOne(lesson.getNumber(), Experiments.isKn(lesson.getAudience())),
                                    Toast.LENGTH_SHORT).show();

                        }
                        return false;
                    }
                });
                listView.setAdapter(adapter);
            }
        } else {
            if (getActivity() != null && getActivity().getApplicationContext() != null)
                Toast.makeText(getActivity().getApplicationContext(), "Включите интернет", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void openCalendar() {
        RequestDates requestDates = new RequestDates();
        requestDates.setTeacherFragment(this);
        requestDates.execute();
    }


    public void executeNetworking(boolean forced) {
        if (refresher != null)
            refresher.setRefreshing(true);

        RequestPairs requestPairs = new RequestPairs();
        requestPairs.setPupilFragment(this);
        requestPairs.setForced(forced);
        requestPairs.execute();
    }

}
