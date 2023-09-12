package com.example.attendancemanagementsystem;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import com.google.firebase.database.ValueEventListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentClassEnrolmentActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private DatabaseReference studentEnrollmentsRef;
    private DatabaseReference scheduleRef;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_course_enrolment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.menu_dashboard) {
                    startActivity(new Intent(StudentClassEnrolmentActivity.this, StudentDashboardActivity.class));
                    /*
                } else if (menuItem.getItemId() == R.id.menu_qr_scanner) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentQRScannerActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentEventsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentProfileActivity.class));
                     */
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    startActivity(new Intent(StudentClassEnrolmentActivity.this, MainActivity.class));
                }
                return true;
            }
        });

        drawerLayout = findViewById(R.id.nav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        TableLayout currentEnrolment = findViewById(R.id.current_enrolment_table);
        TableLayout availableEnrolment = findViewById(R.id.available_enrolment_table);

        if (userId.isEmpty()) {
            startActivity(new Intent(StudentClassEnrolmentActivity.this, MainActivity.class));
        } else {
            // Firebase Realtime Database reference
            studentEnrollmentsRef = FirebaseDatabase.getInstance().getReference("courses");
            scheduleRef = FirebaseDatabase.getInstance().getReference("course_schedules");
            userRef = FirebaseDatabase.getInstance().getReference("users");
            studentEnrollmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                        List<String> students = courseSnapshot.child("students").getValue(new GenericTypeIndicator<List<String>>() {});

                        String courseCode = courseSnapshot.child("courseCode").getValue(String.class);
                        String courseName = courseSnapshot.child("courseName").getValue(String.class);

                        TableRow row = createTableRow(courseCode, courseName, students, userId, currentEnrolment, availableEnrolment);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("RegisterActivity", "Database Error: " + databaseError.getMessage());
                }
            });
        }
    }

    private TableRow createTableRow(String courseCode, String courseName, List<String> students, String userId, TableLayout currentEnrolment, TableLayout availableEnrolment) {
        TableRow row = new TableRow(StudentClassEnrolmentActivity.this);

        TextView codeTextView = createTextView(courseCode, 0.5f);
        TextView nameTextView = createTextView(courseName, 1.5f);

        Button actionButton;
        if (students != null && students.contains(userId)) {
            actionButton = createButton("Remove");
        } else {
            actionButton = createButton("Enrol");
        }

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionButton.getText().equals("Remove")) {
                    // Remove the student from the course's students list
                    removeFromCourse(courseCode, userId);
                    // Update the UI
                    currentEnrolment.removeView(row);
                    availableEnrolment.addView(row);
                    actionButton.setText("Enrol");
                } else {
                    // Validate the class schedules before enrollment
                    validateClassSchedules(courseCode, userId, new ScheduleValidationCallback() {
                        @Override
                        public void onValidationComplete(boolean hasConflict) {
                            Log.d(TAG, hasConflict ? "Schedule conflict detected." : "No schedule conflict detected.");

                            if (!hasConflict) {
                                // Add the student to the course's students list
                                addToCourse(courseCode, userId);
                                // Update the UI
                                availableEnrolment.removeView(row);
                                currentEnrolment.addView(row);
                                actionButton.setText("Remove");
                                Log.d(TAG, "Enrollment successful.");
                            } else {
                                // Handle the case when there are conflicts
                                // Display a message or take appropriate action
                                Toast.makeText(StudentClassEnrolmentActivity.this, "Enrollment not allowed due to schedule conflicts.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        row.addView(codeTextView);
        row.addView(nameTextView);
        row.addView(actionButton);

        if (students != null && students.contains(userId)) {
            currentEnrolment.addView(row);
        } else {
            availableEnrolment.addView(row);
        }

        return row;
    }

    private void validateClassSchedules(String courseCode, String studentId, ScheduleValidationCallback callback) {
        DatabaseReference newCourseRef = studentEnrollmentsRef.child(courseCode);
        DatabaseReference studentCoursesRef = userRef.child(studentId).child("courses");

        List<String> studentCourses = new ArrayList<>();
        List<Integer> newCourseSchedules = new ArrayList<>();
        List<Integer> existingCourseSchedules = new ArrayList<>();
        List<CourseSchedule> existingScheduleInfo = new ArrayList<>();
        List<CourseSchedule> newScheduleInfo = new ArrayList<>();

        // Use AtomicBoolean to hold the hasConflict value
        AtomicBoolean hasConflict = new AtomicBoolean(false);

        // Fetch student courses
        fetchStudentCourses(studentCoursesRef, studentCourses, () -> {
            // After fetching student courses, fetch existing course schedules
            fetchExistingCourseSchedules(studentCourses, existingCourseSchedules, () -> {
                // After fetching existing course schedules, fetch new course schedules
                fetchNewCourseSchedules(newCourseRef, newCourseSchedules, () -> {
                    // After fetching all schedules, compare them
                    getScheduleInfo(existingCourseSchedules, (existingInfo) -> {
                        existingScheduleInfo.addAll(existingInfo); // Add existing schedule info to the list
                        Log.d(TAG, "Existing Schedule Info: " + existingScheduleInfo);

                        // After fetching existing schedules, fetch new schedules
                        getScheduleInfo(newCourseSchedules, (newInfo) -> {
                            newScheduleInfo.addAll(newInfo); // Add new schedule info to the list
                            Log.d(TAG, "New Schedule Info: " + newScheduleInfo);

                            // Iterate through existingScheduleInfo and newScheduleInfo to compare the schedule info
                            for (CourseSchedule existingSchedule : existingScheduleInfo) {
                                for (CourseSchedule newSchedule : newScheduleInfo) {
                                    if (existingSchedule.getDay().equals(newSchedule.getDay())) {
                                        Log.d(TAG, existingSchedule.getCourseCode() + newSchedule.getCourseCode()+ "Comparing existing day: " + existingSchedule.getDay() + " with new" + newSchedule.getDay());
                                        // Check for time conflicts
                                        // Log time for existing and new schedule
                                        Log.d(TAG, existingSchedule.getCourseCode() + newSchedule.getCourseCode()+ "Comparing existing start time: " + existingSchedule.getStartTime() + " with new" + newSchedule.getStartTime());
                                        // Log time for existing and new schedule
                                        Log.d(TAG, existingSchedule.getCourseCode() + newSchedule.getCourseCode()+ "Comparing existing end time: " + existingSchedule.getEndTime() + " with new" + newSchedule.getEndTime());
                                        if (isTimeConflict(existingSchedule.getStartTime(), existingSchedule.getEndTime(), newSchedule.getStartTime(), newSchedule.getEndTime())) {
                                            // Set the hasConflict variable to true if there is a conflict
                                            hasConflict.set(true);
                                            break;
                                        }
                                    }
                                }
                                if (hasConflict.get()) {
                                    break;
                                }
                            }
                            // Call the callback with the boolean indicating whether there are conflicts
                            callback.onValidationComplete(hasConflict.get());
                        });
                    });
                });
            });
        });
    }

    // Define a callback interface
    interface ScheduleValidationCallback {
        void onValidationComplete(boolean hasConflict);
    }

    // Function to fetch student courses
    private void fetchStudentCourses(DatabaseReference studentCoursesRef, List<String> studentCourses, Runnable onComplete) {
        studentCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String existingCourseId = courseSnapshot.getValue(String.class);
                    studentCourses.add(existingCourseId);
                }
                Log.d(TAG, "Student Courses: " + studentCourses);
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors here
            }
        });
    }

    // Function to fetch existing course schedules for student courses
    private void fetchExistingCourseSchedules(List<String> studentCourses, List<Integer> existingCourseSchedules, Runnable onComplete) {
        // Initialize a counter to keep track of completed fetches
        AtomicInteger fetchCounter = new AtomicInteger(0);

        for (String course : studentCourses) {
            DatabaseReference courseRef = studentEnrollmentsRef.child(course);
            courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot scheduleSnapshot : snapshot.child("courseSchedules").getChildren()) {
                        Integer schedule = scheduleSnapshot.getValue(Integer.class);
                        existingCourseSchedules.add(schedule);
                    }
                    Log.d(TAG, "Existing Course Schedules: " + existingCourseSchedules);
                    // Increment the fetch counter
                    int counter = fetchCounter.incrementAndGet();
                    // Check if all fetches are completed
                    if (counter == studentCourses.size()) {
                        onComplete.run();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle any errors here
                }
            });
        }
    }

    // Function to fetch new course schedules
    private void fetchNewCourseSchedules(DatabaseReference newCourseRef, List<Integer> newCourseSchedules, Runnable onComplete) {
        newCourseRef.child("courseSchedules").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                    Integer schedule = scheduleSnapshot.getValue(Integer.class);
                    newCourseSchedules.add(schedule);
                }
                Log.d(TAG, "New Course Schedules: " + newCourseSchedules);
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors here
            }
        });
    }

    private void getScheduleInfo(List<Integer> scheduleIds, ScheduleInfoCallback callback) {
        List<CourseSchedule> scheduleInfoList = new ArrayList<>();
        AtomicInteger fetchCounter = new AtomicInteger(0);

        for (Integer scheduleId : scheduleIds) {
            DatabaseReference courseScheduleRef = scheduleRef.child(scheduleId.toString());
            courseScheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String courseCode = snapshot.child("courseCode").getValue(String.class);
                    String day = snapshot.child("day").getValue(String.class);
                    String startTime = snapshot.child("startTime").getValue(String.class);
                    String endTime = snapshot.child("endTime").getValue(String.class);

                    // Store schedule info into scheduleInfoList
                    CourseSchedule scheduleInfo = new CourseSchedule();
                    scheduleInfo.setCourseCode(courseCode);
                    scheduleInfo.setDay(day);
                    scheduleInfo.setStartTime(startTime);
                    scheduleInfo.setEndTime(endTime);
                    scheduleInfoList.add(scheduleInfo);

                    // Increment the fetch counter
                    int counter = fetchCounter.incrementAndGet();

                    // Log the fetched schedule info
                    Log.d(TAG, "Fetched Schedule Info: " + scheduleInfo);

                    // Check if all fetches are completed
                    if (counter == scheduleIds.size()) {
                        // Call the callback with the complete scheduleInfoList
                        callback.onScheduleInfoFetched(scheduleInfoList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle any errors here
                    int counter = fetchCounter.incrementAndGet();
                    if (counter == scheduleIds.size()) {
                        // Call the callback with the incomplete scheduleInfoList
                        callback.onScheduleInfoFetched(scheduleInfoList);
                    }
                }
            });
        }
    }

    public interface ScheduleInfoCallback {
        void onScheduleInfoFetched(List<CourseSchedule> scheduleInfo);
    }

    private boolean isTimeConflict(String startTime1, String endTime1, String startTime2, String endTime2) {
        // Parse the time strings to compare as integers (e.g., "8:00 a.m." -> 800)
        int start1 = parseTime(startTime1);
        int end1 = parseTime(endTime1);
        int start2 = parseTime(startTime2);
        int end2 = parseTime(endTime2);

        // Check for time conflicts
        return (start1 < end2 && end1 > start2);
    }

    private int parseTime(String timeString) {
        // Convert time strings like "8:00 a.m." to integers (e.g., 800)
        String[] parts = timeString.split(":|\\s|\\.");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        // Check if it's PM and not 12:00 PM
        if (parts[2].equalsIgnoreCase("p.m.") && hours != 12) {
            hours += 12;
        }

        // Handle 12:00 AM (midnight) as 00:00 in 24-hour format
        if (hours == 12 && parts[2].equalsIgnoreCase("a.m.")) {
            hours = 0;
        }

        return hours * 100 + minutes;
    }

    private void removeFromCourse(String courseCode, String studentId) {
        // Get a reference to the specific course
        DatabaseReference courseRef = studentEnrollmentsRef.child(courseCode);
        DatabaseReference studentRef = userRef.child(studentId);

        // Get the current list of students in the course
        courseRef.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> students = new ArrayList<>();

                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String existingStudentId = studentSnapshot.getValue(String.class);

                    // Exclude the student to be removed from the list
                    if (!existingStudentId.equals(studentId)) {
                        students.add(existingStudentId);
                    }
                }

                // Set the updated list of students back to the course
                courseRef.child("students").setValue(students);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RegisterActivity", "Database Error: " + databaseError.getMessage());
            }
        });

        studentRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> courses = new ArrayList<>();

                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String existingCourseId = courseSnapshot.getValue(String.class);

                    if (!existingCourseId.equals(courseCode)) {
                        courses.add(existingCourseId);
                    }
                }

                studentRef.child("courses").setValue(courses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RegisterActivity", "Database Error: " + databaseError.getMessage());
            }
        });

        Toast.makeText(StudentClassEnrolmentActivity.this, "Successfully dropped " + courseCode + ".", Toast.LENGTH_SHORT).show();
    }

    private void addToCourse(String courseCode, String studentId) {
        // Get a reference to the specific course
        DatabaseReference courseRef = studentEnrollmentsRef.child(courseCode);
        DatabaseReference studentRef = userRef.child(studentId);

        // Get the current list of students in the course
        List<String> students = new ArrayList<>();
        courseRef.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String existingStudentId = studentSnapshot.getValue(String.class);
                    students.add(existingStudentId);
                }
                // Add the new student to the list
                students.add(studentId);

                // Set the updated list of students back to the course
                courseRef.child("students").setValue(students);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors here
            }
        });

        studentRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> courses = new ArrayList<>();

                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String existingCourseId = courseSnapshot.getValue(String.class);
                    courses.add(existingCourseId);
                }

                courses.add(courseCode);

                Log.d("Enrolment", "Success");
                studentRef.child("courses").setValue(courses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RegisterActivity", "Database Error: " + databaseError.getMessage());
            }
        });

        Toast.makeText(StudentClassEnrolmentActivity.this, "Successfully enrolled in " + courseCode + ".", Toast.LENGTH_SHORT).show();
    }

    private TextView createTextView(String text, float weight) {
        TextView textView = new TextView(this);
        textView.setText(text);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        textView.setLayoutParams(layoutParams);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.font_family));
        textView.setPadding(20, 8, 8, 8);
        return textView;
    }

    private Button createButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        button.setLayoutParams(layoutParams);
        button.setTypeface(ResourcesCompat.getFont(this, R.font.font_family));
        button.setPadding(8, 8, 8, 8);
        return button;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


