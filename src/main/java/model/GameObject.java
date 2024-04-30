package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.shape.Shape;

public class GameObject {
	public short length;
	public short id;
	public byte type;
	public short parentID;
	public short previousID;
	public byte transformFlags;
	public double rotation;
	public double xScale;
	public double yScale;
	public short xPos;
	public short yPos;
	public short xAbs;
	public short yAbs;
	public boolean absSet = false;
	public int flags;
	public int zcoord;
	public int noDraw;
	public short nbRead;
	
	@Override
	public String toString() {
		return "GameObject [id=" + id + ", type=" + type + ", parentID=" + parentID + 
				", xPos=" + xPos + ", yPos=" + yPos + ", xAbs=" + xAbs +
				", yAbs=" + yAbs + ", flags=" + flags + ", zcoord=" +
				zcoord + ", noDraw=" + noDraw + "]";
	}
	
	public GameObject(short id, byte type) {
		this.id = id;
		this.type = type;
	}
	
	public short read(DataInputStream dis) throws IOException {
		length = dis.readShort();
		parentID = dis.readShort();
		previousID = dis.readShort();
		transformFlags = dis.readByte();
		nbRead = 13;
		if ((transformFlags & 7) == 7) {
			//This flag is only used by the first cannon in Ch11 for no reason
			//There may never be any use of it due to the impracticality of scaling
			dis.skip(8); 
			xPos = (short)(dis.readInt() >> 16);
			dis.skip(8);
			yPos = (short)(dis.readInt() >> 16);
			rotation = 0.0;
			xScale = -1.0;
			yScale = 1.0;
			nbRead += 20;
		} else {
			if ((transformFlags & 1) > 0) {
				xPos = dis.readShort();
				yPos = dis.readShort();
				rotation = 0.0;
				xScale = 1.0;
				yScale = 1.0;
			}
			if ((transformFlags & 2) > 0) {
				nbRead += 4;
				rotation = Math.round(Math.toDegrees(dis.readInt()/65536.0) * 100.0) / 100.0;
			}
			if ((transformFlags & 4) > 0) {
				nbRead += 8;
				xScale = dis.readInt()/65536.0;
				yScale = dis.readInt()/65536.0;
			}
		}
		flags = dis.readInt();
		zcoord = flags & 0x1f;
		noDraw = flags & 0x80;
		return nbRead;
	}
	
	public void skip(DataInputStream dis) throws IOException {
		dis.skip(length-nbRead);
	}
	
	public int preWrite(DataOutputStream dos) throws IOException {
		dos.writeByte(type);
		if (transformFlags == 7) {
			length -= 12;
		}
		dos.writeShort(length);
		dos.writeShort(parentID);
		dos.writeShort(previousID);
		int offset = 0;
		if (transformFlags == 7) {
			dos.writeByte(5);
			offset = 12;
		} else {
			dos.writeByte(transformFlags);
		}
		dos.writeShort(xPos);
		dos.writeShort(yPos);
		if (transformFlags == 3) {
			dos.writeInt((int) Math.round(Math.toRadians(rotation)*65536));
			offset += 4;
		} else if (transformFlags == 5 || transformFlags == 7) {
			dos.writeInt((int) Math.round(xScale*65536));
			dos.writeInt((int) Math.round(yScale*65536));
			offset += 8;
		}
		dos.writeInt(flags);
		return offset;
	}
	
	public void write(DataOutputStream dos, DataInputStream disOG) throws IOException {
		int offset = preWrite(dos);
		disOG.skip(16+offset);
		dos.write(disOG.readNBytes(transformFlags != 7 ? length-13-offset : 3));
		if (transformFlags == 7) {
			transformFlags = 5;	
		}
	}
	
	public short[] doAbs(List<GameObject> objects) {
		if (!absSet) {
			absSet = true;
			if (id == 0 || parentID == 0) {
				xAbs = xPos;
				yAbs = yPos;
			} else {
				short[] parentXY = objects.get(parentID).doAbs(objects);
				xAbs = (short) (parentXY[0] + xPos);
				yAbs = (short) (parentXY[1] + yPos);
			}
		}
		return new short[]{xAbs, yAbs};
	}

	public List<Shape> getShapes(Controller controller) {
		return new ArrayList<Shape>();
	}
	
	public void onClick(Controller controller) {}
}
