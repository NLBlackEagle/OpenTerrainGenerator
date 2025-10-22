package com.pg85.otg.customobjects.bo3.bo3function;

import java.io.DataInput;
import java.io.IOException;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
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
    
    public BO3ModDataFunction rotate()
    {
        BO3ModDataFunction rotatedBlock = new BO3ModDataFunction();
        rotatedBlock.x(z());
        rotatedBlock.y(y());
        rotatedBlock.z(-x());
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
