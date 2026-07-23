package com.pg85.otg.util;

public class BlockPos2D
{
    public final int x;
    public final int z;

    public BlockPos2D(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        hash = hash * 31 + this.x;
        hash = hash * 31 + this.z;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
        {
            return true;
        }
        if(obj instanceof BlockPos2D)
        {
            BlockPos2D other = (BlockPos2D) obj;
            return this.x == other.x && this.z == other.z;
        }
        return false;
    }
}