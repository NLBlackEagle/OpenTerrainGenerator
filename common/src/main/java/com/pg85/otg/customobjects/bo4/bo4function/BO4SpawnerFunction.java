package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public class BO4SpawnerFunction extends SpawnerFunction<BO4Config>
{	
	public BO4SpawnerFunction() { }
	
	public BO4SpawnerFunction(BO4Config holder)
	{
		this.holder = holder;
	}
	
    public static BO4SpawnerFunction read(DataInput in) throws IOException
    {
        BO4SpawnerFunction spawnerFunction = new BO4SpawnerFunction();

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
	
    public BO4SpawnerFunction rotate(Rotation rotation)
    {
    	BO4SpawnerFunction rotatedBlock = new BO4SpawnerFunction(this.getHolder());

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.velocityX = velocityX;
        rotatedBlock.velocityY = velocityY;
        rotatedBlock.velocityZ = velocityZ;

        rotatedBlock.velocityXSet = velocityXSet;
        rotatedBlock.velocityYSet = velocityYSet;
        rotatedBlock.velocityZSet = velocityZSet;

        double newVelocityX = rotatedBlock.velocityX;
        double newVelocityZ = rotatedBlock.velocityZ;

        boolean newVelocityXSet = rotatedBlock.velocityXSet;
        boolean newVelocityZSet = rotatedBlock.velocityZSet;

    	for(int i = 0; i < rotation.getRotationId(); i++)
    	{
            newVelocityX = rotatedBlock.velocityZ;
            newVelocityZ = -rotatedBlock.velocityX;

            rotatedBlock.velocityX = newVelocityX;
            rotatedBlock.velocityY = rotatedBlock.velocityY;
            rotatedBlock.velocityZ = newVelocityZ;

            newVelocityXSet = rotatedBlock.velocityZSet;
            newVelocityZSet = rotatedBlock.velocityXSet;

            rotatedBlock.velocityXSet = newVelocityXSet;
            rotatedBlock.velocityYSet = rotatedBlock.velocityYSet;
            rotatedBlock.velocityZSet = newVelocityZSet;
    	}

        rotatedBlock.mobName = mobName;

        rotatedBlock.originalnbtFileName = originalnbtFileName;
        rotatedBlock.nbtFileName = nbtFileName;

        rotatedBlock.groupSize = groupSize;
        rotatedBlock.interval = interval;
        rotatedBlock.spawnChance = spawnChance;
        rotatedBlock.maxCount = maxCount;
        rotatedBlock.despawnTime = despawnTime;

        rotatedBlock.yaw = yaw; // TODO: Rotate! +90 or -90?
        rotatedBlock.pitch = pitch;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }

	@Override
	public SpawnerFunction<BO4Config> getNewInstance()
	{
		return new BO4SpawnerFunction(this.getHolder());
	}
	
    public void writeToStream(DataOutput stream) throws IOException
    {
        stream.writeInt(this.x);
        stream.writeInt(this.y);
        stream.writeInt(this.z);

        stream.writeBoolean(this.firstSpawn);

    	StreamHelper.writeStringToStream(stream, this.mobName);

    	StreamHelper.writeStringToStream(stream, this.nbtFileName);
    	StreamHelper.writeStringToStream(stream, this.originalnbtFileName);
    	stream.writeInt(this.groupSize);
    	
        stream.writeInt(this.interval);
        stream.writeInt(this.intervalOffset);

        stream.writeInt(this.spawnChance);
        stream.writeInt(this.maxCount);

        stream.writeInt(this.despawnTime);
      
        stream.writeDouble(this.velocityX);
        stream.writeDouble(this.velocityY);
        stream.writeDouble(this.velocityZ);

        stream.writeFloat(this.yaw);
        stream.writeFloat(this.pitch);
       
        stream.writeBoolean(this.velocityXSet);
        stream.writeBoolean(this.velocityYSet);
        stream.writeBoolean(this.velocityZSet);

        StreamHelper.writeStringToStream(stream, this.metaDataTag);
        stream.writeBoolean(this.metaDataProcessed);
    }
    
    public static BO4SpawnerFunction fromStream(BO4Config holder, DataInput in) throws IOException
    {
    	BO4SpawnerFunction spawnerFunction = new BO4SpawnerFunction(holder);
    	
    	spawnerFunction.x = in.readInt();
    	spawnerFunction.y = in.readInt();
    	spawnerFunction.z = in.readInt();
    	
    	spawnerFunction.firstSpawn = in.readByte() != 0;

    	spawnerFunction.mobName = StreamHelper.readStringFromStream(in);

    	spawnerFunction.nbtFileName = StreamHelper.readStringFromStream(in);
    	spawnerFunction.originalnbtFileName = StreamHelper.readStringFromStream(in);
    	spawnerFunction.groupSize = in.readInt();
    	
    	spawnerFunction.interval = in.readInt();
    	spawnerFunction.intervalOffset = in.readInt();
    	
    	spawnerFunction.spawnChance = in.readInt();
    	spawnerFunction.maxCount = in.readInt();

    	spawnerFunction.despawnTime = in.readInt();
    	
    	spawnerFunction.velocityX = in.readDouble();
    	spawnerFunction.velocityY = in.readDouble();
    	spawnerFunction.velocityZ = in.readDouble();
    	
    	spawnerFunction.yaw = in.readFloat();
    	spawnerFunction.pitch = in.readFloat();
    	
    	spawnerFunction.velocityXSet = in.readByte() != 0;
    	spawnerFunction.velocityYSet = in.readByte() != 0;
    	spawnerFunction.velocityZSet = in.readByte() != 0;
    	
    	spawnerFunction.metaDataTag = StreamHelper.readStringFromStream(in);
    	spawnerFunction.metaDataProcessed = in.readByte() != 0;
    	
    	return spawnerFunction;
    }
}
