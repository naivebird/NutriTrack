package com.example.nutritrack.data.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.nutritrack.data.model.UserProfile;

public class DatabaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "NUTRI_TRACK_DATABASE";
    private static final String TABLE_NAME = "PROFILE_TABLE";
    private static final String ID = "ID";
    private static final String EMAIL = "EMAIL";

    private static final String NAME = "NAME";
    private static final String AGE = "AGE";
    private static final String HEIGHT = "HEIGHT";
    private static final String WEIGHT = "WEIGHT";
    private static final String ACTIVITY_LEVEL = "ACTIVITY_LEVEL";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, EMAIL TEXT, NAME TEXT, AGE INTEGER, HEIGHT FLOAT, WEIGHT FLOAT, ACTIVITY_LEVEL INT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public UserProfile getUserProfile(String email) {
        db = this.getReadableDatabase();
        UserProfile userProfile = null;

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE EMAIL = ?";
        try (android.database.Cursor cursor = db.rawQuery(query, new String[]{email})) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(AGE));
                float height = cursor.getFloat(cursor.getColumnIndexOrThrow(HEIGHT));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(WEIGHT));
                int activityLevel = cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_LEVEL));

                userProfile = new UserProfile(id, email, name, age, height, weight, activityLevel);
            }
        }

        return userProfile;
    }

    public void insertUserProfile(UserProfile profile) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME, profile.getName());
        values.put(EMAIL, profile.getEmail());
        values.put(AGE, profile.getAge());
        values.put(HEIGHT, profile.getHeight());
        values.put(WEIGHT, profile.getWeight());
        values.put(ACTIVITY_LEVEL, profile.getActivityLevel());
        db.insert(TABLE_NAME, null, values);
    }

    public void updateUserProfile(int id, String email, String name, int age, float height, float weight, int activityLevel) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EMAIL, email);
        values.put(NAME, name);
        values.put(AGE, age);
        values.put(HEIGHT, height);
        values.put(WEIGHT, weight);
        values.put(ACTIVITY_LEVEL, activityLevel);
        db.update(TABLE_NAME, values, "ID=?", new String[]{String.valueOf(id)});
    }
}
