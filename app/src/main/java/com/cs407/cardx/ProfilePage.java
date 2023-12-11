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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private EditText companyEdit, occupationEdit, emailEdit, phoneEdit, schoolEdit, nameEdit, bioEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);
        avatarView = findViewById(R.id.avatarProfile);
        companyEdit = findViewById(R.id.companyEdit);
        occupationEdit = findViewById(R.id.occupationEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
        emailEdit = findViewById(R.id.emailEdit);
        schoolEdit = findViewById(R.id.schoolEdit);
        nameEdit = findViewById(R.id.name);
        bioEdit = findViewById(R.id.bio);
        sharedPreferences = getSharedPreferences("com.cs407.cardx", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        getUserInfo();
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

        ImageView editView = findViewById(R.id.edit);
        editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canEdit = !canEdit;
                companyEdit.setEnabled(canEdit);
                occupationEdit.setEnabled(canEdit);
                emailEdit.setEnabled(canEdit);
                phoneEdit.setEnabled(canEdit);
                schoolEdit.setEnabled(canEdit);
                nameEdit.setEnabled(canEdit);
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
                    editor.putString("avatar",picBase64);
                    editor.putString("company",companyEdit.getText().toString());
                    editor.putString("occupation",occupationEdit.getText().toString());
                    editor.putString("email",emailEdit.getText().toString());
                    editor.putString("phone",phoneEdit.getText().toString());
                    editor.putString("school",schoolEdit.getText().toString());
                    editor.putString("name",nameEdit.getText().toString());
                    editor.putString("bio",bioEdit.getText().toString());
                    editor.apply();
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
                        Toast.makeText(ProfilePage.this, response.substring(11, response.length()-2).replace("\"","").replace(System.lineSeparator(),""), Toast.LENGTH_LONG).show();
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

    private void getUserInfo() {

        byte[] decodedString = Base64.decode(sharedPreferences.getString("avatar",""), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        if (decodedString == Base64.decode("", Base64.DEFAULT))
            avatarView.setImageURI(Uri.parse("android.resource://com.cs407.cardx/"+R.drawable.avatar));
        else
            avatarView.setImageBitmap(decodedByte);
        companyEdit.setText(sharedPreferences.getString("company",""));
        occupationEdit.setText(sharedPreferences.getString("occupation",""));
        emailEdit.setText(sharedPreferences.getString("email",""));
        phoneEdit.setText(sharedPreferences.getString("phone",""));
        schoolEdit.setText(sharedPreferences.getString("school",""));
        nameEdit.setText(sharedPreferences.getString("name",""));
        bioEdit.setText(sharedPreferences.getString("bio",""));
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

    public void logOut(View view) {
        editor.clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
