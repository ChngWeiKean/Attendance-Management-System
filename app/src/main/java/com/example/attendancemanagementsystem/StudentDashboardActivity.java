package com.example.attendancemanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ValueEventListener;

public class StudentDashboardActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Button courseEnrolmentButton;

    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;
    private DatabaseReference userRef;
    private DatabaseReference studentEnrollmentsRef;
    private DatabaseReference courseRef;
    private DatabaseReference courseDetailsRef;
    List<Course> courseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_dashboard);

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
                    startActivity(new Intent(StudentDashboardActivity.this, StudentDashboardActivity.class));
                    /*
                } else if (menuItem.getItemId() == R.id.menu_qr_scanner) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentQRScannerActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentEventsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(StudentDashboardActivity.this, StudentProfileActivity.class));
                     */
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    startActivity(new Intent(StudentDashboardActivity.this, MainActivity.class));
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

        // Get references to the user's course enrollments in the database.
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userRef = databaseReference.child("users").child(userId);
        studentEnrollmentsRef = userRef.child("courses");

        // Get a reference for retrieving course details from the database.
        courseRef = databaseReference.child("courses");

        // Retrieve the user's enrolled courses from the database.
        studentEnrollmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userCourseSnapshot) {
                // Check if user has any enrolled courses.
                if (userCourseSnapshot.exists()) {
                    // Iterate through each enrolled course to fetch its details from the database.
                    for (DataSnapshot userCourse : userCourseSnapshot.getChildren()) {
                        String userCourseCode = userCourse.getValue(String.class);

                        // Get a reference to the course details in the database.
                        courseDetailsRef = courseRef.child(userCourseCode);

                        // Retrieve course details from the database.
                        courseDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot courseDetailsSnapshot) {
                                if (courseDetailsSnapshot.exists()) {
                                    // Extract course codes and names from the database.
                                    String courseCode = courseDetailsSnapshot.child("courseCode").getValue(String.class);
                                    String courseName = courseDetailsSnapshot.child("courseName").getValue(String.class);

                                    // Create a Course object and add it to the list.
                                    Course course = new Course();
                                    course.setCourseCode(courseCode);
                                    course.setCourseName(courseName);
                                    courseList.add(course);

                                    // Set up the RecyclerView when all courses have been added to the list.
                                    if (courseList.size() == userCourseSnapshot.getChildrenCount()) {
                                        setupRecyclerView();
                                    }
                                } else {
                                    Log.e("", "Course details snapshot is null or does not exist");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("CourseDetails", "Database Error: " + error.getMessage());
                            }
                        });
                    }
                } else {
                    Log.e("", "Course snapshot is null or does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("StudentEnrolmentCourses", "Database Error: " + databaseError.getMessage());
            }
        });

        courseEnrolmentButton = findViewById(R.id.course_enrolment_button);
        courseEnrolmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StudentDashboardActivity.this, StudentClassEnrolmentActivity.class));
            }
        });
    }

    private void setupRecyclerView() {
        // Initialize and configure the RecyclerView.
        RecyclerView recyclerView = findViewById(R.id.dashboard_cards_container);
        StudentDashboardRecyclerAdapter adapter = new StudentDashboardRecyclerAdapter(courseList);

        // Use a linear layout manager for the RecyclerView.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Set the adapter for the RecyclerView.
        recyclerView.setAdapter(adapter);
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

