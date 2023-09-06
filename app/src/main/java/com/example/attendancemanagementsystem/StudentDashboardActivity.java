package com.example.attendancemanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;

public class StudentDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_dashboard);
    }
}
