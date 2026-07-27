package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class JournalActivity extends AppCompatActivity {

    private TextView tvJournalWaterVal, tvJournalWeightVal;
    private ProgressBar pbJournalWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void initViews() {
        tvJournalWaterVal = findViewById(R.id.tvJournalWaterVal);
        tvJournalWeightVal = findViewById(R.id.tvJournalWeightVal);
        pbJournalWater = findViewById(R.id.pbJournalWater);
    }

    private void setupEvents() {
        // 1. NÚT XEM CHI TIẾT 4 MỤC
        View btnFoodDetail = findViewById(R.id.btnFoodDetail);
        if (btnFoodDetail != null) {
            btnFoodDetail.setOnClickListener(v -> {
                Intent intent = new Intent(JournalActivity.this, FoodJournalActivity.class);
                startActivity(intent);
            });
        }

        View btnWorkoutDetail = findViewById(R.id.btnWorkoutDetail);
        if (btnWorkoutDetail != null) {
            btnWorkoutDetail.setOnClickListener(v -> {
                Intent intent = new Intent(JournalActivity.this, WorkoutJournalActivity.class);
                startActivity(intent);
            });
        }

        // ĐÃ KẾT NỐI: Bấm nút Xem chi tiết Nước -> Sang WaterJournalActivity
        View btnWaterDetail = findViewById(R.id.btnWaterDetail);
        if (btnWaterDetail != null) {
            btnWaterDetail.setOnClickListener(v -> {
                Intent intent = new Intent(JournalActivity.this, WaterJournalActivity.class);
                startActivity(intent);
            });
        }

        View btnWeightDetail = findViewById(R.id.btnWeightDetail);
        if (btnWeightDetail != null) {
            btnWeightDetail.setOnClickListener(v -> {
                Intent intent = new Intent(JournalActivity.this, WeightJournalActivity.class);
                startActivity(intent);
            });
        }

        // 2. BOTTOM NAVIGATION BAR
        View menuHome = findViewById(R.id.menuHome);
        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                Intent intent = new Intent(JournalActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View menuJournal = findViewById(R.id.menuJournal);
        if (menuJournal != null) {
            menuJournal.setOnClickListener(v -> {
                // Đang ở trang Nhật ký rồi nên không cần chuyển nữa
            });
        }
    }

    private void refreshUI() {
        AppDataManager manager = AppDataManager.getInstance();

        // Cập nhật thẻ Nước
        int water = manager.getWaterForSelectedDate();
        if (tvJournalWaterVal != null) {
            tvJournalWaterVal.setText(String.format(Locale.US, "%,d / 2,000 ml", water));
        }
        if (pbJournalWater != null) {
            pbJournalWater.setProgress(Math.min(2000, water));
        }

        // Cập nhật thẻ Cân nặng
        float weight = manager.getLatestWeight();
        String weightDate = manager.getLatestWeightDateKey();
        if (tvJournalWeightVal != null) {
            if (weight > 0) {
                tvJournalWeightVal.setText(String.format(Locale.US, "%.1f kg • %s", weight, weightDate));
            } else {
                tvJournalWeightVal.setText("Chưa có dữ liệu");
            }
        }
    }
}