package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.manager.ManagerCustomersActivity;
import com.example.mobile.manager.ManagerOrdersActivity;
import com.example.mobile.manager.ManagerProductsActivity;
import com.example.mobile.manager.ManagerPromotionsActivity;
import com.example.mobile.manager.ManagerRoutinesActivity;
import com.example.mobile.manager.ManagerStaffsActivity;
import com.example.mobile.manager.ManagerUsersActivity;

public class ManagerHomeActivity extends AppCompatActivity {
    private DropdownMenuManager dropdownMenuManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_home);

        // Initialize DropdownMenuManager
        dropdownMenuManager = new DropdownMenuManager(this);

        // Initialize buttons
        Button usersButton = findViewById(R.id.users_button);
        Button customersButton = findViewById(R.id.customers_button);
        Button ordersButton = findViewById(R.id.orders_button);
        Button productsButton = findViewById(R.id.products_button);
        Button promotionsButton = findViewById(R.id.promotions_button);
        Button routinesButton = findViewById(R.id.routines_button);
        Button staffButton = findViewById(R.id.staff_button);

        // Set click listeners
        usersButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomeActivity.this, ManagerUsersActivity.class);
            startActivity(intent);
        });

        customersButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomeActivity.this, ManagerCustomersActivity.class);
            startActivity(intent);
        });

        ordersButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomeActivity.this, ManagerOrdersActivity.class);
            startActivity(intent);
        });

        productsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomeActivity.this, ManagerProductsActivity.class);
            startActivity(intent);
        });

        promotionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomeActivity.this, ManagerPromotionsActivity.class);
            startActivity(intent);
        });

        routinesButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomeActivity.this, ManagerRoutinesActivity.class);
            startActivity(intent);
        });

        staffButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomeActivity.this, ManagerStaffsActivity.class);
            startActivity(intent);
        });
    }
}