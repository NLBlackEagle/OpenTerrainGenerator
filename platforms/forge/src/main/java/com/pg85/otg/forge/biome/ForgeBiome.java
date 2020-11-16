package com.pg85.otg.forge.biome;

import java.util.Optional;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeConfig.MineshaftType;
import com.pg85.otg.config.biome.BiomeConfig.RareBuildingType;
import com.pg85.otg.config.biome.BiomeConfig.VillageType;
import com.pg85.otg.config.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.BiomeIds;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeGenerationSettings.Builder;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MoodSoundAmbience;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;

public class ForgeBiome implements LocalBiome
{
	private final Biome biomeBase;
	private final BiomeConfig biomeConfig;
    
    public ForgeBiome(Biome biomeBase, BiomeConfig biomeConfig)
    {
    	this.biomeBase = biomeBase;
    	this.biomeConfig = biomeConfig;
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
        return this.biomeBase.getTemperature(new BlockPos(x, y, z));
    }

	@Override
	public BiomeConfig getBiomeConfig()
	{
		return this.biomeConfig;
	}    
    
	@Override
	public boolean isCustom()
	{
		// TODO
		return false;
	}

	@Override
	public String getName()
	{
		// TODO
		return null;
	}

	@Override
	public BiomeIds getIds()
	{
		// TODO
		return null;
	}

	public static Biome createOTGBiome(BiomeConfig biomeConfig)
	{
		BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder = new BiomeGenerationSettings.Builder();

		// Mob spawning
		MobSpawnInfo.Builder mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);
		
		// NOOP surface builder, surface/ground/stone blocks / sagc are done during base terrain gen.
		biomeGenerationSettingsBuilder.func_242517_a(ConfiguredSurfaceBuilders.field_244184_p);

		// Default structures
		addDefaultStructures(biomeGenerationSettingsBuilder, biomeConfig);
		
		// Carvers
		addCarvers(biomeGenerationSettingsBuilder, biomeConfig);

	    float safeTemperature = biomeConfig.biomeTemperature;
	    if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
	    {
	        // Avoid temperatures between 0.1 and 0.2, Minecraft restriction
	        safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
	    }

	    BiomeAmbience.Builder biomeAmbienceBuilder =
			new BiomeAmbience.Builder()
				.func_235239_a_(biomeConfig.fogColor != 0x000000 ? biomeConfig.fogColor : 12638463)
				.func_235248_c_(biomeConfig.fogColor != 0x000000 ? biomeConfig.fogColor : 329011) // TODO: Add a setting for Water fog color.						
				.func_235246_b_(biomeConfig.waterColor != 0xffffff ? biomeConfig.waterColor : 4159204)
				.func_242539_d(biomeConfig.skyColor != 0x7BA5FF ? biomeConfig.skyColor : getSkyColorForTemp(safeTemperature)) // TODO: Sky color is normally based on temp, make a setting for that?
				// TODO: Implement these
				//particle
				//.func_235244_a_()
				//ambient_sound
				//.func_235241_a_() // Sound event?
				//mood_sound
				.func_235243_a_(MoodSoundAmbience.field_235027_b_) // TODO: Find out what this is, a sound?
				//additions_sound
				//.func_235242_a_()
				//music
				//.func_235240_a_()				
		;

	    if(biomeConfig.foliageColor != 0xffffff)
	    {
			biomeAmbienceBuilder.func_242540_e(biomeConfig.foliageColor);
	    }

	    if(biomeConfig.grassColor != 0xffffff)
	    {
	    	if(!biomeConfig.grassColorIsMultiplier)
	    	{
				biomeAmbienceBuilder.func_242541_f(biomeConfig.grassColor);
	    	} else {
	    		// TODO: grass color multiplier
	    		//int multipliedGrassColor = (defaultGrassColor + biomeConfig.grassColor) / 2;
				//biomeAmbienceBuilder.func_242537_a(biomeConfig.grassColor);
	    	}
	    }

		Biome.Builder biomeBuilder = 
			new Biome.Builder()
			.precipitation(
				biomeConfig.biomeWetness <= 0.0001 ? Biome.RainType.NONE : 
				biomeConfig.biomeTemperature > WorldStandardValues.SNOW_AND_ICE_TEMP ? Biome.RainType.RAIN : 
				Biome.RainType.SNOW
			)
			.category(Biome.Category.PLAINS) // TODO: Find out what category is used for.
			.depth(biomeConfig.biomeHeight)
			.scale(biomeConfig.biomeVolatility)
			.temperature(safeTemperature)
			.downfall(biomeConfig.biomeWetness)
			// Ambience (colours/sounds)
			.func_235097_a_(
				biomeAmbienceBuilder.func_235238_a_()
			// Mob spawning
			).func_242458_a(
				mobSpawnInfoBuilder.func_242577_b() // Validate & build
			// All other biome settings...
			).func_242457_a(
				biomeGenerationSettingsBuilder.func_242508_a() // Validate & build
			)
		;

