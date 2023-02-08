package com.pg85.otg.forge.dimensions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.worldsave.DimensionData;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class OTGDimensionManager
{
	private static HashMap<Integer,Integer> orderedDimensions;
	
	public static boolean isDimensionNameRegistered(String dimensionName)
	{
		if(dimensionName.equals("overworld"))
		{
			return true;
		}
		for(int i = -1000; i < Long.SIZE << 4; i++) // -1000 For other mods that add dimensions with id's below zero, hopefully -1000 is enough..
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimensionType = DimensionManager.getProviderType(i);

				if(dimensionType.getName().equals(dimensionName))
				{
    				return true;
				}
			}
		}
		return false;
	}

	public static void registerDimension(int dimId, DimensionType type)
	{		
		DimensionManager.registerDimension(dimId, type);

		// TODO: Don't add the same dim id to  
		// orderedDimensions twice, untangle this.
		// * caused by loadCustomDimensionData calling LoadOrderedDimensionData
		if(!orderedDimensions.containsKey(dimId))
		{
			int maxOrder = -1;
			for(Integer dimOrder : orderedDimensions.values())
			{
				if(dimOrder > maxOrder)
				{
					maxOrder = dimOrder;
				}
			}
			orderedDimensions.put(dimId, maxOrder + 1);
		}
		
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dimensionID", dimId);
        ArrayList<String> types = new ArrayList<String>();
        types.add("OPEN_TERRAIN_GENERATOR");
        writeNBTStrings("types", types, compound);
        FMLInterModComms.sendMessage(PluginStandardValues.MOD_ID, "registerDimension", compound);
	}

    private static void unregisterDimension(int dimensionId)
    {
    	if(dimensionId == 0)
    	{
    		return; // Never unregister the overworld
    	}

    	DimensionManager.unregisterDimension(dimensionId);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dimensionID", dimensionId);

        ArrayList<String> types = new ArrayList<String>();
        types.add("OPEN_TERRAIN_GENERATOR");
        writeNBTStrings("types", types, compound);

        FMLInterModComms.sendMessage(PluginStandardValues.MOD_ID, "unregisterDimension", compound);
    }

    private static void writeNBTStrings(String id, Collection<String> strings, NBTTagCompound compound)
    {
        if (strings != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (String s : strings)
            {
                nbtTagList.appendTag(new NBTTagString(s));
            }

            compound.setTag(id, nbtTagList);
        }
    }
	
	public static boolean IsOTGDimension(int dimensionId)
	{
		if(dimensionId == 0)
		{
			return true;
		}			
		DimensionType dimType = DimensionManager.isDimensionRegistered(dimensionId) ? DimensionManager.getProviderType(dimensionId) : null;
		if(dimType != null && dimType.suffix != null) // Can be null for modded dims like Galacticraft dims.
		{
			return dimType.suffix.equals("OTG"); 
		}
		return false;
	}

	public static ArrayList<Integer> GetOTGDimensions()
	{
		return new ArrayList<Integer>(orderedDimensions.keySet());
	}
	
	public static boolean createDimension(DimensionConfig dimConfig, boolean saveDimensionData)
	{
		return createDimension(dimConfig, -1l, saveDimensionData);
	}
	
	private static int getNextFreeDimId(String presetName)
	{
		HashMap<Integer, String> reservedIds = OTG.getEngine().getModPackConfigManager().getReservedDimIds();
		for(int i = 3; i <= 2048; i++) // Changed to 2048
		{
			String presetReservingId = reservedIds.get(Integer.valueOf(i)); 
			if(presetReservingId == null || presetReservingId.equals(presetName))
			{
				if(!DimensionManager.isDimensionRegistered(i))
				{
					return i;
				}
			}
		}
		return 0;
	}

	private static int checkDimIdReserved(int dimId, String presetName)
	{
		HashMap<Integer, String> reservedIds = OTG.getEngine().getModPackConfigManager().getReservedDimIds();
		String presetReservingId = reservedIds.get(Integer.valueOf(dimId)); 
		if(presetReservingId == null || presetReservingId.equals(presetName))
		{
			if(!DimensionManager.isDimensionRegistered(dimId))
			{
				return dimId;
			}
		}
		return 0;
	}

	public static boolean createDimension(DimensionConfig dimConfig, long seed, boolean saveDimensionData)
	{			
		int newDimId = dimConfig.DimensionId == 0 ? getNextFreeDimId(dimConfig.PresetName) : checkDimIdReserved(dimConfig.DimensionId, dimConfig.PresetName);
		
		if(newDimId == 0)
		{
			return false;
		}

		dimConfig.DimensionId = newDimId;
		
		if(!OTG.getDimensionsConfig().Dimensions.contains(dimConfig))
		{
			OTG.getDimensionsConfig().Dimensions.add(dimConfig);
		} else {
			// Should only happen when loading existing dims on server start.
			String breakpoint = "";
		}

		// TODO: Don't use presetname == dimname, allow presets to be used across dimensions (need to fix biomes first).
		registerDimension(newDimId, DimensionType.register(dimConfig.PresetName, "OTG", newDimId, OTGWorldProvider.class, false));
		
		initDimension(newDimId, seed);

		if(saveDimensionData)
		{
			SaveDimensionData();
		}

		return true;
	}

	// TODO: Getting "world may have leaked log messages from DimensionManager. Make sure world/weakworlds/dimensions are all properly cleared when deleting dims?
	private static void removeFromUsedIds(int dimensionId)
	{
		// Forge 1.12.2-14.23.5.2768 uses BitSet dimensionMap
		BitSet dimensionMap = null;
		try {
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					dimensionMap = (BitSet) field.get(new DimensionManager());
			        break;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if(dimensionMap != null) // null happens when shutting down the MP server
		{
			dimensionMap.clear(dimensionId);
		}
		
		// Forge 1.12.2-14.23.5.2838 uses IntSet usedIds
		IntSet usedIds = null;
		try {
			Field[] fields = DimensionManager.class.getDeclaredFields();
			int i = 0;
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(IntSet.class))
				{
					i++;
					if(i == 3)
					{
						field.setAccessible(true);
						usedIds = (IntSet) field.get(new DimensionManager());
				        break;
					}
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if(usedIds != null) // null happens when shutting down the MP server
		{
			usedIds.rem(dimensionId);
		}
		
		// Forge 1.12.2-14.23.5.2838 uses lastUsedId
		// Set DimensionManager.lastUsedId = 1;
		try {
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
			    if (field.getType() == int.class) {
			    	field.setAccessible(true);
			    	field.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			    	field.set(null, 1);
			    }
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static void DeleteDimension(int dimToRemove, ForgeWorld world, MinecraftServer server, boolean isServerSide)
	{
		if(DimensionManager.getWorld(dimToRemove) != null) // Can be null on the client if the world was unloaded(?)
		{
			DimensionManager.setWorld(dimToRemove, null, server);
		}
		if(DimensionManager.isDimensionRegistered(dimToRemove))
		{
			unregisterDimension(dimToRemove);
		}
	
		world.unRegisterBiomes();

		((ForgeEngine)OTG.getEngine()).getWorldLoader().removeUnloadedWorld(world.getName());

		// Client side only
		((ForgeEngine)OTG.getEngine()).getWorldLoader().removeLoadedWorld(world.getName());

		// For MP the client knows nothing about dimensions
		if(isServerSide)
		{
			OTGDimensionManager.UnloadCustomDimensionData(dimToRemove);
	
			removeFromUsedIds(dimToRemove);	
	
			// This biome was unregistered via a console command, delete its world data
			File dimensionSaveDir = new File(world.getWorld().getSaveHandler().getWorldDirectory() + File.separator + "DIM" + dimToRemove);
			if(dimensionSaveDir.exists() && dimensionSaveDir.isDirectory())
			{
				OTG.log(LogMarker.INFO, "Deleting world save data for dimension " + dimToRemove);
				try {
					FileUtils.deleteDirectory(dimensionSaveDir);
				} catch (IOException e) {
					OTG.log(LogMarker.ERROR, "Could not delete directory: " + e.toString());
					e.printStackTrace();
				}
			}

			SaveDimensionData();
			
			ArrayList<DimensionConfig> dimConfigs = new ArrayList<DimensionConfig>(OTG.getDimensionsConfig().Dimensions);
			for(DimensionConfig dimensionConfig : dimConfigs)
			{
				if(dimensionConfig.PresetName.equals(world.getName()))
				{
					OTG.getDimensionsConfig().Dimensions.remove(dimensionConfig);
					OTG.getDimensionsConfig().save();
					break;
				}
			}
		}
	}
	
	public static boolean DeleteDimensionServer(String worldName, MinecraftServer server)
	{	
		// First make sure world is unloaded            			
		if(((ForgeEngine)OTG.getEngine()).getWorldLoader().isWorldUnloaded(worldName))
		{
			ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getUnloadedWorld(worldName);
			OTGDimensionManager.DeleteDimension(forgeWorld.getDimensionId(), forgeWorld, server, true);
			ServerPacketManager.sendDimensionSynchPacketToAllPlayers(server);
			return true;
		}		
		return false;
	}

	public static void initDimension(int dim)
	{
		initDimension(dim, -1l);
	}
		
    private static void initDimension(int dim, long seed)
    {
    	String dimensionName = DimensionManager.getProviderType(dim).getName();
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null)
        {
            throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");
        }

        try
        {
            DimensionManager.getProviderType(dim);
        }
        catch (Exception e)
        {
            System.err.println("Cannot Hotload Dim: " + e.getMessage());
            return; // If a provider hasn't been registered then we can't hotload the dim
        }
        MinecraftServer mcServer = overworld.getMinecraftServer();
        ISaveHandler savehandler = overworld.getSaveHandler();

        DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(dimensionName); 
        if(seed == -1 && dimConfig != null && dimConfig.Seed != null && dimConfig.Seed.trim().length() > 0)
        {
            seed = (new Random()).nextLong();

            if (!StringUtils.isEmpty(dimConfig.Seed))
            {
                try
                {
                    long j = Long.parseLong(dimConfig.Seed);

                    if (j != 0L)
                    {
                    	seed = j;
                    }
                }
                catch (NumberFormatException var7)
                {
                	seed = (long)dimConfig.Seed.hashCode();
                }
            }        	
        }
        
        WorldConfig worldConfig = WorldConfig.fromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimConfig.PresetName));
        
		long seedIn = seed == -1 ? (long) Math.floor((Math.random() * Long.MAX_VALUE)) : seed;
		GameType gameType = dimConfig.GameType.equals("Creative") ? GameType.CREATIVE : GameType.SURVIVAL;
		boolean enableMapFeatures = worldConfig.strongholdsEnabled;
		boolean hardcoreMode = dimConfig.GameType.equals("Hardcore");

		WorldSettings settings = new WorldSettings(seedIn, gameType, enableMapFeatures, hardcoreMode, OTGPlugin.OtgWorldType);
		settings.setGeneratorOptions(PluginStandardValues.PLUGIN_NAME);
		
		WorldInfo worldInfo = new WorldInfo(settings, dimensionName);
        WorldServer world = (WorldServer)(new OTGWorldServerMulti(mcServer, savehandler, dim, overworld, mcServer.profiler, worldInfo).init());       
        ApplyGameRulesToWorld(world, dimConfig);
        world.addEventListener(new ServerWorldEventHandler(mcServer, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

        // Save seed to dimension config
        ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorldByDimId(dim);
        if(forgeWorld == null)
        {
        	forgeWorld = ((ForgeEngine)OTG.getEngine()).getUnloadedWorldByDimId(dim);
        }
        
        OTG.getDimensionsConfig().getDimensionConfig(forgeWorld.getName()).Seed = "" + seedIn;
        OTG.getDimensionsConfig().save();
        
        // Apply difficulty and game type
        // TODO: Independent difficulty / spawn types per world?
        if (!mcServer.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(mcServer.getGameType());
        }

        if (world.getWorldInfo().isHardcoreModeEnabled())
        {
        	world.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
        	world.setAllowedSpawnTypes(true, true);
        }
        else if (mcServer.isSinglePlayer())
        {
        	world.getWorldInfo().setDifficulty(mcServer.getDifficulty());
            world.setAllowedSpawnTypes(world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
        } else {
        	world.getWorldInfo().setDifficulty(mcServer.getDifficulty());
            world.setAllowedSpawnTypes(mcServer.allowSpawnMonsters(), mcServer.getCanSpawnAnimals());
        }
    }
    
    public static void ApplyGameRulesToWorld(World world, DimensionConfig dimConfig)
    {
        world.getGameRules().setOrCreateGameRule("commandBlockOutput", dimConfig.GameRules.CommandBlockOutput + "");
		world.getGameRules().setOrCreateGameRule("disableElytraMovementCheck", dimConfig.GameRules.DisableElytraMovementCheck + "");
		world.getGameRules().setOrCreateGameRule("doDaylightCycle", dimConfig.GameRules.DoDaylightCycle + "");
		world.getGameRules().setOrCreateGameRule("doEntityDrops", dimConfig.GameRules.DoEntityDrops + "");
		world.getGameRules().setOrCreateGameRule("doFireTick", dimConfig.GameRules.DoFireTick + "");
		world.getGameRules().setOrCreateGameRule("doLimitedCrafting", dimConfig.GameRules.DoLimitedCrafting + "");
		world.getGameRules().setOrCreateGameRule("doMobLoot", dimConfig.GameRules.DoMobLoot + "");
		world.getGameRules().setOrCreateGameRule("doMobSpawning", dimConfig.GameRules.DoMobSpawning + "");
		world.getGameRules().setOrCreateGameRule("doTileDrops", dimConfig.GameRules.DoTileDrops + "");
		world.getGameRules().setOrCreateGameRule("doWeatherCycle", dimConfig.GameRules.DoWeatherCycle + "");
		world.getGameRules().setOrCreateGameRule("gameLoopFunction", dimConfig.GameRules.GameLoopFunction + "");
		world.getGameRules().setOrCreateGameRule("keepInventory", dimConfig.GameRules.KeepInventory + "");
		world.getGameRules().setOrCreateGameRule("logAdminCommands", dimConfig.GameRules.LogAdminCommands + "");
		world.getGameRules().setOrCreateGameRule("maxCommandChainLength", dimConfig.GameRules.MaxCommandChainLength + "");
		world.getGameRules().setOrCreateGameRule("maxEntityCramming", dimConfig.GameRules.MaxEntityCramming + "");
        world.getGameRules().setOrCreateGameRule("mobGriefing", dimConfig.GameRules.MobGriefing + "");
		world.getGameRules().setOrCreateGameRule("naturalRegeneration", dimConfig.GameRules.NaturalRegeneration + "");
		world.getGameRules().setOrCreateGameRule("randomTickSpeed", dimConfig.GameRules.RandomTickSpeed + "");
        world.getGameRules().setOrCreateGameRule("reducedDebugInfo", dimConfig.GameRules.ReducedDebugInfo + "");
		world.getGameRules().setOrCreateGameRule("sendCommandFeedback", dimConfig.GameRules.SendCommandFeedback + "");
		world.getGameRules().setOrCreateGameRule("showDeathMessages", dimConfig.GameRules.ShowDeathMessages + "");
		world.getGameRules().setOrCreateGameRule("spawnRadius", dimConfig.GameRules.SpawnRadius + "");
        world.getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", dimConfig.GameRules.SpectatorsGenerateChunks + "");       
    }

    // Saving / Loading
    // TODO: It's crude but it works, can improve later

	public static void SaveDimensionData()
	{
		StringBuilder stringbuilder = new StringBuilder();
		for(int i = 0; i < Long.SIZE << 4; i++)
		{
			if(i == 1)
			{
				continue; // Ignore dim 1 (End)
			}
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimType = DimensionManager.getProviderType(i);
				if(dimType != null)
				{
					ForgeWorld forgeWorld = (ForgeWorld) OTG.getWorld(dimType.getName());
					if(forgeWorld == null)
					{
						forgeWorld = (ForgeWorld) OTG.getUnloadedWorld(dimType.getName());
					}
					if(forgeWorld == null)
					{
						continue; // If another mod added a dimension
					}
					if (orderedDimensions.get(i) == null){
						continue; // If a plugin (like Multiverse) has added a dimension
					}

					if(forgeWorld != null)
					{
						stringbuilder.append((stringbuilder.length() == 0 ? "" : ",") + i + "," + dimType.getName() + "," + dimType.shouldLoadSpawn() + "," + forgeWorld.getSeed() + "," + orderedDimensions.get(i));
					}
				}
			}
		}
		DimensionData.saveDimensionData(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), stringbuilder);
	}

	public static OTGDimensionInfo LoadOrderedDimensionData()
	{
		// Fetch the dimension data using the appropriate method to avoid duplicate lines
		ArrayList<DimensionData> dimensionData = DimensionData.loadDimensionData(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory());

		// Store the order in which dimensions were added
		// TODO: Should this still even matter? Biome id's are saved once generated, dim id's are used when loading dims?
		orderedDimensions = new HashMap<Integer, Integer>();
		orderedDimensions.put(0,0);
		HashMap<Integer, DimensionData> orderedDimensions1 = new HashMap<Integer, DimensionData>();
		int highestOrder = 0;
		if(dimensionData != null)
		{
			for(DimensionData dimData : dimensionData)
			{
				orderedDimensions.put(dimData.dimensionId, dimData.dimensionOrder);
				orderedDimensions1.put(dimData.dimensionOrder, dimData);
				if(dimData.dimensionOrder > highestOrder)
				{
					highestOrder = dimData.dimensionOrder;
				}
			}
		}

		return new OTGDimensionInfo(highestOrder, orderedDimensions1);
	}
		
	public static void loadCustomDimensionData()
	{
		OTGDimensionInfo otgDimData = LoadOrderedDimensionData();
		DimensionsConfig dimsConfig = OTG.getDimensionsConfig();
		
		// Recreate dimensions in the correct order
		for(int i = 0; i <= otgDimData.highestOrder; i++)
		{
			if(otgDimData.orderedDimensions.containsKey(i))
			{
				DimensionData dimData = otgDimData.orderedDimensions.get(i);

				if(!DimensionManager.isDimensionRegistered(dimData.dimensionId))
				{
					if(dimData.dimensionId != 0)
					{
						boolean bFound = false;
						for(DimensionConfig dimConfig : dimsConfig.Dimensions)
						{
							if(dimConfig.PresetName.equals(dimData.dimensionName))
							{
								bFound = true;
								break;
							}
						}
						if(!bFound)
						{
							// No DimensionConfig exists for this dimension
							// Must be a legacy dimension, create a config for it based on the worldconfig
							WorldConfig worldConfig = WorldConfig.fromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimData.dimensionName));
							if(worldConfig == null)
							{
								throw new RuntimeException("Could not initialise dimension " + dimData.dimensionId + "\", OTG preset \"" + dimData.dimensionName + "\" is not installed.");
							}
							DimensionConfigGui dimConfig = new DimensionConfigGui(dimData.dimensionName, dimData.dimensionId, true, worldConfig);
							dimsConfig.Dimensions.add(new DimensionConfig(dimConfig));
						}
					} else {
						if(dimsConfig.Overworld == null)
						{
							// No DimensionConfig exists for the overworld
							// Must be a legacy world, create a config for it based on the worldconfig
							WorldConfig worldConfig = WorldConfig.fromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimData.dimensionName));
							if(worldConfig == null)
							{
								throw new RuntimeException("Could not initialise dimension " + dimData.dimensionId + "\", OTG preset \"" + dimData.dimensionName + "\" is not installed.");
							}
							DimensionConfigGui dimConfig = new DimensionConfigGui(dimData.dimensionName, 0, true, worldConfig);
							dimsConfig.Overworld = new DimensionConfig(dimConfig);
						}
					}
					
					OTGDimensionManager.registerDimension(dimData.dimensionId, DimensionType.register(dimData.dimensionName, "OTG", dimData.dimensionId, OTGWorldProvider.class, dimData.keepLoaded));
					OTGDimensionManager.initDimension(dimData.dimensionId);
				}
			}
		}
		dimsConfig.save();
	}

	public static void UnloadAllCustomDimensionData()
	{
		HashMap<Integer,Integer> dimensionsOrderCopy = new HashMap<Integer,Integer>();
		if(orderedDimensions != null)
		{
			dimensionsOrderCopy = new HashMap<Integer,Integer>(orderedDimensions);
		}
		orderedDimensions = new HashMap<Integer,Integer>();
		orderedDimensions.put(0,0);

		for(int i : dimensionsOrderCopy.keySet()) // Ignore dim 0 (Overworld) and 1 (End)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				if(i != 0 && dimensionsOrderCopy.containsKey(i))
				{
					unregisterDimension(i);
					removeFromUsedIds(i);				
				}
			}
		}
	}

	public static void UnloadCustomDimensionData(int dimId)
	{
		if(dimId == 0) // Never unregister dim 0 (overworld) from DimensionManager.dimensions
		{
			return;
		}

		boolean isOTGDimension = orderedDimensions.containsKey(dimId);
		orderedDimensions.remove(dimId);

		if(DimensionManager.isDimensionRegistered(dimId))
		{
			if(isOTGDimension)
			{
				unregisterDimension(dimId);
				removeFromUsedIds(dimId);
			}
		}
	}

	public static HashMap<Integer, String> getAllOTGDimensions()
	{
		HashMap<Integer, String> otgDims = new HashMap<Integer, String>();

		for(int i : orderedDimensions.keySet())
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType type = DimensionManager.getProviderType(i);
				otgDims.put(new Integer(type.getId()), type.getName());				
			}
		}	

		return otgDims;
	}

	public static boolean createNewDimensionSP(DimensionConfig dimensionConfig, MinecraftServer server)
	{		
		// Create new world
		long seed = (long) Math.floor((Math.random() * Long.MAX_VALUE));   	        				
		try
		{
			seed = Long.parseLong(dimensionConfig.Seed);
		}
		catch(NumberFormatException ex)
		{
			// TODO
		}
		
		if(!OTGDimensionManager.createDimension(dimensionConfig, seed, true))
		{
			return false;
		}
		ForgeWorld createdWorld = (ForgeWorld) OTG.getWorld(dimensionConfig.PresetName);
		if(createdWorld == null)
		{
			createdWorld = (ForgeWorld) OTG.getUnloadedWorld(dimensionConfig.PresetName);
		}
		if(dimensionConfig.Settings.CanDropChunk)
		{
			DimensionManager.unloadWorld(createdWorld.getWorld().provider.getDimension());
		}

		OTG.getDimensionsConfig().save();
		return true;
	}
}
