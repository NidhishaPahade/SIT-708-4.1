package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText workoutDurationEditText, restDurationEditText;
    private TextView workoutTimeTextView, restTimeTextView;
    private Button startButton, stopButton;
    private ProgressBar progressBar;

    private CountDownTimer workoutTimer, restTimer;
    private boolean isWorkoutTimerRunning, isRestTimerRunning;

    private Handler handler;
    private Runnable runnable;

    private NotificationManagerCompat notificationManager;
    private static final String CHANNEL_ID = "workout_timer_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        workoutDurationEditText = findViewById(R.id.workoutDurationEditText);
        restDurationEditText = findViewById(R.id.restDurationEditText);
        workoutTimeTextView = findViewById(R.id.workoutTimeTextView);
        restTimeTextView = findViewById(R.id.restTimeTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        progressBar = findViewById(R.id.progressBar);

        handler = new Handler();
        notificationManager = NotificationManagerCompat.from(this);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });
    }

    private void startTimer() {
        String workoutDurationText = workoutDurationEditText.getText().toString();
        String restDurationText = restDurationEditText.getText().toString();

        if (workoutDurationText.isEmpty() || restDurationText.isEmpty()) {
            return;
        }

        long workoutDurationMillis = Long.parseLong(workoutDurationText) * 1000;
        long restDurationMillis = Long.parseLong(restDurationText) * 1000;

        workoutTimer = new CountDownTimer(workoutDurationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateWorkoutTime(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                updateWorkoutTime(0);
                startRestTimer();
            }
        };

        restTimer = new CountDownTimer(restDurationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateRestTime(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                updateRestTime(0);
                stopTimer();
                showNotification("Workout Timer", "Timer has finished!");
            }
        };

        startWorkoutTimer();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void startWorkoutTimer() {
        workoutTimer.start();
        isWorkoutTimerRunning = true;
    }

    private void startRestTimer() {
        restTimer.start();
        isRestTimerRunning = true;
    }

    private void updateWorkoutTime(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        workoutTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void updateRestTime(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        restTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void stopTimer() {
        if (isWorkoutTimerRunning) {
            workoutTimer.cancel();
            isWorkoutTimerRunning = false;
        }

        if (isRestTimerRunning) {
            restTimer.cancel();
            isRestTimerRunning = false;
        }

        resetTimer();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void resetTimer() {
        workoutTimeTextView.setText("00:00");
        restTimeTextView.setText("00:00");
        progressBar.setProgress(0);
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
