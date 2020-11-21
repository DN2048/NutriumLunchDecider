package pt.nutrium.nutriumlunchdecider.activities;

import java.util.ArrayList;

interface AsyncTaskListener {
    void onMainTaskCompleted(ArrayList<String> restaurants);
}
