package pt.nutrium.nutriumlunchdecider.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.nutrium.nutriumlunchdecider.R;
import pt.nutrium.nutriumlunchdecider.models.Restaurant;

class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantAdapterHolder> implements View.OnClickListener {
    private ArrayList<Restaurant> restaurants;
    private Context context;


    public RestaurantAdapter(final Context context, final ArrayList<Restaurant> restaurants) {
        this.context = context;
        this.restaurants = restaurants;
    }


    @Override
    public RestaurantAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_restaurant, parent, false);
        v.setOnClickListener(this);
        return new RestaurantAdapterHolder(v);
    }


    @Override
    public void onBindViewHolder(RestaurantAdapterHolder holder, int position) {
        holder.name.setText("");
        holder.distance.setText("");
        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return restaurants.size();
    }


    @Override
    public void onClick(View view) {
        int p = Integer.parseInt(view.getTag().toString());

    }


    static class RestaurantAdapterHolder extends RecyclerView.ViewHolder {

        protected TextView name;
        protected TextView distance;

        public RestaurantAdapterHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tvRestaurantName);
            distance = (TextView) itemView.findViewById(R.id.tvRestaurantDistance);
        }
    }
}