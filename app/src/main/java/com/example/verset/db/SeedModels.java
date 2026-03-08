package com.example.verset.db;

import java.util.List;

public class SeedModels {
    public static class SeedTag {
        public String key;
        public int weight;
    }

    public static class SeedItem {
        public String type;
        public String text;
        public String reference;
        public String language;
        public String tradition;
        public String tone;
        public String source;
        public boolean isPremium;
        public List<SeedTag> tags;
    }
}