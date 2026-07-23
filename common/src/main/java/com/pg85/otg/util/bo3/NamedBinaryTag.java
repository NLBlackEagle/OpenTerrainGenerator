package com.pg85.otg.util.bo3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.CompressionUtils;

/**
 * NBT IO class
 *
 * @see <a
 *      href="https://github.com/udoprog/c10t/blob/master/docs/NBT.txt">Online
 *      NBT specification</a>
 */
public class NamedBinaryTag
{
    private final Type type;
    private Type listType = null;
    private final String name;
    private Object value;

    /**
     * Enum for the tag types.
     */
    public enum Type
    {
        TAG_End,
        TAG_Byte,
        TAG_Short,
        TAG_Int,
        TAG_Long,
        TAG_Float,
        TAG_Double,
        TAG_Byte_Array,
        TAG_String,
        TAG_List,
        TAG_Compound,
        TAG_Int_Array,
        TAG_Long_Array;

        public static final Type[] VALUES = Type.values();
    }

    /**
     * Create a new TAG_List or TAG_Compound NBT tag.
     *
     * @param type  either TAG_List or TAG_Compound
     * @param name  name for the new tag or null to create an unnamed tag.
     * @param value list of tags to add to the new tag.
     */
    public NamedBinaryTag(Type type, String name, NamedBinaryTag[] value)
    {
        this(type, name, (Object) value);
    }

    /**
     * Create a new TAG_List with an empty list. Use {@link NamedBinaryTag#addTag(NamedBinaryTag)}
     * to add tags later.
     *
     * @param name     name for this tag or null to create an unnamed tag.
     * @param listType type of the elements in this empty list.
     */
    public NamedBinaryTag(String name, Type listType)
    {
        this(Type.TAG_List, name, listType);
    }

