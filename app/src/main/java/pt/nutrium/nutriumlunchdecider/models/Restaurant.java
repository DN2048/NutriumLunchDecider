package pt.nutrium.nutriumlunchdecider.models;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Comparator;

import pt.nutrium.nutriumlunchdecider.utils.LocationProvider;

public class Restaurant {

    private String id;
    private String name;
    private String address;
    private String cuisines;
    private String currency;
    private double distance;
    private double price;
    private double stars;
    private boolean favourite;


    public Restaurant() {

    }


    /**
     * Contructor do objecto apartir dum JSON.
     *
     * @param json      json a onde vai buscar os dados
     * @param latitude  latitude da posição atual para depois calcular a distancia ao restaurante
     * @param longitude lontitude da posição atual para depois calcular a distancia ao restaurante
     */
    public Restaurant(JSONObject json, double latitude, double longitude) {
        try {
            json = json.getJSONObject("restaurant");
            id = json.getString("id");
            name = json.optString("name");
            JSONObject jLocation = json.getJSONObject("location");
            address = jLocation.optString("address"); //String.format("%s, %s - %s", jLocation.optString("address"), jLocation.optString("zipcode"), jLocation.optString("city"));
            distance = LocationProvider.calculateDistance(latitude, longitude, jLocation.getDouble("latitude"), jLocation.getDouble("longitude"));
            cuisines = json.optString("cuisines");
            price = json.optDouble("average_cost_for_two") / 2; // por algum motivo dá o preço para 2 pessoas
            currency = json.optString("currency");
            JSONObject jUserRating = json.getJSONObject("user_rating");
            stars = jUserRating.getDouble("aggregate_rating");
        } catch (Exception ex) {
        }
    }


    /* COMPARADORES */

    public static Comparator<Restaurant> compareByPrice = new Comparator<Restaurant>() {
        @Override
        public int compare(Restaurant r1, Restaurant r2) {
            return Double.compare(r2.getPrice(), r1.getPrice());
        }
    };


    public static Comparator<Restaurant> compareByDistance = new Comparator<Restaurant>() {
        @Override
        public int compare(Restaurant r1, Restaurant r2) {
            return Double.compare(r2.getDistance(), r1.getDistance());
        }
    };


    /* GETS E SETS */

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCuisines() {
        return cuisines;
    }

    public String getCurrency() {
        if (TextUtils.isEmpty(currency))
            return "";
        else
            return currency;
    }

    public double getDistance() {
        return distance;
    }

    public double getPrice() {
        return price;
    }

    public double getStars() {
        return stars;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void toggleFavourite() {
        favourite = !favourite;
    }
}
