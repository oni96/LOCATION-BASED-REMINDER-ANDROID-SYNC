package aniruddhabera.com.locationbasedreminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    LatLng sentLatlng;
    EditText address;
    EditText jobText;
    SeekBar seekBar;
    Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        SharedPreferences temp = getSharedPreferences("activity", MODE_PRIVATE);
        final double lat = Double.valueOf(temp.getString("lat", "0"));
        final double lon = Double.valueOf(temp.getString("lon", "0"));
        sentLatlng = new LatLng(lat, lon);
        Log.d("LOC", sentLatlng.toString());
        address = (EditText) findViewById(R.id.address);
        address.setText(temp.getString("address", ""));
        jobText = (EditText) findViewById(R.id.jobText);
        seekBar = (SeekBar) findViewById(R.id.seekBarRadius);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                circle.remove();
                circle = map.addCircle(new CircleOptions().center(sentLatlng).radius(progress).fillColor(Color.argb(75, 43, 197, 189)));
                Log.d("RADIUS", String.valueOf(progress));
                TextView rad = (TextView) findViewById(R.id.radiusText);
                rad.setText(seekBar.getProgress() + " metres");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        SharedPreferences getLastJob = getSharedPreferences("LASTJOB", MODE_PRIVATE);
        String job = getLastJob.getString("lastjob", "");
        jobText.setText(job);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragmentJob);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setAllGesturesEnabled(false);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                SharedPreferences storeJob = getSharedPreferences("LASTJOB", MODE_PRIVATE);
                SharedPreferences.Editor editor = storeJob.edit();
                editor.putString("lastjob", jobText.getText().toString());
                editor.commit();
                finish();

            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
        map.addMarker(new MarkerOptions().position(sentLatlng));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sentLatlng, 14));
        circle = map.addCircle(new CircleOptions().center(sentLatlng).radius(1.0));


    }


    public void goToMain(View view) {
        SharedPreferences storeJob = getSharedPreferences("LASTJOB", MODE_PRIVATE);
        SharedPreferences.Editor editor = storeJob.edit();
        editor.putString("lastjob", "");
        editor.commit();
        finish();
    }

    public void addButton(View v) {
        DatabaseHelper helper = new DatabaseHelper(this);
        if (jobText.getText().toString().isEmpty()) {
            Toast.makeText(this, "No reminder text added.", Toast.LENGTH_SHORT).show();


        } else if (seekBar.getProgress() == 0) {
            Toast.makeText(this, "Radius is set to zero. Increase it to get perfect reminders.", Toast.LENGTH_SHORT).show();

        } else {
            helper.addToTable(jobText.getText().toString().trim(), sentLatlng.latitude, sentLatlng.longitude, seekBar.getProgress(), address.getText().toString());
            Intent intent = new Intent(AddLocationActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }


    }
}
