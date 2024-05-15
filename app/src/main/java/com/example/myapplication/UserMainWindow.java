package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import android.os.AsyncTask;
import java.sql.Statement;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class UserMainWindow extends AppCompatActivity {
    private Spinner spinner;
    private Spinner spinnerStatus;
    private Button buttonSaveStatus;
    private int userID;
    private String url = "jdbc:postgresql://192.168.42.178:5432/cleanFINAL";
    private String username = "postgres";
    private String datapassword = "123";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_window);

        spinner = findViewById(R.id.spinner);

        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonSaveStatus = findViewById(R.id.buttonSave);

        // Выполнение запроса к базе данных в фоновом потоке
        new FetchDataTask().execute();

        Button buttonLogout = findViewById(R.id.buttonLogout);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMainWindow.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Получение ID пользователя
        userID = getUserID();
        if (userID != -1) {
            loadUserData(userID);
        }
        loadStatusesIntoSpinner();



        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Обработка выбора статуса задачи
                String selectedStatus = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Обработка события, когда не выбрано ничего
            }
        });
    }
    private void loadStatusesIntoSpinner() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> statuses = new ArrayList<>();
                Connection connection = null;
                try {
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(url, username, datapassword);
                    String query = "SELECT название FROM Статусы_задач";
                    PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        statuses.add(resultSet.getString("название"));
                    }
                    // Обновляем Spinner в главном потоке
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(UserMainWindow.this, android.R.layout.simple_spinner_item, statuses);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerStatus.setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> data = new ArrayList<>();
            String connectionUrl = "jdbc:postgresql://192.168.42.178:5432/cleanFINAL";
            String username = "postgres";
            String password = "123";
            userID = getUserID(); // замените это на реальный идентификатор пользователя

            try (Connection connection = DriverManager.getConnection(connectionUrl, username, password)) {
                String query = "SELECT задача_id, Города.название, Улицы.название as название_улицы, Улицы.номер, Типы_работ.название as название_типа, " +
                        "Задачи_сотрудников.описание, Статусы_задач.название as название_статуса, TO_CHAR(дата_выдачи, 'YYYY-MM-DD HH24:MI:SS') as дата_выдачи, " +
                        "TO_CHAR(дата_завершения, 'YYYY-MM-DD HH24:MI:SS') as дата_завершения, фото " +
                        "FROM Задачи_сотрудников " +
                        "JOIN Сотрудники_и_дворы ON Задачи_сотрудников.сотрудник_и_двор  = Сотрудники_и_дворы.двор_сотрудника_id " +
                        "JOIN Дворы ON Дворы.двор_id = Сотрудники_и_дворы.двор " +
                        "JOIN Города ON Дворы.город  = Города.город_id " +
                        "JOIN Улицы ON Дворы.улица  = Улицы.улица_id " +
                        "JOIN Сотрудники ON Сотрудники.сотрудник_id = Сотрудники_и_дворы.сотрудник " +
                        "JOIN Должности_и_типы_работ ON Задачи_сотрудников.должность_и_тип_работ = Должности_и_типы_работ.должность_и_тип_работы_id " +
                        "JOIN Типы_работ ON Должности_и_типы_работ.тип_работы = Типы_работ.тип_работы_id " +
                        "JOIN Статусы_задач ON Статусы_задач.статус_id = Задачи_сотрудников.статус " +
                        "WHERE Сотрудники.сотрудник_id = ? AND Задачи_сотрудников.статус = 1";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, userID);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            String taskID = resultSet.getString("задача_id");
                            String city = resultSet.getString("название");
                            String street = resultSet.getString("название_улицы");
                            String streetNumber = resultSet.getString("номер");
                            String workType = resultSet.getString("название_типа");
                            String description = resultSet.getString("описание");
                            String status = resultSet.getString("название_статуса");
                            String issueDate = resultSet.getString("дата_выдачи");
                            String completionDate = resultSet.getString("дата_завершения");
                            String photo = resultSet.getString("фото");

                            // Формирование строки для отображения в Spinner
                            String item = "Номер задачи: " + taskID + "\nАдрес: г." + city + " ул. " + street + " " + streetNumber +
                                    "\nТип задачи: " + workType + " \nОписание: " + description +
                                    "\nСтатус: " + status + "\nДата выдачи: " + issueDate +
                                    "\nДата завершения: " + completionDate;
                            // Добавление строки в список
                            data.add(item);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Обработка ошибок, например, вывод сообщения об ошибке
                runOnUiThread(() -> Toast.makeText(UserMainWindow.this , "Произошла ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            return data;
        }

        @Override
        protected void onPostExecute(List<String> data) {
            super.onPostExecute(data);
            CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(UserMainWindow.this, android.R.layout.simple_spinner_item, data);
            spinner.setAdapter(spinnerAdapter);
            // Создайте кастомный адаптер для выпадающего списка
            CustomDropdownAdapter dropdownAdapter = new CustomDropdownAdapter(UserMainWindow.this, android.R.layout.simple_spinner_dropdown_item, data);
        }
    }

    private int getUserID() {
        // Здесь код для получения ID пользователя из базы данных
        // Ваша логика подключения к БД и выполнения запроса
        return UserData.getUserID();
    }

    private void loadUserData(int userId) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, username, datapassword);
            Statement stmt = conn.createStatement();
            String query = "SELECT фамилия, имя, отчество FROM Сотрудники WHERE сотрудник_id=" + userId;
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                String surname = rs.getString("фамилия");
                String name = rs.getString("имя");
                String otchestvo = rs.getString("отчество");
                String fio = surname + " " + name + " " + otchestvo;

                TextView labelFIO = findViewById(R.id.textViewFIO);
                labelFIO.setText(fio);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


}