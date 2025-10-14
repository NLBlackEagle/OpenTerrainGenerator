package com.pg85.otg.util.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.pg85.otg.exception.InvalidConfigException;

/**
 * Some methods for string parsing and printing.
 */
public abstract class StringHelper
{
    public static String join(final Collection<?> coll, final String glue)
    {
        return coll.stream().map(Object::toString).collect(Collectors.joining(glue));
    }

    public static String join(final Object[] list, final String glue)
    {
        return Arrays.stream(list).map(Object::toString).collect(Collectors.joining(glue));
    }

    /**
     * Turns the given name into a name suitable for computers, so without
     * strange chars that wouldn't be valid in a Java field.
     * @param name The original name.
     * @return The modified name
     */
    public static String toComputerFriendlyName(String name)
    {
        char[] charArray = name.toCharArray();
        for(int i = 0; i < charArray.length; i++)
        {
            if(!Character.isJavaIdentifierPart(charArray[i]))
            {
                charArray[i] = '_';
            }
            else
            {
                charArray[i] = Character.toLowerCase(charArray[i]);
            }
        }
        return new String(charArray);
    }

    /**
     * Parses the string and returns a number between minValue and maxValue.
     * 
     * @param string
     *            The string to parse.
     * @param minValue
     *            The minimum value, inclusive.
     * @param maxValue
     *            The maximum value, inclusive.
     * @return The number in the String, capped at the minValue and maxValue.
     * @throws InvalidConfigException
     *             If the number is invalid.
     */
    public static int readInt(String string, int minValue, int maxValue) throws InvalidConfigException
    {
        try
        {
            int number = Integer.parseInt(string);
            if(number < minValue)
            {
                return minValue;
            }
            if(number > maxValue)
            {
                return maxValue;
            }
            return number;
        }
        catch(NumberFormatException e)
        {
            throw new InvalidConfigException("Incorrect number: " + string, e);
        }
    }

    /**
     * Parses the string and returns a number between minValue and maxValue.
     * 
     * @param string
     *            The string to parse.
     * @param minValue
     *            The minimum value, inclusive.
     * @param maxValue
     *            The maximum value, inclusive.
     * @return The number in the String, capped at the minValue and maxValue.
     * @throws InvalidConfigException
     *             If the number is invalid.
     */
    public static long readLong(String string, long minValue, long maxValue) throws InvalidConfigException
    {
        try
        {
            long number = Long.parseLong(string);
            if(number < minValue)
            {
                return minValue;
            }
            if(number > maxValue)
            {
                return maxValue;
            }
            return number;
        }
        catch(NumberFormatException e)
        {
            throw new InvalidConfigException("Incorrect number: " + string, e);
        }
    }

    /**
     * Parses the string and returns a number between minValue and maxValue.
     * 
     * @param string
     *            The string to parse.
     * @param minValue
     *            The minimum value, inclusive.
     * @param maxValue
     *            The maximum value, inclusive.
     * @return The number in the String, capped at the minValue and maxValue.
     * @throws InvalidConfigException
     *             If the number is invalid.
     */
    public static double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException
    {
        try
        {
            double number = Double.parseDouble(string);
            if(number < minValue)
            {
                return minValue;
            }
            if(number > maxValue)
            {
                return maxValue;
            }
            return number;
        }
        catch(NumberFormatException e)
        {
            throw new InvalidConfigException("Incorrect number: " + string, e);
        }
    }

    /**
     * Parses a string in the format <code>part1,part2,..</code>, which will
     * return <code>["part1", "part2", ..]</code>. Commas (',') inside braces
     * are ignored, so <code>part1,part2(extravalue,anothervalue),part3</code>
     * gets parsed as
     * <code>["part1", "part2(extravalue,anothervalue)", "part3"]</code>
     * instead of
     * <code>["part1", "part2(extravalue", "anothervalue)", "part3"]</code>.
     *
     * <p>Extra whitespace around each part is removed using
     * {@link String#trim()}. <code>part1, part2</code> will become
     * <code>["part1", "part2"]</code> and not <code>["part1", " part2"]</code>
     *
     * <p>An empty string, or a string consisting of only whitespace, will
     * result in an empty array.
     *
     * @param line
     *            The line to parse.
     * @return The parts of the string.
     */
    public static List<String> readCommaSeperatedString(String line)
    {
        line = line.trim();
        if(line.isEmpty())
        {
            return Collections.emptyList();
        }

        int i = indexOf(line, ',', 0);
        if(i < 0)
        {
            return Collections.singletonList(line);
        }

        List<String> list = new ArrayList<>();
        int j = 0;
        while(i >= 0)
        {
            list.add(line.substring(j, i).trim());
            j = i + 1;
            i = indexOf(line, ',', i + 1);
        }
        list.add(line.substring(i + 1, line.length()).trim());
        return list;
    }

    private static int indexOf(String s, char c, int start)
    {
        for(int i = start; i < s.length(); i++)
        {
            char c1 = s.charAt(i);
            if(c1 == c)
            {
                return i;
            }
            if(c1 == '(')
            {
                i = indexOf(s, ')', i + 1);
                if(i < 0)
                {
                    return -1;
                }
            }
        }
        return -1;
    }
}
