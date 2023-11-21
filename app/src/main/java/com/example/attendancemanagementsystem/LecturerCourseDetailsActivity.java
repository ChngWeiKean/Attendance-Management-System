package com.example.attendancemanagementsystem;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class LecturerCourseDetailsActivity extends AppCompatActivity {
    TextView title;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    List<List<String>> studentAttendancePieChartData = new ArrayList<>();
    List<Float> attendanceData = new ArrayList<>();
    List<String> sessionDates = new ArrayList<>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_course_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        nestedScrollView.setNestedScrollingEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigation_view);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.menu_dashboard);
        menuItem.setChecked(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.menu_dashboard) {
                    startActivity(new Intent(LecturerCourseDetailsActivity.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerCourseDetailsActivity.this, LecturerEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(LecturerCourseDetailsActivity.this, LecturerProfileSettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerCourseDetailsActivity.this, MainActivity.class));
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
        // Call fetchCourseDetails method to get course details and display them
        fetchCourseDetails(courseCode);
    }

    public void fetchCourseDetails(String courseCode) {

        // Find text views
        TextView courseCodeTextView = findViewById(R.id.course_code);
        TextView courseNameTextView = findViewById(R.id.course_name);
        TextView lecturerNameTextView = findViewById(R.id.lecturer_name);
        TextView lecturerEmailTextView = findViewById(R.id.lecturer_email);

        Button createCourseSessionButton = findViewById(R.id.create_session_button);

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
                Boolean hasOngoingSession = snapshot.child("hasOngoingSession").getValue(Boolean.class);
                // If course has ongoing session, set button to redirect to ongoing session
                if (hasOngoingSession) {
                    createCourseSessionButton.setText("Ongoing Session");
                    createCourseSessionButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple));
                    createCourseSessionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            redirectToOngoingSession();
                        }
                    });
                }

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
                    TextView noPreviousStudentAttendanceText = findViewById(R.id.no_previous_student_attendance_text);

                    noPreviousSessionText.setVisibility(View.VISIBLE);
                    noPreviousStudentAttendanceText.setVisibility(View.VISIBLE);
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
        attendanceData = new ArrayList<>();
        sessionDates = new ArrayList<>();

        RecyclerView sessionRecyclerView = findViewById(R.id.sessions_table_rows_recycler_view);
        // Create a new LinearLayoutManager
        LinearLayoutManager previousSessionLayoutManager = new LinearLayoutManager(this);
        sessionRecyclerView.setLayoutManager(previousSessionLayoutManager);

        RecyclerView attendanceSummaryRecyclerView = findViewById(R.id.attendance_summary_table_rows_recycler_view);
        // Create a new LinearLayoutManager
        LinearLayoutManager attendanceSummaryLayoutManager = new LinearLayoutManager(this);
        attendanceSummaryRecyclerView.setLayoutManager(attendanceSummaryLayoutManager);

        // Use a counter to track completion of database operations
        AtomicInteger completionCounter = new AtomicInteger(sessionIDs.size());

        Log.d("Session ID", "Session IDs: " + sessionIDs.toString());
        Collections.reverse(sessionIDs);
        Log.d("Session ID", "Reversed Session IDs: " + sessionIDs.toString());
        // Define sessionList as a 2D list of strings
        List<List<String>> sessionList = new ArrayList<>();
        List<List<String>> attendanceSummaryList = new ArrayList<>();
        final int[] totalPresent = {0};
        final int[] totalLate = {0};
        final int[] totalAbsent = {0};

        String courseCode = getIntent().getStringExtra("course");

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
                    // Find the number of present in studentAttendanceStatus
                    int present = 0;
                    String color = "";

                    // Create a map to store student attendance data for the current session
                    Map<String, Integer> sessionAttendanceData = new HashMap<>();

                    for (DataSnapshot studentAttendanceStatus : snapshot.child("studentAttendanceStatus").getChildren()) {
                        String studentID = studentAttendanceStatus.getKey();
                        String attendanceStatus = studentAttendanceStatus.getValue(String.class);

                        // Increment the attendance count for the current student in the sessionAttendanceData map
                        if (attendanceStatus.equals("Present") || attendanceStatus.equals("Late")) {
                            int presentCount = sessionAttendanceData.getOrDefault(studentID, 0);
                            sessionAttendanceData.put(studentID, presentCount + 1);
                            present++;
                        } else {
                            sessionAttendanceData.put(studentID, 0);
                        }

                        if (attendanceStatus.equals("Present")) {
                            totalPresent[0]++;
                        } else if (attendanceStatus.equals("Late")) {
                            totalLate[0]++;
                        } else if (attendanceStatus.equals("Absent")) {
                            totalAbsent[0]++;
                        }
                    }
                    // Add the sessionAttendanceData map to the studentAttendanceData list
                    studentAttendanceData.add(sessionAttendanceData);

                    long studentCount = snapshot.child("studentAttendanceStatus").getChildrenCount();

                    // Get percentage of present and if percentage is 80% above then set color to light_green, else if percentage is above 50%, set color to orange, else, set color to red
                    if ((float) present / studentCount >= 0.8) {
                        color = "light_green";
                    } else if ((float) present / studentCount >= 0.5) {
                        color = "orange";
                    } else {
                        color = "light_red";
                    }

                    Log.d("Present", "Present: " + present);
                    Log.d("Session Size", "Student Size: " + studentCount);
                    Log.d("Percentage", "Percentage: " + present / studentCount);
                    Log.d("Color", "Color: " + color);

                    float percentage = ((float) present / studentCount * 100);
                    String attendance = present + " / " + studentCount;
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

                    // Find the "Export to CSV" button
                    Button exportSessionsToCSVButton = findViewById(R.id.export_sessions_button);

                    if (sessionList.size() == sessionIDs.size()) {
                        // Session data is available, set up the adapter and enable the button
                        PreviousSessionDetailsRecyclerAdapter adapter = new PreviousSessionDetailsRecyclerAdapter(LecturerCourseDetailsActivity.this, sessionList, new PreviousSessionDetailsRecyclerAdapter.OnSessionDetailsButtonClickListener() {
                            @Override
                            public void onSessionDetailsButtonClicked(int position) {
                                redirectToCourseSessionDetails(sessionIDs.get(position));
                            }
                        });

                        sessionRecyclerView.setAdapter(adapter);

                        exportSessionsToCSVButton.setEnabled(true); // Enable the button
                        exportSessionsToCSVButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("Session List", "Export session to CSV");
                                exportSessionsToCSV(sessionList, courseCode);
                            }
                        });
                    }

                    // Add percentage and date to attendanceData and sessionDates
                    if (attendanceData.size() < 8) {
                        attendanceData.add(percentage);
                        sessionDates.add(formattedDate);
                    }

                    // Decrease the counter and check if all operations are completed
                    if (completionCounter.decrementAndGet() == 0) {
                        // All database operations completed, now update the BarChartView
                        BarChartView barChartView = findViewById(R.id.sessions_statistics_bar_chart);
                        barChartView.setData(attendanceData, sessionDates);

                        float percentagePresent = (float) totalPresent[0] / (totalPresent[0] + totalLate[0] + totalAbsent[0]) * 100;
                        float percentageLate = (float) totalLate[0] / (totalPresent[0] + totalLate[0] + totalAbsent[0]) * 100;
                        float percentageAbsent = (float) totalAbsent[0] / (totalPresent[0] + totalLate[0] + totalAbsent[0]) * 100;

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
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Log database error
                    Log.e("Database Error", error.getMessage());
                }
            });
        }

        // For each student ID, inflate a new row and add it to the table
        for (String studentID : studentIDs) {
            DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("users").child(studentID);
            studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> attendanceSummaryData = new ArrayList<>();

                    String studentID = snapshot.child("id").getValue(String.class);
                    String studentName = snapshot.child("username").getValue(String.class);
                    // Find student id and get value from studentAttendanceData key value array
                    int presentCount = 0;
                    String color = "";

                    // Iterate through the studentAttendanceData list
                    for (Map<String, Integer> sessionAttendanceData : studentAttendanceData) {
                        // Check if the studentID exists as a key in the sessionAttendanceData map
                        if (sessionAttendanceData.containsKey(studentID)) {
                            presentCount += sessionAttendanceData.get(studentID);
                        }
                    }

                    // Get percentage of present and if percentage is 80% above then set color to light_green, else if percentage is above 50%, set color to orange, else, set color to red
                    if ((float) presentCount / sessionIDs.size() >= 0.8) {
                        color = "light_green";
                    } else if ((float) presentCount / sessionIDs.size() >= 0.5) {
                        color = "orange";
                    } else {
                        color = "light_red";
                    }

                    attendanceSummaryData.add(studentName);
                    attendanceSummaryData.add(studentID);
                    attendanceSummaryData.add(presentCount + " / " + sessionIDs.size());
                    attendanceSummaryData.add(color);

                    attendanceSummaryList.add(attendanceSummaryData);

                    // Find the "Export to CSV" button
                    Button exportAttendanceSummaryToCSVButton = findViewById(R.id.export_attendance_summary_button);

                    if (attendanceSummaryList.size() == studentIDs.size()) {
                        // Data is available, set up the adapter and enable the button
                        StudentAttendanceSummaryRecyclerAdapter adapter = new StudentAttendanceSummaryRecyclerAdapter(LecturerCourseDetailsActivity.this, attendanceSummaryList);
                        attendanceSummaryRecyclerView.setAdapter(adapter);

                        exportAttendanceSummaryToCSVButton.setEnabled(true); // Enable the button
                        exportAttendanceSummaryToCSVButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("Session List", "Export attendance summary to CSV");
                                exportAttendanceSummaryToCSV(attendanceSummaryList, courseCode);
                            }
                        });
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

    // Function to create a new course session
    public void createNewCourseSession(View view) {
        CourseSession newCourseSession = new CourseSession();
        String courseCode = getIntent().getStringExtra("course");

        newCourseSession.setCourseCode(courseCode);
        Log.d("Course Code", courseCode);

        // Get current date in the desired format (you need to implement this)
        String currentDate = getCurrentFormattedDate();
        newCourseSession.setDate(currentDate);
        Log.d("Date", currentDate);

        // Get current time in the desired format (you need to implement this)
        String currentTime = getCurrentFormattedTime();
        newCourseSession.setStartTime(currentTime);
        Log.d("Start Time", currentTime);

        // Get all course student IDs with a callback
        getAllCourseStudentIDs(courseCode, new StudentIDsCallback() {
            @Override
            public void onStudentIDsReceived(List<String> studentIDs) {
                // Now you have the student IDs, you can work with them here
                if (studentIDs != null) {
                    Log.d("Student IDs", studentIDs.toString());

                    // Create a new map of student IDs and attendance status
                    Map<String, String> attendanceStatusMap = new HashMap<>();

                    // Set all attendance status to "absent"
                    for (String studentID : studentIDs) {
                        attendanceStatusMap.put(studentID, "Absent");
                    }

                    Log.d("Attendance Status Map", attendanceStatusMap.toString());

                    // Set the attendanceStatusMap in your CourseSession object
                    newCourseSession.setStudentAttendanceStatus(attendanceStatusMap);

                    // Generate the session ID asynchronously
                    generateSessionID(courseCode, new SessionIDCallback() {
                        @Override
                        public void onSessionIDGenerated(String sessionID) {
                            if (sessionID != null) {
                                Log.d("Session ID", sessionID);

                                // Add the newCourseSession into the database
                                DatabaseReference courseSessionRef = FirebaseDatabase.getInstance().getReference("course_sessions");
                                courseSessionRef.child(sessionID).setValue(newCourseSession);
                                courseSessionRef.child(sessionID).child("sessionID").setValue(Integer.parseInt(sessionID));

                                // Add the new session ID to the course's list of sessions
                                DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseCode);
                                courseRef.child("courseSessions").get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        List<String> courseSessions = (List<String>) task.getResult().getValue();
                                        // Add the new session ID to the list
                                        if (courseSessions == null) {
                                            courseSessions = new ArrayList<>();
                                        }
                                        courseSessions.add(sessionID);
                                        // Set the updated list in the database
                                        courseRef.child("courseSessions").setValue(courseSessions);
                                        courseRef.child("hasOngoingSession").setValue(true);

                                        // Start the activity here
                                        Intent intent = new Intent(LecturerCourseDetailsActivity.this, LecturerCreateSessionActivity.class);
                                        intent.putExtra("course", courseCode);
                                        intent.putExtra("ongoingSession", false);
                                        intent.putExtra("newSession", true);
                                        startActivity(intent);
                                    } else {
                                        Log.d("Course Sessions", "Error getting course sessions");
                                    }
                                });
                            } else {
                                Log.e("Session ID", "Failed to generate session ID");
                            }
                        }
                    });
                }
            }
        });
    }

    // Function to get the current date in the format of "dd MMMM yyyy" e.g. 01 January 2020
    private String getCurrentFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        return dateFormat.format(new Date());
    }

    // Function to get the current time in the format of "hh:mm a" e.g. 12:00 PM
    private String getCurrentFormattedTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur")); // Set the timezone to Kuala Lumpur
        return timeFormat.format(new Date());
    }

    // Function to generate a new session ID
    private void generateSessionID(String courseCode, SessionIDCallback callback) {
        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("course_sessions");

        // Use a Firebase query to find the maximum session number for the given course code
        sessionsRef.orderByChild("sessionID").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int maxSessionNumber = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CourseSession session = snapshot.getValue(CourseSession.class);
                    if (session != null) {
                        // Extract the numeric part of the sessionID and convert it to an integer
                        int sessionNumber = session.getSessionID();
                        Log.d("Session Number", String.valueOf(sessionNumber));
                        if (sessionNumber > maxSessionNumber) {
                            maxSessionNumber = sessionNumber;
                            Log.d("Max Session Number", String.valueOf(maxSessionNumber));
                        }
                    }
                }

                // Increment the maxSessionNumber if it's not 0, otherwise, set it to 1
                maxSessionNumber = (maxSessionNumber == 0) ? 1 : maxSessionNumber + 1;

                // Generate the session ID
                String sessionID = String.valueOf(maxSessionNumber);

                // Call the callback with the generated session ID
                callback.onSessionIDGenerated(sessionID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database read errors
            }
        });
    }

    public interface SessionIDCallback {
        void onSessionIDGenerated(String sessionID);
    }

    // Function to get all student IDs for a given course code
    private void getAllCourseStudentIDs(String courseCode, StudentIDsCallback callback) {
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseCode);

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> studentIDs = new ArrayList<>();
                Course course = dataSnapshot.getValue(Course.class);

                if (course != null) {
                    studentIDs.addAll(course.getStudents());
                }

                // Invoke the callback with the student IDs
                callback.onStudentIDsReceived(studentIDs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database read errors
                Log.e("Database Error", databaseError.getMessage());
            }
        });
    }

    public interface StudentIDsCallback {
        void onStudentIDsReceived(List<String> studentIDs);
    }

    public void redirectToOngoingSession() {
        String courseCode = getIntent().getStringExtra("course");
        Intent intent = new Intent(this, LecturerCreateSessionActivity.class);
        intent.putExtra("course", courseCode);
        intent.putExtra("ongoingSession", true);
        intent.putExtra("newSession", false);
        startActivity(intent);
    }

    public void redirectToCourseSessionDetails(String sessionID) {
        String courseCode = getIntent().getStringExtra("course");
        Intent intent = new Intent(this, LecturerSessionDetailsActivity.class);
        intent.putExtra("course", courseCode);
        intent.putExtra("sessionID", sessionID);
        startActivity(intent);
    }

    // Function to export session summary data to a CSV file and allow for download
    public void exportSessionsToCSV(List<List<String>> sessionList, String courseCode) {
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
            File file = new File(dir, courseCode + "_session_data.csv");
            FileWriter writer = new FileWriter(file);

            // Define column names (headers)
            String[] headers = {"Date", "Start Time", "End Time", "Total Attendance Count"};

            // Write the header row
            for (String header : headers) {
                writer.append(header);
                writer.append(",");
            }
            writer.append("\n");

            // Write the data rows
            for (List<String> row : sessionList) {
                writer.append(formatDateForExcel(row.get(0))); // Date
                writer.append(",");
                writer.append(row.get(1)); // Start Time
                writer.append(",");
                writer.append(row.get(2)); // End Time
                writer.append(",");
                writer.append(formatAttendanceCount(row.get(3))); // Total Attendance Count
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

    // Function to export attendance summary data to a CSV file and allow for download
    public void exportAttendanceSummaryToCSV(List<List<String>> attendanceSummaryList, String courseCode) {
        try {
            // Get the external storage directory
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            Log.d(TAG, "Directory: " + dir.getAbsolutePath());

            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    Log.d(TAG, "Directory created successfully");
                } else {
                    Log.e(TAG, "Failed to create directory");
                }
            }

            // Create a CSV file for sessions
            File file = new File(dir, courseCode + "_student_attendance_summary_data.csv");
            FileWriter writer = new FileWriter(file);

            // Define column names (headers)
            String[] headers = {"Student Name", "Student ID", "Total Attendance Count"};

            // Write the header row
            for (String header : headers) {
                writer.append(header);
                writer.append(",");
            }
            writer.append("\n");

            // Write the data rows
            for (List<String> row : attendanceSummaryList) {
                writer.append(row.get(0)); // Student Name
                writer.append(",");
                writer.append(row.get(1)); // Student ID
                writer.append(",");
                writer.append(formatAttendanceCountForSessions(row.get(2))); // Total Attendance Count
                writer.append("\n");
            }

            // Close the writer
            writer.flush();
            writer.close();

            // Display a success message
            Toast.makeText(this, "Attendance summary exported successfully!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "File path: " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            // Handle errors, such as permission denied or file I/O issues
            // Log error
            Log.e("File Error", e.getMessage());
        }
    }

    private String formatDateForExcel(String inputDate) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = outputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate; // Use the original input if parsing fails
        }
    }

    // Helper function to format the attendance count
    private String formatAttendanceCount(String attendanceCount) {
        // Assuming attendanceCount is in the format "X / Y"
        String[] parts = attendanceCount.split(" / ");
        if (parts.length == 2) {
            return parts[0] + " of " + parts[1] + " students";
        } else {
            return attendanceCount; // Return the original input if the format is not as expected
        }
    }

    // Helper function to format the attendance count for all sessions
    private String formatAttendanceCountForSessions(String attendanceCount) {
        // Assuming attendanceCount is in the format "X / Y"
        String[] parts = attendanceCount.split(" / ");
        if (parts.length == 2) {
            return parts[0] + " of " + parts[1] + " sessions";
        } else {
            return attendanceCount; // Return the original input if the format is not as expected
        }
    }
}
