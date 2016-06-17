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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    public DatabaseHelper(Context context) {
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
        SQLiteDatabase db = getWritableDatabase();
        values.put(DATE_COLUMN, date);
        values.put(PUPIL_COLUMN, pupil);
        values.put(TEACHER_COLUMN, teacher);

        int id = (int) db.insertWithOnConflict(DATABASE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update(DATABASE_TABLE, values, DATE_COLUMN+"=?", new String[] {date});  // number 1 is the _id here, update to variable for your code
        }
        db.close();
    }

    public void putPupil(String date, String pupil) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        values.put(DATE_COLUMN, date);
        values.put(PUPIL_COLUMN, pupil);
        int id = (int) db.insertWithOnConflict(DATABASE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update(DATABASE_TABLE, values, DATE_COLUMN+"=?", new String[] {date});  // number 1 is the _id here, update to variable for your code
        }
        db.close();
    }

    public void putTeacher(String date, String teacher) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        values.put(DATE_COLUMN, date);
        //values.put(PUPIL_COLUMN, pupil);
        values.put(TEACHER_COLUMN, teacher);

        int id = (int) db.insertWithOnConflict(DATABASE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update(DATABASE_TABLE, values, DATE_COLUMN+"=?", new String[] {date});  // number 1 is the _id here, update to variable for your code
        }
        db.close();
    }

    public String getTeacherByDate(String date)
    {
        String q="SELECT * FROM "+DATABASE_TABLE+" WHERE "+DATE_COLUMN+"='" + date+"';";

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

        if (cursor != null&&cursor.getCount()>0) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(DatabaseHelper.PUPIL_COLUMN));
        }

        cursor.close();
        return null;
    }


    public List<Calendar> getAllDates(boolean isPupil){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery;
        // Select All Dates
        if(isPupil)
            selectQuery = "select * from " + DATABASE_TABLE +" where " +PUPIL_COLUMN + " is not null";
        else
            selectQuery = "select * from " + DATABASE_TABLE +" where " +TEACHER_COLUMN + " is not null";
        Cursor cursor = db.rawQuery(selectQuery, null);

        List<Calendar> result = parseDates(cursor);

        cursor.close();
        db.close();

        return result;
    }

    public List<Calendar>parseDates(Cursor cursor)
    {
        List<Calendar> results = new ArrayList<Calendar>();
        if (cursor.moveToFirst()) {
            do {
                if(!cursor.getString(0).equals("current"))
                    try {
                        Calendar cal = Calendar.getInstance();
                        Date date;
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        date = format.parse(cursor.getString(0));
                        cal.setTime(date);
                        results.add(cal);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  results;
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
