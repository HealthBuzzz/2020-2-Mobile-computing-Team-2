package com.healthbuzz.healthbuzz;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.healthbuzz.healthbuzz.data.LoginDataSource;
import com.healthbuzz.healthbuzz.data.model.YearData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

class StretchingMonth {
    // quantity unit is number
    public final int year, month;
    public LinkedList<Pair<Integer, Integer>> dayQuantityPairs;

    StretchingMonth(int year, int month, int[] day, int[] quantity) {
        this.year = year;
        this.month = month;
        this.dayQuantityPairs = new LinkedList<>();
        assert (day.length == quantity.length);
        for (int i = 0; i < day.length; i++) {
            dayQuantityPairs.add(new Pair(day[i], quantity[i]));
        }
    }

    int getYear() {
        return year;
    }
}

class YAxisValueFormatterForStretch extends ValueFormatter {

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return value + "회";
    }
}

public class StretchingDetailActivity extends AppCompatActivity {

    private LineChart lineChart;
    private ProgressBar progressBar;
    private int showYear, showMonth;

    private Object SingleObject;
    // This is from file or backend
    int todayStretching = 3;
    int progressValue = 0;

    int dayNeedStretching = 5;
    StretchingMonth[] stretchingMonths;
    List<YearData> year_data_total;
    int minutesToBUZZ = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stretching_detail);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
