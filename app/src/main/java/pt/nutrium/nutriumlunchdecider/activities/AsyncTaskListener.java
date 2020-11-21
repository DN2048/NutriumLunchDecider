package pt.nutrium.nutriumlunchdecider.activities;

import java.util.ArrayList;

import pt.nutrium.nutriumlunchdecider.models.Restaurant;

interface AsyncTaskListener {
    void onMainTaskCompleted(ArrayList<Restaurant> restaurants);
}
