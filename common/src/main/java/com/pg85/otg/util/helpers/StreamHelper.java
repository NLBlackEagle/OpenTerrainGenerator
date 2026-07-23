package com.pg85.otg.util.helpers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StreamHelper
{
    public static void writeStringToStream(DataOutput out, String value) throws IOException
    {
        boolean isNull;
        out.writeBoolean(isNull = value == null);
        if(!isNull)
        {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            out.writeShort(bytes.length);
            out.write(bytes);
        }
    }

    public static String readStringFromStream(DataInput in) throws IOException
    {
        if(in.readBoolean())
        {
            return null;
        }
        byte[] bytes = new byte[in.readShort()];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
