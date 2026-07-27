package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.AttributeSet;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeightJournalActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCurrentWeight, tvTargetWeight, tvWeightDiffSub, tvRemainingGoal;
    private TextView tvMaxWeight, tvMinWeight, tvAvgWeight, tvDiffWeight;
    private TextView tvInputDateSelect;
    private EditText edtWeightInput;
    private View btnSaveWeight;
    private LinearLayout layoutChartContainer, layoutWeightListContainer;
    private ProgressBar pbWeightGoal;

    private TextView btnFilter7Days, btnFilter30Days, btnFilter3Months, btnFilter1Year;

    private Calendar inputCalendar = Calendar.getInstance();
    private int currentFilterDays = 7; // Mặc định 7 ngày

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_journal);

        initViews();
        setupEvents();
        refreshUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvTargetWeight = findViewById(R.id.tvTargetWeight);
        tvWeightDiffSub = findViewById(R.id.tvWeightDiffSub);
        tvRemainingGoal = findViewById(R.id.tvRemainingGoal);
        pbWeightGoal = findViewById(R.id.pbWeightGoal);

        tvMaxWeight = findViewById(R.id.tvMaxWeight);
        tvMinWeight = findViewById(R.id.tvMinWeight);
        tvAvgWeight = findViewById(R.id.tvAvgWeight);
        tvDiffWeight = findViewById(R.id.tvDiffWeight);

        btnFilter7Days = findViewById(R.id.btnFilter7Days);
        btnFilter30Days = findViewById(R.id.btnFilter30Days);
        btnFilter3Months = findViewById(R.id.btnFilter3Months);
        btnFilter1Year = findViewById(R.id.btnFilter1Year);

        tvInputDateSelect = findViewById(R.id.tvInputDateSelect);
        edtWeightInput = findViewById(R.id.edtWeightInput);
        btnSaveWeight = findViewById(R.id.btnSaveWeight);

        layoutChartContainer = findViewById(R.id.layoutChartContainer);
        layoutWeightListContainer = findViewById(R.id.layoutWeightListContainer);

        updateInputDateDisplay();
    }

    // Cập nhật hiển thị ngày chọn (Chỉ hiện chuỗi ngày thuần túy, không nối emoji)
    private void updateInputDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String selectedDateKey = sdf.format(inputCalendar.getTime());

        if (tvInputDateSelect != null) {
            tvInputDateSelect.setText(selectedDateKey);
        }

        // Tự động kiểm tra nếu ngày được chọn đã từng có cân nặng thì điền sẵn vào ô nhập
        AppDataManager.getInstance().setSelectedDateKey(selectedDateKey);
        float existingWeight = AppDataManager.getInstance().getWeightForSelectedDate();
        if (edtWeightInput != null) {
            if (existingWeight > 0) {
                edtWeightInput.setText(String.format(Locale.US, "%.1f", existingWeight));
            } else {
                edtWeightInput.setText("");
            }
        }
    }

    private void setupEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Bấm container chọn ngày hoặc text chọn ngày để mở DatePicker
        View btnSelectDateContainer = findViewById(R.id.btnSelectDateContainer);
        if (btnSelectDateContainer != null) {
            btnSelectDateContainer.setOnClickListener(v -> showDatePicker());
        } else if (tvInputDateSelect != null) {
            tvInputDateSelect.setOnClickListener(v -> showDatePicker());
        }

        // Bộ lọc thời gian
        if (btnFilter7Days != null) btnFilter7Days.setOnClickListener(v -> applyFilter(7, btnFilter7Days));
        if (btnFilter30Days != null) btnFilter30Days.setOnClickListener(v -> applyFilter(30, btnFilter30Days));
        if (btnFilter3Months != null) btnFilter3Months.setOnClickListener(v -> applyFilter(90, btnFilter3Months));
        if (btnFilter1Year != null) btnFilter1Year.setOnClickListener(v -> applyFilter(365, btnFilter1Year));

        // Nút Lưu cân nặng
        if (btnSaveWeight != null) {
            btnSaveWeight.setOnClickListener(v -> {
                String input = edtWeightInput.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập số cân nặng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    float weight = Float.parseFloat(input);
                    if (weight <= 0 || weight > 300) {
                        Toast.makeText(this, "Cân nặng không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateKey = sdf.format(inputCalendar.getTime());

                    AppDataManager.getInstance().setSelectedDateKey(dateKey);
                    AppDataManager.getInstance().setWeightForSelectedDate(weight);

                    Toast.makeText(this, "Đã lưu " + weight + " kg cho ngày " + dateKey, Toast.LENGTH_SHORT).show();
                    refreshUI();
                } catch (Exception e) {
                    Toast.makeText(this, "Vui lòng nhập số hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void applyFilter(int days, TextView activeBtn) {
        this.currentFilterDays = days;

        // Reset màu nút
        TextView[] btns = {btnFilter7Days, btnFilter30Days, btnFilter3Months, btnFilter1Year};
        for (TextView btn : btns) {
            if (btn != null) {
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(Color.parseColor("#8E8B9E"));
            }
        }

        if (activeBtn != null) {
            activeBtn.setBackgroundColor(Color.parseColor("#7C3AED"));
            activeBtn.setTextColor(Color.WHITE);
        }

        refreshUI();
    }

    private void refreshUI() {
        List<AppDataManager.WeightEntry> allList = AppDataManager.getInstance().getAllWeightEntries();

        // Lọc danh sách theo mốc thời gian đã chọn
        List<AppDataManager.WeightEntry> filteredList = new ArrayList<>();
        if (allList.size() <= currentFilterDays) {
            filteredList.addAll(allList);
        } else {
            filteredList.addAll(allList.subList(allList.size() - currentFilterDays, allList.size()));
        }

        // 1. Cập nhật thẻ Tổng quan
        float latest = AppDataManager.getInstance().getLatestWeight();
        if (tvCurrentWeight != null) {
            tvCurrentWeight.setText(latest > 0 ? String.format(Locale.US, "%.1f", latest) : "--");
        }

        float target = 45.0f;
        if (tvTargetWeight != null) tvTargetWeight.setText(String.format(Locale.US, "%.1f", target));

        if (latest > 0) {
            float remain = Math.max(0, latest - target);
            if (tvRemainingGoal != null) tvRemainingGoal.setText(String.format(Locale.US, "Còn %.1f kg", remain));
            if (pbWeightGoal != null) pbWeightGoal.setProgress((int) Math.min(100, (target / latest) * 100));
        }

        // 2. Cập nhật 4 ô Thống kê theo bộ lọc
        if (!filteredList.isEmpty()) {
            float max = filteredList.get(0).weight;
            float min = filteredList.get(0).weight;
            float sum = 0;

            for (AppDataManager.WeightEntry e : filteredList) {
                if (e.weight > max) max = e.weight;
                if (e.weight < min) min = e.weight;
                sum += e.weight;
            }

            float avg = sum / filteredList.size();
            float diff = filteredList.get(filteredList.size() - 1).weight - filteredList.get(0).weight;

            if (tvMaxWeight != null) tvMaxWeight.setText(String.format(Locale.US, "%.1f kg", max));
            if (tvMinWeight != null) tvMinWeight.setText(String.format(Locale.US, "%.1f kg", min));
            if (tvAvgWeight != null) tvAvgWeight.setText(String.format(Locale.US, "%.1f kg", avg));
            if (tvDiffWeight != null) {
                tvDiffWeight.setText(String.format(Locale.US, "%+.1f kg", diff));
                tvDiffWeight.setTextColor(diff <= 0 ? Color.parseColor("#10B981") : Color.parseColor("#FF3B30"));
            }
        }

        renderList(filteredList);
        renderChart(filteredList);
    }

    private void renderList(List<AppDataManager.WeightEntry> list) {
        if (layoutWeightListContainer == null) return;
        layoutWeightListContainer.removeAllViews();

        if (list == null || list.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có lịch sử cân nặng nào");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(14);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 30, 0, 30);
            layoutWeightListContainer.addView(tvEmpty);
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

        for (int i = list.size() - 1; i >= 0; i--) {
            AppDataManager.WeightEntry item = list.get(i);

            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(8, 20, 8, 20);

            // DÙNG ICON LỊCH HỆ THỐNG CÙNG MÀU TÍM (#A78BFA)
            ImageView imgIcon = new ImageView(this);
            imgIcon.setId(View.generateViewId());
            imgIcon.setImageResource(android.R.drawable.ic_menu_my_calendar);
            imgIcon.setColorFilter(Color.parseColor("#A78BFA"));
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(42, 42);
            iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(imgIcon, iconParams);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.HORIZONTAL);
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.RIGHT_OF, imgIcon.getId());
            textParams.addRule(RelativeLayout.CENTER_VERTICAL);
            textParams.setMargins(16, 0, 0, 0);

            TextView tvDate = new TextView(this);
            tvDate.setText(item.dateKey);
            tvDate.setTextColor(Color.WHITE);
            tvDate.setTextSize(14);
            tvDate.setTypeface(null, android.graphics.Typeface.BOLD);

            String dayOfWeekStr = "";
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdfInput.parse(item.dateKey));
                dayOfWeekStr = sdfDayOfWeek.format(cal.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

            TextView tvDayOfWeek = new TextView(this);
            tvDayOfWeek.setText("   " + dayOfWeekStr);
            tvDayOfWeek.setTextColor(Color.parseColor("#8E8B9E"));
            tvDayOfWeek.setTextSize(12);

            textLayout.addView(tvDate);
            textLayout.addView(tvDayOfWeek);
            row.addView(textLayout, textParams);

            TextView tvWeight = new TextView(this);
            tvWeight.setText(String.format(Locale.US, "%.1f kg  ›", item.weight));
            tvWeight.setTextColor(Color.WHITE);
            tvWeight.setTextSize(15);
            tvWeight.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams weightParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            weightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            weightParams.addRule(RelativeLayout.CENTER_VERTICAL);
            row.addView(tvWeight, weightParams);

            row.setOnClickListener(v -> {
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                        .setTitle("Xóa cân nặng")
                        .setMessage("Xóa bản ghi ngày " + item.dateKey + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            AppDataManager.getInstance().removeWeightEntry(item.dateKey);
                            refreshUI();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

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

        card.addView(listContainer);
        layoutWeightListContainer.addView(card);
    }

    private void renderChart(List<AppDataManager.WeightEntry> list) {
        if (layoutChartContainer == null) return;
        layoutChartContainer.removeAllViews();

        if (list == null || list.size() < 2) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Cần ít nhất 2 ngày dữ liệu để vẽ biểu đồ");
            tvEmpty.setTextColor(Color.parseColor("#8E8B9E"));
            tvEmpty.setTextSize(13);
            layoutChartContainer.addView(tvEmpty);
            return;
        }

        WeightGraphView graphView = new WeightGraphView(this, list);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        graphView.setLayoutParams(params);
        layoutChartContainer.addView(graphView);
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
                },
                year, month, day
        );

        // Đồng bộ màu sắc Bảng chọn lịch theo tông tím Dark Mode
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

    public static class WeightGraphView extends View {
        private final List<AppDataManager.WeightEntry> data;
        private Paint linePaint, pointPaint, fillPaint, textPaint;
        private Path linePath, fillPath;

        public WeightGraphView(Context context, List<AppDataManager.WeightEntry> data) {
            super(context);
            this.data = data;
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

            fillPaint = new Paint();
            fillPaint.setAntiAlias(true);
            fillPaint.setStyle(Paint.Style.FILL);

            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(24f);

            linePath = new Path();
            fillPath = new Path();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (data == null || data.size() < 2 || getWidth() == 0 || getHeight() == 0) return;

            float minW = data.get(0).weight;
            float maxW = data.get(0).weight;

            for (AppDataManager.WeightEntry e : data) {
                if (e.weight < minW) minW = e.weight;
                if (e.weight > maxW) maxW = e.weight;
            }

            if (minW == maxW) {
                minW -= 1;
                maxW += 1;
            }

            float paddingX = 60f;
            float paddingTop = 40f;
            float paddingBottom = 60f;

            float width = getWidth() - paddingX * 2;
            float height = getHeight() - paddingTop - paddingBottom;
            float stepX = width / (data.size() - 1);

            linePath.reset();
            fillPath.reset();

            fillPath.moveTo(paddingX, getHeight() - paddingBottom);

            for (int i = 0; i < data.size(); i++) {
                float x = paddingX + i * stepX;
                float normY = (data.get(i).weight - minW) / (maxW - minW);
                float y = (getHeight() - paddingBottom) - normY * height;

                if (i == 0) {
                    linePath.moveTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                }
                fillPath.lineTo(x, y);
            }

            fillPath.lineTo(paddingX + (data.size() - 1) * stepX, getHeight() - paddingBottom);
            fillPath.close();

            Shader shader = new LinearGradient(
                    0, 0, 0, getHeight(),
                    Color.parseColor("#4A7C3AED"),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            );
            fillPaint.setShader(shader);

            canvas.drawPath(fillPath, fillPaint);
            canvas.drawPath(linePath, linePaint);

            for (int i = 0; i < data.size(); i++) {
                float x = paddingX + i * stepX;
                float normY = (data.get(i).weight - minW) / (maxW - minW);
                float y = (getHeight() - paddingBottom) - normY * height;

                canvas.drawCircle(x, y, 8f, pointPaint);
                canvas.drawText(String.format(Locale.US, "%.1f", data.get(i).weight), x - 20, y - 14, textPaint);

                String dateSub = data.get(i).dateKey.length() >= 5 ? data.get(i).dateKey.substring(0, 5) : data.get(i).dateKey;
                canvas.drawText(dateSub, x - 22, getHeight() - 15, textPaint);
            }
        }
    }
}