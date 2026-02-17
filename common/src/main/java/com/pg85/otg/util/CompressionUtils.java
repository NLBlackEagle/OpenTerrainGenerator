package com.pg85.otg.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class CompressionUtils
{
    public static InputStream newInflaterInputStream(Path file) throws IOException
    {
        return new InflaterInputStream(Files.newInputStream(file));
    }

    public static OutputStream newDeflaterOutputStream(Path file) throws IOException
    {
        return new DeflaterOutputStream(Files.newOutputStream(file));
    }

    public static InputStream newGZIPInputStream(Path file) throws IOException
    {
        @SuppressWarnings("resource")
        SeekableByteChannel channel = Files.newByteChannel(file);
        ByteBuffer buf = ByteBuffer.allocate(2).order(ByteOrder.nativeOrder());
        while(buf.hasRemaining())
        {
            if(channel.read(buf) < 0)
            {
                throw new EOFException();
            }
        }
        channel.position(0);
        InputStream in = Channels.newInputStream(channel);
        return Short.toUnsignedInt(buf.getShort(0)) == GZIPInputStream.GZIP_MAGIC ? new GZIPInputStream(in) : in;
    }

    public static OutputStream newGZIPOutputStream(Path file) throws IOException
    {
        return new GZIPOutputStream(Files.newOutputStream(file));
    }
}
