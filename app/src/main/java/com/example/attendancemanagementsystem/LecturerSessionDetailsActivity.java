package com.example.attendancemanagementsystem;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LecturerSessionDetailsActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private String userId;
    private SharedPreferences sharedPreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_session_details);

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
                    startActivity(new Intent(LecturerSessionDetailsActivity.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerSessionDetailsActivity.this, LecturerEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(LecturerSessionDetailsActivity.this, LecturerProfileSettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerSessionDetailsActivity.this, MainActivity.class));
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
        userId = sharedPreferences.getString("userId", "");

        // Fetch session ID and course Code from intent
        Intent intent = getIntent();
        String sessionId = intent.getStringExtra("sessionID");
        String courseCode = intent.getStringExtra("course");
        fetchCourseAndSessionDetails(sessionId, courseCode, new CourseSessionCallback() {
            @Override
            public void onCourseSessionReceived(CourseSession fetchedCourseSession) {
                // Inside this callback, you can access the fetched courseSession object
                // and perform any actions you need.
                CourseSession courseSession = fetchedCourseSession;
                TextView courseCodeTextView = findViewById(R.id.course_code);
                TextView dateTextView = findViewById(R.id.session_date);
                TextView startTimeTextView = findViewById(R.id.session_start_time);
                TextView endTimeTextView = findViewById(R.id.session_end_time);

                courseCodeTextView.setText(courseSession.getCourseCode());
                dateTextView.setText(courseSession.getDate());
                startTimeTextView.setText(courseSession.getStartTime());
                endTimeTextView.setText(courseSession.getEndTime());

                createStudentArray(courseSession, new LecturerSessionDetailsActivity.StudentArrayCallback() {
                    @Override
                    public void onStudentArrayReceived(String[][] studentArray) {
                        TableLayout studentAttendanceTable = findViewById(R.id.student_attendance_list_table);
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
                            } else if (studentInfo[2].equals("Absent")) {
                                attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_red)));
                            } else if (studentInfo[2].equals("Late")) {
                                attendanceStatusButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                            }

                            // Add the inflated TableRow to the TableLayout
                            studentAttendanceTable.addView(row);
                        }

                        Button exportSessionDetailsButton = findViewById(R.id.export_session_details_button);
                        exportSessionDetailsButton.setEnabled(true);
                        exportSessionDetailsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("Session List", "Export session details to CSV");
                                exportSessionsToCSV(sessionId, courseCode, courseSession, studentArray);
                            }
                        });
                    }
                });
            }
        });
    }

    public interface CourseSessionCallback {
        void onCourseSessionReceived(CourseSession courseSession);
    }

    public void fetchCourseAndSessionDetails(String sessionId, String courseCode, CourseSessionCallback callback) {
        DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("course_sessions").child(sessionId);
        sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String date = snapshot.child("date").getValue(String.class);
                String startTime = snapshot.child("startTime").getValue(String.class);
                String endTime = snapshot.child("endTime").getValue(String.class);
                HashMap<String, String> studentAttendanceData = new HashMap<>();

                DataSnapshot studentAttendanceSnapshot = snapshot.child("studentAttendanceStatus");

                for (DataSnapshot studentSnapshot : studentAttendanceSnapshot.getChildren()) {
                    String studentID = studentSnapshot.getKey();
                    String attendanceStatus = studentSnapshot.getValue(String.class);
                    studentAttendanceData.put(studentID, attendanceStatus);
                }

                CourseSession courseSession = new CourseSession();
                courseSession.setCourseCode(courseCode);
                courseSession.setDate(date);
                courseSession.setStartTime(startTime);
                courseSession.setEndTime(endTime);
                courseSession.setStudentAttendanceStatus(studentAttendanceData);

                // Call the callback with the courseSession object when the data is fetched
                callback.onCourseSessionReceived(courseSession);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
                Log.w("fetchSessionDetails", "Failed to read value.", error.toException());
            }
        });
    }

    public void createStudentArray(CourseSession courseSession, LecturerSessionDetailsActivity.StudentArrayCallback callback) {
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

    public void exportSessionsToCSV(String sessionId, String courseCode, CourseSession courseSession, String[][] studentArray) {
        try {
            // Get the app's data directory
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            Log.d(TAG, "Directory: " + dir.getAbsolutePath());

            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    Log.d(TAG, "Directory created successfully");
                } else {
                    Log.e(TAG, "Failed to create directory");
                }
            }

            // Create a CSV file for sessions in the app's data directory
            File file = new File(dir, courseCode + "_" + sessionId + "_session_details_data.csv");
            FileWriter writer = new FileWriter(file);

            // Define the session details
            String[] sessionDetails = {"Course Code", "Session ID", "Start Time", "End Time"};


            // Write the session details
            for (String detail : sessionDetails) {
                writer.append(detail);
                writer.append(",");
            }
            writer.append("\n");

            writer.append(courseCode);
            writer.append(",");
            writer.append(sessionId);
            writer.append(",");
            writer.append(courseSession.getStartTime());
            writer.append(",");
            writer.append(courseSession.getEndTime());

            writer.append("\n");
            writer.append("\n");

            // Define column names (headers)
            String[] headers = {"Student Name", "Student ID", "Attendance Status"};

            // Write the header row
            for (String header : headers) {
                writer.append(header);
                writer.append(",");
            }
            writer.append("\n");

            // Write the data rows based on the provided studentArray
            for (String[] studentInfo : studentArray) {
                for (String info : studentInfo) {
                    writer.append(info);
                    writer.append(",");
                }
                writer.append("\n");
            }

            // Close the writer
            writer.flush();
            writer.close();

            // Display a success message
            Toast.makeText(this, "Session details exported successfully!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "File path: " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            // Handle errors, such as file I/O issues
            // Log error
            Log.e("File Error", e.getMessage());
        }
    }
}
