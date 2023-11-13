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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class ProfilePage extends AppCompatActivity {

    private boolean canEdit = false;
    private boolean readMediaGranted = false;
    private boolean readMediaVisualUserGranted = false;

    private static final int PERMISSIONS_REQUEST_READ_MEDIA_IMAGES = 1;
    private static final int PERMISSIONS_REQUEST_READ_MEDIA_VISUAL_USER_SELECTED = 2;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ImageView avatarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);
        avatarView = findViewById(R.id.avatarProfile);

        pickMedia =
                registerForActivityResult(new PickVisualMedia(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Callback is invoked after the user selects a media item or closes the
                        // photo picker.
                        if (uri != null) {
                            avatarView.setImageURI(null);
                            avatarView.setImageURI(uri);
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
                    Log.i(TAG, previousActivity+ " class not found");
                }
            }
        });

        ImageView editView = findViewById(R.id.edit);
        editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canEdit = !canEdit;
                Uri imgUri;
                if (canEdit) {
                    imgUri=Uri.parse("android.resource://com.cs407.cardx/"+R.drawable.done);
                }
                else {
                    imgUri=Uri.parse("android.resource://com.cs407.cardx/"+R.drawable.edit);
                    //TODO: save updated info to database
                }
                editView.setImageURI(null);
                editView.setImageURI(imgUri);
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
}