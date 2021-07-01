package com.pg85.otg.spigot.materials;

import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;
import com.pg85.otg.util.materials.MaterialProperties;
import com.pg85.otg.util.materials.MaterialProperty;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 */
public class SpigotMaterialData extends LocalMaterialData
{
	static final LocalMaterialData blank = new SpigotMaterialData(null, null, true);
	private static final HashMap<IBlockData, LocalMaterialData> stateToMaterialDataMap = new HashMap<>();

	private final IBlockData blockData;
	private String name = null;

	private SpigotMaterialData(IBlockData blockData, String raw)
	{
		this(blockData, raw, false);
	}
	
	private SpigotMaterialData(IBlockData blockData, String raw, boolean isBlank)
	{
		this.blockData = blockData;
		this.rawEntry = raw;
		this.isBlank = isBlank;
	}

	public static LocalMaterialData ofBlockData(IBlockData blockData)
	{
		return ofBlockData(blockData, null);
	}

	public static LocalMaterialData ofBlockData(IBlockData blockData, String raw)
	{
		// Create only one LocalMaterialData object for each BlockState		
		if (stateToMaterialDataMap.containsKey(blockData))
		{
			return stateToMaterialDataMap.get(blockData);
		}
		LocalMaterialData data = new SpigotMaterialData(blockData, raw);
		stateToMaterialDataMap.put(blockData, data);
		return data;
	}

	public static LocalMaterialData ofSpigotMaterial(org.bukkit.Material type)
	{
		return ofBlockData(((CraftBlockData)type.createBlockData()).getState(), null);
	}

	public IBlockData internalBlock()
	{
		return this.blockData;
	}
	
	public BlockData toSpigotBlockData()
	{
		if (this.blockData == null)
		{
			return null;
		}
		return CraftBlockData.fromData(this.blockData);
	}	
	
	@Override
	public String getName()
	{
		if (this.name != null)
		{
			return this.name;
		}
		if (isBlank)
		{
			this.name = "BLANK";
		}
		else if (this.blockData == null)
		{
			if (this.rawEntry != null)
			{
				this.name = this.rawEntry;
			} else {
				this.name = "Unknown";
			}
		} else {
			this.name = this.blockData.toString()
				.replace("Block{", "")
				.replace("}", "");
		}
		return this.name;
	}

	@Override
	public boolean isLiquid()
	{
		return 
			this.blockData != null &&
			(
				this.blockData.getMaterial() == Material.WATER || 
				this.blockData.getMaterial() == Material.LAVA
			);
	}

	@Override
	public boolean isSolid()
	{
		// TODO: This should check for isSolid and isSolidBlocking, there is no isSolidBlocking for Spigot tho?
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}

	@Override
	public boolean isEmptyOrAir()
	{
		return this.blockData == null || this.blockData.getMaterial() == Material.AIR;
	}

	@Override
	public boolean isAir()
	{
		return this.blockData != null && this.blockData.getMaterial() == Material.AIR;
	}

	@Override
	public boolean isEmpty()
	{
		return this.blockData == null;
	}

	@Override
	public boolean canFall()
	{
		return this.blockData != null && this.blockData.getBlock() instanceof BlockFalling;
	}
	
	@Override
	public boolean canSnowFallOn()
	{
		return this.blockData != null && this.blockData.getMaterial().isSolid();
	}
	
	@Override
	public boolean isMaterial(LocalMaterialData material)
	{
		return 
			(this.isBlank && ((SpigotMaterialData) material).isBlank) ||
			(
				!this.isBlank &&
				!((SpigotMaterialData) material).isBlank &&
				Objects.equals(IRegistry.BLOCK.getKey(this.blockData.getBlock()), IRegistry.BLOCK.getKey(((SpigotMaterialData) material).blockData.getBlock()))
			)
		;
	}

	@Override
	public LocalMaterialData rotate(int rotateTimes)
	{
		// Get the rotation if we haven't stored the rotation yet
		if (rotated == null)
		{
			IBlockData state = this.blockData;
			Collection<IBlockState<?>> properties = state.r();
			// Loop through the blocks properties
			for (IBlockState<?> property : properties)
			{
				// Anything with a direction
				if (property instanceof BlockStateDirection)
				{
					EnumDirection direction = (EnumDirection) state.get(property);
					switch (direction)
					{
						case DOWN:
						case UP:
							break;
						case NORTH:
							state = state.set((BlockStateDirection) property, EnumDirection.WEST);
							break;
						case SOUTH:
							state = state.set((BlockStateDirection) property, EnumDirection.EAST);
							break;
						case WEST:
							state = state.set((BlockStateDirection) property, EnumDirection.SOUTH);
							break;
						case EAST:
							state = state.set((BlockStateDirection) property, EnumDirection.NORTH);
							break;
					}
				}
			}

			if (state.b(BlockTall.EAST)) // fence or glass pane
			{
				// Cache the east value, before it's overwritten by the rotated south value
				boolean hasEast = state.get(BlockTall.EAST);
				state = state.set(BlockTall.EAST, state.get(BlockTall.SOUTH));
				state = state.set(BlockTall.SOUTH, state.get(BlockTall.WEST));
				state = state.set(BlockTall.WEST, state.get(BlockTall.NORTH));
				state = state.set(BlockTall.NORTH, hasEast);
			}
			// Block is rotated, store a pointer to it			
			this.rotated = SpigotMaterialData.ofBlockData(state);
		}

		if (rotateTimes > 1) {
			return rotated.rotate(rotateTimes-1);
		}

		return this.rotated;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> LocalMaterialData withProperty(MaterialProperty<T> materialProperty, T value)
	{
		@SuppressWarnings("rawtypes")
		IBlockState property = null;
		T finalVal = value;

		// TODO: This is really bad. We need a way to append properties onto the MaterialProperty
		if (materialProperty == MaterialProperties.AGE_0_25)
		{
			property = BlockProperties.ak;
		}
		else if (materialProperty == MaterialProperties.PICKLES_1_4)
		{
			property = BlockProperties.ay;
		} else if (materialProperty == MaterialProperties.SNOWY)
		{
			property = BlockProperties.z;
		} else if (materialProperty == MaterialProperties.HORIZONTAL_DIRECTION)
		{
			// Extremely ugly hack for directions
			property = BlockProperties.O;
			EnumDirection direction = EnumDirection.values()[((OTGDirection)value).ordinal()];
			return SpigotMaterialData.ofBlockData(this.blockData.set(property, direction));
		} else
		{
			throw new IllegalArgumentException("Unknown property: " + materialProperty);
		}

		return SpigotMaterialData.ofBlockData(this.blockData.set(property, finalVal));
	}
	
	@Override
	public boolean isBlockTag(LocalMaterialTag tag)
	{
		return this.blockData == null ? false : this.blockData.a(((SpigotMaterialTag)tag).getTag());
	}	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof SpigotMaterialData))
		{
			return false;
		}
		SpigotMaterialData other = (SpigotMaterialData) obj;
		return
			(this.isBlank && other.isBlank) ||
			(
				!this.isBlank &&
				!other.isBlank &&
				this.blockData.equals(other.blockData)
			);
	}

	@Override
	public int hashCode()
	{
		// TODO: Implement this for 1.16
		return this.blockData == null ? -1 : this.blockData.hashCode();
	}
}
