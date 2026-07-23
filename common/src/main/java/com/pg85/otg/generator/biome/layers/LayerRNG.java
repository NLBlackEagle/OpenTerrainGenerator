package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.util.RNG;
import com.pg85.otg.worldsave.WorldSaveData;

public interface LayerRNG
{
    void setLayerSeed(long seed);

    void setWorldSeed(long seed);

    void initChunkSeed(int x, int z);

    @Deprecated
    default void initChunkSeed(long x, long z)
    {
        initChunkSeed((int) x, (int) z);
    }

    @Deprecated
    default void initGroupSeed(long x, long z)
    {
        initChunkSeed(x, z);
    }

    int nextChunkInt(int bound);

    @Deprecated
    default int nextGroupInt(int bound)
    {
        return nextChunkInt(bound);
    }

    @Deprecated
    default int nextGroupIntEntropy(int bound)
    {
        return nextGroupInt(bound);
    }

    static LayerRNG create(WorldSaveData worldSaveData)
    {
        if(worldSaveData.version < 9)
        {
            return new Legacy();
        }
        return new Default();
    }

    class Default implements LayerRNG
    {
        private final RNG rng = new RNG();
        private long scrambledLayerSeed;
        private long scrambledWorldSeed;

        @Override
        public void setLayerSeed(long seed)
        {
            this.scrambledLayerSeed = RNG.murmurHash3(seed);
        }

        @Override
        public void setWorldSeed(long seed)
        {
            this.scrambledWorldSeed = this.scrambledLayerSeed ^ RNG.murmurHash3(seed);
        }

        @Override
        public void initChunkSeed(int x, int z)
        {
            this.rng.setSeed(this.scrambledWorldSeed ^ (x & 0xFFFF_FFFFL | (z & 0xFFFF_FFFFL) << 32));
        }

        @Override
        public int nextChunkInt(int bound)
        {
            return this.rng.nextInt(bound);
        }
    }

    @Deprecated
    class Legacy implements LayerRNG
    {
        private static final long M = 6364136223846793005L;
        private static final long A = 1442695040888963407L;

        private long scrambledLayerSeed;
        private long scrambledWorldSeed;
        private long scrambledChunkSeed;
        private long scrambledGroupSeed;

        @Override
        public void setLayerSeed(long seed)
        {
            this.scrambledLayerSeed = scramble3(seed, seed);
        }

        @Override
        public void setWorldSeed(long seed)
        {
            this.scrambledWorldSeed = scramble3(seed, this.scrambledLayerSeed);
        }

        private static long scramble3(long base, long addend)
        {
            long z = base;
            z = z * (z * M + A) + addend;
            z = z * (z * M + A) + addend;
            z = z * (z * M + A) + addend;
            return z;
        }

        @Override
        public void initChunkSeed(int x, int z)
        {
            this.initChunkSeed((long) x, (long) z);
        }

        @Override
        public void initChunkSeed(long x, long z)
        {
            this.scrambledChunkSeed = scramble22(this.scrambledWorldSeed, x, z);
        }

        @Override
        public void initGroupSeed(long x, long z)
        {
            this.scrambledGroupSeed = scramble22(this.scrambledChunkSeed, x, z);
        }

        private static long scramble22(long base, long addend1, long addend2)
        {
            long z = base;
            z = z * (z * M + A) + addend1;
            z = z * (z * M + A) + addend2;
            z = z * (z * M + A) + addend1;
            z = z * (z * M + A) + addend2;
            return z;
        }

        @Override
        public int nextChunkInt(int bound)
        {
            int i = (int) ((this.scrambledChunkSeed >> 24) % bound);
            if(i < 0)
            {
                i += bound;
            }
            this.scrambledChunkSeed = scramble1(this.scrambledChunkSeed, this.scrambledWorldSeed);
            return i;
        }

        @Override
        public int nextGroupInt(int bound)
        {
            int i = (int) ((this.scrambledGroupSeed >> 24) % bound);
            if(i < 0)
            {
                i += bound;
            }
            this.scrambledGroupSeed = scramble1(this.scrambledGroupSeed, this.scrambledChunkSeed);
            return i;
        }

        private static long scramble1(long base, long addend)
        {
            return base * (base * M + A) + addend;
        }

        @Override
        public int nextGroupIntEntropy(int bound)
        {
            return this.nextGroupInt(bound * 10000) / 10000;
        }
    }
}
