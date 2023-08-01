package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.scene.paint.Color;

public class Level {
	public ArrayList<GameObject> objects;
	public int xMin;
	public int xMax;
	public int yMin;
	public int yMax;
	
	public Level(String path) throws IOException {
		this.objects = new ArrayList<GameObject>();
		readObjects(path);
	}
	
	public void readObjects(String path) throws IOException {
		FileInputStream fis = new FileInputStream(path);
		DataInputStream dis = new DataInputStream(fis);
		dis.skip(14);
		short id = 0;
		byte type;
		while ((type = dis.readByte()) != 127) {
			GameObject obj = new GameObject();
			if (type == 4) {
				obj = new GeometryObject();
			}
			obj.id = id;
			obj.type = type;
			obj.length = dis.readShort();
			obj.parentID = dis.readShort();
			obj.previousID = dis.readShort();
			obj.transformFlags = dis.readByte();
			int offset = 13;
			if ((obj.transformFlags & 7) == 7) {
				//This flag is only used by the first cannon in Ch11 for no reason
				//There may never be any use of it due to the impracticality of scaling
				dis.skip(8); 
				obj.xPos = (short)(dis.readInt() >> 16);
				dis.skip(8);
				obj.yPos = (short)(dis.readInt() >> 16);
				obj.rotation = 0.0;
				obj.xScale = -1.0;
				obj.yScale = 1.0;
				offset += 20;
			} else {
				if ((obj.transformFlags & 1) > 0) {
					obj.xPos = dis.readShort();
					obj.yPos = dis.readShort();
					obj.rotation = 0.0;
					obj.xScale = 1.0;
					obj.yScale = 1.0;
				}
				if ((obj.transformFlags & 2) > 0) {
					offset += 4;
					obj.rotation = Math.round(Math.toDegrees(dis.readInt()/65536.0) * 100.0) / 100.0;
				}
				if ((obj.transformFlags & 4) > 0) {
					offset += 8;
					obj.xScale = dis.readInt()/65536.0;
					obj.yScale = dis.readInt()/65536.0;
				}
			}
			obj.flags = dis.readInt();
			obj.zcoord = obj.flags & 0x1f;
			obj.noDraw = obj.flags & 0x80;
			if (type == 4) {
				GeometryObject gObj = (GeometryObject) obj;
				gObj.angles = dis.readShort();
				gObj.polygons = dis.readShort();
				dis.skip(1);
				gObj.color = Color.rgb(dis.read(), dis.read(), dis.read());
				byte bitSize = dis.readByte();
				int fullSize = (int)Math.ceil(bitSize*gObj.angles/8.0);
				short base = dis.readShort();
				gObj.xList = toIndex(dis.readNBytes(fullSize), bitSize, base);
				base = dis.readShort();
				gObj.yList = toIndex(dis.readNBytes(fullSize), bitSize, base);
				bitSize = dis.readByte();
				fullSize = (int)Math.ceil(bitSize*gObj.polygons/8.0);
				base = 0;
				gObj.indexBuffer = toIndex(dis.readNBytes(fullSize), bitSize, base);
				objects.add(gObj);
				dis.skip(2);
			} else {
				objects.add(obj);
				dis.skip(obj.length-offset);
			}
			id++;
		}
		for (GameObject obj: objects) {
			obj.doAbs(objects);
			if (obj instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) obj;
				gObj.trueX = new int[gObj.angles];
				gObj.trueY = new int[gObj.angles];
				for (int i = 0; i < gObj.angles; i++) {
					gObj.trueX[i] = gObj.xList[i] + gObj.xAbs;
					gObj.trueY[i] = gObj.yList[i] + gObj.yAbs;
				}
			}
		}
		dis.close();
		fis.close();
		
