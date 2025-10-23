package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.MinecraftObjectFunction;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Represents a block in a BO3.
 */
public class BO3MinecraftObjectFunction extends MinecraftObjectFunction<BO3Config>
{
    public BO3MinecraftObjectFunction rotate(Rotation rotation)
    {
        int rotatedX;
        int rotatedZ;
        switch(rotation)
        {
            case NORTH:
                return this;
            case WEST:
                rotatedX = z();
                rotatedZ = -x();
                break;
            case SOUTH:
                rotatedX = -x();
                rotatedZ = -z();
                break;
            case EAST:
                rotatedX = -z();
                rotatedZ = x();
                break;
            default:
                throw new IllegalArgumentException();
        }

        BO3MinecraftObjectFunction rotatedBlock = new BO3MinecraftObjectFunction();
        rotatedBlock.x(rotatedX);
        rotatedBlock.y(y());
        rotatedBlock.z(rotatedZ);
        rotatedBlock.rotation = rotation.next();

        return rotatedBlock;
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }
}
