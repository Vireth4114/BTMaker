package model;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.Function;

import BTMaker.BTMaker.Controller;

public class ImageMap {
	public int width;
	public int height;
	public int originX;
	public int originY;
	public int atlasX;
	public int atlasY;
	public String image;

	public static int readByte(DataInputStream in) {
		try {return in.readByte();}
		catch (IOException e) {return 0;}
	}
	
	public static int readUByte(DataInputStream in) {
		try {return in.readByte() & 0xff;}
		catch (IOException e) {return 0;}
	}
	
	public static int readShort(DataInputStream in) {
		try {return in.readShort();}
		catch (IOException e) {return 0;}
	}

	public static int readYShort(DataInputStream in) {
		try {return in.readShort() & 0xffff;}
		catch (IOException e) {return 0;}
	}
	
	public ImageMap(DataInputStream dis, Resource b, short baseImageId) throws IOException {
		this(dis, b, baseImageId, false);
	}
	
	public ImageMap(DataInputStream dis, Resource b, short baseImageId, boolean is16) throws IOException {
		Function<DataInputStream, Integer> func = is16 ? ImageMap::readShort : ImageMap::readUByte;
		this.width = func.apply(dis);
		this.height = func.apply(dis);
		this.originX = func.apply(dis);
		this.originY = func.apply(dis);
		this.atlasX = func.apply(dis);
		this.atlasY = func.apply(dis);
		this.image = Controller.rscBatch.get(b).get(func.apply(dis) - baseImageId).name;
	}

	@Override
	public String toString() {
		return width+" "+height+" ("+originX+","+originY+") ("+atlasX+","+atlasY+") "+image;
	}
}