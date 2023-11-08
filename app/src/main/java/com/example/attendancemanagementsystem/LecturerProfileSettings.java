package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LecturerProfileSettings extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;
    private DatabaseReference userRef;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_profile_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        NavigationView navigationView = findViewById(R.id.navigation_view);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.menu_user_profile);
        menuItem.setChecked(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.menu_dashboard) {
                    startActivity(new Intent(LecturerProfileSettings.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerProfileSettings.this, LecturerEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(LecturerProfileSettings.this, LecturerProfileSettings.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerProfileSettings.this, MainActivity.class));
                }
                return true;
            }
        });

        drawerLayout = findViewById(R.id.nav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        currentPasswordEditText = findViewById(R.id.lecturer_edit_profile_current_password);
        newPasswordEditText = findViewById(R.id.lecturer_edit_profile_new_password);
        confirmPasswordEditText = findViewById(R.id.lecturer_edit_profile_confirm_new_password);
        nameEditText = findViewById(R.id.lecturer_edit_profile_username);
        emailEditText = findViewById(R.id.lecturer_edit_profile_email);
        applyButton = findViewById(R.id.lecturer_edit_profile_apply_button);

        // Get the user's ID from shared preferences, which was obtained during login.
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        // Get references to the user in the database.
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userRef = databaseReference.child("users").child(userId);

        // Retrieve the user's name and email from the database
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentName = dataSnapshot.child("name").getValue(String.class);
                    String currentEmail = dataSnapshot.child("email").getValue(String.class);

                    // Set the EditTexts with the current name and email
                    nameEditText.setText(currentName);
                    emailEditText.setText(currentEmail);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Database Error", databaseError.getMessage());
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String currentPassword = currentPasswordEditText.getText().toString();
                final String newPassword = newPasswordEditText.getText().toString();
                final String confirmNewPassword = confirmPasswordEditText.getText().toString();
                final String newName = nameEditText.getText().toString();
                final String newEmail = emailEditText.getText().toString();

                userRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String storedPassword = dataSnapshot.getValue(String.class);
                            boolean isPasswordValid = currentPassword.equals(storedPassword);

                            if (isPasswordValid) {
                                if (!newName.isEmpty() && !newEmail.isEmpty()) {
                                    verifyName(newName, newEmail, newPassword, confirmNewPassword);
                                } else {
                                    Toast.makeText(LecturerProfileSettings.this, "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LecturerProfileSettings.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Database Error", databaseError.getMessage());
                    }
                });
            }
        });
    }

    private void verifyName(final String newName, final String newEmail, final String newPassword, final String confirmNewPassword) {
        userRef.orderByChild("name").equalTo(newName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot nameSnapshot) {
                if (nameSnapshot.exists()) {
                    Toast.makeText(LecturerProfileSettings.this, "Name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    verifyEmail(newName, newEmail, newPassword, confirmNewPassword);
                }
            }

            @Override
            public void onCancelled(DatabaseError nameError) {
                Log.e("Database Error", nameError.getMessage());
            }
        });
    }

    private void verifyEmail(final String newName, final String newEmail, final String newPassword, final String confirmNewPassword) {
        if (isValidEmail(newEmail)) {
            userRef.orderByChild("email").equalTo(newEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot emailSnapshot) {
                    if (emailSnapshot.exists()) {
                        Toast.makeText(LecturerProfileSettings.this, "Email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        verifyPassword(newName, newEmail, newPassword, confirmNewPassword);
                    }
                }

                @Override
                public void onCancelled(DatabaseError emailError) {
                    Log.e("Database Error", emailError.getMessage());
                }
            });
        } else {
            Toast.makeText(LecturerProfileSettings.this, "Invalid email format", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyPassword(final String newName, final String newEmail, final String newPassword, final String confirmNewPassword) {
        if (!newPassword.isEmpty() && !confirmNewPassword.isEmpty()) {
            if (newPassword.equals(confirmNewPassword)) {
                userRef.child("password").setValue(newPassword);
            } else {
                Toast.makeText(LecturerProfileSettings.this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
            }
        }
        updateName(newName);
        updateEmail(newEmail);
        Toast.makeText(LecturerProfileSettings.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(String email) {
        String lecturerPattern = "^l\\d{8}@newinti\\.edu\\.my$";

        return email.matches(lecturerPattern);
    }

    private void updateName(String newName) {
        userRef.child("name").setValue(newName);
    }

    private void updateEmail(String newEmail) {
        userRef.child("email").setValue(newEmail);
    }
}

