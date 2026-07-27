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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FoodJournalActivity extends AppCompatActivity {

    private CardView btnHeaderCalendar, btnSelectDate, btnAddFoodLong;
    private ImageView btnPrevDay, btnNextDay;
    private TextView tvDateDisplay, tvTotalCalo, tvCaloPercent;
    private ProgressBar pbCaloGoal;
    private LinearLayout layoutEmptyState, layoutDataState, menuHome, menuJournal;
    private FloatingActionButton fabAdd;

    private final int targetCalo = 2000;
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_journal);

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
        btnHeaderCalendar = findViewById(R.id.btnHeaderCalendar);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);
        tvDateDisplay = findViewById(R.id.tvDateDisplay);
        tvTotalCalo = findViewById(R.id.tvTotalCalo);

        pbCaloGoal = findViewById(R.id.pbCaloGoal);
        tvCaloPercent = findViewById(R.id.tvCaloPercent);

        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutDataState = findViewById(R.id.layoutDataState);
        btnAddFoodLong = findViewById(R.id.btnAddFoodLong);

        menuHome = findViewById(R.id.menuHome);
        menuJournal = findViewById(R.id.menuJournal);
    }

    private void setupEvents() {
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

        if (btnAddFoodLong != null) {
            btnAddFoodLong.setOnClickListener(v -> {
                Intent intent = new Intent(FoodJournalActivity.this, AddFoodActivity.class);
                startActivity(intent);
            });
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(FoodJournalActivity.this, AddFoodActivity.class);
                startActivity(intent);
            });
        }

        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                Intent intent = new Intent(FoodJournalActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (menuJournal != null) {
            menuJournal.setOnClickListener(v -> {
                Intent intent = new Intent(FoodJournalActivity.this, JournalActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void renderFoodEntries() {
        int totalCalo = AppDataManager.getInstance().getTotalCalo();

        if (tvTotalCalo != null) {
            tvTotalCalo.setText(String.format(Locale.US, "%,d", totalCalo));
        }

        if (pbCaloGoal != null) {
            pbCaloGoal.setMax(targetCalo);
            pbCaloGoal.setProgress(totalCalo);
        }

        if (tvCaloPercent != null) {
            int percent = (int) Math.round(((double) totalCalo / targetCalo) * 100);
            tvCaloPercent.setText(percent + "% mục tiêu calo");
        }

        List<AppDataManager.FoodEntry> entries = AppDataManager.getInstance().getFoodEntries();

        if (entries == null || entries.isEmpty()) {
            if (layoutDataState != null) layoutDataState.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        if (layoutDataState != null) layoutDataState.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);

        int childCount = layoutDataState.getChildCount();
        if (childCount > 1) {
            layoutDataState.removeViews(1, childCount - 1);
        }

        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            AppDataManager.FoodEntry food = entries.get(i);

            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 24);
            cardView.setLayoutParams(cardParams);
            cardView.setCardBackgroundColor(Color.parseColor("#1D1A38"));
            cardView.setRadius(28f);

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(24, 24, 24, 24);

            TextView tvIcon = new TextView(this);
            tvIcon.setId(View.generateViewId());
            tvIcon.setText(food.icon);
            tvIcon.setTextSize(22);
            tvIcon.setGravity(Gravity.CENTER);
            tvIcon.setBackgroundColor(Color.parseColor("#2A2845"));
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(110, 110);
            row.addView(tvIcon, iconParams);

            LinearLayout textContainer = new LinearLayout(this);
            textContainer.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.RIGHT_OF, tvIcon.getId());
            textParams.setMargins(24, 0, 0, 0);

            TextView tvName = new TextView(this);
            tvName.setText(food.mealType + " • " + food.name);
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(15);
            tvName.setTypeface(null, Typeface.BOLD);

            TextView tvGram = new TextView(this);
            tvGram.setText(food.weightGram + " g");
            tvGram.setTextColor(Color.parseColor("#8E8B9E"));

            textContainer.addView(tvName);
            textContainer.addView(tvGram);
            row.addView(textContainer, textParams);

            TextView tvCalo = new TextView(this);
            tvCalo.setText(food.calo + " kcal");
            tvCalo.setTextColor(Color.WHITE);
            tvCalo.setTextSize(14);
            tvCalo.setTypeface(null, Typeface.BOLD);
            RelativeLayout.LayoutParams caloParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            caloParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            caloParams.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvCalo, caloParams);

            cardView.addView(row);

            cardView.setOnClickListener(v -> showDeleteDialog(index, food));

            layoutDataState.addView(cardView);
        }
    }

    private void showDeleteDialog(int index, AppDataManager.FoodEntry food) {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Xóa món ăn")
                .setMessage("Bạn có muốn xóa món \"" + food.name + "\" khỏi nhật ký không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDataManager.getInstance().removeFoodEntry(index);
                    Toast.makeText(this, "Đã xóa " + food.name, Toast.LENGTH_SHORT).show();
                    renderFoodEntries();
                })
                .setNegativeButton("Hủy", null)
                .show();
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

        renderFoodEntries();
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