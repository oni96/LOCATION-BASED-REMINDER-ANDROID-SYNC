package aniruddhabera.com.locationbasedreminder;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.security.Permission;

public class StartActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {


                    SharedPreferences preferences = getSharedPreferences("STORENOTE",MODE_PRIVATE);

                    Intent intent = new Intent(StartActivity.this,MainActivity.class);

                    intent.putExtra("lat",preferences.getString("lat",""));
                    intent.putExtra("lon",preferences.getString("lon",""));
                    intent.putExtra("note",preferences.getString("note",""));
                    intent.putExtra("address",preferences.getString("address",""));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            },4000);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1: if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        SharedPreferences preferences = getSharedPreferences("STORENOTE",MODE_PRIVATE);
//
                        Intent intent = new Intent(StartActivity.this,MainActivity.class);
//
//                        intent.putExtra("lat",preferences.getString("lat",""));
//                        intent.putExtra("lon",preferences.getString("lon",""));
//                        intent.putExtra("note",preferences.getString("note",""));
//                        intent.putExtra("address",preferences.getString("address",""));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                },4000);

            }else{
                Toast.makeText(this, "App cannot work without Location Permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
