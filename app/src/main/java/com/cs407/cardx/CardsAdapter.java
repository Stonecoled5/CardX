package com.cs407.cardx;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {

    private List<Card> cardList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // Modify the constructor to remove the context and data parameters
    CardsAdapter(List<Card> cardList) {
        this.cardList = cardList;
    }

    // Initialize the LayoutInflater using the context provided by onCreateViewHolder
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mInflater.inflate(R.layout.card_list_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
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
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTextView;

        CardViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    Card getItem(int id) {
        return cardList.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Method to update the adapter's dataset
    public void setCards(List<Card> newCardList) {
        this.cardList = newCardList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
}

