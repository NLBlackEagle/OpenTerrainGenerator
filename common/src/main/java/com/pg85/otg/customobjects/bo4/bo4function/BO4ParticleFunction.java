package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public class BO4ParticleFunction extends ParticleFunction<BO4Config>
{
    public static BO4ParticleFunction read(DataInput in) throws IOException
    {
        BO4ParticleFunction particleFunction = new BO4ParticleFunction();

        particleFunction.x(in.readInt());
        particleFunction.y(in.readInt());
        particleFunction.z(in.readInt());
        particleFunction.particleName = StreamHelper.readStringFromStream(in);
        particleFunction.interval = in.readDouble();
        particleFunction.velocityX = in.readDouble();
        particleFunction.velocityY = in.readDouble();
        particleFunction.velocityZ = in.readDouble();
        particleFunction.velocityXSet = in.readByte() != 0;
        particleFunction.velocityYSet = in.readByte() != 0;
        particleFunction.velocityZSet = in.readByte() != 0;

        return particleFunction;
    }
    
    public BO4ParticleFunction rotate(Rotation rotation)
    {
    	BO4ParticleFunction rotatedBlock = new BO4ParticleFunction();

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x(), y(), z(), rotation);

        rotatedBlock.x(rotatedCoords.getX());
        rotatedBlock.y(rotatedCoords.getY());
        rotatedBlock.z(rotatedCoords.getZ());

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

    	rotatedBlock.particleName = particleName;
    	rotatedBlock.interval = interval;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }

	@Override
	public ParticleFunction<BO4Config> getNewInstance()
	{
		return new BO4ParticleFunction();
	}
	
    public void writeToStream(DataOutput stream) throws IOException
    {
        stream.writeInt(this.x());
        stream.writeInt(this.y());
        stream.writeInt(this.z());       

        stream.writeBoolean(this.firstSpawn);

    	StreamHelper.writeStringToStream(stream, this.particleName);

        stream.writeDouble(this.interval);
        stream.writeDouble(this.intervalOffset);

        stream.writeDouble(this.velocityX);
        stream.writeDouble(this.velocityY);
        stream.writeDouble(this.velocityZ);

        stream.writeBoolean(this.velocityXSet);
        stream.writeBoolean(this.velocityYSet);
        stream.writeBoolean(this.velocityZSet);
    }
    
    public static BO4ParticleFunction fromStream(BO4Config holder, DataInput in) throws IOException
    {
    	BO4ParticleFunction particleFunction = new BO4ParticleFunction();
    	
    	particleFunction.x(in.readInt());
    	particleFunction.y(in.readInt());
    	particleFunction.z(in.readInt());
    	
    	particleFunction.firstSpawn = in.readByte() != 0;
    	particleFunction.particleName = StreamHelper.readStringFromStream(in);
    	particleFunction.interval = in.readDouble();
    	particleFunction.intervalOffset = in.readDouble();
    	particleFunction.velocityX = in.readDouble();
    	particleFunction.velocityY = in.readDouble();
    	particleFunction.velocityZ = in.readDouble();
    	
    	particleFunction.velocityXSet = in.readByte() != 0;
    	particleFunction.velocityYSet = in.readByte() != 0;
    	particleFunction.velocityZSet = in.readByte() != 0;
    	
    	return particleFunction;
    }
}
