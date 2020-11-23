package pt.nutrium.nutriumlunchdecider.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import pt.nutrium.nutriumlunchdecider.R;
import pt.nutrium.nutriumlunchdecider.models.Restaurant;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantAdapterHolder> implements View.OnClickListener, View.OnLongClickListener {
    private ArrayList<Restaurant> restaurants;
    private final Context context;
    private final RestaurantAdapterInterface rai;
    private Comparator<Restaurant> lastComparatorUsed;
    private boolean sortAsc;


    public RestaurantAdapter(final Context context, final RestaurantAdapterInterface rai, final ArrayList<Restaurant> restaurants) {
        this.context = context;
        this.restaurants = restaurants;
        this.rai = rai;
    }


    @Override
    public RestaurantAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_restaurant, parent, false);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new RestaurantAdapterHolder(v);
    }


    @Override
    public void onBindViewHolder(RestaurantAdapterHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        holder.name.setText(restaurant.getName());
        if (restaurant.getDistance() < 0)
            holder.distance.setText(String.format("-- %s", context.getString(R.string.km)));
        else
            holder.distance.setText(String.format(Locale.getDefault(), "%.2f %s", restaurant.getDistance(), context.getString(R.string.km)));
        holder.price.setText(String.format(Locale.getDefault(), "%s%.2f", restaurant.getCurrency(), restaurant.getPrice()));
        holder.stars.setRating((float) restaurant.getStars());
        holder.address.setText(restaurant.getAddress());
        holder.cuisines.setText(restaurant.getCuisines());
        holder.favourite.setVisibility(restaurant.isFavourite() ? View.VISIBLE : View.GONE);

        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return restaurants.size();
    }


    @Override
    public void onClick(View view) {
        int p = Integer.parseInt(view.getTag().toString());
        Uri addressUri = Uri.parse("geo:0,0?q=" + Uri.encode(restaurants.get(p).getName().replace(", ", "+")));
        context.startActivity(new Intent(Intent.ACTION_VIEW, addressUri));
    }


    @Override
    public boolean onLongClick(View view) {
        int p = Integer.parseInt(view.getTag().toString());
        restaurants.get(p).toggleFavourite();
        rai.favouriteChanged(restaurants.get(p));
        this.notifyItemChanged(p);
        return true;
    }


    public void sortRestaurants(Comparator<Restaurant> comparator) {
        // Só alterar a ordenação se o comparator for o mesmo (ex: para evitar de ordenação por preços ASC para distancia DESC)
        if (comparator != lastComparatorUsed)
            lastComparatorUsed = comparator;
        else
            sortAsc = !sortAsc;

        if (sortAsc)
            Collections.sort(restaurants, comparator);
        else
            Collections.sort(restaurants, Collections.reverseOrder(comparator));

        this.notifyDataSetChanged();
    }


    public void updateRestaurants(ArrayList<Restaurant> restaurants) {
        this.restaurants = restaurants;
        // Voltar a ordenar
        if (lastComparatorUsed != null)
            if (sortAsc)
                Collections.sort(restaurants, lastComparatorUsed);
            else
                Collections.sort(restaurants, Collections.reverseOrder(lastComparatorUsed));

        this.notifyDataSetChanged();
    }


    static class RestaurantAdapterHolder extends RecyclerView.ViewHolder {

        protected TextView name;
        protected TextView distance;
        protected TextView price;
        protected RatingBar stars;
        protected TextView cuisines;
        protected TextView address;
        protected ImageView favourite;

        public RestaurantAdapterHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvRestaurantName);
            distance = itemView.findViewById(R.id.tvRestaurantDistance);
            price = itemView.findViewById(R.id.tvRestaurantPrice);
            stars = itemView.findViewById(R.id.rbRestaurantStars);
            cuisines = itemView.findViewById(R.id.tvRestaurantCuisines);
            address = itemView.findViewById(R.id.tvRestaurantAddress);
            favourite = itemView.findViewById(R.id.ivFavourite);
        }
    }
}