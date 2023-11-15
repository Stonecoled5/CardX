package com.cs407.cardx;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class CardViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTextView;
    public TextView titleTextView;
    public TextView contactInfoTextView;
    // ... other views that might be in your card layout

    public CardViewHolder(View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.name_text_view);
        titleTextView = itemView.findViewById(R.id.title_text_view);
        contactInfoTextView = itemView.findViewById(R.id.contact_info_text_view);
        // ... initialize other views in your card layout
    }
}

