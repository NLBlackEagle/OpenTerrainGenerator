package com.pg85.otg.customobjects.bo3.bo3function;

import java.io.DataInput;
import java.io.IOException;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public class BO3SpawnerFunction extends SpawnerFunction<BO3Config>
{
    public static BO3SpawnerFunction read(DataInput in) throws IOException
    {
        BO3SpawnerFunction spawnerFunction = new BO3SpawnerFunction();

        spawnerFunction.x = in.readInt();
        spawnerFunction.y = in.readInt();
        spawnerFunction.z = in.readInt();
        spawnerFunction.mobName = StreamHelper.readStringFromStream(in);
        spawnerFunction.originalnbtFileName = StreamHelper.readStringFromStream(in);
        spawnerFunction.nbtFileName = StreamHelper.readStringFromStream(in);
        spawnerFunction.groupSize = in.readInt();
        spawnerFunction.interval = in.readInt();
        spawnerFunction.spawnChance = in.readInt();
        spawnerFunction.maxCount = in.readInt();
        spawnerFunction.despawnTime = in.readInt();
        spawnerFunction.velocityX = in.readDouble();
        spawnerFunction.velocityY = in.readDouble();
        spawnerFunction.velocityZ = in.readDouble();
        spawnerFunction.velocityXSet = in.readByte() != 0;
        spawnerFunction.velocityYSet = in.readByte() != 0;
        spawnerFunction.velocityZSet = in.readByte() != 0;
        spawnerFunction.yaw = in.readFloat();
        spawnerFunction.pitch = in.readFloat();

        return spawnerFunction;
    }
    
    public BO3SpawnerFunction rotate()
    {
    	BO3SpawnerFunction rotatedBlock = new BO3SpawnerFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.mobName = mobName;

        rotatedBlock.originalnbtFileName = originalnbtFileName;
        rotatedBlock.nbtFileName = nbtFileName;

        rotatedBlock.groupSize = groupSize;
        rotatedBlock.interval = interval;
        rotatedBlock.spawnChance = spawnChance;
        rotatedBlock.maxCount = maxCount;
        rotatedBlock.despawnTime = despawnTime;

        rotatedBlock.velocityX = velocityZ;
        rotatedBlock.velocityY = velocityY;
        rotatedBlock.velocityZ = -velocityX;

        rotatedBlock.velocityXSet = velocityZSet;
        rotatedBlock.velocityYSet = velocityYSet;
        rotatedBlock.velocityZSet = velocityXSet;

        rotatedBlock.yaw = yaw; // TODO: Rotate! +90 or -90?
        rotatedBlock.pitch = pitch;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public SpawnerFunction<BO3Config> getNewInstance()
	{
		return new BO3SpawnerFunction();
	}
}
