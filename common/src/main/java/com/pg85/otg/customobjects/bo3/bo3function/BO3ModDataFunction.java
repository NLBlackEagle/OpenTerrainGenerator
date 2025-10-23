package com.pg85.otg.customobjects.bo3.bo3function;

import java.io.DataInput;
import java.io.IOException;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public class BO3ModDataFunction extends ModDataFunction<BO3Config>
{
    public static BO3ModDataFunction read(DataInput in) throws IOException
    {
        BO3ModDataFunction modDataFunction = new BO3ModDataFunction();

        modDataFunction.x(in.readInt());
        modDataFunction.y(in.readInt());
        modDataFunction.z(in.readInt());
        modDataFunction.modId = StreamHelper.readStringFromStream(in);
        modDataFunction.modData = StreamHelper.readStringFromStream(in);

        return modDataFunction;
    }
    
    public BO3ModDataFunction rotate(Rotation rotation)
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

        BO3ModDataFunction rotatedBlock = new BO3ModDataFunction();
        rotatedBlock.x(rotatedX);
        rotatedBlock.y(y());
        rotatedBlock.z(rotatedZ);
        rotatedBlock.modId = modId;
        rotatedBlock.modData = modData;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public ModDataFunction<BO3Config> getNewInstance()
	{
		return new BO3ModDataFunction();
	}
}
