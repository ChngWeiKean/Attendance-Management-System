package com.example.attendancemanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.GenericTypeIndicator;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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

public class StudentClassEnrolmentActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private DatabaseReference studentEnrollmentsRef;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_course_enrolment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

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
                    // Add the student to the course's students list
                    addToCourse(courseCode, userId);
                    // Update the UI
                    availableEnrolment.removeView(row);
                    currentEnrolment.addView(row);
                    actionButton.setText("Remove");
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
        // Pass the event to the ActionBarDrawerToggle
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
