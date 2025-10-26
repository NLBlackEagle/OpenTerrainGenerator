package com.pg85.otg.forge.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

class UnknownMaterialData extends ForgeMaterialData
{
    private static final Object2ReferenceMap<String, UnknownMaterialData> INSTANCES = new Object2ReferenceOpenHashMap<>();
    private final String raw;
    private LocalMaterialData replacement;

    private UnknownMaterialData(String raw)
    {
        super(DefaultMaterial.AIR.id);
        this.raw = raw;
    }

    static UnknownMaterialData of(String raw)
    {
        return INSTANCES.computeIfAbsent(raw, UnknownMaterialData::new);
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public LocalMaterialData parseForWorld(LocalWorld world)
    {
        LocalMaterialData result;
        if((result = this.replacement) == null)
        {
            result = world.getConfigs().getWorldConfig().parseFallback(this.raw);
            if(result == null)
            {
                result = this;
            }
            this.replacement = result;
        }
        return result;
    }

    @Override
    public LocalMaterialData withoutBlockData()
    {
        return this;
    }

    @Override
    public LocalMaterialData withBlockData()
    {
        return this;
    }

    @Override
    public LocalMaterialData withDefaultBlockData()
    {
        return this;
    }

    @Override
    public LocalMaterialData withBlockData(int newData)
    {
        return this;
    }

    @Override
    public String getName()
    {
        return this.raw;
    }

    @Override
    public Block getBlock()
    {
        return Blocks.AIR;
    }

    @Override
    public IBlockState getBlockState()
    {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public byte getBlockData()
    {
        return 0;
    }

    @Override
    public int getBlockDataMask()
    {
        return 0;
    }

    @Override
    public LocalMaterialData rotate(Rotation rotation)
    {
        return this;
    }
}
