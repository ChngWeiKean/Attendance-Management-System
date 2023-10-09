package com.example.attendancemanagementsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

public class HorizontalBarChartView extends View {
    private Paint paint;
    private float recommendedPercentage;
    private float percentagePresent;
    private float classAverage;

    public HorizontalBarChartView(Context context) {
        super(context);
        init();
    }

    public HorizontalBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setData(float recommendedPercentage, float percentagePresent, float classAverage) {
        this.recommendedPercentage = recommendedPercentage;
        this.percentagePresent = percentagePresent;
        this.classAverage = classAverage;
        invalidate(); // Redraw the view when data changes
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Define the gap between bars
        int gap = 60; // You can adjust this value

        // Calculate bar heights
        int barHeight = (height - 2 * gap) / 4;

        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.font_family);
        paint.setTypeface(typeface);

        // Check if the data is empty
        if (recommendedPercentage <= 0 && percentagePresent <= 0 && classAverage <= 0) {
            // Data is empty, display a message
            paint.setColor(Color.BLACK); // Set text color to black
            paint.setTextSize(50); // Set text size
            String noDataMessage = "No data available";
            float textWidth = paint.measureText(noDataMessage);
            float textX = (width - textWidth) / 2;
            float textY = height / 2;
            canvas.drawText(noDataMessage, textX, textY, paint);
            return;
        }

        // Draw the x and y axis
        paint.setColor(Color.BLACK); // Set axis color to black
        paint.setStrokeWidth(3); // Set axis line thickness

        // Draw y-axis
        canvas.drawLine(gap + 40, 0, gap + 40, height - 20, paint);

        // Draw x-axis
        canvas.drawLine(gap, height - gap, width, height - gap, paint);

        // Draw axis labels
        paint.setTextSize(50); // Set text size for labels

        // Label for y-axis
        String yAxisLabel = "Category";
        float yAxisLabelX = gap ; // X position for y-axis label
        float yAxisLabelY = height / 2 + 40; // Y position for y-axis label
        canvas.rotate(-90, yAxisLabelX, yAxisLabelY); // Rotate label vertically
        canvas.drawText(yAxisLabel, yAxisLabelX, yAxisLabelY, paint);
        // Rotate the canvas back to its original orientation
        canvas.rotate(90, yAxisLabelX, yAxisLabelY);

        // Label for x-axis
        String xAxisLabel = "Percentage";
        float xAxisLabelX = (width - 200) / 2; // X position for x-axis label
        float xAxisLabelY = height - gap / 2 + 20; // Y position for x-axis label
        canvas.drawText(xAxisLabel, xAxisLabelX, xAxisLabelY, paint);

        // Draw the recommended bar
        paint.setColor(getResources().getColor(R.color.blue));
        canvas.drawRect(gap + 40, gap - 20, (width * recommendedPercentage / 100) - gap, gap + barHeight - 20, paint);

        // Draw the present bar
        paint.setColor(getResources().getColor(R.color.light_green));
        canvas.drawRect(gap + 40, gap + barHeight + gap - 20, (width * percentagePresent / 100) - gap, gap + 2 * barHeight + gap - 20, paint);

        // Draw the class average bar
        paint.setColor(getResources().getColor(R.color.orange));
        canvas.drawRect(gap + 40, gap + 2 * (barHeight + gap) - 20, (width * classAverage / 100) - gap, gap + 3 * barHeight + 2 * gap - 20, paint);

        // Draw percentages inside the bars
        paint.setColor(Color.BLACK); // Set text color to black
        paint.setTextSize(50); // Set text size

        String recommendedText = String.format("%.0f%%", recommendedPercentage);
        String presentText = String.format("%.0f%%", percentagePresent);
        String classAverageText = String.format("%.0f%%", classAverage);

        // Calculate text positions at the end of the bars
        float textX1 = width * recommendedPercentage / 100 - 200; // Adjust the X position
        float textX2 = width * percentagePresent / 100 - 200; // Adjust the X position
        float textX3 = width * classAverage / 100 - 200; // Adjust the X position
        float textY1 = gap + barHeight / 2; // Y position for recommended bar
        float textY2 = gap + barHeight + gap + barHeight / 2; // Y position for present bar
        float textY3 = gap + 2 * (barHeight + gap) + barHeight / 2; // Y position for class average bar

        // Draw percentages at the end of the bars
        canvas.drawText(recommendedText, textX1, textY1, paint);
        canvas.drawText(presentText, textX2, textY2, paint);
        canvas.drawText(classAverageText, textX3, textY3, paint);

        float categoryX = gap + 60;
        float categoryYRecommended = gap + barHeight / 2;
        float categoryYPresent = gap + barHeight + gap + barHeight / 2;
        float categoryYClassAverage = gap + 2 * (barHeight + gap) + barHeight / 2;

        // Draw percentages at the end of the bars
        canvas.drawText("Recommended", categoryX, categoryYRecommended, paint);
        canvas.drawText("Present", categoryX, categoryYPresent, paint);
        canvas.drawText("Class Average", categoryX, categoryYClassAverage, paint);

    }


}
