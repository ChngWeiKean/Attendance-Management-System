package com.example.attendancemanagementsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    private Paint paint;
    private List<List<String>> studentAttendanceData;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setData(List<List<String>> studentAttendanceData) {
        this.studentAttendanceData = studentAttendanceData;
        invalidate(); // Redraw the view when data changes
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (studentAttendanceData == null || studentAttendanceData.isEmpty()) {
            // Draw an empty pie chart (just the outline of the circle)
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(centerX, centerY);
            paint.setColor(Color.GRAY); // Set the color for the outline
            paint.setStyle(Paint.Style.FILL); // Set the style to stroke (no fill)

            canvas.drawCircle(centerX, centerY, radius, paint);

            // Add a text in the middle of the pie chart
            paint.setColor(Color.BLACK); // You can change the text color
            paint.setTextSize(50); // You can change the text size
            // Set font
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.font_family);
            paint.setTypeface(typeface);
            canvas.drawText("No records", centerX - 120, centerY, paint);

            // Draw legends
            drawLegend(canvas, "Present", getResources().getColor(getResources().getIdentifier("light_green", "color", getContext().getPackageName())), centerX + radius + 20, centerY - radius + 30);
            drawLegend(canvas, "Late", getResources().getColor(getResources().getIdentifier("orange", "color", getContext().getPackageName())), centerX + radius + 20, centerY - radius + 100); // Adjust Y position
            drawLegend(canvas, "Absent", getResources().getColor(getResources().getIdentifier("light_red", "color", getContext().getPackageName())), centerX + radius + 20, centerY - radius + 170); // Adjust Y position
            return;
        }

        float total = 0;
        List<Float> percentages = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        // Convert percentage strings to floats and collect colors
        for (List<String> data : studentAttendanceData) {
            if (data.size() >= 2) {
                try {
                    float percentage = Float.parseFloat(data.get(0)); // Assuming percentage is at index 0
                    percentages.add(percentage);
                    String color = data.get(1); // Assuming color is at index 1
                    colors.add(color);
                    total += percentage;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace(); // Handle invalid data gracefully
                }
            }
        }

        // Calculate the center point and radius for the circle
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY);

        float startAngle = 0;

        for (int i = 0; i < percentages.size(); i++) {
            float sweepAngle = 360f * percentages.get(i) / total;
            float midAngle = startAngle + sweepAngle / 2;

            // Calculate the position for the text label
            float labelX = (float) (centerX + radius * 0.6 * Math.cos(Math.toRadians(midAngle)));
            float labelY = (float) (centerY + radius * 0.5 * Math.sin(Math.toRadians(midAngle)));

            paint.setStyle(Paint.Style.FILL);

            paint.setColor(getResources().getColor(getResources().getIdentifier(colors.get(i), "color", getContext().getPackageName())));
            canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, startAngle, sweepAngle, true, paint);

            // Draw the percentage text label
            paint.setColor(Color.BLACK); // You can change the text color
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.font_family);
            paint.setTypeface(typeface);
            paint.setTextSize(40); // You can change the text size
            canvas.drawText(String.format("%.1f%%", percentages.get(i)), labelX, labelY, paint);

            startAngle += sweepAngle;
        }

        // Draw legends
        drawLegend(canvas, "Present", getResources().getColor(getResources().getIdentifier("light_green", "color", getContext().getPackageName())), centerX + radius + 20, centerY - radius + 30);
        drawLegend(canvas, "Late", getResources().getColor(getResources().getIdentifier("orange", "color", getContext().getPackageName())), centerX + radius + 20, centerY - radius + 100); // Adjust Y position
        drawLegend(canvas, "Absent", getResources().getColor(getResources().getIdentifier("light_red", "color", getContext().getPackageName())), centerX + radius + 20, centerY - radius + 170); // Adjust Y position
    }

    private void drawLegend(Canvas canvas, String label, int color, int x, int y) {
        int legendSize = 40; // Adjust the legend size as needed
        int legendPadding = 10; // Padding between legend and pie chart

        paint.setColor(color);
        canvas.drawRect(x, y, x + legendSize, y + legendSize, paint);

        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.font_family);
        paint.setTypeface(typeface);

        paint.setColor(Color.BLACK); // You can change the text color
        paint.setTextSize(50); // You can change the text size
        canvas.drawText(label, x + legendSize + legendPadding, y + legendSize, paint);
    }
}