		ArrayList<Integer> allXs = new ArrayList<Integer>();
		ArrayList<Integer> allYs = new ArrayList<Integer>();
		for (GameObject obj: objects) {
			if (obj instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) obj;
				for (int x: gObj.trueX) {
					allXs.add(x);
				}
				for (int y: gObj.trueY) {
					allYs.add(y);
				}
			}
		}
		
		xMin = Collections.min(allXs);
		xMax = Collections.max(allXs);
		yMin = Collections.min(allYs);
		yMax = Collections.max(allYs);
	}
	
	public int writeObjects(String path, String originalPath) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		DataOutputStream dos = new DataOutputStream(fos);
		FileInputStream fisOG = new FileInputStream(originalPath);
		DataInputStream disOG = new DataInputStream(fisOG);
		dos.write(disOG.readNBytes(8));
		dos.writeShort(objects.size());
		disOG.skip(2);
		dos.write(disOG.readNBytes(4));
		for (GameObject obj: objects) {
			dos.writeByte(obj.type);
			if (obj.type != 4) {
				if (obj.transformFlags == 7) {
					obj.length -= 12;
				}
				dos.writeShort(obj.length);
				dos.writeShort(obj.parentID);
				dos.writeShort(obj.previousID);
				if (obj.transformFlags == 7) {
					dos.writeByte(5);
				} else {
					dos.writeByte(obj.transformFlags);
				}
				dos.writeShort(obj.xPos);
				dos.writeShort(obj.yPos);
				int offset = 0;
				if (obj.transformFlags == 3) {
					dos.writeInt((int) Math.round(Math.toRadians(obj.rotation)*65536));
					offset += 4;
				} else if (obj.transformFlags == 5 || obj.transformFlags == 7) {
					dos.writeInt((int) Math.round(obj.xScale*65536));
					dos.writeInt((int) Math.round(obj.yScale*65536));
					offset += 8;
				}
				dos.writeInt(obj.flags);
				disOG.skip((obj.transformFlags != 7) ? 16+offset : 36);
				dos.write(disOG.readNBytes((obj.transformFlags != 7) ? obj.length-13-offset : 3));
				if (obj.transformFlags == 7) {
					obj.transformFlags = 5;	
				}
			} else {
				disOG.skip(obj.length+3);
				GeometryObject gObj = (GeometryObject) obj;
				int xMin = 100000;
				int xMax = -100000;
				for (int x: gObj.xList) {
					xMin = Math.min(xMin, x);
					xMax = Math.max(xMax, x);
				}
				int yMin = 100000;
				int yMax = -100000;
				for (int y: gObj.yList) {
					yMin = Math.min(yMin, y);
					yMax = Math.max(yMax, y);
				}
				short xBase = (short) Math.ceil((xMax + xMin)/2);
				short yBase = (short) Math.ceil((yMax + yMin)/2);
				byte bitSize = (byte) Math.max(Math.ceil(Math.log(xMax - xBase + 1)/Math.log(2)) + 1,
							                   Math.ceil(Math.log(yMax - yBase + 1)/Math.log(2)) + 1);
				int idxMax = -1;
				for (int idx: gObj.indexBuffer) {
					idxMax = Math.max(idxMax, idx);
				}
				byte idxBitSize = (byte) (Math.ceil(Math.log(idxMax + 1)/Math.log(2)) + 1);
				gObj.length = (short) (29 + 2 * (int) Math.ceil(bitSize*gObj.angles/8.0) + 
										        (int) Math.ceil(idxBitSize*gObj.polygons/8.0));

				if (obj.transformFlags == 3) {
					gObj.length += 4;
				} else if (obj.transformFlags == 5) {
					gObj.length += 8;
				}
				
				dos.writeShort(gObj.length);
				dos.writeShort(gObj.parentID);
				dos.writeShort(gObj.previousID);
				dos.writeByte(obj.transformFlags);
				dos.writeShort(obj.xPos);
				dos.writeShort(obj.yPos);
				if (obj.transformFlags == 3) {
					dos.writeInt((int) Math.round(Math.toRadians(obj.rotation)*65536));
				} else if (obj.transformFlags == 5) {
					dos.writeInt((int) Math.round(obj.xScale*65536));
					dos.writeInt((int) Math.round(obj.yScale*65536));
				}
				dos.writeInt(gObj.flags);
				dos.writeShort(gObj.angles);
				dos.writeShort(gObj.polygons);
				dos.writeByte(-1);
				dos.writeByte((byte) Math.round(gObj.color.getRed()*255));
				dos.writeByte((byte) Math.round(gObj.color.getGreen()*255));
				dos.writeByte((byte) Math.round(gObj.color.getBlue()*255));
				dos.writeByte(bitSize);
				dos.writeShort(xBase);
				dos.write(toHex(gObj.xList, bitSize, xBase));
				dos.writeShort(yBase);
				dos.write(toHex(gObj.yList, bitSize, yBase));
				dos.writeByte(idxBitSize);
				dos.write(toHex(gObj.indexBuffer, idxBitSize, (short)0));
				dos.writeShort(-1);
			}
		}
		dos.writeByte(127);
		dos.close();
		disOG.close();
		fos.close();
		fisOG.close();
		int totalLength = 15;
		for (GameObject obj: objects) {
			totalLength += obj.length + 3;
		}
		return totalLength;
	}
	
	public static int[] toIndex(byte[] byteList, byte n, short base) {
		String str = "";
		for (int i = byteList.length-1; i >= 0; i--) {
			String binText = String.format("%8s", Integer.toBinaryString(byteList[i])).replace(' ', '0');
			str += binText.substring(binText.length()-8);
		}
		String[] splitted = str.substring(str.length()%n).split("(?<=\\G.{"+n+"})");
		int len = splitted.length;
		int[] result = new int[len];
		for (int i = 0; i < len; i++) {
			result[len-i-1] = Integer.parseInt(splitted[i], 2) - 2*(Integer.parseInt(splitted[i], 2) & (int)Math.pow(2, n-1)) + base;
		}
		return result;
	}
	
	public static byte[] toHex(int[] intList, byte n, short base) {
		String str = "";
		for (int i = intList.length-1; i >= 0; i--) {
			String binText = String.format("%"+n+"s", Integer.toBinaryString(intList[i] - base)).replace(' ', '0');
			str += binText.substring(binText.length()-n);
		}
		String[] splitted = String.format("%"+(int)(8*Math.ceil(str.length()/8.0))+"s", str).replace(' ', '0').split("(?<=\\G.{8})");
		int len = splitted.length;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++) {
			result[len-i-1] = (byte) (Integer.parseInt(splitted[i], 2) - 2*(Integer.parseInt(splitted[i], 2) & 128));
		}
		return result;
	}
}
