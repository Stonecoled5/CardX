package com.cs407.cardx;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_details);

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

        // Determine if the currentCard is the user's personal card and set the visibility accordingly
        if (isUsersPersonalCard(currentCard)) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the CardDesignActivity
                    Intent intent = new Intent(String.valueOf(CardDetailsActivity.class)); // add edit card page to this
                    // Pass the Card object to the editing activity if needed
                    intent.putExtra("card", (Serializable) currentCard);
                    startActivity(intent);
                }
            });
        } else {
            editButton.setVisibility(View.GONE);
        }

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
    }

    private boolean isUsersPersonalCard(Card card) {
        // Implement the logic to check if the current card is the user's personal card
        return false; // Placeholder
    }

    public void delCard(View view) {
        CardService service = ApiClient.getClient();

        //replace with userid and cardid to delete
        service.deleteCard("2", "3").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Card successfully deleted
                    Log.d("CardWalletActivity", "Card deleted successfully");
                    Toast.makeText(CardDetailsActivity.this, "Card deleted successfully", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
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
