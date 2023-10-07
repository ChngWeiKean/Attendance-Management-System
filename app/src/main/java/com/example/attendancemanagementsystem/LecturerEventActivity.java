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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LecturerEventActivity extends AppCompatActivity {
    TextView title;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private List<Event> approvedEventList = new ArrayList<>();

    String userId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        NavigationView navigationView = findViewById(R.id.navigation_view);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.menu_events);
        menuItem.setChecked(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.menu_dashboard) {
                    startActivity(new Intent(LecturerEventActivity.this, LecturerDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(LecturerEventActivity.this, LecturerEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(LecturerEventActivity.this, MainActivity.class));
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

        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events");
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                        Event event = new Event();

                        // Check if the "images" node exists for the event
                        if (eventSnapshot.hasChild("images")) {
                            DataSnapshot imagesSnapshot = eventSnapshot.child("images");

                            List<String> imageList = new ArrayList<>();

                            // Iterate through child nodes to get image URLs
                            for (DataSnapshot imageSnapshot : imagesSnapshot.getChildren()) {
                                String imageUrl = imageSnapshot.getValue(String.class);
                                imageList.add(imageUrl);

                                // Log the retrieved image URL
                                Log.d("FirebaseFetch", "Retrieved image URL: " + imageUrl);
                            }

                            event.setImages(imageList);
                        } else {
                            // If the "images" node does not exist, add a placeholder image.
                            List<String> imageList = new ArrayList<>();
                            // Add the default_events image from drawable
                            imageList.add("android.resource://com.example.attendancemanagementsystem/drawable/default_events");
                            event.setImages(imageList);
                        }

                        // Get userID from event
                        String eventUserID = eventSnapshot.child("userID").getValue(String.class);
                        String eventID = eventSnapshot.getKey();
                        String status = eventSnapshot.child("approvalStatus").getValue(String.class);
                        event.setUserID(eventUserID);
                        event.setEventID(eventID);
                        event.setApprovalStatus(status);
                        Log.d("event", event.toString());
                        if (Objects.equals(event.getApprovalStatus(), "Approved")) {
                            approvedEventList.add(event);
                        }
                        Log.d("Pending List", "approved: " + approvedEventList);
                    }
                    setupRecyclerView();
                } else {
                    TextView noEvents = findViewById(R.id.no_events_text);
                    noEvents.setVisibility(View.VISIBLE);
                    Log.e("FirebaseFetch", "Course details snapshot is null or does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseFetch", "Database fetch cancelled: " + error.getMessage());
            }
        });
    }

    private void setupRecyclerView() {
        // Initialize and configure the RecyclerView.
        if (!approvedEventList.isEmpty()) {
            RecyclerView approvedEventRecyclerView = findViewById(R.id.event_cards_container);
            LecturerEventRecyclerAdapter approvedEventAdapter = new LecturerEventRecyclerAdapter(approvedEventList, userId);
            LinearLayoutManager approvedLayoutManager = new LinearLayoutManager(this);
            approvedEventRecyclerView.setLayoutManager(approvedLayoutManager);
            adapterOnClick(approvedEventAdapter, approvedEventList, approvedEventRecyclerView);
        } else {
            TextView title = findViewById(R.id.title);
            RecyclerView recyclerView = findViewById(R.id.event_cards_container);
            // Set visibility to gone
            title.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void adapterOnClick(LecturerEventRecyclerAdapter adapter, List<Event> eventList, RecyclerView eventRecyclerView) {
        // Set the adapter for the RecyclerView.
        eventRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            // Handle item click here
            Event event = eventList.get(position);
            // Redirect to another page or perform any action you want
            // For example, start a new activity with course details
            Intent intent = new Intent(LecturerEventActivity.this, LecturerViewAndApproveEventsActivity.class);
            intent.putExtra("eventID", event.getEventID());
            startActivity(intent);
        });
    }

    public void redirectToApproveEvent(View view) {
        // redirect to create event page
        startActivity(new Intent(LecturerEventActivity.this, LecturerEventsForApproval.class));
    }

}
