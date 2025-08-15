package com.pg85.otg.generator.biome.layers;

public class LayerZoomFuzzy extends LayerZoom
{

    LayerZoomFuzzy(long seed, int defaultOceanId, Layer childLayer)
    {
        super(seed, defaultOceanId, childLayer);
    }

    @Override
    protected int getRandomOf4(int a, int b, int c, int d)
    {
        switch (this.nextInt(4)){
            case 0: return a;
            case 1: return b;
            case 2: return c;
            case 3: default: return d;
        }
    }
}