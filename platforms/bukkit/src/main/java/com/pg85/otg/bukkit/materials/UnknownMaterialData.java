package com.pg85.otg.bukkit.materials;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.IBlockData;

class UnknownMaterialData extends BukkitMaterialData
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
    public IBlockData getBlockState()
    {
        return Blocks.AIR.getBlockData();
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
    public LocalMaterialData rotate(int rotateTimes)
    {
        return this;
    }
}
