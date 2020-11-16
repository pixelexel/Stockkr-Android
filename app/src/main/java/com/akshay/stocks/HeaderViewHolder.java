package com.akshay.stocks;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

final class HeaderViewHolder extends RecyclerView.ViewHolder {

    final TextView title;

    HeaderViewHolder(@NonNull View view) {
        super(view);

        title = view.findViewById(R.id.list_title);
    }
}
