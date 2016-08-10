package ru.equestriadev.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import ru.equestriadev.arch.Day;
import ru.equestriadev.arch.Group;
import ru.equestriadev.arch.Lesson;
import ru.equestriadev.mgke.DatabaseHelper;
import ru.equestriadev.mgke.R;

/**
 * Created by Bronydell on 6/16/16.
 */
public class ListFactory implements RemoteViewsService.RemoteViewsFactory {

    Group group;
    Context context;
    int widgetID;


    ListFactory(Context context, Intent intent)
    {
        this.context=context;
        updateContent();
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);


        Log.i("Info", widgetID+" idid");
    }
    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        //?

       updateContent();

    }

    public void updateContent()
    {
        SharedPreferences prefs = context.getSharedPreferences("widgets", 0);
        Day day;
        Gson gson = new Gson();
        String json;
        DatabaseHelper helper = new DatabaseHelper(context);
        if(prefs.getBoolean("Type_"+widgetID, false))
            json=helper.getPupilByDate("current");
        else
            json=helper.getTeacherByDate("current");
        day = gson.fromJson(json, Day.class);
        if(day!=null)
        for(Group group: day.getGroups())
        {
            if(group.getTitle().equals(prefs.getString("Title_"+widgetID, "Error")))
            {
                this.group=group;
                break;
            }
        }
        else
        {
            this.group= new Group();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
       if(group.getLessons()!=null)
           return group.getLessons().size();
       else
           return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rView = new RemoteViews(context.getPackageName(),
                R.layout.widget_list_item);
        Lesson lesson = group.getLessons().get(position);
        rView.setTextViewText(R.id.lessonTitle, Html.fromHtml("<b>" + lesson.getNumber() + "</b>. " + lesson.getLesson() + " в кабинете(кабинетах): <b>" + lesson.getAudience() + "</b>"));
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
