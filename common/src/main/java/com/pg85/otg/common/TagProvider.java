package com.pg85.otg.common;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.pg85.otg.OTG;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.bo3.NamedBinaryTag.Type;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class TagProvider
{
    private static final Object2ReferenceMap<Path, TagProvider> INSTANCES = new Object2ReferenceOpenHashMap<>();
    private final Path file;
    private NamedBinaryTag tag;

    private TagProvider(Path file)
    {
        this.file = file;
    }

    public static TagProvider of(Path file)
    {
        return INSTANCES.computeIfAbsent(file.toAbsolutePath(), TagProvider::new);
    }

    public NamedBinaryTag get()
    {
        NamedBinaryTag tag;
        if((tag = this.tag) == null)
        {
            try
            {
                tag = NamedBinaryTag.readFrom(file);

                if(tag.getTag("id") == null)
                {
                    if(tag.getValue() instanceof NamedBinaryTag[] && ((NamedBinaryTag[]) tag.getValue()).length != 0)
                    {
                        tag = ((NamedBinaryTag[]) tag.getValue())[0];
                    }
                    else
                    {
                        OTG.log(LogMarker.WARN, "Structure of NBT file is incorrect: " + file);
                        tag = new NamedBinaryTag(Type.TAG_Compound, null, new NamedBinaryTag[0]);
                    }
                }

                this.tag = tag;
            }
            catch(NoSuchFileException e)
            {
                if(OTG.getPluginConfig().spawnLog)
                {
                    OTG.log(LogMarker.WARN, "NBT file {} not found", file);
                }
                tag = new NamedBinaryTag(Type.TAG_Compound, null, new NamedBinaryTag[0]);
            }
            catch(IOException | InvalidConfigException e)
            {
                OTG.log(LogMarker.ERROR, "Failed to read NBT file {}", file);
                OTG.printStackTrace(LogMarker.ERROR, e);
                tag = new NamedBinaryTag(Type.TAG_Compound, null, new NamedBinaryTag[0]);
            }
        }
        return tag;
    }
}
