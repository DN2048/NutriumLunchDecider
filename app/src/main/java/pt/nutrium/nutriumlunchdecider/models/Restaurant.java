package pt.nutrium.nutriumlunchdecider.models;

import android.database.Cursor;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Comparator;
import java.util.LinkedHashMap;

import pt.nutrium.nutriumlunchdecider.utils.LocalDatabase;
import pt.nutrium.nutriumlunchdecider.utils.LocationProvider;

public class Restaurant {
    public static final String DB_ID = "id";
    public static final String DB_NAME = "name";
    public static final String DB_ADDRESS = "address";
    public static final String DB_CUISINES = "cuisines";
    public static final String DB_CURRENCY = "currency";
    public static final String DB_PRICE = "price";
    public static final String DB_STARS = "stars";

    public static LinkedHashMap<String, String> sqlTableFields() {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        {
            fields.put(DB_ID, "TEXT PRIMARY KEY NOT NULL");
            fields.put(DB_NAME, "TEXT DEFAULT NULL");
            fields.put(DB_ADDRESS, "TEXT DEFAULT NULL");
            fields.put(DB_CUISINES, "TEXT DEFAULT NULL");
            fields.put(DB_CURRENCY, "TEXT DEFAULT €");
            fields.put(DB_PRICE, "REAL DEFAULT 0");
            fields.put(DB_STARS, "REAL DEFAULT 0");
        }

        return fields;
    }


    private String id;
    private String name;
    private String address;
    private String cuisines;
    private String currency;
    private double distance;
    private double price;
    private double stars;
    private boolean favourite;


    /**
     * Contructor do objecto apartir da BD
     *
     * @param cursor    cursor aberto da db
     */
    public Restaurant(Cursor cursor) {
        id = LocalDatabase.getValueString(cursor, DB_ID);
        name = LocalDatabase.getValueString(cursor, DB_NAME);
        address = LocalDatabase.getValueString(cursor, DB_ADDRESS);
        cuisines = LocalDatabase.getValueString(cursor, DB_CUISINES);
        currency = LocalDatabase.getValueString(cursor, DB_CURRENCY);
        distance = -1;
        price = LocalDatabase.getValueDouble(cursor, DB_PRICE);
        stars = LocalDatabase.getValueDouble(cursor, DB_STARS);
        favourite = true;
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

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public void toggleFavourite() {
        favourite = !favourite;
    }
}
