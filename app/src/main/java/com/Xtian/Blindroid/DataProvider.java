package com.Xtian.Blindroid;

/**
 * Created by xtianrock on 15/05/2015.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class DataProvider extends ContentProvider {

    public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://com.Xtian.Blindroid.provider/messages");
    public static final Uri CONTENT_URI_PROFILE = Uri.parse("content://com.Xtian.Blindroid.provider/profile");

    public static final String COL_ID = "_id";

    public enum MessageType {

        INCOMING, OUTGOING
    }

    //parameters recognized by demo server
    public static final String SENDER_EMAIL = "senderEmail";
    public static final String RECEIVER_EMAIL = "receiverEmail";
    public static final String REG_ID = "regId";
    public static final String MESSAGE = "message";



    // TABLE MESSAGE
    public static final String TABLE_MESSAGES = "messages";
    public static final String COL_TYPE = "type";
    public static final String COL_SENDER_PHONE = "senderPhone";
    public static final String COL_RECEIVER_PHONE = "receiverPhone";
    public static final String COL_MESSAGE = "message";
    public static final String COL_TIME = "time";

    // TABLE PROFILE
    public static final String TABLE_PROFILE = "profile";
    public static final String COL_NAME = "name";
    public static final String COL_PHONE = "phone";
    public static final String COL_COUNT = "count";

    private DbHelper dbHelper;

    private static final int MESSAGES_ALLROWS = 1;
    private static final int MESSAGES_SINGLE_ROW = 2;
    private static final int PROFILE_ALLROWS = 3;
    private static final int PROFILE_SINGLE_ROW = 4;
    private static final int CHAT_LIST_ORDER = 5;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.Xtian.Blindroid.provider", "messages", MESSAGES_ALLROWS);
        uriMatcher.addURI("com.Xtian.Blindroid.provider", "messages/*", MESSAGES_SINGLE_ROW);
        uriMatcher.addURI("com.Xtian.Blindroid.provider", "profile", PROFILE_ALLROWS);
        uriMatcher.addURI("com.Xtian.Blindroid.provider", "profile/chatlist", CHAT_LIST_ORDER);
        uriMatcher.addURI("com.Xtian.Blindroid.provider", "profile/*", PROFILE_SINGLE_ROW);
    }

    @Override
    public boolean onCreate() {
        dbHelper = DbHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupBy=null;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
                qb.setTables(TABLE_MESSAGES);
                break;

            case MESSAGES_SINGLE_ROW:
                qb.setTables(TABLE_MESSAGES);
                qb.appendWhere(COL_ID+" = " + uri.getLastPathSegment());
                break;

            case PROFILE_ALLROWS:
                qb.setTables(TABLE_PROFILE);
                break;

            case PROFILE_SINGLE_ROW:
                qb.setTables(TABLE_PROFILE);
                qb.appendWhere(COL_ID+" = " + uri.getLastPathSegment());
                break;
            case CHAT_LIST_ORDER:
                qb.setTables("profile p inner join messages m on senderPhone=phone or receiverPhone=phone");
                groupBy="phone";
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        long id;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
                id = db.insertOrThrow(TABLE_MESSAGES, null, values);
                break;

            case PROFILE_ALLROWS:
                id = db.insertOrThrow(TABLE_PROFILE, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri insertUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(insertUri, null);
        getContext().getContentResolver().notifyChange(CONTENT_URI_PROFILE, null);
        return insertUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
                count = db.update(TABLE_MESSAGES, values, selection, selectionArgs);
                break;

            case MESSAGES_SINGLE_ROW:
                count = db.update(TABLE_MESSAGES, values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            case PROFILE_ALLROWS:
                count = db.update(TABLE_PROFILE, values, selection, selectionArgs);
                break;

            case PROFILE_SINGLE_ROW:
                count = db.update(TABLE_PROFILE, values, COL_ID+" = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
                count = db.delete(TABLE_MESSAGES, selection, selectionArgs);
                break;

            case MESSAGES_SINGLE_ROW:
                count = db.delete(TABLE_MESSAGES, "senderPhone = ?", new String[]{uri.getLastPathSegment()});
                break;

            case PROFILE_ALLROWS:
                count = db.delete(TABLE_PROFILE, selection, selectionArgs);
                break;

            case PROFILE_SINGLE_ROW:

                count = db.delete(TABLE_PROFILE, "phone = ?", new String[]{uri.getLastPathSegment()});
                Log.i("count",String.valueOf(count));
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }


    public static ArrayList<Message> getMessages(Context context, String phone,String limit)
    {
        ArrayList <Message> messages = new ArrayList<>();
        DbHelper dbHelper= new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_MESSAGES);
        String[] projection = {COL_MESSAGE,COL_SENDER_PHONE,COL_RECEIVER_PHONE};
        String selection = COL_SENDER_PHONE + " = ? or " +COL_RECEIVER_PHONE + " = ?";
        String[] selectionArgs = {phone,phone};
        Cursor c=qb.query(db, projection, selection, selectionArgs, null, null, COL_TIME+" DESC",limit);
        if(c.moveToFirst())
        {
            while (c.moveToNext())
            {
               messages.add(new Message(c.getString(0),c.getString(1),c.getString(2),MessageType.INCOMING.ordinal()));
            }
        }
        c.close();
        db.close();
        return messages;
    }

    public static void incrementCount(Context context,String phone)
    {
        DbHelper dbHelper= new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update profile set count = count+1 where phone = ?",new String[]{ phone});
        context.getContentResolver().notifyChange(CONTENT_URI_PROFILE, null);
        db.close();
    }
    public static void refreshChatlist(Context context)
    {
        context.getContentResolver().notifyChange(CONTENT_URI_PROFILE, null);
    }

    public static int getProfileId(Context context, String senderPhone)
    {
        DbHelper dbHelper= new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_PROFILE);
        String[] projection = {"_id"};
        String selection = "phone=?";
        String[] selectionArgs = {senderPhone};
        Cursor c=qb.query(db, projection, selection, selectionArgs, null, null, null);

        if (c.moveToFirst())
        {
            int id=c.getInt(0);
            c.close();
            db.close();
            return id;
        }
        else
        {
            return 0;
        }

    }

    public static Boolean exist(Context context, String senderPhone)
    {
        DbHelper dbHelper= new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_PROFILE);
        String[] projection = {"phone"};
        String selection = "phone=?";
        String[] selectionArgs = {senderPhone};
        Cursor c=qb.query(db, projection, selection, selectionArgs, null, null, null);
        int count = c.getCount();
        c.close();
        db.close();
        return count > 0;

    }

    private static class DbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "blindroid.db";
        private static final int DATABASE_VERSION = 2;
        private static DbHelper mInstance = null;


        public static DbHelper getInstance(Context ctx) {

            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (mInstance == null) {
                mInstance = new DbHelper(ctx.getApplicationContext());
            }
            return mInstance;
        }


        private DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table messages ("
                    + "_id integer primary key autoincrement, "
                    + COL_TYPE + " integer, "
                    + COL_MESSAGE + " text, "
                    + COL_SENDER_PHONE + " text, "
                    + COL_RECEIVER_PHONE + " text, "
                    + COL_TIME + " datetime default current_timestamp);");

            db.execSQL("create table profile("
                    + "_id integer primary key autoincrement, "
                    + COL_NAME + " text, "
                    + COL_PHONE + " text unique, "
                    + COL_COUNT + " integer default 0);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
