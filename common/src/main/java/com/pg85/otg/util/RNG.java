package com.pg85.otg.util;

public class RNG
{
    private long state;

    public RNG()
    {
        this(murmurHash3(System.nanoTime()) ^ murmurHash3(System.currentTimeMillis()));
    }

    public RNG(long seed)
    {
        setSeed(seed);
    }

    public void setSeed(long seed)
    {
        state = murmurHash3(seed);
    }

    public void setState(long state)
    {
        this.state = state;
    }

    public long getState()
    {
        return state;
    }

    /**
     * Based on <a href="https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp">MurmurHash3</a>
     */
    public static long murmurHash3(long x)
    {
        x = (x ^ (x >>> 33)) * 0xff51afd7ed558ccdL;
        x = (x ^ (x >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return x ^ (x >>> 33);
    }

    /**
     * Based on <a href="http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html">Stafford's Mix4</a>
     */
    public int nextInt()
    {
        long z = (state += 0x9e3779b97f4a7c15L);
        z = (z ^ (z >>> 33)) * 0x62a9d9ed799705f5L;
        z = (z ^ (z >>> 28)) * 0xcb24d0a5c88c35b3L;
        return (int) (z >>> 32);
    }

    /**
     * Based on <a href="http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html">Stafford's Mix13</a>
     */
    public long nextLong()
    {
        long z = (state += 0x9e3779b97f4a7c15L);
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    public boolean nextBoolean()
    {
        return nextInt() < 0;
    }

    public float nextFloat()
    {
        return (nextInt() >>> 8) * 0x1.0p-24f;
    }

    public double nextDouble()
    {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    public int nextInt(int bound)
    {
        if(bound <= 0)
            throw new IllegalArgumentException("bound must be positive");
        int m = bound - 1;
        if((bound & m) == 0)
            return nextInt() & m;
        int r;
        while((r = nextInt() >>> 1) - (r %= bound) + m < 0)
            ;
        return r;
    }

    public long nextLong(long bound)
    {
        if(bound <= 0)
            throw new IllegalArgumentException("bound must be positive");
        long m = bound - 1;
        if((bound & m) == 0)
            return nextLong() & m;
        long r;
        while((r = nextLong() >>> 1) - (r %= bound) + m < 0)
            ;
        return r;
    }
}
