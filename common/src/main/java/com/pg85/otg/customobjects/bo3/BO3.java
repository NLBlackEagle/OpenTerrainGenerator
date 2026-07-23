package com.pg85.otg.customobjects.bo3;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3EntityFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3SpawnerFunction;
import com.pg85.otg.customobjects.bo3.checks.BO3Check;
import com.pg85.otg.customobjects.structures.Branch;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.BoundingBox;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class BO3 implements StructuredCustomObject
{
    private BO3Config settings;
    private final String name;
    private final File file;
    private boolean isInvalidConfig;

    /**
     * Creates a BO3 from a file.
     *
     * @param name Name of the BO3.
     * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
     */
    public BO3(String name, File file)
    {
        this.name = name;
        this.file = file;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    public BO3Config getSettings()
    {
        return this.settings;
    }

    @Override
    public boolean onEnable()
    {
    	if(this.isInvalidConfig)
    	{
    		return false;
    	}
    	if(this.settings != null)
    	{
    		return true;
    	}
        try
        {
            this.settings = new BO3Config(new FileSettingsReaderOTGPlus(this.name, this.file));
            if (this.settings.settingsMode != ConfigMode.WriteDisable)
            {
                FileSettingsWriterOTGPlus.writeToFile(this.settings, this.settings.settingsMode);
            }
        }
        catch (InvalidConfigException ex)
        {
        	this.isInvalidConfig = true;
            return false;
        }
        return true;
    }

    @Override
    public boolean canSpawnAsTree()
    {
        return this.settings.tree;
    }

    // Used for saplings
    @Override
    public boolean canRotateRandomly()
    {
        return this.settings.rotateRandomly;
    }
    
	@Override
	public boolean loadChecks()
	{
		return this.settings == null ? false : this.settings.parseModChecks();
	}

    // Used to safely spawn this object from a grown sapling
    @Override
    public boolean spawnFromSapling(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        List<BO3BlockFunction> blocksToSpawn = new ArrayList<>();
        ObjectExtrusionHelper extrusionHelper = new ObjectExtrusionHelper(this.settings.extrudeMode, this.settings.extrudeThroughBlocks);

        for(BO3BlockFunction block : this.settings.blocks(rotation))
        {
            LocalMaterialData localMaterial = world.getMaterial(x + block.x(), y + block.y(), z + block.z(), null);

            // Ignore blocks in the ground when checking spawn conditions
            if(block.y() >= 0 && blocksFromSapling(localMaterial))
            {
                // Do not spawn if non-tree blocks are in the way
                return false;
            }

            // Only overwrite air
            if(localMaterial.isAir())
            {
                blocksToSpawn.add(block);
            }

            extrusionHelper.addBlock(block);
        }

        for(BO3BlockFunction block : blocksToSpawn)
        {
            block.spawn(world, random, x + block.x(), y + block.y(), z + block.z(), null, false);
        }

        extrusionHelper.extrude(world, random, x, y, z, null, false);
        handleBO3Functions(null, world, random, rotation, x, y, z, null);

        return true;
    }

    private static MaterialSet replaceableFromSapling;

    private static boolean blocksFromSapling(LocalMaterialData material)
    {
        MaterialSet replaceableFromSapling;
        if((replaceableFromSapling = BO3.replaceableFromSapling) == null)
        {
            BO3.replaceableFromSapling = replaceableFromSapling = MaterialSet.create(DefaultMaterial.AIR, DefaultMaterial.LOG, DefaultMaterial.LOG_2, DefaultMaterial.LEAVES, DefaultMaterial.LEAVES_2, DefaultMaterial.SAPLING);
        }
        return !replaceableFromSapling.contains(material);
    }

    // Force spawns a BO3 object. Used by /otg spawn and bo3AtSpawn.
    // This method ignores the maxPercentageOutsideBlock setting
    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        ObjectExtrusionHelper extrusionHelper = new ObjectExtrusionHelper(this.settings.extrudeMode, this.settings.extrudeThroughBlocks);

        for(BO3BlockFunction block : this.settings.blocks(rotation))
        {
            // Places if BO3 is in placeAnyway mode, or if target block is a source block
            if(this.settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway || this.settings.sourceBlocks.contains(world.getMaterial(x + block.x(), y + block.y(), z + block.z(), null)))
            {
                block.spawn(world, random, x + block.x(), y + block.y(), z + block.z(), null, this.doReplaceBlocks());
                extrusionHelper.addBlock(block);
            }
        }

        extrusionHelper.extrude(world, random, x, y, z, null, this.doReplaceBlocks());
        handleBO3Functions(null, world, random, rotation, x, y, z, null);

        return true;
    }

    // This method is only used to spawn CustomObject.
    // Called during population.
    @Override
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
        boolean atLeastOneObjectHasSpawned = false;

        int chunkMiddleX = chunkCoord.getBlockXCenter();
        int chunkMiddleZ = chunkCoord.getBlockZCenter();
        for (int i = 0; i < this.settings.frequency; i++)
        {
            if (this.settings.rarity > random.nextDouble() * 100.0)
            {
                int x = chunkMiddleX + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
                int z = chunkMiddleZ + random.nextInt(ChunkCoordinate.CHUNK_SIZE);

                if (spawn(world, random, x, z, this.settings.minHeight, this.settings.maxHeight, chunkCoord, this.doReplaceBlocks()))
                {
                    atLeastOneObjectHasSpawned = true;
                }
            }
        }

        return atLeastOneObjectHasSpawned;
    }

    // Used for trees during population
    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated)
    {
    	// A bit ugly, but avoids having to create and implement another spawnAsTree method.
    	if(minY == -1)
    	{
    		minY = this.getSettings().minHeight;
    	}
    	if(maxY == -1)
    	{
    		maxY = this.getSettings().maxHeight;
    	}
        return spawn(world, random, x, z, minY, maxY, chunkBeingPopulated, false);
    }

    // Used for customobject and trees during population
    private boolean spawn(LocalWorld world, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
    {
        Rotation rotation = this.settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
        int offsetY = 0;
        int baseY = 0;
        if (this.settings.spawnHeight == SpawnHeightEnum.randomY)
        {
        	baseY = minY == maxY ? minY : RandomHelper.numberInRange(random, minY, maxY);
        }
        if (this.settings.spawnHeight == SpawnHeightEnum.highestBlock)
        {
        	baseY = world.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);
        }
        if (this.settings.spawnHeight == SpawnHeightEnum.highestSolidBlock)
        {
        	baseY = world.getBlockAboveSolidHeight(x, z, chunkBeingPopulated);
        }
        // Offset by static and random settings values
        // TODO: This is pointless used with randomY?
        offsetY = baseY + this.getOffsetAndVariance(random, this.settings.spawnHeightOffset, this.settings.spawnHeightVariance);
        return trySpawnAt(null, world, random, rotation, x, offsetY, z, minY, maxY, baseY, chunkBeingPopulated, replaceBlocks);
    }

    // Used for trees, customobjects and customstructures during population.
    public boolean trySpawnAt(CustomStructure structure, LocalWorld world, Random random, Rotation rotation, int x, int y, int z, int minY, int maxY, int baseY, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
    {
        if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT) // Isn't this already done before this method is called?
        {
            return false;
        }

        // Height check
        if(y < minY || y > maxY)
        {
            return false;
        }

        // Check for spawning
        for(BO3Check check : this.settings.bo3Checks[rotation.getRotationId()])
        {
            // Don't apply spawn height offset/variance to block checks,
            // they should only be used with highestBlock/highestSolidBlock,
            // and need to check for things like grass at the original spawn y.
            if(check.preventsSpawn(world, x + check.x(), baseY + check.y(), z + check.z(), chunkBeingPopulated))
            {
                // A check failed
                return false;
            }
        }

        boolean spawnAllBlocks = this.settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway && this.settings.maxPercentageOutsideSourceBlock >= 100;
        int maxBlocksOutsideSourceBlock = (int) (this.settings.blockCount() * (this.settings.maxPercentageOutsideSourceBlock / 100.0));

        List<BO3BlockFunction> blocksToSpawn = !spawnAllBlocks || rotation != Rotation.NORTH ? new ArrayList<>() : null;
        LongSet chunks;
        Entry<CustomStructure, LongSet> structureInfo;
        if(structure != null)
        {
            chunks = new LongOpenHashSet();
            structureInfo = new SimpleEntry<>(structure, chunks);
        }
        else
        {
            chunks = null;
            structureInfo = null;
        }
        ObjectExtrusionHelper extrusionHelper = new ObjectExtrusionHelper(this.settings.extrudeMode, this.settings.extrudeThroughBlocks);

        for(BO3BlockFunction block : this.settings.blocks(rotation))
        {
            if(y + block.y() < PluginStandardValues.WORLD_DEPTH || y + block.y() >= PluginStandardValues.WORLD_HEIGHT)
            {
                return false;
            }
            if(chunkBeingPopulated != null && !OTG.IsInAreaBeingPopulated(x + block.x(), z + block.z(), chunkBeingPopulated))
            {
                return false;
            }

            boolean outsideSourceBlock = !spawnAllBlocks && !this.settings.sourceBlocks.contains(world.getMaterial(x + block.x(), y + block.y(), z + block.z(), chunkBeingPopulated));
            // this.settings.maxPercentageOutsideSourceBlock < 100
            // and
            // !this.settings.sourceBlocks.contains(world.getMaterial(x + block.x(), y + block.y(), z + block.z(), chunkBeingPopulated))
            if(outsideSourceBlock && this.settings.maxPercentageOutsideSourceBlock < 100)
            {
                if(--maxBlocksOutsideSourceBlock < 0)
                {
                    return false;
                }
            }
            // this.settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway
            // or
            // this.settings.sourceBlocks.contains(world.getMaterial(x + block.x(), y + block.y(), z + block.z(), chunkBeingPopulated))
            if(!outsideSourceBlock || this.settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway)
            {
                if(blocksToSpawn != null)
                    blocksToSpawn.add(block);
                if(chunks != null)
                    chunks.add(ChunkCoordinate.packed((x + block.x()) >> 4, (z + block.z()) >> 4));
            }
            extrusionHelper.addBlock(block);
        }

        // Call event
        if(!OTG.fireCanCustomObjectSpawnEvent(this, world, x, y, z))
        {
            // Cancelled
            return false;
        }

        // Spawn

        for(BO3BlockFunction block : blocksToSpawn == null ? Arrays.asList(this.settings.getBlocks()) : blocksToSpawn)
        {
            block.spawn(world, random, x + block.x(), y + block.y(), z + block.z(), chunkBeingPopulated, replaceBlocks);
        }

        extrusionHelper.extrude(world, random, x, y, z, chunkBeingPopulated, replaceBlocks);
        handleBO3Functions(structureInfo, world, random, rotation, x, y, z, chunkBeingPopulated);

        return true;
    }

    private void handleBO3Functions(Entry<CustomStructure, LongSet> structureInfo, LocalWorld world, Random random, Rotation rotation, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        CustomStructure structure;
        LongSet chunks;
        boolean placeholder;
        if(placeholder = (structureInfo == null))
        {
            structure = new BO3CustomStructure(new BO3CustomStructureCoordinate(world, this, this.getName(), Rotation.NORTH, x, (short) 0, z));
            chunks = new LongOpenHashSet();
        }
        else
        {
            structure = structureInfo.getKey();
            chunks = structureInfo.getValue();
        }

        for(BO3ModDataFunction modData : this.settings.modDataFunctions[rotation.getRotationId()])
        {
            BO3ModDataFunction newModData = new BO3ModDataFunction();

            newModData.y(y + modData.y());
            newModData.x(x + modData.x());
            newModData.z(z + modData.z());

            newModData.modData = modData.modData;
            newModData.modId = modData.modId;

            structure.modDataManager.modData.add(newModData);
            chunks.add(ChunkCoordinate.packed(newModData.x() >> 4, newModData.z() >> 4));
        }

        for(BO3SpawnerFunction spawnerData : settings.spawnerFunctions[rotation.getRotationId()])
        {
            BO3SpawnerFunction newSpawnerData = new BO3SpawnerFunction();

            newSpawnerData.y(y + spawnerData.y());
            newSpawnerData.x(x + spawnerData.x());
            newSpawnerData.z(z + spawnerData.z());

            newSpawnerData.mobName = spawnerData.mobName;
            newSpawnerData.originalnbtFileName = spawnerData.originalnbtFileName;
            newSpawnerData.nbtFileName = spawnerData.nbtFileName;
            newSpawnerData.groupSize = spawnerData.groupSize;
            newSpawnerData.interval = spawnerData.interval;
            newSpawnerData.spawnChance = spawnerData.spawnChance;
            newSpawnerData.maxCount = spawnerData.maxCount;

            newSpawnerData.despawnTime = spawnerData.despawnTime;

            newSpawnerData.velocityX = spawnerData.velocityX;
            newSpawnerData.velocityY = spawnerData.velocityY;
            newSpawnerData.velocityZ = spawnerData.velocityZ;

            newSpawnerData.velocityXSet = spawnerData.velocityXSet;
            newSpawnerData.velocityYSet = spawnerData.velocityYSet;
            newSpawnerData.velocityZSet = spawnerData.velocityZSet;

            newSpawnerData.yaw = spawnerData.yaw;
            newSpawnerData.pitch = spawnerData.pitch;

            structure.spawnerManager.spawnerData.add(newSpawnerData);
            chunks.add(ChunkCoordinate.packed(newSpawnerData.x() >> 4, newSpawnerData.z() >> 4));
        }

        for(BO3ParticleFunction particleData : this.settings.particleFunctions[rotation.getRotationId()])
        {
            BO3ParticleFunction newParticleData = new BO3ParticleFunction();

            newParticleData.y(y + particleData.y());
            newParticleData.x(x + particleData.x());
            newParticleData.z(z + particleData.z());

            newParticleData.particleName = particleData.particleName;

            newParticleData.interval = particleData.interval;

            newParticleData.velocityX = particleData.velocityX;
            newParticleData.velocityY = particleData.velocityY;
            newParticleData.velocityZ = particleData.velocityZ;

            newParticleData.velocityXSet = particleData.velocityXSet;
            newParticleData.velocityYSet = particleData.velocityYSet;
            newParticleData.velocityZSet = particleData.velocityZSet;

            structure.particlesManager.particleData.add(newParticleData);
            chunks.add(ChunkCoordinate.packed(newParticleData.x() >> 4, newParticleData.z() >> 4));
        }

        for(LongIterator iterator = chunks.iterator(); iterator.hasNext();)
        {
            long packed = iterator.nextLong();
            world.getStructureCache().addBo3ToStructureCache(ChunkCoordinate.fromPacked(packed), structure, !placeholder);
        }

        for(BO3EntityFunction entity : this.settings.entityFunctions[rotation.getRotationId()])
        {
            BO3EntityFunction newEntityData = new BO3EntityFunction();

            newEntityData.y(y + entity.y());
            newEntityData.x(x + entity.x());
            newEntityData.z(z + entity.z());

            newEntityData.name = entity.name;
            newEntityData.resourceLocation = entity.resourceLocation;
            newEntityData.groupSize = entity.groupSize;
            newEntityData.nameTagOrNBTFileName = entity.nameTagOrNBTFileName;
            newEntityData.originalNameTagOrNBTFileName = entity.originalNameTagOrNBTFileName;
            newEntityData.namedBinaryTag = entity.namedBinaryTag;
            newEntityData.rotation = entity.rotation;

            world.spawnEntity(newEntityData, chunkBeingPopulated);
        }
    }

    public Branch[] getBranches(Rotation rotation)
    {
        return this.settings.branches[rotation.getRotationId()];
    }
    
    public StructurePartSpawnHeight getStructurePartSpawnHeight()
    {
        return this.settings.spawnHeight.toStructurePartSpawnHeight();
    }

    // TODO: Use BoundingBox for BO4's?
    public BoundingBox getBoundingBox(Rotation rotation)
    {
        return this.settings.boundingBoxes[rotation.getRotationId()];
    }

    public int getMaxBranchDepth() // This used to be in CustomObject?
    {
        return this.settings.maxBranchDepth;
    }

    /**
     * Computes the offset and variance for spawning a bo3
     *
     * @param random   Random number generator.
     * @param offset   Base spawn offset.
     * @param variance Max variance from this offset.
     *
     * @return The sum of the offset and variance.
     */
    private int getOffsetAndVariance(Random random, int offset, int variance)
    {
        if (variance == 0)
        {
            return offset;
        } else if (variance < 0)
        {
            variance = -random.nextInt(MathHelper.abs(variance) + 1);
        } else {
            variance = random.nextInt(variance + 1);
        }
        return MathHelper.clamp(offset + variance, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT - 1);
    }
    
    public CustomStructureCoordinate makeCustomStructureCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        Rotation rotation = this.settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
        int height = RandomHelper.numberInRange(random, this.settings.minHeight, this.settings.maxHeight);
        return new BO3CustomStructureCoordinate(world, this, this.getName(), rotation, chunkX * 16 + 8 + random.nextInt(16), (short)height, chunkZ * 16 + 7 + random.nextInt(16));
    }

	@Override
	public boolean doReplaceBlocks()
	{
		return this.settings.doReplaceBlocks;
	}
}
