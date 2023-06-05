package com.example.magnetometro;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.Manifest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Magnetometro extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback {
    public ImageButton button;
    private GoogleMap mMap;
    private TextView textView;
    private TextView x;
    private TextView y;
    private TextView z;
    private Location location;
    private SensorManager mSensorManager;
    private Sensor mMagField;
    private GeomagneticField geoField;
    private static final float ALPHA = 0.1f;
    private float[] filteredMagneticField = new float[3];
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean permissaoDada;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.magnetometro);

        textView = findViewById(R.id.textView);
        x = findViewById(R.id.eixoX);
        y = findViewById(R.id.eixoY);
        z = findViewById(R.id.eixoZ);
        button = findViewById(R.id.ButtonMagReturn);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    textView.setText("Location: " + location.getLatitude() + ", " + location.getLongitude());
                    Magnetometro.this.location = location;
                }
            }

        };

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        getLocationPermission();


    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissaoDada = true;
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void startLocationUpdates() {
        if (permissaoDada) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(new LocationRequest(),
                    mLocationCallback,
                    null);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mMagField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            if (location == null) {
                return;
            }
            geoField = new GeomagneticField(
                    (float)location.getLatitude(),
                    (float)location.getLongitude(),
                    (float)location.getAltitude(),
                    System.currentTimeMillis()
            );

            filteredMagneticField[0] += ALPHA * (event.values[0] - filteredMagneticField[0]);
            filteredMagneticField[1] += ALPHA * (event.values[1] - filteredMagneticField[1]);
            filteredMagneticField[2] += ALPHA * (event.values[2] - filteredMagneticField[2]);


            x.setText("Valor aceitável: 100 nT");
            z.setText(String.format("Valor medido: %.2f", (filteredMagneticField[0] + filteredMagneticField[1] + filteredMagneticField[2])/3) + " µT");
            //z.setText(String.format("Z: %.2f", filteredMagneticField[2]) + " µT");

        }
        if (mMap != null) {
            if (permissaoDada) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                                    .title("Localização Atual")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        }
                    }
                });
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
