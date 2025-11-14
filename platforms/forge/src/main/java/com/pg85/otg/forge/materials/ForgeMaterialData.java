package com.pg85.otg.forge.materials;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public abstract class ForgeMaterialData extends LocalMaterialData
{
    private static final Set<Block> PROBLEMATIC_BLOCKS = ImmutableSet.of(Blocks.PORTAL, Blocks.DISPENSER, Blocks.ACACIA_STAIRS, Blocks.BIRCH_STAIRS, Blocks.BRICK_STAIRS, Blocks.DARK_OAK_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.NETHER_BRICK_STAIRS, Blocks.OAK_STAIRS, Blocks.PURPUR_STAIRS, Blocks.QUARTZ_STAIRS, Blocks.RED_SANDSTONE_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_STAIRS);
    private static final ForgeMaterialData BLANK = UnknownMaterialData.of(BLANK_NAME);
    public static final StateMaterialData AIR = ofMinecraftBlockState(Blocks.AIR.getDefaultState());

    ForgeMaterialData(int blockId)
    {
        super(blockId);
    }

    public static ForgeMaterialData ofString(String input) throws InvalidConfigException
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
                return ofMinecraftBlockState(block.getDefaultState());
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
        Block block = Block.getBlockFromName(input);
        if(block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(input);
            if(defaultMaterial != null)
            {
                block = Block.getBlockById(defaultMaterial.id);
            }
        }
        return block;
    }

    public static StateMaterialData ofDefaultMaterial(DefaultMaterial defaultMaterial, int blockData)
    {
        return ofMinecraftBlockState(Block.getBlockById(defaultMaterial.id), blockData);
    }

    public static BlockMaterialData ofMinecraftBlock(Block block)
    {
        return BlockMaterialData.of(block);
    }

    @SuppressWarnings("deprecation")
    public static StateMaterialData ofMinecraftBlockState(Block block, int blockData)
    {
        return ofMinecraftBlockState(block.getStateFromMeta(blockData));
    }

    public static StateMaterialData ofMinecraftBlockState(IBlockState state)
    {
        return StateMaterialData.of(state);
    }

    public abstract Block getBlock();

    public abstract IBlockState getBlockState();

    @Override
    public boolean isLiquid()
    {
        return this.getBlockState().getMaterial().isLiquid();
    }

    @Override
    public boolean isSolid()
    {
        return this.getBlockState().getMaterial().isSolid() && this.getBlockState().causesSuffocation();
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