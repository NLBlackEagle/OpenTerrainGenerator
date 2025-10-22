package com.pg85.otg.customobjects.structures;

import static com.pg85.otg.customobjects.structures.CustomStructureCache.REGION_SIZE;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.pg85.otg.util.ChunkCoordinate;

public class StructureDataRegion
{
    private boolean requiresSave = false;
    private CustomStructure[] structures = new CustomStructure[REGION_SIZE * REGION_SIZE];

    public boolean requiresSave()
    {
        return this.requiresSave;
    }

    public void markSaved()
    {
        this.requiresSave = false;
    }

    public void markSaveRequired()
    {
        this.requiresSave = true;
    }

    public CustomStructure getStructure(int internalX, int internalZ)
    {
        return this.structures[internalX * REGION_SIZE + internalZ];
    }

    public void setStructure(int internalX, int internalZ, CustomStructure structure, boolean requiresSave)
    {
        this.structures[internalX * REGION_SIZE + internalZ] = structure;
        this.requiresSave = this.requiresSave || requiresSave;
    }

    public Stream<Map.Entry<ChunkCoordinate, CustomStructure>> getStructures()
    {
        return IntStream.range(0, REGION_SIZE * REGION_SIZE).filter(i -> structures[i] != null).mapToObj(i -> new SimpleEntry<>(ChunkCoordinate.fromChunkCoords(i / REGION_SIZE, i % REGION_SIZE), structures[i]));
    }
}
