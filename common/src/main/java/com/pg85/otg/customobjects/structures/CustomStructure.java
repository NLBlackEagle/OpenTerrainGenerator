package com.pg85.otg.customobjects.structures;

import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;
import com.pg85.otg.util.ChunkCoordinate;
import java.util.*;

/**
 * Represents a collection of all {@link CustomObject}s in a structure. It is
 * calculated by finding the branches of one object, then finding the branches
 * of those branches, etc., until
 * {@link CustomObject#getMaxBranchDepth()} is reached.
 *
 */
public abstract class CustomStructure
{
    // The origin BO3 for this branching structure
    public CustomStructureCoordinate start;

    public EntitiesManager entitiesManager = new EntitiesManager();
    public ParticlesManager particlesManager = new ParticlesManager();
    public ModDataManager modDataManager = new ModDataManager();
    public SpawnerManager spawnerManager = new SpawnerManager();

    protected Map<ChunkCoordinate, Set<CustomStructureCoordinate>> objectsToSpawn;
    protected Random random;

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        if(!(obj instanceof CustomStructure))
        {
            return false;
        }
        CustomStructure other = (CustomStructure) obj;
        return Objects.equals(this.start.bo3Name, other.start.bo3Name)
                && this.start.getX() == other.start.getX()
                && this.start.getY() == other.start.getY()
                && this.start.getZ() == other.start.getZ();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = 31 * result + Objects.hash(this.start.bo3Name);
        result = 31 * result + this.start.getX();
        result = 31 * result + this.start.getY();
        result = 31 * result + this.start.getZ();
        return result;
    }

    public HashSet<ModDataFunction<?>> getModData()
    {
        return modDataManager.modData;
    }

    public HashSet<SpawnerFunction<?>> getSpawnerData()
    {
        return spawnerManager.spawnerData;
    }

    public HashSet<ParticleFunction<?>> getParticleData()
    {
        return particlesManager.particleData;
    }
}
