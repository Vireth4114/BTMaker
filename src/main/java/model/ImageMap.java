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

	public ImageMap(DataInputStream dis, Resource b, short baseImageId) throws IOException {
		this(dis, b, baseImageId, ImageMap::readUByte);
	}

	public ImageMap(DataInputStream dis, Resource b, short baseImageId, Function<DataInputStream, Integer> readFromStream) throws IOException {
		this.width = readFromStream.apply(dis);
		this.height = readFromStream.apply(dis);
		this.originX = readFromStream.apply(dis);
		this.originY = readFromStream.apply(dis);
		this.atlasX = readFromStream.apply(dis);
		this.atlasY = readFromStream.apply(dis);
		this.image = Controller.rscBatch.get(b).get(readFromStream.apply(dis) - baseImageId).name;
	}

	public static int readByte(DataInputStream in) {
		try {
			return in.readByte();
		} catch (IOException e) {
			return 0;
		}
	}

	public static int readUByte(DataInputStream in) {
		try {
			return in.readByte() & 0xff;
		} catch (IOException e) {
			return 0;
		}
	}

	public static int readShort(DataInputStream in) {
		try {
			return in.readShort();
		} catch (IOException e) {
			return 0;
		}
	}

	public static int readYShort(DataInputStream in) {
		try {
			return in.readShort() & 0xffff;
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public String toString() {
		return toString("");
	}

	public String toString(String padding) {
		return padding+"file: "+image+", \n"+
				padding+"\tsize: ("+width+", "+height+"), \n"+
				padding+"\torigin: ("+originX+", "+originY+"), \n" +
				padding+"\tatlas: ("+atlasX+", "+atlasY+")";
	}
}