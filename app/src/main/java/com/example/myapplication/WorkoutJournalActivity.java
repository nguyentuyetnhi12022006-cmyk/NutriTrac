package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkoutJournalActivity extends AppCompatActivity {

    private ImageView btnBack, btnPrevDay, btnNextDay;
    private CardView btnHeaderCalendar, btnSelectDate, btnAddWorkoutLong;
    private TextView tvDateDisplay, tvTotalBurnedCalo, tvTotalWorkoutTime;
    private LinearLayout menuHome, menuJournal, menuAnalytics, menuProfile;

    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_journal);

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncCalendarFromManager();
        checkAndStyleForSelectedDate();
    }

    private void syncCalendarFromManager() {
        try {
            String selectedKey = AppDataManager.getInstance().getSelectedDateKey();
            if (selectedKey != null && !selectedKey.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(selectedKey));
                currentCalendar = cal;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHeaderCalendar = findViewById(R.id.btnHeaderCalendar);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);
        tvDateDisplay = findViewById(R.id.tvDateDisplay);

        tvTotalBurnedCalo = findViewById(R.id.tvTotalBurnedCalo);
        tvTotalWorkoutTime = findViewById(R.id.tvTotalWorkoutTime);
        btnAddWorkoutLong = findViewById(R.id.btnAddWorkoutLong);

        menuHome = findViewById(R.id.menuHome);
        menuJournal = findViewById(R.id.menuJournal);
        menuAnalytics = findViewById(R.id.menuAnalytics);
        menuProfile = findViewById(R.id.menuProfile);
    }

    private void setupEvents() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnPrevDay != null) {
            btnPrevDay.setOnClickListener(v -> {
                currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
                checkAndStyleForSelectedDate();
            });
        }

        if (btnNextDay != null) {
            btnNextDay.setOnClickListener(v -> {
                currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
                checkAndStyleForSelectedDate();
            });
        }

        View.OnClickListener openCalendarListener = v -> showDatePicker();
        if (btnSelectDate != null) btnSelectDate.setOnClickListener(openCalendarListener);
        if (btnHeaderCalendar != null) btnHeaderCalendar.setOnClickListener(openCalendarListener);

        if (btnAddWorkoutLong != null) {
            btnAddWorkoutLong.setOnClickListener(v -> {
                Intent intent = new Intent(WorkoutJournalActivity.this, AddWorkoutActivity.class);
                startActivity(intent);
            });
        }

        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                Intent intent = new Intent(WorkoutJournalActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (menuJournal != null) {
            menuJournal.setOnClickListener(v -> {
                Intent intent = new Intent(WorkoutJournalActivity.this, JournalActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (menuAnalytics != null) {
            menuAnalytics.setOnClickListener(v -> Toast.makeText(this, "Tính năng Thống kê", Toast.LENGTH_SHORT).show());
        }

        if (menuProfile != null) {
            menuProfile.setOnClickListener(v -> Toast.makeText(this, "Tính năng Hồ sơ", Toast.LENGTH_SHORT).show());
        }
    }

    private void checkAndStyleForSelectedDate() {
        Calendar today = Calendar.getInstance();

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar selected = (Calendar) currentCalendar.clone();
        selected.set(Calendar.HOUR_OF_DAY, 0);
        selected.set(Calendar.MINUTE, 0);
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdfDisplay.format(currentCalendar.getTime());

        AppDataManager.getInstance().setSelectedDateKey(dateStr);

        if (tvDateDisplay != null) {
            if (selected.equals(today)) {
                tvDateDisplay.setText("Hôm nay, " + dateStr);
            } else {
                tvDateDisplay.setText(dateStr);
            }
        }

        renderWorkoutList();
    }

    private void renderWorkoutList() {
        int totalBurned = AppDataManager.getInstance().getTotalBurnedCalo();
        int totalTime = AppDataManager.getInstance().getTotalWorkoutMinutes();

        if (tvTotalBurnedCalo != null) tvTotalBurnedCalo.setText(String.valueOf(totalBurned));
        if (tvTotalWorkoutTime != null) tvTotalWorkoutTime.setText(String.valueOf(totalTime));

        List<AppDataManager.WorkoutEntry> list = AppDataManager.getInstance().getWorkoutEntries();
        LinearLayout container = findViewById(R.id.layoutWorkoutListContainer);

        if (container == null) return;
        container.removeAllViews();

        if (list == null || list.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Hoạt động chưa cập nhật");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(16);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 50, 0, 50);
            container.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            AppDataManager.WorkoutEntry item = list.get(i);

            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);
            card.setCardBackgroundColor(Color.parseColor("#1D1A38"));
            card.setRadius(16f);

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(20, 20, 20, 20);

            TextView tvIcon = new TextView(this);
            tvIcon.setId(View.generateViewId());
            tvIcon.setText(item.icon);
            tvIcon.setTextSize(20);
            tvIcon.setGravity(Gravity.CENTER);
            tvIcon.setBackgroundColor(Color.parseColor("#2A2845"));
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(100, 110);
            row.addView(tvIcon, iconParams);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.RIGHT_OF, tvIcon.getId());
            textParams.setMargins(20, 0, 0, 0);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(item.name);
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(15);
            tvTitle.setTypeface(null, Typeface.BOLD);

            // CƯỜNG ĐỘ VẬN ĐỘNG DỰA TÍNH THEO PHÚT
            String level;
            if (item.minutes < 20) {
                level = "Nhẹ nhàng";
            } else if (item.minutes <= 45) {
                level = "Trung bình";
            } else {
                level = "Cường độ cao";
            }

            TextView tvSub = new TextView(this);
            tvSub.setText(item.minutes + " phút • " + level);
            tvSub.setTextColor(Color.parseColor("#8E8B9E"));

            textLayout.addView(tvTitle);
            textLayout.addView(tvSub);
            row.addView(textLayout, textParams);

            TextView tvCalo = new TextView(this);
            tvCalo.setText(item.caloBurned + " kcal ›");
            tvCalo.setTextColor(Color.parseColor("#FF7043"));
            tvCalo.setTextSize(14);
            tvCalo.setTypeface(null, Typeface.BOLD);
            RelativeLayout.LayoutParams caloParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            caloParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            caloParams.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvCalo, caloParams);

            card.addView(row);

            card.setOnClickListener(v -> {
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                        .setTitle("Xóa bài tập")
                        .setMessage("Bạn có muốn xóa bài tập \"" + item.name + "\" không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            AppDataManager.getInstance().removeWorkoutEntry(index);
                            Toast.makeText(this, "Đã xóa " + item.name, Toast.LENGTH_SHORT).show();
                            renderWorkoutList(); // Vẽ lại danh sách và cập nhật tổng calo / phút
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            container.addView(card);
        }
    }

    private void showDatePicker() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                AlertDialog.THEME_HOLO_DARK,
                (view, selectedYear, selectedMonth, dayOfMonth) -> {
                    currentCalendar.set(Calendar.YEAR, selectedYear);
                    currentCalendar.set(Calendar.MONTH, selectedMonth);
                    currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    checkAndStyleForSelectedDate();
                },
                year, month, day
        );

        datePickerDialog.setOnShowListener(dialog -> {
            if (datePickerDialog.getWindow() != null) {
                datePickerDialog.getWindow().setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(Color.parseColor("#1D1A38"))
                );
            }

            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.parseColor("#A78BFA"));
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.parseColor("#8E8B9E"));

            DatePicker datePicker = datePickerDialog.getDatePicker();
            if (datePicker != null) {
                datePicker.setBackgroundColor(Color.parseColor("#1D1A38"));
            }
        });

        datePickerDialog.show();
    }
}