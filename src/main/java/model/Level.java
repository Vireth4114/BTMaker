package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Level {
	public static byte nextEvent;
	public byte countEvent;
	public short bounceObject;
	public ArrayList<GameObject> objects;
	public int xMin;
	public int xMax;
	public int yMin;
	public int yMax;
	public HashMap<Short, Short> deletedObjects = new HashMap<Short, Short>();

	public Level(String path) throws IOException {
		this.objects = new ArrayList<GameObject>();
		readObjects(path);
	}

	public void readObjects(String path) throws IOException {
		nextEvent = 0;
		System.out.println(path);
		FileInputStream fis = new FileInputStream(path);
		DataInputStream dis = new DataInputStream(fis);
		dis.skip(14);
		short id = 0;
		byte type;
		while ((type = dis.readByte()) != 127) {
			GameObject obj = new GameObject(id, type);
			switch (type) {
				case 4:  obj = new GeometryObject(id);   break;
				case 6:  obj = new EventObject(id);      break;
				case 8:  obj = new BounceObject(id);     bounceObject = id; break;
				case 9:  obj = new SpriteObject(id);     break;
				case 10: obj = new WaterObject(id);      break;
				case 11: obj = new CannonObject(id);     break;
				case 12: obj = new TrampolineObject(id); break;
				case 13: obj = new EggObject(id);        break;
				case 15: obj = new EnemyObject(id);      break;
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
		for (GameObject obj : objects) {
			obj.doAbs(objects);
			if (obj instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) obj;
				for (int x : gObj.trueX) {
					if (x < xMin)
						xMin = x;
					if (x > xMax)
						xMax = x;
				}
				for (int y : gObj.trueY) {
					if (y < yMin)
						yMin = y;
					if (y > yMax)
						yMax = y;
				}
			}
		}
		countEvent = nextEvent;
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
		dos.write(disOG.readNBytes(2));
		disOG.skip(2);
		dos.writeShort(objects.stream().filter(e -> e.type == 6).collect(Collectors.toList()).size());
		short idTester = 0;
		short deletedCount = 0;
		for (GameObject obj : objects) {
			if (obj.initialID != idTester++) {
				System.out.println(idTester);
				disOG.skip(3 + deletedObjects.get((short) (idTester++ - ++deletedCount)));
			}
			obj.write(dos, disOG);
		}
		dos.writeByte(127);
		dos.close();
		disOG.close();
		fos.close();
		fisOG.close();
		int totalLength = 15;
		for (GameObject obj : objects) {
			totalLength += obj.length + 3;
			obj.initialID = obj.id;
		}
		return totalLength;
	}
}
