package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationManager {
    private static final String TAG = "NavigationManager";

    public static void setupNavigation(AppCompatActivity activity, BottomNavigationView bottomNavigationView) {
        if (bottomNavigationView != null) {
            // Set the correct selected item based on current activity
            setCorrectSelectedItem(activity, bottomNavigationView);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    if (!(activity instanceof HomeActivity)) {
                        Log.d(TAG, "Navigating to HomeActivity");
                        Intent intent = new Intent(activity, HomeActivity.class);
                        activity.startActivity(intent);
                    }
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    if (!(activity instanceof CartActivity)) {
                        Log.d(TAG, "Navigating to CartActivity");
                        Intent intent = new Intent(activity, CartActivity.class);
                        activity.startActivity(intent);
                    }
                    return true;
                } else if (itemId == R.id.nav_quiz) {
                    if (!(activity instanceof QuizActivity)) {
                        Log.d(TAG, "Navigating to QuizActivity");
                        Intent intent = new Intent(activity, QuizActivity.class);
                        activity.startActivity(intent);
                    }
                    return true;
                }
                return false;
            });
        } else {
            Log.e(TAG, "BottomNavigationView not found in layout");
        }
    }

    private static void setCorrectSelectedItem(AppCompatActivity activity, BottomNavigationView bottomNavigationView) {
        if (activity instanceof HomeActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (activity instanceof CartActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        } else if (activity instanceof QuizActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_quiz);
        }
    }
}