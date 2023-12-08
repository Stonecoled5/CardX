package com.cs407.cardx;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfilePage extends AppCompatActivity {

    private boolean canEdit = false;
    private boolean readMediaGranted = false;
    private boolean readMediaVisualUserGranted = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int PERMISSIONS_REQUEST_READ_MEDIA_IMAGES = 1;
    private static final int PERMISSIONS_REQUEST_READ_MEDIA_VISUAL_USER_SELECTED = 2;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ImageView avatarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);
        avatarView = findViewById(R.id.avatarProfile);
        sharedPreferences = getSharedPreferences("com.cs407.cardx", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("userId", "7");
        getUserInfo(sharedPreferences.getString("userId", "7"));
        pickMedia =
                registerForActivityResult(new PickVisualMedia(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Callback is invoked after the user selects a media item or closes the
                        // photo picker.
                        if (uri != null) {
                            avatarView.setImageURI(null);
                            avatarView.setImageURI(uri);
                            Log.d(TAG, avatarView.toString());
                            Log.d("PhotoPicker", "Selected URI: " + uri);
                        } else {
                            Log.d("PhotoPicker", "No media selected");
                        }
                    }
                });

        ImageView backView = findViewById(R.id.back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent oldIntent = getIntent();
                String previousActivity = oldIntent.getStringExtra("previousActivity");
                try{
                    Class class_name = Class.forName(previousActivity);
                    Intent intent = new Intent(getApplicationContext(), class_name);
                    startActivity(intent);
                } catch(Exception e) {
                    Log.e("Going Back", previousActivity+ " class not found");
                }
            }
        });

        ImageView editView = findViewById(R.id.edit);
        editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canEdit = !canEdit;
                EditText companyEdit = findViewById(R.id.companyEdit);
                companyEdit.setEnabled(canEdit);
                EditText occupationEdit = findViewById(R.id.occupationEdit);
                occupationEdit.setEnabled(canEdit);
                EditText emailEdit = findViewById(R.id.emailEdit);
                emailEdit.setEnabled(canEdit);
                EditText phoneEdit = findViewById(R.id.phoneEdit);
                phoneEdit.setEnabled(canEdit);
                EditText schoolEdit = findViewById(R.id.schoolEdit);
                schoolEdit.setEnabled(canEdit);
                EditText nameEdit = findViewById(R.id.name);
                nameEdit.setEnabled(canEdit);
                EditText bioEdit = findViewById(R.id.bio);
                bioEdit.setEnabled(canEdit);
                Uri imgUri;
                if (canEdit) {
                    imgUri=Uri.parse("android.resource://com.cs407.cardx/"+R.drawable.done);
                }
                else {
                    imgUri=Uri.parse("android.resource://com.cs407.cardx/"+R.drawable.edit);
                    avatarView.setDrawingCacheEnabled(true);
                    avatarView.buildDrawingCache(true);
                    Bitmap selectedImage =  Bitmap.createBitmap(avatarView.getDrawingCache());
                    String picBase64 = null;
                    if (selectedImage != null) {
                        try {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] byteArray = stream.toByteArray();
                            picBase64 = Base64.encodeToString(byteArray, 0);
                        }
                        catch(Exception e) {
                            Toast.makeText(ProfilePage.this, "Encoding of Image failed. Check Logcat for details.", Toast.LENGTH_LONG).show();
                        }
                    }
                    avatarView.setDrawingCacheEnabled(false);
                    editUserInfo(sharedPreferences.getString("userId", "7"), companyEdit.getText().toString(),
                            occupationEdit.getText().toString(),
                            emailEdit.getText().toString(),
                            phoneEdit.getText().toString(),
                            schoolEdit.getText().toString(),
                            nameEdit.getText().toString(),
                            bioEdit.getText().toString(),
                            picBase64);
                }
                editView.setImageURI(null);
                editView.setImageURI(imgUri);
            }
        });
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canEdit) {
                    handleAvatarEdit();
                }
            }
        });
    }

    private void editUserInfo(String userId, String company, String occupation, String email, String phone, String school, String name, String bio, String avatar) {
        final String registerUrl = "https://verified-jay-correct.ngrok-free.app/editUserInfo";

        executorService.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(registerUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Prepare the POST parameters
                String postParameters = "userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&company=" + URLEncoder.encode(company, "UTF-8") +
                        "&occupation=" + URLEncoder.encode(occupation, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8") +
                        "&phone=" + URLEncoder.encode(phone, "UTF-8") +
                        "&school=" + URLEncoder.encode(school, "UTF-8") +
                        "&name=" + URLEncoder.encode(name, "UTF-8") +
                        "&bio=" + URLEncoder.encode(bio, "UTF-8") +
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

                final String logTag = "Editing";

                handler.post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(ProfilePage.this, "Save was successful.", Toast.LENGTH_LONG).show();
                    } else {
                        // Log the error response in Logcat
                        Log.e(logTag, "Edit failed with response code " + responseCode + ": " + response);
                        Toast.makeText(ProfilePage.this, "Edit failed. Check Logcat for details.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    // Log the exception
                    Log.e("Editing", "Exception during editing: " + e.getMessage());
                    Toast.makeText(ProfilePage.this, "Error during editing. Check Logcat for details.", Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
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
                        mapInfoToSections(response);
                    } else {
                        // Log the error response in Logcat
                        Log.e(logTag, "Info retrieval failure with response code " + responseCode + ": " + response);
                        Toast.makeText(ProfilePage.this, "Failed to retrieve user information. Check Logcat for details.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    // Log the exception
                    Log.e("SignupError", "Exception during information retrieval: " + e.getMessage());
                    Toast.makeText(ProfilePage.this, "Failed to retrieve user information. Check Logcat for details.", Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    public void mapInfoToSections(String response) {
        String[] responseArray = response.split(",  ");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String s : responseArray) {
            if (s.contains("avatar")) {
                String avatarString = s.substring(13,s.length());
                if (avatarString.contains("null"))
                    continue;
                try {
                    avatarString = avatarString.replace("\"", "");
                    avatarString = avatarString.replace("\\n", System.lineSeparator());
                    byte[] decodedString = Base64.decode(avatarString, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    avatarView.setImageBitmap(decodedByte);
                    editor.putString("avatar", avatarString);
                }
                catch (Exception e) {
                    Log.e("Decode", "Exception during decoding: " + e,e);
                }
            }
            else if (s.contains("bio")) {
                mapInfoToSectionsHelper(s,7,s.length(),findViewById(R.id.bio),"bio", editor);
            }
            else if (s.contains("company")) {
                mapInfoToSectionsHelper(s,11,s.length(),findViewById(R.id.companyEdit),"company", editor);
            }
            else if (s.contains("email")) {
                mapInfoToSectionsHelper(s,9,s.length(),findViewById(R.id.emailEdit),"email", editor);
            }
            else if (s.contains("name")) {
                mapInfoToSectionsHelper(s,8,s.length(),findViewById(R.id.name),"name", editor);
            }
            else if (s.contains("occupation")) {
                mapInfoToSectionsHelper(s,14,s.length(),findViewById(R.id.occupationEdit),"occupation", editor);
            }
            else if (s.contains("phone")) {
                mapInfoToSectionsHelper(s,9,s.length(),findViewById(R.id.phoneEdit),"phone", editor);
            }
            else if (s.contains("school")) {
                mapInfoToSectionsHelper(s,10,s.length()-1,findViewById(R.id.schoolEdit),"school", editor);
            }
        }
    }

    public void mapInfoToSectionsHelper(String s, int startIndex, int endIndex, EditText editText, String prefId, SharedPreferences.Editor editor) {
        String string = s.substring(startIndex, endIndex);
        if (string.contains("null"))
            return;
        string = string.replace("\"", "");
        editText.setText(string);
        editor.putString(prefId, string);
    }

    public void handleAvatarEdit() {
        int permission1 = ActivityCompat.checkSelfPermission(this.getApplicationContext(), READ_MEDIA_IMAGES);
        int permission2 = ActivityCompat.checkSelfPermission(this.getApplicationContext(), READ_MEDIA_VISUAL_USER_SELECTED);
        if (permission1 == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_MEDIA_IMAGES}, PERMISSIONS_REQUEST_READ_MEDIA_IMAGES);
        }
        if (permission2 == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_MEDIA_VISUAL_USER_SELECTED}, PERMISSIONS_REQUEST_READ_MEDIA_VISUAL_USER_SELECTED);
        }
        else {
            PickVisualMedia.VisualMediaType mediaType = (PickVisualMedia.VisualMediaType) PickVisualMedia.ImageOnly.INSTANCE;
            PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                    .setMediaType(mediaType)
                    .build();
            pickMedia.launch(request);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readMediaGranted = true;
            }
        }
        else if (requestCode == PERMISSIONS_REQUEST_READ_MEDIA_VISUAL_USER_SELECTED) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readMediaVisualUserGranted = true;
            }
        }
        if (readMediaGranted && readMediaVisualUserGranted) {
            handleAvatarEdit();
        }
    }

    public void logOut() {

    }
}
