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

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailEditText;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

    }

    public void sendResetPasswordEmail(View view) {
        emailEditText = findViewById(R.id.email);
        String email = emailEditText.getText().toString();
        String verificationCode = generateVerificationCode();
        // Store verification code in shared preferences
        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putString("verificationCode", verificationCode).apply();

        String customHtmlContent = "<html><body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;\">"
                + "<div style=\"background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\">"
                + "<h1 style=\"color: #333;\">Password Reset Verification</h1>"
                + "<p style=\"color: #555; font-size: 16px;\">Dear User,</p>"
                + "<p style=\"color: #555; font-size: 16px;\">You have requested a password reset for your account.</p>"
                + "<p style=\"color: #555; font-size: 16px;\">Here is your 4-digit verification code:</p>"
                + "<h2 style=\"color: #007BFF; font-size: 24px; margin-top: 10px;\">" + verificationCode + "</h2>"
                + "<p style=\"color: #555; font-size: 16px;\">If you didn't request this reset, please ignore this email.</p>"
                + "<p style=\"color: #555; font-size: 16px;\">Best regards,</p>"
                + "<p style=\"color: #555; font-size: 16px;\">CheckMate</p>"
                + "</div>"
                + "</body></html>";


        // Check if email is empty
        if (email.isEmpty()) {
            // Display error message
            emailEditText.setError("Email is required");
        } else {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
            // Check if email exists in the database
            userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Email exists in the database, send the reset email
                        EmailService.sendEmail(email, "Password Reset Verification Code", customHtmlContent, new EmailService.EmailCallback() {
                            @Override
                            public void onEmailSent() {
                                // Handle success, for example, show a success message to the user
                                Log.d("EmailService", "Email sent successfully");
                                getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putString("email", email).apply();
                                // Redirect to the verification code page
                                startActivity(new Intent(ForgotPasswordActivity.this, ResetPasswordVerificationCodeActivity.class));
                            }

                            @Override
                            public void onError(Exception e) {
                                // Handle the error, for example, show an error message or log the exception
                                Log.e("EmailService", "Email not sent", e);
                            }
                        });
                    } else {
                        // Email does not exist in the database
                        emailEditText.setError("Email does not exist");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error if needed
                    Log.e("FirebaseDatabase", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }

    public String generateVerificationCode() {
        Random rand = new Random();
        int code = rand.nextInt(9000) + 1000; // Generate a random 4-digit code
        return String.valueOf(code);
    }

    public void redirectBack() {
        // Redirect back to the login page
        startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
    }
}
