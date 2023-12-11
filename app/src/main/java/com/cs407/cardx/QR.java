package com.cs407.cardx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class QR extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);
        sharedPreferences = getSharedPreferences("com.cs407.cardx", Context.MODE_PRIVATE);
        ImageView profileView = findViewById(R.id.avatarQR);
        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfilePage.class);
                startActivity(intent);
            }
        });
        getQRcode(sharedPreferences.getString("userId", ""));
    }

    public void getQRcode(String userId) {
        final String registerUrl = "https://verified-jay-correct.ngrok-free.app/getQr";

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
                InputStream inputStream = conn.getInputStream();

                // Process the blob data as needed
                // read the data into a byte array
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                conn.disconnect();

                final String logTag = "SignupError";

                handler.post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        placeQRCode(buffer);
                    } else {
                        // Log the error response in Logcat
                        Log.e(logTag, "Info retrieval failure with response code " + responseCode + ": " + buffer);
                        Toast.makeText(QR.this, "Failed to retrieve user information. Check Logcat for details.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    // Log the exception
                    Log.e("SignupError", "Exception during information retrieval: " + e.getMessage());
                    Toast.makeText(QR.this, "Failed to retrieve user information. Check Logcat for details.", Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    public void placeQRCode(byte[] buffer) {
        ImageView QRCode = findViewById(R.id.QRCode);
        if (buffer == null || buffer.length < 1)
            return;
        try {
            Bitmap decodedByte = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            QRCode.setImageBitmap(decodedByte);
        }
        catch (Exception e) {
            Log.e("Decode", "Exception during decoding: " + e,e);
        }
    }

    public void go_to_profile(View view) {
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
        finish();
    }

}
