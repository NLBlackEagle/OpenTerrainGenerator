package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Represents an entity in a BO3.
 */
public class BO3EntityFunction extends EntityFunction<BO3Config>
{
    public BO3EntityFunction rotate(Rotation rotation)
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

    	BO3EntityFunction rotatedBlock = new BO3EntityFunction();
        rotatedBlock.x(rotatedX);
        rotatedBlock.y(y());
        rotatedBlock.z(rotatedZ);
        rotatedBlock.name = name;
        rotatedBlock.resourceLocation = resourceLocation;
        rotatedBlock.groupSize = groupSize;
        rotatedBlock.originalNameTagOrNBTFileName = originalNameTagOrNBTFileName;
        rotatedBlock.nameTagOrNBTFileName = nameTagOrNBTFileName;
        rotatedBlock.namedBinaryTag = namedBinaryTag;
        rotatedBlock.rotation = (this.rotation + rotation.getRotationId()) % 4;

        return rotatedBlock;
    }

    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public EntityFunction<BO3Config> createNewInstance()
	{
		return new BO3EntityFunction();
	}
}
