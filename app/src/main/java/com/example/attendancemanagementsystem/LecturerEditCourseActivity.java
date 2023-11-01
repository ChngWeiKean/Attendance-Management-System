package com.example.attendancemanagementsystem;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LecturerEditCourseActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    List<CourseSchedule> newScheduleList = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    int count = 0;
    String courseIdString;
    String courseNameString;
    DatabaseReference courseSchedulesRef;
    DatabaseReference coursesRef;

    Uri imageUri;

    private boolean timeConflictToastShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_edit_course);

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
                    startActivity(new Intent(LecturerEditCourseActivity.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerEditCourseActivity.this, LecturerEventActivity.class));
                    /*
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentProfileActivity.class));
                     */
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerEditCourseActivity.this, MainActivity.class));
                }
                return true;
            }
        });

        drawerLayout = findViewById(R.id.nav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        courseIdString = getIntent().getStringExtra("courseId");
        courseNameString = getIntent().getStringExtra("courseName");

        EditText courseCode = findViewById(R.id.edit_course_code);
        courseCode.setText(courseIdString);

        EditText courseName = findViewById(R.id.edit_course_name);
        courseName.setText(courseNameString);

        // Define the array of days
        String[] daysArray = getResources().getStringArray(R.array.days_array);
        String[] timeArray = getResources().getStringArray(R.array.course_time_array);
        // Create an ArrayAdapter using the days array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_layout, daysArray);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, timeArray);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        coursesRef = FirebaseDatabase.getInstance().getReference("courses");
        courseSchedulesRef = FirebaseDatabase.getInstance().getReference("course_schedules");

        fetchCourseSchedule(courseIdString);

        Button uploadButton = findViewById(R.id.edit_upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    private void fetchCourseSchedule(String courseId) {
        courseSchedulesRef.orderByChild("courseCode").equalTo(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot courseScheduleSnapshot : snapshot.getChildren()) {
                    String day = courseScheduleSnapshot.child("day").getValue(String.class);
                    String startTime = courseScheduleSnapshot.child("startTime").getValue(String.class);
                    String endTime = courseScheduleSnapshot.child("endTime").getValue(String.class);

                    systemAddScheduleRow(day, startTime, endTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching course schedule: " + databaseError.getMessage());
            }
        });
    }

    public void systemAddScheduleRow(String day, String startTime, String endTime) {
        LinearLayout parentLayout = findViewById(R.id.edit_course_schedule_layout);
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

        // Set the default value of the spinner
        daySpinner.setSelection(adapter.getPosition(day));
        startTimeSpinner.setSelection(timeAdapter.getPosition(startTime));
        endTimeSpinner.setSelection(timeAdapter.getPosition(endTime));

        // Add the scheduleRow to the parentLayout
        parentLayout.addView(scheduleRow);

        count++;
        Log.d(TAG, "Row added. Count is: " + count);
    }

    public void editAddScheduleRow(View view) {
        LinearLayout parentLayout = findViewById(R.id.edit_course_schedule_layout);
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

        // Add the scheduleRow to the parentLayout
        parentLayout.addView(scheduleRow);

        count++;
        Log.d(TAG, "Row added. Count is: " + count);

        if (count > 1) {
            Button deleteBtn = findViewById(R.id.edit_delete_button);
            deleteBtn.setVisibility(View.VISIBLE);
        }
    }

    public void editDeleteScheduleRow(View view) {
        LinearLayout parentLayout = findViewById(R.id.edit_course_schedule_layout);
        parentLayout.removeViewAt(parentLayout.getChildCount() - 1);
        count--;

        if (count == 1) {
            Button deleteBtn = findViewById(R.id.edit_delete_button);
            deleteBtn.setVisibility(View.GONE);
        }
    }

    public void saveEditCourse(View view) {
        // Get the original course code and course name
        String originalCourseCode = getIntent().getStringExtra("courseId");
        String originalCourseName = getIntent().getStringExtra("courseName");
        LinearLayout parentLayout = findViewById(R.id.edit_course_schedule_layout);

        // Get the new course code and course name
        EditText newCourseCodeEditText = findViewById(R.id.edit_course_code);
        EditText newCourseNameEditText = findViewById(R.id.edit_course_name);

        String newCourseCode = newCourseCodeEditText.getText().toString();
        String newCourseName = newCourseNameEditText.getText().toString();

        // Delete the original course schedule
        courseSchedulesRef.orderByChild("courseCode").equalTo(originalCourseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> existingInstanceKeys = new ArrayList<>();
                int instanceCount = 0;

                for (DataSnapshot courseScheduleSnapshot : snapshot.getChildren()) {
                    long instanceNumber = Long.parseLong(courseScheduleSnapshot.getKey());
                    Log.e(TAG, "Instance number is: " + instanceNumber);

                    // Store existing instance key
                    existingInstanceKeys.add(String.valueOf(instanceNumber));
                    instanceCount++;

                    courseScheduleSnapshot.getRef().removeValue();
                }

                addNewScheduleInstances(originalCourseCode, newCourseCode, parentLayout, existingInstanceKeys, instanceCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching course schedule: " + error.getMessage());
            }
        });
    }

    private void addNewScheduleInstances(String originalCourseCode, String newCourseCode, LinearLayout parentLayout, List<String> existingInstanceKeys, int instanceCount) {

        // Define the list to store the new schedule IDs
        List<Integer> newScheduleIds = new ArrayList<>();

        // Query the database to find the maximum instance number of the course
        courseSchedulesRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long maxInstanceNumber = 0;

                Log.d(TAG, "Instance count: " + instanceCount);

                if (snapshot.exists()) {
                    // Get the largest key value (in this case, the last key in the query result)
                    String largestKey = snapshot.getChildren().iterator().next().getKey();
                    maxInstanceNumber = Long.parseLong(largestKey);
                }

                Log.e(TAG, "Max instance number is: " + maxInstanceNumber);

                // Add the new schedule instances
                for (int i = 0; i < parentLayout.getChildCount(); i++) {
                    // Get the schedule row
                    View scheduleRow = parentLayout.getChildAt(i);

                    // Get the day, start time, and end time
                    Spinner daySpinner = scheduleRow.findViewById(R.id.daySpinner);
                    Spinner startTimeSpinner = scheduleRow.findViewById(R.id.startTimeSpinner);
                    Spinner endTimeSpinner = scheduleRow.findViewById(R.id.endTimeSpinner);

                    String day = daySpinner.getSelectedItem().toString();
                    String startTime = startTimeSpinner.getSelectedItem().toString();
                    String endTime = endTimeSpinner.getSelectedItem().toString();

                    CourseSchedule courseSchedule = new CourseSchedule();
                    courseSchedule.setCourseCode(newCourseCode);
                    courseSchedule.setDay(day);
                    courseSchedule.setStartTime(startTime);
                    courseSchedule.setEndTime(endTime);

                    // If there are existing instances, update them
                    if (i < instanceCount) {
                        String keyToUpdate = existingInstanceKeys.get(i);
                        Log.e(TAG, "Updating instance with key: " + keyToUpdate);
                        courseSchedulesRef.child(keyToUpdate).setValue(courseSchedule);

                        // Add the existing instance key to the list of new schedule IDs
                        newScheduleIds.add(Integer.parseInt(keyToUpdate));
                    } else {
                        // Add the new schedule instance to the database
                        long nextInstanceNumber = maxInstanceNumber + 1;
                        Log.e(TAG, "Adding new instance with key: " + nextInstanceNumber);
                        courseSchedulesRef.child(String.valueOf(nextInstanceNumber)).setValue(courseSchedule);
                        maxInstanceNumber = nextInstanceNumber;

                        // Add the new instance key to the list of new schedule IDs
                        newScheduleIds.add((int) nextInstanceNumber);
                    }
                }

                // Update the course code and course name
                EditText courseCodeEditText = findViewById(R.id.edit_course_code);
                EditText courseNameEditText = findViewById(R.id.edit_course_name);

                editCourse(courseCodeEditText.getText().toString(), courseNameEditText.getText().toString(), newScheduleIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching course schedule: " + error.getMessage());
            }
        });
    }

    // Declare a boolean flag to track if the Toast has been shown for editCourse
    boolean toastShownForEditCourse = false;

    private void editCourse(String newCourseCode, String newCourseName, List<Integer> newScheduleIds) {
        // Get the original course code and course name
        String originalCourseCode = getIntent().getStringExtra("courseId");

        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        // Find the course using the originalCourseCode
        Query query = coursesRef.orderByChild("courseCode").equalTo(originalCourseCode);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot courseSnapshot = snapshot.getChildren().iterator().next();

                    // Update the course code and course name
                    courseSnapshot.child("courseCode").getRef().setValue(newCourseCode);
                    courseSnapshot.child("courseName").getRef().setValue(newCourseName);

                    // Update the course schedules
                    courseSnapshot.child("courseSchedules").getRef().setValue(newScheduleIds);

                    // Display a success message
                    if (!toastShownForEditCourse) {
                        // Show the Toast message
                        Toast.makeText(LecturerEditCourseActivity.this, "Course updated successfully!", Toast.LENGTH_LONG).show();

                        // Set the flag to true to prevent further Toasts
                        toastShownForEditCourse = true;
                    }
                } else {
                    // Display an error message
                    if (!toastShownForEditCourse) {
                        // Show the Toast message
                        Toast.makeText(LecturerEditCourseActivity.this, "Course not found!", Toast.LENGTH_LONG).show();

                        // Set the flag to true to prevent further Toasts
                        toastShownForEditCourse = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
            }
        });
    }

    public void validateEditCourse(View view) {

        timeConflictToastShown = false;
        final boolean[] toastShown = {false};

        Log.d(TAG, "Validating edit course");
        EditText courseCode = findViewById(R.id.edit_course_code);
        EditText courseName = findViewById(R.id.edit_course_name);

        String courseCodeString = courseCode.getText().toString();
        // Check if course code and course name are empty
        if (courseCode.getText().toString().isEmpty() || courseName.getText().toString().isEmpty()) {
            Toast.makeText(LecturerEditCourseActivity.this, "Course code or course name cannot be empty!", Toast.LENGTH_LONG).show();
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
                                        if (!newSchedule.equals(existingSchedule) && isTimeConflict(newSchedule.getStartTime(), newSchedule.getEndTime(), existingSchedule.getStartTime(), existingSchedule.getEndTime())) {
                                            hasTimeConflict = true;

                                            Log.d(TAG, "Time conflict found between " + newSchedule + " and " + existingSchedule);
                                            Log.d(TAG, "Time conflict found between " + newSchedule.getStartTime() + " and " + newSchedule.getEndTime() + " and " + existingSchedule.getStartTime() + " and " + existingSchedule.getEndTime());
                                            break;
                                        } // End of time conflict check
                                    } // End of day check
                                } // End of existingScheduleInfo loop
                            } // End of newScheduleInfo loop

                            // If there is a time conflict, display an error message
                            // else create the new course
                            if (hasTimeConflict) {
                                // Display an error message
                                if (!toastShown[0]) {
                                    // Show the Toast message
                                    Toast.makeText(this, "There is a time conflict with your existing courses", Toast.LENGTH_LONG).show();

                                    // Set the flag to true to prevent further Toasts
                                    toastShown[0] = true;
                                }
                            } else {
                                // No time conflicts, create the new course
                                saveEditCourse(view);
                                uploadImageToFirebase(imageUri, courseCodeString);
                            }

                        }); // End of fetchNewCourseSchedules
                    }); // End of fetchExistingCourseScheduleDetails
                }); // End of fetchExistingCourseSchedules
            }); // End of validateCourseCodeAndCourseName
        }); // End of fetchExistingCourseCodes
    }

    public void validateCourseCodeAndCourseName(DatabaseReference coursesRef, Runnable onComplete) {

        String originalCourseCode = getIntent().getStringExtra("courseId");
        String originalCourseName = getIntent().getStringExtra("courseName");

        EditText courseCode = findViewById(R.id.edit_course_code);
        EditText courseName = findViewById(R.id.edit_course_name);
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
                    if (code.equalsIgnoreCase(originalCourseCode)) {
                        hasCourseCodeConflict = false;
                    }
                    if (name.equalsIgnoreCase(originalCourseName)) {
                        hasCourseNameConflict = false;
                    }
                }
                // check if both are true
                if (hasCourseCodeConflict && hasCourseNameConflict) {
                    Toast.makeText(LecturerEditCourseActivity.this, "Course code and course name already exist!", Toast.LENGTH_LONG).show();
                } else if (hasCourseCodeConflict) {
                    Toast.makeText(LecturerEditCourseActivity.this, "Course code already exists!", Toast.LENGTH_LONG).show();
                } else if (hasCourseNameConflict) {
                    Toast.makeText(LecturerEditCourseActivity.this, "Course name already exists!", Toast.LENGTH_LONG).show();
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
        EditText courseCode = findViewById(R.id.edit_course_code);
        LinearLayout parentLayout = findViewById(R.id.edit_course_schedule_layout);

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
        final String editedCourseCode = getIntent().getStringExtra("courseId");

        for (Integer scheduleId : courseScheduleIds) {
            courseSchedulesRef.child(String.valueOf(scheduleId)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CourseSchedule schedule = snapshot.getValue(CourseSchedule.class);

                    // Skip the schedule if it already exists
                    if (!schedule.getCourseCode().equals(editedCourseCode)) {
                        existingScheduleInfo.add(schedule);
                    }

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

    public void cancelEditCourse(View view) {
        finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            ImageView courseImageView = findViewById(R.id.edit_course_image_view);
            courseImageView.setImageURI(imageUri);
            courseImageView.setVisibility(View.VISIBLE);
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String courseCode) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference courseImageRef = storageRef.child("courseImage");

        String filename = courseCode + ".jpg";

        StorageReference imageRef = courseImageRef.child(filename);

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveCourseImageURLToDatabase(uri.toString(), courseCode);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveCourseImageURLToDatabase(String imageURL, String courseCode) {
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        coursesRef.child(courseCode).child("courseImage").setValue(imageURL);
    }
}


