package com.pg85.otg.customobjects.structures.bo4.smoothing;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SmoothingAreaLine
{
	public int beginPointX;
	public short beginPointY = -1;
	public int beginPointZ;

	public int endPointX;
	public short endPointY = -1;
	public int endPointZ;

	public int originPointX;
	public short originPointY = -1;
	public int originPointZ;

	public int finalDestinationPointX;
    public short finalDestinationPointY = -1;
    public int finalDestinationPointZ;
    
    public SmoothingAreaLine() {}
    
    public SmoothingAreaLine(int beginPointX, short beginPointY, int beginPointZ, int endPointX, short endPointY, int endPointZ, int originPointX, short originPointY, int originPointZ, int finalDestinationPointX, short finalDestinationPointY, int finalDestinationPointZ)
    {
    	this(beginPointX, beginPointZ, endPointX, endPointZ, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointZ);
    	this.beginPointY = beginPointY;
    	this.endPointY = endPointY;
    	this.finalDestinationPointY = finalDestinationPointY;
    }
    
    public SmoothingAreaLine(int beginPointX, int beginPointZ, int endPointX, int endPointZ, int originPointX, short originPointY, int originPointZ, int finalDestinationPointX, int finalDestinationPointZ)
    {
    	this.beginPointX = beginPointX;
    	this.beginPointZ = beginPointZ;

    	this.endPointX = endPointX;
    	this.endPointZ = endPointZ;

    	this.originPointX = originPointX;
    	this.originPointY = originPointY;
    	this.originPointZ = originPointZ;

    	this.finalDestinationPointX = finalDestinationPointX;
    	this.finalDestinationPointZ = finalDestinationPointZ;
    }

    public static void write(DataOutput out, SmoothingAreaLine line) throws IOException
    {
        // TODO: Should only need origin and destination?
        out.writeInt(line.beginPointX);
        out.writeInt(line.beginPointY);
        out.writeInt(line.beginPointZ);

        out.writeInt(line.endPointX);
        out.writeInt(line.endPointY);
        out.writeInt(line.endPointZ);

        out.writeInt(line.originPointX);
        out.writeInt(line.originPointY);
        out.writeInt(line.originPointZ);

        out.writeInt(line.finalDestinationPointX);
        out.writeInt(line.finalDestinationPointY);
        out.writeInt(line.finalDestinationPointZ);
    }
    
    public static SmoothingAreaLine read(DataInput in) throws IOException
    {
        int beginPointX = in.readInt();
        int beginPointY = in.readInt();
        int beginPointZ = in.readInt();

        int endPointX = in.readInt();
        int endPointY = in.readInt();
        int endPointZ = in.readInt();

        int originPointX = in.readInt();
        int originPointY = in.readInt();
        int originPointZ = in.readInt();

        int finalDestinationPointX = in.readInt();
        int finalDestinationPointY = in.readInt();
        int finalDestinationPointZ = in.readInt();

        return new SmoothingAreaLine(beginPointX, (short) beginPointY, beginPointZ, endPointX, (short) endPointY, endPointZ, originPointX, (short) originPointY, originPointZ, finalDestinationPointX, (short) finalDestinationPointY, finalDestinationPointZ);
    }
}