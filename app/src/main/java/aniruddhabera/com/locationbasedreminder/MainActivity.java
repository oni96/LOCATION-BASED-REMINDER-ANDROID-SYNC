package aniruddhabera.com.locationbasedreminder;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    final float result[] = new float[2];
    GoogleMap map;
    Circle circleJobs[] = new Circle[10];
    Marker myLocationMarker, selectedPlace, prevJobMarker[] = new Marker[10];
    MarkerOptions prevMarker;
    Button button;
    LocationManager locationManager;
    int PLACE_REQ = 1, PREV_PLACE = 0;
    EditText searchBar;
    boolean doubleBack = false;
    Toast toast;
    LatLng prevJoblatlng;
    private boolean locationAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBar = (EditText) findViewById(R.id.search_bar);
        button = (Button) findViewById(R.id.button);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        mapFragment.getMapAsync(this);

        /*
        TODO REMOVE SHARED PREFERENCES AND ADD DATABASE
         */

//        SharedPreferences preferences = getSharedPreferences("STORENOTE", MODE_PRIVATE);
//        Log.d("LAT STORE", preferences.getString("lat", ""));
//        if (!preferences.getString("lat", "").isEmpty()) {
//            prevJoblatlng = new LatLng(Double.valueOf(preferences.getString("lat", "")), Double.valueOf(preferences.getString("lon", "")));
//            prevMarker = new MarkerOptions().position(prevJoblatlng).title(preferences.getString("note", "")).snippet(preferences.getString("address", ""));
//            PREV_PLACE = 1;
//        }

//        final Handler handler = new Handler();
//
//        final Runnable checkDistance = new Runnable() {
//            @Override
//            public void run() {
//                if (result[0] <= circle.getRadius() && (circle != null))
//                    Toast.makeText(MainActivity.this, "You're nearby a task.", Toast.LENGTH_SHORT).show();
//
//                handler.postDelayed(this, 10000);
//            }
//        };
//
//        handler.postDelayed(checkDistance, 10000);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location Permissions were not found. Restart app to grant permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(GPS_PROVIDER, 10, 1, this);
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);

        myLocationMarker = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));   //adding an arbitary marker to initialize
        myLocationMarker.setVisible(false);
        selectedPlace = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        selectedPlace.setVisible(false);
        reqLoc();


        final Cursor cursor = new DatabaseHelper(MainActivity.this).getAll();

        int i = 0;
        while (cursor.moveToNext()) {

            //TODO ADD ```Address``` TO THE MARKERS
            String job = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL1));
            String lat = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL2));
            String lon = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL3));
            String rad = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL4));

            if (!lat.isEmpty() && !lon.isEmpty()) {
                LatLng temp = new LatLng(Double.valueOf(lat), Double.valueOf(lon));

                prevMarker = new MarkerOptions().position(temp).title(job);
                prevJobMarker[i] = map.addMarker(prevMarker);
                CircleOptions options = new CircleOptions().center(prevMarker.getPosition()).radius(Double.valueOf(rad)).strokeWidth(1)
                                                            .strokeColor(Color.argb(255,43,197,189))
                                                            .fillColor(Color.argb(75,43,197,189));      //TODO CHANGE YOUR COLOR HERE IF NEEDED
                circleJobs[i] = map.addCircle(options);
                circleJobs[i].setVisible(false);
                i++;
            }
        }

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                selectedPlace.remove();
                selectedPlace = map.addMarker(new MarkerOptions().position(latLng));
                locationAdded = true;
                if (locationAdded) {
                    searchBar.setText("Fetching address...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getAddress(selectedPlace);
                        }
                    }, 2000);}

            }
        });


