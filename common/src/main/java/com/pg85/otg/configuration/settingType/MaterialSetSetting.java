package com.pg85.otg.configuration.settingType;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

/**
 * Reads and writes a set of materials, used for matching.
 *
 * <p>Materials are separated using a comma and, optionally, whitespace. Each
 * material is stripped from its whitespace and read using
 * {@link MaterialSet#parseAndAdd(String)}.
 *
 */
class MaterialSetSetting extends Setting<MaterialSet>
{
    private final MaterialSet defaultValues;

    MaterialSetSetting(String name, DefaultMaterial... defaultMaterials)
    {
        super(name);
        this.defaultValues = MaterialSet.create(defaultMaterials);
    }

    public MaterialSetSetting(String name, String... defaultValues)
    {
        super(name);
        this.defaultValues = MaterialSet.create(defaultValues);
    }

    @Override
    public MaterialSet getDefaultValue()
    {
        return defaultValues;
    }

    @Override
    public MaterialSet read(String string) throws InvalidConfigException
    {
        return MaterialSet.create(StringHelper.readCommaSeperatedString(string));
    }
}
