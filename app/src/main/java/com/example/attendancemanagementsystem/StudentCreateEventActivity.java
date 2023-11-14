package com.example.attendancemanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StudentCreateEventActivity extends AppCompatActivity {

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
        setContentView(R.layout.student_create_event);

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
                    startActivity(new Intent(StudentCreateEventActivity.this, StudentDashboardActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_events) {
                    startActivity(new Intent(StudentCreateEventActivity.this, StudentEventActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_qr_scanner) {
                    startActivity(new Intent(StudentCreateEventActivity.this, StudentQRScannerActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_user_profile) {
                    startActivity(new Intent(StudentCreateEventActivity.this, StudentProfileSettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.menu_logout) {
                    // Implement logout
                    // Clear the "Remember Me" preference
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    // Redirect to login page
                    startActivity(new Intent(StudentCreateEventActivity.this, MainActivity.class));
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

        Spinner startTimeSpinner = findViewById(R.id.start_time_spinner);
        Spinner endTimeSpinner = findViewById(R.id.end_time_spinner);

        String[] timeArray = getResources().getStringArray(R.array.course_time_array);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, timeArray);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startTimeSpinner.setAdapter(timeAdapter);
        endTimeSpinner.setAdapter(timeAdapter);

        viewPager = findViewById(R.id.image_view_pager);
        dotsLayout = findViewById(R.id.dots_layout);

        // Initialize the list of dots
        dots = new ArrayList<>();

        Button uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open an image picker when the button is clicked
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGES_REQUEST);
            }
        });

        // Initialize the ViewPager and ImagePagerAdapter
        imagePagerAdapter = new ImagePagerAdapter(this, new ArrayList<Uri>());
        viewPager.setAdapter(imagePagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            // Handle selected images
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                selectedImageUris.clear();

                for (int i = 0; i < count; i++) {
                    // Handle each selected image here and add its URI to the list
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }

                // Set the selected image URIs to the ImagePagerAdapter
                imagePagerAdapter.updateImages(selectedImageUris);
                imagePagerAdapter.notifyDataSetChanged();

                // Generate dots for the selected images
                generateDots(count);

                // Show the ViewPager and dots
                viewPager.setVisibility(View.VISIBLE);
                dotsLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private int uriToResourceId(Uri uri) {
        int resourceId;

        try {
            String path = uri.getLastPathSegment();
            if (path != null) {
                int dotIndex = path.lastIndexOf(".");
                if (dotIndex > 0) {
                    path = path.substring(0, dotIndex);
                }
                resourceId = getResources().getIdentifier(path, "drawable", getPackageName());
            } else {
                // If URI doesn't have a valid path, return a default resource ID
                resourceId = R.drawable.dashboard_card_image;
            }
        } catch (Exception e) {
            // Handle any exceptions, and return a default resource ID if needed
            resourceId = R.drawable.dashboard_card_image;
        }

        return resourceId;
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

    public void requestApproval(View view) {
        // save the user inputs to the database
        for (Uri uri : selectedImageUris) {
            System.out.println(uri.toString());
        }

        EditText eventTitle = findViewById(R.id.event_title);
        EditText eventDescription = findViewById(R.id.event_description);
        Spinner startTimeSpinner = findViewById(R.id.start_time_spinner);
        Spinner endTimeSpinner = findViewById(R.id.end_time_spinner);
        EditText date = findViewById(R.id.date);
        EditText venue = findViewById(R.id.venue);

        // Set error to the fields if empty
        if (eventTitle.getText().toString().isEmpty()) {
            eventTitle.setError("Please enter event title");
            return;
        }
        if (eventDescription.getText().toString().isEmpty()) {
            eventDescription.setError("Please enter event description");
            return;
        }
        if (date.getText().toString().isEmpty()) {
            date.setError("Please enter event date");
            return;
        }
        if (venue.getText().toString().isEmpty()) {
            venue.setError("Please enter event venue");
            return;
        }

        // Get the user's ID from shared preferences, which was obtained during login.
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        // Create a new Event object
        Event event = new Event();
        event.setTitle(eventTitle.getText().toString());
        event.setDescription(eventDescription.getText().toString());
        event.setStartTime(startTimeSpinner.getSelectedItem().toString());
        event.setEndTime(endTimeSpinner.getSelectedItem().toString());
        event.setDate(date.getText().toString());
        event.setVenue(venue.getText().toString());
        event.setUserID(userId);
        event.setApprovalStatus("Pending");
        event.setImages(new ArrayList<>());

        // Check if the events node exists in the database
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        // Check how many children does event node has, the event ID will start from e0001 onwards
        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int count = 0;
                for (DataSnapshot ignored : task.getResult().getChildren()) {
                    count++;
                }
                // Generate the event ID
                String eventID = "e" + String.format("%04d", count + 1);
                event.setEventID(eventID);
                // Save the event to the database
                eventsRef.child(eventID).setValue(event);
                // Save the images to database
                if (!selectedImageUris.isEmpty()) {
                    // Call the function to upload images to Firebase Storage
                    uploadImagesToFirebase(selectedImageUris, eventID);
                }
                Toast.makeText(this, "Created event successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(StudentCreateEventActivity.this, StudentEventActivity.class));
            }
        });
    }

    private void uploadImagesToFirebase(ArrayList<Uri> selectedImages, String eventID) {
        // Create a Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a folder in Firebase Storage to store the images (e.g., "images/")
        StorageReference imagesFolderRef = storageRef.child("images/");

        for (Uri imageUri : selectedImages) {
            // Generate a unique filename for each image (you can customize this)
            String filename = generateUniqueFilename();

            // Create a reference to the image in Firebase Storage
            StorageReference imageRef = imagesFolderRef.child(filename);

            // Upload the image to Firebase Cloud Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Monitor the upload task
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully, you can get the download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save the image URL (uri.toString()) to your database
                    saveImageUrlToDatabase(uri.toString(), eventID);
                });
            }).addOnFailureListener(e -> {
                // Handle the error
                Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Generate a unique filename for each image
    private String generateUniqueFilename() {
        // Generate a unique filename using timestamp and UUID
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
    }

    // Implement the saveImageUrlToDatabase method as described earlier.
    private void saveImageUrlToDatabase(String imageUrl, String eventID) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("events");

        // Find the newly created event and add path of image to the images variable
        databaseRef.child(eventID).child("images").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> images = new ArrayList<>();
                for (DataSnapshot ignored : task.getResult().getChildren()) {
                    images.add(ignored.getValue().toString());
                }
                images.add(imageUrl);
                databaseRef.child(eventID).child("images").setValue(images);
            }
        });
    }
}