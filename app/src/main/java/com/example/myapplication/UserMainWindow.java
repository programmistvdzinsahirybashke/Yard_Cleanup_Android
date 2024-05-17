package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserMainWindow extends AppCompatActivity {
    private Spinner spinner;
    private Spinner spinnerStatus;
    private Button buttonSaveStatus;
    private static final int PICK_IMAGE_REQUEST = 1;
    private int selectedTaskID;
    private int userID;
    private List<String> data;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ImageView imageView;
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
        imageView = findViewById(R.id.imageView);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openGallery();
            } else {
                // Обработка отказа в разрешении
            }
        });

        Button buttonOpenGallery = findViewById(R.id.buttonImage);
        buttonOpenGallery.setOnClickListener(v -> openGallery());


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

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskStatusInDatabase(selectedTaskID, getStatusId(spinnerStatus.getSelectedItem().toString()));
                new FetchDataTask().execute();

            }
        });
    }

    private int parseTaskID(String selectedItem) {
        // Предполагаем, что номер задачи находится перед "Номер задачи: " и после первого пробела
        int startIndex = selectedItem.indexOf("Номер задачи: "); // Длина "Номер задачи: "
        int endIndex = selectedItem.indexOf("\n", startIndex); // Найдем конец номера
        String taskIDString = selectedItem.substring(startIndex, endIndex);
        // Преобразуем строку в целое число и возвращаем
        return Integer.parseInt(taskIDString);
    }


    private void updateTaskStatusInDatabase(int taskId, int statusId) {
        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, datapassword);

            // Преобразование изображения из ImageView в байты
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Обновление статуса задачи и фото
            String query = "UPDATE Задачи_сотрудников SET статус = ?, фото = ? WHERE задача_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, statusId);
            statement.setBytes(2, imageBytes);
            statement.setInt(3, taskId);


            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                // Если обновление прошло успешно, показываем сообщение об успехе
                Toast.makeText(UserMainWindow.this, "Статус задачи успешно обновлен", Toast.LENGTH_SHORT).show();
            } else {
                // Если ни одна строка не была обновлена, показываем сообщение об ошибке
                Toast.makeText(UserMainWindow.this, "Ошибка при обновлении статуса задачи = " + selectedTaskID + statusId , Toast.LENGTH_SHORT).show();
            }

            // Закрываем ресурсы
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            // В случае возникновения ошибки показываем сообщение об ошибке
            Toast.makeText(UserMainWindow.this, "Произошла ошибка", Toast.LENGTH_SHORT).show();
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                // Установить изображение в ImageView
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private int getStatusId(String statusName) {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, username, datapassword);
            String query = "SELECT статус_id FROM Статусы_задач WHERE название =?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, statusName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("статус_id");
            } else {
                return 0; // Возвращаем 0, если статус не найден
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Обработка ошибки
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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
                    String query = "SELECT название FROM Статусы_задач where название = 'На проверке' OR название = 'Выдана'";
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
            data = new ArrayList<>(); // Инициализируем список данных

            userID = getUserID(); // замените это на реальный идентификатор пользователя
            try (Connection connection = DriverManager.getConnection(url, username, datapassword)) {
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

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    String selectedItem = data.get(position);

                    // Ищем номер задачи в строке с помощью регулярного выражения
                    Pattern pattern = Pattern.compile("\\d+"); // Ищем все последовательности цифр
                    Matcher matcher = pattern.matcher(selectedItem);

                    if (matcher.find()) {
                        // Извлекаем найденное число
                        String taskID = matcher.group();

                        // Преобразуем идентификатор задачи в int и сохраняем его в переменную
                        selectedTaskID = Integer.parseInt(taskID);
                    } else {
                        // Если номер задачи не найден, обработайте эту ситуацию по вашему усмотрению
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Обработка, если ничего не выбрано
                }
            });
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