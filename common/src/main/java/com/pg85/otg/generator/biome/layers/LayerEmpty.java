package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerEmpty extends Layer
{
    LayerEmpty(long seed, int defaultOceanId)
    {
        super(seed, defaultOceanId);
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        return cache.getArray(xSize * zSize);
    }
}
