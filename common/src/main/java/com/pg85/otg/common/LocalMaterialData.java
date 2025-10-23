package com.pg85.otg.common;

import java.nio.file.Path;

import com.pg85.otg.OTGEngine;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

/**
 * Represents one of Minecraft's materials. Also includes its data value.
 * Immutable.
 * 
 * @see OTGEngine#readMaterial(String)
 * @see OTGEngine#toLocalMaterialData(DefaultMaterial, int)
 */
public abstract class LocalMaterialData
{
    protected static final String BLANK_NAME = "BLANK";
    protected final DefaultMaterial defaultMaterial;
    protected final int blockId;
    private BlockContainer blockContainer;

    public LocalMaterialData(int blockId)
    {
        this.defaultMaterial = DefaultMaterial.getMaterial(blockId);
        this.blockId = blockId;
    }

    public abstract LocalMaterialData withoutBlockData();

    /**
     * Gets an instance with the same material as this object, but the default
     * block data of the material. This instance is not modified.
     *
     * @return An instance with the default block data.
     */
    public abstract LocalMaterialData withDefaultBlockData();

    /**
     * Gets an instance with the same material as this object, but with the
     * given block data. This instance is not modified.
     *
     * @param newData
     *                The new block data.
     * @return An instance with the given block data.
     */
    public abstract LocalMaterialData withBlockData(int newData);

    public BlockContainer blockContainer()
    {
        BlockContainer blockContainer;
        if((blockContainer = this.blockContainer) == null)
        {
            this.blockContainer = blockContainer = BlockContainer.of(this);
        }
        return blockContainer;
    }

    public BlockContainer blockContainer(Path directory, String relativePath)
    {
        return BlockContainer.of(this, directory, relativePath);
    }

    /**
     * Gets the default material belonging to this material. The block data will
     * be lost. If the material is not one of the vanilla Minecraft materials,
     * {@link DefaultMaterial#UNKNOWN_BLOCK} is returned.
     * 
     * @return The default material.
     */
    public DefaultMaterial toDefaultMaterial()
    {
        return this.defaultMaterial;
    }

    /**
     * Gets whether the block is of the given material. Block data is ignored,
     * as {@link DefaultMaterial} doesn't include block data.
     * 
     * @param material
     *                 The material to check.
     * @return True if this block is of the given material, false otherwise.
     */
    public boolean isMaterial(DefaultMaterial material)
    {
        return this.defaultMaterial == material;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    /**
     * Gets the name of this material. If a {@link #toDefaultMaterial()
     * DefaultMaterial is available,} that name is used, otherwise it's up to
     * the mod that provided this block to name it. Block data is appended to
     * the name, separated with a colon, like "WOOL:2".
     * 
     * @return The name of this material.
     */
    public abstract String getName();

    /**
     * Gets the internal block id. At the moment, all of Minecraft's vanilla
     * materials have a static id, but this can change in the future. Mods
     * already have dynamic ids.
     * 
     * @return The internal block id.
     */
    public int getBlockId()
    {
        return this.blockId;
    }

    /**
     * Gets the internal block data. Block data represents things like growth
     * stage and rotation.
     * 
     * @return The internal block data.
     */
    public abstract byte getBlockData();

    public abstract int getBlockDataMask();

    public boolean matches(LocalMaterialData other)
    {
        return this.blockId == other.getBlockId() && ((1 << other.getBlockData()) & this.getBlockDataMask()) != 0;
    }

    /**
     * Gets whether this material is a liquid, like water or lava.
     * 
     * @return True if this material is a liquid, false otherwise.
     */
    public abstract boolean isLiquid();

    /**
     * Gets whether this material is solid. If there is a
     * {@link #toDefaultMaterial() DefaultMaterial available}, this property is
     * defined by {@link DefaultMaterial#isSolid()}. Otherwise, it's up to the
     * mod that provided this block to say whether it's solid or not.
     * 
     * @return True if this material is solid, false otherwise.
     */
    public abstract boolean isSolid();

    public boolean isEmpty()
    {
        return false;
    }

    /**
     * Gets whether this material is air. This is functionally equivalent to
     * {@code isMaterial(DefaultMaterial.AIR)}, but may yield better
     * performance.
     * 
     * @return True if this material is air, false otherwise.
     */
    public abstract boolean isAir();

    public abstract boolean isLeaves();

    /**
     * Gets whether this material falls down when no other block supports this
     * block, like gravel and sand do.
     * 
     * @return True if this material can fall, false otherwise.
     */
    public abstract boolean canFall();

    /**
     * Gets a new material that is rotated 90 degrees. North -> west -> south ->
     * east. If this material cannot be rotated, the material itself is
     * returned.
     * 
     * @return The rotated material.
     */
    public abstract LocalMaterialData rotate(Rotation rotation);

    /**
     * Gets whether this material can be used as an anchor point for a smooth area
     * 
     * @return True if this material is a solid block, false if it is a tile-entity, half-slab, stairs(?), water, wood or leaves
     */
    public abstract boolean isSmoothAreaAnchor(boolean allowWood, boolean ignoreWater);

    /**
     * Parses this material through the fallback system of the world if required.
     * 
     * @param world The world this material will be parsed through, each world may have different fallbacks.
     * @return The parsed material
     */
    public abstract LocalMaterialData parseForWorld(LocalWorld world);

    public LocalMaterialData parseWithBiomeAndHeight(LocalWorld world, BiomeConfig biomeConfig, int y)
    {
        if(!biomeConfig.worldConfig.biomeConfigsHaveReplacement)
        {
            // Don't waste time here, ReplacedBlocks is empty everywhere
            return this;
        }
        return biomeConfig.replacedBlocks.replaceBlock(y, this);
    }
}
