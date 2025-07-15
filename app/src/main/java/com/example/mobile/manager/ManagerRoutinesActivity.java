package com.example.mobile.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.RoutineAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Routine;
import com.example.mobile.manager.ManagerRoutineDetailActivity;
import com.example.mobile.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerRoutinesActivity extends AppCompatActivity {
    private static final String TAG = "ManagerRoutinesActivity";
    private RecyclerView routinesRecyclerView;
    private RoutineAdapter routineAdapter;
    private EditText searchNameEditText, routineIdEditText;
    private Spinner skinTypeSpinner;
    private Button searchNameButton, searchIdButton, filterButton;
    private LinearLayout filtersLayout;
    private FloatingActionButton createButton, updateButton, deleteButton;
    private List<Routine> routines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_routines);

        routinesRecyclerView = findViewById(R.id.routines_recycler_view);
        routinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routineAdapter = new RoutineAdapter(this, routine -> {
            Intent intent = new Intent(ManagerRoutinesActivity.this, ManagerRoutineDetailActivity.class);
            intent.putExtra("routineID", routine.getRoutineID());
            startActivity(intent);
        });
        routinesRecyclerView.setAdapter(routineAdapter);

        searchNameEditText = findViewById(R.id.search_name_edit_text);
        routineIdEditText = findViewById(R.id.routine_id_edit_text);
        skinTypeSpinner = findViewById(R.id.skin_type_spinner);
        searchNameButton = findViewById(R.id.search_name_button);
        searchIdButton = findViewById(R.id.search_id_button);
        filterButton = findViewById(R.id.filter_button);
        filtersLayout = findViewById(R.id.filters_layout);
        createButton = findViewById(R.id.create_button);
        updateButton = findViewById(R.id.update_button);
        deleteButton = findViewById(R.id.delete_button);

        // Set up spinner with skin types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.skin_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinTypeSpinner.setAdapter(adapter);

        searchNameButton.setOnClickListener(v -> handleSearchByName());
        searchIdButton.setOnClickListener(v -> handleSearchById());
        filterButton.setOnClickListener(v -> handleFilterBySkinType());
        createButton.setOnClickListener(v -> {
            // Placeholder for create logic
            Toast.makeText(this, "Create routine (to be implemented)", Toast.LENGTH_SHORT).show();
        });
        updateButton.setOnClickListener(v -> {
            // Placeholder for update logic
            Toast.makeText(this, "Update routine (to be implemented)", Toast.LENGTH_SHORT).show();
        });
        deleteButton.setOnClickListener(v -> {
            // Placeholder for delete logic
            Toast.makeText(this, "Delete routine (to be implemented)", Toast.LENGTH_SHORT).show();
        });

        loadRoutines();
    }

    private void loadRoutines() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Routine>> call = apiService.getSkinCareRoutines();
        call.enqueue(new Callback<List<Routine>>() {
            @Override
            public void onResponse(Call<List<Routine>> call, Response<List<Routine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Routine> routineList = response.body();
                    if (!routineList.isEmpty()) {
                        routines.clear();
                        routines.addAll(routineList);
                        routineAdapter.setRoutines(routines);
                    } else {
                        routines.clear();
                        routineAdapter.setRoutines(routines);
                        Toast.makeText(ManagerRoutinesActivity.this, "No routines found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerRoutinesActivity.this, "Failed to load routines. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Routine>> call, Throwable t) {
                Log.e(TAG, "API Call Failed for routines: " + t.getMessage(), t);
                Toast.makeText(ManagerRoutinesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSearchByName() {
        String searchName = searchNameEditText.getText().toString().trim();
        if (searchName.isEmpty()) {
            loadRoutines();
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Routine>> call = apiService.searchRoutinesByName(searchName);
        call.enqueue(new Callback<List<Routine>>() {
            @Override
            public void onResponse(Call<List<Routine>> call, Response<List<Routine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Routine> routineList = response.body();
                    if (!routineList.isEmpty()) {
                        routines.clear();
                        routines.addAll(routineList);
                        routineAdapter.setRoutines(routines);
                        Toast.makeText(ManagerRoutinesActivity.this, "Found " + routines.size() + " routines", Toast.LENGTH_SHORT).show();
                    } else {
                        routines.clear();
                        routineAdapter.setRoutines(routines);
                        Toast.makeText(ManagerRoutinesActivity.this, "No routines found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerRoutinesActivity.this, "Search failed. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Routine>> call, Throwable t) {
                Log.e(TAG, "API Call Failed for search: " + t.getMessage(), t);
                Toast.makeText(ManagerRoutinesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSearchById() {
        String routineId = routineIdEditText.getText().toString().trim();
        if (routineId.isEmpty()) {
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Routine>> call = apiService.getRoutineById(routineId);
        call.enqueue(new Callback<List<Routine>>() {
            @Override
            public void onResponse(Call<List<Routine>> call, Response<List<Routine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Routine> routineList = response.body();
                    if (!routineList.isEmpty()) {
                        routines.clear();
                        routines.addAll(routineList);
                        routineAdapter.setRoutines(routines);
                        Toast.makeText(ManagerRoutinesActivity.this, "Routine found", Toast.LENGTH_SHORT).show();
                    } else {
                        routines.clear();
                        routineAdapter.setRoutines(routines);
                        Toast.makeText(ManagerRoutinesActivity.this, "No routine found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerRoutinesActivity.this, "Search by ID failed. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Routine>> call, Throwable t) {
                Log.e(TAG, "API Call Failed for ID search: " + t.getMessage(), t);
                Toast.makeText(ManagerRoutinesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFilterBySkinType() {
        String skinType = skinTypeSpinner.getSelectedItem().toString();
        if ("All Skin Types".equals(skinType)) {
            loadRoutines();
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Routine>> call = apiService.getRoutinesBySkinType(skinType);
        call.enqueue(new Callback<List<Routine>>() {
            @Override
            public void onResponse(Call<List<Routine>> call, Response<List<Routine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Routine> routineList = response.body();
                    if (!routineList.isEmpty()) {
                        routines.clear();
                        routines.addAll(routineList);
                        routineAdapter.setRoutines(routines);
                        Toast.makeText(ManagerRoutinesActivity.this, "Filtered " + routines.size() + " routines", Toast.LENGTH_SHORT).show();
                    } else {
                        routines.clear();
                        routineAdapter.setRoutines(routines);
                        Toast.makeText(ManagerRoutinesActivity.this, "No routines found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerRoutinesActivity.this, "Filter failed. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Routine>> call, Throwable t) {
                Log.e(TAG, "API Call Failed for filter: " + t.getMessage(), t);
                Toast.makeText(ManagerRoutinesActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}