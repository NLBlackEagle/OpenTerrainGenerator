package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalWorld;

public class LayerZoomFuzzy extends LayerZoom
{

    LayerZoomFuzzy(long seed, LocalWorld world, Layer childLayer)
    {
        super(seed, world, childLayer);
    }

    @Override
    protected int mostCommonOrRandom(int a, int b, int c, int d)
    {
        switch (this.nextInt(4)){
            case 0: return a;
            case 1: return b;
            case 2: return c;
            case 3: default: return d;
        }
    }
}