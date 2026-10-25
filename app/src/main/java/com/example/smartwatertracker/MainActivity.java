package com.example.smartwatertracker;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "WaterTrackerPrefs";
    public static final String KEY_WATER_INTAKE = "key_water_intake";
    public static final String KEY_DAILY_GOAL = "key_daily_goal";
    public static final String KEY_LAST_DATE = "key_last_date";
    public static final String KEY_REMINDER_ENABLED = "key_reminder_enabled";

    public static final int DEFAULT_GOAL = 2000;

    private TextView tvIntakeAmount;
    private TextView tvPercentage;
    private ProgressBar progressBar;
    private TextView tvStatusMessage;
    private MaterialButton btnAdd250;
    private MaterialButton btnAdd500;
    private MaterialButton btnSetGoal;
    private MaterialButton btnReset;
    private SwitchMaterial switchReminder;
    private TextView tvReminderStatus;

    private SharedPreferences sharedPreferences;
    private int currentIntake = 0;
    private int dailyGoal = DEFAULT_GOAL;
    private boolean isReminderEnabled = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableReminders();
                } else {
                    switchReminder.setChecked(false);
                    Toast.makeText(this, "Notification permission is required to receive reminders.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initDataAndCheckRollover();
        setupListeners();
    }

    private void initViews() {
        tvIntakeAmount = findViewById(R.id.tv_intake_amount);
        tvPercentage = findViewById(R.id.tv_percentage);
        progressBar = findViewById(R.id.progressBar);
        tvStatusMessage = findViewById(R.id.tv_status_message);
        btnAdd250 = findViewById(R.id.btn_add_250);
        btnAdd500 = findViewById(R.id.btn_add_500);
        btnSetGoal = findViewById(R.id.btn_set_goal);
        btnReset = findViewById(R.id.btn_reset);
        switchReminder = findViewById(R.id.switch_reminder);
        tvReminderStatus = findViewById(R.id.tv_reminder_status);
    }

    private void initDataAndCheckRollover() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String savedDate = sharedPreferences.getString(KEY_LAST_DATE, "");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        dailyGoal = sharedPreferences.getInt(KEY_DAILY_GOAL, DEFAULT_GOAL);

        if (!currentDate.equals(savedDate)) {
            // New day detected: auto reset daily intake
            currentIntake = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_LAST_DATE, currentDate);
            editor.putInt(KEY_WATER_INTAKE, 0);
            editor.apply();
        } else {
            currentIntake = sharedPreferences.getInt(KEY_WATER_INTAKE, 0);
        }

        isReminderEnabled = sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, false);
        switchReminder.setChecked(isReminderEnabled);

        updateUI();
    }

    private void setupListeners() {
        btnAdd250.setOnClickListener(v -> addWater(250));
        btnAdd500.setOnClickListener(v -> addWater(500));

        btnSetGoal.setOnClickListener(v -> showSetGoalDialog());

        btnReset.setOnClickListener(v -> showResetConfirmationDialog());

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkPermissionAndEnableReminders();
            } else {
                disableReminders();
            }
        });
    }

    private void addWater(int amount) {
        if (amount <= 0) return;

        currentIntake += amount;
        saveIntake();
        updateUI();

        Toast.makeText(this, "+ " + amount + " ml water added!", Toast.LENGTH_SHORT).show();
    }

    private void saveIntake() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_WATER_INTAKE, currentIntake);
        editor.apply();
    }

    private void updateUI() {
        tvIntakeAmount.setText(getString(R.string.intake_format, currentIntake, dailyGoal));

        int percentage = dailyGoal > 0 ? (int) (((float) currentIntake / dailyGoal) * 100) : 0;
        tvPercentage.setText(getString(R.string.percentage_format, percentage));

        progressBar.setMax(dailyGoal);
        progressBar.setProgress(Math.min(currentIntake, dailyGoal));

        if (percentage == 0) {
            tvStatusMessage.setText("Start drinking water to reach your daily goal!");
        } else if (percentage < 50) {
            tvStatusMessage.setText("Good start! Keep drinking water throughout the day.");
        } else if (percentage < 100) {
            tvStatusMessage.setText("You are more than halfway there! Great job!");
        } else {
            tvStatusMessage.setText("Awesome! Daily hydration goal completed! 🎉");
        }

        if (isReminderEnabled) {
            tvReminderStatus.setText(R.string.reminder_status_enabled);
        } else {
            tvReminderStatus.setText(R.string.reminder_status_disabled);
        }
    }

    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Daily Goal (ml)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(dailyGoal));
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String valueStr = input.getText().toString().trim();
            if (!valueStr.isEmpty()) {
                try {
                    int newGoal = Integer.parseInt(valueStr);
                    if (newGoal > 0 && newGoal <= 10000) {
                        dailyGoal = newGoal;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(KEY_DAILY_GOAL, dailyGoal);
                        editor.apply();

                        updateUI();
                        Toast.makeText(this, "Daily goal updated to " + dailyGoal + " ml", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter a goal between 100 and 10000 ml", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Today's Intake?")
                .setMessage("Are you sure you want to reset your water intake for today back to 0 ml?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    currentIntake = 0;
                    saveIntake();
                    updateUI();
                    Toast.makeText(this, "Today's water intake reset.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkPermissionAndEnableReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        enableReminders();
    }

    private void enableReminders() {
        WaterReminderReceiver.scheduleReminder(this);
        isReminderEnabled = true;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMINDER_ENABLED, true);
        editor.apply();

        updateUI();
        Toast.makeText(this, "Water reminders scheduled every 2 hours!", Toast.LENGTH_SHORT).show();
    }

    private void disableReminders() {
        WaterReminderReceiver.cancelReminder(this);
        isReminderEnabled = false;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMINDER_ENABLED, false);
        editor.apply();

        updateUI();
        Toast.makeText(this, "Water reminders disabled.", Toast.LENGTH_SHORT).show();
    }
}
