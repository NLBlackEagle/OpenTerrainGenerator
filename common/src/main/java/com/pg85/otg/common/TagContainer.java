package com.pg85.otg.common;

import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

import com.pg85.otg.util.bo3.NamedBinaryTag;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class TagContainer
{
    private static final Object2ReferenceMap<Map.Entry<Path, String>, TagContainer> INSTANCES = new Object2ReferenceOpenHashMap<>();
    private final String relativePath;
    private final TagProvider tagProvider;

    private TagContainer(Map.Entry<Path, String> p)
    {
        this.relativePath = p.getValue();
        this.tagProvider = TagProvider.of(p.getKey().resolve(p.getValue()));
    }

    public static TagContainer of(Path directory, String relativePath)
    {
        return INSTANCES.computeIfAbsent(new SimpleEntry<>(directory.toAbsolutePath(), relativePath), TagContainer::new);
    }

    public NamedBinaryTag getTag()
    {
        return tagProvider.get();
    }

    public String getRelativePath()
    {
        return relativePath;
    }
}
