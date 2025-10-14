package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeGroup;
import com.pg85.otg.configuration.biome.BiomeGroupManager;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerBiome extends Layer
{
    private BiomeGroupManager manager;
    private int depth;
    private double freezeTemp;

    LayerBiome(long seed, LocalWorld world, Layer childLayer, BiomeGroupManager groupManager, int depth)
    {
        super(seed, world);
        this.child = childLayer;
        this.manager = groupManager;
        this.depth = depth;
        this.freezeTemp = world.getConfigs().getWorldConfig().frozenOceanTemperature;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

        int currentPiece;
        
        for (int i = 0; i < zSize; i++)
        {
            for (int j = 0; j < xSize; j++)
            {
                initChunkSeed(j + x, i + z);
                currentPiece = childInts[(j + i * xSize)];

                if ((currentPiece & BiomeGroupBits) != 0 && ((currentPiece & BiomeBitsAreSetBit) == 0 || (currentPiece & BiomeBits) == this.defaultOceanId))    // has biomegroup bits but not biome bits
                {
                    BiomeGroup group = manager.getGroupById((currentPiece & BiomeGroupBits) >> BiomeGroupShift);
                    LocalBiome biome = group.getDepthMapOrHigher(depth).getRandom(this::nextInt);
                    if (biome != null) {
                        currentPiece |= biome.getIds().getOTGBiomeId() |
                            // Set IceBit based on Biome Temperature
                            (biome.getBiomeConfig().biomeTemperature <= freezeTemp ? IceBit : 0) |
                            // Set BiomeBitsAreSetBit
                            BiomeBitsAreSetBit
                            ;
                    }
                }
                thisInts[(j + i * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }
}
