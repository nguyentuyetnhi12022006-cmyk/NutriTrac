package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView btnCalendar;
    private TextView txtHeaderDate, btnAddWorkout, btnMinusWater, btnAddWater, tvWaterVal, tvWorkoutVal;
    private ProgressBar pbWater, pbWorkout;
    private FloatingActionButton fabAdd;

    private TextView tvCaloIntake, tvCaloRemaining, tvCaloBurned;
    private TextView tvProteinVal, tvCarbVal, tvFatVal;
    private ProgressBar pbProtein, pbCarb, pbFat;
    private ArcProgressBar arcProgressBar;

    private LinearLayout layoutFoodListContainer;
    private LinearLayout layoutWeekDaysContainer;
    private TextView btnPrevWeek, btnNextWeek, tvCurrentWeekLabel;

    private Calendar currentWeekStart = Calendar.getInstance();
    private Calendar selectedCalendar = Calendar.getInstance();

    // Lưu lại tuần đang xem để khi chuyển tab qua lại không bị reset về tuần hiện tại
    private static Calendar savedWeekStart = null;

    private int currentWater = 0;
    private final int targetCalo = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedWeekStart != null) {
            currentWeekStart = (Calendar) savedWeekStart.clone();
        } else {
            selectedCalendar = Calendar.getInstance();
            setupWeekStartFromDate(selectedCalendar);
        }

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndStyleForSelectedDate();
    }

    private void setupWeekStartFromDate(Calendar cal) {
        currentWeekStart = (Calendar) cal.clone();
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);

        int dayOfWeek = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        int diff = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        currentWeekStart.add(Calendar.DAY_OF_MONTH, diff);
    }

    private void initViews() {
        btnCalendar = findViewById(R.id.btnCalendar);
        txtHeaderDate = findViewById(R.id.txtHeaderDate);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        tvWorkoutVal = findViewById(R.id.tvWorkoutVal);
        pbWorkout = findViewById(R.id.pbWorkout);

        btnMinusWater = findViewById(R.id.btnMinusWater);
        btnAddWater = findViewById(R.id.btnAddWater);
        tvWaterVal = findViewById(R.id.tvWaterVal);
        pbWater = findViewById(R.id.pbWater);

        arcProgressBar = findViewById(R.id.arcProgressBar);
        tvCaloIntake = findViewById(R.id.tvCaloIntake);
        tvCaloRemaining = findViewById(R.id.tvCaloRemaining);
        tvCaloBurned = findViewById(R.id.tvCaloBurned);

        tvProteinVal = findViewById(R.id.tvProteinVal);
        tvCarbVal = findViewById(R.id.tvCarbVal);
        tvFatVal = findViewById(R.id.tvFatVal);

        pbProtein = findViewById(R.id.pbProtein);
        pbCarb = findViewById(R.id.pbCarb);
        pbFat = findViewById(R.id.pbFat);

        layoutFoodListContainer = findViewById(R.id.layoutFoodListContainer);
        layoutWeekDaysContainer = findViewById(R.id.layoutWeekDaysContainer);
        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnNextWeek = findViewById(R.id.btnNextWeek);
        tvCurrentWeekLabel = findViewById(R.id.tvCurrentWeekLabel);

        buildWeekBar();
    }

    private void setupEvents() {
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> showDatePicker());
        }

        // Sự kiện bấm vào tab Trang chủ dưới cùng để reset về ngày hôm nay
        View menuHome = findViewById(R.id.menuHome);
        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                selectedCalendar = Calendar.getInstance();
                setupWeekStartFromDate(selectedCalendar);
                checkAndStyleForSelectedDate();
                buildWeekBar();
            });
        }

        // Sự kiện bấm vào tab Thống kê dưới cùng để chuyển sang AnalyticsActivity
        View menuAnalytics = findViewById(R.id.menuAnalytics);
        if (menuAnalytics != null) {
            menuAnalytics.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AnalyticsActivity.class);
                startActivity(intent);
            });
        }

        if (btnPrevWeek != null) {
            btnPrevWeek.setOnClickListener(v -> {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
                buildWeekBar();
            });
        }

        if (btnNextWeek != null) {
            btnNextWeek.setOnClickListener(v -> {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
                buildWeekBar();
            });
        }

        if (btnAddWorkout != null) {
            btnAddWorkout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddWorkoutActivity.class);
                startActivity(intent);
            });
        }

        if (btnAddWater != null) {
            btnAddWater.setOnClickListener(v -> {
                currentWater += 200;
                AppDataManager.getInstance().setWaterForSelectedDate(currentWater);
                updateWaterUI();
            });
        }

        if (btnMinusWater != null) {
            btnMinusWater.setOnClickListener(v -> {
                if (currentWater >= 200) {
                    currentWater -= 200;
                } else {
                    currentWater = 0;
                }
                AppDataManager.getInstance().setWaterForSelectedDate(currentWater);
                updateWaterUI();
            });
        }

        if (tvWaterVal != null) {
            tvWaterVal.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WaterJournalActivity.class);
                startActivity(intent);
            });
        }
        TextView tvWaterTitle = findViewById(R.id.tvWaterTitle);
        if (tvWaterTitle != null) {
            tvWaterTitle.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WaterJournalActivity.class);
                startActivity(intent);
            });
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
                startActivity(intent);
            });
        }

        View menuJournal = findViewById(R.id.menuJournal);
        if (menuJournal != null) {
            menuJournal.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, JournalActivity.class);
                startActivity(intent);
            });
        }

        TextView tvSeeAllFood = findViewById(R.id.tvSeeAllFood);
        if (tvSeeAllFood != null) {
            tvSeeAllFood.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, FoodJournalActivity.class);
                startActivity(intent);
            });
        }

        TextView tvSeeAllWorkout = findViewById(R.id.tvSeeAllWorkout);
        if (tvSeeAllWorkout != null) {
            tvSeeAllWorkout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WorkoutJournalActivity.class);
                startActivity(intent);
            });
        }

        TextView tvSeeMore = findViewById(R.id.tvSeeMore);
        if (tvSeeMore != null) {
            tvSeeMore.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WeightJournalActivity.class);
                startActivity(intent);
            });
        }
    }

    private void buildWeekBar() {
        if (layoutWeekDaysContainer == null) return;
        layoutWeekDaysContainer.removeAllViews();

        savedWeekStart = (Calendar) currentWeekStart.clone();

        SimpleDateFormat sdfLabel = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat sdfKey = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Calendar tempCal = (Calendar) currentWeekStart.clone();
        if (tvCurrentWeekLabel != null) {
            String startStr = sdfLabel.format(tempCal.getTime());
            Calendar endCal = (Calendar) tempCal.clone();
            endCal.add(Calendar.DAY_OF_MONTH, 6);
            tvCurrentWeekLabel.setText(startStr + " - " + sdfLabel.format(endCal.getTime()));
        }

        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        for (int i = 0; i < 7; i++) {
            final Calendar dayCal = (Calendar) tempCal.clone();
            String dateKey = sdfKey.format(dayCal.getTime());

            boolean isSelected = isSameDay(dayCal, selectedCalendar);

            LinearLayout dayCell = new LinearLayout(this);
            dayCell.setOrientation(LinearLayout.VERTICAL);
            dayCell.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            dayCell.setLayoutParams(cellParams);
            dayCell.setPadding(2, 6, 2, 6);

            LinearLayout circleContainer = new LinearLayout(this);
            circleContainer.setGravity(Gravity.CENTER);
            circleContainer.setOrientation(LinearLayout.VERTICAL);
            int sizePx = (int) (36 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(sizePx, sizePx);
            circleContainer.setLayoutParams(circleParams);



            TextView tvDayName = new TextView(this);
            tvDayName.setText(dayNames[i]);
            tvDayName.setTextSize(13);
            tvDayName.setGravity(Gravity.CENTER);

            if (isSelected) {
                tvDayName.setTextColor(Color.WHITE);
                tvDayName.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvDayName.setTextColor(Color.parseColor("#8E8B9E"));
                tvDayName.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            circleContainer.addView(tvDayName);

            View dot = new View(this);
            String oldKey = AppDataManager.getInstance().getSelectedDateKey();
            AppDataManager.getInstance().setSelectedDateKey(dateKey);
            boolean dayHasData = AppDataManager.getInstance().getWaterForSelectedDate() > 0 ||
                    !AppDataManager.getInstance().getFoodEntries().isEmpty() ||
                    !AppDataManager.getInstance().getWorkoutEntries().isEmpty();
            AppDataManager.getInstance().setSelectedDateKey(oldKey);

            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(5, 5);
            dotParams.setMargins(0, 3, 0, 0);
            dot.setLayoutParams(dotParams);
            if (dayHasData && isSelected) {
                dot.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else if (dayHasData) {
                dot.setBackgroundColor(Color.parseColor("#A78BFA"));
            } else {
                dot.setBackgroundColor(Color.TRANSPARENT);
            }

            dayCell.addView(circleContainer);
            dayCell.addView(dot);

            dayCell.setOnClickListener(v -> {
                selectedCalendar = (Calendar) dayCal.clone();
                checkAndStyleForSelectedDate();
                buildWeekBar();
            });

            layoutWeekDaysContainer.addView(dayCell);
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void checkAndStyleForSelectedDate() {
        SimpleDateFormat sdfKey = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateKey = sdfKey.format(selectedCalendar.getTime());

        AppDataManager.getInstance().setSelectedDateKey(dateKey);

        Calendar today = Calendar.getInstance();
        if (txtHeaderDate != null) {
            if (isSameDay(selectedCalendar, today)) {
                txtHeaderDate.setText("Hôm nay, " + dateKey);
            } else {
                txtHeaderDate.setText("Ngày " + selectedCalendar.get(Calendar.DAY_OF_MONTH) + " Thg " + (selectedCalendar.get(Calendar.MONTH) + 1));
            }
        }

        currentWater = AppDataManager.getInstance().getWaterForSelectedDate();
        updateWaterUI();

        updateDashboardUI();
        renderFoodList();
        renderWorkoutList();
    }

    private void updateDashboardUI() {
        AppDataManager manager = AppDataManager.getInstance();
        String dateKey = manager.getSelectedDateKey();

        int totalCaloIntake = manager.getTotalCalo();
        int caloBurned = manager.getTotalBurnedCalo();
        int remainingCalo = targetCalo - totalCaloIntake + caloBurned;

        if (tvCaloIntake != null) {
            tvCaloIntake.setText(String.format(Locale.US, "%,d", totalCaloIntake));
        }
        if (tvCaloRemaining != null) {
            tvCaloRemaining.setText(String.format(Locale.US, "%,d", Math.max(0, remainingCalo)));
        }
        if (tvCaloBurned != null) {
            tvCaloBurned.setText(String.format(Locale.US, "%,d", caloBurned));
        }

        if (arcProgressBar != null) {
            arcProgressBar.setProgress(totalCaloIntake, targetCalo);
        }

        double totalProtein = manager.getTotalProtein();
        double totalCarb = manager.getTotalCarb();
        double totalFat = manager.getTotalFat();

        if (tvProteinVal != null) tvProteinVal.setText(String.format(Locale.US, "%.0f / 120g", totalProtein));
        if (pbProtein != null) pbProtein.setProgress((int) totalProtein);

        if (tvCarbVal != null) tvCarbVal.setText(String.format(Locale.US, "%.0f / 250g", totalCarb));
        if (pbCarb != null) pbCarb.setProgress((int) totalCarb);

        if (tvFatVal != null) tvFatVal.setText(String.format(Locale.US, "%.0f / 60g", totalFat));
        if (pbFat != null) pbFat.setProgress((int) totalFat);

        int workoutMins = manager.getTotalWorkoutMinutes();
        if (tvWorkoutVal != null) {
            tvWorkoutVal.setText(workoutMins + " phút");
        }

        if (pbWorkout != null) {
            pbWorkout.setProgress(workoutMins);
            if (workoutMins == 0) {
                pbWorkout.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#2A2845")));
            } else if (workoutMins > 45) {
                pbWorkout.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF3B30")));
            } else {
                pbWorkout.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFCC00")));
            }
        }

        TextView tvWeightVal = findViewById(R.id.tvWeightVal);
        TextView tvWeightDiff = findViewById(R.id.tvWeightDiff);

        float effectiveWeight = manager.getEffectiveWeightForDate(dateKey);
        String effectiveDate = manager.getEffectiveWeightDateKey(dateKey);

        if (effectiveWeight > 0) {
            if (tvWeightVal != null) {
                tvWeightVal.setText(String.format(Locale.US, "%.1f kg", effectiveWeight));
            }
            if (tvWeightDiff != null) {
                tvWeightDiff.setText("Cập nhật ngày " + effectiveDate);
                tvWeightDiff.setTextColor(Color.parseColor("#8E8B9E"));
            }
        } else {
            if (tvWeightVal != null) tvWeightVal.setText("-- kg");
            if (tvWeightDiff != null) {
                tvWeightDiff.setText("Chưa có dữ liệu cân nặng");
                tvWeightDiff.setTextColor(Color.parseColor("#8E8B9E"));
            }
        }

        TextView tvDate1 = findViewById(R.id.tvChartDate1);
        TextView tvDate2 = findViewById(R.id.tvChartDate2);
        TextView tvDate3 = findViewById(R.id.tvChartDate3);
        TextView tvDate4 = findViewById(R.id.tvChartDate4);

        List<AppDataManager.WeightEntry> weightList = manager.getAllWeightEntries();
        if (weightList != null && !weightList.isEmpty()) {
            int total = weightList.size();
            if (tvDate1 != null) tvDate1.setText(getShortDate(weightList, 0));
            if (tvDate2 != null) tvDate2.setText(getShortDate(weightList, total / 3));
            if (tvDate3 != null) tvDate3.setText(getShortDate(weightList, (total * 2) / 3));
            if (tvDate4 != null) tvDate4.setText(getShortDate(weightList, total - 1));
        }

        MiniLineChart miniChart = findViewById(R.id.weightMiniChart);
        if (miniChart != null) {
            miniChart.updateChartData();
        }
    }

    private String getShortDate(List<AppDataManager.WeightEntry> list, int index) {
        if (index >= 0 && index < list.size()) {
            String key = list.get(index).dateKey;
            if (key != null && key.length() >= 5) {
                return key.substring(0, 5);
            }
            return key;
        }
        return "--";
    }

    private void renderFoodList() {
        if (layoutFoodListContainer == null) return;
        layoutFoodListContainer.removeAllViews();

        List<AppDataManager.FoodEntry> list = AppDataManager.getInstance().getFoodEntries();

        if (list == null || list.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có món ăn nào trong ngày");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(14);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 20, 0, 20);
            layoutFoodListContainer.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            AppDataManager.FoodEntry item = list.get(i);

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(0, 16, 0, 16);

            TextView tvIcon = new TextView(this);
            tvIcon.setId(View.generateViewId());
            tvIcon.setText(item.icon);
            tvIcon.setTextSize(20);
            tvIcon.setGravity(Gravity.CENTER);
            tvIcon.setBackgroundColor(Color.parseColor("#2A2845"));
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(110, 110);
            row.addView(tvIcon, iconParams);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.RIGHT_OF, tvIcon.getId());
            textParams.setMargins(24, 0, 0, 0);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(item.mealType + " • " + item.name);
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSub = new TextView(this);
            tvSub.setText(item.weightGram + "g | P: " + (int)item.protein + "g C: " + (int)item.carb + "g F: " + (int)item.fat + "g");
            tvSub.setTextColor(Color.parseColor("#8E8B9E"));
            tvSub.setTextSize(11);

            textLayout.addView(tvTitle);
            textLayout.addView(tvSub);
            row.addView(textLayout, textParams);

            TextView tvCalo = new TextView(this);
            tvCalo.setText(item.calo + " kcal ›");
            tvCalo.setTextColor(Color.parseColor("#10B981"));
            tvCalo.setTextSize(14);
            tvCalo.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams caloParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            caloParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            caloParams.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvCalo, caloParams);

            row.setOnClickListener(v -> showDeleteDialog(index, item));
            layoutFoodListContainer.addView(row);
        }
    }

    private void renderWorkoutList() {
        LinearLayout container = findViewById(R.id.layoutMainWorkoutListContainer);
        if (container == null) return;
        container.removeAllViews();

        List<AppDataManager.WorkoutEntry> workoutList = AppDataManager.getInstance().getWorkoutEntries();

        if (workoutList == null || workoutList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có bài tập nào trong ngày");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(14);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 20, 0, 20);
            container.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < workoutList.size(); i++) {
            final int index = i;
            AppDataManager.WorkoutEntry item = workoutList.get(i);

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(0, 16, 0, 16);

            TextView tvIcon = new TextView(this);
            tvIcon.setId(View.generateViewId());
            tvIcon.setText(item.icon);
            tvIcon.setTextSize(20);
            tvIcon.setGravity(Gravity.CENTER);
            tvIcon.setBackgroundColor(Color.parseColor("#2A2845"));
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(110, 110);
            row.addView(tvIcon, iconParams);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.RIGHT_OF, tvIcon.getId());
            textParams.setMargins(24, 0, 0, 0);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(item.name);
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSub = new TextView(this);
            tvSub.setText(item.minutes + " phút");
            tvSub.setTextColor(Color.parseColor("#8E8B9E"));
            tvSub.setTextSize(12);

            textLayout.addView(tvTitle);
            textLayout.addView(tvSub);
            row.addView(textLayout, textParams);

            TextView tvCalo = new TextView(this);
            tvCalo.setText(item.caloBurned + " kcal ›");
            tvCalo.setTextColor(Color.parseColor("#FF7043"));
            tvCalo.setTextSize(14);
            tvCalo.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams caloParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            caloParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            caloParams.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvCalo, caloParams);

            row.setOnClickListener(v -> showDeleteWorkoutDialog(index, item));
            container.addView(row);
        }
    }

    private void showDeleteDialog(int index, AppDataManager.FoodEntry food) {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Xóa món ăn")
                .setMessage("Bạn có muốn xóa món \"" + food.name + "\" khỏi nhật ký không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDataManager.getInstance().removeFoodEntry(index);
                    Toast.makeText(this, "Đã xóa " + food.name, Toast.LENGTH_SHORT).show();
                    updateDashboardUI();
                    renderFoodList();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteWorkoutDialog(int index, AppDataManager.WorkoutEntry workout) {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Xóa vận động")
                .setMessage("Bạn có muốn xóa bài tập \"" + workout.name + "\" khỏi nhật ký không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDataManager.getInstance().removeWorkoutEntry(index);
                    Toast.makeText(this, "Đã xóa " + workout.name, Toast.LENGTH_SHORT).show();
                    updateDashboardUI();
                    renderWorkoutList();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateWaterUI() {
        if (tvWaterVal != null) {
            tvWaterVal.setText(String.format(Locale.US, "%,d / 2,000 ml", currentWater));
        }
        if (pbWater != null) {
            pbWater.setProgress(currentWater);
        }
    }

    private void showDatePicker() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                AlertDialog.THEME_HOLO_DARK,
                (view, selectedYear, selectedMonth, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, selectedYear);
                    selectedCalendar.set(Calendar.MONTH, selectedMonth);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    setupWeekStartFromDate(selectedCalendar);
                    checkAndStyleForSelectedDate();
                    buildWeekBar();
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    public static class ArcProgressBar extends View {
        private Paint bgPaint, progressPaint;
        private RectF rectF;
        private float progress = 0f;
        private float maxProgress = 2000f;

        public ArcProgressBar(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            rectF = new RectF();

            bgPaint = new Paint();
            bgPaint.setAntiAlias(true);
            bgPaint.setColor(Color.parseColor("#2A2845"));
            bgPaint.setStyle(Paint.Style.STROKE);
            bgPaint.setStrokeWidth(36f);
            bgPaint.setStrokeCap(Paint.Cap.ROUND);

            progressPaint = new Paint();
            progressPaint.setAntiAlias(true);
            progressPaint.setStyle(Paint.Style.STROKE);
            progressPaint.setStrokeWidth(36f);
            progressPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        public void setProgress(float current, float max) {
            this.progress = current;
            this.maxProgress = max;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float padding = 36f;
            rectF.set(padding, padding, getWidth() - padding, getHeight() * 2 - padding);

            Shader shader = new LinearGradient(
                    0, 0, getWidth(), 0,
                    Color.parseColor("#A78BFA"),
                    Color.parseColor("#FF7043"),
                    Shader.TileMode.CLAMP
            );
            progressPaint.setShader(shader);

            canvas.drawArc(rectF, 180, 180, false, bgPaint);

            float sweepAngle = (progress / maxProgress) * 180f;
            if (sweepAngle > 180f) sweepAngle = 180f;
            canvas.drawArc(rectF, 180, sweepAngle, false, progressPaint);
        }
    }

    public static class MiniLineChart extends View {
        private Paint linePaint, pointPaint, pointHolePaint;
        private Path path;
        private float[] points = {0.6f, 0.4f, 0.5f, 0.3f, 0.6f, 0.4f, 0.2f};

        public MiniLineChart(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            linePaint = new Paint();
            linePaint.setAntiAlias(true);
            linePaint.setColor(Color.parseColor("#A78BFA"));
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(5f);

            pointPaint = new Paint();
            pointPaint.setAntiAlias(true);
            pointPaint.setColor(Color.parseColor("#A78BFA"));
            pointPaint.setStyle(Paint.Style.FILL);

            pointHolePaint = new Paint();
            pointHolePaint.setAntiAlias(true);
            pointHolePaint.setColor(Color.parseColor("#1D1A38"));
            pointHolePaint.setStyle(Paint.Style.FILL);

            path = new Path();
        }

        public void updateChartData() {
            List<AppDataManager.WeightEntry> list = AppDataManager.getInstance().getLast7WeightEntries();
            if (list != null && list.size() >= 2) {
                float min = list.get(0).weight;
                float max = list.get(0).weight;
                for (AppDataManager.WeightEntry e : list) {
                    if (e.weight < min) min = e.weight;
                    if (e.weight > max) max = e.weight;
                }
                if (min == max) { min -= 1; max += 1; }

                float[] newPoints = new float[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    float norm = (list.get(i).weight - min) / (max - min);
                    newPoints[i] = 0.85f - (norm * 0.7f);
                }
                this.points = newPoints;
            }
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getWidth() == 0 || getHeight() == 0 || points == null || points.length < 2) return;

            float width = getWidth();
            float height = getHeight();
            float stepX = width / (points.length - 1);

            path.reset();
            for (int i = 0; i < points.length; i++) {
                float x = i * stepX;
                float y = points[i] * height;
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }

            canvas.drawPath(path, linePaint);

            for (int i = 0; i < points.length; i++) {
                float x = i * stepX;
                float y = points[i] * height;
                canvas.drawCircle(x, y, 7f, pointPaint);
                canvas.drawCircle(x, y, 3.5f, pointHolePaint);
            }
        }
    }
}