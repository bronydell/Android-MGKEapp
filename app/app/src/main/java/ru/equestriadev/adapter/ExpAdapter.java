package ru.equestriadev.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;

import ru.equestriadev.arch.Day;
import ru.equestriadev.arch.Group;
import ru.equestriadev.arch.Lesson;
import ru.equestriadev.mgke.R;

/**
 * Created by Bronydell on 6/8/16.
 */
public class ExpAdapter extends BaseExpandableListAdapter {

    private Day day;
    private Context mContext;
    private boolean isPupil;
    public ExpAdapter(Context context, Day day, boolean isPupil) {
        mContext = context;
        this.day = day;
        this.isPupil=isPupil;
    }

    public int getChildrenCount(int groupPosition) {
        return day.getGroups().get(groupPosition).getLessons().size();
    }

    public Group getGroup(int groupPosition) {
        return day.getGroups().get(groupPosition);
    }

    public int getGroupCount() {
        return day.getGroups().size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public Lesson getChild(int groupPosition, int childPosition) {
        return day.getGroups().get(groupPosition).getLessons().get(childPosition);
    }

    public Day getDay() {
        return day;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_title, null);
        }


        TextView textGroup = (TextView) convertView.findViewById(R.id.title);
        if(isPupil)
            textGroup.setText("Группа "+day.getGroups().get(groupPosition).getTitle());
        else
            textGroup.setText(day.getGroups().get(groupPosition).getTitle());
        final LikeButton button = (LikeButton) convertView.findViewById(R.id.fave);
        SharedPreferences myPrefs = mContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);

        button.setLiked(myPrefs.getBoolean(day.getGroups().get(groupPosition).getTitle(), false));
        button.setFocusable(false);
        button.setOnLikeListener(new OnLikeListener() {

            SharedPreferences myPrefs = mContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = myPrefs.edit();

            @Override
            public void liked(LikeButton likeButton) {
                edit.putBoolean(day.getGroups().get(groupPosition).getTitle(), true);
                edit.commit();
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                edit.putBoolean(day.getGroups().get(groupPosition).getTitle(), false);
                edit.commit();
            }

        });

        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_lesson, null);
        }
        TextView textChild = (TextView) convertView.findViewById(R.id.textChild);
        Lesson lesson = day.getGroups().get(groupPosition).getLessons().get(childPosition);
        textChild.setText(Html.fromHtml("<b>" + lesson.getNumber() + "</b>. " + lesson.getLesson() + " в кабинете(кабинетах): <b>" + lesson.getAudience() + "</b>"));
        convertView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_in));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}