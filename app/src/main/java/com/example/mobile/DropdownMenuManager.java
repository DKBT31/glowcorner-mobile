package com.example.mobile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class DropdownMenuManager {
    private static final String TAG = "DropdownMenuManager";
    private static final String PREF_NAME = "UserPrefs";
    private final AppCompatActivity activity;
    private final ImageButton menuButton;
    private Dialog dropdownDialog;
    private MaterialCardView dropdownMenu;

    public DropdownMenuManager(AppCompatActivity activity) {
        this.activity = activity;
        this.menuButton = activity.findViewById(R.id.menu_button);
        if (menuButton == null) {
            Log.e(TAG, "Menu button not found in layout");
        }
        setupMenu();
    }

    private void setupMenu() {
        // Initialize dialog with custom theme
        dropdownDialog = new Dialog(activity, R.style.DialogOverlayTheme);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dropdown_menu_layout, null);
        dropdownMenu = dialogView.findViewById(R.id.dropdown_menu);
        if (dropdownMenu == null) {
            Log.e(TAG, "Dropdown menu not found in layout");
        }
        dropdownDialog.setContentView(dialogView);

        // Configure dialog window
        Window window = dropdownDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 16; // Left margin
            params.y = 64; // Top margin (above status bar and button)
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        } else {
            Log.e(TAG, "Window is null");
        }

        // Ensure dialog is cancellable and dismisses on outside tap
        dropdownDialog.setCancelable(true);
        dropdownDialog.setCanceledOnTouchOutside(true);

        // Setup menu items
        ImageButton profileMenuItem = dialogView.findViewById(R.id.profile_menu_item);
        ImageButton routineMenuItem = dialogView.findViewById(R.id.routine_menu_item);
        ImageButton logoutMenuItem = dialogView.findViewById(R.id.logout_menu_item);

        if (profileMenuItem != null && routineMenuItem != null && logoutMenuItem != null) {
            profileMenuItem.setOnClickListener(v -> {
                Log.d(TAG, "Profile clicked");
                dropdownDialog.dismiss();
                if (!(activity instanceof ProfileActivity)) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });

            routineMenuItem.setOnClickListener(v -> {
                Log.d(TAG, "Routine clicked");
                dropdownDialog.dismiss();
                if (!(activity instanceof UserRoutineActivity)) {
                    Intent intent = new Intent(activity, UserRoutineActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });

            logoutMenuItem.setOnClickListener(v -> {
                Log.d(TAG, "Logout clicked");
                dropdownDialog.dismiss();
                SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                activity.startActivity(new Intent(activity, SignInActivity.class));
                activity.finish();
            });
        } else {
            Log.e(TAG, "Menu items not found in layout");
        }

        // Toggle menu on button click
        menuButton.setOnClickListener(v -> {
            Log.d(TAG, "Menu button clicked");
            toggleDropdownMenu();
        });
    }

    private void toggleDropdownMenu() {
        if (dropdownDialog.isShowing()) {
            hideDropdownMenu();
        } else {
            showDropdownMenu();
        }
    }

    private void showDropdownMenu() {
        if (!dropdownDialog.isShowing()) {
            Log.d(TAG, "Showing dropdown menu");
            dropdownDialog.show();
            if (dropdownMenu != null) {
                dropdownMenu.setVisibility(View.VISIBLE); // Ensure visible before animation
                Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                fadeIn.setStartOffset(0); // Start immediately
                dropdownMenu.startAnimation(fadeIn);
            } else {
                Log.e(TAG, "Dropdown menu is null");
            }
        }
    }

    private void hideDropdownMenu() {
        if (dropdownDialog.isShowing() && dropdownMenu != null) {
            Log.d(TAG, "Hiding dropdown menu");
            Animation fadeOut = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
            dropdownMenu.startAnimation(fadeOut);
            dropdownMenu.postDelayed(() -> {
                if (dropdownDialog.isShowing()) {
                    dropdownDialog.dismiss();
                }
            }, 200);
        }
    }
}