package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tab7Days, tab30Days, tab3Months, tab1Year;
    private TextView tvDateRangeLabel, tvCaloAvgVal, tvWaterAvgVal;
    private TextView tvProteinAvgVal, tvCarbAvgVal, tvFatAvgVal;
    private TextView tvBmiVal, tvBmrVal, tvBodyStatus, tvLatestWeightVal;
    private ImageView btnCalendarAnalytics;
    private LinearLayout layoutChartContainer;

    private Calendar selectedCal = Calendar.getInstance();
    private String currentTab = "7 ngày";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_analytics);

        // Đồng bộ ngày đang chọn với trang chủ nếu có
        String currentKey = AppDataManager.getInstance().getSelectedDateKey();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            selectedCal.setTime(sdf.parse(currentKey));
        } catch (Exception e) {
            e.printStackTrace();
        }

        initViews();
        setupEvents();
        updateAnalyticsData();
    }

    private void initViews() {
        tab7Days = findViewById(R.id.tab7Days);
        tab30Days = findViewById(R.id.tab30Days);
        tab3Months = findViewById(R.id.tab3Months);
        tab1Year = findViewById(R.id.tab1Year);

        tvDateRangeLabel = findViewById(R.id.tvDateRangeLabel);
        tvCaloAvgVal = findViewById(R.id.tvCaloAvgVal);
        tvWaterAvgVal = findViewById(R.id.tvWaterAvgVal);

        tvProteinAvgVal = findViewById(R.id.tvProteinAvgVal);
        tvCarbAvgVal = findViewById(R.id.tvCarbAvgVal);
        tvFatAvgVal = findViewById(R.id.tvFatAvgVal);

        tvBmiVal = findViewById(R.id.tvBmiVal);
        tvBmrVal = findViewById(R.id.tvBmrVal);
        tvBodyStatus = findViewById(R.id.tvBodyStatus);
        tvLatestWeightVal = findViewById(R.id.tvLatestWeightVal);

        btnCalendarAnalytics = findViewById(R.id.btnCalendarAnalytics);
        layoutChartContainer = findViewById(R.id.layoutChartContainer);
    }

    private void setupEvents() {
        View.OnClickListener tabClickListener = v -> {
            tab7Days.setBackgroundColor(Color.TRANSPARENT);
            tab30Days.setBackgroundColor(Color.TRANSPARENT);
            tab3Months.setBackgroundColor(Color.TRANSPARENT);
            tab1Year.setBackgroundColor(Color.TRANSPARENT);

            tab7Days.setTextColor(Color.parseColor("#8E8B9E"));
            tab30Days.setTextColor(Color.parseColor("#8E8B9E"));
            tab3Months.setTextColor(Color.parseColor("#8E8B9E"));
            tab1Year.setTextColor(Color.parseColor("#8E8B9E"));

            TextView clicked = (TextView) v;
            clicked.setBackgroundColor(Color.parseColor("#7C3AED"));
            clicked.setTextColor(Color.WHITE);
            currentTab = clicked.getText().toString();
            updateAnalyticsData();
        };

        tab7Days.setOnClickListener(tabClickListener);
        tab30Days.setOnClickListener(tabClickListener);
        tab3Months.setOnClickListener(tabClickListener);
        tab1Year.setOnClickListener(tabClickListener);

        if (btnCalendarAnalytics != null) {
            btnCalendarAnalytics.setOnClickListener(v -> showDatePicker());
        }

        View menuHome = findViewById(R.id.menuHome);
        if (menuHome != null) {
            menuHome.setOnClickListener(v -> finish());
        }

        View menuJournal = findViewById(R.id.menuJournal);
        if (menuJournal != null) {
            menuJournal.setOnClickListener(v -> {
                startActivity(new Intent(AnalyticsActivity.this, JournalActivity.class));
                finish();
            });
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                AlertDialog.THEME_HOLO_DARK,
                (view, year, month, dayOfMonth) -> {
                    selectedCal.set(Calendar.YEAR, year);
                    selectedCal.set(Calendar.MONTH, month);
                    selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateAnalyticsData();
                },
                selectedCal.get(Calendar.YEAR),
                selectedCal.get(Calendar.MONTH),
                selectedCal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateAnalyticsData() {
        SimpleDateFormat sdfLabel = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfKey = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfShortDay = new SimpleDateFormat("dd/MM", Locale.getDefault());

        int rangeDays = 7;
        if (currentTab.equals("7 ngày")) {
            rangeDays = 7;
        } else if (currentTab.equals("30 ngày")) {
            rangeDays = 30;
        } else if (currentTab.equals("3 tháng")) {
            rangeDays = 90;
        } else if (currentTab.equals("1 năm")) {
            rangeDays = 365;
        }

        // Lấy khoảng thời gian đúng bằng số ngày gần nhất tính từ ngày đang chọn lùi về trước
        Calendar calEnd = (Calendar) selectedCal.clone();
        Calendar calStart = (Calendar) selectedCal.clone();
        calStart.add(Calendar.DAY_OF_MONTH, -(rangeDays - 1));

        tvDateRangeLabel.setText(sdfLabel.format(calStart.getTime()) + " - " + sdfLabel.format(calEnd.getTime()));

        List<String> targetDates = new ArrayList<>();
        Calendar temp = (Calendar) calStart.clone();
        while (!temp.after(calEnd)) {
            targetDates.add(sdfKey.format(temp.getTime()));
            temp.add(Calendar.DAY_OF_MONTH, 1);
        }

        AppDataManager manager = AppDataManager.getInstance();
        String currentSavedKey = manager.getSelectedDateKey();

        int daysCount = targetDates.size();
        int sumCaloIntake = 0;
        int sumWater = 0;
        double sumProtein = 0;
        double sumCarb = 0;
        double sumFat = 0;

        float[] dailyIntakes = new float[daysCount];
        float[] dailyBurned = new float[daysCount];

        for (int i = 0; i < daysCount; i++) {
            String key = targetDates.get(i);
            manager.setSelectedDateKey(key);

            int intake = manager.getTotalCalo();
            int burned = manager.getTotalBurnedCalo();
            int water = manager.getWaterForSelectedDate();
            double protein = manager.getTotalProtein();
            double carb = manager.getTotalCarb();
            double fat = manager.getTotalFat();

            sumCaloIntake += intake;
            sumWater += water;
            sumProtein += protein;
            sumCarb += carb;
            sumFat += fat;

            dailyIntakes[i] = intake;
            dailyBurned[i] = burned;
        }

        manager.setSelectedDateKey(currentSavedKey);

        int avgCalo = daysCount > 0 ? sumCaloIntake / daysCount : 0;
        int avgWater = daysCount > 0 ? sumWater / daysCount : 0;
        double avgProtein = daysCount > 0 ? sumProtein / daysCount : 0;
        double avgCarb = daysCount > 0 ? sumCarb / daysCount : 0;
        double avgFat = daysCount > 0 ? sumFat / daysCount : 0;

        tvCaloAvgVal.setText(avgCalo + " kcal");
        tvWaterAvgVal.setText(avgWater + " / 2,000 ml");
        tvProteinAvgVal.setText(String.format(Locale.US, "%.0fg / 120g", avgProtein));
        tvCarbAvgVal.setText(String.format(Locale.US, "%.0fg / 250g", avgCarb));
        tvFatAvgVal.setText(String.format(Locale.US, "%.0fg / 60g", avgFat));

        // Cập nhật biểu đồ cột Calo
        AnalyticsBarChart barChart = findViewById(R.id.barChartAnalytics);
        if (barChart != null) {
            if (rangeDays == 7) {
                String[] labels = new String[7];
                Calendar lCal = (Calendar) calStart.clone();
                for (int i = 0; i < 7; i++) {
                    labels[i] = sdfShortDay.format(lCal.getTime());
                    lCal.add(Calendar.DAY_OF_MONTH, 1);
                }
                barChart.setChartData(dailyIntakes, dailyBurned, labels);
            } else {
                int groupCount = rangeDays == 30 ? 6 : (rangeDays == 90 ? 6 : 12);
                float[] groupedIntakes = new float[groupCount];
                float[] groupedBurned = new float[groupCount];
                String[] groupLabels = new String[groupCount];

                int step = daysCount / groupCount;
                Calendar lCal = (Calendar) calStart.clone();

                for (int g = 0; g < groupCount; g++) {
                    int startIdx = g * step;
                    int endIdx = (g == groupCount - 1) ? daysCount : (g + 1) * step;
                    int countInGroup = Math.max(1, endIdx - startIdx);

                    float sumIn = 0, sumBurn = 0;
                    for (int k = startIdx; k < endIdx; k++) {
                        sumIn += dailyIntakes[k];
                        sumBurn += dailyBurned[k];
                    }
                    groupedIntakes[g] = sumIn / countInGroup;
                    groupedBurned[g] = sumBurn / countInGroup;
                    groupLabels[g] = sdfShortDay.format(lCal.getTime());

                    lCal.add(Calendar.DAY_OF_MONTH, step);
                }
                barChart.setChartData(groupedIntakes, groupedBurned, groupLabels);
            }
        }

        // Cân nặng & Chỉ số cơ thể
        float latestWeight = manager.getLatestWeight();
        if (latestWeight > 0) {
            tvLatestWeightVal.setText(String.format(Locale.US, "%.1f kg", latestWeight));

            float heightM = 1.62f;
            float bmi = latestWeight / (heightM * heightM);
            tvBmiVal.setText(String.format(Locale.US, "%.1f", bmi));

            String status = "Bình thường";
            if (bmi < 18.5) status = "Hơi gầy";
            else if (bmi >= 25 && bmi < 30) status = "Hơi mập";
            else if (bmi >= 30) status = "Béo phì";
            tvBodyStatus.setText(status);

            float bmr = (10 * latestWeight) + (6.25f * 162) - (5 * 22) - 161;
            tvBmrVal.setText(String.format(Locale.US, "%.0f kcal", bmr));
        } else {
            tvLatestWeightVal.setText("-- kg");
            tvBmiVal.setText("--");
            tvBmrVal.setText("--");
            tvBodyStatus.setText("Chưa có dữ liệu");
        }

        renderAnalyticsWeightChart(rangeDays);
    }

    private void renderAnalyticsWeightChart(int filterLimit) {
        if (layoutChartContainer == null) return;
        layoutChartContainer.removeAllViews();

        List<AppDataManager.WeightEntry> allWeights = AppDataManager.getInstance().getAllWeightEntries();
        List<AppDataManager.WeightEntry> filteredList = new ArrayList<>();
        if (allWeights.size() <= filterLimit) {
            filteredList.addAll(allWeights);
        } else {
            filteredList.addAll(allWeights.subList(allWeights.size() - filterLimit, allWeights.size()));
        }

        if (filteredList.size() < 2) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Cần ít nhất 2 ngày dữ liệu để vẽ biểu đồ");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(13);
            layoutChartContainer.addView(tvEmpty);
            return;
        }

        WeightJournalActivity.WeightGraphView graphView = new WeightJournalActivity.WeightGraphView(this, filteredList);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        graphView.setLayoutParams(params);
        layoutChartContainer.addView(graphView);
    }

    public static class AnalyticsBarChart extends View {
        private Paint paintIntake, paintBurned, paintText, paintEmpty;
        private float[] intakes = new float[0];
        private float[] burned = new float[0];
        private String[] labels = new String[0];
        private boolean hasData = false;

        public AnalyticsBarChart(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paintIntake = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintIntake.setColor(Color.parseColor("#A78BFA"));
            paintIntake.setStyle(Paint.Style.FILL);

            paintBurned = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintBurned.setColor(Color.parseColor("#FF7043"));
            paintBurned.setStyle(Paint.Style.FILL);

            paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.parseColor("#8E8B9E"));
            paintText.setTextSize(22f);
            paintText.setTextAlign(Paint.Align.CENTER);

            paintEmpty = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintEmpty.setColor(Color.parseColor("#8E8B9E"));
            paintEmpty.setTextSize(26f);
            paintEmpty.setTextAlign(Paint.Align.CENTER);
        }

        public void setChartData(float[] intakes, float[] burned, String[] labels) {
            this.intakes = intakes != null ? intakes : new float[0];
            this.burned = burned != null ? burned : new float[0];
            this.labels = labels != null ? labels : new String[0];

            this.hasData = false;
            for (float v : this.intakes) {
                if (v > 0) { hasData = true; break; }
            }
            if (!hasData) {
                for (float v : this.burned) {
                    if (v > 0) { hasData = true; break; }
                }
            }
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();
            if (width == 0 || height == 0) return;

            if (!hasData || intakes.length == 0) {
                canvas.drawText("Chưa có dữ liệu thống kê calo", width / 2f, height / 2f, paintEmpty);
                return;
            }

            int count = intakes.length;
            float groupWidth = width / count;
            float barWidth = groupWidth * 0.28f;
            float maxVal = 2500f;

            for (int i = 0; i < count; i++) {
                if (intakes[i] > maxVal) maxVal = intakes[i];
                if (burned[i] > maxVal) maxVal = burned[i];
            }

            for (int i = 0; i < count; i++) {
                float centerX = groupWidth * i + groupWidth / 2f;
                float intakeH = (intakes[i] / maxVal) * (height - 50f);
                float burnedH = (burned[i] / maxVal) * (height - 50f);

                float left1 = centerX - barWidth - 2f;
                float right1 = centerX - 2f;
                float top1 = height - intakeH - 35f;
                float bottom1 = height - 35f;
                if (intakes[i] > 0) {
                    canvas.drawRoundRect(new RectF(left1, top1, right1, bottom1), 6f, 6f, paintIntake);
                }

                float left2 = centerX + 2f;
                float right2 = centerX + barWidth + 2f;
                float top2 = height - burnedH - 35f;
                float bottom2 = height - 35f;
                if (burned[i] > 0) {
                    canvas.drawRoundRect(new RectF(left2, top2, right2, bottom2), 6f, 6f, paintBurned);
                }

                if (i < labels.length && labels[i] != null) {
                    canvas.drawText(labels[i], centerX, height - 8f, paintText);
                }
            }
        }
    }
}