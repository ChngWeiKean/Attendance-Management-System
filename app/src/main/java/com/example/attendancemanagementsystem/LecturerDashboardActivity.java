package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LecturerDashboardActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Button createCourseButton;

    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;
    private DatabaseReference userRef;
    private DatabaseReference lecturerCoursesRef;
    private DatabaseReference courseRef;
    private DatabaseReference courseDetailsRef;
    List<Course> courseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_dashboard);

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
                    startActivity(new Intent(LecturerDashboardActivity.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerDashboardActivity.this, LecturerEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(LecturerDashboardActivity.this, LecturerProfileSettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerDashboardActivity.this, MainActivity.class));
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
        lecturerCoursesRef = userRef.child("courses");

        // Get a reference for retrieving course details from the database.
        courseRef = databaseReference.child("courses");

        // Retrieve the user's enrolled courses from the database.
        lecturerCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    String courseImageURL = courseDetailsSnapshot.child("courseImage").getValue(String.class);

                                    // Create a Course object and add it to the list.
                                    Course course = new Course();
                                    course.setCourseCode(courseCode);
                                    course.setCourseName(courseName);
                                    course.setCourseImageURL(courseImageURL);
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

        createCourseButton = findViewById(R.id.create_course_button);
        createCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LecturerDashboardActivity.this, LecturerCreateCourseActivity.class));
            }
        });

    }

    private void setupRecyclerView() {
        // Initialize and configure the RecyclerView.
        RecyclerView recyclerView = findViewById(R.id.lecturer_dashboard_cards_container);
        LecturerDashboardRecyclerAdapter adapter = new LecturerDashboardRecyclerAdapter(courseList);

        // Use a linear layout manager for the RecyclerView.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Set the adapter for the RecyclerView.
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new LecturerDashboardRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Course course = courseList.get(position);
                Intent intent = new Intent(LecturerDashboardActivity.this, LecturerCourseDetailsActivity.class);
                intent.putExtra("course", course.getCourseCode());
                intent.putExtra("courseName", course.getCourseName());
                startActivity(intent);
            }

            @Override
            public void onEditClick(int position) {
                Course course = courseList.get(position);
                Intent intent = new Intent(LecturerDashboardActivity.this, LecturerEditCourseActivity.class);
                intent.putExtra("courseId", course.getCourseCode());
                intent.putExtra("courseName", course.getCourseName());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {

            }
        });
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