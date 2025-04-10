package com.example.nutritrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutritrack.data.Utils.DatabaseHelper;
import com.example.nutritrack.data.model.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView txvProgressText;
    TextView txvAdvice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        txvProgressText = findViewById(R.id.txvProgressText);
        txvAdvice = findViewById(R.id.txvAdvice);

        DatabaseHelper helper = new DatabaseHelper(this);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String userEmail = getSharedPreferences("nutritrack_prefs", MODE_PRIVATE)
                .getString("user_email", null);
        UserProfile profile = helper.getUserProfile(userEmail);

        if (profile == null) {
            txvAdvice.setText(R.string.please_set_up_your_profile_first);
        } else {
            float calorieGoal = profile.getGoal();
            float caloriesToday = helper.getTotalCaloriesForDate(today, profile.getId());

            if (caloriesToday < calorieGoal) {
                txvAdvice.setText(R.string.you_haven_t_reached_your_calorie_goal_for_today_yet_eat_more_and_remember_to_log_your_meals);
            } else {
                txvAdvice.setText(R.string.you_ve_reached_your_calorie_goal_for_today_stop_eating);
            }

            int percentage = Math.min((int) ((caloriesToday / calorieGoal) * 100), 100);
            progressBar.setProgress(percentage);
            txvProgressText.setText(String.format("%d / %d kcal", (int) caloriesToday, (int) calorieGoal));
        }
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