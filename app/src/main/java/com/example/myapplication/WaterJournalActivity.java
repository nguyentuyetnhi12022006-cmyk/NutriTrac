package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WaterJournalActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTodayWater, tvWaterStatus, tvInputDateSelect;
    private ProgressBar pbWaterGoal;
    private EditText edtWaterInput;
    private View btnSaveWater;
    private LinearLayout layoutWaterListContainer;

    private Calendar inputCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_journal);

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTodayWater = findViewById(R.id.tvTodayWater);
        tvWaterStatus = findViewById(R.id.tvWaterStatus);
        pbWaterGoal = findViewById(R.id.pbWaterGoal);

        tvInputDateSelect = findViewById(R.id.tvInputDateSelect);
        edtWaterInput = findViewById(R.id.edtWaterInput);
        btnSaveWater = findViewById(R.id.btnSaveWater);

        layoutWaterListContainer = findViewById(R.id.layoutWaterListContainer);

        updateInputDateDisplay();
    }

    private void updateInputDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String selectedDateKey = sdf.format(inputCalendar.getTime());

        if (tvInputDateSelect != null) {
            tvInputDateSelect.setText(selectedDateKey);
        }

        // Tải lượng nước tạm thời cho ngày được chọn
        String tempOldKey = AppDataManager.getInstance().getSelectedDateKey();
        AppDataManager.getInstance().setSelectedDateKey(selectedDateKey);
        int existingWater = AppDataManager.getInstance().getWaterForSelectedDate();
        AppDataManager.getInstance().setSelectedDateKey(tempOldKey); // Trả lại key cũ tránh đè Trang chủ

        if (edtWaterInput != null) {
            if (existingWater > 0) {
                edtWaterInput.setText(String.valueOf(existingWater));
            } else {
                edtWaterInput.setText("");
            }
        }
    }

    private void setupEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View btnSelectDateContainer = findViewById(R.id.btnSelectDateContainer);
        if (btnSelectDateContainer != null) {
            btnSelectDateContainer.setOnClickListener(v -> showDatePicker());
        } else if (tvInputDateSelect != null) {
            tvInputDateSelect.setOnClickListener(v -> showDatePicker());
        }

        // NÚT LƯU / CẬP NHẬT LƯỢNG NƯỚC
        if (btnSaveWater != null) {
            btnSaveWater.setOnClickListener(v -> {
                String input = edtWaterInput.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập lượng nước (ml)!", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int water = Integer.parseInt(input);
                    if (water < 0 || water > 10000) {
                        Toast.makeText(this, "Lượng nước không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateKey = sdf.format(inputCalendar.getTime());

                    // Lưu theo dateKey riêng mà không ảnh hưởng key đang active của trang chủ
                    String currentActiveKey = AppDataManager.getInstance().getSelectedDateKey();
                    AppDataManager.getInstance().setSelectedDateKey(dateKey);
                    AppDataManager.getInstance().setWaterForSelectedDate(water);
                    AppDataManager.getInstance().setSelectedDateKey(currentActiveKey);

                    Toast.makeText(this, "Đã cập nhật " + water + " ml cho ngày " + dateKey, Toast.LENGTH_SHORT).show();
                    refreshUI();
                } catch (Exception e) {
                    Toast.makeText(this, "Vui lòng nhập số hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void refreshUI() {
        // 1. Cập nhật thẻ Tổng quan cho Ngày được chọn trong lịch
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String selectedDateKey = sdf.format(inputCalendar.getTime());

        String currentActiveKey = AppDataManager.getInstance().getSelectedDateKey();
        AppDataManager.getInstance().setSelectedDateKey(selectedDateKey);
        int currentWater = AppDataManager.getInstance().getWaterForSelectedDate();
        AppDataManager.getInstance().setSelectedDateKey(currentActiveKey);

        if (tvTodayWater != null) {
            tvTodayWater.setText(String.format(Locale.US, "%,d", currentWater));
        }

        if (pbWaterGoal != null) {
            pbWaterGoal.setProgress(Math.min(2000, currentWater));
        }

        if (tvWaterStatus != null) {
            if (currentWater >= 2000) {
                tvWaterStatus.setText("🎉 Đã đạt mục tiêu 2L!");
                tvWaterStatus.setTextColor(Color.parseColor("#10B981"));
            } else {
                int remain = 2000 - currentWater;
                tvWaterStatus.setText("Còn thiếu " + String.format(Locale.US, "%,d", remain) + " ml");
                tvWaterStatus.setTextColor(Color.parseColor("#FFCC00"));
            }
        }

        // 2. Render danh sách lịch sử chuẩn layout
        renderWaterHistoryList();
    }

    private void renderWaterHistoryList() {
        if (layoutWaterListContainer == null) return;
        layoutWaterListContainer.removeAllViews();

        List<String> dateList = AppDataManager.getInstance().getAllWaterDates();

        if (dateList == null || dateList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có lịch sử nước uống nào");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(14);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 30, 0, 30);
            layoutWaterListContainer.addView(tvEmpty);
            return;
        }

        CardView card = new CardView(this);
        card.setCardBackgroundColor(Color.parseColor("#1D1A38"));
        card.setRadius(16f);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(16, 8, 16, 8);

        SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));

        int count = 0;
        String currentActiveKey = AppDataManager.getInstance().getSelectedDateKey();

        for (int i = dateList.size() - 1; i >= 0; i--) {
            String dateKey = dateList.get(i);

            AppDataManager.getInstance().setSelectedDateKey(dateKey);
            int water = AppDataManager.getInstance().getWaterForSelectedDate();

            if (water <= 0) continue; // Bỏ qua ngày chưa nhập nước
            count++;

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(12, 20, 12, 20);

            // --- CỤM BÊN TRÁI: Icon giọt nước + Ngày trắng đậm + Thứ xám đầy đủ ---
            LinearLayout leftGroup = new LinearLayout(this);
            leftGroup.setOrientation(LinearLayout.HORIZONTAL);
            leftGroup.setGravity(Gravity.CENTER_VERTICAL);
            RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            leftParams.addRule(RelativeLayout.CENTER_VERTICAL);

            // 1. Icon giọt nước
            TextView tvIcon = new TextView(this);
            tvIcon.setText("💧");
            tvIcon.setTextSize(18);
            tvIcon.setPadding(0, 0, 12, 0);

            // 2. Ngày tháng (Màu trắng, đậm)
            TextView tvDate = new TextView(this);
            tvDate.setText(dateKey);
            tvDate.setTextColor(Color.WHITE);
            tvDate.setTextSize(16);
            tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDate.setPadding(0, 0, 16, 0);

            // 3. Thứ đầy đủ (Màu xám nhạt: "Thứ Hai", "Thứ Ba"...)
            TextView tvDayOfWeek = new TextView(this);
            String dayOfWeekStr = "";
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdfInput.parse(dateKey));
                dayOfWeekStr = sdfDayOfWeek.format(cal.getTime());
                if (!dayOfWeekStr.isEmpty()) {
                    dayOfWeekStr = dayOfWeekStr.substring(0, 1).toUpperCase() + dayOfWeekStr.substring(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            tvDayOfWeek.setText(dayOfWeekStr);
            tvDayOfWeek.setTextColor(Color.parseColor("#8E8B9E"));
            tvDayOfWeek.setTextSize(15);

            leftGroup.addView(tvIcon);
            leftGroup.addView(tvDate);
            leftGroup.addView(tvDayOfWeek);
            row.addView(leftGroup, leftParams);

            // --- CỤM BÊN PHẢI: Số ml nước ---
            TextView tvWaterVal = new TextView(this);
            tvWaterVal.setText(String.format(Locale.US, "%,d ml  ›", water));
            tvWaterVal.setTextColor(Color.parseColor("#00BFFF"));
            tvWaterVal.setTextSize(15);
            tvWaterVal.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams valParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            valParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            valParams.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvWaterVal, valParams);

            // Bấm vào dòng để Sửa hoặc Xóa
            row.setOnClickListener(v -> showEditDeleteDialog(dateKey, water));

            listContainer.addView(row);

            if (i > 0) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divParams.setMargins(8, 0, 8, 0);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(Color.parseColor("#2A2845"));
                listContainer.addView(divider);
            }
        }

        AppDataManager.getInstance().setSelectedDateKey(currentActiveKey);

        if (count == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có lịch sử nước uống nào");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(14);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 30, 0, 30);
            layoutWaterListContainer.addView(tvEmpty);
            return;
        }

        card.addView(listContainer);
        layoutWaterListContainer.addView(card);
    }

    private void showEditDeleteDialog(String dateKey, int currentWater) {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Lịch sử ngày " + dateKey)
                .setMessage("Bạn muốn chỉnh sửa hay xóa bản ghi nước uống (" + currentWater + " ml) này?")
                .setPositiveButton("Sửa", (dialog, which) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        inputCalendar.setTime(sdf.parse(dateKey));
                        updateInputDateDisplay();
                        refreshUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Xóa", (dialog, which) -> {
                    String currentActiveKey = AppDataManager.getInstance().getSelectedDateKey();
                    AppDataManager.getInstance().setSelectedDateKey(dateKey);
                    AppDataManager.getInstance().setWaterForSelectedDate(0); // Đặt về 0 = Xóa
                    AppDataManager.getInstance().setSelectedDateKey(currentActiveKey);

                    Toast.makeText(this, "Đã xóa bản ghi ngày " + dateKey, Toast.LENGTH_SHORT).show();
                    refreshUI();
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void showDatePicker() {
        int year = inputCalendar.get(Calendar.YEAR);
        int month = inputCalendar.get(Calendar.MONTH);
        int day = inputCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                AlertDialog.THEME_HOLO_DARK,
                (view, selectedYear, selectedMonth, dayOfMonth) -> {
                    inputCalendar.set(Calendar.YEAR, selectedYear);
                    inputCalendar.set(Calendar.MONTH, selectedMonth);
                    inputCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    updateInputDateDisplay();
                    refreshUI();
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
                    .setTextColor(Color.parseColor("#00BFFF"));
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