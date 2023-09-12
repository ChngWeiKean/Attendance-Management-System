package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText idEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        idEditText = findViewById(R.id.studentID);
        registerButton = findViewById(R.id.registerButton);

        // Set click listener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString().toLowerCase();
                String id = idEditText.getText().toString().toLowerCase();
                String password = passwordEditText.getText().toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("users");

                // Check if the email and ID already exist in a case-insensitive manner
                usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot emailSnapshot) {
                        if (emailSnapshot.exists()) {
                            // Email already exists, show an error message
                            Toast.makeText(RegisterActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Check if the ID exists
                            usersRef.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot idSnapshot) {
                                    if (idSnapshot.exists()) {
                                        // ID already exists, show an error message
                                        Toast.makeText(RegisterActivity.this, "ID already exists!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Both email and ID are unique
                                        // Validate email format and determine user type
                                        if (isValidEmail(email)) {
                                            String userType = getUserType(email);

                                            // Attempt to save the user data
                                            try {
                                                // Create a User object with the user information
                                                User user = new User(username, email, password, userType, id);

                                                // Save the user to Firebase Realtime Database
                                                usersRef.child(id).setValue(user);

                                                // Registration successful
                                                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                                Log.d("RegisterActivity", "Registration Successful!");
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            } catch (Exception e) {
                                                // Registration failed, handle the error (e.g., username already exists)
                                                Toast.makeText(RegisterActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.e("RegisterActivity", "Registration Failed: " + e.getMessage());
                                            }
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Invalid Email Format! Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle any errors that may occur during database read
                                    Toast.makeText(RegisterActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("RegisterActivity", "Database Error: " + databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors that may occur during database read
                        Toast.makeText(RegisterActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("RegisterActivity", "Database Error: " + databaseError.getMessage());
                    }
                });
            }
        });

    }

    public void openLoginPage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private boolean isValidEmail(String email) {
        // Define regular expressions for student and lecturer email formats
        String studentPattern = "^p\\d{8}@student\\.newinti\\.edu\\.my$";
        String lecturerPattern = "^l\\d{8}@newinti\\.edu\\.my$";

        // Check if the email matches either student or lecturer format
        return email.matches(studentPattern) || email.matches(lecturerPattern);
    }

    private String getUserType(String email) {
        if (email.matches("^p\\d{8}@student\\.newinti\\.edu\\.my$")) {
            return "student";
        } else if (email.matches("^l\\d{8}@newinti\\.edu\\.my$")) {
            return "lecturer";
        } else {
            return "unknown";
        }
    }
}