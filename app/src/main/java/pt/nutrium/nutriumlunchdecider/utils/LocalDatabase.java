package pt.nutrium.nutriumlunchdecider.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;

import pt.nutrium.nutriumlunchdecider.models.Restaurant;

public class LocalDatabase extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_FILE_NAME = "restaurants.db";
    private static final String TAG = "DB";
    private static final int COLUMN_DOESNT_EXISTS = -1;
    private static final String CREATE_TABLE = "CREATE TABLE %s (%s)";

    private static final String FAVOURITES_TABLENAME = "favourites";

    private final SQLiteDatabase db;

    public LocalDatabase(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);

        db = getWritableDatabase();
    }


    @Override // 1º
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }


    @Override // 2º
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(CREATE_TABLE, FAVOURITES_TABLENAME, sqlTableCreateString(Restaurant.sqlTableFields())));
    }


    @Override // 2º
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    @Override // 2º
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    @Override // 3º
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


    @Override
    public synchronized void close() {
        if (db != null) {
            db.close();
            super.close();
        }
    }


    public HashMap<String, Restaurant> getFavouriteRestaurants() {
        Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s", FAVOURITES_TABLENAME), null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            HashMap<String, Restaurant> restaurants = new HashMap<>();
            while (!cursor.isAfterLast()) {
                restaurants.put(getValueString(cursor, Restaurant.DB_ID), new Restaurant(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            return restaurants;
        }

        return null;
    }


    public void insertFavouriteRestaurant(Restaurant restaurant) {
        ContentValues cv = new ContentValues();
        cv.put(Restaurant.DB_ID, restaurant.getId());
        cv.put(Restaurant.DB_NAME, restaurant.getName());
        cv.put(Restaurant.DB_ADDRESS, restaurant.getAddress());
        cv.put(Restaurant.DB_CUISINES, restaurant.getCuisines());
        cv.put(Restaurant.DB_PRICE, restaurant.getPrice());
        cv.put(Restaurant.DB_STARS, restaurant.getStars());
        db.insert(FAVOURITES_TABLENAME, null, cv);
    }


    public void updateFavouriteRestaurant(Restaurant restaurant) {
        ContentValues cv = new ContentValues();
        cv.put(Restaurant.DB_NAME, restaurant.getName());
        cv.put(Restaurant.DB_ADDRESS, restaurant.getAddress());
        cv.put(Restaurant.DB_CUISINES, restaurant.getCuisines());
        cv.put(Restaurant.DB_PRICE, restaurant.getPrice());
        cv.put(Restaurant.DB_STARS, restaurant.getStars());
        db.update(FAVOURITES_TABLENAME, cv, String.format("%s=?", Restaurant.DB_ID), new String[]{restaurant.getId()});
    }


    public void deleteFavouriteRestaurant(String id) {
        db.delete(FAVOURITES_TABLENAME, String.format("%s=?", Restaurant.DB_ID), new String[]{id});
    }


    public static String sqlTableCreateString(LinkedHashMap<String, String> fields) {
        StringBuilder sb = new StringBuilder();

        for (String field : fields.keySet()) {
            sb.append(field).append(" ").append(fields.get(field)).append(",");
        }

        return sb.toString().substring(0, sb.length() - 1);
    }


    public static String getValueString(Cursor cursor, String columnName) {
        return cursor.getColumnIndex(columnName) != LocalDatabase.COLUMN_DOESNT_EXISTS ? cursor.getString(cursor.getColumnIndex(columnName)) : null;
    }


    public static int getValueInt(Cursor cursor, String columnName) {
        return cursor.getColumnIndex(columnName) != LocalDatabase.COLUMN_DOESNT_EXISTS ? cursor.getInt(cursor.getColumnIndex(columnName)) : -1;
    }


    public static double getValueDouble(Cursor cursor, String columnName) {
        return cursor.getColumnIndex(columnName) != LocalDatabase.COLUMN_DOESNT_EXISTS ? cursor.getDouble(cursor.getColumnIndex(columnName)) : -1;
    }
}