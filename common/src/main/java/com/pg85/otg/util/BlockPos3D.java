package com.pg85.otg.util;

public class BlockPos3D
{
    public final int x;
    public final int y;
    public final int z;

    public BlockPos3D(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
        {
            return true;
        }
        if(obj instanceof BlockPos3D)
        {
            BlockPos3D other = (BlockPos3D) obj;
            return this.x == other.x && this.z == other.z;
        }
        return false;
    }
}