		return
			biomeBuilder
				// Finalise
				.func_242455_a() // Validate & build
				.setRegistryName(new ResourceLocation(biomeConfig.getRegistryKey().toResourceLocationString()))
		;
	}

	private static MobSpawnInfo.Builder createMobSpawnInfo(BiomeConfig biomeConfig)
	{
		MobSpawnInfo.Builder mobSpawnInfoBuilder = new MobSpawnInfo.Builder();
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.spawnMonstersMerged)
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.func_242575_a(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.spawnCreaturesMerged)
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.func_242575_a(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.spawnWaterCreaturesMerged)
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.func_242575_a(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}		
		for(WeightedMobSpawnGroup mobSpawnGroup : biomeConfig.spawnAmbientCreaturesMerged)
		{
			Optional<EntityType<?>> entityType = EntityType.byKey(mobSpawnGroup.getInternalName());
			if(entityType.isPresent())
			{
				mobSpawnInfoBuilder.func_242575_a(EntityClassification.AMBIENT, new MobSpawnInfo.Spawners(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				OTG.log(LogMarker.WARN, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeConfig.getName());
			}
		}
		
		// TODO: EntityClassification.WATER_AMBIENT / EntityClassification.MISC ?
		
		mobSpawnInfoBuilder.func_242571_a(); // Default biomes do this, not sure if needed. Does the opposite of disablePlayerSpawn?
		return mobSpawnInfoBuilder;
	}
	
	private static void addDefaultStructures(Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig)
	{
		// TODO: Village size, distance.
		if(biomeConfig.worldConfig.villagesEnabled)
		{
			if(biomeConfig.villageType == VillageType.sandstone)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244155_u);			
			}
			else if(biomeConfig.villageType == VillageType.savanna)
	        {
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244156_v);
	        }
			else if(biomeConfig.villageType == VillageType.taiga)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244158_x);			
			}
			else if(biomeConfig.villageType == VillageType.wood)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244154_t);
			}
			else if(biomeConfig.villageType == VillageType.snowy)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244157_w);
			}
		}
		
		// TODO: Stronghold count, distance, spread.
		if(biomeConfig.worldConfig.strongholdsEnabled)
		{
			biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244145_k);
		}
				
		// TODO: Ocean monument gridsize, offset.
		if(biomeConfig.worldConfig.oceanMonumentsEnabled)
		{
			biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244146_l);
		}
		
		// TODO: Min/max distance for rare buildings.
		if(biomeConfig.worldConfig.rareBuildingsEnabled)
		{
			if(biomeConfig.rareBuildingType == RareBuildingType.desertPyramid)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244140_f);
			}		
			if(biomeConfig.rareBuildingType == RareBuildingType.igloo)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244141_g);
			}		
			if(biomeConfig.rareBuildingType == RareBuildingType.jungleTemple)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244139_e);
			}
			if(biomeConfig.rareBuildingType == RareBuildingType.swampHut)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244144_j);
			}
		}
		
		if(biomeConfig.woodLandMansionsEnabled)
		{
			biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244138_d);
		}
		
		if(biomeConfig.netherFortressesEnabled)
		{
			biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244149_o);
		}

		// TODO: Mineshaft rarity.
		if(biomeConfig.worldConfig.mineshaftsEnabled)
		{
			if(biomeConfig.mineshaftType == MineshaftType.normal)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244136_b);
			}
			else if(biomeConfig.mineshaftType == MineshaftType.mesa)
			{
				biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244137_c);
			}
		}
		
		// Buried Treasure
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244152_r); // buried_treasure		
		
		// Ocean Ruins
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244147_m); // ocean_ruin_cold
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244148_n); // ocean_ruin_warm
				
		// Shipwreck
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244142_h); // shipwreck
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244143_i); // shipwreck_beached		
		
		// Pillager Outpost
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244135_a);
		
		// Bastion Remnant
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244153_s);
		
		// Nether Fossil
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244150_p);
		
		// End City
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244151_q);
		
		// Ruined Portal
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244159_y); // ruined_portal
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244160_z); // ruined_portal_desert
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244130_A); // ruined_portal_jungle
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244131_B); // ruined_portal_swamp
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244132_C); // ruined_portal_mountain
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244133_D); // ruined_portal_ocean
		//biomeGenerationSettingsBuilder.func_242516_a(StructureFeatures.field_244134_E); // ruined_portal_nether
		
		// TODO: Fossil
		// TODO: Amethyst Geode (1.17?)
		
		// Misc structures: These structures generate even when the "Generate structures" world option is disabled, and also cannot be located with the /locate command.
		// TODO: Dungeon
		// TODO: Desert Well
	}
	
	private static void addCarvers(Builder biomeGenerationSettingsBuilder, BiomeConfig biomeConfig)
	{
		// TODO: Hook up caves/ravines properly.
		if(biomeConfig.worldConfig.caveFrequency > 0 && biomeConfig.worldConfig.caveRarity > 0)
		{
			// Caves and ravines default config
			DefaultBiomeFeatures.func_243738_d(biomeGenerationSettingsBuilder);
			//DefaultBiomeFeatures.func_243740_e(biomeGenerationSettingsBuilder); // Ocean caves, air and water carver?
		}
	}
	
	private static int getSkyColorForTemp(float p_244206_0_)
	{
		float lvt_1_1_ = p_244206_0_ / 3.0F;
		lvt_1_1_ = MathHelper.clamp(lvt_1_1_, -1.0F, 1.0F);
		return MathHelper.hsvToRGB(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}
}