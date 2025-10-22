package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents an entity in a BO3.
 */
public class BO4EntityFunction extends EntityFunction<BO4Config>
{	
    public BO4EntityFunction rotate(Rotation rotation)
    {
    	BO4EntityFunction rotatedBlock = new BO4EntityFunction();

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x(), y(), z(), rotation);

        rotatedBlock.x(rotatedCoords.getX());
        rotatedBlock.y(rotatedCoords.getY());
        rotatedBlock.z(rotatedCoords.getZ());

        rotatedBlock.name = name;
        rotatedBlock.resourceLocation = resourceLocation;
        rotatedBlock.groupSize = groupSize;
        rotatedBlock.originalNameTagOrNBTFileName = originalNameTagOrNBTFileName;
        rotatedBlock.nameTagOrNBTFileName = nameTagOrNBTFileName;
        rotatedBlock.namedBinaryTag = namedBinaryTag;
        rotatedBlock.rotation = rotation.getRotationId();

        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }

	@Override
	public EntityFunction<BO4Config> createNewInstance()
	{
		return new BO4EntityFunction();
	}
	
    public void writeToStream(DataOutput stream) throws IOException
    {    	
        stream.writeInt(this.x());
        stream.writeInt(this.y());
        stream.writeInt(this.z());       

        StreamHelper.writeStringToStream(stream, this.resourceLocation);
        stream.writeInt(this.groupSize);
        StreamHelper.writeStringToStream(stream, this.nameTagOrNBTFileName);
        StreamHelper.writeStringToStream(stream, this.originalNameTagOrNBTFileName);        	
    }
    
    public static BO4EntityFunction fromStream(BO4Config holder, DataInput in) throws IOException
    {
    	BO4EntityFunction entityFunction = new BO4EntityFunction();
    	   	
    	entityFunction.x(in.readInt());
    	entityFunction.y(in.readInt());
    	entityFunction.z(in.readInt());

    	entityFunction.processEntityName(StreamHelper.readStringFromStream(in));
    	entityFunction.groupSize = in.readInt();
    	entityFunction.nameTagOrNBTFileName= StreamHelper.readStringFromStream(in);
    	entityFunction.originalNameTagOrNBTFileName= StreamHelper.readStringFromStream(in);
    	if (entityFunction.originalNameTagOrNBTFileName != null)
    	{
    	    entityFunction.processNameTagOrFileName(holder, entityFunction.originalNameTagOrNBTFileName);
    	}
    	entityFunction.rotation = 0;
    	
    	return entityFunction;
    }
}
