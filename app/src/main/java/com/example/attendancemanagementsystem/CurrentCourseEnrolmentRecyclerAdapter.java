package com.example.attendancemanagementsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CurrentCourseEnrolmentRecyclerAdapter extends RecyclerView.Adapter<CurrentCourseEnrolmentRecyclerAdapter.ViewHolder> {
    private List<Course> currentCourseEnrolmentList;
    private OnRemoveButtonClickListener removeButtonClickListener;

    public CurrentCourseEnrolmentRecyclerAdapter(List<Course> currentCourseEnrolmentList) {
        this.currentCourseEnrolmentList = currentCourseEnrolmentList;
    }

    public void setOnRemoveButtonClickListener(OnRemoveButtonClickListener listener) {
        this.removeButtonClickListener = listener;
    }

    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClicked(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_current_enrolment_course_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course courseData = (Course) currentCourseEnrolmentList.get(position);

        if (courseData != null) {
            holder.courseCodeTextView.setText(courseData.getCourseCode());
            holder.courseNameTextView.setText(courseData.getCourseName());

            // Set an OnClickListener for the button
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Call the callback method when the button is clicked
                    int clickedPosition = holder.getBindingAdapterPosition(); // Get the current position
                    if (removeButtonClickListener != null && clickedPosition != RecyclerView.NO_POSITION) {
                        removeButtonClickListener.onRemoveButtonClicked(clickedPosition);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return currentCourseEnrolmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView courseCodeTextView;
        public TextView courseNameTextView;
        public Button removeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            courseCodeTextView = itemView.findViewById(R.id.new_current_enrolment_row_course_code);
            courseNameTextView = itemView.findViewById(R.id.new_current_enrolment_row_course_name);
            removeButton = itemView.findViewById(R.id.new_current_enrolment_row_remove_button);
        }
    }
}
