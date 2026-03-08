package com.example.verset.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface ContentDao {
    @Query("SELECT * FROM content_items WHERE language = :lang ORDER BY RANDOM() LIMIT 1")
    ContentItemEntity getRandomVerse(String lang);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertContentItem(ContentItemEntity item);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertTag(TagEntity tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJoin(ContentItemTagEntity join);

    @Query("SELECT id FROM tags WHERE `key` = :key LIMIT 1")
    Long getTagIdByKey(String key);

    @Query("SELECT COUNT(*) FROM content_items")
    int getContentCount();

    @Query(
            "SELECT COUNT(DISTINCT ci.id) " +
                    "FROM content_items ci " +
                    "JOIN content_item_tags cit ON cit.content_id = ci.id " +
                    "JOIN tags t ON t.id = cit.tag_id " +
                    "WHERE t.`key` = :tagKey AND ci.language = :lang"
    )
    int countByTag(String tagKey, String lang);

    @Transaction
    @Query(
            "SELECT ci.* " +
                    "FROM content_items ci " +
                    "JOIN content_item_tags cit ON cit.content_id = ci.id " +
                    "JOIN tags t ON t.id = cit.tag_id " +
                    "WHERE t.`key` = :tagKey AND ci.language = :lang " +
                    "ORDER BY cit.weight DESC, ci.id DESC " +
                    "LIMIT :limit"
    )
    List<ContentItemEntity> getTopByTag(String tagKey, String lang, int limit);

    @Transaction
    @Query(
            "SELECT ci.* " +
                    "FROM content_items ci " +
                    "JOIN content_item_tags cit ON cit.content_id = ci.id " +
                    "JOIN tags t ON t.id = cit.tag_id " +
                    "WHERE t.`key` IN (:tagKeys) AND ci.language = :lang " +
                    "GROUP BY ci.id " +
                    "ORDER BY SUM(cit.weight) DESC " +
                    "LIMIT :limit"
    )
    List<ContentItemEntity> getTopByTags(List<String> tagKeys, String lang, int limit);
}