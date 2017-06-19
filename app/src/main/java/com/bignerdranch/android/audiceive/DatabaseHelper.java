package com.bignerdranch.android.audiceive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyDBName.db";
    private static final String FINGERPRINTS_TABLE_NAME = "fingerprints";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table if not exists scenes " +
                        "(name varchar(75), address varchar(75), details varchar(65535), link varchar(100), image_id int, scene_id int, primary key (scene_id))"
        );
        db.execSQL(
                "create table if not exists fingerprints " +
                        "(anchor_frequency smallint, point_frequency smallint, delta smallint, absolute_time smallint, scene_id int, foreign key (scene_id) references scenes(scene_id))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS fingerprints");
        db.execSQL("DROP TABLE IF EXISTS scenes");
        onCreate(db);
    }

    public void refreshDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS fingerprints");
        db.execSQL("DROP TABLE IF EXISTS scenes");
        onCreate(db);
    }

    public boolean addFingerprint(short anchorFrequency, short pointFrequency, byte delta, short absoluteTime, int sceneID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("anchor_frequency", anchorFrequency);
        values.put("point_frequency", pointFrequency);
        values.put("delta", delta);
        values.put("absolute_time", absoluteTime);
        values.put("scene_id", sceneID);
        db.insert("fingerprints", null, values);
        return true;
    }

    public boolean addScene(String name, String address, String details, String link, int imageID, int sceneID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("address", address);
        values.put("details", details);
        values.put("link", link);
        values.put("image_id", imageID);
        values.put("scene_id", sceneID);
        db.insert("scenes", null, values);
        return true;
    }

    public Cursor getData(short anchorFrequency, short pointFrequency, byte delta) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select absolute_time, scene_id from fingerprints " +
                "where anchor_frequency=" + anchorFrequency + " and point_frequency=" + pointFrequency + " and delta=" + delta, null);
        return res;
    }

    public Cursor getSceneInfo(int sceneID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select name, address, details, link, image_id from scenes " +
                "where scene_id=" + sceneID, null);
        return res;
    }

    public long getNumOfScenes() {
        SQLiteDatabase db = this.getReadableDatabase();
        long numRows = DatabaseUtils.queryNumEntries(db, "scenes");
        return numRows;
    }

}
