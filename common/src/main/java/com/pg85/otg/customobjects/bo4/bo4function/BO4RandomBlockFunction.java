package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import com.pg85.otg.common.BlockContainer;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.MaterialHelper;

public class BO4RandomBlockFunction extends BO4BlockFunction
{
    public BlockContainer[] blockContainers;
    public byte[] blockChances;

    public byte blockCount = 0;   

    @Override
    public void load(BO4Config holder, List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        x(readInt(args.get(0), -100, 100));
        y((short) readInt(args.get(1), -1000, 1000));
        z(readInt(args.get(2), -100, 100));

        // Now read the random parts
        int i = 3;
        int size = args.size();

        // Get number of blocks first, params can vary so can't just count.
        while (i < size)
        {           
        	i++;
            if (i >= size)
            {
                throw new InvalidConfigException("Missing chance parameter");
            }
            try
            {
                readInt(args.get(i), 1, 100);
            }
            catch (InvalidConfigException e)
            {
                // Get the chance
                i++;
                if (i >= size)
                {
                    throw new InvalidConfigException("Missing chance parameter");
                }
                readInt(args.get(i), 1, 100);
            }
            i++;
            blockCount++;
        }
        
        this.blockContainers = new BlockContainer[blockCount];
        this.blockChances = new byte[blockCount];
        
        i = 3;
        blockCount = 0;
        while (i < size)
        {
            // Parse chance and metadata
        	LocalMaterialData material = MaterialHelper.readMaterial(args.get(i));
            i++;
            if (i >= size)
            {
                throw new InvalidConfigException("Missing chance parameter");
            }
            try
            {
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
                blockContainers[blockCount] = material.blockContainer();
            }
            catch (InvalidConfigException e)
            {
                // Maybe it's a NBT file?

                // Get the file
                blockContainers[blockCount] = material.blockContainer(holder.getFile().getParentFile().toPath(), args.get(i));

                // Get the chance
                i++;
                if (i >= size)
                {
                    throw new InvalidConfigException("Missing chance parameter");
                }
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
            }

            i++;
            blockCount++;
        }
    }
    
    public BO4RandomBlockFunction rotate(Rotation rotation)
    {
        BO4RandomBlockFunction rotatedBlock = new BO4RandomBlockFunction();

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x(), y(), z(), rotation);
        rotatedBlock.x(rotatedCoords.getX());
        rotatedBlock.y(rotatedCoords.getY());
        rotatedBlock.z(rotatedCoords.getZ());
        rotatedBlock.blockContainers = new BlockContainer[blockCount];
        for(int i = 0; i < blockCount; i++)
        {
            rotatedBlock.blockContainers[i] = blockContainers[i].rotate(4 - rotation.getRotationId());
        }
        rotatedBlock.blockCount = blockCount;
        rotatedBlock.blockChances = blockChances;
        return rotatedBlock;
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, boolean replaceBlock)
    {
        for (int i = 0; i < blockCount; i++)
        {
            if (random.nextInt(100) < blockChances[i])
            {
                world.setBlock(x, y, z, blockContainers[i].material(), blockContainers[i].tag(), chunkBeingPopulated, true);
                break;
            }
        }
    }
    
    @Override
    public String makeString()
    {
        String text = "RandomBlock(" + x() + "," + y() + "," + z();
        for (int i = 0; i < blockCount; i++)
        {
            if (blockContainers[i].hasTag())
            {
                text += "," + blockContainers[i].material() + "," + blockChances[i];
            } else
            {
                text += "," + blockContainers[i].material() + "," + blockContainers[i].tagPath() + "," + blockChances[i];
            }
        }
        return text + ")";
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }
    
    @Override
    public void writeToStream(String[] metaDataNames, LocalMaterialData[] materials, DataOutput stream) throws IOException
    {
        stream.writeShort(this.y());
        
        stream.writeByte(this.blockContainers.length);
        
        boolean bFound;
        for(int i = 0; i < this.blockContainers.length; i++)
        {
        	byte blockChance = this.blockChances[i];
        	stream.writeByte(blockChance);
        	
        	bFound = false;
        	if(this.blockContainers[i] != null)
        	{
		        for(int j = 0; j < materials.length; j++)
		        {
		        	if(materials[j].equals(this.blockContainers[i].material()))
		        	{
		        		stream.writeShort(j);
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
        
        boolean metaDataFound = false;
        for(int i = 0; i < this.blockContainers.length; i++)
        {
        	if(this.blockContainers[i].hasTag())
        	{
        		metaDataFound = true;
        		break;
        	}
        }
        
        if(metaDataFound)
        {
        	stream.writeByte(this.blockContainers.length);
	        for(int i = 0; i < this.blockContainers.length; i++)
	        {
	        	bFound = false;
	        	if(this.blockContainers[i].hasTag())
	        	{
		            for(int j = 0; j < metaDataNames.length; j++)
		            {
		            	if(metaDataNames[j].equals(this.blockContainers[i].tagPath()))
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
        } else {
        	stream.writeByte(0);
        }
    }
    
    public static BO4RandomBlockFunction fromStream(int x, int z, String[] metaDataNames, LocalMaterialData[] materials, BO4Config holder, DataInput in) throws IOException
    {    	
    	BO4RandomBlockFunction rbf = new BO4RandomBlockFunction();
    	
    	rbf.x(x);
    	rbf.y(in.readShort());
    	rbf.z(z);

    	byte blocksLength = in.readByte();
    	
    	rbf.blockCount = blocksLength;
    	rbf.blockContainers = new BlockContainer[blocksLength];
    	rbf.blockChances = new byte[blocksLength];

    	LocalMaterialData[] blocks = new LocalMaterialData[blocksLength];
    	for(int i = 0; i < blocksLength; i++)
    	{
    		rbf.blockChances[i] = in.readByte();
        	short materialId = in.readShort();
        	if(materialId != -1)
        	{
        		blocks[i] = materials[materialId];
        	}
    	}
    	
        boolean hasMetaDataTags = in.readByte() != -1;
        for(int i = 0; i < blocksLength; i++)
        {
            short metaDataNameId;
            String metaDataName;
            if(hasMetaDataTags && (metaDataNameId = in.readShort()) != -1 && (metaDataName = metaDataNames[metaDataNameId]) != null)
            {
                rbf.blockContainers[i] = materials[i].blockContainer(holder.getFile().getParentFile().toPath(), metaDataName);
            }
            else
            {
                rbf.blockContainers[i] = materials[i].blockContainer();
            }
        }
    	
    	return rbf;
    }
}
