package com.pg85.otg.generator.resource;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.Random;

/**
 * Represents a single ore vein.
 *
 */
class Vein
{
    private int x, y, z, size;

    Vein(int blockX, int blockY, int blockZ, int size)
    {
        this.x = blockX;
        this.y = blockY;
        this.z = blockZ;
        this.size = size;
    }

    public int getChunkSize()
    {
        return (size + 15) / 16;
    }

    public boolean reachesChunk(int otherChunkX, int otherChunkZ)
    {
        // Calculate the nearest chunk x and z
        int chunkX = (x + 8) / 16;
        int chunkZ = (z + 8) / 16;
        // Calculate the ceiled chunk size
        int chunkSize = getChunkSize();

        if (MathHelper.abs(otherChunkX - chunkX) > chunkSize || MathHelper.abs(otherChunkZ - chunkZ) > chunkSize)
        {
            return false;
        }

        return true;
    }

    public void spawn(LocalWorld world, Random random, ChunkCoordinate chunkBeingPopulated, VeinGen gen)
    {
        int sizeSquared = size * size;

        for (int i = 0; i < gen.oreFrequency; i++)
        {
            if (random.nextInt(100) < gen.oreRarity)
            {
                int oreX = chunkBeingPopulated.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
                int oreY = RandomHelper.numberInRange(random, gen.minAltitude, gen.maxAltitude);
                int oreZ = chunkBeingPopulated.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);

                if ((oreX - x) * (oreX - x) + (oreY - y) * (oreY - y) + (oreZ - z) * (oreZ - z) < sizeSquared)
                {
                    spawnOre(world, random, oreX, oreY, oreZ, gen, chunkBeingPopulated);
                }
            }
        }
    }

    private void spawnOre(LocalWorld world, Random rand, int x, int y, int z, VeinGen gen, ChunkCoordinate chunkBeingPopulated)
    {
        int maxSize = gen.oreSize;
        LocalMaterialData material = gen.material;
        MaterialSet sourceBlocks = gen.sourceBlocks;

        float f = rand.nextFloat() * 3.141593F;

        double maxX = x + 8 + MathHelper.sin(f) * maxSize / 8.0F;
        double minX = x + 8 - MathHelper.sin(f) * maxSize / 8.0F;
        double maxZ = z + 8 + MathHelper.cos(f) * maxSize / 8.0F;
        double minZ = z + 8 - MathHelper.cos(f) * maxSize / 8.0F;

        double maxY = y + rand.nextInt(3) - 2;
        double minY = y + rand.nextInt(3) - 2;

        for (int i = 0; i < maxSize; i++)
        {
            float iFactor = (float) i / (float) maxSize;
            double x1 = maxX + (minX - maxX) * iFactor;
            double y1 = maxY + (minY - maxY) * iFactor;
            double z1 = maxZ + (minZ - maxZ) * iFactor;

            double d10 = rand.nextDouble() * maxSize / 16.0D;
            double d11 = (MathHelper.sin((float) Math.PI * iFactor) + 1.0) * d10 + 1.0;
            double d12 = (MathHelper.sin((float) Math.PI * iFactor) + 1.0) * d10 + 1.0;

            int minX1 = MathHelper.floor(x1 - d11 / 2.0D);
            int minY1 = MathHelper.floor(y1 - d12 / 2.0D);
            int minZ1 = MathHelper.floor(z1 - d11 / 2.0D);

            int maxX1 = MathHelper.floor(x1 + d11 / 2.0D);
            int maxY1 = MathHelper.floor(y1 + d12 / 2.0D);
            int maxZ1 = MathHelper.floor(z1 + d11 / 2.0D);

            for (int x2 = minX1; x2 <= maxX1; x2++)
            {
                double dx = (x2 + 0.5D - x1) / (d11 / 2.0D);
                if (dx * dx < 1.0D)
                {
                    for (int y2 = minY1; y2 <= maxY1; y2++)
                    {
                        double dy = (y2 + 0.5D - y1) / (d12 / 2.0D);
                        if (dx * dx + dy * dy < 1.0D)
                        {
                            for (int z2 = minZ1; z2 <= maxZ1; z2++)
                            {
                                double dz = (z2 + 0.5D - z1) / (d11 / 2.0D);
                                if ((dx * dx + dy * dy + dz * dz < 1.0D) && sourceBlocks.contains(world.getMaterial(x2, y2, z2, chunkBeingPopulated)))
                                {
                                    world.setBlock(x2, y2, z2, material, null, chunkBeingPopulated, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}