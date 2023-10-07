package com.example.attendancemanagementsystem;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LecturerCreateCourseActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    List<CourseSchedule> newScheduleList = new ArrayList<>();
    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_create_course);

        // Set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        // Get current lecturer ID
        String userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null);
        Log.d(TAG, "Current user id is: " + userId);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.menu_dashboard) {
                    startActivity(new Intent(LecturerCreateCourseActivity.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerCreateCourseActivity.this, LecturerEventActivity.class));
                    /*
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentProfileActivity.class));
                     */
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerCreateCourseActivity.this, MainActivity.class));
                }
                return true;
            }
        });

        drawerLayout = findViewById(R.id.nav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Find views
        TextInputLayout dayInputLayout = findViewById(R.id.dayInputLayout);
        Spinner daySpinner = findViewById(R.id.daySpinner);
        TextInputLayout startTimeInputLayout = findViewById(R.id.startTimeInputLayout);
        Spinner startTimeSpinner = findViewById(R.id.startTimeSpinner);
        TextInputLayout endTimeInputLayout = findViewById(R.id.endTimeInputLayout);
        Spinner endTimeSpinner = findViewById(R.id.endTimeSpinner);

        // Define the array of days
        String[] daysArray = getResources().getStringArray(R.array.days_array);
        String[] timeArray = getResources().getStringArray(R.array.course_time_array);
        // Create an ArrayAdapter using the days array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_layout, daysArray);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, timeArray);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        daySpinner.setAdapter(adapter);
        startTimeSpinner.setAdapter(timeAdapter);
        endTimeSpinner.setAdapter(timeAdapter);

        Button deleteBtn = findViewById(R.id.delete_button);
        deleteBtn.setVisibility(View.GONE);
    }

    public void addScheduleRow(View view) {
        LinearLayout parentLayout = findViewById(R.id.course_schedule_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate scheduleRow with the parentLayout as its root
        View scheduleRow = inflater.inflate(R.layout.add_new_schedule_row, parentLayout, false);

        // add text input layout and spinner array to scheduleRow
        TextInputLayout dayInputLayout = scheduleRow.findViewById(R.id.dayInputLayout);
        Spinner daySpinner = scheduleRow.findViewById(R.id.daySpinner);
        TextInputLayout startTimeInputLayout = scheduleRow.findViewById(R.id.startTimeInputLayout);
        Spinner startTimeSpinner = scheduleRow.findViewById(R.id.startTimeSpinner);
        TextInputLayout endTimeInputLayout = scheduleRow.findViewById(R.id.endTimeInputLayout);
        Spinner endTimeSpinner = scheduleRow.findViewById(R.id.endTimeSpinner);

        // Define the array of days
        String[] daysArray = getResources().getStringArray(R.array.days_array);
        String[] timeArray = getResources().getStringArray(R.array.course_time_array);

        // Create an ArrayAdapter using the days array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_layout, daysArray);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, timeArray);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        daySpinner.setAdapter(adapter);
        startTimeSpinner.setAdapter(timeAdapter);
        endTimeSpinner.setAdapter(timeAdapter);

        parentLayout.addView(scheduleRow);

        Button deleteBtn = findViewById(R.id.delete_button);
        deleteBtn.setVisibility(View.VISIBLE);
        count++;
        Log.d(TAG, "Row added. Count is: " + count);
    }

    public void deleteScheduleRow(View view) {
        LinearLayout parentLayout = findViewById(R.id.course_schedule_layout);
        parentLayout.removeViewAt(parentLayout.getChildCount() - 1);
        count--;

        if (count == 1) {
            Button deleteBtn = findViewById(R.id.delete_button);
            deleteBtn.setVisibility(View.GONE);
        }
    }

    public void createNewCourseSchedule(View view) {
        // Clear the list to prevent duplicate data
        newScheduleList.clear();
        Log.d(TAG, "New schedule list is cleared: " + newScheduleList.size());

        // Find views
        EditText courseCode = findViewById(R.id.course_code);
        EditText courseName = findViewById(R.id.course_name);
        LinearLayout parentLayout = findViewById(R.id.course_schedule_layout);

        // Calculate the next schedule ID based on the existing data
        DatabaseReference courseSchedulesRef = FirebaseDatabase.getInstance().getReference("course_schedules");
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        // Define the list to store the new schedule IDs
        List<Integer> newScheduleIds = new ArrayList<>();

        courseSchedulesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long maxId = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long id = Long.parseLong(snapshot.getKey());
                    if (id > maxId) {
                        maxId = id;
                    }
                }

                // Iterate through the parentLayout to collect data from the generated rows
                for (int i = 0; i < parentLayout.getChildCount(); i++) {
                    View scheduleRow = parentLayout.getChildAt(i);

                    // Retrieve views and data from the scheduleRow
                    Spinner daySpinner = scheduleRow.findViewById(R.id.daySpinner);
                    Spinner startTimeSpinner = scheduleRow.findViewById(R.id.startTimeSpinner);
                    Spinner endTimeSpinner = scheduleRow.findViewById(R.id.endTimeSpinner);

                    String selectedDay = daySpinner.getSelectedItem().toString();
                    String startTime = startTimeSpinner.getSelectedItem().toString();
                    String endTime = endTimeSpinner.getSelectedItem().toString();

                    // Create a new CourseSchedule object
                    CourseSchedule scheduleEntry = new CourseSchedule();
                    scheduleEntry.setCourseCode(courseCode.getText().toString());
                    scheduleEntry.setDay(selectedDay);
                    scheduleEntry.setStartTime(startTime);
                    scheduleEntry.setEndTime(endTime);

                    // Assign the next schedule ID and increment it
                    long nextId = maxId + 1;

                    // Add the entry to the list
                    newScheduleList.add(scheduleEntry);

                    // Save the new entry to the database with the assigned ID
                    courseSchedulesRef.child(String.valueOf(nextId)).setValue(scheduleEntry);
                    newScheduleIds.add((int) nextId);
                    Log.d(TAG, "Schedule List after adding is : " + newScheduleList);
                }

                // After adding schedules, proceed to create the course
                createCourse(courseCode.getText().toString(), courseName.getText().toString(), newScheduleIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors here.
            }
        }); // End of creating course schedules
    }

    private void createCourse(String courseCode, String courseName, List<Integer> newScheduleIds) {
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        // Create new course object
        Course newCourse = new Course();
        newCourse.setCourseCode(courseCode);
        newCourse.setCourseName(courseName);
        newCourse.setCourseSchedules(newScheduleIds);
        newCourse.setLecturer(getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null));
        newCourse.setStudents(new ArrayList<>());
        newCourse.setHasOngoingSession(false);

        // Save course object into courses node in firebase database
        coursesRef.child(courseCode).setValue(newCourse);

        // Add course code to lecturer's courses node in firebase database
        DatabaseReference userCoursesRef = FirebaseDatabase.getInstance().getReference("users").child(getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null)).child("courses");

        // Get the courses array, and add the new course code to it
        userCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> courses = new ArrayList<>();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String courseCode = courseSnapshot.getValue(String.class);
                    courses.add(courseCode);
                }
                courses.add(courseCode);
                userCoursesRef.setValue(courses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validateNewCourse(View view) {

        Log.d(TAG, "Validating new course");
        EditText courseCode = findViewById(R.id.course_code);
        EditText courseName = findViewById(R.id.course_name);
        // Check if course code and course name are empty
        if (courseCode.getText().toString().isEmpty() || courseName.getText().toString().isEmpty()) {
            Toast.makeText(LecturerCreateCourseActivity.this, "Course code or course name cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }
        // Get current lecturer ID
        String userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null);

        // Defining the database references
        DatabaseReference courseSchedulesRef = FirebaseDatabase.getInstance().getReference("course_schedules");
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");
        DatabaseReference userCoursesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("courses");

        // Defining the lists to store the data
        List<String> courseCodes = new ArrayList<>();
        List<Integer> courseScheduleIds = new ArrayList<>();
        List<CourseSchedule> existingScheduleInfo = new ArrayList<>();
        List<CourseSchedule> newScheduleInfo = new ArrayList<>();

        // Validate the course code and course name
        // Validate the new course schedules with lecturer's existing courses
        fetchExistingCourseCodes(userCoursesRef, courseCodes, () -> {
            Log.d(TAG, "Course codes are: " + courseCodes);
            validateCourseCodeAndCourseName(coursesRef, () -> {
                Log.d(TAG, "Course code and name are valid");
                fetchExistingCourseSchedules(coursesRef, courseCodes, courseScheduleIds, () -> {
                    Log.d(TAG, "Course schedule IDs are: " + courseScheduleIds);
                    fetchExistingCourseScheduleDetails(courseSchedulesRef, courseScheduleIds, existingScheduleInfo, () -> {
                        Log.d(TAG, "Existing schedule info is: " + existingScheduleInfo);
                        fetchNewCourseSchedules(courseSchedulesRef, newScheduleInfo, () -> {
                            Log.d(TAG, "New schedule info is: " + newScheduleInfo);
                            // Check for time conflicts
                            boolean hasTimeConflict = false;
                            // Loop through the new schedule info to compare with existing schedule info for time conflicts
                            for (CourseSchedule newSchedule : newScheduleInfo) {
                                for (CourseSchedule existingSchedule : existingScheduleInfo) {
                                    // Check if the days are the same then check for time conflicts
                                    // if there is a time conflict, break out of the loop
                                    if (newSchedule.getDay().equalsIgnoreCase(existingSchedule.getDay())) {
                                        if (isTimeConflict(newSchedule.getStartTime(), newSchedule.getEndTime(), existingSchedule.getStartTime(), existingSchedule.getEndTime())) {
                                            hasTimeConflict = true;
                                            break;
                                        } // End of time conflict check
                                    } // End of day check
                                } // End of existingScheduleInfo loop
                            } // End of newScheduleInfo loop

                            // If there is a time conflict, display an error message
                            // else create the new course
                            if (hasTimeConflict) {
                                // Display an error message
                                Toast.makeText(this, "There is a time conflict with your existing courses", Toast.LENGTH_LONG).show();
                            } else {
                                // No time conflicts, create the new course
                                createNewCourseSchedule(view);
                                // Display a success message
                                Toast.makeText(this, "Course created successfully", Toast.LENGTH_LONG).show();
                            }

                        }); // End of fetchNewCourseSchedules
                    }); // End of fetchExistingCourseScheduleDetails
                }); // End of fetchExistingCourseSchedules
            }); // End of validateCourseCodeAndCourseName
        }); // End of fetchExistingCourseCodes
    }

    public void validateCourseCodeAndCourseName(DatabaseReference coursesRef, Runnable onComplete) {

        EditText courseCode = findViewById(R.id.course_code);
        EditText courseName = findViewById(R.id.course_name);
        // Check if course code and course name exist in database
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasCourseCodeConflict = false;
                boolean hasCourseNameConflict = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String code = snapshot.child("courseCode").getValue(String.class);
                    String name = snapshot.child("courseName").getValue(String.class);
                    if (code.equalsIgnoreCase(courseCode.getText().toString())) {
                        hasCourseCodeConflict = true;
                    }
                    if (name.equalsIgnoreCase(courseName.getText().toString())) {
                        hasCourseNameConflict = true;
                    }
                }
                // check if both are true
                if (hasCourseCodeConflict && hasCourseNameConflict) {
                    Toast.makeText(LecturerCreateCourseActivity.this, "Course code and course name already exist!", Toast.LENGTH_LONG).show();
                } else if (hasCourseCodeConflict) {
                    Toast.makeText(LecturerCreateCourseActivity.this, "Course code already exists!", Toast.LENGTH_LONG).show();
                } else if (hasCourseNameConflict) {
                    Toast.makeText(LecturerCreateCourseActivity.this, "Course name already exists!", Toast.LENGTH_LONG).show();
                } else {
                    onComplete.run();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors here.
            }
        });
    }

    public void fetchNewCourseSchedules(DatabaseReference courseSchedulesRef, List<CourseSchedule> newScheduleInfo, Runnable onComplete) {
        EditText courseCode = findViewById(R.id.course_code);
        LinearLayout parentLayout = findViewById(R.id.course_schedule_layout);

        courseSchedulesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long maxId = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long id = Long.parseLong(snapshot.getKey());
                    if (id > maxId) {
                        maxId = id;
                    }
                }

                // Iterate through the parentLayout to collect data from the generated rows
                for (int i = 0; i < parentLayout.getChildCount(); i++) {
                    View scheduleRow = parentLayout.getChildAt(i);

                    // Retrieve views and data from the scheduleRow
                    Spinner daySpinner = scheduleRow.findViewById(R.id.daySpinner);
                    Spinner startTimeSpinner = scheduleRow.findViewById(R.id.startTimeSpinner);
                    Spinner endTimeSpinner = scheduleRow.findViewById(R.id.endTimeSpinner);

                    String selectedDay = daySpinner.getSelectedItem().toString();
                    String startTime = startTimeSpinner.getSelectedItem().toString();
                    String endTime = endTimeSpinner.getSelectedItem().toString();

                    // Create a CourseSchedule object
                    CourseSchedule scheduleEntry = new CourseSchedule();
                    scheduleEntry.setCourseCode(courseCode.getText().toString());
                    scheduleEntry.setDay(selectedDay);
                    scheduleEntry.setStartTime(startTime);
                    scheduleEntry.setEndTime(endTime);

                    // Assign the next schedule ID and increment it
                    long nextId = maxId + 1;

                    // Add the entry to the list
                    newScheduleInfo.add(scheduleEntry);
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors here.
            }
        });
    }

    public void fetchExistingCourseCodes(DatabaseReference userCoursesRef, List<String> courseCodes, Runnable onComplete) {
        // loop through userCoursesRef and add course codes to array
        userCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String courseCode = courseSnapshot.getValue(String.class);
                    courseCodes.add(courseCode);
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fetchExistingCourseSchedules(DatabaseReference coursesRef, List<String> courseCodes, List<Integer> courseScheduleIds, Runnable onComplete) {
        for (String course : courseCodes) {
            coursesRef.child(course).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot scheduleSnapshot : snapshot.child("courseSchedules").getChildren()) {
                        Integer schedule = scheduleSnapshot.getValue(Integer.class);
                        courseScheduleIds.add(schedule);
                    }
                    onComplete.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void fetchExistingCourseScheduleDetails(DatabaseReference courseSchedulesRef, List<Integer> courseScheduleIds, final List<CourseSchedule> existingScheduleInfo, final Runnable onComplete) {
        final AtomicInteger counter = new AtomicInteger(courseScheduleIds.size());

        for (Integer scheduleId : courseScheduleIds) {
            courseSchedulesRef.child(String.valueOf(scheduleId)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CourseSchedule schedule = snapshot.getValue(CourseSchedule.class);
                    existingScheduleInfo.add(schedule);
                    // Decrement the counter and check if all data has been fetched
                    if (counter.decrementAndGet() == 0) {
                        // All data has been fetched, call onComplete
                        onComplete.run();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error if needed
                    // Decrement the counter even in case of an error to ensure onComplete is eventually called
                    if (counter.decrementAndGet() == 0) {
                        onComplete.run();
                    }
                }
            });
        }
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