package com.cs407.cardx;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final EditText emailEditText = findViewById(R.id.editTextEmailSignup);
        final EditText passwordEditText = findViewById(R.id.editTextPasswordSignup);
        final EditText firstNameEditText = findViewById(R.id.editTextFirstName);
        final EditText lastNameEditText = findViewById(R.id.editTextLastName);
        final EditText phoneEditText = findViewById(R.id.editTextPhone);
        Button signUpButton = findViewById(R.id.buttonSignUp);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String phone = phoneEditText.getText().toString();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
                String avatar = null;
                if (bitmap != null) {
                    try {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byte[] byteArray = stream.toByteArray();
                        avatar = Base64.encodeToString(byteArray, 0);
                    }
                    catch(Exception e) {
                        Toast.makeText(SignupActivity.this, "Encoding of Image failed. Check Logcat for details.", Toast.LENGTH_LONG).show();
                    }
                }

                registerUser(email, password, firstName, lastName, phone, avatar);
            }
        });
    }

    private void registerUser(String email, String password, String firstName, String lastName, String phone, String avatar) {
        final String registerUrl = "https://verified-jay-correct.ngrok-free.app/register";

        executorService.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(registerUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Prepare the POST parameters
                String postParameters = "email=" + URLEncoder.encode(email, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8") +
                        "&name=" + URLEncoder.encode(firstName + " " + lastName, "UTF-8") +
                        "&phone=" + URLEncoder.encode(phone, "UTF-8") +
                        "&avatar=" + URLEncoder.encode(avatar, "UTF-8");

                // Send the request
                OutputStream os = conn.getOutputStream();
                os.write(postParameters.getBytes());
                os.flush();
                os.close();

                // Read the response
                int responseCode = conn.getResponseCode();
                InputStream in;

                if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST)
                    in = new BufferedInputStream(conn.getErrorStream());
                else
                    in = new BufferedInputStream(conn.getInputStream());

                // Convert InputStream to String
                Scanner scanner = new Scanner(in).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";

                final String logTag = "SignupError";

                handler.post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        navigateToLogin();
                    } else {
                        // Log the error response in Logcat
                        //Log.e(logTag, "Sign up failed with response code " + responseCode + ": " + response);
                        //Toast.makeText(SignupActivity.this, email, Toast.LENGTH_SHORT).show();
                        Toast.makeText(SignupActivity.this, "Sign up failed. Check Logcat for details.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    // Log the exception
                    //Log.e("SignupError", "Exception during sign up: " + e.getMessage());
                    Toast.makeText(SignupActivity.this, "Error during sign up. Check Logcat for details.", Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private void navigateToLogin() {
        Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
