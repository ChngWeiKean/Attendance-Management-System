package com.example.attendancemanagementsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class BarChartView extends View {
    private Paint barPaint;
    private List<Float> attendanceData; // List to hold attendance percentages
    private List<String> sessionDates; // List to hold session dates

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint();
        barPaint.setColor(getResources().getColor(R.color.purple)); // Bar color
    }

    // Set the attendance data and session dates
    public void setData(List<Float> attendanceData, List<String> sessionDates) {
        this.attendanceData = attendanceData;
        this.sessionDates = sessionDates;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int barWidthInDp = 25; // Set the desired bar width in dp
        int barSpacingInDp = 15; // Set the desired spacing between bars in dp
        float density = getResources().getDisplayMetrics().density;
        int maxBarWidth = (int) (barWidthInDp * density); // Convert dp to pixels
        int maxBarSpacing = (int) (barSpacingInDp * density); // Convert dp to pixels
        float maxAttendancePercentage = 100; // Max attendance percentage
        int minTextSize = 20; // Set the minimum text size in dp
        int textRotationDegrees = 18; // Set the rotation degree for the text

        if (attendanceData == null || sessionDates == null || attendanceData.size() != sessionDates.size()) {
            // Draw x and y axes
            barPaint.setColor(getResources().getColor(R.color.purple)); // Axis color
            barPaint.setStrokeWidth(5); // Axis line width

            // Draw y-axis with arrowhead
            float yAxisX = 50; // X-coordinate of the y-axis
            float yAxisStartY = 0; // Start Y-coordinate of the y-axis
            float yAxisEndY = height - 60; // End Y-coordinate of the y-axis

            // Draw arrowhead at the top of the y-axis
            canvas.drawLine(yAxisX, yAxisStartY, yAxisX, yAxisEndY, barPaint);
            canvas.drawLine(yAxisX - 15, 20, yAxisX, 0, barPaint); // Upper arrowhead line
            canvas.drawLine(yAxisX + 15, 20, yAxisX, 0, barPaint); // Upper arrowhead line

            // Draw x-axis with arrowhead
            float xAxisStartX = 10; // Start X-coordinate of the x-axis
            float xAxisEndX = width; // End X-coordinate of the x-axis
            float xAxisY = height - 100; // Y-coordinate of the x-axis

            // Draw arrowhead at the right end of the x-axis
            canvas.drawLine(xAxisStartX, xAxisY, xAxisEndX, xAxisY, barPaint);
            canvas.drawLine(xAxisEndX - 20, xAxisY - 15, xAxisEndX, xAxisY, barPaint); // Right arrowhead line
            canvas.drawLine(xAxisEndX - 20, xAxisY + 15, xAxisEndX, xAxisY, barPaint); // Right arrowhead line

            return;
        }

        int barCount = attendanceData.size();

        for (int i = 0; i < barCount; i++) {
            float attendancePercentage = attendanceData.get(i);
            float left = (i * (maxBarWidth + maxBarSpacing)) + maxBarSpacing;
            float right = left + maxBarWidth;
            float bottom = height - 100;

            // Define a maximum bar height in pixels
            float maxBarHeight = height - 100; // Adjust as needed

            // Calculate the bar height based on attendancePercentage, limiting it to the maximum
            float barHeight = Math.min(maxBarHeight, (height - 100) * (attendancePercentage / maxAttendancePercentage));

            // Set a minimum bar height (adjust as needed)
            float minBarHeight = maxBarWidth;

            // If attendancePercentage is 0, set the bar height to a different value
            if (attendancePercentage == 0) {
                barHeight = 0;
            } else {
                // Ensure that the bar height is not below the minimum
                barHeight = Math.max(barHeight, minBarHeight);
            }

            // Calculate the top coordinate based on the adjusted bar height
            float top = height - barHeight - 100;

            // Set color based on attendance percentage
            if (attendancePercentage >= 80) {
                barPaint.setColor(getResources().getColor(R.color.light_green));
            } else if (attendancePercentage >= 50) {
                barPaint.setColor(getResources().getColor(R.color.orange));
            } else {
                barPaint.setColor(getResources().getColor(R.color.light_red));
            }

            // Draw the bar
            canvas.drawRect(left, top, right, bottom, barPaint);


            // Draw session date labels with rotation
            barPaint.setColor(getResources().getColor(R.color.black)); // Text color
            String sessionDate = sessionDates.get(i);

            // Calculate the center point of the bar for text positioning
            float centerX = left + (maxBarWidth / 2f) - 30;
            float centerY = height - 50; // Adjust this value to position the date labels below the x-axis

            barPaint.setTextSize(40); // Text size for date labels

            // Rotate the canvas before drawing the text
            canvas.save();
            canvas.rotate(textRotationDegrees, centerX, centerY);

            // Draw the rotated text (session date)
            canvas.drawText(sessionDate, centerX, centerY, barPaint);

            // Restore the canvas to its original orientation
            canvas.restore();

            // Change color depending on attendanceData
            barPaint.setTextSize(50); // Text size for attendance values

            // Draw the attendance value, handle 0% differently
            if (attendancePercentage != 0 && attendancePercentage < 100) {
                canvas.drawText(String.valueOf((int) attendancePercentage), centerX, top + 60, barPaint);
            } else if (attendancePercentage == 100) {
                canvas.drawText(String.valueOf((int) attendancePercentage), centerX - 10, top + 60, barPaint);
            } else {
                canvas.drawText(String.valueOf((int) attendancePercentage), centerX, top - 20, barPaint);
            }

            barPaint.setColor(getResources().getColor(R.color.purple)); // Reset bar color
        }

        // Draw x and y axes
        barPaint.setColor(getResources().getColor(R.color.purple)); // Axis color
        barPaint.setStrokeWidth(5); // Axis line width

        // Draw y-axis with arrowhead
        float yAxisX = 50; // X-coordinate of the y-axis
        float yAxisStartY = 0; // Start Y-coordinate of the y-axis
        float yAxisEndY = height - 60; // End Y-coordinate of the y-axis

        // Draw arrowhead at the top of the y-axis
        canvas.drawLine(yAxisX, yAxisStartY, yAxisX, yAxisEndY, barPaint);
        canvas.drawLine(yAxisX - 15, 20, yAxisX, 0, barPaint); // Upper arrowhead line
        canvas.drawLine(yAxisX + 15, 20, yAxisX, 0, barPaint); // Upper arrowhead line

        // Draw x-axis with arrowhead
        float xAxisStartX = 10; // Start X-coordinate of the x-axis
        float xAxisEndX = width; // End X-coordinate of the x-axis
        float xAxisY = height - 100; // Y-coordinate of the x-axis

        // Draw arrowhead at the right end of the x-axis
        canvas.drawLine(xAxisStartX, xAxisY, xAxisEndX, xAxisY, barPaint);
        canvas.drawLine(xAxisEndX - 20, xAxisY - 15, xAxisEndX, xAxisY, barPaint); // Right arrowhead line
        canvas.drawLine(xAxisEndX - 20, xAxisY + 15, xAxisEndX, xAxisY, barPaint); // Right arrowhead line

    }
}