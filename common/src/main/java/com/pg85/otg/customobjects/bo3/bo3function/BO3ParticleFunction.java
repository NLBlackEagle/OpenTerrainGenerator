package com.pg85.otg.customobjects.bo3.bo3function;

import java.io.DataInput;
import java.io.IOException;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public class BO3ParticleFunction extends ParticleFunction<BO3Config>
{
    public static BO3ParticleFunction read(DataInput in) throws IOException
    {
        BO3ParticleFunction particleFunction = new BO3ParticleFunction();

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
    
    public BO3ParticleFunction rotate()
    {
    	BO3ParticleFunction rotatedBlock = new BO3ParticleFunction();
        rotatedBlock.x(z());
        rotatedBlock.y(y());
        rotatedBlock.z(-x());
        rotatedBlock.particleName = particleName;

        rotatedBlock.interval = interval;

        rotatedBlock.velocityX = velocityZ;
        rotatedBlock.velocityY = velocityY;
        rotatedBlock.velocityZ = -velocityX;

        rotatedBlock.velocityXSet = velocityZSet;
        rotatedBlock.velocityYSet = velocityYSet;
        rotatedBlock.velocityZSet = velocityXSet;

        return rotatedBlock;
    }

    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public ParticleFunction<BO3Config> getNewInstance()
	{
		return new BO3ParticleFunction();
	}
}
