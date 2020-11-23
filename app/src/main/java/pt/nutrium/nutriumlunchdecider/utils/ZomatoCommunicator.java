package pt.nutrium.nutriumlunchdecider.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import pt.nutrium.nutriumlunchdecider.models.Restaurant;

public class ZomatoCommunicator {
    private final static String TAG = "ZC";
    private final static String KEY = "3b7dea60af428d2f8daeb80f7dd4bd37";
    private final static int TIMEOUT = 15000; // em ms

    private final static String BASE_URL = "https://developers.zomato.com/api/v2.1/";
    private final static String GET_GEOCODE = "geocode?lat=%f&lon=%f";

    private final RequestQueue requestQueue;
    private String myLocation;


    public ZomatoCommunicator(final Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }


    public HashMap<String, Restaurant> getRestaurants(double latitude, double longitude) {
        JSONObject jData = requestSyncJsonResponse(String.format(Locale.getDefault(), GET_GEOCODE, latitude, longitude));
        if (jData != null) {
            // Processar localização
            try {
                JSONObject jLocation = jData.getJSONObject("location");
                myLocation = String.format("%s, %s, %s", jLocation.optString("title"), jLocation.optString("city_name"), jLocation.optString("country_name"));
            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
            // Processar restaurantes
            HashMap<String, Restaurant> restaurants = new HashMap<>();
            try {
                JSONArray jRests = jData.getJSONArray("nearby_restaurants");
                for (int i = 0; i < jRests.length(); i++) {
                    Restaurant restaurant = new Restaurant(jRests.getJSONObject(i), latitude, longitude);
                    restaurants.put(restaurant.getId(), restaurant);
                }
                return restaurants;
            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
        }
        return null;
    }


    /**
     * Devolve a localização onde o Zomato pensa que o dispositivo está apos uma consulta
     */
    public String getMyLocation() {
        return myLocation;
    }


    private JSONObject requestSyncJsonResponse(final String url) {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        CommJsonRequest request = new CommJsonRequest(String.format("%s%s", BASE_URL, url), future, future);
        requestQueue.add(request);

        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Log.d(TAG, "Error1: " + ex.getMessage());
        } catch (ExecutionException ex) {
            Log.d(TAG, "Error2: " + ex.getMessage());
        } catch (TimeoutException ex) {
            Log.d(TAG, "Error3: " + ex.getMessage());
        }

        return null;
    }


    // Classe modificada para envio com key no header
    private static class CommJsonRequest extends JsonObjectRequest {

        public CommJsonRequest(final String url, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
            super(Request.Method.GET, url, null, listener, errorListener);
            setRetryPolicy(new DefaultRetryPolicy(TIMEOUT, 1, 1));
        }


        @Override
        public Map<String, String> getHeaders() {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("user-key", KEY);
            return headers;
        }
    }
}
