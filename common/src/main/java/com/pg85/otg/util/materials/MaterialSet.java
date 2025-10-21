package com.pg85.otg.util.materials;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

/**
 * A material set that accepts special values such as "All" or "Solid". These
 * special values make it almost impossible to know which materials are in
 * this set, and as such, this set can't be iterated over and its size remains
 * unknown.
 */
public class MaterialSet
{
    enum Mode
    {
        ALL
        {
            @Override
            boolean contains(LocalMaterialData material)
            {
                return true;
            }

            @Override
            boolean contains(MaterialSet set, LocalMaterialData material)
            {
                return true;
            }
        },
        SOLID
        {
            @Override
            boolean contains(LocalMaterialData material)
            {
                return material.isSolid();
            }

            @Override
            boolean contains(MaterialSet set, LocalMaterialData material)
            {
                return material.isSolid() || DEFAULT.contains(set, material);
            }
        },
        NON_SOLID
        {
            @Override
            boolean contains(LocalMaterialData material)
            {
                return !material.isSolid();
            }

            @Override
            boolean contains(MaterialSet set, LocalMaterialData material)
            {
                return !material.isSolid() || DEFAULT.contains(set, material);
            }
        },
        DEFAULT
        {
            @Override
            boolean contains(LocalMaterialData material)
            {
                return false;
            }

            @Override
            boolean contains(MaterialSet set, LocalMaterialData material)
            {
                return (set.map.getInt(material.withoutBlockData()) & (1 << material.getBlockData())) != 0;
            }
        };

        abstract boolean contains(LocalMaterialData material);

        abstract boolean contains(MaterialSet set, LocalMaterialData material);
    }

    /**
     * Keyword that adds all materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    private static final String ALL_MATERIALS = "All";

    /**
     * Keyword that adds all solid materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    public static final String SOLID_MATERIALS = "Solid";

    /**
     * Keyword that adds all non solid materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    private static final String NON_SOLID_MATERIALS = "NonSolid";

    private Mode mode = Mode.DEFAULT;
    private final Set<LocalMaterialData> materials = new LinkedHashSet<>();
    private final Reference2IntMap<LocalMaterialData> map = new Reference2IntOpenHashMap<>();

    /**
     * Adds the given material to the list.
     *
     * <p>If the material is "All", all
     * materials in existence are added to the list. If the material is
     * "Solid", all solid materials are added to the list. Otherwise,
     * {@link OTG#readMaterial(String)} is used to read the
     * material.
     *
     * <p>If the material {@link StringHelper#specifiesBlockData(String)
     * specifies block data}, it will match only materials with exactly that
     * block data. If the material doesn't specify block data, it will match
     * materials with any block data.
     *
     * @param input The name of the material to add.
     * @throws InvalidConfigException If the name is invalid.
     */
    public void parseAndAdd(String input) throws InvalidConfigException
    {
        if(this.mode == Mode.ALL)
        {
            return;
        }
        if(input.equalsIgnoreCase(ALL_MATERIALS) || this.mode == Mode.SOLID && input.equalsIgnoreCase(SOLID_MATERIALS) || this.mode == Mode.NON_SOLID && input.equalsIgnoreCase(NON_SOLID_MATERIALS))
        {
            this.mode = Mode.ALL;
            this.materials.clear();
            this.map.clear();
            return;
        }
        if(input.equalsIgnoreCase(SOLID_MATERIALS) && this.mode != Mode.SOLID)
        {
            this.mode = Mode.SOLID;
            if(this.materials.removeIf(this.mode::contains))
            {
                this.recomputeMap();
            }
            return;
        }
        if(input.equalsIgnoreCase(NON_SOLID_MATERIALS) && this.mode != Mode.NON_SOLID)
        {
            this.mode = Mode.NON_SOLID;
            if(this.materials.removeIf(this.mode::contains))
            {
                this.recomputeMap();
            }
            return;
        }

        LocalMaterialData material = MaterialHelper.readMaterial(input);
        if(!this.mode.contains(material))
        {
            if(this.materials.add(material))
            {
                this.computeMapEntry(material);
            }
        }
    }

    private void recomputeMap()
    {
        this.map.clear();
        this.materials.forEach(this::computeMapEntry);
    }

    private void computeMapEntry(LocalMaterialData material)
    {
        if(material == null || material.isEmpty())
        {
            return;
        }
        LocalMaterialData k = material.withoutBlockData();
        int v = this.map.getInt(k);
        if((v & material.getBlockDataMask()) != material.getBlockDataMask())
        {
            this.map.put(k, v | material.getBlockDataMask());
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        hash = 31 * hash + this.mode.hashCode();
        hash = 31 * hash + this.materials.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        if(!(obj instanceof MaterialSet))
        {
            return false;
        }
        MaterialSet other = (MaterialSet) obj;
        if(this.mode != other.mode)
        {
            return false;
        }
        if(!this.materials.equals(other.materials))
        {
            return false;
        }
        return true;
    }

    private boolean parsedFallBacks;

    public void parseForWorld(LocalWorld world)
    {
        if(!this.parsedFallBacks)
        {
            this.parsedFallBacks = true;
            for(LocalMaterialData material : this.materials)
            {
                LocalMaterialData replacement = material.parseForWorld(world);
                if(replacement != material)
                {
                    this.materials.remove(material);
                    this.map.remove(material.withoutBlockData());
                    if(this.materials.add(replacement))
                    {
                        this.computeMapEntry(replacement);
                    }
                }
            }
        }
    }

    /**
     * Gets whether the specified material is in this collection. Returns
     * false if the material is null.
     *
     * @param material The material to check.
     * @return True if the material is in this set.
     */
    public boolean contains(LocalMaterialData material)
    {
        if(material == null || material.isEmpty())
        {
            return false;
        }
        return this.mode.contains(this, material);
    }

    /**
     * Returns a comma (",") seperated list of all materials in this set.
     * Keywords are left intact. No brackets ("[" or "]") are used at the
     * begin and end of the string.
     *
     * @return The string.
     */
    @Override
    public String toString()
    {
        Stream<String> prefix;
        switch(this.mode)
        {
        case ALL:
            return SOLID_MATERIALS;
        case SOLID:
            prefix = Stream.of(SOLID_MATERIALS);
            break;
        case NON_SOLID:
            prefix = Stream.of(NON_SOLID_MATERIALS);
            break;
        default:
            prefix = Stream.empty();
            break;
        }
        return Stream.concat(prefix, this.materials.stream().map(Object::toString)).collect(Collectors.joining(","));
    }

    /**
     * Gets a new material set where all blocks are rotated.
     *
     * @return The new material set.
     */
    public MaterialSet rotate()
    {
        MaterialSet rotated = new MaterialSet();
        rotated.mode = this.mode;
        this.materials.stream().map(LocalMaterialData::rotate).forEach(rotated.materials::add);
        rotated.recomputeMap();
        return rotated;
    }
}
