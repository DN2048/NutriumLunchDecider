package pt.nutrium.nutriumlunchdecider.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;

import java.util.ArrayList;

import pt.nutrium.nutriumlunchdecider.R;
import pt.nutrium.nutriumlunchdecider.utils.LocationProvider;

public class MainActivity extends AppCompatActivity implements AsyncTaskListener {
    private final static String TAG = "MACT";
    private final static int LOCATION_REQUEST_CODE = 100;

    private ContentLoadingProgressBar pbLoading;
    private LocationProvider lpGps;
    private LocationProvider lpNetwork;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // iniciar variaveis
        lpGps = new LocationProvider(this, LocationProvider.ProviderType.GPS);
        lpNetwork = new LocationProvider(this, LocationProvider.ProviderType.NETWORK);

        // Atribuir views às variaveis
        pbLoading = findViewById(R.id.pbLoading);
    }


    @Override
    protected void onResume() {
        super.onResume();
        findRestaurants();
    }

    @Override
    protected void onPause() {
        if (lpGps != null)
            lpGps.stop();
        if (lpNetwork != null)
            lpNetwork.stop();
        super.onPause();
    }


    /* ROTINAS */

    private void findRestaurants() {
        // Verificar se app tem permissões para aceder à localização
        if (checkLocationPermission()) {
            pbLoading.show();
            mainTask(lpGps, lpNetwork, this).execute();
        }
    }


    private static AsyncTask<Void, Void, Void> mainTask(LocationProvider lpGps, LocationProvider lpNetwork, AsyncTaskListener listener) {
        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                lpGps.start();
                lpNetwork.start();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                // Verificar qual sistema devolve melhor precisão da localização (GPS ou rede)
                Location lGps = lpGps.getLocation();
                Location lNetwork = lpNetwork.getLocation();
                if (lGps == null && lNetwork == null) {
                    Log.d(TAG, "nada");
                    return null;
                }
                float accGps = lGps == null ? LocationProvider.NO_LOCATION_ACCURACY : lGps.getAccuracy();
                float accNetwork = lNetwork == null ? LocationProvider.NO_LOCATION_ACCURACY : lNetwork.getAccuracy();
                // Ir buscar restaurantes baseado na posição do vencedor
                if (accGps < accNetwork)
                    Log.d(TAG, "GPS Wins: " + String.valueOf(accGps));
                else
                    Log.d(TAG, "Network Wins: " + String.valueOf(accNetwork));

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                listener.onMainTaskCompleted(new ArrayList<String>());
            }
        };
    }


    @Override
    public void onMainTaskCompleted(ArrayList<String> restaurants) {
        pbLoading.hide();
    }


    /* MENU */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                findRestaurants();
                return true;
            case R.id.action_about:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /* PERMISSÕES */

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return false;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findRestaurants();
            }
        }
    }
}