    /**
     * Create a new NBT tag.
     *
     * @param type  any value from the {@link Type} enum.
     * @param name  name for the new tag or null to create an unnamed tag.
     * @param value an object that fits the tag type or a {@link Type} to
     *              create an empty TAG_List with this list type.
     */
    public NamedBinaryTag(Type type, String name, Object value)
    {
        switch (type)
        {
            case TAG_End:
                if (value != null)
                    throw new IllegalArgumentException();
                break;
            case TAG_Byte:
                if (!(value instanceof Byte))
                    throw new IllegalArgumentException();
                break;
            case TAG_Short:
                if (!(value instanceof Short))
                    throw new IllegalArgumentException();
                break;
            case TAG_Int:
                if (!(value instanceof Integer))
                    throw new IllegalArgumentException();
                break;
            case TAG_Long:
                if (!(value instanceof Long))
                    throw new IllegalArgumentException();
                break;
            case TAG_Float:
                if (!(value instanceof Float))
                    throw new IllegalArgumentException();
                break;
            case TAG_Double:
                if (!(value instanceof Double))
                    throw new IllegalArgumentException();
                break;
            case TAG_Byte_Array:
                if (!(value instanceof byte[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_String:
                if (!(value instanceof String))
                    throw new IllegalArgumentException();
                break;
            case TAG_List:
                if (value instanceof Type)
                {
                    this.listType = (Type) value;
                    value = new NamedBinaryTag[0];
                } else
                {
                    if (!(value instanceof NamedBinaryTag[]))
                        throw new IllegalArgumentException();
                    this.listType = (((NamedBinaryTag[]) value)[0]).getType();
                }
                break;
            case TAG_Compound:
                if (!(value instanceof NamedBinaryTag[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_Int_Array:
                if (!(value instanceof int[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_Long_Array:
                if (!(value instanceof long[]))
                    throw new IllegalArgumentException();
                break;
            default:
                throw new IllegalArgumentException();
        }
        this.type = type;
        this.name = name == null ? "" : name;
        this.value = value;
    }

    public Type getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object newValue)
    {
        switch (type)
        {
            case TAG_End:
                if (value != null)
                    throw new IllegalArgumentException();
                break;
            case TAG_Byte:
                if (!(value instanceof Byte))
                    throw new IllegalArgumentException();
                break;
            case TAG_Short:
                if (!(value instanceof Short))
                    throw new IllegalArgumentException();
                break;
            case TAG_Int:
                if (!(value instanceof Integer))
                    throw new IllegalArgumentException();
                break;
            case TAG_Long:
                if (!(value instanceof Long))
                    throw new IllegalArgumentException();
                break;
            case TAG_Float:
                if (!(value instanceof Float))
                    throw new IllegalArgumentException();
                break;
            case TAG_Double:
                if (!(value instanceof Double))
                    throw new IllegalArgumentException();
                break;
            case TAG_Byte_Array:
                if (!(value instanceof byte[]))
                    throw new IllegalArgumentException();
            case TAG_String:
                if (!(value instanceof String))
                    throw new IllegalArgumentException();
                break;
            case TAG_List:
                if (value instanceof Type)
                {
                    this.listType = (Type) value;
                    value = new NamedBinaryTag[0];
                } else
                {
                    if (!(value instanceof NamedBinaryTag[]))
                        throw new IllegalArgumentException();
                    this.listType = (((NamedBinaryTag[]) value)[0]).getType();
                }
                break;
            case TAG_Compound:
                if (!(value instanceof NamedBinaryTag[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_Int_Array:
                if (!(value instanceof int[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_Long_Array:
                if (!(value instanceof long[]))
                    throw new IllegalArgumentException();
                break;
            default:
                throw new IllegalArgumentException();
        }

        value = newValue;
    }

    public Type getListType()
    {
        return listType;
    }

    /**
     * Add a tag to a TAG_List or a TAG_Compound.
     * @param tag The tag to add.
     */
    public void addTag(NamedBinaryTag tag)
    {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        NamedBinaryTag[] subtags = (NamedBinaryTag[]) value;

        int index = subtags.length;

        // For TAG_Compund entries, we need to add the tag BEFORE the end,
        // or the new tag gets placed after the TAG_End, messing up the data.
        // TAG_End MUST be kept at the very end of the TAG_Compound.
        if (type == Type.TAG_Compound)
            index--;
        insertTag(tag, index);
    }

    /**
     * Add a tag to a TAG_List or a TAG_Compound at the specified index.
     * @param tag   The tag to add.
     * @param index Index of the tag.
     */
    private void insertTag(NamedBinaryTag tag, int index)
    {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        NamedBinaryTag[] subtags = (NamedBinaryTag[]) value;
        if (subtags.length > 0)
            if (type == Type.TAG_List && tag.getType() != getListType())
                throw new IllegalArgumentException();
        if (index > subtags.length)
            throw new IndexOutOfBoundsException();
        NamedBinaryTag[] newValue = new NamedBinaryTag[subtags.length + 1];
        System.arraycopy(subtags, 0, newValue, 0, index);
        newValue[index] = tag;
        System.arraycopy(subtags, index, newValue, index + 1, subtags.length - index);
        value = newValue;
    }

    /**
     * Remove a tag from a TAG_List or a TAG_Compound at the specified index.
     *
     * @param index Index of the tag.
     * @return the removed tag
     */
    private NamedBinaryTag removeTag(int index)
    {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        NamedBinaryTag[] subtags = (NamedBinaryTag[]) value;
        NamedBinaryTag victim = subtags[index];
        NamedBinaryTag[] newValue = new NamedBinaryTag[subtags.length - 1];
        System.arraycopy(subtags, 0, newValue, 0, index);
        index++;
        System.arraycopy(subtags, index, newValue, index - 1, subtags.length - index);
        value = newValue;
        return victim;
    }

    /**
     * Remove a tag from a TAG_List or a TAG_Compound. If the tag is not a
     * child of this tag then nested tags are searched.
     *
     * @param tag tag to look for
     */
    public void removeSubTag(NamedBinaryTag tag)
    {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        if (tag == null)
            return;
        NamedBinaryTag[] subtags = (NamedBinaryTag[]) value;
        for (int i = 0; i < subtags.length; i++)
        {
            if (subtags[i] == tag)
            {
                removeTag(i);
                return;
            } else
            {
                if (subtags[i].type == Type.TAG_List || subtags[i].type == Type.TAG_Compound)
                {
                    subtags[i].removeSubTag(tag);
                }
            }
        }
    }

    /**
     * Find the tag with specified name in a TAG_Compound. If this tag has any
     * other type this method returns null. If no child tag exists with this
     * name this method returns null.
     *
     * @param name the name to look for. May be null to look for unnamed tags.
     * @return the first nested tag that has the specified name.
     */
    public NamedBinaryTag getTag(String name)
    {
        if (type != Type.TAG_Compound)
        {
            return null;
        }

        if (name == null)
        {
            name = "";
        }

        for (NamedBinaryTag subTag : values())
        {
            if (subTag.name.equals(name))
            {
                return subTag;
            }
        }
        return null;
    }

    /**
     * Get all child tags of this tag. If the type of this tag isn't
     * TAG_Compound or TAG_List, an emtpy array is returned. Changing the
     * array will change the structure of this tag.
     *
     * @return All child tags of this tag.
     */
    private NamedBinaryTag[] values()
    {
        if (type != Type.TAG_Compound && type != Type.TAG_List)
        {
            return new NamedBinaryTag[0];
        }
        return (NamedBinaryTag[]) value;
    }

    public static NamedBinaryTag readFrom(Path file) throws IOException, InvalidConfigException
    {
        try(DataInputStream in = new DataInputStream(new BufferedInputStream(CompressionUtils.newGZIPInputStream(file))))
        {
            return readFrom(in);
        }
    }

    /**
     * Read a tag and its nested tags from a {@link DataInputStream}
     *
     * @param in stream to read from
     * @return NBT tag or structure read from the InputStream
     * @throws IOException if there was no valid NBT structure in the
     *             InputStream or if another IOException occurred.
     */
    public static NamedBinaryTag readFrom(DataInputStream in) throws IOException, InvalidConfigException
    {
        try
        {
            Type type = readType(in);
            if(type == Type.TAG_End)
            {
                return new NamedBinaryTag(type, null, null);
            }
            return new NamedBinaryTag(type, in.readUTF(), readPayload(in, type));
        }
        catch(IOException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new InvalidConfigException(null, e);
        }
    }

    private static Type readType(DataInputStream in) throws IOException
    {
        return Type.VALUES[in.readByte()];
    }

    private static Object readPayload(DataInputStream in, Type type) throws IOException
    {
        switch(type)
        {
            case TAG_End:
                return null;
            case TAG_Byte:
                return in.readByte();
            case TAG_Short:
                return in.readShort();
            case TAG_Int:
                return in.readInt();
            case TAG_Long:
                return in.readLong();
            case TAG_Float:
                return in.readFloat();
            case TAG_Double:
                return in.readDouble();
            case TAG_Byte_Array:
                byte[] bytes = new byte[in.readInt()];
                in.readFully(bytes);
                return bytes;
            case TAG_String:
                return in.readUTF();
            case TAG_List:
                Type elementType = readType(in);
                int size = in.readInt();
                if(size == 0)
                {
                    return elementType;
                }
                NamedBinaryTag[] elements = new NamedBinaryTag[size];
                for(int i = 0; i < size; i++)
                {
                    elements[i] = new NamedBinaryTag(elementType, null, readPayload(in, elementType));
                }
                return elements;
            case TAG_Compound:
                NamedBinaryTag[] tags = new NamedBinaryTag[0];
                Type tagType;
                while((tagType = readType(in)) != Type.TAG_End)
                {
                    tags = Arrays.copyOf(tags, tags.length + 1);
                    tags[tags.length - 1] = new NamedBinaryTag(tagType, in.readUTF(), readPayload(in, tagType));
                }
                return tags;
            case TAG_Int_Array:
                int[] ints = new int[in.readInt()];
                for(int i = 0; i < ints.length; i++)
                {
                    ints[i] = in.readInt();
                }
                return ints;
            case TAG_Long_Array:
                long[] longs = new long[in.readInt()];
                for(int i = 0; i < longs.length; i++)
                {
                    longs[i] = in.readLong();
                }
                return longs;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void writeTo(Path file) throws IOException
    {
        try(DataOutputStream out = new DataOutputStream(new BufferedOutputStream(CompressionUtils.newGZIPOutputStream(file))))
        {
            writeTo(out);
        }
    }

    /**
     * Write a tag and its nested tags to a {@link DataOutputStream}
     *
     * @param out stream to write to
     * @throws IOException if this is not a valid NBT structure or if any
     *             IOException occurred.
     */
    public void writeTo(DataOutputStream out) throws IOException
    {
        writeType(out, type);
        if(type != Type.TAG_End)
        {
            out.writeUTF("");
            writePayload(out);
        }
    }

    private void writeType(DataOutputStream out, Type type) throws IOException
    {
        out.writeByte(type.ordinal());
    }

    private void writePayload(DataOutputStream out) throws IOException
    {
        switch(type)
        {
            case TAG_End:
                break;
            case TAG_Byte:
                out.writeByte((byte) value);
                break;
            case TAG_Short:
                out.writeShort((short) value);
                break;
            case TAG_Int:
                out.writeInt((int) value);
                break;
            case TAG_Long:
                out.writeLong((long) value);
                break;
            case TAG_Float:
                out.writeFloat((float) value);
                break;
            case TAG_Double:
                out.writeDouble((double) value);
                break;
            case TAG_Byte_Array:
                byte[] bytes = (byte[]) value;
                out.writeInt(bytes.length);
                out.write(bytes);
                break;
            case TAG_String:
                out.writeUTF((String) value);
                break;
            case TAG_List:
                NamedBinaryTag[] elements = (NamedBinaryTag[]) value;
                writeType(out, listType);
                out.writeInt(elements.length);
                for(NamedBinaryTag element : elements)
                {
                    element.writePayload(out);
                }
                break;
            case TAG_Compound:
                for(NamedBinaryTag tag : (NamedBinaryTag[]) value)
                {
                    writeType(out, tag.type);
                    if(tag.type != Type.TAG_End)
                    {
                        out.writeUTF(tag.name);
                        tag.writePayload(out);
                    }
                }
                break;
            case TAG_Int_Array:
                int[] ints = (int[]) value;
                out.writeInt(ints.length);
                for(int i : ints)
                {
                    out.writeInt(i);
                }
                break;
            case TAG_Long_Array:
                long[] longs = (long[]) value;
                out.writeInt(longs.length);
                for(long l : longs)
                {
                    out.writeLong(l);
                }
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private String getTypeString(Type type)
    {
        return type.name();
    }

    private void indent(int indent)
    {
        for (int i = 0; i < indent; i++)
        {
            System.out.print("   ");
        }
    }

    private void print(NamedBinaryTag t, int indent)
    {
        Type type = t.getType();
        if (type == Type.TAG_End)
            return;
        String name = t.getName();
        indent(indent);
        System.out.print(getTypeString(t.getType()));
        if (name != null)
            System.out.print("(\"" + t.getName() + "\")");
        if (type == Type.TAG_Byte_Array)
        {
            byte[] b = (byte[]) t.getValue();
            System.out.println(": [" + b.length + " bytes]");
        } else if (type == Type.TAG_List)
        {
            NamedBinaryTag[] subtags = (NamedBinaryTag[]) t.getValue();
            System.out.println(": " + subtags.length + " entries of type " + getTypeString(t.getListType()));
            for (NamedBinaryTag st : subtags)
            {
                print(st, indent + 1);
            }
            indent(indent);
            System.out.println("}");
        } else if (type == Type.TAG_Compound)
        {
            NamedBinaryTag[] subtags = (NamedBinaryTag[]) t.getValue();
            System.out.println(": " + (subtags.length - 1) + " entries");
            indent(indent);
            System.out.println("{");
            for (NamedBinaryTag st : subtags)
            {
                print(st, indent + 1);
            }
            indent(indent);
            System.out.println("}");
        } else if (type == Type.TAG_Int_Array)
        {
            int[] i = (int[]) t.getValue();
            System.out.println(": [" + i.length * 4 + " bytes]");

        } else
        {
            System.out.println(": " + t.getValue());
        }
    }

}
