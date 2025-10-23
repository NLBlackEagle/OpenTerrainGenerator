package com.pg85.otg.forge.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.util.bo3.Rotation;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

class BlockMaterialData extends ForgeMaterialData
{
    private static final Reference2ReferenceMap<Block, BlockMaterialData> INSTANCES = new Reference2ReferenceOpenHashMap<>();
    private final Block block;

    private BlockMaterialData(Block block)
    {
        super(Block.getIdFromBlock(block));
        this.block = block;
    }

    static BlockMaterialData of(Block block)
    {
        return INSTANCES.computeIfAbsent(block, BlockMaterialData::new);
    }

    @Override
    public LocalMaterialData withoutBlockData()
    {
        return this;
    }

    @Override
    public StateMaterialData withDefaultBlockData()
    {
        return ForgeMaterialData.ofMinecraftBlockState(this.block.getDefaultState());
    }

    @SuppressWarnings("deprecation")
    @Override
    public StateMaterialData withBlockData(int newData)
    {
        return ForgeMaterialData.ofMinecraftBlockState(this.block.getStateFromMeta(newData));
    }

    @Override
    public String getName()
    {
        if(this.defaultMaterial != null)
        {
            return this.defaultMaterial.name();
        }
        return Block.REGISTRY.getNameForObject(this.block).toString();
    }

    @Override
    public Block getBlock()
    {
        return this.block;
    }

    @Override
    public IBlockState getBlockState()
    {
        return this.block.getDefaultState();
    }

    @Override
    public byte getBlockData()
    {
        return 0;
    }

    @Override
    public int getBlockDataMask()
    {
        return -1;
    }

    @Override
    public BlockMaterialData rotate(Rotation rotation)
    {
        return this;
    }
}