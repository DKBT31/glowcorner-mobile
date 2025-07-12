package com.example.mobile.manager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.User;
import com.example.mobile.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerUsersActivity extends AppCompatActivity {
    private static final String TAG = "ManagerUsersActivity";
    private RecyclerView userList;
    private ProgressDialog progressDialog;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_users);

        userList = findViewById(R.id.user_list);
        userList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter();
        userList.setAdapter(userAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(v -> {
            // Logic to be added later
        });

        loadUsers();
    }

    private void loadUsers() {
        progressDialog.show();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<User>> call = apiService.getManagerUsers();

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    userAdapter.setUsers(response.body());
                } else {
                    String errorMsg = response.message();
                    Log.e(TAG, "Load Users Response unsuccessful: " + errorMsg);
                    Toast.makeText(ManagerUsersActivity.this, "Failed to load users: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Load Users API Call Failed: " + t.getMessage(), t);
                Toast.makeText(ManagerUsersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;

        public void setUsers(List<User> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.userId.setText("ID: " + user.getUserID());
            holder.name.setText("Name: " + (user.getFullName() != null ? user.getFullName() : "N/A"));
            holder.email.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
            if (user.getAvatar_url() != null) {
                Glide.with(holder.itemView.getContext()).load(user.getAvatar_url()).into(holder.avatar);
            } else {
                holder.avatar.setImageResource(android.R.drawable.ic_menu_gallery); // Default image
            }

            holder.deleteButton.setOnClickListener(v -> deleteUser(user.getUserID()));
            holder.updateButton.setOnClickListener(v -> {
                Intent intent = new Intent(ManagerUsersActivity.this, ManagerEditUserActivity.class);
                intent.putExtra("userID", user.getUserID());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }

        private void deleteUser(String userId) {
            progressDialog.show();
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<Void> call = apiService.deleteManagerUser(userId);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(ManagerUsersActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Refresh list
                    } else {
                        Toast.makeText(ManagerUsersActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ManagerUsersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView userId, name, email;
            ImageView avatar;
            ImageButton deleteButton, updateButton; // Changed from Button to ImageButton

            UserViewHolder(View itemView) {
                super(itemView);
                userId = itemView.findViewById(R.id.user_id);
                name = itemView.findViewById(R.id.user_name);
                email = itemView.findViewById(R.id.user_email);
                avatar = itemView.findViewById(R.id.user_avatar);
                deleteButton = itemView.findViewById(R.id.delete_button); // Cast to ImageButton
                updateButton = itemView.findViewById(R.id.update_button); // Cast to ImageButton
            }
        }
    }
}