package com.example.attendancemanagementsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvailableCourseEnrolmentRecyclerAdapter extends RecyclerView.Adapter<AvailableCourseEnrolmentRecyclerAdapter.ViewHolder> {
    private List<Course> availableCourseEnrolmentList;
    private OnEnrolButtonClickListener enrolButtonClickListener;

    public AvailableCourseEnrolmentRecyclerAdapter(List<Course> availableCourseEnrolmentList) {
        this.availableCourseEnrolmentList = availableCourseEnrolmentList;
    }

    public void setOnEnrolButtonClickListener(OnEnrolButtonClickListener listener) {
        this.enrolButtonClickListener = listener;
    }

    public interface OnEnrolButtonClickListener {
        void onEnrolButtonClicked(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_avaliable_enrolment_course_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course courseData = (Course) availableCourseEnrolmentList.get(position);

        if (courseData != null) {
            holder.courseCodeTextView.setText(courseData.getCourseCode());
            holder.courseNameTextView.setText(courseData.getCourseName());

            // Set an OnClickListener for the button
            holder.enrolButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Call the callback method when the button is clicked
                    int clickedPosition = holder.getBindingAdapterPosition(); // Get the current position
                    if (enrolButtonClickListener != null && clickedPosition != RecyclerView.NO_POSITION) {
                        enrolButtonClickListener.onEnrolButtonClicked(clickedPosition);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return availableCourseEnrolmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView courseCodeTextView;
        public TextView courseNameTextView;
        public Button enrolButton;

        public ViewHolder(View itemView) {
            super(itemView);
            courseCodeTextView = itemView.findViewById(R.id.new_available_enrolment_row_course_code);
            courseNameTextView = itemView.findViewById(R.id.new_available_enrolment_row_course_name);
            enrolButton = itemView.findViewById(R.id.new_available_enrolment_row_enrol_button);
        }
    }
}
