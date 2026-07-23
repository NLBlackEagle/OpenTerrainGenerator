package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeGroup;
import com.pg85.otg.configuration.biome.BiomeGroupManager;
import com.pg85.otg.generator.biome.ArraysCache;
import com.pg85.otg.util.WeightedList;

import java.util.function.IntUnaryOperator;

public class LayerBiomeGroups extends Layer
{

    private BiomeGroupManager biomeGroupManager;
    private int depth;
    private boolean freezeGroups;

    LayerBiomeGroups(long seed, LocalWorld world, Layer paramGenLayer, BiomeGroupManager biomeGroups, int depth)
    {
        super(seed, world);
        this.child = paramGenLayer;
        this.biomeGroupManager = biomeGroups;
        this.depth = depth;
        this.freezeGroups = world.getConfigs().getWorldConfig().freezeAllColdGroupBiomes;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] childInts = this.child.getInts(world, arraysCache, x, z, x_size, z_size);
        int[] thisInts = arraysCache.getArray(x_size * z_size);

        int currentPiece;
        boolean improvedBiomeGroups = world.getConfigs().getWorldConfig().improvedBiomeGroups;
        IntUnaryOperator rng = improvedBiomeGroups ? this::nextGroupIntEntropy : this::nextGroupInt;

        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
            	if(improvedBiomeGroups)
            	{
            		initChunkSeed(j + x, i + z);
            	}
                initGroupSeed(j + x, i + z);
            	
                currentPiece = childInts[(j + i * x_size)];

                if ((currentPiece & LandBit) != 0 && (currentPiece & BiomeGroupBits) == 0) // land without biome group
                {
                	// TODO: even with rarity 1 this always spawns the biome

                    WeightedList<BiomeGroup> weightedGroups = biomeGroupManager.getGroupDepthMap(depth);
                    if(!weightedGroups.isEmpty())
                    {
                        BiomeGroup group = weightedGroups.getRandom(rng);
                        if(group != null)
                        {
                            currentPiece |= (group.getGroupId() << BiomeGroupShift) |
                            //>>    If the average temp of the group is cold
                            ((group.isColdGroup() && freezeGroups) ? IceBit : 0);
                        }
                    }
                }
                thisInts[(j + i * x_size)] = currentPiece;
            }
        }
        return thisInts;
    }
}