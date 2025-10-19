package com.pg85.otg.util;

import java.io.ByteArrayOutputStream;
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
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;

public class CompressionUtils
{
	public static byte[] compress(byte[] data) throws IOException
	{  
		Deflater deflater = new Deflater();  
		deflater.setInput(data);  
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);   
		deflater.finish();  
		byte[] buffer = new byte[1024];   
		while (!deflater.finished())
		{  
			int count = deflater.deflate(buffer); // returns the generated code... index  
			outputStream.write(buffer, 0, count);   
		}  
		outputStream.close();  
		byte[] output = outputStream.toByteArray();
		if(OTG.getPluginConfig().spawnLog)
		{
			OTG.log(LogMarker.INFO, "Original: " + data.length / 1024 + " Kb");  
			OTG.log(LogMarker.INFO, "Compressed: " + output.length / 1024 + " Kb");
		}
		return output;  
	}

	public static byte[] decompress(byte[] data) throws IOException, DataFormatException
	{  
		Inflater inflater = new Inflater();   
		inflater.setInput(data);  
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);  
		byte[] buffer = new byte[1024];  
		while (!inflater.finished())
		{  
			int count = inflater.inflate(buffer);  
			outputStream.write(buffer, 0, count);  
		}  
		outputStream.close();  
		byte[] output = outputStream.toByteArray();
		//OTG.log(LogMarker.INFO, "Original: " + data.length);  
		//OTG.log(LogMarker.INFO, "Decompressed: " + output.length);
		return output;  
	}

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
        return (buf.getShort(0) & 0xFFFF) == GZIPInputStream.GZIP_MAGIC ? new GZIPInputStream(in) : in;
    }

    public static OutputStream newGZIPOutputStream(Path file) throws IOException
    {
        return new GZIPOutputStream(Files.newOutputStream(file));
    }
}
