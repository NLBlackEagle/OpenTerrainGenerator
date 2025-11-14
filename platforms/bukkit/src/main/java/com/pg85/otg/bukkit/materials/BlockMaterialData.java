package com.pg85.otg.bukkit.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.util.bo3.Rotation;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.IBlockData;

class BlockMaterialData extends BukkitMaterialData
{
    private static final Reference2ReferenceMap<Block, BlockMaterialData> INSTANCES = new Reference2ReferenceOpenHashMap<>();
    private StateMaterialData withDefaultBlockData;
    private final Block block;

    private BlockMaterialData(Block block)
    {
        super(Block.getId(block));
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
    public LocalMaterialData withBlockData()
    {
        return withDefaultBlockData();
    }

    @Override
    public StateMaterialData withDefaultBlockData()
    {
        StateMaterialData withDefaultBlockData;
        if((withDefaultBlockData = this.withDefaultBlockData) == null)
        {
            this.withDefaultBlockData = withDefaultBlockData = StateMaterialData.of(this.getBlockState());
        }
        return withDefaultBlockData;
    }

    @SuppressWarnings("deprecation")
    @Override
    public StateMaterialData withBlockData(int newData)
    {
        return BukkitMaterialData.ofMinecraftBlockState(this.block.fromLegacyData(newData));
    }

    @Override
    public String getName()
    {
        if(this.defaultMaterial != null)
        {
            return this.defaultMaterial.name();
        }
        return Block.REGISTRY.b(this.block).toString();
    }

    @Override
    public Block getBlock()
    {
        return this.block;
    }

    @Override
    public IBlockData getBlockState()
    {
        return this.block.getBlockData();
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