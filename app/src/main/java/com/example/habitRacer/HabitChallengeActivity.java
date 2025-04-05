package com.example.habitRacer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HabitChallengeActivity extends AppCompatActivity {

    private TextView habitText, timerText;
    private EditText inputText;
    private Button startTimerButton, openCameraButton, confirmButton;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final long TIMER_DURATION = 60000; // 1 минута

    private String habitDifficulty;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_challenge);

        habitText = findViewById(R.id.habitText);
        inputText = findViewById(R.id.inputText);
        startTimerButton = findViewById(R.id.startTimerButton);
        timerText = findViewById(R.id.timerText);
        openCameraButton = findViewById(R.id.openCameraButton);
        confirmButton = findViewById(R.id.confirmButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String text = intent.getStringExtra("text");
        habitDifficulty = intent.getStringExtra("habitDifficulty");

        habitText.setText(text);

        switch (type) {
            case "text":
                inputText.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.VISIBLE);
                confirmButton.setOnClickListener(v -> {
                    String result = inputText.getText().toString();
                    if (!result.isEmpty()) {
                        onHabitCompleted();
                    } else {
                        Toast.makeText(this, "Моля, попълни текста", Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case "timer":
                startTimerButton.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.VISIBLE);
                startTimerButton.setOnClickListener(v -> startTimer());
                break;

            case "camera":
                openCameraButton.setVisibility(View.VISIBLE);
                openCameraButton.setOnClickListener(v -> openCamera());
                break;

            case "confirm":
                confirmButton.setVisibility(View.VISIBLE);
                confirmButton.setOnClickListener(v -> onHabitCompleted());
                break;
        }
    }

    private void startTimer() {
        startTimerButton.setEnabled(false);
        new CountDownTimer(TIMER_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Остават: " + millisUntilFinished / 1000 + " сек.");
            }

            public void onFinish() {
                timerText.setText("Готово!");
                onHabitCompleted();
            }
        }.start();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            onHabitCompleted();
        }
    }

    private void onHabitCompleted() {
        String uid = auth.getCurrentUser().getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DocumentReference habitDoc = db.collection("users")
                .document(uid)
                .collection("dailyHabits")
                .document(today);

        Map<String, Object> update = new HashMap<>();
        update.put("completed", true);

        habitDoc.update(update).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Успешно изпълнен навик!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Грешка при запис!", Toast.LENGTH_SHORT).show();
        });
    }
}
