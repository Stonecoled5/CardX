package com.cs407.cardx;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText emailEditText = findViewById(R.id.editTextEmail);
        final EditText passwordEditText = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        final String loginUrl = "https://verified-jay-correct.ngrok-free.app/login";

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(loginUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Prepare the POST parameters
                    String postParameters = "email=" + email + "&password=" + password;

                    // Send the request
                    OutputStream os = conn.getOutputStream();
                    os.write(postParameters.getBytes());
                    os.flush();
                    os.close();

                    // Read the response
                    int responseCode = conn.getResponseCode();
                    String responseString = "";
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        responseString = response.toString();
                        in.close();

                        // Switch to the main thread to update the UI
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Handle the response from the server here
                                handleLoginSuccess(response.toString());
                            }
                        });
                    } else {
                        // Switch to the main thread to show an error message
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Login failed: Incorrect Username/password", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    // Switch to the main thread to show an error message
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void handleLoginSuccess(String userId) {
        // Save the login state and userId in SharedPreferences
        sharedPreferences = getSharedPreferences("com.cs407.cardx", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        userId = userId.substring(13, userId.length() - 1);
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", userId);
        editor.apply();
        getUserInfo(userId);
        // Navigate to the CardWalletActivity
        Intent intent = new Intent(LoginActivity.this, CardWalletActivity.class);
        startActivity(intent);
        finish();
    }

    private void getUserInfo(String userId) {
        final String registerUrl = "https://verified-jay-correct.ngrok-free.app/getUserInfo";

        executorService.execute(() -> {
            HttpURLConnection conn = null;
            try {
                // Create the URL object with the endpoint and parameters
                URL url = new URL(registerUrl + "?userId=" + userId);
                conn = (HttpURLConnection) url.openConnection();

                // Set the request method to GET
                conn.setRequestMethod("GET");

                // Get the response code
                int responseCode = conn.getResponseCode();

                // Read the response from the input stream
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder responseBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String response = responseBuilder.toString();
                // Close the connection and print the response
                reader.close();
                conn.disconnect();

                final String logTag = "SignupError";

                handler.post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        putInfoInSharedPreferences(response);
                    } else {
                        // Log the error response in Logcat
                        Log.e(logTag, "Info retrieval failure with response code " + responseCode + ": " + response);
                        Toast.makeText(LoginActivity.this, "Failed to retrieve user information. Check Logcat for details.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    // Log the exception
                    Log.e("SignupError", "Exception during information retrieval: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, "Failed to retrieve user information. Check Logcat for details.", Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    public void putInfoInSharedPreferences(String response) {
        String[] responseArray = response.split(",  ");
        for (String s : responseArray) {
            if (s.contains("avatar")) {
                String avatarString = s.substring(13);
                if (avatarString.contains("null"))
                    continue;
                try {
                    avatarString = avatarString.replace("\"", "");
                    avatarString = avatarString.replace("\\n", System.lineSeparator());
                    editor.putString("avatar", avatarString);
                    editor.apply();
                }
                catch (Exception e) {
                    Log.e("Decode", "Exception during decoding: " + e,e);
                }
            }
            else if (s.contains("bio")) {
                putInfoInSharedPreferencesHelper(s,7,s.length(),"bio");
            }
            else if (s.contains("company")) {
                putInfoInSharedPreferencesHelper(s,11,s.length(),"company");
            }
            else if (s.contains("email")) {
                putInfoInSharedPreferencesHelper(s,9,s.length(),"email");
            }
            else if (s.contains("name")) {
                putInfoInSharedPreferencesHelper(s,8,s.length(),"name");
            }
            else if (s.contains("occupation")) {
                putInfoInSharedPreferencesHelper(s,14,s.length(),"occupation");
            }
            else if (s.contains("phone")) {
                putInfoInSharedPreferencesHelper(s,9,s.length(),"phone");
            }
            else if (s.contains("school")) {
                putInfoInSharedPreferencesHelper(s,10,s.length()-1,"school");
            }
        }
    }

    public void putInfoInSharedPreferencesHelper(String s, int startIndex, int endIndex, String prefId) {
        String string = s.substring(startIndex, endIndex);
        if (string.contains("null"))
            return;
        string = string.replace("\"", "");
        editor.putString(prefId, string);
        editor.apply();
    }
}
