package com.pg85.otg.generator.biome.layers;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.configuration.world.WorldConfig.ImageMode;
import com.pg85.otg.generator.biome.ArraysCache;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.MathHelper;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

public class LayerFromImage extends Layer
{
    private final Image image;
    private final ImageMode imageMode;
    private final int xOffset;
    private final int zOffset;
    private final int fillBiome;

    LayerFromImage(long seed, LocalWorld world, Layer childLayer)
    {
        super(seed, world);
        this.child = childLayer;
        WorldConfig config = world.getConfigs().getWorldConfig();
        try
        {
            this.image = new Image(config);
        }
        catch(IOException e)
        {
            throw new UncheckedIOException("Failed initializing image layer", e);
        }
        this.xOffset = config.imageXOffset;
        this.zOffset = config.imageZOffset;
        if(config.imageMode == ImageMode.ContinueNormal && child == null)
        {
            OTG.log(LogMarker.ERROR, "Can't use image mode " + ImageMode.ContinueNormal + " without a child layer. Falling back to " + ImageMode.FillEmpty);
            this.imageMode = ImageMode.FillEmpty;
        }
        else
        {
            this.imageMode = config.imageMode;
        }
        this.fillBiome = world.getBiomeByNameOrNull(config.imageFillBiome).getIds().getOTGBiomeId();
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] biomes = cache.getArray(xSize * zSize);

        image.update();
        switch(imageMode)
        {
            case Repeat:
                for(int z0 = 0; z0 < zSize; z0++)
                {
                    int z1 = MathHelper.floorMod(z + z0 - zOffset, image.height);
                    for(int x0 = 0; x0 < xSize; x0++)
                    {
                        int x1 = MathHelper.floorMod(x + x0 - xOffset, image.width);

                        int biome = image.get(x1, z1);
                        if(biome == -1)
                        {
                            biome = fillBiome;
                        }
                        biomes[z0 * xSize + x0] = biome;
                    }
                }
                break;
            case Mirror:
                for(int z0 = 0; z0 < zSize; z0++)
                {
                    int z1 = MathHelper.transformMirror(z + z0 - zOffset, image.height);
                    for(int x0 = 0; x0 < xSize; x0++)
                    {
                        int x1 = MathHelper.transformMirror(x + x0 - xOffset, image.width);

                        int biome = image.get(x1, z1);
                        if(biome == -1)
                        {
                            biome = fillBiome;
                        }
                        biomes[z0 * xSize + x0] = biome;
                    }
                }
                break;
            case ContinueNormal:
                int[] childBiomes = null;
                for(int z0 = 0; z0 < zSize; z0++)
                {
                    int z1 = z + z0 - zOffset;
                    for(int x0 = 0; x0 < xSize; x0++)
                    {
                        int x1 = x + x0 - xOffset;

                        int biome = -1;
                        if(x1 >= 0 && x1 < image.width && z1 >= 0 && z1 < image.height)
                        {
                            biome = image.get(x1, z1);
                        }
                        if(biome == -1)
                        {
                            if(childBiomes == null)
                                childBiomes = child.getInts(world, cache, x, z, xSize, zSize);
                            biome = childBiomes[z0 * xSize + x0];
                        }
                        if(biome == -1)
                        {
                            biome = fillBiome;
                        }
                        biomes[z0 * xSize + x0] = biome;
                    }
                }
                break;
            case FillEmpty:
                for(int z0 = 0; z0 < zSize; z0++)
                {
                    int z1 = z + z0 - zOffset;
                    for(int x0 = 0; x0 < xSize; x0++)
                    {
                        int x1 = x + x0 - xOffset;

                        int biome = -1;
                        if(x1 >= 0 && x1 < image.width && z1 >= 0 && z1 < image.height)
                        {
                            biome = image.get(x1, z1);
                        }
                        if(biome == -1)
                        {
                            biome = fillBiome;
                        }
                        biomes[z0 * xSize + x0] = biome;
                    }
                }
                break;
            default:
                throw new IllegalStateException();
        }

