package com.example.verset.Tiktoklike;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.verset.R;

import java.util.ArrayList;
import java.util.List;

public class VersePagerAdapter extends RecyclerView.Adapter<VersePagerAdapter.VH> {

    private final List<Verse> items = new ArrayList<>();

    public VersePagerAdapter(FavoritesStore favoritesStore, OnLikeClick onLikeClick) {
        // On garde le constructeur mais on ne gère plus le bouton ici
    }

    public void submit(List<Verse> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    // ✅ IMPORTANT pour FeedActivity
    public Verse getItem(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_verse, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Verse verse = items.get(position);
        holder.tvVerse.setText(verse.text);
        holder.tvRef.setText(verse.ref);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvVerse;
        TextView tvRef;

        VH(@NonNull View itemView) {
            super(itemView);
            tvVerse = itemView.findViewById(R.id.tvVerse);
            tvRef = itemView.findViewById(R.id.tvRef);
        }
    }

    // Interface conservée pour compatibilité
    public interface OnLikeClick {
        void onLike(Verse verse);
    }
}