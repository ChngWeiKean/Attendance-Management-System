package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentCourseDetailsActivity extends AppCompatActivity {

    TextView title;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    List<List<String>> studentAttendancePieChartData = new ArrayList<>();
    String userId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_course_details);

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
                    startActivity(new Intent(StudentCourseDetailsActivity.this, StudentDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentCourseDetailsActivity.this, StudentEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_qr_scanner) {
                    startActivity(new Intent(StudentCourseDetailsActivity.this, StudentQRScannerActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(StudentCourseDetailsActivity.this, MainActivity.class));
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

        title = findViewById(R.id.title);
        // Access course code from intent
        String courseCode = getIntent().getStringExtra("course");
        // Assign text of title to courseCode
        title.setText(courseCode);
        // Call fetchCourseDetails method to get course details and display them
        fetchCourseDetails(courseCode);
    }

    public void fetchCourseDetails(String courseCode) {

        // Find text views
        TextView courseCodeTextView = findViewById(R.id.course_code);
        TextView courseNameTextView = findViewById(R.id.course_name);
        TextView lecturerNameTextView = findViewById(R.id.lecturer_name);
        TextView lecturerEmailTextView = findViewById(R.id.lecturer_email);

        // Find course details in courses node
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseCode);
        // Get course details
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> sessionIDs = new ArrayList<>();
                List<String> studentIDs = new ArrayList<>();

                // get course info
                String courseCode = snapshot.child("courseCode").getValue(String.class);
                String courseName = snapshot.child("courseName").getValue(String.class);

                studentIDs = (List<String>) snapshot.child("students").getValue();
                String lecturerID = snapshot.child("lecturer").getValue(String.class);

                // Find lecturer name and email in users node
                DatabaseReference lecturerRef = FirebaseDatabase.getInstance().getReference("users").child(lecturerID);
                lecturerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lecturerName = snapshot.child("username").getValue(String.class);
                        String lecturerEmail = snapshot.child("email").getValue(String.class);
                        // Set text of text views
                        courseCodeTextView.setText(courseCode);
                        courseNameTextView.setText(courseName);
                        lecturerNameTextView.setText(lecturerName);
                        lecturerEmailTextView.setText(lecturerEmail);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Log database error
                        Log.e("Database Error", error.getMessage());
                    }
                });

                // Check if there are courseSessions node
                if (snapshot.child("courseSessions").getValue() != null) {
                    sessionIDs = (List<String>) snapshot.child("courseSessions").getValue();
                    Log.d("Session ID", "Session IDs: " + sessionIDs.toString());
                    fetchCourseSessionDetailsAndStudentAttendanceDetails(sessionIDs, studentIDs);
                } else {
                    TextView noPreviousSessionText = findViewById(R.id.no_previous_session_text);

                    noPreviousSessionText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log database error
                Log.e("Database Error", error.getMessage());
            }
        });
    }

    public void fetchCourseSessionDetailsAndStudentAttendanceDetails(List<String> sessionIDs, List<String> studentIDs) {
        // Make a key value pair array to store number of present for students for all sessions
        List<Map<String, Integer>> studentAttendanceData = new ArrayList<>();

        RecyclerView sessionRecyclerView = findViewById(R.id.student_previous_sessions_recycler_view);
        // Create a new LinearLayoutManager
        LinearLayoutManager previousSessionLayoutManager = new LinearLayoutManager(this);
        sessionRecyclerView.setLayoutManager(previousSessionLayoutManager);

        // Use a counter to track completion of database operations
        AtomicInteger completionCounter = new AtomicInteger(sessionIDs.size());

        Collections.sort(sessionIDs, Collections.reverseOrder());
        // Define sessionList as a 2D list of strings
        List<List<String>> sessionList = new ArrayList<>();
        final int[] totalPresent = {0};
        final int[] totalLate = {0};
        final int[] totalAbsent = {0};
        final int[] totalClassPresent = {0};
        final int[] totalClassStudent = {0};
        final int[] totalClassAbsent = {0};

        // For each session ID, inflate a new row and add it to the table
        for (String sessionID : sessionIDs) {
            DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("course_sessions").child(String.valueOf(sessionID));
            sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Create a new list to store session data
                    List<String> sessionData = new ArrayList<>();

                    // Add the session data list to the session list
                    CourseSession previousSession = new CourseSession();
                    String date = snapshot.child("date").getValue(String.class);
                    String formattedDate = convertDateFormat(date);
                    String startTime = snapshot.child("startTime").getValue(String.class);
                    String endTime = snapshot.child("endTime").getValue(String.class);

                    String color = "", attendance = "";

                    // Create a map to store student attendance data for the current session
                    Map<String, Integer> sessionAttendanceData = new HashMap<>();

                    for (DataSnapshot studentAttendanceStatus : snapshot.child("studentAttendanceStatus").getChildren()) {
                        String studentID = studentAttendanceStatus.getKey();
                        String attendanceStatus = studentAttendanceStatus.getValue(String.class);

                        if (studentID.equals(userId)) {
                            if (attendanceStatus.equals("Present")) {
                                attendance = "Present";
                                color = "light_green";
                                totalPresent[0]++;
                            } else if (attendanceStatus.equals("Late")) {
                                attendance = "Late";
                                color = "orange";
                                totalLate[0]++;
                            } else if (attendanceStatus.equals("Absent")) {
                                attendance = "Absent";
                                color = "light_red";
                                totalAbsent[0]++;
                            }
                        } else {
                            if (attendanceStatus.equals("Present") || attendanceStatus.equals("Late")) {
                                totalClassPresent[0]++; // Increment the total number of presents
                            } else {
                                totalClassAbsent[0]++; // Increment the total number of absents
                            }
                        }

                    }
                    // Add the sessionAttendanceData map to the studentAttendanceData list
                    studentAttendanceData.add(sessionAttendanceData);

                    // Add the percentage to the sessionAttendanceData map
                    previousSession.setDate(date);
                    previousSession.setStartTime(startTime);
                    previousSession.setEndTime(endTime);

                    // Populating an instance of a previous course session row
                    sessionData.add(formattedDate);
                    sessionData.add(startTime);
                    sessionData.add(endTime);
                    sessionData.add(attendance);
                    sessionData.add(color);

                    // Add the sessionData list to sessionList
                    sessionList.add(sessionData);

                    // Check if all sessions have been processed
                    if (sessionList.size() == sessionIDs.size()) {
                        // All sessions have been processed, now update the RecyclerView
                        StudentPreviousSessionDetailsRecyclerAdapter previousSessionDetailsRecyclerAdapter = new StudentPreviousSessionDetailsRecyclerAdapter(StudentCourseDetailsActivity.this, sessionList);
                        sessionRecyclerView.setAdapter(previousSessionDetailsRecyclerAdapter);
                    }

                    // Decrease the counter and check if all operations are completed
                    if (completionCounter.decrementAndGet() == 0) {
                        // All database operations completed, now update the PieChartView
                        // Find percentage of attendance
                        float percentagePresent = (float) totalPresent[0] / (totalPresent[0] + totalLate[0] + totalAbsent[0]) * 100;
                        float percentageLate = (float) totalLate[0] / (totalPresent[0] + totalLate[0] + totalAbsent[0]) * 100;
                        float percentageAbsent = (float) totalAbsent[0] / (totalPresent[0] + totalLate[0] + totalAbsent[0]) * 100;
                        float classAverage = (float) totalClassPresent[0] / (totalClassPresent[0] + totalClassAbsent[0]) * 100;
                        float recommendedPercentage = 95;

                        List<String> presentData = new ArrayList<>(), lateData = new ArrayList<>(), absentData = new ArrayList<>();

                        presentData.add(String.valueOf(percentagePresent));
                        presentData.add("light_green");

                        lateData.add(String.valueOf(percentageLate));
                        lateData.add("orange");

                        absentData.add(String.valueOf(percentageAbsent));
                        absentData.add("light_red");

                        studentAttendancePieChartData.add(presentData);
                        studentAttendancePieChartData.add(lateData);
                        studentAttendancePieChartData.add(absentData);

                        Log.d("Pie Chart Data", "Pie Chart Data: " + studentAttendancePieChartData.toString());

                        PieChartView pieChartView = findViewById(R.id.student_attendance_pie_chart);
                        pieChartView.setData(studentAttendancePieChartData);

                        TextView attendanceScoreTitle = findViewById(R.id.attendance_score_title);

                        if (percentagePresent >= recommendedPercentage) {
                            attendanceScoreTitle.setText("Good Attendance Score!");
                            attendanceScoreTitle.setTextColor(getResources().getColor(R.color.light_green));
                        } else if (percentagePresent >= classAverage && percentagePresent < recommendedPercentage) {
                            attendanceScoreTitle.setText("Attendance Score Above Average!");
                            attendanceScoreTitle.setTextColor(getResources().getColor(R.color.orange));
                        } else if (percentagePresent < classAverage) {
                            attendanceScoreTitle.setText("Attendance Score Below Average!");
                            attendanceScoreTitle.setTextColor(getResources().getColor(R.color.light_red));
                        } else if (percentagePresent == 0) {
                            attendanceScoreTitle.setText("No Attendance Score!");
                            attendanceScoreTitle.setTextColor(getResources().getColor(R.color.light_red));
                        }

                        HorizontalBarChartView horizontalBarChartView = findViewById(R.id.horizontal_bar_chart);
                        horizontalBarChartView.setData(recommendedPercentage, percentagePresent + percentageLate, classAverage);

                        // Log classAverage and totalPresents and totalStudents
                        Log.d("Total Presents", "Total Presents: " + totalClassPresent[0]);
                        Log.d("Total Students", "Total Absents: " + totalClassAbsent[0]);
                        Log.d("Class Average", "Class Average: " + classAverage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Log database error
                    Log.e("Database Error", error.getMessage());
                }
            });
        }
    }

    public String convertDateFormat(String originalDateStr) {
        try {
            // Create a SimpleDateFormat object for the original date format
            SimpleDateFormat originalDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);

            // Create a SimpleDateFormat object for the desired output format
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yy");

            // Parse the original date string
            Date originalDate = originalDateFormat.parse(originalDateStr);

            // Format the date in the desired output format
            return outputDateFormat.format(originalDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // Return the original date string if there's an error
            return originalDateStr;
        }
    }

}
