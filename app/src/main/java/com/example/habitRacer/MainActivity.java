package com.example.habitRacer;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView easyHabitText, mediumHabitText, hardHabitText;
    private ImageView avatarMoving;
    private float progress = 0f;
    private float moveStep;

    private String selectedHabit = null;
    private String selectedDifficulty = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        easyHabitText = findViewById(R.id.easyHabitText);
        mediumHabitText = findViewById(R.id.mediumHabitText);
        hardHabitText = findViewById(R.id.hardHabitText);
        avatarMoving = findViewById(R.id.avatarMoving);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        moveStep = metrics.widthPixels * 0.3f;

        String uid = auth.getCurrentUser().getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DocumentReference habitDoc = db.collection("users")
                .document(uid)
                .collection("dailyHabits")
                .document(today);

        habitDoc.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                showHabits(snapshot);
            } else {
                generateDailyHabits(habitDoc);
            }
        });
    }

    private void showHabits(DocumentSnapshot snapshot) {
        String easy = snapshot.getString("easy");
        String medium = snapshot.getString("medium");
        String hard = snapshot.getString("hard");

        easyHabitText.setText("\uD83C\uDF53 1. " + easy);
        mediumHabitText.setText("\uD83C\uDF53 2. " + medium);
        hardHabitText.setText("\uD83C\uDF53 3. " + hard);

        setHabitListeners(easy, medium, hard);
    }

    private void generateDailyHabits(DocumentReference habitDoc) {
        getRandomHabit("easyHabits", easy -> {
            getRandomHabit("mediumHabit", medium -> {
                getRandomHabit("hardHabit", hard -> {

                    Map<String, Object> data = new HashMap<>();
                    data.put("easy", easy);
                    data.put("medium", medium);
                    data.put("hard", hard);
                    data.put("selected", null);
                    data.put("completed", false);

                    habitDoc.set(data).addOnSuccessListener(unused -> {
                        easyHabitText.setText("\uD83C\uDF53 1. " + easy);
                        mediumHabitText.setText("\uD83C\uDF53 2. " + medium);
                        hardHabitText.setText("\uD83C\uDF53 3. " + hard);
                        setHabitListeners(easy, medium, hard);
                    });
                });
            });
        });
    }

    private void getRandomHabit(String collection, final OnHabitFetchedListener listener) {
        db.collection(collection)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Random random = new Random();
                    int index = random.nextInt(querySnapshot.size());
                    String habit = querySnapshot.getDocuments().get(index).getString("text");
                    String type = querySnapshot.getDocuments().get(index).getString("type");
                    listener.onHabitFetched(habit + "||" + type);
                });
    }

    private void setHabitListeners(String easy, String medium, String hard) {
        easyHabitText.setOnClickListener(v -> selectHabit("easy", easy));
        mediumHabitText.setOnClickListener(v -> selectHabit("medium", medium));
        hardHabitText.setOnClickListener(v -> selectHabit("hard", hard));
    }

    private void selectHabit(String difficulty, String habitData) {
        if (selectedHabit != null) return;

        String[] parts = habitData.split("\\|\\|");
        String habitText = parts[0];
        String type = parts.length > 1 ? parts[1] : "confirm";

        selectedHabit = habitText;
        selectedDifficulty = difficulty;

        if (!difficulty.equals("easy")) easyHabitText.setVisibility(View.GONE);
        if (!difficulty.equals("medium")) mediumHabitText.setVisibility(View.GONE);
        if (!difficulty.equals("hard")) hardHabitText.setVisibility(View.GONE);

        String uid = auth.getCurrentUser().getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DocumentReference habitDoc = db.collection("users")
                .document(uid)
                .collection("dailyHabits")
                .document(today);

        Map<String, Object> update = new HashMap<>();
        update.put("selected", difficulty);
        update.put("completed", false);

        habitDoc.update(update);

        Intent intent = new Intent(MainActivity.this, HabitChallengeActivity.class);
        intent.putExtra("text", habitText);
        intent.putExtra("type", type);
        intent.putExtra("habitDifficulty", difficulty);
        startActivity(intent);
    }

    interface OnHabitFetchedListener {
        void onHabitFetched(String habitWithType);
    }
}