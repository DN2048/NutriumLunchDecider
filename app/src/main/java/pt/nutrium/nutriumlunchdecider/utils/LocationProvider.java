package pt.nutrium.nutriumlunchdecider.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.location.LocationManagerCompat;

public class LocationProvider implements LocationListener {

    private static final long MIN_UPDATE_DISTANCE = 5; // em metros
    private static final long MIN_UPDATE_TIME = 15000; // em ms
    private static final long STALE_THRESHOLD = MIN_UPDATE_TIME * 5;
    public static final float NO_LOCATION_ACCURACY = 9999;

    public enum ProviderType {
        NETWORK, GPS
    }

    private final LocationManager lm;
    private final String provider;

    private Location lastLocation;
    private long lastTime;
    private boolean isRunning;
    private LocationProviderInterface lpi;

    public LocationProvider(Context context, LocationProviderInterface lpi, ProviderType type) {
        this.lpi = lpi;
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (type == ProviderType.NETWORK)
            provider = LocationManager.NETWORK_PROVIDER;
        else
            provider = LocationManager.GPS_PROVIDER;
    }


    public void start() {
        if (!isRunning) {
            isRunning = true;
            lastLocation = null;
            lastTime = 0;
            lm.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
        }
    }


    public void stop() {
        if (isRunning) {
            lm.removeUpdates(this);
            isRunning = false;
        }
    }


    public boolean hasLocation() {
        if (lastLocation == null) {
            return false;
        } else if (System.currentTimeMillis() - lastTime > STALE_THRESHOLD)
            // Não houve mais atualizações de posição
            return false;
        else
            return true;
    }


    public boolean hasPossiblyStaleLocation() {
        if (lastLocation != null) {
            return true;
        }
        return lm.getLastKnownLocation(provider) != null;
    }


    public Location getLocation() {
        if (lastLocation == null) {
            return null;
        }
        if (System.currentTimeMillis() - lastTime > STALE_THRESHOLD) {
            return null;
        }
        return lastLocation;
    }


    public Location getPossiblyStaleLocation() {
        if (lastLocation != null) {
            return lastLocation;
        }
        return lm.getLastKnownLocation(provider);
    }


    public void onLocationChanged(Location newLoc) {
        long now = System.currentTimeMillis();
        lastLocation = newLoc;
        lastTime = now;
        if (lpi != null)
            lpi.locationUpdate(this);
    }


    public void onProviderDisabled(String arg0) {

    }


    public void onProviderEnabled(String arg0) {

    }


    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }


    public static boolean isLocationEnabled(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }


    public static double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        float[] results = new float[3];
        Location.distanceBetween(startLat, startLon, endLat, endLon, results);
        return results[0] / 1000; // Resultado vem em metros
    }
}
