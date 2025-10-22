package com.pg85.otg.customobjects.bo3.bo3function;

import java.util.Random;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.BlockFunction;
import com.pg85.otg.util.ChunkCoordinate;

/**
 * Represents a block in a BO3.
 */
public class BO3BlockFunction extends BlockFunction<BO3Config>
{
    public BO3BlockFunction rotate(int rotation)
    {
        int rotatedX;
        int rotatedZ;
        switch(rotation)
        {
            case 0:
                return this;
            case 1:
                rotatedX = z();
                rotatedZ = -x();
                break;
            case 2:
                rotatedX = -x();
                rotatedZ = -z();
                break;
            case 3:
                rotatedX = -z();
                rotatedZ = x();
                break;
            default:
                throw new IllegalArgumentException();
        }

        BO3BlockFunction rotatedBlock = new BO3BlockFunction();
        rotatedBlock.x(rotatedX);
        rotatedBlock.y(y());
        rotatedBlock.z(rotatedZ);
        rotatedBlock.blockContainer = blockContainer.rotate(rotation);
        return rotatedBlock;
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, boolean replaceBlock)
    {
        world.setBlock(x, y, z, material(), tag(), chunkBeingPopulated, replaceBlock);
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }
}