package com.pg85.otg.bukkit.generator;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.bukkit.util.NBTHelper;
import com.pg85.otg.bukkit.world.BukkitWorld;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.ChunkProviderOTG;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.LRUCache;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.DataConverter;
import net.minecraft.server.v1_12_R1.DataConverterRegistry;
import net.minecraft.server.v1_12_R1.DataConverterTypes;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.TileEntity;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class OTGChunkGenerator extends ChunkGenerator
{
    private DataConverter dataConverter;
    private ChunkProviderOTG chunkProviderOTG;
    // Why does the chunk generator require multiple block populators, each with their own ObjectSpawner instance? For multiple dims?
    private ArrayList<BlockPopulator> BlockPopulator = new ArrayList<BlockPopulator>();
    private boolean NotGenerate = false;
    private OTGPlugin plugin;
    private BukkitWorld world;
    
    // Caches
	private LRUCache<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache;
	private LRUCache<ChunkCoordinate, ChunkData> unloadedChunksCache;
    //
    
    public OTGChunkGenerator(OTGPlugin _plugin, BukkitWorld world)
    {
        this.plugin = _plugin;
        this.world = world;
        this.dataConverter = DataConverterRegistry.a();
        // TODO: Add a setting to the worldconfig for the size of these caches. 
        // Worlds with lots of BO4's and large smoothing areas may want to increase this. 
        this.unloadedBlockColumnsCache = new LRUCache<BlockPos2D, LocalMaterialData[]>(1024);
        this.unloadedChunksCache = new LRUCache<ChunkCoordinate, ChunkData>(1024); //Changed 128 chunks cache to 1024 chunks cache for customstructures
    }
    
    /**
     * Initializes the world if it hasn't already been initialized.
     * 
     * @param world
     *            The world of this generator.
     */
    private void makeSureWorldIsInitialized(World world)
    {
        if (this.chunkProviderOTG == null)
        {
            // Not yet initialized, do it now
            this.plugin.onWorldInit(world);
        }
    }

    /**
     * Called whenever a BukkitWorld instance becomes available.
     * 
     * @param _world
     *            The BukkitWorld instance.
     */
    public void onInitialize(BukkitWorld _world)
    {
        this.chunkProviderOTG = new ChunkProviderOTG(_world.getConfigs(), _world);

        WorldConfig.TerrainMode mode = _world.getConfigs().getWorldConfig().modeTerrain;

        if (mode == WorldConfig.TerrainMode.Normal)// || mode == WorldConfig.TerrainMode.OldGenerator)
        {
            this.BlockPopulator.add(new OTGBlockPopulator(_world));
        }

        if (mode == WorldConfig.TerrainMode.NotGenerate)
        {
            this.NotGenerate = true;
        }
    }

    public ObjectSpawner getObjectSpawner()
    {
    	if (this.chunkProviderOTG == null)
    	{
    		throw new RuntimeException();
    	}
        return ((OTGBlockPopulator)this.BlockPopulator.get(0)).getObjectSpawner();
    }
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        makeSureWorldIsInitialized(world);
        return this.BlockPopulator;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        makeSureWorldIsInitialized(world);

        int y = this.getHighestBlockYInUnloadedChunk(x, z, true, false, false, true);
        return  y >  -1;
        //Material material = world.getHighestBlockAt(x, z).getType();
        //return material.isSolid();
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome)
    {
        makeSureWorldIsInitialized(world);

        if(this.NotGenerate)
        {
            return this.createChunkData(this.world.getWorld().getWorld());
        }

        return this.unloadedChunksCache.computeIfAbsent(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), k -> {
            ChunkData v = this.createChunkData(this.world.getWorld().getWorld());
            this.chunkProviderOTG.generate(new BukkitChunkBuffer(k, v));
            return v;
        });
    }

    public Chunk getChunk(int x, int z)
    {
        return this.world.getWorld().getChunkAt(x >> 4, z >> 4);
    }

    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, BiomeConfig biomeConfig)
    {
        if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
        {
            return;
        }

        BlockPosition pos = new BlockPosition(x, y, z);

        this.world.getWorld().setTypeAndData(pos, ((BukkitMaterialData) material).getBlockState(), 2 | 16);

        if(metaDataTag != null)
        {
            TileEntity tileEntity = this.world.getWorld().getTileEntity(pos);
            if(tileEntity != null)
            {
                NBTTagCompound nbtTag = NBTHelper.getNMSFromNBTTagCompound(metaDataTag);
                nbtTag.setInt("x", x);
                nbtTag.setInt("y", y);
                nbtTag.setInt("z", z);
                // Update to current Minecraft format (maybe we want to do this at
                // server startup instead, and then save the result?)
                nbtTag = this.dataConverter.a(DataConverterTypes.BLOCK_ENTITY, nbtTag, -1);
                tileEntity.load(nbtTag);
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

    @SuppressWarnings("deprecation")
    public LocalMaterialData[] getBlockColumnInUnloadedChunk(int x, int z)
    {
        LocalMaterialData[] blockColumn = new LocalMaterialData[256];

        Chunk chunk = this.world.getWorld().getChunkProvider().getLoadedChunkAt(x >> 4, z >> 4);
        if(chunk != null)
        {
            int y = 0;
            for(ChunkSection section : chunk.getSections())
            {
                if(section != null)
                {
                    for(int i = 0; i < 16; i++)
                    {
                        blockColumn[y++] = BukkitMaterialData.ofMinecraftBlockState(section.getType(x & 15, i, z & 15));
                    }
                }
                else
                {
                    for(int i = 0; i < 16; i++)
                    {
                        blockColumn[y++] = BukkitMaterialData.ofMinecraftBlockState(Blocks.AIR.getBlockData());
                    }
                }
            }
        }
        else
        {
            ChunkData chunkData = this.generateChunkData(this.world.getWorld().getWorld(), null, x >> 4, z >> 4, null);
            for(int y = 0; y < 256; y++)
            {
                MaterialData materialData = chunkData.getTypeAndData(x & 15, y, z & 15);
                blockColumn[y] = BukkitMaterialData.ofMinecraftBlockState(materialData.getItemTypeId(), materialData.getData());
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

        for(int y = 255; y > -1; y--)
        {
        	BukkitMaterialData material = (BukkitMaterialData) blockColumn[y];
        	boolean isLiquid = material.isLiquid();
        	boolean isSolid = material.isSolid() || (!ignoreSnow && material.isMaterial(DefaultMaterial.SNOW));
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
}