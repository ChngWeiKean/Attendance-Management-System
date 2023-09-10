package com.example.attendancemanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CheckBox;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    Button loginButton;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView signup = (TextView) findViewById(R.id.signupText);
        signup.setMovementMethod(LinkMovementMethod.getInstance());

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();

                // Authenticate the user by checking email and password in the database
                authenticateUser(userEmail, userPassword);
            }
        });

        rememberMeCheckBox = findViewById(R.id.remember_me);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Load the "Remember Me" preference and set the CheckBox state
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        rememberMeCheckBox.setChecked(rememberMe);

        // If "Remember Me" is checked, auto-login
        if (rememberMe) {
            String savedEmail = sharedPreferences.getString("userEmail", "");
            String savedPassword = sharedPreferences.getString("userPassword", "");
            if (!TextUtils.isEmpty(savedEmail) && !TextUtils.isEmpty(savedPassword)) {
                email.setText(savedEmail);
                password.setText(savedPassword);
                // Perform auto-login
                authenticateUser(savedEmail, savedPassword);
            }
        }

        rememberMeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the "Remember Me" preference when the CheckBox state changes
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("rememberMe", isChecked);
                editor.apply();
            }
        });
    }

    private void authenticateUser(final String userEmail, final String userPassword) {
        // Firebase Realtime Database reference
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Query to find a user with the provided email
        Query emailQuery = usersRef.orderByChild("email").equalTo(userEmail);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email exists in the database, now check the password
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.getPassword().equals(userPassword)) {
                            // Password matches, login successful
                            String userType = user.getUserType();
                            rememberMeCheckBox = findViewById(R.id.remember_me);
                            // Load the "Remember Me" preference and set the CheckBox state
                            boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
                            rememberMeCheckBox.setChecked(rememberMe);

                            sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            // If "Remember Me" is checked, auto-login
                            if (rememberMe) {
                                editor.putString("userEmail", user.getEmail());
                                editor.putString("userPassword", user.getPassword());
                            }

                            editor.putString("userId", user.getId());
                            editor.apply();
                            // Redirect user to the appropriate dashboard based on userType
                            redirectToDashboard(userType);
                            return; // Exit the loop
                        }
                    }
                    // Password doesn't match
                    Toast.makeText(MainActivity.this, "Invalid password. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    // Email not found in the database
                    Toast.makeText(MainActivity.this, "Email not registered.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur during database read
                Toast.makeText(MainActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToDashboard(String userType) {
        if ("student".equalsIgnoreCase(userType)) {
            // Redirect to the student dashboard
            startActivity(new Intent(MainActivity.this, StudentDashboardActivity.class));
            Log.d("TAG", "student dashboard");
        } else if ("lecturer".equalsIgnoreCase(userType)) {
            // Redirect to the lecturer dashboard
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        } else {
            // Handle other user types or show an error
            Toast.makeText(MainActivity.this, "Unknown user type.", Toast.LENGTH_SHORT).show();
        }
    }

    public void openRegistrationPage(View view) {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
    }
}