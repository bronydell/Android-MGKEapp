package ru.equestriadev.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by Bronydell on 6/16/16.
 */
public class FactoryService extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("WoW", "Such Doge");
        return new ListFactory(getApplicationContext(), intent);
    }

}