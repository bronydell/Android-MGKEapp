package ru.equestriadev.mgke;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

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
        SharedPreferences myPrefs = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if(myPrefs.getBoolean("Auto", false))
            startService(new Intent(this, UpdateService.class));
        mNavController = getController(savedInstanceState);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottom_menu);
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                switch(menuItemId)
                {
                    case  R.id.teacher:
                        mNavController.switchTab(INDEX_TEACHER);
                        break;
                    case  R.id.pupil:
                        mNavController.switchTab(INDEX_STUDENT);
                        break;
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

                switch(menuItemId)
                {
                    case  R.id.teacher:
                        teacherFragment.onTop();
                        break;
                    case  R.id.pupil:
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


}
