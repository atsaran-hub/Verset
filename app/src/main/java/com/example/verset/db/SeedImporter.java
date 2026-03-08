package com.example.verset.db;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class SeedImporter {

    private static final String ASSET_FILE = "content_seed_kjv.json";

    private static final List<String> DEFAULT_TAGS = Arrays.asList(
            "comfort", "encouragement", "peace", "hope", "anxiety", "guidance"
    );

    public static void ensureSeeded(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        ContentDao dao = db.contentDao();

        if (dao.getContentCount() > 0) return;

        for (String key : DEFAULT_TAGS) {
            dao.insertTag(new TagEntity(key));
        }

        String json = readAsset(context, ASSET_FILE);
        if (json == null || json.trim().isEmpty()) return;

        Gson gson = new Gson();
        Type listType = new TypeToken<List<SeedModels.SeedItem>>() {}.getType();
        List<SeedModels.SeedItem> items = gson.fromJson(json, listType);
        if (items == null) return;

        long now = System.currentTimeMillis();

        for (SeedModels.SeedItem s : items) {
            if (s == null) continue;
            if (s.reference == null || s.reference.trim().isEmpty()) continue;
            if (s.text == null || s.text.trim().isEmpty()) continue;

            ContentItemEntity entity = new ContentItemEntity(
                    safe(s.type, "VERSE"),
                    s.text,
                    s.reference,
                    safe(s.language, "en"),
                    safe(s.tradition, "ALL"),
                    safe(s.tone, "BALANCED"),
                    safe(s.source, "KJV"),
                    s.isPremium,
                    now
            );

            long contentId = dao.insertContentItem(entity);
            if (contentId == -1) continue;

            if (s.tags == null) continue;

            for (SeedModels.SeedTag t : s.tags) {
                if (t == null || t.key == null) continue;

                Long tagId = dao.getTagIdByKey(t.key);
                if (tagId == null) {
                    long inserted = dao.insertTag(new TagEntity(t.key));
                    tagId = inserted > 0 ? inserted : dao.getTagIdByKey(t.key);
                }

                if (tagId != null) {
                    int w = t.weight;
                    if (w < 0) w = 0;
                    if (w > 100) w = 100;
                    dao.insertJoin(new ContentItemTagEntity(contentId, tagId, w));
                }
            }
        }
    }

    private static String readAsset(Context context, String filename) {
        try {
            InputStream is = context.getAssets().open(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String safe(String v, String fallback) {
        if (v == null) return fallback;
        v = v.trim();
        return v.isEmpty() ? fallback : v;
    }
}