package ru.equestriadev.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ru.equestriadev.arch.Day;
import ru.equestriadev.mgke.DatabaseHelper;

/**
 * Created by Bronydell on 6/15/16.
 */
public class WidgetConfigure extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID; //Widget ID
    boolean isPupil = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        makeAWish();
    }

    private void makeAChoose()
    {
        final List<String> titles = new ArrayList<>();
        DatabaseHelper helper = DatabaseHelper.getInstance(getApplicationContext());
        Day doomsDay;
        String result;
        Gson gson = new Gson();
        if(isPupil)
            result = helper.getPupilByDate("current");
        else {
            Toast.makeText(getApplicationContext(), "Учитель", Toast.LENGTH_LONG).show();
            result = helper.getTeacherByDate("current");
        }
        if(result==null) {
            Toast.makeText(getApplicationContext(), "Зайдите в приложение и скачайте нужное расписание", Toast.LENGTH_LONG).show();
            finish();
        }
        doomsDay = gson.fromJson(result, Day.class);
        if(doomsDay!=null)
        {
            for(int i=0;i<doomsDay.getGroups().size();i++)
                titles.add(doomsDay.getGroups().get(i).getTitle());

            AlertDialog.Builder builder = new AlertDialog.Builder(WidgetConfigure.this);
            if(isPupil)
                builder.setTitle("Выберите группу");
            else
                builder.setTitle("Выберите преподавателя");
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                    final Context context = WidgetConfigure.this;
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("widgets", 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Title_" + mAppWidgetId, titles.get(id));
                    editor.putBoolean("Type_"+mAppWidgetId, isPupil);
                    editor.commit();
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                    //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hellowidget_layout);
                    //appWidgetManager.updateAppWidget(mAppWidgetId, views);
                    HomeWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            };
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            builder.setItems(titles.toArray(new CharSequence[titles.size()]), listener);
            AlertDialog alert = builder.create();

            alert.show();

        }
        else
        {
            Toast.makeText(getApplicationContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
        }
        if (helper != null)
            helper.close();
    }

    private void makeAWish()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(WidgetConfigure.this);
        builder.setTitle("Выберите тип расписания");
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                switch (id)
                {
                    case 0:
                        isPupil=false;
                        break;
                    case 1:
                        isPupil=true;
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Фича", Toast.LENGTH_SHORT).show();
                }
                makeAChoose();

            }
        };
        String [] titles = {"Для преподавателей", "Для учащихся"};
        builder.setItems(titles, listener);

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        AlertDialog alert = builder.create();

        alert.show();
    }


}
