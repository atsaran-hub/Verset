package com.example.verset.Tiktoklike;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FavoritesStore {
    private static final String PREFS = "favorites_store";
    private static final String KEY = "favorite_ids";

    private final SharedPreferences sp;

    public FavoritesStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public Set<String> getAll() {
        Set<String> set = sp.getStringSet(KEY, null);
        if (set == null) return Collections.emptySet();
        return new HashSet<>(set); // copie (important)
    }

    public boolean isFavorite(String id) {
        return getAll().contains(id);
    }

    public void toggle(String id) {
        Set<String> set = new HashSet<>(getAll());
        if (set.contains(id)) set.remove(id);
        else set.add(id);
        sp.edit().putStringSet(KEY, set).apply();
    }
}
