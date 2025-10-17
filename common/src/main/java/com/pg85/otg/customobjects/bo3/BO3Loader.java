package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectLoader;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3EntityFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3MinecraftObjectFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3SpawnerFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3WeightedBranchFunction;
import com.pg85.otg.customobjects.bo3.checks.BlockCheck;
import com.pg85.otg.customobjects.bo3.checks.BlockCheckNot;
import com.pg85.otg.customobjects.bo3.checks.LightCheck;
import com.pg85.otg.customobjects.bo3.checks.ModCheck;
import com.pg85.otg.customobjects.bo3.checks.ModCheckNot;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.NamedBinaryTag;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BO3Loader implements CustomObjectLoader
{

	// TOOD: Update this
    /** A list of already loaded meta Tags. The path is the key, a NBT Tag is
     * the value.
     */
    private static final Map<String, NamedBinaryTag> LoadedTags = new HashMap<>();

    public BO3Loader()
    {
        // Register BO3 ConfigFunctions
        CustomObjectResourcesManager registry = OTG.getCustomObjectResourcesManager();
        registry.registerConfigFunction("Block", BO3BlockFunction.class);
        registry.registerConfigFunction("B", BO3BlockFunction.class);
        registry.registerConfigFunction("Branch", BO3BranchFunction.class);
        registry.registerConfigFunction("BR", BO3BranchFunction.class);
        registry.registerConfigFunction("WeightedBranch", BO3WeightedBranchFunction.class);
        registry.registerConfigFunction("WBR", BO3WeightedBranchFunction.class);
        registry.registerConfigFunction("RandomBlock", BO3RandomBlockFunction.class);
        registry.registerConfigFunction("RB", BO3RandomBlockFunction.class);
        registry.registerConfigFunction("MinecraftObject", BO3MinecraftObjectFunction.class);
        registry.registerConfigFunction("MCO", BO3MinecraftObjectFunction.class);
        registry.registerConfigFunction("BlockCheck", BlockCheck.class);
        registry.registerConfigFunction("BC", BlockCheck.class);
        registry.registerConfigFunction("BlockCheckNot", BlockCheckNot.class);
        registry.registerConfigFunction("BCN", BlockCheckNot.class);
        registry.registerConfigFunction("LightCheck", LightCheck.class);
        registry.registerConfigFunction("LC", LightCheck.class);
        registry.registerConfigFunction("Entity", BO3EntityFunction.class);
        registry.registerConfigFunction("E", BO3EntityFunction.class);
        registry.registerConfigFunction("Particle", BO3ParticleFunction.class);
        registry.registerConfigFunction("P", BO3ParticleFunction.class);
        registry.registerConfigFunction("Spawner", BO3SpawnerFunction.class);
        registry.registerConfigFunction("S", BO3SpawnerFunction.class);
        registry.registerConfigFunction("ModData", BO3ModDataFunction.class);
        registry.registerConfigFunction("MD", BO3ModDataFunction.class);
        registry.registerConfigFunction("ModCheck", ModCheck.class);
        registry.registerConfigFunction("MC", ModCheck.class);
        registry.registerConfigFunction("ModCheckNot", ModCheckNot.class);
        registry.registerConfigFunction("MCN", ModCheckNot.class);
    }

    @Override
    public CustomObject loadFromFile(String objectName, File file)
    {
   		return new BO3(objectName, file);
    }

    public static NamedBinaryTag loadMetadata(String name, File bo3Folder)
    {
        return LoadedTags.computeIfAbsent(bo3Folder.getParent() + File.separator + name, BO3Loader::loadTileEntityFromNBT);
    }

    private static NamedBinaryTag loadTileEntityFromNBT(String path)
    {
        // Load from file
        NamedBinaryTag metadata;
        try
        {
            metadata = NamedBinaryTag.readFrom(Paths.get(path));
        }
        catch(NoSuchFileException e)
        {
            // File not found
            if(OTG.getPluginConfig().spawnLog)
            {
                OTG.log(LogMarker.WARN, "NBT file {} not found", (Object) path);
            }
            return null;
        }
        catch(IOException | InvalidConfigException e)
        {
            if(OTG.getPluginConfig().spawnLog)
            {
                OTG.log(LogMarker.ERROR, "Failed to read NBT meta file: ", e.getMessage());
                OTG.printStackTrace(LogMarker.ERROR, e);
            }
            return null;
        }

        if(metadata != null)
        {
	        // The file can be structured in two ways:
	        // 1. chest.nbt with all the contents directly in it
	        // 2. chest.nbt with a Compound tag in it with all the data
	
	        // Check for type 1 by searching for an id tag
	        NamedBinaryTag idTag = metadata.getTag("id");
	        if (idTag != null)
	        {
	            // Found id tag, so return the root tag
	            return metadata;
	        }
	        // No id tag found, so check for type 2
	        if (metadata.getValue() instanceof NamedBinaryTag[])
	        {
	            NamedBinaryTag[] subtag = (NamedBinaryTag[]) metadata.getValue();
	            if (subtag.length != 0)
	            {
	                return subtag[0];
	            }
	        }
        }
        // Unknown/bad structure
        OTG.log(LogMarker.WARN, "Structure of NBT file is incorrect: " + path);
        return null;
    }

    @Override
    public void onShutdown()
    {
        // Clean up the cache
        LoadedTags.clear();
    }

}
