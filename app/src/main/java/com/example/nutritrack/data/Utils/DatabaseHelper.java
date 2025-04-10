package com.example.nutritrack.data.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.nutritrack.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "NUTRI_TRACK_DATABASE";
    private static final String PROFILE_TABLE = "PROFILE_TABLE";
    private static final String ID = "ID";
    private static final String EMAIL = "EMAIL";

    private static final String NAME = "NAME";
    private static final String AGE = "AGE";
    private static final String HEIGHT = "HEIGHT";
    private static final String WEIGHT = "WEIGHT";
    private static final String ACTIVITY_LEVEL = "ACTIVITY_LEVEL";
    private static final String GOAL = "GOAL";

    private static final String FOOD_LOG_TABLE = "FOOD_LOG_TABLE";
    private static final String FOOD_ID = "ID";
    private static final String USER_ID = "USER_ID";
    private static final String FOOD_NAME = "FOOD_NAME";
    private static final String FOOD_CALORIES = "CALORIES";
    private static final String FOOD_DATE = "DATE";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PROFILE_TABLE +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "EMAIL TEXT," +
                " NAME TEXT," +
                " AGE INTEGER," +
                " HEIGHT FLOAT," +
                " WEIGHT FLOAT, " +
                "ACTIVITY_LEVEL INT," +
                " GOAL FLOAT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + FOOD_LOG_TABLE + " (" +
                FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FOOD_NAME + " TEXT, " +
                FOOD_CALORIES + " FLOAT, " +
                FOOD_DATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + PROFILE_TABLE + " ADD COLUMN " + GOAL + " FLOAT DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + FOOD_LOG_TABLE + " (" +
                    FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FOOD_NAME + " TEXT, " +
                    FOOD_CALORIES + " FLOAT, " +
                    FOOD_DATE + " TEXT)");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + FOOD_LOG_TABLE + " ADD COLUMN " + USER_ID +  " INT DEFAULT 1");
        }
    }

    public UserProfile getUserProfile(String email) {
        db = this.getReadableDatabase();
        UserProfile userProfile = null;

        String query = "SELECT * FROM " + PROFILE_TABLE + " WHERE EMAIL = ?";
        try (android.database.Cursor cursor = db.rawQuery(query, new String[]{email})) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(AGE));
                float height = cursor.getFloat(cursor.getColumnIndexOrThrow(HEIGHT));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(WEIGHT));
                int activityLevel = cursor.getInt(cursor.getColumnIndexOrThrow(ACTIVITY_LEVEL));
                float goal = cursor.getFloat(cursor.getColumnIndexOrThrow(GOAL));

                userProfile = new UserProfile(id, email, name, age, height, weight, activityLevel, goal);
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
        values.put(GOAL, profile.getGoal());
        db.insert(PROFILE_TABLE, null, values);
    }

    public void updateUserProfile(int id, String email, String name, int age, float height, float weight, int activityLevel, float goal) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EMAIL, email);
        values.put(NAME, name);
        values.put(AGE, age);
        values.put(HEIGHT, height);
        values.put(WEIGHT, weight);
        values.put(ACTIVITY_LEVEL, activityLevel);
        values.put(GOAL, goal);
        db.update(PROFILE_TABLE, values, "ID=?", new String[]{String.valueOf(id)});
    }

    public void insertFoodLog(int userId, String name, float calories, String date) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, userId);
        values.put(FOOD_NAME, name);
        values.put(FOOD_CALORIES, calories);
        values.put(FOOD_DATE, date);
        db.insert(FOOD_LOG_TABLE, null, values);
    }

    public float getTotalCaloriesForDate(String date, int userId) {
        db = this.getReadableDatabase();
        float totalCalories = 0;

        String query = "SELECT SUM(" + FOOD_CALORIES + ") FROM " + FOOD_LOG_TABLE +
                " WHERE " + FOOD_DATE + " = ? AND " + USER_ID + " = ?" ;
        try (android.database.Cursor cursor = db.rawQuery(query, new String[]{date,  String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                totalCalories = cursor.getFloat(0);
            }
        }
        return totalCalories;
    }

    public List<String> getFoodItemsForDate(String date, int userId) {
        db = this.getReadableDatabase();
        List<String> foods = new ArrayList<>();

        String query = "SELECT " + FOOD_NAME + ", " + FOOD_CALORIES + " FROM " + FOOD_LOG_TABLE +
                " WHERE " + FOOD_DATE + " = ? AND " + USER_ID + " = ?";
        try (android.database.Cursor cursor = db.rawQuery(query, new String[]{date, String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                float calories = cursor.getFloat(1);
                foods.add(name + ": " + calories + " kcal");
            }
        }
        return foods;
    }

    public void clearFoodLogForDateAndUser(String date, int userId) {
        db = this.getWritableDatabase();
        db.delete(FOOD_LOG_TABLE, FOOD_DATE + " = ? AND " + USER_ID + " = ?", new String[]{date, String.valueOf(userId)});
    }
}
