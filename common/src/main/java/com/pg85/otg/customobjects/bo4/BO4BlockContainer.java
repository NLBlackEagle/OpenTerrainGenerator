package com.pg85.otg.customobjects.bo4;

import com.pg85.otg.common.BlockContainer;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.util.bo3.NamedBinaryTag;

class BO4BlockContainer
{
    private int x;
    private int y;
    private int z;
    BlockContainer blockContainer;

    void x(int x)
    {
        this.x = x;
    }

    int x()
    {
        return x;
    }

    void y(int y)
    {
        this.y = y;
    }

    int y()
    {
        return y;
    }

    void z(int z)
    {
        this.z = z;
    }

    int z()
    {
        return z;
    }

    void blockContainer(BlockContainer blockContainer)
    {
        this.blockContainer = blockContainer;
    }

    BlockContainer blockContainer()
    {
        return blockContainer;
    }

    LocalMaterialData material()
    {
        return blockContainer.material();
    }

    NamedBinaryTag tag()
    {
        return blockContainer.tag();
    }
}
