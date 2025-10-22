package com.pg85.otg.customobjects.bofunctions;

import java.io.DataInput;
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
public abstract class ModDataFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T>
{
    public String modId;
    public String modData;
    
    public static void write(DataOutput out, ModDataFunction<?> modDataFunction) throws IOException
    {
        out.writeInt(modDataFunction.x());
        out.writeInt(modDataFunction.y());
        out.writeInt(modDataFunction.z());
        StreamHelper.writeStringToStream(out, modDataFunction.modId.replace(":", "&#58;").replace(" ", "&nbsp;"));
        StreamHelper.writeStringToStream(out, modDataFunction.modData.replace(":", "&#58;").replace(" ", "&nbsp;"));
    }
    
    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
		x(readInt(args.get(0), -100, 100));
        y(readInt(args.get(1), -1000, 1000));
        z(readInt(args.get(2), -100, 100));
        modId = args.get(3);
        modData = args.get(4);
    }

    @Override
    public String makeString()
    {
        return "ModData(" + x() + ',' + y() + ',' + z() + ',' + modId + ',' + modData + ')';
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        ModDataFunction<T> block = (ModDataFunction<T>) other;
        return block.x() == x() && block.y() == y() && block.z() == z() && block.modId.equalsIgnoreCase(modId) && block.modData.equalsIgnoreCase(modData);
    }
    
    public abstract ModDataFunction<T> getNewInstance();
}
