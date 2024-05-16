package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainActivity extends Activity {
    private String connection = "jdbc:postgresql://192.168.57.178:5432/cleanFINAL";
    private EditText editTextLogin;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);


        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonFeedback = findViewById(R.id.buttonFeedback);
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = editTextLogin.getText().toString();
                String password = editTextPassword.getText().toString();
                authenticateUser(login, password);
            }
        });

        buttonFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendFeedbackWindow.class);
                startActivity(intent);
            }
        });
    }

    private void authenticateUser(final String login, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName("org.postgresql.Driver");
                    Connection conn = DriverManager.getConnection(connection, "postgres", "123");

                    String query = "SELECT * FROM Сотрудники WHERE логин = ?";
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.setString(1, login);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        int userID = resultSet.getInt("сотрудник_id");
                        String storedPassword = resultSet.getString("пароль");
                        int positionID = resultSet.getInt("должность");

                        if (password.equals(storedPassword)) {
                            // Действия при успешной аутентификации
                            UserData.setUserID(userID);

                            if (positionID == 21) {
                                // Открывать окно администратора
                                Intent intent = new Intent(MainActivity.this, AdminPanel.class);
                                startActivity(intent);
                            } else {
                                // Открывать окно пользователя
                                Intent intent = new Intent(MainActivity.this, UserMainWindow.class);
                                startActivity(intent);
                            }
                        } else {
                            showToast("Неверный логин или пароль!");
                        }
                    } else {
                        showToast("Неверный логин или пароль!");
                    }

                    conn.close();
                } catch (ClassNotFoundException | SQLException e) {
                    showToast(e.getMessage());
                }
            }
        }).start();
    }
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
