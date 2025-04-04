package com.example.habitRacer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.habitRacer.R;


public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView starCountText;
    private ImageView avatarImage;

    private int progress = 0;
    private int stars = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Свързване с елементите от XML
        progressBar = findViewById(R.id.progressBar);
        starCountText = findViewById(R.id.starCountText);
        avatarImage = findViewById(R.id.avatarImage);

        Button easyBtn = findViewById(R.id.easyHabitButton);
        Button mediumBtn = findViewById(R.id.mediumHabitButton);
        Button hardBtn = findViewById(R.id.hardHabitButton);

        // Бутони за навици
        easyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProgress(10);
                addStars(1);
            }
        });

        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProgress(20);
                addStars(2);
            }
        });

        hardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProgress(30);
                addStars(3);
            }
        });
    }

    private void updateProgress(int amount) {
        progress += amount;
        if (progress > 100) progress = 100;
        progressBar.setProgress(progress);
    }

    private void addStars(int amount) {
        stars += amount;
        starCountText.setText("⭐ " + stars);
    }
}
