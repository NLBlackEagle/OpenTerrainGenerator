package com.pg85.otg.config.standard;

import com.pg85.otg.config.PluginConfig.LogLevels;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;

public class PluginStandardValues extends Settings
{   
	// Files
	
    // Main Plugin Config
    public static final String PluginConfigFilename = "OTG.ini";
    
    // Folders
    
    public static final String PRESETS_FOLDER = "Presets";
    public static final String MODPACK_CONFIGS_FOLDER = "Modpacks";
    public static final String GLOBAL_OBJECTS_FOLDER = "GlobalObjects";
	public static final String DEFAULT_PRESET_NAME = "Default";
    
    // Network
    
    public static final String ChannelName = "OpenTerrainGenerator";
    public static final int ProtocolVersion = 6;
    
    // Plugin Defaults
    
    public static final Setting<LogLevels> LogLevel = enumSetting("LogLevel", LogLevels.Standard);
    public static final String MOD_ID = "OpenTerrainGenerator";
	public static final String MOD_ID_LOWER_CASE = "openterraingenerator";
    public static final String MOD_ID_SHORT = "otg";
    public static final Setting<Boolean> SPAWN_LOG = booleanSetting("SpawnLog", false);
    public static final Setting<Boolean> DEVELOPER_MODE = booleanSetting("DeveloperMode", false);
    public static final Setting<Integer> PREGENERATOR_MAX_CHUNKS_PER_TICK = intSetting("PregeneratorMaxChunksPerTick", 5, 1, 10);

	/**
	 * The world depth that the engine supports. Not the actual depth the
	 * world is capped at. 0 in Minecraft.
	 */
	public static final int WORLD_DEPTH = 0;

	/**
	 * The world height that the engine supports. Not the actual height the
	 * world is capped at. 256 in Minecraft.
	 */
	public static final int WORLD_HEIGHT = 256;
}