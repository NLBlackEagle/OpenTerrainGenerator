package com.pg85.otg.generator.biome;

import java.lang.ref.WeakReference;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pg85.otg.util.ChunkCoordinate;

public class CachedBiomeGeneratorDebugger
{

    private static class Entry
    {

        private final ChunkCoordinate chunkCoordinate;
        private final boolean resultInCache;
        private final long time;
        private final Thread thread;
        private final Throwable stackTrace;

        public Entry(ChunkCoordinate chunkCoordinate, boolean resultInCache)
        {
            this.chunkCoordinate = chunkCoordinate;
            this.resultInCache = resultInCache;
            this.time = System.nanoTime();
            this.thread = Thread.currentThread();
            this.stackTrace = new Throwable();
        }

    }

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<CachedBiomeGeneratorDebugger> INSTANCES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final String cacheIdentifier;
    private final Queue<Entry> entries = new ConcurrentLinkedQueue<>();

    public CachedBiomeGeneratorDebugger(CachedBiomeGenerator cache)
    {
        this.cacheIdentifier = cache.toString();

        INSTANCES.add(this);
        WeakReference<CachedBiomeGenerator> ref = new WeakReference<>(cache);
        Thread cleaner = new Thread(() ->
        {
            while (ref.get() != null)
            {
                try
                {
                    Thread.sleep(10);
                } catch (InterruptedException e)
                {
                    // ignore
                }
                try
                {
                    update();
                } catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
            INSTANCES.remove(this);
        });
        cleaner.setDaemon(true);
        cleaner.start();
    }

    public void record(ChunkCoordinate chunkCoordinate, boolean resultInCache)
    {
        entries.add(new Entry(chunkCoordinate, resultInCache));
    }

    public void update()
    {
        Entry entry;
        while ((entry = entries.peek()) != null && System.nanoTime() - entry.time > 60_000_000_000L)
        {
            entries.remove();
        }
    }

    public static void printAll()
    {
        INSTANCES.forEach(CachedBiomeGeneratorDebugger::print);
    }

    public void print()
    {
        LOGGER.info("Printing CachedBiomeGenerator accesses... (instance={})", cacheIdentifier);
        LOGGER.info("time | thread | chunk | isCached");

        List<Entry> copy = new ArrayList<>(entries);
        if (!copy.isEmpty())
        {
            long start = copy.get(0).time;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss:SSS", Locale.ENGLISH);
            for (Entry entry : copy)
            {
                LOGGER.info("{} {} {} {}", LocalTime.ofNanoOfDay(entry.time - start).format(formatter), entry.thread.getName(), entry.chunkCoordinate, entry.resultInCache);
                for (StackTraceElement e : entry.stackTrace.getStackTrace())
                {
                    LOGGER.debug("\t\t{}", e);
                }
            }
        }

        LOGGER.info("Finished printing CachedBiomeGeneratorAccesses.");
    }

}
