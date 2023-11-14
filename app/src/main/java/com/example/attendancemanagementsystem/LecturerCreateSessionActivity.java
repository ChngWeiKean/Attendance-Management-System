package com.example.attendancemanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class LecturerCreateSessionActivity extends AppCompatActivity {
    TextView title;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private CourseSession courseSession;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_session);

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
                    startActivity(new Intent(LecturerCreateSessionActivity.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerCreateSessionActivity.this, LecturerEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(LecturerCreateSessionActivity.this, LecturerProfileSettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerCreateSessionActivity.this, MainActivity.class));
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

        title = findViewById(R.id.title);
        // Access course code from intent
        String courseCode = getIntent().getStringExtra("course");
        Log.d("Course Code", courseCode);
        // Assign text of page title to courseCode
        title.setText(courseCode);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ProgressBar qrCodeLoader = findViewById(R.id.loadingProgressBarQRCode);
        ProgressBar tableRowLoader = findViewById(R.id.loadingProgressBarTableRows);

        Runnable onComplete = () -> {
            qrCodeLoader.setVisibility(View.VISIBLE);

            // This code will run when the fetch operation is complete
            Log.d("Fetch Course", "Fetch completed!");
            String qrCodeString = String.valueOf(courseSession.getSessionID()) + '|' + courseSession.getCourseCode() + '|' + courseSession.getDate();
            Log.d("QR Code String", "QR Code string: " + qrCodeString);
            Bitmap qrCodeBitmap = generateQRCode(qrCodeString);
            ImageView qrCodeImageView = findViewById(R.id.qr_code);
            qrCodeImageView.setImageBitmap(qrCodeBitmap);

            qrCodeLoader.setVisibility(View.GONE);

            createStudentArray(courseSession, new StudentArrayCallback() {
                @Override
                public void onStudentArrayReceived(String[][] studentArray) {
                    tableRowLoader.setVisibility(View.VISIBLE);
                    TableLayout studentAttendanceTable = findViewById(R.id.current_sessions_attendance_table);
                    DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("course_sessions").child(String.valueOf(courseSession.getSessionID()));

                    for (String[] studentInfo : studentArray) {
                        // Inflate a TableRow from the row layout XML
                        TableRow row = (TableRow) inflater.inflate(R.layout.add_new_current_session_student_attendance_row, null);

                        // Find the TextViews within the inflated TableRow and set their text
                        TextView nameTextView = row.findViewById(R.id.name);
                        TextView idTextView = row.findViewById(R.id.id);
                        Button attendanceStatusButton = row.findViewById(R.id.status);

                        // Set the text for each TextView based on the student's information
                        nameTextView.setText(studentInfo[0]);
                        idTextView.setText(studentInfo[1]);
                        attendanceStatusButton.setText(studentInfo[2]);
                        if (studentInfo[2].equals("Present")) {
                            attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                            attendanceStatusButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Create a confirmation dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LecturerCreateSessionActivity.this);
                                    builder.setTitle("Set attendance of " + studentInfo[1] + " to absent?");
                                    builder.setMessage("Are you sure you want to approve this action?");

                                    // Add a positive button (Yes) and its action
                                    builder.setPositiveButton("Yes", (dialog, which) -> {
                                        attendanceStatusButton.setText("Absent");
                                        attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_red)));
                                        courseSession.getStudentAttendanceStatus().put(studentInfo[1], "Absent");
                                        // Update the attendance status in the database
                                        sessionRef.child("studentAttendanceStatus").child(studentInfo[1]).setValue("Absent");

                                        Toast.makeText(LecturerCreateSessionActivity.this, "Attendance of " + studentInfo[1] + " has been set to absent.", Toast.LENGTH_LONG).show();
                                    });

                                    // Add a negative button (No) and its action
                                    builder.setNegativeButton("No", (dialog, which) -> {
                                        // Dismiss the dialog (do nothing)
                                        dialog.dismiss();
                                    });

                                    // Show the dialog
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
                        } else if (studentInfo[2].equals("Absent")) {
                            attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_red)));
                            attendanceStatusButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Create a confirmation dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LecturerCreateSessionActivity.this);
                                    builder.setTitle("Set attendance of " + studentInfo[1] + " to present?");
                                    builder.setMessage("Are you sure you want to approve this action?");

                                    // Add a positive button (Yes) and its action
                                    builder.setPositiveButton("Yes", (dialog, which) -> {
                                        attendanceStatusButton.setText("Present");
                                        attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                                        courseSession.getStudentAttendanceStatus().put(studentInfo[1], "Present");
                                        // Update the attendance status in the database
                                        sessionRef.child("studentAttendanceStatus").child(studentInfo[1]).setValue("Present");

                                        Toast.makeText(LecturerCreateSessionActivity.this, "Attendance of " + studentInfo[1] + " has been set to present.", Toast.LENGTH_LONG).show();
                                    });

                                    // Add a negative button (No) and its action
                                    builder.setNegativeButton("No", (dialog, which) -> {
                                        // Dismiss the dialog (do nothing)
                                        dialog.dismiss();
                                    });

                                    // Show the dialog
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
                        } else if (studentInfo[2].equals("Late")) {
                            attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                            attendanceStatusButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Create a confirmation dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LecturerCreateSessionActivity.this);
                                    builder.setTitle("Set attendance of " + studentInfo[1] + " to absent?");
                                    builder.setMessage("Are you sure you want to approve this action?");

                                    // Add a positive button (Yes) and its action
                                    builder.setPositiveButton("Yes", (dialog, which) -> {
                                        attendanceStatusButton.setText("Absent");
                                        attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_red)));
                                        courseSession.getStudentAttendanceStatus().put(studentInfo[1], "Absent");
                                        // Update the attendance status in the database
                                        sessionRef.child("studentAttendanceStatus").child(studentInfo[1]).setValue("Absent");

                                        Toast.makeText(LecturerCreateSessionActivity.this, "Attendance of " + studentInfo[1] + " has been set to absent.", Toast.LENGTH_LONG).show();
                                    });

                                    // Add a negative button (No) and its action
                                    builder.setNegativeButton("No", (dialog, which) -> {
                                        // Dismiss the dialog (do nothing)
                                        dialog.dismiss();
                                    });

                                    // Show the dialog
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
                        }

                        // Add the inflated TableRow to the TableLayout
                        studentAttendanceTable.addView(row);
                    }

                    tableRowLoader.setVisibility(View.GONE);
                }
            });
        };

        courseSession = fetchCourseDetailsAndSession(courseCode, onComplete);
    }

    private CourseSession fetchCourseDetailsAndSession(String courseCode, Runnable onComplete) {
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseCode);
        DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("course_sessions");

        // Initialize a CourseSession object
        CourseSession courseSession = new CourseSession();

        // Get course sessions and update the database
        courseRef.child("courseSessions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> courseSessions = (List<String>) task.getResult().getValue();

                // Initialize variables to track the session with the largest numeric value
                int largestSessionID = 0;
                int largestSessionNumber = 0;

                // Iterate through the course sessions to find the one with the largest numeric value
                for (String sessionKey : courseSessions) {
                    Integer sessionID = Integer.valueOf(sessionKey);

                    // Compare session numbers to find the largest one
                    if (sessionID > largestSessionNumber) {
                        largestSessionNumber = sessionID;
                        largestSessionID = sessionID;
                    }
                }

                // Fetch session info based on the largestSessionID
                int finalLargestSessionID = largestSessionID;
                sessionRef.child(String.valueOf(largestSessionID)).get().addOnCompleteListener(sessionTask -> {
                    if (sessionTask.isSuccessful()) {
                        DataSnapshot sessionData = sessionTask.getResult();

                        if (sessionData.exists()) {
                            // Retrieve session details
                            String date = sessionData.child("date").getValue(String.class);
                            // Get student hash map from session
                            HashMap<String, String> studentAttendanceData = new HashMap<>();

                            // Get the "studentAttendanceStatus" node from sessionData
                            DataSnapshot studentAttendanceSnapshot = sessionData.child("studentAttendanceStatus");

                            // Iterate through the child nodes of "studentAttendanceStatus"
                            for (DataSnapshot studentSnapshot : studentAttendanceSnapshot.getChildren()) {
                                // Get the student ID (key) and attendance status (value)
                                String studentID = studentSnapshot.getKey();
                                String attendanceStatus = studentSnapshot.getValue(String.class);

                                // Put the data into the HashMap
                                studentAttendanceData.put(studentID, attendanceStatus);
                            }

                            // Set the retrieved data in the CourseSession object
                            courseSession.setCourseCode(courseCode);
                            courseSession.setSessionID(finalLargestSessionID);
                            courseSession.getSessionID();
                            Log.d("Session ID", "Session ID: " + String.valueOf(courseSession.getSessionID()));
                            courseSession.setStudentAttendanceStatus(studentAttendanceData);
                            courseSession.setDate(date);

                            // Log the largest session ID
                            Log.d("Largest Session ID", "Current session ID: " + String.valueOf(finalLargestSessionID));
                        } else {
                            Log.d("Session Data", "No session data found for the session ID.");
                        }
                        onComplete.run();
                    } else {
                        Log.d("Session Data", "Error getting session data: " + sessionTask.getException());
                    }
                });
            } else {
                Log.d("Course Sessions", "Error getting course sessions");
            }
        });

        // Return the CourseSession object
        return courseSession;
    }

    public Bitmap generateQRCode(String qrData) {
        try {
            // Encode the data into a QR code
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    qrData, // Data to encode
                    BarcodeFormat.QR_CODE,
                    1200, // Width of the QR code
                    1200 // Height of the QR code
            );

            // Create a bitmap from the BitMatrix
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // Set the pixels based on the BitMatrix
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return qrBitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null; // Handle the exception as needed
        }
    }

    public void createStudentArray(CourseSession courseSession, StudentArrayCallback callback) {
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("users");
        HashMap<String, String> studentAttendanceData = (HashMap<String, String>) courseSession.getStudentAttendanceStatus();

        // Create a list to store student information and attendance status
        List<String[]> studentList = new ArrayList<>();

        // Iterate through studentAttendanceData and fetch student details asynchronously
        for (Map.Entry<String, String> entry : studentAttendanceData.entrySet()) {
            String studentID = entry.getKey();
            String attendanceStatus = entry.getValue();

            studentRef.child(studentID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String studentName = dataSnapshot.child("username").getValue(String.class);

                        // Create a string array with student information and attendance status
                        String[] studentInfo = new String[3];
                        studentInfo[0] = studentName;
                        studentInfo[1] = studentID;
                        studentInfo[2] = attendanceStatus;

                        // Add the studentInfo to the list
                        studentList.add(studentInfo);

                        // Check if all students have been processed
                        if (studentList.size() == studentAttendanceData.size()) {
                            // Convert the list to a 2D array
                            String[][] studentArray = new String[studentList.size()][3];
                            for (int i = 0; i < studentList.size(); i++) {
                                String[] info = studentList.get(i);
                                studentArray[i][0] = info[0]; // Student name
                                studentArray[i][1] = info[1]; // Student ID
                                studentArray[i][2] = info[2]; // Attendance status
                            }

                            // Invoke the callback with the studentArray
                            callback.onStudentArrayReceived(studentArray);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database read errors
                }
            });
        }
    }

    public interface StudentArrayCallback {
        void onStudentArrayReceived(String[][] studentArray);
    }

    public void endCurrentSession(View view) {
        String courseCode = getIntent().getStringExtra("course");
        Runnable onComplete = new Runnable() {
            @Override
            public void run() {
                Log.d("Update Session", "Session updating!");
                Integer sessionID = courseSession.getSessionID();
                updateSession(sessionID, courseCode);
            }
        };

        CourseSession courseSession = fetchCourseDetailsAndSession(courseCode, onComplete);
    }

    public void updateSession(Integer sessionID, String courseCode) {
        // Find course session
        DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("course_sessions").child(String.valueOf(sessionID));
        // Update the end time to current time
        sessionRef.child("endTime").setValue(getCurrentFormattedTime());
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseCode);
        courseRef.child("hasOngoingSession").setValue(false);

        Log.d("Update Session", "Session updated!");
        Toast.makeText(this, "Session " + sessionID + " for " + courseCode + " has ended.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LecturerCourseDetailsActivity.class);
        intent.putExtra("course", courseCode);
        startActivity(intent);
    }

    private String getCurrentFormattedTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur")); // Set the timezone to Kuala Lumpur
        return timeFormat.format(new Date());
    }
}
