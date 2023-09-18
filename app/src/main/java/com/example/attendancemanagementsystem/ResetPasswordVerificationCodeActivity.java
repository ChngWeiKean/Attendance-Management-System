package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordVerificationCodeActivity extends AppCompatActivity {

    EditText verificationCodeEditText;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_verification);
    }

    public void verifyCode(View view) {
        // Get the verification code from the user
        verificationCodeEditText = findViewById(R.id.verificationCode);
        String verificationCode = verificationCodeEditText.getText().toString();

        // Get verification code from shared preferences
        String storedVerificationCode = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("verificationCode", "");
        Log.d("Correct Verification Code", storedVerificationCode);
        Log.d("Verification Code Input", verificationCode);

        // Compare the verification code entered by the user with the one stored in shared preferences
        if (verificationCode.equals(storedVerificationCode)) {
            // Display success message
            verificationCodeEditText.setError(null);
            // Redirect to the reset password page
            startActivity(new Intent(ResetPasswordVerificationCodeActivity.this, ResetPasswordActivity.class));
        } else {
            // Display error message
            verificationCodeEditText.setError("Invalid verification code. Please try again.");
        }
    }

    public void redirectBack() {
        // Redirect back to the login page
        startActivity(new Intent(ResetPasswordVerificationCodeActivity.this, ForgotPasswordActivity.class));
    }
}
