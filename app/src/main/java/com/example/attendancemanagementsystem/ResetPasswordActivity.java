package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText newPasswordEditText;
    EditText confirmPasswordEditText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectBack();
            }
        });
    }

    public void changePassword(View view) {
        // Get email from shared preference
        String email = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("email", "");

        // Get the new password from the user
        newPasswordEditText = findViewById(R.id.new_password);
        String newPassword = newPasswordEditText.getText().toString();
        // Get confirm password from user
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        String confirmPassword = confirmPasswordEditText.getText().toString();
        // Ensure confirm password matches new password
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        // Find the user with the email
        userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get user id and reset password
                    String userId = dataSnapshot.getChildren().iterator().next().getKey();
                    userRef.child(userId).child("password").setValue(newPassword);
                    // Display success pop-up
                    newPasswordEditText.setError(null);
                    // Remove verification code and email from shared preferences
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().remove("verificationCode").apply();
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().remove("email").apply();
                    // Redirect to the login page
                    startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
                } else {
                    // Handle the case where no user with the specified email was found
                    Log.d("Error", "No user found with email: " + email);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database errors
                Log.d("Error", "onCancelled: " + databaseError);
            }
        });
    }

    public void redirectBack() {
        // Redirect back to the login page
        startActivity(new Intent(ResetPasswordActivity.this, ResetPasswordVerificationCodeActivity.class));
    }
}