package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddFoodActivity extends AppCompatActivity {

    private ImageView btnBack;
    private AutoCompleteTextView edtSearch;
    private TextView tvFoodIcon, tvFoodName, tvFoodBaseInfo;
    private TextView tvWeightGram, tvCalcCalo, tvCalcProtein, tvCalcCarb, tvCalcFat;
    private Button btnSang, btnTrua, btnToi, btnPhu, btnSave;
    private TextView btnMinus, btnPlus;

    // Biến quản lý trạng thái chọn
    private int currentGram = 200;
    private String selectedMeal = "Sáng";
    private AppDataManager.FoodItem selectedFoodItem = null; // Mặc định ban đầu chưa chọn món nào

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_entry);

        initViews();
        setupListeners();
        setupSearchDropdown();

        // Ban đầu hiển thị khung giao diện nhưng đặt các chỉ số = 0
        resetToDefaultState();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearch = findViewById(R.id.edtSearch);

        tvFoodIcon = findViewById(R.id.tvFoodIcon);
        tvFoodName = findViewById(R.id.tvFoodName);
        tvFoodBaseInfo = findViewById(R.id.tvFoodBaseInfo);

        tvWeightGram = findViewById(R.id.tvWeightGram);
        tvCalcCalo = findViewById(R.id.tvCalcCalo);
        tvCalcProtein = findViewById(R.id.tvCalcProtein);
        tvCalcCarb = findViewById(R.id.tvCalcCarb);
        tvCalcFat = findViewById(R.id.tvCalcFat);

        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);

        btnSang = findViewById(R.id.btnSang);
        btnTrua = findViewById(R.id.btnTrua);
        btnToi = findViewById(R.id.btnToi);
        btnPhu = findViewById(R.id.btnPhu);
        btnSave = findViewById(R.id.btnSave);
    }

    // Đặt về trạng thái mặc định: Giữ nguyên khung nhưng đưa chỉ số về 0
    private void resetToDefaultState() {
        selectedFoodItem = null;

        if (tvFoodIcon != null) tvFoodIcon.setText("🍽️");
        if (tvFoodName != null) tvFoodName.setText("Chưa chọn món ăn");
        if (tvFoodBaseInfo != null) tvFoodBaseInfo.setText("0 kcal / 100g");

        if (tvWeightGram != null) tvWeightGram.setText("200");
        currentGram = 200;

        if (tvCalcCalo != null) tvCalcCalo.setText("0 kcal");
        if (tvCalcProtein != null) tvCalcProtein.setText("0.0 g");
        if (tvCalcCarb != null) tvCalcCarb.setText("0.0 g");
        if (tvCalcFat != null) tvCalcFat.setText("0.0 g");
    }

    // TẢI DANH SÁCH 15 MÓN ĂN - ĐỔI MÀU TÍM NỔI BẬT + DẤU TÍCH KHI CHỌN
    private void setupSearchDropdown() {
        if (edtSearch == null) return;

        edtSearch.setDropDownBackgroundDrawable(new ColorDrawable(Color.parseColor("#1D1A38")));

        List<AppDataManager.FoodItem> sampleList = AppDataManager.getInstance().getSampleFoods();
        List<String> foodNames = new ArrayList<>();

        for (AppDataManager.FoodItem item : sampleList) {
            foodNames.add(item.icon + "  " + item.name + " (" + item.caloPer100g + " kcal/100g)");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                foodNames
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);

                String currentItemText = getItem(position);

                // KIỂM TRA MÓN ĐANG ĐƯỢC CHỌN -> TÔ MÀU TÍM NỔI BẬT + THÊM DẤU TÍCH ✔
                if (selectedFoodItem != null && currentItemText != null && currentItemText.contains(selectedFoodItem.name)) {
                    view.setBackgroundColor(Color.parseColor("#7C3AED")); // Đổi sang màu tím
                    if (tv != null) {
                        tv.setText("✔  " + currentItemText);
                        tv.setTextColor(Color.WHITE);
                        tv.setTextSize(14);
                        tv.setPadding(20, 24, 20, 24);
                    }
                } else {
                    view.setBackgroundColor(Color.parseColor("#1D1A38")); // Nền tối mặc định
                    if (tv != null) {
                        tv.setText(currentItemText);
                        tv.setTextColor(Color.parseColor("#E2E8F0"));
                        tv.setTextSize(14);
                        tv.setPadding(20, 24, 20, 24);
                    }
                }

                return view;
            }
        };

        edtSearch.setAdapter(adapter);

        // Chạm vào ô tìm kiếm là hiện ngay danh sách
        edtSearch.setOnTouchListener((v, event) -> {
            edtSearch.showDropDown();
            return false;
        });

        // KHI NGƯỜI DÙNG BẤM CHỌN MÓN
        edtSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            for (AppDataManager.FoodItem item : sampleList) {
                if (selectedText.contains(item.name)) {
                    selectFoodItem(item);
                    break;
                }
            }
        });

        // Theo dõi khi người dùng xóa hết chữ trong ô tìm kiếm -> Tự động đưa chỉ số về 0
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    resetToDefaultState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Cập nhật thông tin món được chọn và tính toán chỉ số
    private void selectFoodItem(AppDataManager.FoodItem item) {
        selectedFoodItem = item;

        // Cập nhật text ô tìm kiếm về tên món
        if (edtSearch != null) {
            edtSearch.setText(item.name, false);
        }

        // Điền dữ liệu vào thẻ
        if (tvFoodIcon != null) tvFoodIcon.setText(item.icon);
        if (tvFoodName != null) tvFoodName.setText(item.name);
        if (tvFoodBaseInfo != null) tvFoodBaseInfo.setText(item.caloPer100g + " kcal / 100g");

        calculateAndRender();
    }

    private void setupListeners() {
        // Nút Trở ra (Back) ở góc trên bên trái
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Tăng / Giảm gram
        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (currentGram >= 20) {
                    currentGram -= 10;
                    if (tvWeightGram != null) tvWeightGram.setText(String.valueOf(currentGram));
                    calculateAndRender();
                }
            });
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                currentGram += 10;
                if (tvWeightGram != null) tvWeightGram.setText(String.valueOf(currentGram));
                calculateAndRender();
            });
        }

        // Chọn Bữa ăn (Sáng, Trưa, Tối, Phụ)
        View.OnClickListener mealClickListener = v -> {
            resetMealButtons();
            Button clickedButton = (Button) v;
            clickedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7C3AED")));
            clickedButton.setTextColor(Color.WHITE);

            int id = v.getId();
            if (id == R.id.btnSang) selectedMeal = "Sáng";
            else if (id == R.id.btnTrua) selectedMeal = "Trưa";
            else if (id == R.id.btnToi) selectedMeal = "Tối";
            else if (id == R.id.btnPhu) selectedMeal = "Phụ";
        };

        if (btnSang != null) btnSang.setOnClickListener(mealClickListener);
        if (btnTrua != null) btnTrua.setOnClickListener(mealClickListener);
        if (btnToi != null) btnToi.setOnClickListener(mealClickListener);
        if (btnPhu != null) btnPhu.setOnClickListener(mealClickListener);

        // Nút Lưu
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveEntryAndReturn());
        }
    }

    private void resetMealButtons() {
        int darkBg = Color.parseColor("#1D1A38");
        int textGrey = Color.parseColor("#8E8B9E");

        if (btnSang != null) {
            btnSang.setBackgroundTintList(ColorStateList.valueOf(darkBg));
            btnSang.setTextColor(textGrey);
        }
        if (btnTrua != null) {
            btnTrua.setBackgroundTintList(ColorStateList.valueOf(darkBg));
            btnTrua.setTextColor(textGrey);
        }
        if (btnToi != null) {
            btnToi.setBackgroundTintList(ColorStateList.valueOf(darkBg));
            btnToi.setTextColor(textGrey);
        }
        if (btnPhu != null) {
            btnPhu.setBackgroundTintList(ColorStateList.valueOf(darkBg));
            btnPhu.setTextColor(textGrey);
        }
    }

    // Tính toán Calo/Protein/Carb/Fat theo số gram
    private void calculateAndRender() {
        if (selectedFoodItem == null) {
            if (tvCalcCalo != null) tvCalcCalo.setText("0 kcal");
            if (tvCalcProtein != null) tvCalcProtein.setText("0.0 g");
            if (tvCalcCarb != null) tvCalcCarb.setText("0.0 g");
            if (tvCalcFat != null) tvCalcFat.setText("0.0 g");
            return;
        }

        double ratio = currentGram / 100.0;
        int calo = (int) Math.round(selectedFoodItem.caloPer100g * ratio);
        double protein = selectedFoodItem.proteinPer100g * ratio;
        double carb = selectedFoodItem.carbPer100g * ratio;
        double fat = selectedFoodItem.fatPer100g * ratio;

        if (tvCalcCalo != null) tvCalcCalo.setText(calo + " kcal");
        if (tvCalcProtein != null) tvCalcProtein.setText(String.format(Locale.US, "%.1f g", protein));
        if (tvCalcCarb != null) tvCalcCarb.setText(String.format(Locale.US, "%.1f g", carb));
        if (tvCalcFat != null) tvCalcFat.setText(String.format(Locale.US, "%.1f g", fat));
    }

    // Lưu vào nhật ký theo ngày đang chọn hiện tại trong AppDataManager
    private void saveEntryAndReturn() {
        if (selectedFoodItem == null) {
            Toast.makeText(this, "Vui lòng chọn 1 món ăn trước khi lưu!", Toast.LENGTH_SHORT).show();
            return;
        }

        double ratio = currentGram / 100.0;
        int calo = (int) Math.round(selectedFoodItem.caloPer100g * ratio);
        double protein = selectedFoodItem.proteinPer100g * ratio;
        double carb = selectedFoodItem.carbPer100g * ratio;
        double fat = selectedFoodItem.fatPer100g * ratio;

        AppDataManager.FoodEntry newEntry = new AppDataManager.FoodEntry(
                selectedFoodItem.name,
                selectedFoodItem.icon,
                selectedMeal,
                currentGram,
                calo,
                protein,
                carb,
                fat
        );

        AppDataManager.getInstance().addFoodEntry(newEntry);

        Toast.makeText(this, "Đã thêm " + selectedFoodItem.name + " vào nhật ký!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}