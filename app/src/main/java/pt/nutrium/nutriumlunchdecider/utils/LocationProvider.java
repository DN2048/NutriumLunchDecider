package pt.nutrium.nutriumlunchdecider.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationProvider implements LocationListener, GpsStatus.Listener {
    private static final String TAG = "LP";
    private static final long MIN_UPDATE_DISTANCE = 5; // em metros
    private static final long MIN_UPDATE_TIME = 15000; // em ms
    private static final long STALE_THRESHOLD = MIN_UPDATE_TIME * 4 * 5; // = 5 minutos
    public static final float NO_LOCATION_ACCURACY = 9999;


    public enum ProviderType {
        NETWORK, GPS
    }

    private final LocationManager lm;
    private final String provider;

    private Location lastLocation;
    private long lastTime;
    private boolean isRunning;
    private final LocationProviderInterface lpi;

    public LocationProvider(Context context, LocationProviderInterface lpi, ProviderType type) {
        this.lpi = lpi;
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (type == ProviderType.NETWORK)
            provider = LocationManager.NETWORK_PROVIDER;
        else
            provider = LocationManager.GPS_PROVIDER;
    }


    @SuppressLint("MissingPermission") // As permissões são verificadas na actividade principal antes de iniciar
    public void start() {
        if (!isRunning) {
            isRunning = true;
            lastLocation = null;
            lastTime = 0;
            lm.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
            lm.addGpsStatusListener(this);
        }
    }


    public void stop() {
        if (isRunning) {
            lm.removeUpdates(this);
            isRunning = false;
        }
    }


    public Location getLocation() {
        if (lastLocation == null) {
            return null;
        }
        // Não houve mais atualizações de posição
        if (System.currentTimeMillis() - lastTime > STALE_THRESHOLD) {
            return null;
        }
        return lastLocation;
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


    @Override
    public void onGpsStatusChanged(int status) {
        if (status == GpsStatus.GPS_EVENT_STARTED)
            lpi.locationServicesTurnedOn();
    }


    public static double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        float[] results = new float[3];
        Location.distanceBetween(startLat, startLon, endLat, endLon, results);
        return results[0] / 1000; // Resultado vem em metros
    }
}