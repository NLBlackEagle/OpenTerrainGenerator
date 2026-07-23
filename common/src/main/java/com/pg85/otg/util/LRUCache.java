package com.pg85.otg.util;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LRUCache<K, V> extends LinkedHashMap<K, V>
{
    private static final float LOAD_FACTOR = 0.75F;
    private final int max;

    public LRUCache(int max)
    {
        super((int) Math.ceil((max + 1) / LOAD_FACTOR), LOAD_FACTOR, true);
        this.max = max;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        return this.size() > this.max;
    }
}