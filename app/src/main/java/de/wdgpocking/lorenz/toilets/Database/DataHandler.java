package de.wdgpocking.lorenz.toilets.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;

public class DataHandler {

    //constants for database
    public static final String DATABASE_NAME = "toiletsDB";
    public static final int DATABASE_VERSION = 1;

    //constants for table and columns
    public static final String TABLE_NAME = "toiletsTable";
    public static final String ID = "ID";       //int   %d
    public static final String title = "title"; //string    %s
    public static final String lat = "lat";     //double    %f
    public static final String lng = "lng";     //double
    public static final String description = "description"; //string
    public static final String rating = "rating";   //float %f
    public static final String price = "price";     //float

    private DataBaseHelper dbHelper;
    private SQLiteDatabase db;

    public DataHandler(Context ctx){
        dbHelper = new DataBaseHelper(ctx);
    }

    private void openWrite(){
        db = dbHelper.getWritableDatabase();
    }

    private void openRead(){
        db = dbHelper.getReadableDatabase();
    }

    private void close(){
        dbHelper.close();
    }

    public void addToilet(DatabaseToilet dbT){
        openWrite();
        //TODO
        //DEBUG
        /*db.execSQL(String.format(Locale.getDefault(), "insert into " + TABLE_NAME + " VALUES ('%d', '%s', '%f', '%f', '%s', '%f', '%f');",
                dbT.getID(),
                dbT.getTitle(),
                dbT.getLatlng().latitude,
                dbT.getLatlng().longitude,
                dbT.getDescription(),
                dbT.getRating(),
                dbT.getPrice()));*/

        String input = String.format(Locale.ENGLISH, "'%d', '%s', '%f', '%f', '%s', '%f', '%f'",
                dbT.getID(),
                dbT.getTitle(),
                dbT.getLatlng().latitude,
                dbT.getLatlng().longitude,
                dbT.getDescription(),
                dbT.getRating(),
                dbT.getPrice());

        db.execSQL("insert into " + TABLE_NAME + " VALUES (" + input + ");");

        close();
    }

    public ArrayList<DatabaseToilet> getAllToilets(){
        ArrayList<DatabaseToilet> list = new ArrayList<>();

        openRead();

        Cursor c = db.rawQuery("select * from " + TABLE_NAME + ";", null);

        if(c.moveToFirst()){
            list.add(databaseToiletFromTable(c));

            while(c.moveToNext()){
                list.add(databaseToiletFromTable(c));
            }
        }

        close();

        return list;
    }

    public void deleteToiletByID(int id){
        openWrite();
        db.execSQL("DELETE FROM " + TABLE_NAME + "WHERE ID = " + id +";");
        close();
    }

    private DatabaseToilet databaseToiletFromTable(Cursor c){
        return new DatabaseToilet()
                .setID(c.getInt(0))
                .setTitle(c.getString(1))
                .setLatlng(new LatLng(c.getDouble(2), c.getDouble(3)))
                .setDescription(c.getString(4))
                .setRating(c.getFloat(5))
                .setPrice(c.getFloat(6));
    }

    public boolean checkID(int id){
        openRead();
        Cursor c = db.rawQuery("select 1 from " +  TABLE_NAME + " where ID =" + id +  ";", null);
        boolean exists = c.moveToFirst();
        close();
        return exists;
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context ctx){
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL("create table if not exists toiletsTable(" +
                    "ID int not null, " +
                    "title text not null, " +
                    "lat double not null, " +
                    "lng double not null, " +
                    "description text not null, " +
                    "rating float not null, " +
                    "price float not null);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            //TODO
        }
    }
}