//        toolBarLayout.setTitle(getTitle());

        // This is mock data
        int[] dayArr1 = new int[]{1, 2, 8, 17, 30};
        int[] dayArr2 = new int[]{1, 2, 3, 14, 30};
        int[] quantityArr1 = new int[]{1, 2, 5, 6, 1};
        int[] quantityArr2 = new int[]{3, 2, 6, 1, 4};
        stretchingMonths = new StretchingMonth[]{new StretchingMonth(2020, 12, dayArr1, quantityArr1),
                new StretchingMonth(2020, 11, dayArr2, quantityArr2)};

        if (UserInfo.INSTANCE.getUserName().getValue() != "")
            LoginDataSource.getYearStretching();

        // Get current year&month for initial showing
        Calendar cal = Calendar.getInstance();
        showYear = cal.get(Calendar.YEAR);
        showMonth = cal.get(Calendar.MONTH) + 1;

        // ProgressBar configure
        progressBar = findViewById(R.id.progressBar);

        todayStretching = (int) RealtimeModel.INSTANCE.getStretching_count().getValue().intValue();

        progressValue = Math.round((float) todayStretching / dayNeedStretching * 100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(progressValue, true);
        } else {
            progressBar.setProgress(progressValue);
        }

        // textProgress configure
        TextView textProgress = findViewById(R.id.textProgress);
        textProgress.setText("  " + todayStretching + "/" + dayNeedStretching);
        textProgress.setText("  " + RealtimeModel.INSTANCE.getStretching_count().getValue() + "/" + dayNeedStretching);

        RealtimeModel.INSTANCE.getStretching_count().observe(this, aLong -> {
            Log.d("StretchingDetail", "changed to" + aLong);
            todayStretching = aLong.intValue();
            textProgress.setText("  " + aLong + "/" + dayNeedStretching);
            progressValue = Math.round((float) todayStretching / dayNeedStretching * 100);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(progressValue, true);
            } else {
                progressBar.setProgress(progressValue);
            }
        });

        // textBuzz configure, this must be called every minute ?through service?
        buzzTextUpdate();

        RealtimeModel.INSTANCE.getStretching_time_left().observe(this, new androidx.lifecycle.Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                TextView textBuzz = findViewById(R.id.textBuzz);
                String message = UtilKt.formatTime(StretchingDetailActivity.this, aLong.intValue());
                if (aLong >= 0)
                    textBuzz.setText("BUZZ " + message);
                else
                    textBuzz.setText(getString(R.string.you_need_stretch));
            }
        });

        TextView textBuzz = findViewById(R.id.textBuzz);
        textBuzz.setTypeface(null, Typeface.BOLD);

        ////// LINE CHART BELOW
        lineChart = (LineChart) findViewById(R.id.chart);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);
        xAxis.setTextSize(12f);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(30);


        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);
        yLAxis.setAxisMinimum(0);
        yLAxis.setAxisMaximum(8);
        LimitLine lim = new LimitLine(5, "Day objective"); // Create a limit line. This line also has some related methods for drawing properties. Just look at it yourself, not much.
        yLAxis.addLimitLine(lim);
        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.animateY(2000, Easing.EaseInCubic);

        lineChart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("Day");
        description.setTextSize(30f);
        lineChart.setDescription(description);
        yLAxis.setValueFormatter(new YAxisValueFormatterForStretch());

        lineChartDataUpdate();
        lineChart.invalidate();
        historyTextUpdate();

        RealtimeModel.INSTANCE.getYear_data().observe(this, year_data -> {
            lineChart = (LineChart) findViewById(R.id.chart);
            //XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(Color.BLACK);
            xAxis.enableGridDashedLine(8, 24, 0);
            xAxis.setTextSize(12f);
            xAxis.setAxisMinimum(0);
            xAxis.setAxisMaximum(30);


            //YAxis yLAxis = lineChart.getAxisLeft();
            yLAxis.setTextColor(Color.BLACK);
            yLAxis.setAxisMinimum(0);
            yLAxis.setAxisMaximum(8);
            //LimitLine lim = new LimitLine(5, "Day objective"); // Create a limit line. This line also has some related methods for drawing properties. Just look at it yourself, not much.
            yLAxis.addLimitLine(lim);
            //YAxis yRAxis = lineChart.getAxisRight();
            yRAxis.setDrawLabels(false);
            yRAxis.setDrawAxisLine(false);
            yRAxis.setDrawGridLines(false);
            lineChart.getLegend().setEnabled(false);
            lineChart.setDoubleTapToZoomEnabled(false);
            lineChart.setDrawGridBackground(false);
            lineChart.animateY(2000, Easing.EaseInCubic);

            lineChart.getDescription().setEnabled(true);
            //Description description = new Description();
            description.setText("Day");
            description.setTextSize(30f);
            lineChart.setDescription(description);
            yLAxis.setValueFormatter(new YAxisValueFormatterForStretch());

            year_data_total = year_data;

            lineChartDataUpdateForYearData();
            lineChart.invalidate();
            historyTextUpdate();
        });
    }

    // textBuzz configure, this must be updated every minute ?through service?
    public void buzzTextUpdate() {
        TextView textBuzz = findViewById(R.id.textBuzz);
        textBuzz.setText("BUZZ " + UtilKt.formatTime(this, RealtimeModel.INSTANCE.getStretching_time_left().getValue().intValue()));
    }

    private void lineChartDataUpdate() {
        List<Entry> entries = new ArrayList<>();

        StretchingMonth target = null;
        for (StretchingMonth stretchingMonth : stretchingMonths) {
            if (stretchingMonth.year == showYear && stretchingMonth.month == showMonth) {
                target = stretchingMonth;
            }
        }
        if (target != null) {
            for (int i = 0; i < target.dayQuantityPairs.size(); i++) {
                entries.add(new Entry(target.dayQuantityPairs.get(i).x,
                        target.dayQuantityPairs.get(i).y));
            }
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setCircleHoleColor(Color.WHITE);
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }


    private void lineChartDataUpdateForYearData() {
        List<Entry> entries = new ArrayList<>();
        if (year_data_total == null) {
            return;
        }
        for (YearData year_data : year_data_total) {
            if (year_data.getYear() == showYear && year_data.getMonth() == showMonth)
                entries.add(new Entry(year_data.getDay(),
                        year_data.getAmount()));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setCircleHoleColor(Color.WHITE);
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }

    private void historyTextUpdate() {
        TextView hisoryText = (TextView) findViewById(R.id.textHistory);
        hisoryText.setText("History in " + showYear + "." + (showMonth < 10 ? "0" : "") + showMonth);
    }

    public void onBeforeClicked(View v) {
        if (showMonth == 1) {
            showYear -= 1;
            showMonth = 12;
        } else {
            showMonth -= 1;
        }
        lineChartDataUpdate();
        if (UserInfo.INSTANCE.getUserName().getValue() != "" && year_data_total != null)
            lineChartDataUpdateForYearData();
        historyTextUpdate();
        lineChart.invalidate();
    }

    public void onAfterClicked(View v) {
        if (showMonth == 12) {
            showYear += 1;
            showMonth = 1;
        } else {
            showMonth += 1;
        }
        lineChartDataUpdate();
        if (UserInfo.INSTANCE.getUserName().getValue() != "" && year_data_total != null)
            lineChartDataUpdateForYearData();
        historyTextUpdate();
        lineChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UserInfo.INSTANCE.getUserName().getValue() != "")
            LoginDataSource.getYearStretching();
    }
}