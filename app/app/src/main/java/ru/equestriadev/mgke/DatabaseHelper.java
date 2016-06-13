package ru.equestriadev.mgke;

/**
 * Created by Bronydell on 6/10/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    // названия столбцов
    public static final String DATE_COLUMN = "date";
    public static final String PUPIL_COLUMN = "pupil";
    public static final String TEACHER_COLUMN = "teacher";
    // имя базы данных
    private static final String DATABASE_NAME = "offline.db";
    // версия базы данных
    private static final int DATABASE_VERSION = 1;
    // имя таблицы
    private static final String DATABASE_TABLE = "days";
    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + DATE_COLUMN
            + " string primary key, " + PUPIL_COLUMN
            + " text, " + TEACHER_COLUMN + " text);";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public void putAll(String date, String pupil, String teacher) {
        ContentValues values = new ContentValues();
        values.put(DATE_COLUMN, date);
        values.put(PUPIL_COLUMN, pupil);
        values.put(TEACHER_COLUMN, teacher);

        getWritableDatabase().insertWithOnConflict(DATABASE_TABLE, null, values, getWritableDatabase().CONFLICT_REPLACE);
        getWritableDatabase().close();
    }

    public void putPupil(String date, String pupil) {
        ContentValues values = new ContentValues();
        values.put(DATE_COLUMN, date);
        values.put(PUPIL_COLUMN, pupil);
        //values.put(TEACHER_COLUMN, teacher);

        getWritableDatabase().insertWithOnConflict(DATABASE_TABLE, null, values, getWritableDatabase().CONFLICT_REPLACE);
        getWritableDatabase().close();
    }

    public void putTeacher(String date, String teacher) {
        ContentValues values = new ContentValues();
        values.put(DATE_COLUMN, date);
        //values.put(PUPIL_COLUMN, pupil);
        values.put(TEACHER_COLUMN, teacher);

        getWritableDatabase().insertWithOnConflict(DATABASE_TABLE, null, values, getWritableDatabase().CONFLICT_REPLACE);
        getWritableDatabase().close();
    }

    public String getTeacherByDate(String date)
    {
        String q="SELECT * FROM "+DATABASE_TABLE+" WHERE "+DATE_COLUMN+"='" + date +"';";

        Cursor cursor = getReadableDatabase().rawQuery(q, null);

        if (cursor != null) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEACHER_COLUMN));
        }
        cursor.close();
        return null;
    }

    public String getPupilByDate(String date)
    {
        String q="SELECT * FROM "+DATABASE_TABLE+" WHERE "+DATE_COLUMN+"='" + date+"';";

        Cursor cursor = getReadableDatabase().rawQuery(q, null);

        if (cursor != null) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(DatabaseHelper.PUPIL_COLUMN));
        }

        cursor.close();
        return null;
    }


    public List<String> getAllDates(){
        List<String> results = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query
        String selectQuery = "select * from " + DATABASE_TABLE;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                results.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return results;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Запишем в журнал
        Log.w("SQLite", "Update " + oldVersion + " on version " + newVersion);

        // Удаляем старую таблицу и создаём новую
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
        // Создаём новую таблицу
        onCreate(db);
    }
}
