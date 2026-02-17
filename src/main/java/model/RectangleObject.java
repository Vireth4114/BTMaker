package model;

import java.io.DataInputStream;
import java.io.IOException;

public class RectangleObject extends GameObject {
	public short minX;
	public short maxX;
	public short minY;
	public short maxY;

	public RectangleObject(short id, byte type) {
		super(id, type);
	}

	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		minX = dis.readShort();
		maxY = dis.readShort();
		maxX = dis.readShort();
		minY = dis.readShort();
		nbRead = length;
		return nbRead;
	}

	public String getExport() {
		return super.getExport() +
				"\n\tminPos: (" + (xAbs + minX) + ", " + (yAbs + minY) + ")" +
                "\n\tmaxPos: (" + (xAbs + maxX) + ", " + (yAbs + maxY) + ")";
	}
}
