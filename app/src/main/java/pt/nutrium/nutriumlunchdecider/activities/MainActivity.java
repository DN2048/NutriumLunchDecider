package pt.nutrium.nutriumlunchdecider.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import pt.nutrium.nutriumlunchdecider.R;
import pt.nutrium.nutriumlunchdecider.adapters.RestaurantAdapter;
import pt.nutrium.nutriumlunchdecider.adapters.RestaurantAdapterInterface;
import pt.nutrium.nutriumlunchdecider.models.Restaurant;
import pt.nutrium.nutriumlunchdecider.utils.LocalDatabase;
import pt.nutrium.nutriumlunchdecider.utils.LocationProvider;
import pt.nutrium.nutriumlunchdecider.utils.LocationProviderInterface;
import pt.nutrium.nutriumlunchdecider.utils.ZomatoCommunicator;

public class MainActivity extends AppCompatActivity implements LocationProviderInterface, RestaurantAdapterInterface {
    private final static String TAG = "MACT";
    private final static int LOCATION_REQUEST_CODE = 100;

    private ProgressBar pbLoading;
    private TextView tvToolbarSubtext;
    private RecyclerView rvRestaurants;
    private TextView tvIntroMessage;
    private TextView tvWaitingMessage;

    private LocalDatabase localDb;
    private LocationProvider lpGps;
    private LocationProvider lpNetwork;
    private HashMap<String, Restaurant> restaurants;
    private RestaurantAdapter restaurantAdapter;
    private boolean foundLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // iniciar variaveis
        localDb = new LocalDatabase(this);
        lpGps = new LocationProvider(this, this, LocationProvider.ProviderType.GPS);
        lpNetwork = new LocationProvider(this, this, LocationProvider.ProviderType.NETWORK);

        // Atribuir views às variaveis
        pbLoading = findViewById(R.id.pbLoading);
        tvToolbarSubtext = findViewById(R.id.tvToolbarSubtext);
        tvIntroMessage = findViewById(R.id.tvIntroMessage);
        tvWaitingMessage = findViewById(R.id.tvWaitingMessage);
        rvRestaurants = findViewById(R.id.rcRestaurants);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvRestaurants.setLayoutManager(layoutManager);

        // Carregar favoritos se existirem
        restaurants = localDb.getFavouriteRestaurants();
        if (restaurants != null)
            mainTaskCompleted();
        else
            restaurants = new HashMap<>();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationProviders();
    }


    @Override
    protected void onPause() {
        stopLocationProviders();
        super.onPause();
    }


    @Override
    protected void onStop() {
        localDb.close();
        super.onStop();
    }


    /* ROTINAS */

    private void startLocationProviders() {
        // Verificar se app tem permissões para aceder à localização
        if (checkLocationPermission()) {
            lpGps.start();
            lpNetwork.start();
        }
    }


    private void stopLocationProviders() {
        if (lpGps != null)
            lpGps.stop();
        if (lpNetwork != null)
            lpNetwork.stop();
    }


    private void findRestaurants() {

        if (checkLocationPermission()) {
            pbLoading.setVisibility(View.VISIBLE);
            tvToolbarSubtext.setText("");
            mainTask(this).execute();
        }
    }


    private static AsyncTask<Void, String, Integer> mainTask(MainActivity mainActivity) {
        return new AsyncTask<Void, String, Integer>() {
            final WeakReference<MainActivity> activityRef = new WeakReference<>(mainActivity);

            @Override
            protected Integer doInBackground(Void... voids) {
                final MainActivity main = activityRef.get();
                // Verificar qual sistema devolve melhor precisão da localização (GPS ou rede)
                Location lGps = main.lpGps.getLocation();
                Location lNetwork = main.lpNetwork.getLocation();
                if (lGps == null && lNetwork == null) {
                    // Sem gps
                    return 1;
                }
                float accGps = lGps == null ? LocationProvider.NO_LOCATION_ACCURACY : lGps.getAccuracy();
                float accNetwork = lNetwork == null ? LocationProvider.NO_LOCATION_ACCURACY : lNetwork.getAccuracy();
                // Ir buscar restaurantes baseado na localização do vencedor
                HashMap<String, Restaurant> newRestaurants;
                ZomatoCommunicator zc = new ZomatoCommunicator(main);
                if (accGps < accNetwork) {
                    newRestaurants = zc.getRestaurants(lGps.getLatitude(), lGps.getLongitude());
                } else {
                    newRestaurants = zc.getRestaurants(lNetwork.getLatitude(), lNetwork.getLongitude());
                }
                // Apresentar localização de a apanhou
                publishProgress(zc.getMyLocation());
                // Se foi possivel obter os restaurantes, juntar/atualizar com os atuais
                if (newRestaurants != null) {
                    for (String key : newRestaurants.keySet()) {
                        Restaurant newRestaurant = newRestaurants.get(key);
                        if (main.restaurants.containsKey(key)) {
                            // Restaurante já existe na lista, atualizar
                            if (main.restaurants.put(key, newRestaurants.get(key)).isFavourite()) {
                                // Ja existe e é um favorito. Atualizar na bd local tambem
                                newRestaurant.setFavourite(true);
                                main.localDb.updateFavouriteRestaurant(newRestaurant);
                            }
                        } else {
                            // Restaurante não existe na lista. Inserir.
                            main.restaurants.put(newRestaurant.getId(), newRestaurant);
                        }
                    }
                    // tudo ok
                    return 0;
                } else
                    // Sem net ou não encontrou restaurantes perto
                    return 2;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                final MainActivity main = activityRef.get();
                main.tvToolbarSubtext.setText(values[0]);
            }

            @Override
            protected void onPostExecute(Integer result) {
                final MainActivity main = activityRef.get();
                main.mainTaskCompleted();
                if (result == 1) // sem gps
                    main.showSnackbar(main.getString(R.string.failed_to_get_location));
                else if (result == 2) // sem net ou nao ha restaurantes
                    main.showSnackbar(main.getString(R.string.failed_to_get_restaurants));
            }
        };
    }


    void mainTaskCompleted() {
        if (restaurants.size() > 0) {
            tvIntroMessage.setVisibility(View.GONE);
            if (restaurantAdapter == null) {
                restaurantAdapter = new RestaurantAdapter(this, this, new ArrayList<>(restaurants.values()));
                rvRestaurants.setAdapter(restaurantAdapter);
            } else {
                restaurantAdapter.updateRestaurants(new ArrayList<>(restaurants.values()));
            }
        }

        pbLoading.setVisibility(View.GONE);
    }


    @Override
    public void locationUpdate(LocationProvider lp) {
        if (lp.getLocation() != null && !foundLocation) {
            foundLocation = true;
            tvWaitingMessage.setVisibility(View.GONE);
            findRestaurants();
        }
    }


    @Override
    public void locationServicesTurnedOn() {
        startLocationProviders();
    }


    @Override
    public void favouriteChanged(Restaurant restaurant) {
        if (restaurant.isFavourite())
            localDb.insertFavouriteRestaurant(restaurant);
        else
            localDb.deleteFavouriteRestaurant(restaurant.getId());
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
            case R.id.action_sort_price:
                if (restaurantAdapter != null)
                    restaurantAdapter.sortRestaurants(Restaurant.compareByPrice);
                return true;
            case R.id.action_sort_distance:
                if (restaurantAdapter != null)
                    restaurantAdapter.sortRestaurants(Restaurant.compareByDistance);
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