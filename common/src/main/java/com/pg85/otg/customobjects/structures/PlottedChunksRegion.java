package com.pg85.otg.customobjects.structures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;

public class PlottedChunksRegion
{
    private boolean requiresSave = false;
    private final boolean[] plottedChunks = new boolean[CustomStructureCache.REGION_SIZE * CustomStructureCache.REGION_SIZE];

    public boolean requiresSave()
    {
        return this.requiresSave;
    }

    public void markSaved()
    {
        this.requiresSave = false;
    }

    public boolean getChunk(int internalX, int internalZ)
    {
        return this.plottedChunks[internalX * CustomStructureCache.REGION_SIZE + internalZ];
    }

    public void setChunk(int internalX, int internalZ)
    {
        if(!this.plottedChunks[internalX * CustomStructureCache.REGION_SIZE + internalZ])
        {
            this.plottedChunks[internalX * CustomStructureCache.REGION_SIZE + internalZ] = true;
            this.requiresSave = true;
        }
    }

    public void write(DataOutput out) throws IOException
    {
        write(out, this);
    }

    public static void write(DataOutput out, PlottedChunksRegion region) throws IOException
    {
        out.writeInt(1); // version
        out.writeInt(CustomStructureCache.REGION_SIZE);
        for(boolean plotted : region.plottedChunks)
        {
            out.writeBoolean(plotted);
        }
    }

    public static PlottedChunksRegion read(DataInput in) throws IOException
    {
        in.readInt(); // version
        PlottedChunksRegion region = new PlottedChunksRegion();
        if(in.readInt() != CustomStructureCache.REGION_SIZE)
        {
            OTG.log(LogMarker.INFO, "PlottedChunks region files were corrupted or exported with an incompatible version of OTG, ignoring.");
            Arrays.fill(region.plottedChunks, true);
        }
        else
        {
            for(int i = 0; i < region.plottedChunks.length; i++)
            {
                region.plottedChunks[i] = in.readBoolean();
            }
        }
        return region;
    }
}
