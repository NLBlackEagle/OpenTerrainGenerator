package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.WorldLoader;
import com.khorn.terraincontrol.forge.util.WorldHelper;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldListener
{

    private WorldLoader worldLoader;

    public WorldListener(WorldLoader worldLoader)
    {
        this.worldLoader = worldLoader;
    }

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		((ForgeEngine)TerrainControl.getEngine()).getPregenerator().SavePreGeneratorData(event.getWorld());
	}
    
    // TODO: This method should not be called by DimensionManager when switching dimensions (main -> nether -> main). Find out why it is being called
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        World mcWorld = event.getWorld();
        ForgeWorld forgeWorld = this.worldLoader.getWorld(WorldHelper.getName(mcWorld));
        if (forgeWorld == null)
        {
            return;
        }
        if(mcWorld.provider.getDimension() == 0) // Temporary fix, this may break multi-world support (I assume it uses dimensions to load other worlds?) 
        {
        	((ForgeEngine)TerrainControl.getEngine()).getPregenerator().SavePreGeneratorData(mcWorld);
        	this.worldLoader.unloadWorld(forgeWorld);
        }
    }
}