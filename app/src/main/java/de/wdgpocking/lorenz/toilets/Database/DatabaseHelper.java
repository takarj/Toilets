package de.wdgpocking.lorenz.toilets.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import de.wdgpocking.lorenz.toilets.Database.FeedReaderContract.FeedEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LocalToilets.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void saveToDatabase(String title, LatLng latlng, String description, float rating, float price){
        SQLiteDatabase db = getWritableDatabase();

        //values to save to database
        double lat = latlng.latitude;
        double lng = latlng.longitude;

        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_POSITION_LAT, lat);
        values.put(FeedEntry.COLUMN_NAME_POSITION_LNG, lng);
        values.put(FeedEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(FeedEntry.COLUMN_NAME_RATING, rating);
        values.put(FeedEntry.COLUMN_NAME_PRICE, price);


        long newRowId = db.insert(FeedEntry.TABLE_NAME, null, values);

    }


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_TITLE + " TEXT," +
                    FeedEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    FeedEntry.COLUMN_NAME_POSITION_LAT + "DOUBLE," +
                    FeedEntry.COLUMN_NAME_POSITION_LNG + "DOUBLE," +
                    FeedEntry.COLUMN_NAME_PRICE + "FLOAT," +
                    FeedEntry.COLUMN_NAME_RATING +"FLOAT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
}
