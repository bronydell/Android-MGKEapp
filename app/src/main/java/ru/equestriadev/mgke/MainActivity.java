package ru.equestriadev.mgke;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.List;

import ru.equestriadev.notify.UpdateService;


public class MainActivity extends AppCompatActivity {

    private BottomBar mBottomBar;
    private FragNavController mNavController;

    private Pupil pupilFragment;
    private Teacher teacherFragment;
    //Better convention to properly name the indices what they are in your app

    private final int INDEX_STUDENT = FragNavController.TAB1;
    private final int INDEX_TEACHER = FragNavController.TAB2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), "10b73f7b-e0f2-49dd-b27f-545687ea021e");
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(getApplication());
        //new IntroductionBuilder(this).withSlides(generateSlides()).introduceMyself();
        SharedPreferences myPrefs = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = myPrefs.edit();
        if(myPrefs.getBoolean("Auto", false))
            startService(new Intent(this, UpdateService.class));
        mNavController = getController(savedInstanceState);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottom_menu);
        if(myPrefs.getInt("State", 0)==1&&mBottomBar.getCurrentTabPosition()!=1)
            mBottomBar.selectTabAtPosition(1, false);
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                switch(menuItemId)
                {

                    case  R.id.teacher:
                        editor.putInt("State", 1);
                        editor.commit();
                        mNavController.switchTab(INDEX_TEACHER);
                        break;
                    case  R.id.pupil:
                        editor.putInt("State", 0);
                        editor.commit();
                        mNavController.switchTab(INDEX_STUDENT);
                        break;
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

                switch(menuItemId)
                {
                    case  R.id.teacher:
                        if(teacherFragment!=null)
                            teacherFragment.onTop();
                        break;
                    case  R.id.pupil:
                        if(pupilFragment!=null)
                            pupilFragment.onTop();
                        break;
                }
            }
        });

    }


    public FragNavController getController(Bundle savedInstanceState)
    {

        List<Fragment> fragments = new ArrayList<>(3);
        pupilFragment = Pupil.newInstance();
        teacherFragment = Teacher.newInstance();
        fragments.add(pupilFragment);
        fragments.add(teacherFragment);

        return
                new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.container, fragments);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mBottomBar.onSaveInstanceState(outState);
        mNavController.onSaveInstanceState(outState);
    }
    @Override
    public void onBackPressed() {
        if (mNavController.getCurrentStack().size() > 1) {
            mNavController.pop();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        SharedPreferences myPrefs = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        // Операции для выбранного пункта меню
        switch (id) {

            case R.id.update:
                if(item.isChecked()) {
                    item.setChecked(false);
                    stopService(new Intent(this, UpdateService.class));
                }
                else {
                    item.setChecked(true);
                    startService(new Intent(this, UpdateService.class));
                }
                editor.putBoolean("Auto", item.isChecked());
                editor.commit();
                return true;
            case R.id.about:
                showAbout();
                return true;
            case R.id.browse:

                wtf();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SharedPreferences myPrefs = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        menu.findItem(R.id.update).setChecked(myPrefs.getBoolean("Auto", false));

        return super.onCreateOptionsMenu(menu);
    }


    public void showAbout(){
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            ((TextView)builder.setTitle("О приложении")
                    .setMessage("Приложение основано на расписании с сайта колледжа электроники, " +
                            "автор не несет ответственности за правильность расписания(приложение " +
                            "является прослойкой между расписанием и пользователем). Если на сайте " +
                            "происходит так называемый сдвиг расписания, то приложение не сможет " +
                            "этого понять(такое понимание невозможно реализовать с технической" +
                            " точки зрения).\n\n" +
                            "Автор: Никишин Ростислав\n" +
                            "Номер сборки: " + pInfo.versionCode + "\n" +
                            "Версия: " + pInfo.versionName + "\n")
                    .setCancelable(true)
                    .setPositiveButton("Написать про баг", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto","littleponyapps@gmail.com", null));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Приложение расписание МГКЭ");
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "Опишите что случилось или " +
                                    "что надо");
                            startActivity(Intent.createChooser(emailIntent, "Отправить сообщение..."));
                        }
                    })
                    .setNegativeButton("Закрыть",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .show()
                    .findViewById(android.R.id.message)
                    ).setMovementMethod(LinkMovementMethod.getInstance());

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void wtf()
    {
        final CharSequence[] items = {
                "Кнорина на день",
                "Казинца на день",
                "Для преподователей на день",
                "Кнорина на неделю",
                "Казинца на неделю",
                "Для преподователей на неделю",
        };

        final String[] url = {
                "http://mgke.minsk.edu.by/ru/main.aspx?guid=3841",
                "http://mgke.minsk.edu.by/ru/main.aspx?guid=3831",
                "http://mgke.minsk.edu.by/ru/main.aspx?guid=3821",
                "http://mgke.minsk.edu.by/ru/main.aspx?guid=3791",
                "http://mgke.minsk.edu.by/ru/main.aspx?guid=3781",
                "http://mgke.minsk.edu.by/ru/main.aspx?guid=3811"

        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Что открываем?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse(url[item]));
                startActivity(intent);

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        YandexMetrica.onResumeActivity(this);
    }

    @Override
    protected void onPause() {
        YandexMetrica.onPauseActivity(this);
        super.onPause();
    }

}
