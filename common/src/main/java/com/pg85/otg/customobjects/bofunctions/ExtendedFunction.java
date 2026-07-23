package com.pg85.otg.customobjects.bofunctions;

import java.util.List;

import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;

public abstract class ExtendedFunction<T> extends CustomObjectConfigFunction<T>
{
    private int x;
    private int y;
    private int z;

    @Override
    public void readXYZ(List<String> args, int index) throws InvalidConfigException
    {
        this.assureSize(index + 3, args);
        x = this.readInt(args.get(index + 0), X_MIN, X_MAX);
        y = this.readInt(args.get(index + 1), Y_MIN, Y_MAX);
        z = this.readInt(args.get(index + 2), Z_MIN, Z_MAX);
    }

    @Override
    public void x(int x)
    {
        this.x = x;
    }

    @Override
    public int x()
    {
        return x;
    }

    @Override
    public void y(int y)
    {
        this.y = y;
    }

    @Override
    public int y()
    {
        return y;
    }

    @Override
    public void z(int z)
    {
        this.z = z;
    }

    @Override
    public int z()
    {
        return z;
    }
}
