package com.pg85.otg.bukkit.materials;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockFalling;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.Material;

public abstract class BukkitMaterialData extends LocalMaterialData
{
    private static final Set<Block> PROBLEMATIC_BLOCKS = ImmutableSet.of(Blocks.PORTAL, Blocks.DISPENSER, Blocks.ACACIA_STAIRS, Blocks.BIRCH_STAIRS, Blocks.BRICK_STAIRS, Blocks.DARK_OAK_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.NETHER_BRICK_STAIRS, Blocks.OAK_STAIRS, Blocks.PURPUR_STAIRS, Blocks.QUARTZ_STAIRS, Blocks.RED_SANDSTONE_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_STAIRS);
    private static final BukkitMaterialData BLANK = UnknownMaterialData.of(BLANK_NAME);
    public static final StateMaterialData AIR = ofMinecraftBlockState(Blocks.AIR.getBlockData());

    BukkitMaterialData(int blockId)
    {
        super(blockId);
    }

    public static BukkitMaterialData ofString(String input) throws InvalidConfigException
    {
        if(input.equalsIgnoreCase(BLANK_NAME))
        {
            return BLANK;
        }

        Block block = parseBlock(input);
        int meta = -1;
        if(block == null)
        {
            int i = input.lastIndexOf(':');
            if(i < 0)
            {
                i = input.lastIndexOf('.');
            }
            if(i >= 0)
            {
                try
                {
                    meta = Integer.parseInt(input.substring(i + 1));
                    block = parseBlock(input.substring(0, i));
                }
                catch(NumberFormatException e)
                {
                    // ignore
                }
            }
        }

        if(block != null)
        {
            if(meta < 0 && PROBLEMATIC_BLOCKS.contains(block))
            {
                return ofMinecraftBlockState(block.getBlockData());
            }

            if(meta < 0)
            {
                return ofMinecraftBlock(block);
            }
            else
            {
                try
                {
                    return ofMinecraftBlockState(block, meta);
                }
                catch(ArrayIndexOutOfBoundsException e)
                {
                    throw new InvalidConfigException("Illegal meta data for the block type, cannot use " + input);
                }
                catch(IllegalArgumentException e)
                {
                    throw new InvalidConfigException("Illegal block data for the block type, cannot use " + input);
                }
            }
        }
        return UnknownMaterialData.of(input);
    }

    private static Block parseBlock(String input)
    {
        Block block = Block.getByName(input);
        if(block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(input);
            if(defaultMaterial != null)
            {
                block = Block.getById(defaultMaterial.id);
            }
        }
        return block;
    }

    public static StateMaterialData ofDefaultMaterial(DefaultMaterial defaultMaterial, int blockData)
    {
        return ofMinecraftBlockState(defaultMaterial.id, blockData);
    }

    public static BlockMaterialData ofMinecraftBlock(Block block)
    {
        return BlockMaterialData.of(block);
    }

    public static StateMaterialData ofMinecraftBlockState(int blockId, int blockData)
    {
        return ofMinecraftBlockState(Block.getById(blockId), blockData);
    }

    @SuppressWarnings("deprecation")
    public static StateMaterialData ofMinecraftBlockState(Block block, int blockData)
    {
        return ofMinecraftBlockState(block.fromLegacyData(blockData));
    }

    public static StateMaterialData ofMinecraftBlockState(IBlockData state)
    {
        return StateMaterialData.of(state);
    }

    @SuppressWarnings("deprecation")
    public static StateMaterialData ofBukkitBlock(org.bukkit.block.Block block)
    {
        return ofMinecraftBlockState(block.getType().getId(), block.getData());
    }

    public abstract Block getBlock();

    public abstract IBlockData getBlockState();

    @Override
    public boolean isLiquid()
    {
        return this.getBlockState().getMaterial().isLiquid();
    }

    @Override
    public boolean isSolid()
    {
        return this.getBlockState().getMaterial().isSolid() && this.getBlockState().r();
    }

    @Override
    public boolean isAir()
    {
        return this.getBlock() == Blocks.AIR;
    }

    @Override
    public boolean canFall()
    {
        return this.getBlock() instanceof BlockFalling;
    }

    @Override
    public boolean isLeaves()
    {
        return this.getBlockState().getMaterial() == Material.LEAVES;
    }

    @Override
    public boolean isSmoothAreaAnchor(boolean allowWood, boolean ignoreWater)
    {
        if(this.isSolid())
        {
            return allowWood || this.getBlockState().getMaterial() != Material.WOOD;
        }
        return !ignoreWater && this.getBlockState().getMaterial().isLiquid();
    }

    @Override
    public LocalMaterialData parseForWorld(LocalWorld world)
    {
        return this;
    }
}