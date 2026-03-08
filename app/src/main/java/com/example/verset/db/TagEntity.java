package com.example.verset.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tags",
        indices = {@Index(value = {"key"}, unique = true)}
)
public class TagEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String key;

    public TagEntity(@NonNull String key) {
        this.key = key;
    }
}