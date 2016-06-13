package ru.equestriadev.mgke;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomBar mBottomBar;
    private FragNavController mNavController;

    //Better convention to properly name the indices what they are in your app

    private final int INDEX_STUDENT = FragNavController.TAB1;
    private final int INDEX_TEACHER = FragNavController.TAB2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavController = getController(savedInstanceState);

        //mNavController.switchTab(INDEX_EVENTS);
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
                        break;
                    case  R.id.pupil:
                        break;
                }
            }
        });
    }


    public FragNavController getController(Bundle savedInstanceState)
    {

        List<Fragment> fragments = new ArrayList<>(3);

        fragments.add(Pupil.newInstance());
        fragments.add(Teacher.newInstance());

        return
                new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.container, fragments);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
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

    public static String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month-1];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