//        if (PREV_PLACE == 1) {
//            prevJobMarker = map.addMarker(prevMarker);
//
//            circle = map.addCircle(new CircleOptions().center(prevJoblatlng).radius(1000));
//
//
//        }


        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (locationAdded) {
                    selectedPlace.remove();
                    LatLng newpos = map.getCameraPosition().target;
                    selectedPlace = map.addMarker(new MarkerOptions().position(newpos));

                }


            }
        });
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                if (locationAdded) {
                    searchBar.setText("Fetching address...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getAddress(selectedPlace);
                        }
                    }, 2000);
                }

            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for (int i = 0; i < 10; i++) {
                    if (circleJobs[i] != null)
                        circleJobs[i].setVisible(false);
                }
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int i;

                for (i = 0; i < 10; i++) {
                    if (circleJobs[i] != null)
                        circleJobs[i].setVisible(false);
                }
                for (i = 0; i < 10; i++) {


                    if (circleJobs[i] != null && marker.getPosition().latitude == circleJobs[i].getCenter().latitude && marker.getPosition().longitude == circleJobs[i].getCenter().longitude) {

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(circleJobs[i].getCenter(), 15);
                        map.animateCamera(cameraUpdate);
                        circleJobs[i].setVisible(true);

                        break;

                    }


                }


                return false;
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(MainActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
                //TODO open dialog for user to edit or delete
                Cursor allData = new DatabaseHelper(MainActivity.this).getAll();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.marker_on_click_layout, null);

                EditText editText = (EditText) view.findViewById(R.id.reminder);
                editText.setText(marker.getTitle());

                while (allData.moveToNext()) {
                    if (allData.getString(allData.getColumnIndex(DatabaseHelper.COL1)).equals(marker.getTitle())) {
                        EditText address = (EditText) view.findViewById(R.id.address);
                        address.setText(allData.getString(allData.getColumnIndex(DatabaseHelper.COL5)));
                        break;
                    }
                }

                builder.setView(view)
                        .setPositiveButton("Save", null)
                        .setNegativeButton("Cancel", null)
                        .setNeutralButton("Delete", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }


    public String getAddress(Marker marker) {
        //TODO write geocoder address fetch function here
        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        final LatLng latLng = marker.getPosition();
        final String[] place = new String[1];


        try {
            Log.d("MARKER", geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) + "");
            List<Address> address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            place[0] = address.get(0).getAddressLine(0) + ", " + address.get(0).getAddressLine(1) + ", " + address.get(0).getLocality();
            searchBar.setText(place[0]);
            Toast.makeText(MainActivity.this, place[0], Toast.LENGTH_SHORT).show();
            selectedPlace.setTitle(place[0]);
        } catch (
                IOException e) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
        return place[0];
    }


    public void getLocationButton(View v) {
        locationAdded = false;
        searchBar.setText("");
        myLocationMarker.remove();
        selectedPlace.remove();
        reqLoc();
    }


    public void searchPlaceMethod() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, PLACE_REQ);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void searchPlace(View v) {
        //TODO NEED TO DISABLE ALL MARKERS
        // prevJobMarker.setVisible(false);
        searchPlaceMethod();
    }


    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(this);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {

        for (int i = 0; i < 10; i++) {
            if (circleJobs[i] != null)
                circleJobs[i].setVisible(false);
        }

        selectedPlace.remove();

        searchBar.setText("");

        locationAdded = false;
//        if (prevJobMarker != null)  TODO check if user was searching
//            prevJobMarker.setVisible(true);

        if (doubleBack) {
            toast.cancel();
            super.onBackPressed();

        }

        doubleBack = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBack = false;
            }
        }, 2000);

        toast = Toast.makeText(this, "Press back button twice to exit", Toast.LENGTH_SHORT);
        toast.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_REQ) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Toast.makeText(this, place.getAddress(), Toast.LENGTH_SHORT).show();
                selectedPlace.remove();
                selectedPlace = map.addMarker(new MarkerOptions().position(place.getLatLng())
                        .draggable(true)
                        .title(place.getAddress().toString()));

                searchBar.setText("Fetching address...");

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17));
                locationAdded = true;
            } else if (requestCode == RESULT_CANCELED) {
                Toast.makeText(this, "Invalid Place", Toast.LENGTH_SHORT).show();
                map.addMarker(prevMarker);

            }
        }
    }


    public void reqLoc() {

        if (!locationManager.isProviderEnabled(GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services are turned off! Please enable them.")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            /**
                             * CUSTOM TOAST BELOW!
                             */
                            Toast toast = Toast.makeText(MainActivity.this, "Please enable Location Services.\nUsing last known location.", Toast.LENGTH_SHORT);
                            TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
                            toastMessage.setGravity(Gravity.CENTER);
                            toast.show();

                            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                            double lat = Double.valueOf(preferences.getString("lastLat", "0"));
                            double lon = Double.valueOf(preferences.getString("lastLong", "0"));
                            LatLng latLng = new LatLng(lat, lon);
                            myLocationMarker = map.addMarker(new MarkerOptions().position(latLng)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                            map.animateCamera(cameraUpdate);
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permissions were not found. Restart app to grant permissions", Toast.LENGTH_SHORT).show();
                return;
            }
            Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
            }
            Log.d("Location", location.toString());

            myLocationMarker = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15);
            map.animateCamera(cameraUpdate);

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("lastLat", Double.toString(location.getLatitude()));
            editor.putString("lastLong", Double.toString(location.getLongitude()));
            editor.commit();
        }

    }


    public void addButton(View v) {
//        if (prevJobMarker != null) TODO DISABLE ALL MARKERS
//            prevJobMarker.setVisible(false);

//        if (circle != null) Todo hide circles here
//            circle.setVisible(false);

        if (searchBar.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please choose a location to add reminder", Toast.LENGTH_SHORT).show();
            searchPlaceMethod();
        } else if (searchBar.getText().toString().equals("Fetching address...")) {
            Toast.makeText(this, "Fetching Address...", Toast.LENGTH_SHORT).show();
        } else {


            //TODO GO TO ADD LOCATION REMINDER ACTIVITY

            SharedPreferences temp = getSharedPreferences("activity", MODE_PRIVATE);
            SharedPreferences.Editor editor = temp.edit();
            LatLng latLng = selectedPlace.getPosition();
            editor.putString("address", searchBar.getText().toString());
            editor.putString("lat", String.valueOf(latLng.latitude));
            editor.putString("lon", String.valueOf(latLng.longitude));
            editor.commit();

            Intent intent = new Intent(MainActivity.this, AddLocationActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        }

    }


    @Override
    public void onLocationChanged(Location location) {
        myLocationMarker.remove();

        myLocationMarker = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));




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

    public void showAllJobs(View view) {

        //TODO GET JOBS FROM DBASE and use the CAmera to view all in bounds

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myLocationMarker.getPosition());
        int i = 0;

        for (i = 0; i < prevJobMarker.length; i++) {
            if (prevJobMarker[i] != null) {
                builder.include(prevJobMarker[i].getPosition());
            } else {
                break;
            }
        }

        LatLngBounds bounds = builder.build();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        if(i>0){
            CameraUpdate showAllJobs = CameraUpdateFactory.newLatLngBounds(bounds, height / 2, width/2, 10);
            map.animateCamera(showAllJobs);
        }else{
            Toast.makeText(this, "No reminders to view.", Toast.LENGTH_SHORT).show();
            getLocationButton(view);
        }


    }
}
