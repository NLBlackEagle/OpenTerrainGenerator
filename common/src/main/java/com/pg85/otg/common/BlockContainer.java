package com.pg85.otg.common;

import java.nio.file.Path;
import java.util.Objects;

import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

public abstract class BlockContainer
{
    protected final LocalMaterialData material;

    protected BlockContainer(LocalMaterialData material)
    {
        this.material = Objects.requireNonNull(material);
    }

    public static BlockContainer of(LocalMaterialData material)
    {
        return WithoutTag.of(material);
    }

    public static BlockContainer of(LocalMaterialData material, Path directory, String relativePath)
    {
        return WithTag.of(material, directory, relativePath);
    }

    public LocalMaterialData material()
    {
        return material;
    }

    public abstract boolean hasTag();

    public abstract NamedBinaryTag tag();

    public abstract String tagPath();

    public BlockContainer withMaterial(BlockContainer other)
    {
        return withMaterial(other.material);
    }

    public abstract BlockContainer withMaterial(LocalMaterialData material);

    public abstract BlockContainer rotate(Rotation rotation);

    public static class WithoutTag extends BlockContainer
    {
        private static final Reference2ReferenceMap<LocalMaterialData, WithoutTag> INSTANCES = new Reference2ReferenceOpenHashMap<>();

        private WithoutTag(LocalMaterialData material)
        {
            super(material);
        }

        public static BlockContainer of(LocalMaterialData material)
        {
            return INSTANCES.computeIfAbsent(material.withDefaultBlockData(), WithoutTag::new);
        }

        @Override
        public boolean hasTag()
        {
            return false;
        }

        @Override
        public NamedBinaryTag tag()
        {
            return null;
        }

        @Override
        public String tagPath()
        {
            return null;
        }

        @Override
        public BlockContainer withMaterial(LocalMaterialData material)
        {
            if(material == this.material)
                return this;
            return of(material);
        }

        @Override
        public BlockContainer rotate(Rotation rotation)
        {
            LocalMaterialData rotatedMaterial;
            if((rotatedMaterial = material.rotate(rotation)) == material)
            {
                return this;
            }
            return of(rotatedMaterial);
        }
    }

    public static class WithTag extends BlockContainer
    {
        private final TagContainer tagContainer;

        private WithTag(LocalMaterialData material, TagContainer tagContainer)
        {
            super(material);
            this.tagContainer = Objects.requireNonNull(tagContainer);
        }

        public static BlockContainer of(LocalMaterialData material, Path directory, String tagFileName)
        {
            // TODO implement cache?
            return new WithTag(material, TagContainer.of(directory, tagFileName));
        }

        @Override
        public boolean hasTag()
        {
            return true;
        }

        @Override
        public NamedBinaryTag tag()
        {
            return tagContainer.getTag();
        }

        @Override
        public String tagPath()
        {
            return tagContainer.getRelativePath();
        }

        @Override
        public BlockContainer withMaterial(LocalMaterialData material)
        {
            if(material == this.material)
                return this;
            return new WithTag(material, tagContainer);
        }

        @Override
        public BlockContainer rotate(Rotation rotation)
        {
            LocalMaterialData rotatedMaterial;
            if((rotatedMaterial = material.rotate(rotation)) == material)
            {
                return this;
            }
            return new WithTag(rotatedMaterial, tagContainer);
        }
    }
}
