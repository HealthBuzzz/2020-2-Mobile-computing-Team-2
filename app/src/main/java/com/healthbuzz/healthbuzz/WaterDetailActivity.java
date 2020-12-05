package com.healthbuzz.healthbuzz;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.healthbuzz.healthbuzz.data.LoginDataSource;
import com.healthbuzz.healthbuzz.data.model.TodayData;
import com.healthbuzz.healthbuzz.data.model.YearData;

import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

class DrinkMonth {
    // quantity unit is mililiter
    public final int year, month;
    public LinkedList<Pair<Integer, Integer>> dayQuantityPairs;

    DrinkMonth(int year, int month, int[] day, int[] quantity) {
        this.year = year;
        this.month = month;
        this.dayQuantityPairs = new LinkedList<>();
        assert (day.length == quantity.length);
        for (int i = 0; i < day.length; i++) {
            dayQuantityPairs.add(new Pair(day[i], quantity[i]));
        }
    }
}

class YAxisValueFormatterForWater extends ValueFormatter {

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return value + "mL";
    }
}

public class WaterDetailActivity extends AppCompatActivity {

    private LineChart lineChart;
    private PieChart pieChart;
    private int showYear, showMonth;

    // This is from file or backend
    long todayDrink = 1100;
    long dayNeedDrink = 2000;
    ArrayList<PieEntry> pieEntries;
    PieDataSet pieDataSet;
    PieData pieData;
    DrinkMonth[] drinkMonths;

    List<YearData> total_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_detail);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
//        toolBarLayout.setTitle(getTitle());

        // This is mock data
        if (UserInfo.INSTANCE.getUserName().getValue() != "")
            LoginDataSource.getYearWater();
        int[] dayArr1 = new int[]{1, 5, 23, 30};
        int[] dayArr2 = new int[]{1, 5, 13, 24, 30};
        int[] quantityArr1 = new int[]{1100, 2200, 2300, 1100};
        int[] quantityArr2 = new int[]{300, 1200, 2300, 2100, 400};
        drinkMonths = new DrinkMonth[]{new DrinkMonth(2020, 12, dayArr1, quantityArr1),
                new DrinkMonth(2020, 11, dayArr2, quantityArr2)};

        // Get current year&month for initial showing
        Calendar cal = Calendar.getInstance();
        showYear = cal.get(Calendar.YEAR);
        showMonth = cal.get(Calendar.MONTH) + 1;

        pieChart = findViewById(R.id.piechart);
        int[] colorArray = new int[]{Color.CYAN, Color.GRAY};
        pieEntries = new ArrayList();


        //todayDrink = SingleObject.getInstance().water_count.getValue();

        todayDrink = RealtimeModel.INSTANCE.getWater_count().getValue();
        long marginToday = dayNeedDrink - todayDrink;
        if (marginToday < 0) {
            marginToday = 0;
        }
        pieEntries.add(new PieEntry(todayDrink, ""));
        pieEntries.add(new PieEntry(marginToday, ""));
        pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(colorArray);

        pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(15f); // <- here
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("You drinked\n" + Math.round((float) todayDrink / dayNeedDrink * 100) + "%");
        pieChart.setCenterTextSize(10f);
        pieChart.animate();
        pieChart.invalidate();

        RealtimeModel.INSTANCE.getWater_count().observe(this, aLong -> {
            todayDrink = aLong;
            long new_marginToday = dayNeedDrink - todayDrink;
            if (new_marginToday < 0) {
                new_marginToday = 0;
            }
            pieEntries = new ArrayList();
            pieEntries.add(new PieEntry(todayDrink, ""));
            pieEntries.add(new PieEntry(new_marginToday, ""));
            pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setColors(colorArray);

            pieData = new PieData(pieDataSet);
            pieData.setValueTextSize(15f); // <- here
            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.getLegend().setEnabled(false);
            pieChart.setCenterText("You drinked\n" + Math.round((float) todayDrink / dayNeedDrink * 100) + "%");
            pieChart.setCenterTextSize(10f);
            pieChart.animate();
            pieChart.invalidate();
        });

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

        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        yLAxis.setAxisMinimum(0);
        yLAxis.setAxisMaximum(3000);
        lineChart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("Day");
        description.setTextSize(30f);
        LimitLine lim = new LimitLine(2000, "Day objective"); // Create a limit line. This line also has some related methods for drawing properties. Just look at it yourself, not much.
        yLAxis.addLimitLine(lim);
        lineChart.setDescription(description);
        yLAxis.setValueFormatter(new YAxisValueFormatterForWater());
        yLAxis.setTextSize(10);


        lineChart.getLegend().setEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.animateY(2000, Easing.EaseInCubic);
        lineChartDataUpdate();
        lineChart.invalidate();
        historyTextUpdate();

        RealtimeModel.INSTANCE.getYear_data().observe(this, year_data -> {
            lineChart = (LineChart) findViewById(R.id.chart);
            total_data = year_data;
            lineChartDataUpdateForYear();
            lineChart.invalidate();
            historyTextUpdate();

        });
    }

    private void lineChartDataUpdateForYear() {
        List<YearData> year_data = total_data;
        if(total_data == null)
            return;
        List<Entry> entries = new ArrayList<>();

        DrinkMonth target = null;
        for (DrinkMonth drinkMonth : drinkMonths) {
            if (drinkMonth.year == showYear && drinkMonth.month == showMonth) {
                target = drinkMonth;
            }
        }
        for (YearData data : year_data ) {
            if(data.getYear() == showYear && data.getMonth() == showMonth) {
                entries.add(new Entry(data.getDay(), data.getAmount()));
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

    private void lineChartDataUpdate() {
        List<Entry> entries = new ArrayList<>();

        DrinkMonth target = null;
        for (DrinkMonth drinkMonth : drinkMonths) {
            if (drinkMonth.year == showYear && drinkMonth.month == showMonth) {
                target = drinkMonth;
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
        if (UserInfo.INSTANCE.getUserName().getValue() != "" && total_data!= null)
            lineChartDataUpdateForYear();
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
        if (UserInfo.INSTANCE.getUserName().getValue() != "" && total_data!= null)
            lineChartDataUpdateForYear();
        historyTextUpdate();
        lineChart.invalidate();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (UserInfo.INSTANCE.getUserName().getValue() != "")
            LoginDataSource.getYearWater();
    }
}