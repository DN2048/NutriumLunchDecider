package pt.nutrium.nutriumlunchdecider.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import pt.nutrium.nutriumlunchdecider.R;
import pt.nutrium.nutriumlunchdecider.adapters.RestaurantAdapter;
import pt.nutrium.nutriumlunchdecider.models.Restaurant;
import pt.nutrium.nutriumlunchdecider.utils.LocationProvider;
import pt.nutrium.nutriumlunchdecider.utils.ZomatoCommunicator;

public class MainActivity extends AppCompatActivity implements AsyncTaskListener {
    private final static String TAG = "MACT";
    private final static int LOCATION_REQUEST_CODE = 100;

    private ContentLoadingProgressBar pbLoading;
    private TextView tvToolbarSubtext;
    private LocationProvider lpGps;
    private LocationProvider lpNetwork;
    private RecyclerView rvRestaurants;
    private TextView tvIntroMessage;


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
        tvToolbarSubtext = findViewById(R.id.tvToolbarSubtext);
        rvRestaurants = findViewById(R.id.rcRestaurants);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvRestaurants.setLayoutManager(layoutManager);
        tvIntroMessage = findViewById(R.id.tvIntroMessage);
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
            tvToolbarSubtext.setText("");
            mainTask(this, lpGps, lpNetwork, this).execute();
        }
    }


    private static AsyncTask<Void, Void, ArrayList<Restaurant>> mainTask(final Context context, final LocationProvider lpGps, final LocationProvider lpNetwork, final AsyncTaskListener listener) {
        return new AsyncTask<Void, Void, ArrayList<Restaurant>>() {
            @Override
            protected void onPreExecute() {
                lpGps.start();
                lpNetwork.start();
            }

            @Override
            protected ArrayList<Restaurant> doInBackground(Void... voids) {
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
                ZomatoCommunicator zc = new ZomatoCommunicator(context);
                if (accGps < accNetwork) {
                    Log.d(TAG, "GPS Wins: " + String.valueOf(accGps));
                    return zc.getRestaurants(lGps.getLatitude(), lGps.getLongitude());
                } else {
                    Log.d(TAG, "Network Wins: " + String.valueOf(accNetwork));
                    return zc.getRestaurants(lNetwork.getLatitude(), lNetwork.getLongitude());
                }
            }

            @Override
            protected void onPostExecute(ArrayList<Restaurant> restaurants) {
                listener.onMainTaskCompleted(restaurants);
            }
        };
    }


    @Override
    public void onMainTaskCompleted(ArrayList<Restaurant> restaurants) {
        if (restaurants != null) {
            tvIntroMessage.setVisibility(View.GONE);
            rvRestaurants.setAdapter(new RestaurantAdapter(this, restaurants));
        } else
            showSnackbar(getString(R.string.failed_to_get_restaurants));

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
            case R.id.action_help:
                showDialog(getString(R.string.action_help), getString(R.string.help_message));
                return true;
            case R.id.action_about:
                showDialog(getString(R.string.action_about), getString(R.string.about_message));
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findRestaurants();
            }
        }
    }


    /* OUTROS */

    private void showDialog(final String title, final String message) {
        new AlertDialog
                .Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(android.R.string.ok), null)
                .show();
    }


    private void showSnackbar(final String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}