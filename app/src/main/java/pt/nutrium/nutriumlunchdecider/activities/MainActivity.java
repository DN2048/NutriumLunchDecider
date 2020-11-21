package pt.nutrium.nutriumlunchdecider.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import pt.nutrium.nutriumlunchdecider.utils.ProviderLocationTracker;

public class MainActivity extends AppCompatActivity implements AsyncTaskListener {
    private final static int LOCATION_REQUEST_CODE = 100;

    private ContentLoadingProgressBar pbLoading;
    private ProviderLocationTracker locGps;
    private ProviderLocationTracker locNetwork;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // iniciar variaveis
        locGps = new ProviderLocationTracker(this, ProviderLocationTracker.ProviderType.GPS);
        locNetwork = new ProviderLocationTracker(this, ProviderLocationTracker.ProviderType.NETWORK);

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
        if (locGps != null)
            locGps.stop();
        if (locNetwork != null)
            locNetwork.stop();
        super.onPause();
    }


    /* ROTINAS */

    private void findRestaurants() {
        // Verificar se app tem permissões para aceder à localização
        if (checkLocationPermission()) {
            pbLoading.show();
            mainTask(locGps, locNetwork, this).execute();
        }
    }


    private static AsyncTask<Void, Void, Void> mainTask(ProviderLocationTracker locGps, ProviderLocationTracker locNetwork, AsyncTaskListener listener) {
        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                locGps.start();
                locNetwork.start();

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

        if (id == R.id.action_about) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /* PERMISSÕES */

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
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