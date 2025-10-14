package com.pg85.otg.forge.materials;

import com.pg85.otg.common.LocalMaterialData;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Rotation;

class StateMaterialData extends ForgeMaterialData
{
    private static final Reference2ReferenceMap<IBlockState, StateMaterialData> INSTANCES = new Reference2ReferenceOpenHashMap<>();
    private final IBlockState state;
    private final byte blockData;

    private StateMaterialData(IBlockState state)
    {
        super(Block.getIdFromBlock(state.getBlock()));
        this.state = state;
        this.blockData = (byte) state.getBlock().getMetaFromState(state);
    }

    static StateMaterialData of(IBlockState state)
    {
        return INSTANCES.computeIfAbsent(state, StateMaterialData::new);
    }

    @Override
    public LocalMaterialData withoutBlockData()
    {
        return ForgeMaterialData.ofMinecraftBlock(this.getBlock());
    }

    @Override
    public StateMaterialData withDefaultBlockData()
    {
        if(this.blockData == 0)
        {
            return this;
        }
        return ForgeMaterialData.ofMinecraftBlockState(this.getBlock().getDefaultState());
    }

    @SuppressWarnings("deprecation")
    @Override
    public StateMaterialData withBlockData(int newData)
    {
        if(this.blockData == newData)
        {
            return this;
        }
        return ForgeMaterialData.ofMinecraftBlockState(this.getBlock().getStateFromMeta(newData));
    }

    @Override
    public String getName()
    {
        if(this.defaultMaterial != null)
        {
            return this.defaultMaterial.name() + ":" + this.blockData;
        }
        return Block.REGISTRY.getNameForObject(this.getBlock()) + ":" + this.blockData;
    }

    @Override
    public Block getBlock()
    {
        return this.state.getBlock();
    }

    @Override
    public IBlockState getBlockState()
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
    public StateMaterialData rotate(int rotateTimes)
    {
        IBlockState newState;
        switch(rotateTimes & 3)
        {
        case 1:
            newState = this.state.withRotation(Rotation.CLOCKWISE_90);
            break;
        case 2:
            newState = this.state.withRotation(Rotation.CLOCKWISE_180);
            break;
        case 3:
            newState = this.state.withRotation(Rotation.COUNTERCLOCKWISE_90);
            break;
        default:
            return this;
        }
        if(newState == this.state)
        {
            return this;
        }
        return ForgeMaterialData.ofMinecraftBlockState(newState);
    }
}