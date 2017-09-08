package aniruddhabera.com.locationbasedreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by Primus on 31-Aug-17.
 */


public class DatabaseHelper extends SQLiteOpenHelper {

    private static int VERSION = 1;
    private static String TABLE_NAME = "JOB_TABLE";
    /*COLUMN VARIABLES BELOW AS DESCRIBED*/
    static String COL1 = "JOB", COL2 = "LAT", COL3 = "LON", COL4 = "RAD", COL5 = "ADDRESS";
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlQuery = "CREATE TABLE " + TABLE_NAME + " (" + COL1 + " VARCHAR(30), " + COL2 + " VARCHAR(30), " + COL3 + " VARCHAR(30), " + COL4 + " VARCHAR(30), " + COL5 + " VARCHAR(30));";
        try {
            db.execSQL(sqlQuery);
            Log.d("DatabaseHelper onCreate", "Table Created");
        } catch (SQLException e) {
            Log.d("DatabaseHelper onCreate", "Table Created");
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addToTable(String job, double lat, double lon, double radius, String address) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, job);
        contentValues.put(COL2, lat);
        contentValues.put(COL3, lon);
        contentValues.put(COL4, radius);
        contentValues.put(COL5, address);
        if (database.insert(TABLE_NAME, null, contentValues) > 0) {
            Log.d("Database addToTable", job + " " + lat + " " + lon + " " + radius);
            Toast.makeText(context, "Your reminder has been saved", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Database addToTable", "Data Not Added");
        }
    }

    public void fetchdata(String lat, String lon) {
        SQLiteDatabase database = getWritableDatabase();
        database.query(TABLE_NAME, new String[]{COL1, COL2, COL3, COL4}, COL2 + "=? OR " + COL3 + "=?", new String[]{lat, lon}, null, null, COL2);
        //TODO fetch data as per necessary hello
    }

    public void deleteFromTable(String job) {
        //TODO DELETE FROM DATABASE
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME, COL1 + "=?", new String[]{job});
    }

    public Cursor getAll() {
        SQLiteDatabase database = getReadableDatabase();
        String getAllQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL1;
        Cursor cursor = database.rawQuery(getAllQuery, null);
        //Log.d("CURSOR SIZE", cursor.getCount() + "");
        return cursor;
    }

}
