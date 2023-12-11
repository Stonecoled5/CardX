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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardWalletActivity extends AppCompatActivity implements CardsAdapter.ItemClickListener {

    private static final int CARD_DETAILS_REQUEST = 1; // Unique request code
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
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
        Button addCardButton = findViewById(R.id.btnAddCard); // Replace with your button's ID
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });

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
        cardService.getUserInfo(userIdList).enqueue(new Callback<List<Card>>() {
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

    //add card should be called after you already have the card you want to add
    public void addCard(View view) {
        CardService service = ApiClient.getClient();
        service.addCard(sharedPreferences.getString("userId",""), "7").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Parse the successful response if necessary
                    Log.d("CardWalletActivity", "Card added successfully");

                    // Assuming you want to refresh the card list after adding
                    getCards(sharedPreferences.getString("userId","")); // Replace "2" with the actual userId

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

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Permission was denied. Handle the error.
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            // Handle the error (e.g. no camera app can handle the intent)
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Handling the camera result
            if (data != null && data.getExtras() != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Use the bitmap as needed
            }
        } else if (requestCode == CARD_DETAILS_REQUEST && resultCode == RESULT_OK) {
            // Handling the result from CardDetailsActivity
            // The card was deleted, refresh the cards list
            getCards(sharedPreferences.getString("userId", "")); // Replace "2" with the actual userId
        }
    }




}
