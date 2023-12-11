package com.cs407.cardx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardDetailsActivity extends AppCompatActivity {

    // Define TextViews for each field
    private TextView nameTextView;
    private TextView companyTextView;
    private TextView occupationTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView schoolTextView;
    private TextView bioTextView;
    private Button editButton;
    private Card currentCard;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_details);
        sharedPreferences = getSharedPreferences("com.cs407.cardx", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        // Initialize TextViews
        nameTextView = findViewById(R.id.nameTextView);
        companyTextView = findViewById(R.id.companyTextView);
        occupationTextView = findViewById(R.id.occupationTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        schoolTextView = findViewById(R.id.schoolTextView);
        bioTextView = findViewById(R.id.bioTextView);

        editButton = findViewById(R.id.editButton);
        // Retrieve the Card object from the intent
        currentCard = (Card) getIntent().getSerializableExtra("card");

        editButton.setVisibility(View.GONE);

        if (currentCard != null) {
            // Set the TextViews with the Card's details
            nameTextView.setText("Name: " + currentCard.getName());
            occupationTextView.setText("Occupation: " + currentCard.getOccupation());
            companyTextView.setText("Company: " + currentCard.getCompany());
            emailTextView.setText("Email: " + currentCard.getEmail());
            phoneTextView.setText("Phone: " + currentCard.getPhone());
            schoolTextView.setText("School: " + currentCard.getSchool());
            bioTextView.setText("Bio: " + currentCard.getBio());
        }

        ImageView profile = findViewById(R.id.ivPersonIcon);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfilePage.class);
                startActivity(intent);
            }
        });


    }

    private boolean isUsersPersonalCard(Card card) {
        // Implement the logic to check if the current card is the user's personal card
        return false; // Placeholder
    }

    public void delCard(View view) {
        CardService service = ApiClient.getClient();

        //replace with userid and cardid to delete
        String cardUserId = getIntent().getExtras().get("cardUserId").toString();
        if (cardUserId.equals("")) {
            Toast.makeText(CardDetailsActivity.this, "Card User Id Null", Toast.LENGTH_SHORT).show();
            return;
        }
        service.deleteCard(sharedPreferences.getString("userId",""), getIntent().getExtras().get("cardUserId").toString()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Card successfully deleted
                    Log.d("CardWalletActivity", "Card deleted successfully");
                    Toast.makeText(CardDetailsActivity.this, "Card deleted successfully", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    Intent intent = new Intent(getApplicationContext(), CardWalletActivity.class);
                    startActivity(intent);
                    finish(); // Close the CardDetailsActivity
                } else {
                    // Server returned an error response
                    Log.e("CardWalletActivity", "Server error on card deletion: " + response.code());

                    // inform the user of the error
                    Toast.makeText(CardDetailsActivity.this, "Failed to delete card", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // There was a network failure when attempting the request
                Log.e("CardWalletActivity", "Network failure on card deletion", t);

                // inform the user of the failure
                Toast.makeText(CardDetailsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
