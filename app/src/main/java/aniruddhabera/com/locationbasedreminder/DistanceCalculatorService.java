package aniruddhabera.com.locationbasedreminder;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Primus on 08-Sep-17.
 */

public class DistanceCalculatorService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "Created");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        final Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.d("Service", "Running");
                h.postDelayed(this, 3000);
            }
        };

        h.postDelayed(r, 3000);
        super.onCreate();

        //TODO use the same alogrithm to detect changes is distance so that it can be used to send notifs. :0
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
