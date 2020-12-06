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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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

    private BarChart barChart;
    private ProgressBar progressBar;
    private int showYear, showMonth;

    YAxis yLAxis;
    LimitLine lim;
    Description description;
    YAxis yRAxis;

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
        barChart = (BarChart) findViewById(R.id.chart);

        yLAxis = barChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);
        yLAxis.setAxisMinimum(0);
        yLAxis.setAxisMaximum(8);
        lim = new LimitLine(5, "Day objective"); // Create a limit line. This line also has some related methods for drawing properties. Just look at it yourself, not much.
        yLAxis.addLimitLine(lim);
        yRAxis = barChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(31);
        barChart.getLegend().setEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.animateY(200, Easing.Linear);

        barChart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("Day");
        description.setTextSize(30f);
        description.setTextColor(Color.GRAY);
        barChart.setDescription(description);
        yLAxis.setValueFormatter(new YAxisValueFormatterForStretch());

        barChartDataUpdate();
        barChart.invalidate();
        historyTextUpdate();

        RealtimeModel.INSTANCE.getYear_data().observe(this, year_data -> {

            year_data_total = year_data;
            barChart = (BarChart) findViewById(R.id.chart);

            YAxis yLAxis = barChart.getAxisLeft();
            yLAxis.setTextColor(Color.BLACK);
            yLAxis.setAxisMinimum(0);
            yLAxis.setAxisMaximum(8);
            LimitLine lim = new LimitLine(5, "Day objective"); // Create a limit line. This line also has some related methods for drawing properties. Just look at it yourself, not much.
            yLAxis.addLimitLine(lim);
            YAxis yRAxis = barChart.getAxisRight();
            yRAxis.setDrawLabels(false);
            yRAxis.setDrawAxisLine(false);
            yRAxis.setDrawGridLines(false);
            barChart.getLegend().setEnabled(false);
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.setDrawGridBackground(false);
            barChart.animateY(200, Easing.Linear);

            barChart.getDescription().setEnabled(true);
            barChart.setDescription(description);
            yLAxis.setValueFormatter(new YAxisValueFormatterForStretch());

            barChartDataUpdateForYearData();
            barChart.invalidate();
            historyTextUpdate();
        });
    }

    // textBuzz configure, this must be updated every minute ?through service?
    public void buzzTextUpdate() {
        TextView textBuzz = findViewById(R.id.textBuzz);
        textBuzz.setText("BUZZ " + UtilKt.formatTime(this, RealtimeModel.INSTANCE.getStretching_time_left().getValue().intValue()));
    }

    private void barChartDataUpdate() {
        List<BarEntry> entries = new ArrayList<>();

        StretchingMonth target = null;
        for (StretchingMonth stretchingMonth : stretchingMonths) {
            if (stretchingMonth.year == showYear && stretchingMonth.month == showMonth) {
                target = stretchingMonth;
            }
        }
        if (target != null) {
            for (int i = 0; i < target.dayQuantityPairs.size(); i++) {
                entries.add(new BarEntry(target.dayQuantityPairs.get(i).x,
                        target.dayQuantityPairs.get(i).y));
            }
        }
        BarDataSet barDataSet = new BarDataSet(entries, "");
        barDataSet.setColor(Color.BLACK);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
    }


    private void barChartDataUpdateForYearData() {

        List<BarEntry> entries = new ArrayList<>();

        StretchingMonth target = null;
        if(year_data_total == null || UserInfo.INSTANCE.getUserName() == null){
            return;
        }
        for (YearData year_data : year_data_total) {
            if (year_data.getYear() == showYear && year_data.getMonth() == showMonth) {
                entries.add(new BarEntry(year_data.getDay(),
                        year_data.getAmount()));
                Log.d("SUG",""+year_data.getAmount());
            }
        }
        BarDataSet barDataSet = new BarDataSet(entries, "");
        barDataSet.setColor(Color.BLACK);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
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
        if (UserInfo.INSTANCE.getUserName().getValue() != "" && year_data_total != null)
            barChartDataUpdateForYearData();
        else
            barChartDataUpdate();
        historyTextUpdate();
        barChart.invalidate();
    }

    public void onAfterClicked(View v) {
        if (showMonth == 12) {
            showYear += 1;
            showMonth = 1;
        } else {
            showMonth += 1;
        }
        if (UserInfo.INSTANCE.getUserName().getValue() != "" && year_data_total != null)
            barChartDataUpdateForYearData();
        else
            barChartDataUpdate();
        historyTextUpdate();
        barChart.invalidate();
    }
    @Override
    public void onResume(){
        super.onResume();
        if (UserInfo.INSTANCE.getUserName().getValue() != "")
            LoginDataSource.getYearStretching();
    }
}