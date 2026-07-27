package com.example.myapplication;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddWorkoutActivity extends AppCompatActivity {

    private ImageView btnBack;
    private AutoCompleteTextView edtSearchWorkout;
    private TextView tvWorkoutIcon, tvWorkoutName, tvCaloPerMin, tvCalcBurnedCalo, tvWorkoutLevel;
    private EditText tvMinutesVal;
    private TextView btnMinusTime, btnPlusTime;
    private View btnSaveWorkout;

    private int currentMinutes = 30;
    private AppDataManager.WorkoutItem selectedWorkout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        initViews();
        setupSearchDropdown();
        setupEvents();

        // Ban đầu chưa chọn hoạt động
        resetToDefault();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearchWorkout = findViewById(R.id.edtSearchWorkout);

        tvWorkoutIcon = findViewById(R.id.tvWorkoutIcon);
        tvWorkoutName = findViewById(R.id.tvWorkoutName);
        tvCaloPerMin = findViewById(R.id.tvCaloPerMin);

        btnMinusTime = findViewById(R.id.btnMinusTime);
        btnPlusTime = findViewById(R.id.btnPlusTime);
        tvMinutesVal = findViewById(R.id.tvMinutesVal);

        tvCalcBurnedCalo = findViewById(R.id.tvCalcBurnedCalo);
        tvWorkoutLevel = findViewById(R.id.tvWorkoutLevel);
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
    }

    private void resetToDefault() {
        selectedWorkout = null;
        if (tvWorkoutIcon != null) tvWorkoutIcon.setText("🏋️");
        if (tvWorkoutName != null) tvWorkoutName.setText("Chưa chọn hoạt động");
        if (tvCaloPerMin != null) tvCaloPerMin.setText("0 kcal / phút");
        if (tvCalcBurnedCalo != null) tvCalcBurnedCalo.setText("0 kcal");
        if (tvWorkoutLevel != null) tvWorkoutLevel.setText("Chưa chọn");
    }

    private void setupSearchDropdown() {
        if (edtSearchWorkout == null) return;

        edtSearchWorkout.setDropDownBackgroundDrawable(new ColorDrawable(Color.parseColor("#1D1A38")));
        List<AppDataManager.WorkoutItem> list = AppDataManager.getInstance().getSampleWorkouts();
        List<String> names = new ArrayList<>();
        for (AppDataManager.WorkoutItem item : list) {
            names.add(item.icon + "  " + item.name + " (" + item.caloPerMinute + " kcal/phút)");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, names) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(14);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(14);
                }
                return view;
            }
        };

        edtSearchWorkout.setAdapter(adapter);

        edtSearchWorkout.setOnTouchListener((v, event) -> {
            edtSearchWorkout.showDropDown();
            return false;
        });

        edtSearchWorkout.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            for (AppDataManager.WorkoutItem item : list) {
                if (selectedText.contains(item.name)) {
                    selectWorkoutItem(item);
                    break;
                }
            }
        });

        edtSearchWorkout.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    resetToDefault();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void selectWorkoutItem(AppDataManager.WorkoutItem item) {
        selectedWorkout = item;
        if (tvWorkoutIcon != null) tvWorkoutIcon.setText(item.icon);
        if (tvWorkoutName != null) tvWorkoutName.setText(item.name);
        if (tvCaloPerMin != null) tvCaloPerMin.setText(item.caloPerMinute + " kcal / phút");
        calculateCalo();
    }

    private void setupEvents() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Nút Trừ (-) 5 phút
        if (btnMinusTime != null) {
            btnMinusTime.setOnClickListener(v -> {
                if (currentMinutes > 5) {
                    currentMinutes -= 5;
                    if (tvMinutesVal != null) tvMinutesVal.setText(String.valueOf(currentMinutes));
                    calculateCalo();
                }
            });
        }

        // Nút Cộng (+) 5 phút
        if (btnPlusTime != null) {
            btnPlusTime.setOnClickListener(v -> {
                currentMinutes += 5;
                if (tvMinutesVal != null) tvMinutesVal.setText(String.valueOf(currentMinutes));
                calculateCalo();
            });
        }

        // Tự động tính calo khi gõ số phút
        if (tvMinutesVal != null) {
            tvMinutesVal.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        if (s.length() > 0) {
                            currentMinutes = Integer.parseInt(s.toString());
                            calculateCalo();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Nút Lưu vào nhật ký
        if (btnSaveWorkout != null) {
            btnSaveWorkout.setOnClickListener(v -> {
                if (selectedWorkout == null) {
                    Toast.makeText(this, "Vui lòng chọn 1 hoạt động trước khi lưu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int totalCalo = selectedWorkout.caloPerMinute * currentMinutes;
                AppDataManager.getInstance().addWorkoutEntry(
                        new AppDataManager.WorkoutEntry(selectedWorkout.name, selectedWorkout.icon, currentMinutes, totalCalo)
                );
                Toast.makeText(this, "Đã lưu " + selectedWorkout.name + " (" + currentMinutes + " phút)", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void calculateCalo() {
        if (selectedWorkout != null) {
            int total = selectedWorkout.caloPerMinute * currentMinutes;
            if (tvCalcBurnedCalo != null) tvCalcBurnedCalo.setText(total + " kcal");

            if (tvWorkoutLevel != null) {
                if (currentMinutes < 20) {
                    tvWorkoutLevel.setText("Nhẹ nhàng");
                } else if (currentMinutes <= 45) {
                    tvWorkoutLevel.setText("Trung bình");
                } else {
                    tvWorkoutLevel.setText("Cường độ cao");
                }
            }
        } else {
            if (tvCalcBurnedCalo != null) tvCalcBurnedCalo.setText("0 kcal");
            if (tvWorkoutLevel != null) tvWorkoutLevel.setText("Chưa chọn");
        }
    }
}