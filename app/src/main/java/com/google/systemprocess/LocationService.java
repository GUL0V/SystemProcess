package com.google.systemprocess;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import static com.google.systemprocess.App.CHANNEL_ID;

public class LocationService extends Service implements LocationListener {

    public Notification notification;
    NotificationCompat.Builder builder;
    double longitude = 0.0, latitude = 0.0;
    long time = 0;
    LocationManager lm;

    private DatabaseReference mDatabase;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);



        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Геотрекер неактивен")
                .setContentText("Поиск GPS")
                .setSmallIcon(R.drawable.ic_baseline_gps_fixed_24)
                .setContentIntent(pendingIntent);
        notification = builder.build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public int size = 0;
    String city = "";
    @Override
    public void onLocationChanged(@NonNull Location location) {
        Date d = new Date(location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        NumberFormat formatter = new DecimalFormat("#0.00");

        longitude = location.getLongitude();
        latitude = location.getLatitude();
        time = location.getTime();
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
             city = addresses.get(0).getAddressLine(0);;
        } catch (IOException e) {
            e.printStackTrace();
        }


        mDatabase.child("locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                size = (int)snapshot.getChildrenCount();
                mDatabase.child("locations").child((size+1)+"").setValue(new LocationModel(latitude+"", longitude+"", time+"", location.getAltitude()+"", city));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


            builder.setContentTitle("Геотрекер активен")
                    .setSilent(true)
                    .setContentText(latitude +", " + longitude);
            notification = builder.build();
            startForeground(1, notification);
            Toast.makeText(getApplicationContext(), latitude+"", Toast.LENGTH_SHORT).show();



        Log.d("planeta", "onLocationChanged: "+location);
        }


    }




