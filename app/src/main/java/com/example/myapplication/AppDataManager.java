package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AppDataManager {
    private static AppDataManager instance;

    public static class FoodItem {
        public String name, icon;
        public int caloPer100g;
        public double proteinPer100g, carbPer100g, fatPer100g;

        public FoodItem(String name, String icon, int calo, double protein, double carb, double fat) {
            this.name = name; this.icon = icon; this.caloPer100g = calo;
            this.proteinPer100g = protein; this.carbPer100g = carb; this.fatPer100g = fat;
        }
    }

    public static class FoodEntry {
        public String name, icon, mealType;
        public int weightGram, gram, calo;
        public double protein, carb, fat;

        public FoodEntry(String name, String icon, String mealType, int weightGram, int calo, double protein, double carb, double fat) {
            this.name = name; this.icon = icon; this.mealType = mealType;
            this.weightGram = weightGram; this.gram = weightGram; this.calo = calo;
            this.protein = protein; this.carb = carb; this.fat = fat;
        }
    }

    public static class WorkoutItem {
        public String name, icon;
        public int caloPerMinute;

        public WorkoutItem(String name, String icon, int caloPerMinute) {
            this.name = name; this.icon = icon; this.caloPerMinute = caloPerMinute;
        }
    }

    public static class WorkoutEntry {
        public String name, icon;
        public int minutes, caloBurned;

        public WorkoutEntry(String name, String icon, int minutes, int caloBurned) {
            this.name = name; this.icon = icon; this.minutes = minutes; this.caloBurned = caloBurned;
        }
    }

    public static class WeightEntry {
        public String dateKey;
        public float weight;

        public WeightEntry(String dateKey, float weight) {
            this.dateKey = dateKey; this.weight = weight;
        }
    }

    private final List<FoodItem> sampleFoods = new ArrayList<>();
    private final List<WorkoutItem> sampleWorkouts = new ArrayList<>();

    private final Map<String, List<FoodEntry>> dailyFoodEntries = new HashMap<>();
    private final Map<String, Integer> dailyWaterMap = new HashMap<>();
    private final Map<String, List<WorkoutEntry>> workoutMap = new HashMap<>();
    private final Map<String, Float> dailyWeightMap = new HashMap<>();

    private String selectedDateKey = getCurrentTodayKey();

    private AppDataManager() {
        initSampleFoods();
        initSampleWorkouts();
        initDefaultTodayData();
    }

    public static synchronized AppDataManager getInstance() {
        if (instance == null) instance = new AppDataManager();
        return instance;
    }

    private void initSampleFoods() {
        sampleFoods.add(new FoodItem("Phở gà", "🍜", 150, 8.3, 15.0, 5.0));
        sampleFoods.add(new FoodItem("Phở bò", "🍜", 160, 9.0, 16.0, 5.5));
        sampleFoods.add(new FoodItem("Cơm tấm sườn", "🍱", 210, 8.5, 28.0, 7.0));
        sampleFoods.add(new FoodItem("Cơm gà nướng", "🍱", 180, 10.0, 22.0, 6.0));
        sampleFoods.add(new FoodItem("Bún chả", "🥗", 175, 7.5, 20.0, 6.5));
        sampleFoods.add(new FoodItem("Bánh mì thịt", "🥖", 250, 9.0, 32.0, 9.0));
        sampleFoods.add(new FoodItem("Gỏi cuốn", "🌯", 110, 6.0, 14.0, 2.0));
        sampleFoods.add(new FoodItem("Salad rau củ", "🥗", 85, 3.0, 8.0, 2.5));
        sampleFoods.add(new FoodItem("Ức gà luộc", "🍗", 165, 31.0, 0.0, 3.6));
        sampleFoods.add(new FoodItem("Trứng chiên", "🍳", 154, 12.5, 0.8, 11.0));
        sampleFoods.add(new FoodItem("Sữa tươi", "🥛", 62, 3.2, 4.8, 3.5));
        sampleFoods.add(new FoodItem("Cơm trắng", "🍚", 130, 2.7, 28.0, 0.3));
        sampleFoods.add(new FoodItem("Táo tây", "🍎", 52, 0.3, 14.0, 0.2));
        sampleFoods.add(new FoodItem("Chuối chín", "🍌", 89, 1.1, 23.0, 0.3));
        sampleFoods.add(new FoodItem("Bún bò Huế", "🍜", 170, 8.0, 18.0, 6.0));
    }

    private void initSampleWorkouts() {
        sampleWorkouts.add(new WorkoutItem("Chạy bộ", "🏃", 10));
        sampleWorkouts.add(new WorkoutItem("Đi bộ", "🚶", 5));
        sampleWorkouts.add(new WorkoutItem("Cầu lông", "🏸", 8));
        sampleWorkouts.add(new WorkoutItem("Đạp xe", "🚴", 7));
        sampleWorkouts.add(new WorkoutItem("Bơi lội", "🏊", 9));
        sampleWorkouts.add(new WorkoutItem("Tập Gym", "🏋️", 6));
        sampleWorkouts.add(new WorkoutItem("Nhảy dây", "🪢", 12));
    }

    private void initDefaultTodayData() {
        dailyFoodEntries.put(selectedDateKey, new ArrayList<>());
        dailyWaterMap.put(selectedDateKey, 0);
        workoutMap.put(selectedDateKey, new ArrayList<>());
    }

    public static String getCurrentTodayKey() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    public List<FoodItem> getSampleFoods() { return sampleFoods; }
    public List<WorkoutItem> getSampleWorkouts() { return sampleWorkouts; }

    public void setSelectedDateKey(String dateKey) { this.selectedDateKey = dateKey; }
    public String getSelectedDateKey() { return selectedDateKey; }

    // --- QUẢN LÝ NƯỚC UỐNG ---
    public int getWaterForSelectedDate() {
        if (!dailyWaterMap.containsKey(selectedDateKey)) dailyWaterMap.put(selectedDateKey, 0);
        return dailyWaterMap.get(selectedDateKey);
    }
    public void setWaterForSelectedDate(int ml) { dailyWaterMap.put(selectedDateKey, Math.max(0, ml)); }

    public List<String> getAllWaterDates() {
        Set<String> allDates = new HashSet<>(dailyWaterMap.keySet());
        allDates.addAll(dailyWeightMap.keySet());
        allDates.add(getCurrentTodayKey());

        List<String> list = new ArrayList<>(allDates);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Collections.sort(list, (d1, d2) -> {
            try {
                return sdf.parse(d1).compareTo(sdf.parse(d2));
            } catch (Exception e) {
                return d1.compareTo(d2);
            }
        });
        return list;
    }

    // --- QUẢN LÝ THỨC ĂN ---
    public List<FoodEntry> getFoodEntries() {
        if (!dailyFoodEntries.containsKey(selectedDateKey)) dailyFoodEntries.put(selectedDateKey, new ArrayList<>());
        return dailyFoodEntries.get(selectedDateKey);
    }
    public void addFoodEntry(FoodEntry entry) { getFoodEntries().add(entry); }
    public void removeFoodEntry(int index) {
        List<FoodEntry> list = getFoodEntries();
        if (index >= 0 && index < list.size()) list.remove(index);
    }
    public int getTotalCalo() {
        int sum = 0;
        for (FoodEntry f : getFoodEntries()) sum += f.calo;
        return sum;
    }
    public double getTotalProtein() { double sum = 0; for (FoodEntry f : getFoodEntries()) sum += f.protein; return sum; }
    public double getTotalCarb() { double sum = 0; for (FoodEntry f : getFoodEntries()) sum += f.carb; return sum; }
    public double getTotalFat() { double sum = 0; for (FoodEntry f : getFoodEntries()) sum += f.fat; return sum; }

    // --- QUẢN LÝ VẬN ĐỘNG ---
    public List<WorkoutEntry> getWorkoutEntries() {
        if (!workoutMap.containsKey(selectedDateKey)) workoutMap.put(selectedDateKey, new ArrayList<>());
        return workoutMap.get(selectedDateKey);
    }
    public void addWorkoutEntry(WorkoutEntry entry) { getWorkoutEntries().add(entry); }
    public void removeWorkoutEntry(int index) {
        List<WorkoutEntry> list = getWorkoutEntries();
        if (index >= 0 && index < list.size()) list.remove(index);
    }
    public int getTotalBurnedCalo() {
        int total = 0;
        for (WorkoutEntry item : getWorkoutEntries()) total += item.caloBurned;
        return total;
    }
    public int getTotalWorkoutMinutes() {
        int total = 0;
        for (WorkoutEntry item : getWorkoutEntries()) total += item.minutes;
        return total;
    }

    // --- QUẢN LÝ CÂN NẶNG ---
    public void setWeightForSelectedDate(float weight) {
        dailyWeightMap.put(selectedDateKey, weight);
    }

    public float getWeightForSelectedDate() {
        if (dailyWeightMap.containsKey(selectedDateKey)) return dailyWeightMap.get(selectedDateKey);
        return 0f;
    }

    // Lấy cân nặng hiệu lực cho ngày đang chọn (nếu ngày đó chưa nhập, lấy cân nặng gần nhất trước đó)
    public float getEffectiveWeightForDate(String dateKey) {
        if (dailyWeightMap.containsKey(dateKey)) {
            return dailyWeightMap.get(dateKey);
        }
        List<WeightEntry> all = getAllWeightEntries();
        float lastW = 0f;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            long targetTime = sdf.parse(dateKey).getTime();
            for (WeightEntry e : all) {
                if (sdf.parse(e.dateKey).getTime() <= targetTime) {
                    lastW = e.weight;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lastW == 0f && !all.isEmpty()) {
            lastW = all.get(0).weight;
        }
        return lastW;
    }

    // Lấy ngày cập nhật cân nặng gần nhất tính đến ngày đang chọn
    public String getEffectiveWeightDateKey(String dateKey) {
        if (dailyWeightMap.containsKey(dateKey)) {
            return dateKey;
        }
        List<WeightEntry> all = getAllWeightEntries();
        String lastKey = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            long targetTime = sdf.parse(dateKey).getTime();
            for (WeightEntry e : all) {
                if (sdf.parse(e.dateKey).getTime() <= targetTime) {
                    lastKey = e.dateKey;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lastKey.isEmpty() && !all.isEmpty()) {
            lastKey = all.get(0).dateKey;
        }
        return lastKey.isEmpty() ? dateKey : lastKey;
    }

    public float getLatestWeight() {
        List<WeightEntry> list = getAllWeightEntries();
        if (!list.isEmpty()) return list.get(list.size() - 1).weight;
        return 0f;
    }

    public String getLatestWeightDateKey() {
        List<WeightEntry> list = getAllWeightEntries();
        if (!list.isEmpty()) return list.get(list.size() - 1).dateKey;
        return getCurrentTodayKey();
    }

    public List<WeightEntry> getAllWeightEntries() {
        List<WeightEntry> list = new ArrayList<>();
        for (Map.Entry<String, Float> entry : dailyWeightMap.entrySet()) {
            list.add(new WeightEntry(entry.getKey(), entry.getValue()));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Collections.sort(list, (o1, o2) -> {
            try {
                return sdf.parse(o1.dateKey).compareTo(sdf.parse(o2.dateKey));
            } catch (Exception e) {
                return o1.dateKey.compareTo(o2.dateKey);
            }
        });
        return list;
    }

    public List<WeightEntry> getLast7WeightEntries() {
        List<WeightEntry> all = getAllWeightEntries();
        if (all.size() <= 7) return all;
        return all.subList(all.size() - 7, all.size());
    }

    public void removeWeightEntry(String dateKey) {
        dailyWeightMap.remove(dateKey);
    }
}