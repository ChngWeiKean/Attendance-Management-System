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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);
    }

    public void changePassword(View view) {
        // Get email from shared preference
        String email = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("email", "");

        // Get the new password from the user
        newPasswordEditText = findViewById(R.id.new_password);
        String newPassword = newPasswordEditText.getText().toString();

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