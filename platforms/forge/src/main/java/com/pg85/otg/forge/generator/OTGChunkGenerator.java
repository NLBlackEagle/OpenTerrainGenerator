package com.pg85.otg.forge.generator;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_SIZE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.NBTHelper;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.generator.ChunkProviderOTG;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.biome.OutputType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.LRUCache;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class OTGChunkGenerator implements IChunkGenerator
{
    private boolean testMode = false;
    private ForgeWorld world;
    private ChunkProviderOTG chunkProviderOTG;
    public ObjectSpawner spawner;
    
    // Caches
	private LRUCache<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache;
	private LRUCache<ChunkCoordinate, Chunk> unloadedChunksCache;
    ForgeChunkBuffer chunkBuffer;
    Object chunkBufferLock = new Object();
    //

    private	DataFixer dataFixer = DataFixesManager.createFixer();
    
    public OTGChunkGenerator(ForgeWorld _world)
    {
        this.world = _world;

        this.testMode = this.world.getConfigs().getWorldConfig().modeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.chunkProviderOTG = new ChunkProviderOTG(this.world.getConfigs(), this.world);
        this.spawner = new ObjectSpawner(this.world.getConfigs(), this.world);
        // TODO: Add a setting to the worldconfig for the size of these caches. 
        // Worlds with lots of BO4's and large smoothing areas may want to increase this. 
        this.unloadedBlockColumnsCache = new LRUCache<BlockPos2D, LocalMaterialData[]>(1024);
        this.unloadedChunksCache = new LRUCache<ChunkCoordinate, Chunk>(1024); //Changed 128 chunks cache to 1024 chunks cache for customstructures
    }
    
	// Chunks

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ)
    {
        Chunk chunk = this.generateRawChunk(chunkX, chunkZ);
        fillBiomeArray(chunk);
        chunk.generateSkylightMap();
        return chunk;
    }

    private Chunk generateRawChunk(int chunkX, int chunkZ)
    {
        return this.unloadedChunksCache.computeIfAbsent(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), k -> {
            Chunk v;
            synchronized(chunkBufferLock)
            {
                chunkBuffer = new ForgeChunkBuffer(k);
                this.chunkProviderOTG.generate(chunkBuffer);
                v = chunkBuffer.toChunk(this.world.getWorld());
                chunkBuffer = null;
            }
            return v;
        });
    }

    @Override
    public void populate(int chunkX, int chunkZ)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        this.unloadedChunksCache.remove(chunkCoord);
    	if(this.testMode)
        {
            return;
        }

        BlockSand.fallInstantly = true;
        BlockGravel.fallInstantly = true;

        this.spawner.populate(chunkCoord);

        BlockSand.fallInstantly = false;
        BlockGravel.fallInstantly = false;

        HashMap<String,ArrayList<ModDataFunction<?>>> MessagesPerMod = world.getWorldSession().getModDataForChunk(chunkCoord);
        if(MessagesPerMod != null && MessagesPerMod.entrySet().size() > 0)
        {
        	for(Entry<String, ArrayList<ModDataFunction<?>>> modNameAndData : MessagesPerMod.entrySet())
        	{
        		String messageString = "";
				if(modNameAndData.getKey().equals("OTG"))
				{
	    			for(ModDataFunction<?> modData : modNameAndData.getValue())
	    			{
						String[] paramString2 = modData.modData.split("\\/");

						if(paramString2.length > 1)
						{
							if(paramString2[0].equals("mob"))
							{
								boolean autoSpawn = paramString2.length > 4 ? Boolean.parseBoolean(paramString2[4]) : false;
	    	    				if(autoSpawn)
	    	    				{
	    	    					messageString += "[" + modData.x() + "," + modData.y() + "," + modData.z() + "," + modData.modData + "]";
	    	    				}
							}
						}
	    			}
				} else {
	    			for(ModDataFunction<?> modData : modNameAndData.getValue())
	    			{
    					messageString += "[" + modData.x() + "," + modData.y() + "," + modData.z() + "," + modData.modData + "]";
	    			}
				}
    			if(messageString.length() > 0)
    			{
    				// Send messages to any mods listening
    				FMLInterModComms.sendRuntimeMessage(OTGPlugin.Instance, modNameAndData.getKey(), "ModData", "[" + "[" + world.getName() + "," + chunkX + "," + chunkZ + "]" + messageString + "]");
    			}
        	}
        }
    }
       
    // If allowOutsidePopulatingArea then normal OTG rules are used:
    // returns any chunk that is inside the area being populated.
    // returns null for chunks outside the populated area if populationBoundsCheck=true
    // returns any loaded chunk or null if populationBoundsCheck=false and chunk is outside the populated area

    // If !allowOutsidePopulatinArea then OTG+ rules are used:
    // returns any chunk that is inside the area being populated. TODO: Or any chunk that is cached, which technically should only be chunks that are in the populated area. Cached chunks could also be from the previously populated area, fix that?
    // returns any loaded chunk outside the populated area
    // throws an exception if any unloaded chunk outside the populated area is requested or if a loaded chunk could not be queried.
    
    public Chunk getChunk(int x, int z)
    {
        return this.world.world.getChunk(x >> 4, z >> 4);
    }

    // Blocks
    
    /**
     * Fills the biome array of a chunk with the proper saved ids (no
     * generation ids).
     * @param chunk The chunk to fill the biomes of.
     */
    private void fillBiomeArray(Chunk chunk)
    {
        byte[] chunkBiomeArray = chunk.getBiomeArray();
        ConfigProvider configProvider = this.world.getConfigs();
        int[] biomeShortArray = this.world.getBiomeGenerator().getBiomes(null, chunk.x * CHUNK_SIZE, chunk.z * CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, OutputType.DEFAULT_FOR_WORLD);
        int generationId;
        LocalBiome biome;
        
        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            generationId = biomeShortArray[i];
            biome = configProvider.getBiomeByOTGIdOrNull(generationId);
        	chunkBiomeArray[i] = (byte) biome.getIds().getSavedId();
        }
    }
    
    public LocalMaterialData[] getBlockColumnInUnloadedChunk(int x, int z)
    {
        Chunk chunk = this.world.world.getChunkProvider().getLoadedChunk(x >> 4, z >> 4);
        if(chunk == null)
        {
            chunk = this.generateRawChunk(x >> 4, z >> 4);
        }

        LocalMaterialData[] blockColumn = new LocalMaterialData[256];
        int y = 0;
        for(ExtendedBlockStorage section : chunk.getBlockStorageArray())
        {
            if(section != null)
            {
                for(int i = 0; i < 16; i++)
                {
                    blockColumn[y++] = ForgeMaterialData.ofMinecraftBlockState(section.get(x & 15, i, z & 15));
                }
            }
            else
            {
                for(int i = 0; i < 16; i++)
                {
                    blockColumn[y++] = ForgeMaterialData.AIR;
                }
            }
        }
        return blockColumn;
    }
    
    public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
    {
    	return this.chunkProviderOTG.getBiomeBlocksNoiseValue(blockX, blockZ);
    }
    
    public LocalMaterialData getMaterialInUnloadedChunk(int x, int y, int z)
    {
    	LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(x,z);
        return blockColumn[y];
    }

    public int getHighestBlockYInUnloadedChunk(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
    {
    	int height = -1;

    	LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(x,z);
    	ForgeMaterialData material;
    	boolean isLiquid;
    	boolean isSolid;
    	
        for(int y = 255; y > -1; y--)
        {
        	material = (ForgeMaterialData) blockColumn[y];
        	isLiquid = material.isLiquid();
        	isSolid = material.isSolid() || (!ignoreSnow && material.isMaterial(DefaultMaterial.SNOW));
        	if(!(isLiquid && ignoreLiquid))
        	{
            	if((findSolid && isSolid) || (findLiquid && isLiquid))
        		{
            		return y;
        		}
            	if((findSolid && isLiquid) || (findLiquid && isSolid))
            	{
            		return -1;
            	}
        	}
        }
    	return height;
    }

    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag)
    {
        if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
        {
            return;
        }

        BlockPos pos = new BlockPos(x, y, z);

        this.world.getWorld().setBlockState(pos, ((ForgeMaterialData) material).getBlockState(), BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);

        if(metaDataTag != null)
        {
            TileEntity tileEntity = this.world.getWorld().getTileEntity(pos);
            if(tileEntity != null)
            {
                NBTTagCompound nbtTag = NBTHelper.getNMSFromNBTTagCompound(metaDataTag);
                nbtTag.setInteger("x", x);
                nbtTag.setInteger("y", y);
                nbtTag.setInteger("z", z);
                // Update to current Minecraft format (maybe we want to do this at
                // server startup instead, and then save the result?)
                // TODO: Use datawalker instead
                nbtTag = this.dataFixer.process(FixTypes.BLOCK_ENTITY, nbtTag);
                tileEntity.readFromNBT(nbtTag);
            }
            else
            {
                if(OTG.getPluginConfig().spawnLog)
                {
                    OTG.log(LogMarker.WARN, "Skipping tile entity with id {}, cannot be placed at {},{},{}", Optional.ofNullable(metaDataTag.getTag("id")).map(NamedBinaryTag::getValue).orElse(null), x, y, z);
                }
            }
        }
    }

    // Structures

    @Override
    public void recreateStructures(Chunk chunkIn, int chunkX, int chunkZ)
    {
    	this.world.recreateStructures(chunkIn, chunkX, chunkZ);
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        return false;
    }

	@Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
		// TODO: Is it okay to not use worldIn here?
		return this.world.isInsideStructure(structureName, pos);
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos blockPos, boolean p_180513_4_)
    {
		// TODO: Is it okay to not use worldIn here?
    	return this.world.getNearestStructurePos(structureName, blockPos, p_180513_4_);
    }    

    // Only called during generate by woodlandmansion. Don't call this anywhere else, chunkBuffer is not thread-safe and may be in use.
    public int getHighestBlockInCurrentlyPopulatingChunk(int x, int z)
    {
    	LocalMaterialData material;
    	for(int i = PluginStandardValues.WORLD_HEIGHT - 1; i > PluginStandardValues.WORLD_DEPTH; i--)
    	{
    		material = chunkBuffer.getBlock(x, i, z);
    		if(material != null && !material.isAir())
			{
    			return i;
			};
    	}

    	return 0;
    }    
    
    // Mob spawning
    
    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType paramaca, BlockPos blockPos)
    {
        return this.world.getPossibleCreatures(paramaca, blockPos);
    }
}
