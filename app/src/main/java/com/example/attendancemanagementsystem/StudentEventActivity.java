package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentEventActivity extends AppCompatActivity {

    TextView title;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private SharedPreferences sharedPreferences;
    private List<Event> approvedEventList = new ArrayList<>();
    private List<Event> pendingEventList = new ArrayList<>();
    private List<Event> rejectedEventList = new ArrayList<>();
    String userId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_event);

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
                    startActivity(new Intent(StudentEventActivity.this, StudentDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentEventActivity.this, StudentEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_qr_scanner) {
                    startActivity(new Intent(StudentEventActivity.this, StudentQRScannerActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(StudentEventActivity.this, MainActivity.class));
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
                        if (Objects.equals(event.getApprovalStatus(), "Pending")) {
                            pendingEventList.add(event);
                        } else if (Objects.equals(event.getApprovalStatus(), "Approved")) {
                            approvedEventList.add(event);
                        } else if (Objects.equals(event.getApprovalStatus(), "Rejected")) {
                            rejectedEventList.add(event);
                        }
                        // log event lists
                        Log.d("Pending List", "pending: " + pendingEventList);
                        Log.d("Pending List", "approved: " + approvedEventList);
                        Log.d("Pending List", "rejected: " + rejectedEventList);

                        // If total number of events in pending, approved, and rejected list is equal to total number of snapshot children, setup recycler view
                        // Check if all events have been processed
                        if (pendingEventList.size() + approvedEventList.size() + rejectedEventList.size() == snapshot.getChildrenCount()) {
                            // All events have been categorized, set up RecyclerView
                            setupRecyclerView(event.getUserID());
                        }
                    }
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

    private void setupRecyclerView(String eventCreatorID) {
        // Initialize and configure the RecyclerView.
        if (!pendingEventList.isEmpty() && userId.equals(eventCreatorID)) {
            RecyclerView pendingEventRecyclerView = findViewById(R.id.pending_event_cards_container);
            StudentEventRecyclerAdapter pendingEventAdapter = new StudentEventRecyclerAdapter(pendingEventList, userId);
            LinearLayoutManager pendingLayoutManager = new LinearLayoutManager(this);
            pendingEventRecyclerView.setLayoutManager(pendingLayoutManager);
            adapterOnClick(pendingEventAdapter, pendingEventList, pendingEventRecyclerView);
        } else {
            TextView title = findViewById(R.id.pending_title);
            RecyclerView recyclerView = findViewById(R.id.pending_event_cards_container);
            // Set visibility to gone
            title.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
        if (!approvedEventList.isEmpty()) {
            RecyclerView approvedEventRecyclerView = findViewById(R.id.approved_event_cards_container);
            StudentEventRecyclerAdapter approvedEventAdapter = new StudentEventRecyclerAdapter(approvedEventList, userId);
            LinearLayoutManager approvedLayoutManager = new LinearLayoutManager(this);
            approvedEventRecyclerView.setLayoutManager(approvedLayoutManager);
            adapterOnClick(approvedEventAdapter, approvedEventList, approvedEventRecyclerView);
        } else {
            TextView title = findViewById(R.id.approved_title);
            RecyclerView recyclerView = findViewById(R.id.approved_event_cards_container);
            // Set visibility to gone
            title.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
        if (!rejectedEventList.isEmpty() && userId.equals(eventCreatorID)) {
            RecyclerView rejectedEventRecyclerView = findViewById(R.id.rejected_event_cards_container);
            StudentEventRecyclerAdapter rejectedEventAdapter = new StudentEventRecyclerAdapter(rejectedEventList, userId);
            LinearLayoutManager rejectedLayoutManager = new LinearLayoutManager(this);
            rejectedEventRecyclerView.setLayoutManager(rejectedLayoutManager);
            adapterOnClick(rejectedEventAdapter, rejectedEventList, rejectedEventRecyclerView);
        } else {
            TextView title = findViewById(R.id.rejected_title);
            RecyclerView recyclerView = findViewById(R.id.rejected_event_cards_container);
            // Set visibility to gone
            title.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void adapterOnClick(StudentEventRecyclerAdapter adapter, List<Event> eventList, RecyclerView eventRecyclerView) {
        // Set the adapter for the RecyclerView.
        eventRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            // Handle item click here
            Event event = eventList.get(position);
            // Redirect to another page or perform any action you want
            // For example, start a new activity with course details
            Intent intent = new Intent(StudentEventActivity.this, StudentViewEventActivity.class);
            intent.putExtra("eventID", event.getEventID());
            startActivity(intent);
        });

        adapter.setOnEditButtonClickListener(position -> {
            // Handle edit button click here
            Event event = eventList.get(position);
            // Redirect to edit activity or perform any edit action you want
            Intent intent = new Intent(StudentEventActivity.this, StudentEditEventActivity.class);
            intent.putExtra("eventID", event.getEventID());
            startActivity(intent);
        });

        adapter.setOnDeleteButtonClickListener(position -> {
            // Get the event to be deleted
            Event event = eventList.get(position);

            // Create a confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Deletion");
            builder.setMessage("Are you sure you want to delete this event?");

            // Add a positive button (Yes) and its action
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Perform the delete action here
                // Delete the event from the database
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(event.getEventID());
                // If there are images, delete them from the database
                eventRef.child("images").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        List<String> images = new ArrayList<>();
                        for (DataSnapshot ignored : task.getResult().getChildren()) {
                            images.add(ignored.getValue().toString());
                        }
                        // Convert image URLs to URIs
                        List<Uri> newList = new ArrayList<>();
                        for (String image : images) {
                            newList.add(Uri.parse(image));
                        }

                        // Remove the images from Firebase Storage and increment count
                        for (Uri imageUri : newList) {
                            StorageReference imageStorageRef = storageRef.child(imageUri.getLastPathSegment());
                            imageStorageRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // File deleted successfully
                                        Log.d("FirebaseDelete", "File deleted successfully");
                                    })
                                    .addOnFailureListener(exception -> {
                                        // Uh-oh, an error occurred!
                                        Log.d("FirebaseDelete", "Uh-oh, an error occurred!");
                                    });
                        }
                    }
                });
                eventRef.removeValue().addOnSuccessListener(aVoid -> {
                    // Event deleted successfully
                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();

                    // Remove the event from the eventList (assuming eventList is your data source for the RecyclerView)
                    eventList.remove(event);

                    // Notify the adapter that the data has changed
                    adapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    // Error occurred while deleting the event
                    Toast.makeText(this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            });

            // Add a negative button (No) and its action
            builder.setNegativeButton("No", (dialog, which) -> {
                // Dismiss the dialog (do nothing)
                dialog.dismiss();
            });

            // Show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public void redirectToCreateEvent(View view) {
        // redirect to create event page
        startActivity(new Intent(StudentEventActivity.this, StudentCreateEventActivity.class));
    }
}