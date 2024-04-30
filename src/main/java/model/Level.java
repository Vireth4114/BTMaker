package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
			GameObject obj = new GameObject(id, type);
			if (type == 4) {
				obj = new GeometryObject(id);
			} else if (type == 8) {
				obj = new BounceObject(id);
			}
			obj.read(dis);
			objects.add(obj);
			obj.skip(dis);
			id++;
		}
		xMin = Integer.MAX_VALUE;
		xMax = Integer.MIN_VALUE;
		yMin = Integer.MAX_VALUE;
		yMax = Integer.MIN_VALUE;
		for (GameObject obj: objects) {
			obj.doAbs(objects);
			if (obj instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) obj;
				for (int x: gObj.trueX) {
					if (x < xMin) xMin = x;
					if (x > xMax) xMax = x;
				}
				for (int y: gObj.trueY) {
					if (y < yMin) yMin = y;
					if (y > yMax) yMax = y;
				}
			}
		}
		dis.close();
		fis.close();
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
			obj.write(dos, disOG);
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
}
