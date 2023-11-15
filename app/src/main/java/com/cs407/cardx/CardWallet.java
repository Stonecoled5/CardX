package com.cs407.cardx;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardWallet extends RecyclerView.Adapter<CardWallet.CardViewHolder> {

    private List<Card> cardList; // This is the list of Card objects

    // Constructor
    public CardWallet(List<Card> cardList) {
        this.cardList = cardList;
    }

    // ViewHolder class
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        // Other views in the card

        public CardViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            // Initialize other views
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the card item layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_details, parent, false);
        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        // Get the Card object for this position
        Card card = cardList.get(position);

        // Set the color based on position
        int colorRes;
        switch (position % 4) {
            case 0:
                colorRes = Color.parseColor("#00a8e8");
                break;
            case 1:
                colorRes = Color.parseColor("#007ea7");
                break;
            case 2:
                colorRes = Color.parseColor("#003459");
                break;
            case 3:
                colorRes = Color.parseColor("#00171f");
                break;
            default:
                colorRes = Color.parseColor("#00a8e8"); // Default case, if needed
                break;
        }

        holder.itemView.setBackgroundColor(colorRes); // Set the background color
        holder.nameTextView.setText(card.getName()); // Set the card name
        // Set other data to views from the card object
    }

    @Override
    public int getItemCount() {
        // Return the size of the card list
        return cardList.size();
    }

    // Add a card to the wallet
    public void addCard(Card card) {
        cardList.add(card);
        notifyItemInserted(cardList.size() - 1);
    }

    // Remove a card from the wallet
    public void removeCard(int position) {
        if (position < cardList.size()) {
            cardList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
