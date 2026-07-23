package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;
import com.pg85.otg.logging.LogMarker;

/**
 * Layer is the abstract base class for the entire layering system.
 * This system works on the principle that given an array of integers
 * representing a given space on a Minecraft map, that each column of
 * blocks is represented by a single array index/location. At each location
 * a sequence of operations is performed such that the end result is a
 * full description of what should be created upon generation with
 * respect to biome information.
 * <p>
 * Each subclass will need to implement getInts() which is responsible
 * to acting upon the array of ints in it's own unique way. As previously
 * mentioned, each array index is associated with a column of blocks in
 * Minecraft. Each array index is acted upon using a bit masking system.
 * <p>
 * The bit masking system works by marking locations with specific properties
 * or data such as: Land, Island, Ice, River, Biome, and BiomeGroup.
 * For example, the first part of the system starts by assigning MainLayer
 * to LayerEmpty. This layer simply initializes the array of ints with zeros.
 * Next MainLayer is assigned to LayerZoom which has the job of increasing
 * the resolution of the array of ints. Notice how each new layer assigned
 * to MainLayer passes the previous MainLayer into the constructor. Next we
 * see MainLayer assigned to LayerLand. LayerLand randomly marks indices with
 * a Land flag. This flag is then used by the next layers, LayerLandRandom
 * and LayerBiomeGroup, to act upon the map in land specific ways. This process
 * continues for @generationDepth times and is then finished with a set of
 * layers that clean up after the layer system and get it ready for generation
 * <p>
 * It is important to note that getInts should, in most cases, call
 * this.child.getInts() which will follow the trail all the way back to the
 * initial `LayerEmpty` call and produce all modifications to the array as
 * it climbs back up the chain of getInts() calls.
 */
public abstract class Layer
{
    private final LayerRNG rng;

    /**
     * The layer to process before this one. getInts() should call
     * child.getInts() before doing any processing -- in most cases.
     */
    protected Layer child;

    /*
     * LayerIsland - chance to big land
     * LayerLandRandom - a(3) - chance to increase big land
     * GenLayerIcePlains - chance to ice
     * GenLayerMushroomIsland - chance to mushroom island
     *
     * biome:
     * 1) is island
     * 2) size
     * 3) chance
     * 4) is shore
     * 5) color
     * 6) temperature
     * 7) downfall
     * 8) is snow biome
     * 9) Have rivers
     *
     * world
     * 1) chance to lands
     * 2) size of big lands
     * 3) chance to increase lands
     * 4) Chance for ice area
     * 5) Ice area size
     * 6) Rivers
     * 7) Rivers size
     */
    // [ Biome Data ]
    protected static final int BiomeBits = 2047;            //>>	1st-11th Bits           // 255 63
    protected static final int BiomeBitsAreSetBit = (1 << 23);       //	24rd Bit, 4194304

    // [ Flags ]
    protected static final int LandBit = (1 << 11);         //>>	12th Bit, 1024          // 256 64
    protected static final int IslandBit = (1 << 12);       //>>	13th Bit, 2048          // 4096 1024
    protected static final int IceBit = (1 << 13);          //>>	14th Bit, 4096

    // [ Biome Group Data ]
    protected static final int BiomeGroupShift = 14;        //>>	Shift amount for biome group data
    protected static final int BiomeGroupBits = (127 << BiomeGroupShift);   //>>	15th-20th Bits, 1040384

    // [ River Data ]
    private static final int RiverShift = 21;
    protected static final int RiverBits = (3 << RiverShift);               //>>	22st-23nd Bits, 3145728  //3072 768
    protected static final int RiverBitOne = (1 << RiverShift);             //>>	22st Bit, 1048576
    protected static final int RiverBitTwo = (1 << (RiverShift + 1));       //>>	23nd Bit, 2097152

    protected final int defaultOceanId;

    protected Layer(long seed, LocalWorld world)
    {
        this.rng = LayerRNG.create(world.getConfigs().getWorldSaveData());
        this.rng.setLayerSeed(seed);
        this.defaultOceanId = getBiomeId(world, world.getConfigs().getWorldConfig().defaultOceanBiome, "DefaultOceanBiome");
    }

    protected static int getBiomeId(LocalWorld world, String id, String name)
    {
        LocalBiome biome = world.getBiomeByNameOrNull(id);
        if(biome == null)
        {
            biome = world.getFirstBiomeOrNull();
            if(biome == null)
            {
                throw new RuntimeException(String.format("Could not find '%s' \"%s\", aborting.", name, id));
            }
            OTG.log(LogMarker.WARN, "Could not find '%s' \"%s\", substituting \"%s\".", name, id, biome.getName());
        }

        return biome.getIds().getOTGBiomeId();
    }

    public void initWorldGenSeed(long worldSeed)
    {
        if (this.child != null)
            this.child.initWorldGenSeed(worldSeed);

        this.rng.setWorldSeed(worldSeed);
    }

    protected void initChunkSeed(int x, int z)
    {
        this.rng.initChunkSeed(x, z);
    }

    @Deprecated
    protected void initChunkSeed(long x, long z)
    {
        this.rng.initChunkSeed(x, z);
    }

    @Deprecated
    protected void initGroupSeed(long x, long z)
    {
        this.rng.initGroupSeed(x, z);
    }

    protected int nextInt(int x)
    {
        return this.rng.nextChunkInt(x);
    }

    @Deprecated
    protected int nextGroupInt(int x)
    {
        return this.rng.nextGroupInt(x);
    }

    @Deprecated
    protected int nextGroupIntEntropy(int x)
    {
        return this.rng.nextGroupIntEntropy(x);
    }

    public abstract int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize);

    protected int mostCommonOrRandom(int a, int b, int c, int d)
    {
        if(a == b)
        {
            // x x ? ?
            if(a == c || c != d)
            {
                // x x x x
                // x x x y
                // x x y x
                // x x y z
                return a;
            }
            // x x y y
        }
        else if(a == c)
        {
            // x y x ?
            if(b != d)
            {
                // x y x x
                // x y x z
                return a;
            }
            // x y x y
        }
        else if(a == d)
        {
            // x !x !x x
            if(b != c)
            {
                // x y z x
                return a;
            }
            // x y y x
        }
        else
        {
            // x !x !x !x
            if(b == c || b == d)
            {
                // x y y y
                // x y y z
                // x y z y
                return b;
            }
            if(c == d)
            {
                // x y z z
                return c;
            }
            // x y z w
        }

        switch (this.nextInt(4)){
            case 0: return a;
            case 1: return b;
            case 2: return c;
            case 3: default: return d;
        }
    }

    /**
     * In a single step, checks for land and when present returns biome data
     * @param selection The location to be checked
     * @return Biome Data or defaultOceanId when not on land
     */
    protected int getBiomeFromLayer(int selection)
    {
        return (selection & (LandBit | BiomeBitsAreSetBit)) == (LandBit | BiomeBitsAreSetBit) ? (selection & BiomeBits) : this.defaultOceanId;
    }
}
