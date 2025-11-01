package com.pg85.otg.customobjects.bo3.bo3function;

import java.util.List;
import java.util.Random;

import com.pg85.otg.common.BlockContainer;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.MaterialHelper;

public class BO3RandomBlockFunction extends BO3BlockFunction
{
    public BlockContainer[] blockContainers;
    public byte[] blockChances;

    public byte blockCount = 0;
	
    public BO3RandomBlockFunction rotate(Rotation rotation)
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

        BO3RandomBlockFunction rotatedBlock = new BO3RandomBlockFunction();
        rotatedBlock.x(rotatedX);
        rotatedBlock.y(y());
        rotatedBlock.z(rotatedZ);
        rotatedBlock.blockContainer = blockContainer.rotate(rotation);
        rotatedBlock.blockCount = blockCount;
        rotatedBlock.blockContainers = new BlockContainer[blockCount];
        for(int i = 0; i < blockCount; i++)
        {
            rotatedBlock.blockContainers[i] = blockContainers[i].rotate(rotation);
        }
        rotatedBlock.blockChances = blockChances;
        return rotatedBlock;
    }
    
    @Override
    public void load(BO3Config holder, List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        x(readInt(args.get(0), -100, 100));
        y((short) readInt(args.get(1), -1000, 1000));
        z(readInt(args.get(2), -100, 100));

        // Now read the random parts
        int i = 3;
        int size = args.size();

        // Get number of blocks first, params can vary so can't just count.
        while(i < size)
        {
            i++;
            if(i >= size)
            {
                throw new InvalidConfigException("Missing chance parameter");
            }
            if(!StringHelper.isNumber(args.get(i)))
            {
                i++;
                if(i >= size)
                {
                    throw new InvalidConfigException("Missing chance parameter");
                }
                if(!StringHelper.isNumber(args.get(i)))
                {
                    throw new NumberFormatException("'" + args.get(i) + "'" + " is not a number!");
                }
            }
            i++;
            blockCount++;
        }
        
        this.blockContainers = new BlockContainer[blockCount];
        this.blockChances = new byte[blockCount];
        
        i = 3;
        blockCount = 0;
        while(i < size)
        {
            // Parse chance and metadata
            LocalMaterialData material = MaterialHelper.readMaterial(args.get(i));
            i++;
            if(StringHelper.isNumber(args.get(i)))
            {
                blockContainers[blockCount] = material.blockContainer();
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
                i++;
            }
            else
            {
                blockContainers[blockCount] = material.blockContainer(holder.getFile().getParentFile().toPath(), args.get(i));
                i++;
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
                i++;
            }
            blockCount++;
        }
        blockContainer = blockContainers.length > 0 ? blockContainers[0] : BlockContainer.of(MaterialHelper.AIR);
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
    public void spawn(LocalWorld world, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, boolean replaceBlock)
    {
        for (int i = 0; i < blockCount; i++)
        {
            if (random.nextInt(100) < blockChances[i])
            {
                world.setBlock(x, y, z, blockContainers[i].material(), blockContainers[i].tag(), chunkBeingPopulated, replaceBlock);
                break;
            }
        }
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }
}
