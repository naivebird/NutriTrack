package com.example.nutritrack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutritrack.data.Utils.DatabaseHelper;
import com.example.nutritrack.data.model.UserProfile;

public class ProfileActivity extends AppCompatActivity {

    DatabaseHelper helper;
    EditText etName;
    EditText etAge;
    EditText etHeight;
    EditText etWeight;
    Spinner spnActivityLevel;

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

        helper = new DatabaseHelper(this);
        String userEmail = getSharedPreferences("nutritrack_prefs", MODE_PRIVATE)
                .getString("user_email", null);
        Toast.makeText(this, userEmail, Toast.LENGTH_LONG).show();
        UserProfile profile = helper.getUserProfile(userEmail);

        if (profile != null) {
            etName.setText(profile.getName());
            etAge.setText(String.valueOf(profile.getAge()));
            etHeight.setText(String.valueOf(profile.getHeight()));
            etWeight.setText(String.valueOf(profile.getWeight()));
            spnActivityLevel.setSelection(profile.getActivityLevel());

        } else {
            Toast.makeText(this, "Welcome! Please complete your profile.", Toast.LENGTH_SHORT).show();
        }
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            int age = Integer.parseInt(etAge.getText().toString().trim());
            float height = Float.parseFloat(etHeight.getText().toString().trim());
            float weight = Float.parseFloat(etWeight.getText().toString().trim());
            int activityLevel = spnActivityLevel.getSelectedItemPosition();

            if (profile != null) {
                helper.updateUserProfile(profile.getId(), profile.getEmail(), name, age, height, weight, activityLevel);
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            } else {
                UserProfile newProfile = new UserProfile(0, userEmail, name, age, height, weight, activityLevel);
                helper.insertUserProfile(newProfile);
                Toast.makeText(this, "Profile created!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}