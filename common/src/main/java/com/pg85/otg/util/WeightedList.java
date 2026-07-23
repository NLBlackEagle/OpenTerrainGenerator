package com.pg85.otg.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.function.IntUnaryOperator;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class WeightedList<T>
{
    private static final Field tree;
    private static final Method left;
    private static final Method right;
    static
    {
        try
        {
            tree = Int2ObjectAVLTreeMap.class.getDeclaredField("tree");
            left = Class.forName(Int2ObjectAVLTreeMap.class.getName() + "$Entry").getDeclaredMethod("left");
            right = Class.forName(Int2ObjectAVLTreeMap.class.getName() + "$Entry").getDeclaredMethod("right");
            tree.setAccessible(true);
            left.setAccessible(true);
            right.setAccessible(true);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    private final Int2ObjectAVLTreeMap<T> map = new Int2ObjectAVLTreeMap<>();

    public boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    public int size()
    {
        return this.map.size();
    }

    public int totalWeight()
    {
        return !this.map.isEmpty() ? this.map.lastIntKey() : 0;
    }

    public void add(T value, int weight)
    {
        if(weight > 0)
        {
            int key = this.totalWeight() + weight;
            if(key < 0)
            {
                throw new IndexOutOfBoundsException();
            }
            this.map.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public T getRandom(IntUnaryOperator rng)
    {
        if(this.map.isEmpty())
        {
            throw new NoSuchElementException();
        }
        try
        {
            if(this.map.size() == 1)
            {
                return ((Int2ObjectMap.Entry<T>) tree.get(this.map)).getValue();
            }

            int t = this.map.lastIntKey();
            int r = rng.applyAsInt(t);
            if(r < 0 || r >= t)
            {
                throw new IndexOutOfBoundsException();
            }
            T value = null;
            Int2ObjectMap.Entry<T> entry = (Int2ObjectMap.Entry<T>) tree.get(this.map);
            while(entry != null)
            {
                if(entry.getIntKey() <= r)
                {
                    entry = (Int2ObjectMap.Entry<T>) right.invoke(entry);
                    continue;
                }
                value = entry.getValue();
                entry = (Int2ObjectMap.Entry<T>) left.invoke(entry);
            }
            return value;
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }
}
