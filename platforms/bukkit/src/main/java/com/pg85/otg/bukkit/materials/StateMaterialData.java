package com.pg85.otg.bukkit.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.util.bo3.Rotation;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.EnumBlockRotation;
import net.minecraft.server.v1_12_R1.IBlockData;

class StateMaterialData extends BukkitMaterialData
{
    private static final Reference2ReferenceMap<IBlockData, StateMaterialData> INSTANCES = new Reference2ReferenceOpenHashMap<>();
    private BlockMaterialData withoutBlockData;
    private final IBlockData state;
    private final byte blockData;

    private StateMaterialData(IBlockData state)
    {
        super(Block.getId(state.getBlock()));
        this.state = state;
        this.blockData = (byte) state.getBlock().toLegacyData(state);
    }

    static StateMaterialData of(IBlockData state)
    {
        return INSTANCES.computeIfAbsent(state, StateMaterialData::new);
    }

    @Override
    public LocalMaterialData withoutBlockData()
    {
        BlockMaterialData withoutBlockData;
        if((withoutBlockData = this.withoutBlockData) == null)
        {
            this.withoutBlockData = withoutBlockData = BlockMaterialData.of(this.getBlock());
        }
        return withoutBlockData;
    }

    @Override
    public LocalMaterialData withBlockData()
    {
        return this;
    }

    @Override
    public StateMaterialData withDefaultBlockData()
    {
        if(this.blockData == 0)
        {
            return this;
        }
        return BukkitMaterialData.ofMinecraftBlockState(this.getBlock().getBlockData());
    }

    @SuppressWarnings("deprecation")
    @Override
    public StateMaterialData withBlockData(int newData)
    {
        if(this.blockData == newData)
        {
            return this;
        }
        return BukkitMaterialData.ofMinecraftBlockState(this.getBlock().fromLegacyData(newData));
    }

    @Override
    public String getName()
    {
        if(this.defaultMaterial != null)
        {
            return this.defaultMaterial.name() + ":" + this.blockData;
        }
        return Block.REGISTRY.b(this.getBlock()) + ":" + this.blockData;
    }

    @Override
    public Block getBlock()
    {
        return this.state.getBlock();
    }

    @Override
    public IBlockData getBlockState()
    {
        return this.state;
    }

    @Override
    public byte getBlockData()
    {
        return this.blockData;
    }

    @Override
    public int getBlockDataMask()
    {
        return 1 << this.blockData;
    }

    @Override
    public StateMaterialData rotate(Rotation rotation)
    {
        IBlockData newState;
        switch(rotation)
        {
            case NORTH:
                return this;
            case WEST:
                newState = this.state.a(EnumBlockRotation.CLOCKWISE_90);
                break;
            case SOUTH:
                newState = this.state.a(EnumBlockRotation.CLOCKWISE_180);
                break;
            case EAST:
                newState = this.state.a(EnumBlockRotation.COUNTERCLOCKWISE_90);
                break;
            default:
                throw new IllegalArgumentException();
        }
        if(newState == this.state)
        {
            return this;
        }
        return BukkitMaterialData.ofMinecraftBlockState(newState);
    }
}