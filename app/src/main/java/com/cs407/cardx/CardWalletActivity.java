package com.cs407.cardx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardWalletActivity extends AppCompatActivity implements CardsAdapter.ItemClickListener {

    private static final int CARD_DETAILS_REQUEST = 1; // Unique request code
    private RecyclerView cardsRecyclerView;
    private CardsAdapter cardsAdapter;
    private List<Card> cardList;
    private CardService cardService;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private List<Integer> userIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_wallet);
        sharedPreferences = getSharedPreferences("com.cs407.cardx", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        // Initialize cardList
        cardList = new ArrayList<>();

        // Initialize the adapter and set an empty card list
        cardsAdapter = new CardsAdapter(cardList);

        // Set up the RecyclerView
        cardsRecyclerView = findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardsRecyclerView.setAdapter(cardsAdapter);

        // Initialize cardService
        cardService = ApiClient.getClient();

        // Set the item click listener for the adapter
        cardsAdapter.setClickListener(this);

        // Initialize the "Add Card" button and set an OnClickListener
        Button addCardButton = findViewById(R.id.btnAddCard);
        addCardButton.setOnClickListener(v -> openQRScanner());

        ImageView profile = findViewById(R.id.ivPersonIcon);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfilePage.class);
                startActivity(intent);
                finish();
            }
        });

        // Fetch cards using the user ID
        getCards(sharedPreferences.getString("userId","")); // replace with actual user ID
    }

    @Override
    public void onItemClick(View view, int position) {
        Card card = cardList.get(position); // Make sure you're retrieving a Card object
        Intent intent = new Intent(this, CardDetailsActivity.class);
        intent.putExtra("card", card); // Put the Card object
        intent.putExtra("cardUserId", userIds.get(position));
        startActivityForResult(intent, CARD_DETAILS_REQUEST);
        finish();
    }

    private void getCards(String userId) {
        cardService.getCards(userId).enqueue(new Callback<CardIdsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CardIdsResponse> call, @NonNull Response<CardIdsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userIds = response.body().getCardUserIds();
                    getCardDetails();
                } else {
                    Log.e("CardWalletActivity", "Error getting card user IDs");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CardIdsResponse> call, @NonNull Throwable t) {
                Log.e("CardWalletActivity", "Failure getting card user IDs", t);
            }
        });
    }

    private void getCardDetails() {
        String userIdList = "";
        if (userIds.size() == 0 )
            userIdList = "-1";
        else{
            for (Integer id: userIds) {
                userIdList = userIdList.concat(id.toString());
                if (!id.equals(userIds.get(userIds.size()-1)))
                    userIdList = userIdList.concat(",");
            }
        }
        cardService.getUsersInfo(userIdList).enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Card> cards = response.body();
                    for(Card card : cards) {
                        cardList.add(card);
                        cardsAdapter.setCards(cardList);
                        cardsAdapter.notifyDataSetChanged();
                    }
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

    private void openQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true); // Lock orientation
        integrator.setCaptureActivity(PortraitCaptureActivity.class); // Use a custom CaptureActivity
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    //add card should be called after you already have the card you want to add
    public void addCard(String userId) {
        CardService service = ApiClient.getClient();
        service.addCard(sharedPreferences.getString("userId",""), userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Parse the successful response if necessary
                    Log.d("CardWalletActivity", "Card added successfully");

                    // Assuming you want to refresh the card list after adding
                    getCards(sharedPreferences.getString("userId",""));

                    // inform the user of success
                    Toast.makeText(CardWalletActivity.this, "Card added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Error response from the server
                    Log.e("CardWalletActivity", "Error response code: " + response.code());

                    // inform the user of the error
                    Toast.makeText(CardWalletActivity.this, "Failed to add card", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // Failure case like no internet connection or server down
                Log.e("CardWalletActivity", "Add card failed", t);

                // Optionally, inform the user of the failure
                Toast.makeText(CardWalletActivity.this, "Add card failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // Assuming the QR code contains a single number (userId)
                String scannedUserId = result.getContents();
                // Call addCard with the scanned user ID
                addCard(scannedUserId);
            }
        }
    }

}
