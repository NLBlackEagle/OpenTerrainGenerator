package com.pg85.otg.configuration.customobjects;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomObjectConfigFunction<T>
{
    private static final int X_BITS = 11;
    private static final int Y_BITS = 10;
    private static final int Z_BITS = 11;
    private static final int X_SHIFT = 0;
    private static final int Y_SHIFT = X_SHIFT + X_BITS;
    private static final int Z_SHIFT = Y_SHIFT + Y_BITS;
    private static final int X_MASK = ((1 << X_BITS) - 1) << X_SHIFT;
    private static final int Y_MASK = ((1 << Y_BITS) - 1) << Y_SHIFT;
    private static final int Z_MASK = ((1 << Z_BITS) - 1) << Z_SHIFT;
    /**
     * layout: z = 11 bits [-1024,1023], y = 10 bits [-512,511], x = 11 bits [-1024,1023]
     */
    private int coords;
	
    /**
     * Convenience method for creating a config function. Used to create
     * the default config functions.
     *
     * @param <T>
     * @param clazz
     * @param args
     * @return
     */
    public static final <T> CustomObjectConfigFunction<T> create(T holder, Class<? extends CustomObjectConfigFunction<T>> clazz, Object... args)
    {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for (Object arg : args)
        {
            stringArgs.add("" + arg);
        }

        CustomObjectConfigFunction<T> configFunction;
        try
        {
            configFunction = clazz.newInstance();
        } catch (InstantiationException e)
        {
            return null;
        } catch (IllegalAccessException e)
        {
            return null;
        }
        try
        {
            configFunction.load(holder, stringArgs);
        } catch (InvalidConfigException e)
        {
            OTG.log(LogMarker.FATAL, "Invalid default config function! Please report! {}: {}",
                    clazz.getName(), e.getMessage());
            OTG.printStackTrace(LogMarker.FATAL, e);
        }

        return configFunction;
    }

    /**
     * Checks the size of the given list.
     * @param size The minimum size of the list.
     * @param args The list to check.
     * @throws InvalidConfigException If the size of the list is small than
     * the given size.
     */
    protected final void assureSize(int size, List<String> args) throws InvalidConfigException
    {
        if (args.size() < size)
        {
            throw new InvalidConfigException("Too few arguments supplied");
        }
    }

    /**
     * Gets the error that occurred while reading this resource.
     * @return The error.
     * @throws IllegalStateException If the object {@link #isValid() is
     * valid}, so no error occurred.
     */
    public String getError() throws IllegalStateException
    {
        throw new IllegalStateException("Function is valid, so no error");
    }

    /**
     * Gets the class of the holder. The {@link #getHolder()holder of this
     * resource} will be an instance of this type. Multiple invocations of
     * this method on the same instance must always yield the same result.
     *
     * <p>This method is intended to combat Java's type erasure: it provides
     * access to the type parameter T.
     * @return The class.
     */
    public abstract Class<T> getHolderType();

    /**
     * Initializes the function: the holder is set and the arguments are read.
     * @param holder The holder to set. Must be of the type returned by
     *               {@link #getHolderType()}.
     * @param args   Arguments to parse.
     * @throws InvalidConfigException If the arguments are invalid.
     */
    final void init(T holder, List<String> args) throws InvalidConfigException
    {
        load(holder, args);
    }

    /**
     * Returns whether or not the two resources are similar to each other AND
     * not equal. This should return true if two resources are of the same class
     * and if critical element are the same. For example source blocks. This
     * will be used to test if a resource should be overridden via inheritance.
     * @return
     */
    public abstract boolean isAnalogousTo(CustomObjectConfigFunction<T> other);

    /**
     * Returns true if this ConfigFunction has a correct syntax.
     * Returns false if the read method hasn't been called yet,
     * or if the function has an incorrect syntax.
     * <p/>
     * @return Whether this ConfigFunction has a correct syntax.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Parses the arguments. {@link #setHolder(Object)} must be called prior
     * to calling this method, as this method is allowed to use
     * {@link #getHolder()}.
     * @param args The arguments to parse.
     * @throws InvalidConfigException If the syntax is invalid.
     */
    protected abstract void load(T holder, List<String> args) throws InvalidConfigException;

    /**
     * Formats the material list as a string list.
     * @param materials The set of materials to be converted
     * @return A string in the format ",materialName,materialName,etc"
     */
    protected final String makeMaterials(MaterialSet materials)
    {
        return "," + materials.toString();
    }

    /**
     * Gets a String representation, like Tree(10,BigTree,50,Tree,100)
     * @return A String representation, like Tree(10,BigTree,50,Tree,100)
     */
    public abstract String makeString();

    /**
     * Parses the string and returns a number between minValue and
     * maxValue.
     * @param string   The string to parse.
     * @param minValue The minimum value.
     * @param maxValue The maximum value.
     * @return A double between min and max.
     * @throws InvalidConfigException If the number is invalid.
     */
    protected final double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns a number between minValue and
     * maxValue.
     * <p/>
     * @param string
     * @param minValue
     * @param maxValue
     * <p/>
     * @return
     * <p/>
     * @throws InvalidConfigException If the number is invalid.
     */
    protected final int readInt(String string, int minValue, int maxValue) throws InvalidConfigException
    {
        return StringHelper.readInt(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns the boolean or false if no value could be found.
     */
    protected final boolean readBoolean(String string)
    {
    	return Boolean.parseBoolean(string);
    }

    /**
     * Returns the material with the given name.
     * @param string Name of the material, case insensitive.
     * @return The material.
     */
    protected final LocalMaterialData readMaterial(String string) throws InvalidConfigException
    {
        return MaterialHelper.readMaterial(string);
    }

    /**
     * Reads all materials from the start position until the end of the
     * list.
     * @param strings The input strings.
     * @param start   The position to start. The first element in the list
     *                has index 0, the last one size() - 1.
     * @return All block ids.
     * @throws InvalidConfigException If one of the elements in the list is
     *                                not a valid block id.
     */
    protected final MaterialSet readMaterials(List<String> strings, int start) throws InvalidConfigException
    {
        return MaterialSet.create(strings.subList(start, strings.size()));
    }

    public final String write()
    {
        return makeString();
    }

    /**
     * @see #coords
     */
    public void xyz(int xyz)
    {
        coords = xyz;
    }

    /**
     * @see #coords
     */
    public int xyz()
    {
        return coords;
    }

    public void x(int x)
    {
        coords = (coords & ~X_MASK) | ((x << X_SHIFT) & X_MASK);
    }

    public int x()
    {
        return coords << (Integer.SIZE - (X_SHIFT + X_BITS)) >> (Integer.SIZE - X_BITS);
    }

    public void y(int y)
    {
        coords = (coords & ~Y_MASK) | ((y << Y_SHIFT) & Y_MASK);
    }

    public int y()
    {
        return coords << (Integer.SIZE - (Y_SHIFT + Y_BITS)) >> (Integer.SIZE - Y_BITS);
    }

    public void z(int z)
    {
        coords = (coords & ~Z_MASK) | ((z << Z_SHIFT) & Z_MASK);
    }

    public int z()
    {
        return coords << (Integer.SIZE - (Z_SHIFT + Z_BITS)) >> (Integer.SIZE - Z_BITS);
    }
}
