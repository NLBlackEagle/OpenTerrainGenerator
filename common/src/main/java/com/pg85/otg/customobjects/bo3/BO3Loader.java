package com.pg85.otg.customobjects.bo3;

import java.io.File;

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

public class BO3Loader implements CustomObjectLoader
{
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

    @Override
    public void onShutdown()
    {

    }
}
