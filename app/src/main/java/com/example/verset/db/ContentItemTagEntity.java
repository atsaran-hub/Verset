package com.example.verset.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "content_item_tags",
        primaryKeys = {"content_id", "tag_id"},
        indices = {@Index("content_id"), @Index("tag_id")}
)
public class ContentItemTagEntity {

    @ColumnInfo(name = "content_id")
    public long contentId;

    @ColumnInfo(name = "tag_id")
    public long tagId;

    public int weight; // 0..100

    public ContentItemTagEntity(long contentId, long tagId, int weight) {
        this.contentId = contentId;
        this.tagId = tagId;
        this.weight = weight;
    }
}