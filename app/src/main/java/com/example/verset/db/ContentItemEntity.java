package com.example.verset.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "content_items",
        indices = {
                @Index(value = {"reference"}, unique = true),
                @Index(value = {"type"}),
                @Index(value = {"language"})
        }
)
public class ContentItemEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String type; // VERSE

    @NonNull
    public String text;

    @NonNull
    public String reference; // "Psalm 23:1"

    @NonNull
    public String language; // "en"

    @NonNull
    public String tradition; // "ALL"

    @NonNull
    public String tone; // SOFT / BALANCED / DIRECT

    @NonNull
    public String source; // "KJV"

    @ColumnInfo(name = "is_premium")
    public boolean isPremium;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public ContentItemEntity(
            @NonNull String type,
            @NonNull String text,
            @NonNull String reference,
            @NonNull String language,
            @NonNull String tradition,
            @NonNull String tone,
            @NonNull String source,
            boolean isPremium,
            long createdAt
    ) {
        this.type = type;
        this.text = text;
        this.reference = reference;
        this.language = language;
        this.tradition = tradition;
        this.tone = tone;
        this.source = source;
        this.isPremium = isPremium;
        this.createdAt = createdAt;
    }
}