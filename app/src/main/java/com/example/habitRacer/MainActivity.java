package com.example.habitRacer;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView easyHabitText, mediumHabitText, hardHabitText;
    private ImageView avatarMoving;
    private float progress = 0f;
    private float moveStep;

    private String selectedHabit = null;
    private String selectedDifficulty = null;
    private int berry = 0;
    private TextView berryCountText;

    private String easyType = "confirm", mediumType = "confirm", hardType = "confirm";

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
        berryCountText = findViewById(R.id.berryCountText);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        moveStep = metrics.widthPixels * 0.3f;

        loadTodaysHabits();
    }

    private void loadTodaysHabits() {
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

    @Override
    protected void onResume() {
        super.onResume();
        loadTodaysHabits();
    }

    private void showHabits(DocumentSnapshot snapshot) {
        String easy = snapshot.getString("easy");
        String medium = snapshot.getString("medium");
        String hard = snapshot.getString("hard");

        easyType = snapshot.contains("easyType") ? snapshot.getString("easyType") : "confirm";
        mediumType = snapshot.contains("mediumType") ? snapshot.getString("mediumType") : "confirm";
        hardType = snapshot.contains("hardType") ? snapshot.getString("hardType") : "confirm";

        easyHabitText.setText("\uD83C\uDF53 " + easy);
        mediumHabitText.setText("\uD83C\uDF53 \uD83C\uDF53 " + medium);
        hardHabitText.setText("\uD83C\uDF53 \uD83C\uDF53 \uD83C\uDF53 " + hard);

        setHabitListeners(easy, medium, hard);
    }

    private void playBerryAnimation(int amount) {
        ImageView berryAnim = findViewById(R.id.berryAnimation);

        // Пусни звук
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.berry_pop);
        mediaPlayer.start();

        berryAnim.setVisibility(View.VISIBLE);
        berryAnim.setScaleX(1f);
        berryAnim.setScaleY(1f);
        berryAnim.setAlpha(1f);
        berryAnim.setTranslationY(0f);

        // Анимация
        berryAnim.animate()
                .rotationBy(15f)
                .setDuration(100)
                .withEndAction(() -> berryAnim.animate()
                        .rotationBy(-30f)
                        .setDuration(100)
                        .withEndAction(() -> berryAnim.animate()
                                .rotationBy(15f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    berryAnim.animate()
                                            .translationYBy(-300f)
                                            .alpha(0f)
                                            .setDuration(600)
                                            .withEndAction(() -> {
                                                berryAnim.setVisibility(View.GONE);
                                                berry += amount;
                                                berryCountText.setText("🍓 " + berry);
                                                berryAnim.setRotation(0f);
                                                mediaPlayer.release(); // Освобождаваме ресурси
                                            });
                                })));
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String reward = data.getStringExtra("reward");
            int amount = 1;
            switch (reward) {
                case "medium": amount = 2; break;
                case "hard": amount = 3; break;
            }
            playBerryAnimation(amount);
        } else if (resultCode == RESULT_CANCELED) {
            if (selectedDifficulty != null) {
                if (selectedDifficulty.equals("easy")) {
                    mediumHabitText.setVisibility(View.VISIBLE);
                    hardHabitText.setVisibility(View.VISIBLE);
                } else if (selectedDifficulty.equals("medium")) {
                    easyHabitText.setVisibility(View.VISIBLE);
                    hardHabitText.setVisibility(View.VISIBLE);
                } else if (selectedDifficulty.equals("hard")) {
                    easyHabitText.setVisibility(View.VISIBLE);
                    mediumHabitText.setVisibility(View.VISIBLE);
                }
            }
            selectedHabit = null;
            selectedDifficulty = null;
        }
    }

    private void generateDailyHabits(DocumentReference habitDoc) {
        getRandomHabit("easyHabits", (easy, typeEasy) -> {
            getRandomHabit("mediumHabit", (medium, typeMedium) -> {
                getRandomHabit("hardHabit", (hard, typeHard) -> {

                    Map<String, Object> data = new HashMap<>();
                    data.put("easy", easy);
                    data.put("medium", medium);
                    data.put("hard", hard);
                    data.put("easyType", typeEasy);
                    data.put("mediumType", typeMedium);
                    data.put("hardType", typeHard);
                    data.put("selected", null);
                    data.put("completed", false);

                    habitDoc.set(data).addOnSuccessListener(unused -> {
                        easyType = typeEasy;
                        mediumType = typeMedium;
                        hardType = typeHard;

                        easyHabitText.setText("\uD83C\uDF53 " + easy);
                        mediumHabitText.setText("\uD83C\uDF53 \uD83C\uDF53 " + medium);
                        hardHabitText.setText("\uD83C\uDF53 \uD83C\uDF53 \uD83C\uDF53 " + hard);
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
                    if (!querySnapshot.isEmpty()) {
                        int index = random.nextInt(querySnapshot.size());
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(index);
                        String text = doc.getString("text");
                        String type = doc.contains("type") ? doc.getString("type") : "confirm";
                        listener.onHabitFetched(text, type);
                    } else {
                        listener.onHabitFetched("Няма навик", "confirm");
                    }
                });
    }

    private void setHabitListeners(String easy, String medium, String hard) {
        easyHabitText.setOnClickListener(v -> selectHabit("easy", easy, easyType));
        mediumHabitText.setOnClickListener(v -> selectHabit("medium", medium, mediumType));
        hardHabitText.setOnClickListener(v -> selectHabit("hard", hard, hardType));
    }

    private void selectHabit(String difficulty, String habitText, String type) {
        if (selectedHabit != null) return;

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
        startActivityForResult(intent, 1);
    }

    interface OnHabitFetchedListener {
        void onHabitFetched(String text, String type);
    }
}
