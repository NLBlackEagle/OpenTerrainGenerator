package com.pg85.otg.customobjects.bofunctions;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import com.pg85.otg.configuration.customobjects.CustomObjectConfigFile;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public abstract class ParticleFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T>
{
	public Boolean firstSpawn = true;

    public String particleName = "";

    public double interval = 1;
    public double intervalOffset = 0;

    public double velocityX = 0;
    public double velocityY = 0;
    public double velocityZ = 0;

    public boolean velocityXSet = false;
    public boolean velocityYSet = false;
    public boolean velocityZSet = false;

    public static void write(DataOutput out, ParticleFunction<?> particleFunction) throws IOException
    {
        out.writeInt(particleFunction.x());
        out.writeInt(particleFunction.y());
        out.writeInt(particleFunction.z());
        StreamHelper.writeStringToStream(out, particleFunction.particleName.replace(":", "&#58;").replace(" ", "&nbsp;"));
        out.writeDouble(particleFunction.interval);
        out.writeDouble(particleFunction.velocityX);
        out.writeDouble(particleFunction.velocityY);
        out.writeDouble(particleFunction.velocityZ);
        out.writeBoolean(particleFunction.velocityXSet);
        out.writeBoolean(particleFunction.velocityYSet);
        out.writeBoolean(particleFunction.velocityZSet);
    }
    
    @Override
    public void load(T holder, List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
    	x(readInt(args.get(0), -100, 100));
		y(readInt(args.get(1), -1000, 1000));
        z(readInt(args.get(2), -100, 100));
        particleName = args.get(3);

        interval = readDouble(args.get(4), 0, Integer.MAX_VALUE);

        if(args.size() > 5)
        {
        	velocityX = readDouble(args.get(5), Integer.MIN_VALUE, Integer.MAX_VALUE);
        	velocityXSet = true;
        }
        if(args.size() > 6)
        {
        	velocityY = readDouble(args.get(6), Integer.MIN_VALUE, Integer.MAX_VALUE);
        	velocityYSet = true;
        }
        if(args.size() > 7)
        {
        	velocityZ = readDouble(args.get(7), Integer.MIN_VALUE, Integer.MAX_VALUE);
        	velocityZSet = true;
        }
    }

    @Override
    public String makeString()
    {
    	return "Particle(" + x() + ',' + y() + ',' + z() + ',' + particleName + ',' + interval + ',' + velocityX + ',' + velocityY + ',' + velocityZ + ')';
    }

    public String makeStringForPacket()
    {
    	return "Particle(" + x() + ',' + y() + ',' + z() + ',' + particleName + ',' + interval + ',' + velocityX + ',' + velocityY + ',' + velocityZ + ',' + velocityXSet + ',' + velocityYSet + ',' + velocityZSet + ')';
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        ParticleFunction<T> block = (ParticleFunction<T>) other;
        return block.x() == x() && block.y() == y() && block.z() == z() && block.particleName.equalsIgnoreCase(particleName) && block.interval == interval && block.velocityX == velocityX && block.velocityY == velocityY && block.velocityZ == velocityZ;
    }
    
    public abstract ParticleFunction<T> getNewInstance();
}
