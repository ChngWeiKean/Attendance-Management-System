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
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentProfileSettingsActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;
    private DatabaseReference userRef;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_profile_settings);

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
                    startActivity(new Intent(StudentProfileSettingsActivity.this, StudentDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentProfileSettingsActivity.this, StudentEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_qr_scanner) {
                    startActivity(new Intent(StudentProfileSettingsActivity.this, StudentQRScannerActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(StudentProfileSettingsActivity.this, StudentProfileSettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(StudentProfileSettingsActivity.this, MainActivity.class));
                }
                return true;
            }
        });

        drawerLayout = findViewById(R.id.nav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        currentPasswordEditText = findViewById(R.id.student_edit_profile_current_password);
        newPasswordEditText = findViewById(R.id.student_edit_profile_new_password);
        confirmPasswordEditText = findViewById(R.id.student_edit_profile_confirm_new_password);
        usernameEditText = findViewById(R.id.student_edit_profile_username);
        emailEditText = findViewById(R.id.student_edit_profile_email);
        applyButton = findViewById(R.id.student_edit_profile_apply_button);

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
                    String currentUsername = dataSnapshot.child("username").getValue(String.class);
                    String currentEmail = dataSnapshot.child("email").getValue(String.class);

                    // Set the EditTexts with the current name and email
                    usernameEditText.setText(currentUsername);
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
                final String newUsername = usernameEditText.getText().toString();
                final String newEmail = emailEditText.getText().toString();

                userRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String storedPassword = dataSnapshot.getValue(String.class);
                            boolean isPasswordValid = currentPassword.equals(storedPassword);

                            if (isPasswordValid) {
                                if (!newUsername.isEmpty() && !newEmail.isEmpty()) {
                                    verifyUsername(newUsername, newEmail, newPassword, confirmNewPassword);
                                } else {
                                    Toast.makeText(StudentProfileSettingsActivity.this, "Username and email cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(StudentProfileSettingsActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
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

    private void verifyUsername(final String newUsername, final String newEmail, final String newPassword, final String confirmNewPassword) {
        userRef.orderByChild("username").equalTo(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot nameSnapshot) {
                if (nameSnapshot.exists()) {
                    Toast.makeText(StudentProfileSettingsActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                } else {
                    verifyEmail(newUsername, newEmail, newPassword, confirmNewPassword);
                }
            }

            @Override
            public void onCancelled(DatabaseError nameError) {
                Log.e("Database Error", nameError.getMessage());
            }
        });
    }

    private void verifyEmail(final String newUsername, final String newEmail, final String newPassword, final String confirmNewPassword) {
        if (isValidEmail(newEmail)) {
            userRef.orderByChild("email").equalTo(newEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot emailSnapshot) {
                    if (emailSnapshot.exists()) {
                        Toast.makeText(StudentProfileSettingsActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        verifyPassword(newUsername, newEmail, newPassword, confirmNewPassword);
                    }
                }

                @Override
                public void onCancelled(DatabaseError emailError) {
                    Log.e("Database Error", emailError.getMessage());
                }
            });
        } else {
            Toast.makeText(StudentProfileSettingsActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyPassword(final String newUsername, final String newEmail, final String newPassword, final String confirmNewPassword) {
        if (!newPassword.isEmpty() && !confirmNewPassword.isEmpty()) {
            if (newPassword.equals(confirmNewPassword)) {
                userRef.child("password").setValue(newPassword);
            } else {
                Toast.makeText(StudentProfileSettingsActivity.this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
            }
        }
        updateUsername(newUsername);
        updateEmail(newEmail);
        Toast.makeText(StudentProfileSettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(String email) {
        String studentPattern = "^p\\d{8}@student\\.newinti\\.edu\\.my$";

        return email.matches(studentPattern);
    }

    private void updateUsername(String newUsername) {
        userRef.child("username").setValue(newUsername);
    }

    private void updateEmail(String newEmail) {
        userRef.child("email").setValue(newEmail);
    }
}