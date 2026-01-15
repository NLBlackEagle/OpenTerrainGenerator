package com.pg85.otg.util.bo3;

import java.util.Random;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

/**
 * An enum to help with CustomObject rotation.
 *
 */
public enum Rotation
{
    NORTH(0),
    WEST(1),
    SOUTH(2),
    EAST(3);

    private static final Rotation[] VALUES = values();
    private final int ROTATION_ID;

    private Rotation(int id)
    {
        this.ROTATION_ID = id;
    }

    /**
     * Returns the id of the rotation. Can be 0, 1, 2 or 3.
     * 
     * @return The id of the rotation.
     */
    public int getRotationId()
    {
        return ROTATION_ID;
    }

    public static Rotation FromString(String rotation)
    {
        try
        {
            return Rotation.valueOf(rotation.toUpperCase());
        }
        catch(IllegalArgumentException e)
        {
            return WEST; // WEST is the default
        }
    }

    /**
     * Get the rotation with the given id. Returns null if the
     * rotation id isn't found.
     * 
     * @param id The rotation id.
     * @return The rotation with the given id, or null if it isn't found.
     */
    public static Rotation getRotation(int id)
    {
        if(id >= 0 && id < VALUES.length)
        {
            return VALUES[id];
        }

        return null;
    }

    /**
     * Returns a random rotation.
     * 
     * @param random The random number generator.
     * @return One of the four directions.
     */
    public static Rotation getRandomRotation(Random random)
    {
        return VALUES[random.nextInt(VALUES.length)];
    }

    /**
     * Returns the next rotation. NORTH -> WEST -> SOUTH -> EAST
     * @return The next rotation.
     */
    public Rotation next(Rotation rotation)
    {
        return VALUES[(ROTATION_ID + rotation.ROTATION_ID) % VALUES.length];
    }

    public static Rotation getRotation(String string) throws InvalidConfigException
    {
        if(StringHelper.isNumber(string))
        {
            int id;
            try
            {
                id = Integer.parseInt(string);
            }
            catch(NumberFormatException e)
            {
                throw new InvalidConfigException("Unknown rotation \"" + string + "\"", e);
            }
            if(id < 0 || id >= VALUES.length)
            {
                throw new InvalidConfigException("Unknown rotation \"" + string + "\"");
            }
            return VALUES[id];
        }
        else
        {
            try
            {
                return valueOf(string.toUpperCase());
            }
            catch(IllegalArgumentException e)
            {
                throw new InvalidConfigException("Unknown rotation \"" + string + "\"", e);
            }
        }
    }
}
