package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerBiomeBorder extends Layer
{
    private boolean[][] bordersFrom;
    private int[] bordersTo;

    LayerBiomeBorder(long seed, LocalWorld world)
    {
        super(seed, world);
        this.bordersFrom = new boolean[world.getMaxBiomesCount()][];
        this.bordersTo = new int[world.getMaxBiomesCount()];
    }

    void addBiome(LocalBiome replaceTo, int replaceFrom, LocalWorld world)
    {
        this.bordersFrom[replaceFrom] = new boolean[world.getMaxBiomesCount()];

        for (int i = 0; i < this.bordersFrom[replaceFrom].length; i++)
        {
        	LocalBiome biome = world.getBiomeByOTGIdOrNull(i);            
            this.bordersFrom[replaceFrom][i] = biome == null || !replaceTo.getBiomeConfig().notBorderNear.contains(biome.getName());
        }
        this.bordersTo[replaceFrom] = replaceTo.getIds().getOTGBiomeId();
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childBiomeData = this.child.getInts(world, cache, x - 1, z - 1, xSize + 2, zSize + 2);
        int[] biomeData = cache.getArray(xSize * zSize);

        if(world.getConfigs().getWorldConfig().improvedBiomeBorders)
        {
            for(int z1 = 0; z1 < zSize; z1++)
            {
                int biomeDataNC = childBiomeData[(z1 + 0) * (xSize + 2) + 0];
                int biomeDataNE = childBiomeData[(z1 + 0) * (xSize + 2) + 1];
                int biomeDataCC = childBiomeData[(z1 + 1) * (xSize + 2) + 0];
                int biomeDataCE = childBiomeData[(z1 + 1) * (xSize + 2) + 1];
                int biomeDataSC = childBiomeData[(z1 + 2) * (xSize + 2) + 0];
                int biomeDataSE = childBiomeData[(z1 + 2) * (xSize + 2) + 1];
                int biomeNW = 0;
                int biomeNC = getBiomeFromLayer(biomeDataNC);
                int biomeNE = getBiomeFromLayer(biomeDataNE);
                int biomeCW = 0;
                int biomeCC = getBiomeFromLayer(biomeDataCC);
                int biomeCE = getBiomeFromLayer(biomeDataCE);
                int biomeSW = 0;
                int biomeSC = getBiomeFromLayer(biomeDataSC);
                int biomeSE = getBiomeFromLayer(biomeDataSE);
                for(int x1 = 0; x1 < xSize; x1++)
                {
                    biomeDataNC = biomeDataNE;
                    biomeDataNE = childBiomeData[(z1 + 0) * (xSize + 2) + (x1 + 2)];
                    biomeDataCC = biomeDataCE;
                    biomeDataCE = childBiomeData[(z1 + 1) * (xSize + 2) + (x1 + 2)];
                    biomeDataSC = biomeDataSE;
                    biomeDataSE = childBiomeData[(z1 + 2) * (xSize + 2) + (x1 + 2)];
                    biomeNW = biomeNC;
                    biomeNC = biomeNE;
                    biomeNE = getBiomeFromLayer(biomeDataNE);
                    biomeCW = biomeCC;
                    biomeCC = biomeCE;
                    biomeCE = getBiomeFromLayer(biomeDataCE);
                    biomeSW = biomeSC;
                    biomeSC = biomeSE;
                    biomeSE = getBiomeFromLayer(biomeDataSE);

                    int biomeDataAt = biomeDataCC;
                    boolean[] biomeFrom;
                    if((biomeFrom = bordersFrom[biomeCC]) != null)
                    {
                        if(biomeFrom[biomeCW] && biomeFrom[biomeCE] && biomeFrom[biomeNC] && biomeFrom[biomeSC])
                        {
                            if(biomeCW != biomeCC || biomeCE != biomeCC || biomeNC != biomeCC || biomeSC != biomeCC)
                            {
                                biomeDataAt = (biomeDataAt & (IslandBit | RiverBits | IceBit)) | LandBit | bordersTo[biomeCC] | BiomeBitsAreSetBit;
                            }
                            else
                            {
                                if(biomeFrom[biomeNW] && biomeFrom[biomeNE] && biomeFrom[biomeSW] && biomeFrom[biomeSE])
                                {
                                    if(biomeNW != biomeCC || biomeNE != biomeCC || biomeSW != biomeCC || biomeSE != biomeCC)
                                    {
                                        biomeDataAt = (biomeDataAt & (IslandBit | RiverBits | IceBit)) | LandBit | bordersTo[biomeCC] | BiomeBitsAreSetBit;
                                    }
                                }
                            }
                        }
                    }
                    biomeData[z1 * xSize + x1] = biomeDataAt;
                }
            }
        }
        else
        {
            for(int z1 = 0; z1 < zSize; z1++)
            {
                int biomeDataN = 0;
                int biomeDataC = childBiomeData[(z1 + 1) * (xSize + 2) + 0];
                int biomeDataE = childBiomeData[(z1 + 1) * (xSize + 2) + 1];
                int biomeDataS = 0;
                int biomeN = 0;
                int biomeW = 0;
                int biomeC = getBiomeFromLayer(biomeDataC);
                int biomeE = getBiomeFromLayer(biomeDataE);
                int biomeS = 0;
                for(int x1 = 0; x1 < xSize; x1++)
                {
                    biomeDataN = childBiomeData[(z1 + 0) * (xSize + 2) + (x1 + 1)];
                    biomeDataC = biomeDataE;
                    biomeDataE = childBiomeData[(z1 + 1) * (xSize + 2) + (x1 + 2)];
                    biomeDataS = childBiomeData[(z1 + 2) * (xSize + 2) + (x1 + 1)];
                    biomeN = getBiomeFromLayer(biomeDataN);
                    biomeW = biomeC;
                    biomeC = biomeE;
                    biomeE = getBiomeFromLayer(biomeDataE);
                    biomeS = getBiomeFromLayer(biomeDataS);

                    int biomeDataAt = biomeDataC;
                    boolean[] biomeFrom;
                    if((biomeFrom = bordersFrom[biomeC]) != null)
                    {
                        if(biomeFrom[biomeW] && biomeFrom[biomeE] && biomeFrom[biomeN] && biomeFrom[biomeS])
                        {
                            if(biomeW != biomeC || biomeE != biomeC || biomeN != biomeC || biomeS != biomeC)
                            {
                                biomeDataAt = (biomeDataAt & (IslandBit | RiverBits | IceBit)) | LandBit | bordersTo[biomeC] | BiomeBitsAreSetBit;
                            }
                        }
                    }
                    biomeData[z1 * xSize + x1] = biomeDataAt;
                }
            }
        }

        return biomeData;
    }
}
