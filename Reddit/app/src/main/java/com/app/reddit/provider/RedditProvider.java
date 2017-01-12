package com.app.reddit.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;


public class RedditProvider extends ContentProvider {

    DbHelper helper;
    private SQLiteDatabase sqlDB;
    public static final String AUTHORITY = "com.app.reddit.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @Override
    public boolean onCreate() {
        helper = new DbHelper(getContext());
        sqlDB = helper.getWritableDatabase();

        return sqlDB != null;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor = sqlDB.query(DbHelper.TABLE_NAME, null, null, null, null, null, null);
        Log.d("No. of rows ret", Integer.toString(cursor.getCount()));
        return cursor;

    }


    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long row = sqlDB.insert("my_subs", null, values);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        sqlDB.execSQL("delete from " + DbHelper.TABLE_NAME);
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
