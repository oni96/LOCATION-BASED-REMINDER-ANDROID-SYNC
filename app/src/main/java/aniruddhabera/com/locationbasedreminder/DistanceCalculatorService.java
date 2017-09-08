package aniruddhabera.com.locationbasedreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Primus on 08-Sep-17.
 */

public class DistanceCalculatorService extends Service implements LocationListener {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "Created");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onCreate();


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("Service Location", location.toString());

        SharedPreferences preferences = getSharedPreferences("LASTGOODLOCATION", MODE_PRIVATE);     //Stores the location incessantly
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("lastLat", Double.toString(location.getLatitude()));
        editor.putString("lastLong", Double.toString(location.getLongitude()));

        editor.commit();

        Cursor cursor = new DatabaseHelper(this).getAll();

        while (cursor.moveToNext()) {
            double lat = Double.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL2)));
            double lon = Double.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL3)));

            float distance[] = new float[2];

            Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lon, distance);

            if (distance[0] <= Double.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL4)))) {
                Log.d("REMINDER ALERT", cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL1)));

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle("You're nearby a reminder");
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL1)) + " at " + cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL5)));

                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                builder.setAutoCancel(true);

                Intent intent = new Intent(this, StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                builder.setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(cursor.getPosition(), builder.build());
            }

        }

        //TODO use the same alogrithm to detect changes is distance so that it can be used to send notifs. :0

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
