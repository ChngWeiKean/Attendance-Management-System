package com.example.attendancemanagementsystem;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.HashMap;
import java.util.List;

public class StudentQRScannerActivity extends AppCompatActivity implements BarcodeCallback {

    private DecoratedBarcodeView barcodeView;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_qr_scanner);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        NavigationView navigationView = findViewById(R.id.navigation_view);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.menu_dashboard);
        menuItem.setChecked(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.menu_dashboard) {
                    startActivity(new Intent(StudentQRScannerActivity.this, StudentDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentQRScannerActivity.this, StudentEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_qr_scanner) {
                    startActivity(new Intent(StudentQRScannerActivity.this, StudentQRScannerActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(StudentQRScannerActivity.this, StudentProfileSettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(StudentQRScannerActivity.this, MainActivity.class));
                }
                return true;
            }
        });

        drawerLayout = findViewById(R.id.nav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Get the user's ID from shared preferences, which was obtained during login.
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        // Initialize the barcode view
        barcodeView = findViewById(R.id.qrCodeScannerView);

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            // Permission already granted, start the scanner
            startScanner();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, start the scanner
                startScanner();
            } else {
                // Camera permission denied, handle this case as needed
                Log.e("QRScanner", "Camera permission denied");
            }
        }
    }

    private void startScanner() {
        // Start camera when the activity resumes
        barcodeView.resume();
        // Set the barcode callback to handle scanned results
        barcodeView.decodeSingle(this);
    }

    @Override
    public void barcodeResult(BarcodeResult result) {
        // Handle the scanned QR code result here
        String scannedData = result.getText();

        // Process the scanned data as needed
        handleScannedData(scannedData);
    }

    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {
        // Handle possible result points here (optional)
    }

    private void handleScannedData(String scannedData) {
        // Get the user's ID from shared preferences, which was obtained during login.
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        // Log scanned data
        Log.d("QRScanner", "Scanned data: " + scannedData);

        // Split data into parts to get course and session info
        String[] dataParts = scannedData.split("\\|");
        // Get the course session using dataParts[0]
        String courseSession = dataParts[0];
        String courseCode = dataParts[1];
        DatabaseReference courseSessionRef = FirebaseDatabase.getInstance().getReference("course_sessions").child(courseSession);
        // Access student attendance data in courseSessionRef
        DatabaseReference studentAttendanceRef = courseSessionRef.child("studentAttendanceStatus");
        // Get the hashmap of student IDs and their attendance status
        studentAttendanceRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the hashmap of student IDs and their attendance status
                HashMap<String, String> studentAttendanceStatus = (HashMap<String, String>) task.getResult().getValue();
                // Check if the attendance status of current user is already marked present
                if (studentAttendanceStatus.get(userId).equals("Present")) {
                    // Make toast message
                    Toast.makeText(StudentQRScannerActivity.this, "Attendance has already been marked", Toast.LENGTH_LONG).show();
                } else {
                    // Update the attendance status of the current user
                    studentAttendanceStatus.put(userId, "Present");
                    // Update the attendance status in the database
                    studentAttendanceRef.setValue(studentAttendanceStatus);

                    Toast.makeText(StudentQRScannerActivity.this, "Attendance for " + courseCode + " has been marked successfully.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Handle this case as needed
                Log.e("QRScanner", "Error getting data", task.getException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start camera when the activity resumes
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop camera when the activity is paused
        barcodeView.pause();
    }
}