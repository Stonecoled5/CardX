package com.cs407.cardx;

import androidx.annotation.NonNull;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                                Log.e("Login", String.valueOf(responseCode));
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
        CardService cardService = ApiClient.getClient();
       cardService.getUsersInfo(userId).enqueue(new Callback<List<Card>>() {
           @Override
           public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
               if (response.isSuccessful() && response.body() != null) {
                   List<Card> cards = response.body();
                   putInfoInSharedPreferences(cards.get(0));
               } else {
                   Log.e("CardWalletActivity", "Error getting card details");
               }
           }
           @Override
           public void onFailure(@NonNull Call<List<Card>> call, @NonNull Throwable t) {
               Log.e("CardWalletActivity", "Failure getting card details", t);
           }
       });
    }

    public void putInfoInSharedPreferences(Card card) {
        editor.putString("avatar", card.getAvatar());
        editor.putString("bio", card.getBio());
        editor.putString("company", card.getCompany());
        editor.putString("email", card.getEmail());
        editor.putString("name", card.getName());
        editor.putString("occupation", card.getOccupation());
        editor.putString("phone", card.getPhone());
        editor.putString("school", card.getSchool());
        editor.apply();
    }

}