        return biomes;
    }

    private static class Image {

        final Path cacheDir;
        final int width;
        final int height;
        final Chunk[] chunks;

        Image(WorldConfig config) throws IOException {
            Path imageFile = new File(config.settingsDir, config.imageFile).toPath();

            if (!Files.exists(imageFile)) {
                throw new IllegalArgumentException("Image '" + config.imageFile + "' for config '" + config.getName() + "' does not exist");
            }

            cacheDir = Paths.get(".").resolve(".otg").resolve("images").resolve(config.getName());
            Path infoFile = cacheDir.resolve("info");
            String hash = sha256(config.biomeColorMap, imageFile);

            if (Files.exists(infoFile)) {
                List<String> lines = Files.readAllLines(infoFile);
                if (lines.size() >= 3 && lines.get(0).equals(hash)) {
                    width = Integer.parseInt(lines.get(1));
                    height = Integer.parseInt(lines.get(2));
                    chunks = new Chunk[(width + Chunk.MASK >> Chunk.SHIFT) * (height + Chunk.MASK >> Chunk.SHIFT)];
                    return;
                }
            }

            BufferedImage image = ImageIO.read(imageFile.toFile());
            width = image.getWidth();
            height = image.getHeight();
            chunks = new Chunk[(width + Chunk.MASK >> Chunk.SHIFT) * (height + Chunk.MASK >> Chunk.SHIFT)];
            int[] biomes = image.getRGB(0, 0, width, height, null, 0, width);
            switch (config.imageOrientation) {
                case North:
                    break;
                case East:
                    int[] r90 = new int[biomes.length];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            r90[x * height + (height - 1 - y)] = biomes[y * width + x];
                        }
                    }
                    biomes = r90;
                    break;
                case South:
                    int[] r180 = new int[biomes.length];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            r180[(height - 1 - y) * width + (width - 1 - x)] = biomes[y * width + x];
                        }
                    }
                    biomes = r180;
                    break;
                case West:
                    int[] r270 = new int[biomes.length];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            r270[(width - 1 - x) * height + y] = biomes[y * width + x];
                        }
                    }
                    biomes = r270;
                    break;
                default:
                    break;
            }
            for (int i = 0; i < biomes.length; i++) {
                biomes[i] = config.biomeColorMap.get(biomes[i] & 0xFFFFFF);
            }

            Files.createDirectories(cacheDir);
            for (int x = 0; x < width + Chunk.MASK >> Chunk.SHIFT; x++) {
                for (int z = 0; z < height + Chunk.MASK >> Chunk.SHIFT; z++) {
                    int[] chunkData = new int[Chunk.SIZE * Chunk.SIZE];
                    for (int z0 = 0; z0 < Math.min(Chunk.SIZE, height - (z << Chunk.SHIFT)); z0++) {
                        for (int x0 = 0; x0 < Math.min(Chunk.SIZE, width - (x << Chunk.SHIFT)); x0++) {
                            chunkData[(z0 << Chunk.SHIFT) | x0] = biomes[((z << Chunk.SHIFT) | z0) * width + ((x << Chunk.SHIFT) | x0)];
                        }
                    }
                    writeChunkData(chunkData, cacheDir.resolve(Integer.toString(z * (width + Chunk.MASK >> Chunk.SHIFT) + x)));
                }
            }
            Files.write(infoFile, Arrays.asList(hash, Integer.toString(width), Integer.toString(height)));
        }

        static String sha256(Int2IntMap colorBiomeMap, Path imageFile) throws IOException {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedOperationException(e);
            }
            colorBiomeMap.int2IntEntrySet().stream().sorted(Comparator.comparingInt(Int2IntMap.Entry::getIntKey)).forEach(e -> {
                digest.update((byte) (e.getIntKey() >>> 24));
                digest.update((byte) (e.getIntKey() >>> 16));
                digest.update((byte) (e.getIntKey() >>> 8));
                digest.update((byte) (e.getIntKey() >>> 0));
                digest.update((byte) (e.getIntValue() >>> 24));
                digest.update((byte) (e.getIntValue() >>> 16));
                digest.update((byte) (e.getIntValue() >>> 8));
                digest.update((byte) (e.getIntValue() >>> 0));
            });
            try (InputStream in = new BufferedInputStream(Files.newInputStream(imageFile))) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) {
                    digest.update(buf, 0, n);
                }
            }
            return DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
        }

        static void writeChunkData(int[] data, Path file) throws IOException {
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(file))))) {
                for (int i = 0; i < Chunk.SIZE * Chunk.SIZE; i++) {
                    out.writeShort(data[i]);
                }
            }
        }

        static short[] readChunkData(Path file) throws IOException {
            short[] data = new short[Chunk.SIZE * Chunk.SIZE];
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(file))))) {
                for (int i = 0; i < Chunk.SIZE * Chunk.SIZE; i++) {
                    data[i] = in.readShort();
                }
            }
            return data;
        }

        int get(int x, int z) {
            assert x >= 0 && x < width;
            assert z >= 0 && z < height;
            return getChunk(x >> Chunk.SHIFT, z >> Chunk.SHIFT).get(x & Chunk.MASK, z & Chunk.MASK);
        }

        Chunk getChunk(int x, int z) {
            int i = z * (width + Chunk.MASK >> Chunk.SHIFT) + x;
            Chunk chunk = chunks[i];
            if (chunk == null) {
                try {
                    chunks[i] = chunk = new Chunk(readChunkData(cacheDir.resolve(Integer.toString(i))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return chunk;
        }

        void update() {
            if (-Chunk.time + (Chunk.time = System.currentTimeMillis()) > 1000) {
                for (int j = 0; j < chunks.length; j++) {
                    Chunk c = chunks[j];
                    if (c != null && Chunk.time - c.lastAccess > 30_000)
                        chunks[j] = null;
                }
            }
        }

        static class Chunk {
            static final int SHIFT = 8;
            static final int SIZE = 1 << SHIFT;
            static final int MASK = SIZE - 1;
            final short[] data;
            long lastAccess = System.currentTimeMillis();
            static long time = System.currentTimeMillis();

            Chunk(short[] data) {
                this.data = data;
            }

            int get(int x, int z) {
                assert x >= 0 && x < SIZE;
                assert z >= 0 && z < SIZE;
                lastAccess = time;
                return data[(z << SHIFT) | x];
            }
        }

    }

}
