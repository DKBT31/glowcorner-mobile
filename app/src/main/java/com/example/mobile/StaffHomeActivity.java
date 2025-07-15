package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.staff.StaffSkinQuizActivity;
import com.example.mobile.staff.StaffFeedbackActivity;

public class StaffHomeActivity extends AppCompatActivity {
    private DropdownMenuManager dropdownMenuManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_home);

        // Initialize DropdownMenuManager
        dropdownMenuManager = new DropdownMenuManager(this);

        // Initialize buttons
        Button skinQuizButton = findViewById(R.id.skin_quiz_button);
        Button shopButton = findViewById(R.id.shop_button);
        Button feedbackButton = findViewById(R.id.feedback_button);

        // Set click listeners
        skinQuizButton.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHomeActivity.this, StaffSkinQuizActivity.class);
            startActivity(intent);
        });

        shopButton.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHomeActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        feedbackButton.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHomeActivity.this, StaffFeedbackActivity.class);
            startActivity(intent);
        });
    }
}