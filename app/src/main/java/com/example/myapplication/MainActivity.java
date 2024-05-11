package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.buttonLogin); // Найти кнопку
        button.setOnClickListener(new View.OnClickListener() { // Установить обработчик нажатия
            @Override
            public void onClick(View v) {
                // Создать Intent для запуска новой активности
                Intent intent = new Intent(MainActivity.this, UserMainWindow.class);
                startActivity(intent); // Запустить новую активность
            }
        });

        Button buttonSendFeedback = findViewById(R.id.buttonFeedback);
        buttonSendFeedback.setOnClickListener(new View.OnClickListener() { // Установить обработчик нажатия
            @Override
            public void onClick(View v) {
                // Создать Intent для запуска новой активности
                Intent intent = new Intent(MainActivity.this, SendFeedbackWindow.class);
                startActivity(intent); // Запустить новую активность
            }
        });
    }
}