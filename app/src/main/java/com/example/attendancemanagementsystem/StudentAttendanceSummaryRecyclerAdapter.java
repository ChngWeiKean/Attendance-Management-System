package com.example.attendancemanagementsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAttendanceSummaryRecyclerAdapter extends RecyclerView.Adapter<StudentAttendanceSummaryRecyclerAdapter.ViewHolder> {
    private List<List<String>> studentAttendanceList;
    private Context context;

    public StudentAttendanceSummaryRecyclerAdapter(Context context, List<List<String>> studentAttendanceList) {
        this.context = context;
        this.studentAttendanceList = studentAttendanceList;
    }

    @NonNull
    @Override
    public StudentAttendanceSummaryRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_student_attendance_summary_row, parent, false);
        return new StudentAttendanceSummaryRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAttendanceSummaryRecyclerAdapter.ViewHolder holder, int position) {
        List<String> attendanceSummaryData = studentAttendanceList.get(position);

        if (attendanceSummaryData != null && attendanceSummaryData.size() >= 4) {
            holder.studentNameTextView.setText(attendanceSummaryData.get(0));
            holder.studentIDTextView.setText(attendanceSummaryData.get(1));
            holder.studentStatusButtonView.setText(attendanceSummaryData.get(2));

            int colorResourceId = context.getResources().getIdentifier(attendanceSummaryData.get(3), "color", context.getPackageName());
            int color = ContextCompat.getColor(context, colorResourceId);
            holder.studentStatusButtonView.setBackgroundColor(color);

        }
    }

    @Override
    public int getItemCount() {
        return studentAttendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView studentNameTextView;
        public TextView studentIDTextView;
        public Button studentStatusButtonView;

        public ViewHolder(View itemView) {
            super(itemView);
            studentNameTextView = itemView.findViewById(R.id.student_name);
            studentIDTextView = itemView.findViewById(R.id.student_id);
            studentStatusButtonView = itemView.findViewById(R.id.attendance_status);
        }
    }
}
