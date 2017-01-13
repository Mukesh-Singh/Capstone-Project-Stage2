package com.app.reddit.provider;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reddit_database";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_SUBREDDIT_ID = "subreddit_id";
    public static final String KEY_SUBREDDIT_NAME = "subreddit_name";
    public static final String KEY_FIELD_SELECTED = "subreddit_selected";
    public static final String TABLE_NAME = "my_subs";
    public static final String CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + KEY_SUBREDDIT_ID + " TEXT PRIMARY KEY, " + KEY_SUBREDDIT_NAME + " TEXT , " + KEY_FIELD_SELECTED + " INTEGER DEFAULT 0" + ");";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
