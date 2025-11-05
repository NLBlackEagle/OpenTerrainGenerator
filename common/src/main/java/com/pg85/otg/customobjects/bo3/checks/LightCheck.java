package com.pg85.otg.customobjects.bo3.checks;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

import java.util.List;

/**
 *
 */
public class LightCheck extends BO3Check
{

    /**
     * The minimum Light level, inclusive
     */
    private int minLightLevel;
    /**
     * The maximum Light level, inclusive
     */
    private int maxLightLevel;

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        int lightLevel = world.getLightLevel(x, y, z, chunkBeingPopulated);
        if (lightLevel < minLightLevel || lightLevel > maxLightLevel)
        {
            // Out of bounds
            return true;
        }

        return false;
    }

    @Override
    public void load(BO3Config holder, List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        readXYZ(args, 0);
        minLightLevel = readInt(args.get(3), 0, 16);
        maxLightLevel = readInt(args.get(4), minLightLevel, 16);
    }

    @Override
    public String makeString()
    {
        return "LightCheck(" + x() + ',' + y() + ',' + z() + ',' + minLightLevel + ',' + maxLightLevel + ')';
    }

    @Override
    public BO3Check rotate(Rotation rotation)
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
        LightCheck rotatedCheck = new LightCheck();
        rotatedCheck.x(rotatedX);
        rotatedCheck.y(y());
        rotatedCheck.z(rotatedZ);
        rotatedCheck.minLightLevel = minLightLevel;
        rotatedCheck.maxLightLevel = maxLightLevel;

        return rotatedCheck;
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }
}
