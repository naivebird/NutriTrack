package com.example.nutritrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutritrack.data.Utils.DatabaseHelper;
import com.example.nutritrack.data.model.UserProfile;

import java.text.NumberFormat;

public class ProfileActivity extends AppCompatActivity {

    DatabaseHelper helper;
    EditText etName;
    EditText etAge;
    EditText etHeight;
    EditText etWeight;
    Spinner spnActivityLevel;
    TextView txvGoal;
    Button btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        spnActivityLevel = findViewById(R.id.spnActivityLevel);
        btnSave = findViewById(R.id.btnSave);
        txvGoal = findViewById(R.id.txvGoal);
        btnSave = findViewById(R.id.btnSave);

        helper = new DatabaseHelper(this);
        String userEmail = getSharedPreferences("nutritrack_prefs", MODE_PRIVATE)
                .getString("user_email", null);
        UserProfile profile = helper.getUserProfile(userEmail);

        if (profile != null) {
            etName.setText(profile.getName());
            etAge.setText(String.valueOf(profile.getAge()));
            etHeight.setText(String.valueOf(profile.getHeight()));
            etWeight.setText(String.valueOf(profile.getWeight()));
            spnActivityLevel.setSelection(profile.getActivityLevel());
            txvGoal.setText(String.valueOf(profile.getGoal()));

        } else {
            Toast.makeText(this, "Welcome! Please complete your profile.", Toast.LENGTH_SHORT).show();
        }
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageText = etAge.getText().toString().trim();
            String heightText = etHeight.getText().toString().trim();
            String weightText = etWeight.getText().toString().trim();

            if (name.isEmpty() | ageText.isEmpty() | heightText.isEmpty() | weightText.isEmpty()) {
                Toast.makeText(this, "Incomplete profile!", Toast.LENGTH_SHORT).show();
            } else {
                int age = Integer.parseInt(ageText);
                float height = Float.parseFloat(heightText);
                float weight = Float.parseFloat(weightText);
                int activityLevel = spnActivityLevel.getSelectedItemPosition();

                float goal = getGoal(weight, height, age);
                NumberFormat numberFormat = NumberFormat.getNumberInstance();
                numberFormat.setMaximumFractionDigits(2);
                txvGoal.setText(numberFormat.format(goal));

                if (profile != null) {
                    helper.updateUserProfile(profile.getId(), profile.getEmail(), name, age, height, weight, activityLevel, goal);
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                } else {
                    UserProfile newProfile = new UserProfile(0, userEmail, name, age, height, weight, activityLevel, goal);
                    helper.insertUserProfile(newProfile);
                    Toast.makeText(this, "Profile created!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private float getGoal(float weight, float height, int age) {
        float activityFactor;
        int activityLevel = spnActivityLevel.getSelectedItemPosition();
        switch (activityLevel) {
            case 0:
                activityFactor = 1.2f;
                break;
            case 1:
                activityFactor = 1.375f;
                break;
            default:
                activityFactor = 1.55f;
        }

        return (float) ((10 * weight + 6.25 * height - 5 * age + 5) * activityFactor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.tracking) {
            Intent intent = new Intent(this, TrackingActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.dashboard) {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

}