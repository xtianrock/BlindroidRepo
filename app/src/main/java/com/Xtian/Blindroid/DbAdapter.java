package com.Xtian.Blindroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * Created by xtianrock on 10/05/2015.
 */

public class DbAdapter
{
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_EMAIL = "email";
    private static final String TAG = "ContactSqliteHelper";
    private static final String DB_NAME = "DbBlindroid";
    private static final String TABLE_NAME = "contacts";
    private static final String DB_CREATE = "create table contacts (_id integer primary key autoincrement, name text not null, full_name text not null, phone text not null, email text)";
    private static final int VERSION = 1;
    private final Context context;
    private DbHelper helper;
    private SQLiteDatabase db;

    public DbAdapter (Context context)
    {
        this.context=context;
        helper=new DbHelper(context);
    }
    private static class DbHelper extends SQLiteOpenHelper {


        public DbHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists "+TABLE_NAME);
            onCreate(db);
        }
    }
    public DbAdapter open() throws SQLException
    {
        db=helper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        helper.close();
    }

    public long insertContact(String name, String fullName, String phone,String email)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME,name);
        contentValues.put(KEY_FULL_NAME,fullName);
        contentValues.put(KEY_PHONE,phone);
        contentValues.put(KEY_EMAIL,email);
        return db.insert(TABLE_NAME,null,contentValues);
    }
    public boolean deleteContact(long id)
    {
        return db.delete(TABLE_NAME,KEY_ID+"="+id,null)>0;
    }

    public Cursor GetContactsByName(String name)
    {
       return db.query(TABLE_NAME, new String[]{KEY_ID,KEY_NAME,KEY_FULL_NAME,KEY_PHONE,KEY_EMAIL},KEY_NAME+" like '%"+name+"%'",null,null,null,null,null);
    }

    public Cursor GetContact(long id) throws SQLException
    {
        Cursor cursor= db.query(TABLE_NAME, new String[]{KEY_ID,KEY_NAME,KEY_FULL_NAME,KEY_PHONE,KEY_EMAIL},KEY_ID+"="+id,null,null,null,null,null);
        if(cursor!=null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }
}


