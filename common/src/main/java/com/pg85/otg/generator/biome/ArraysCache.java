package com.pg85.otg.generator.biome;

import java.util.ArrayList;
import java.util.Arrays;

// Used to prevent creating new arrays during biomegen.
public class ArraysCache
{
    private final int[][] smallArrays = new int[128][256];
    private int smallArraysNext = 0;
    private final ArrayList<int[]> mediumArrays = new ArrayList<>();
    private int mediumArraysNext = 0;
    private final ArrayList<int[]> largeArrays = new ArrayList<>();
    private int largeArraysNext = 0;
    private final ArrayList<int[]> extraLargeArrays = new ArrayList<>();
    private int extraLargeArraysNext = 0;

    boolean isFree = true;

    public OutputType outputType = OutputType.FULL;

    public ArraysCache()
    {

    }

    void release()
    {
        for (int i = 0; i < smallArraysNext; i++)
        {
            Arrays.fill(smallArrays[i], 0);
        }
        smallArraysNext = 0;
        for (int i = 0; i < mediumArraysNext; i++)
        {
            Arrays.fill(mediumArrays.get(i), 0);
        }
        mediumArraysNext = 0;
        for (int i = 0; i < largeArraysNext; i++)
        {
            Arrays.fill(largeArrays.get(i), 0);
        }
        largeArraysNext = 0;
        for (int i = 0; i < extraLargeArraysNext; i++)
        {
            Arrays.fill(extraLargeArrays.get(i), 0);
        }
        extraLargeArraysNext = 0;
        isFree = true;
        outputType = OutputType.FULL;       
    }

    public int[] getArray(int size)
    {
        if (size <= 256)
        {
            int[] array = smallArrays[smallArraysNext];
            smallArraysNext++;
            return array;
        }

        if (size <= 512)
        {
            int[] array;
            if (mediumArraysNext == mediumArrays.size())
            {
                array = new int[512];
                mediumArrays.add(array);
            } else {
                array = mediumArrays.get(mediumArraysNext);
            }

            mediumArraysNext++;

            return array;
        }

        if (size <= 1024)
        {
            int[] array;
            if (largeArraysNext == largeArrays.size())
            {
                array = new int[1024];
                largeArrays.add(array);
            } else {
                array = largeArrays.get(largeArraysNext);
            }

            largeArraysNext++;

            return array;
        }

        int[] array;
        if (extraLargeArraysNext == extraLargeArrays.size())
        {
            array = new int[size];
            extraLargeArrays.add(array);
        } else {
            array = extraLargeArrays.get(extraLargeArraysNext);
            if (array.length < size)
            {
                array = new int[size];
                extraLargeArrays.set(extraLargeArraysNext, array);
            }
        }

        extraLargeArraysNext++;

        return array;
    }
}
