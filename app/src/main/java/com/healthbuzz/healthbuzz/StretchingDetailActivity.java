package com.healthbuzz.healthbuzz;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

class stretchingMonth {
    // quantity unit is number
    public final int year, month;
    public LinkedList<Pair<Integer, Integer>> dayQuantityPairs;

    stretchingMonth(int year, int month, int[] day, int[] quantity) {
        this.year = year;
        this.month = month;
        this.dayQuantityPairs = new LinkedList<>();
        assert (day.length == quantity.length);
        for (int i = 0; i < day.length; i++) {
            dayQuantityPairs.add(new Pair(day[i], quantity[i]));
        }
    }
}

public class StretchingDetailActivity extends AppCompatActivity {

    private LineChart lineChart;
    private ProgressBar progressBar;
    private int showYear, showMonth;

    // This is from file or backend
    int todayStretching = 3;
    int dayNeedStretching = 5;
    stretchingMonth[] stretchingMonths;
    int minutesToBUZZ = 10;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stretching_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        // This is mock data
        int[] dayArr1 = new int[]{1, 2, 3, 4};
        int[] dayArr2 = new int[]{1, 2, 3, 4, 5};
        int[] quantityArr1 = new int[]{100, 200, 300, 100};
        int[] quantityArr2 = new int[]{300, 200, 300, 100, 400};
        stretchingMonths = new stretchingMonth[]{new stretchingMonth(2020, 10, dayArr1, quantityArr1),
                new stretchingMonth(2020, 9, dayArr2, quantityArr2)};

        // Get current year&month for initial showing
        Calendar cal = Calendar.getInstance();
        showYear = cal.get(Calendar.YEAR);
        showMonth = cal.get(Calendar.MONTH) + 1;

        // ProgressBar configure
        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(Math.round((float) todayStretching / dayNeedStretching * 100), true);

        // textProgress configure
        TextView textProgress = findViewById(R.id.textProgress);
        textProgress.setText("  " + todayStretching + "/" + dayNeedStretching);

        // textBuzz configure, this must be called every minute ?through service?
        buzzTextUpdate();
        TextView textBuzz = findViewById(R.id.textBuzz);
        textBuzz.setTypeface(null, Typeface.BOLD);

        ////// LINE CHART BELOW
        lineChart = (LineChart) findViewById(R.id.chart);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);

        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);
        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.animateY(2000, Easing.EaseInCubic);
        lineChartDataUpdate();
        lineChart.invalidate();
        historyTextUpdate();
    }

    // textBuzz configure, this must be updated every minute ?through service?
    public void buzzTextUpdate() {
        TextView textBuzz = findViewById(R.id.textBuzz);
        textBuzz.setText("BUZZ " + minutesToBUZZ + " minutes left!");
    }

    private void lineChartDataUpdate() {
        List<Entry> entries = new ArrayList<>();

        stretchingMonth target = null;
        for (com.healthbuzz.healthbuzz.stretchingMonth stretchingMonth : stretchingMonths) {
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
        historyTextUpdate();
        lineChart.invalidate();
    }
}