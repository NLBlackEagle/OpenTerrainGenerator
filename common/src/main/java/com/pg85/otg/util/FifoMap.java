package com.pg85.otg.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class FifoMap<T, U> extends LinkedHashMap<T, U>
{
    private final int max;

    public FifoMap (int max) {
        super(max + 1);
        this.max = max;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<T, U> eldest) {
        return this.size() > max;
    }
}