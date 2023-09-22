package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentViewEventActivity extends AppCompatActivity {

    TextView title;
    public DrawerLayout drawerLayout;
    private SharedPreferences sharedPreferences;
    private static final int PICK_IMAGES_REQUEST = 1;
    private ViewPager viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private LinearLayout dotsLayout;
    private List<ImageView> dots;
    ArrayList<Uri> selectedImageUris = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_view_event_details);

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
                    startActivity(new Intent(StudentViewEventActivity.this, StudentDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentViewEventActivity.this, StudentEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(StudentViewEventActivity.this, MainActivity.class));
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

        viewPager = findViewById(R.id.image_view_pager);
        dotsLayout = findViewById(R.id.dots_layout);

        // Initialize the list of dots
        dots = new ArrayList<>();

        // Initialize the ViewPager and ImagePagerAdapter
        imagePagerAdapter = new ImagePagerAdapter(this, new ArrayList<Uri>());
        viewPager.setAdapter(imagePagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        // Retrieve event id from intent
        String eventId = getIntent().getStringExtra("eventID");
        Log.d("StudentViewEventActivity", "Event ID: " + eventId);
        // Retrieve event from database
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventId);
        // Retrieve event information and store in an Event instance
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Event event = new Event();
                event.setEventID(snapshot.child("eventID").getValue(String.class));
                event.setTitle(snapshot.child("title").getValue(String.class));
                event.setVenue(snapshot.child("venue").getValue(String.class));
                event.setStartTime(snapshot.child("startTime").getValue(String.class));
                event.setEndTime(snapshot.child("endTime").getValue(String.class));
                GenericTypeIndicator<List<String>> getImage = new GenericTypeIndicator<List<String>>() {};
                event.setImages(snapshot.child("images").getValue(getImage));
                event.setUserID(snapshot.child("userID").getValue(String.class));
                event.setDate(snapshot.child("date").getValue(String.class));
                event.setApprovalStatus(snapshot.child("approvalStatus").getValue(String.class));
                event.setDescription(snapshot.child("description").getValue(String.class));
                Log.d("StudentViewEventActivity", "Event retrieved: " + event.toString());

                Button eventStatus = findViewById(R.id.event_details_status);
                TextView titleEditText = findViewById(R.id.event_title);
                TextView venueEditText = findViewById(R.id.venue);
                TextView descriptionEditText = findViewById(R.id.event_description);
                TextView dateTextView = findViewById(R.id.date);
                TextView startTime = findViewById(R.id.start_time_spinner);
                TextView endTime = findViewById(R.id.end_time_spinner);
                eventStatus.setText(event.getApprovalStatus());
                if (userId == event.getUserID()) {
                    eventStatus.setVisibility(View.VISIBLE);
                    if (event.getApprovalStatus().equals("Pending")) {
                        eventStatus.setBackgroundColor(getResources().getColor(R.color.purple));
                    } else if (event.getApprovalStatus().equals("Approved")) {
                        eventStatus.setBackgroundColor(getResources().getColor(R.color.light_green));
                    } else if (event.getApprovalStatus().equals("Rejected")) {
                        eventStatus.setBackgroundColor(getResources().getColor(R.color.light_red));
                    }
                }
                titleEditText.setText(event.getTitle());
                venueEditText.setText(event.getVenue());
                descriptionEditText.setText(event.getDescription());
                dateTextView.setText(event.getDate());
                startTime.setText(event.getStartTime());
                endTime.setText(event.getEndTime());

                // function to display images in adapter
                // convert string to uri
                if (event.getImages() != null) {
                    List<String> imageList = event.getImages();
                    List<Uri> imageUriList = new ArrayList<>();
                    for (String image : imageList) {
                        imageUriList.add(Uri.parse(image));
                    }
                    imagePagerAdapter.updateImages(imageUriList);
                    imagePagerAdapter.notifyDataSetChanged();

                    // Generate dots for the selected images
                    generateDots(imagePagerAdapter.getCount());

                    // Show the ViewPager and dots
                    viewPager.setVisibility(View.VISIBLE);
                    dotsLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log database error
                Log.e("StudentViewEventActivity", "Database fetch cancelled: " + error.getMessage());
            }
        });
    }

    private void generateDots(int count) {
        dots.clear();
        dotsLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            int drawableId = (i == 0) ? R.drawable.carousel_tab_selected : R.drawable.carousel_tab_unselected;
            Drawable drawable = getResources().getDrawable(drawableId);
            // add some horizontal margin to the dots
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setImageDrawable(drawable);
            dots.add(dot);
            dotsLayout.addView(dot);
        }
    }

    private ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            // Update dots when the page changes
            updateDots(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void updateDots(int currentPage) {
        for (int i = 0; i < dots.size(); i++) {
            int drawableId = (i == currentPage) ? R.drawable.carousel_tab_selected : R.drawable.carousel_tab_unselected;
            Drawable drawable;

            if (i == currentPage) {
                // Use the selected drawable
                drawable = ContextCompat.getDrawable(this, drawableId);
            } else {
                // Use the unselected drawable
                drawable = ContextCompat.getDrawable(this, drawableId);
            }

            dots.get(i).setImageDrawable(drawable);
        }
    }
}
