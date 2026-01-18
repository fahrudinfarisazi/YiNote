package com.faris.yinote.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

public class LocationHelper {

    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public LocationHelper(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startLocationUpdates(LocationCallback callback) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                callback.onLocationReceived(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000, // 10 seconds
                    10, // 10 meters
                    locationListener,
                    Looper.getMainLooper()
            );
        }
    }

    public void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }
}