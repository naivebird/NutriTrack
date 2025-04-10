package com.example.nutritrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutritrack.api.NutritionApi;
import com.example.nutritrack.api.RetrofitClient;
import com.example.nutritrack.data.Utils.DatabaseHelper;
import com.example.nutritrack.data.model.FoodItem;
import com.example.nutritrack.data.model.FoodResponse;
import com.example.nutritrack.data.model.Nutrient;
import com.example.nutritrack.data.model.UserProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingActivity extends AppCompatActivity {

    private EditText mealInput;
    private Button logButton;
    private Button clearButton;
    private TextView totalText, remainingText, mealList;

    private double calorieGoal;
    private double caloriesToday;

    private List<String> meals = new ArrayList<>();
    private static final String API_KEY = "pBJShpnyEA9T5o6MrCcvDx8eK4fGJeDrpumNc9ni";
    private String today;
    private DatabaseHelper dbHelper;
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tracking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mealInput = findViewById(R.id.meal_input);
        logButton = findViewById(R.id.log_button);
        clearButton = findViewById(R.id.clear_button);
        totalText = findViewById(R.id.total_calories);
        remainingText = findViewById(R.id.remaining_calories);
        mealList = findViewById(R.id.meal_list);


        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dbHelper = new DatabaseHelper(this);
        String userEmail = getSharedPreferences("nutritrack_prefs", MODE_PRIVATE)
                .getString("user_email", null);
        profile = dbHelper.getUserProfile(userEmail);
        calorieGoal = profile.getGoal();

        loadTrackedMeals(); // Load previously tracked meals

        logButton.setOnClickListener(v -> {
            String query = mealInput.getText().toString().trim();
            if (!query.isEmpty()) {
                fetchMealInfo(query);
            }
        });

        clearButton.setOnClickListener(v -> {
            dbHelper.clearFoodLogForDateAndUser(today, profile.getId());
            loadTrackedMeals();
            updateUI();
        });
    }

    private void fetchMealInfo(String query) {
        NutritionApi api = RetrofitClient.getApi();
        api.searchFood(query, API_KEY).enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FoodItem item = response.body().foods != null && !response.body().foods.isEmpty()
                            ? response.body().foods.get(0) : null;

                    if (item != null) {
                        double calories = 0;
                        for (Nutrient nutrient : item.foodNutrients) {
                            if ("Energy".equals(nutrient.nutrientName)) {
                                calories = nutrient.value;
                                break;
                            }
                        }

                        // Save to database
                        dbHelper.insertFoodLog(profile.getId(), item.description, (float) calories, today);

                        // Refresh UI
                        loadTrackedMeals();
                        Toast.makeText(TrackingActivity.this, "Meal logged!", Toast.LENGTH_SHORT).show();
                        mealInput.setText("");
                    } else {
                        Toast.makeText(TrackingActivity.this, "Food not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TrackingActivity.this, "Food not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodResponse> call, Throwable t) {
                Toast.makeText(TrackingActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTrackedMeals() {
        meals = dbHelper.getFoodItemsForDate(today, profile.getId());
        caloriesToday = dbHelper.getTotalCaloriesForDate(today, profile.getId());
        updateUI();
    }

    private void updateUI() {
        totalText.setText("Total: " + String.format("%.1f", caloriesToday) + " kcal");
        remainingText.setText("Remaining: " + String.format("%.1f", calorieGoal - caloriesToday) + " kcal");

        StringBuilder sb = new StringBuilder();
        for (String meal : meals) {
            sb.append(meal).append("\n");
        }
        mealList.setText(sb.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (item.getItemId() == R.id.tracking) {
            startActivity(new Intent(this, TrackingActivity.class));
        } else if (item.getItemId() == R.id.dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
