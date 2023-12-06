package com.cs407.cardx;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement login logic here
                // If login is successful, then save login state and navigate to the CardWalletActivity:

                // Save login state
                SharedPreferences sharedPreferences = getSharedPreferences("com.cs407.cardx", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                // Navigate to CardWalletActivity
                Intent intent = new Intent(LoginActivity.this, CardWalletActivity.class);
                startActivity(intent);

                // Optional: Finish LoginActivity so the back button doesn't return to the login screen
                finish();
            }
        });
    }
}
