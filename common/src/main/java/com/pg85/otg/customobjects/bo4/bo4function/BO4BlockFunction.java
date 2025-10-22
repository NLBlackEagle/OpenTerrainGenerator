package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Random;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.BlockFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Represents a block in a BO3.
 */
public class BO4BlockFunction extends BlockFunction<BO4Config>
{	
    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, boolean replaceBlock)
    {
        world.setBlock(x, y, z, material(), tag(), chunkBeingPopulated, true);
    }
    
    public BO4BlockFunction rotate(Rotation rotation)
    {
        BO4BlockFunction rotatedBlock = new BO4BlockFunction();

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x(), y(), z(), rotation);
        rotatedBlock.x(rotatedCoords.getX());
        rotatedBlock.y(rotatedCoords.getY());
        rotatedBlock.z(rotatedCoords.getZ());
        rotatedBlock.blockContainer = blockContainer.rotate(4 - rotation.getRotationId());
        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }
        
    public void writeToStream(String[] metaDataNames, LocalMaterialData[] materials, DataOutput stream) throws IOException
    {
        stream.writeShort(this.y());
        boolean bFound = false;
        if(this.material() != null)
        {
	        for(int i = 0; i < materials.length; i++)
	        {
	        	if(materials[i].equals(this.material()))
	        	{
	        		stream.writeShort(i);
	        		bFound = true;
	        		break;
	        	}
	        }
        }
        if(!bFound)
        {
        	stream.writeShort(-1);
        }
        bFound = false;
        if(this.hasTag())
        {
	        for(int i = 0; i < metaDataNames.length; i++)
	        {        	
	        	if(metaDataNames[i].equals(this.tagPath()))
	        	{
	        		stream.writeShort(i);
	        		bFound = true;
	        		break;
	        	}
	        }
        }
        if(!bFound)
        {
        	stream.writeShort(-1);
        }
    }
        
    public static BO4BlockFunction fromStream(int x, int z, String[] metaDataNames, LocalMaterialData[] materials, BO4Config holder, DataInput in) throws IOException
    {
    	BO4BlockFunction rbf = new BO4BlockFunction();
    	   	
    	rbf.x(x);
    	rbf.y(in.readShort());
    	rbf.z(z);
    	
        short materialId = in.readShort();
        short metaDataNameId = in.readShort();
        if(metaDataNameId != -1)
        {
            rbf.blockContainer = materials[materialId].blockContainer(holder.getFile().getParentFile().toPath(), metaDataNames[metaDataNameId]);
        }
        else
        {
            rbf.blockContainer = materials[materialId].blockContainer();
        }
    	
    	return rbf;
    }
}