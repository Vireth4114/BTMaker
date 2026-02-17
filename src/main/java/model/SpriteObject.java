package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class SpriteObject extends GameObject {

	public int count;
	public int[] xList;
	public int[] yList;
	public int[] imageIDs;
	public int[] trueX;
	public int[] trueY;
	public List<Group> sprites = new ArrayList<Group>();
	
	public SpriteObject(short id) {
		super(id, (byte)9);
	}
	
	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		count = dis.readUnsignedByte();
		xList = new int[count];
		yList = new int[count];
		imageIDs = new int[count];
		nbRead = (short) (length);
		if (count == 0) return nbRead;
		byte bitSize = dis.readByte();
		int fullSize = (int)Math.ceil(bitSize*count/8.0);
		short baseX = dis.readShort();
		short baseY = dis.readShort();
		if (bitSize > 0) {
			xList = GeometryObject.toIndex(dis.readNBytes(fullSize), bitSize, baseX);
			yList = GeometryObject.toIndex(dis.readNBytes(fullSize), bitSize, baseY);
		} else {
			for (int i = 0; i < count; i++) {
				this.xList[i] = (short) baseX;
				this.yList[i] = (short) baseY;
			}
		}
		fullSize = 2*count;
		imageIDs = GeometryObject.toIndex(dis.readNBytes(fullSize), (byte)16, (short)0);
		return nbRead;
	}
	
	public void write(DataOutputStream dos, DataInputStream disOG) throws IOException {
		disOG.skip(length+3);
		int xMin = Integer.MAX_VALUE;
		int xMax = Integer.MIN_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;
		for (int x: xList) {
			xMin = Math.min(xMin, x);
			xMax = Math.max(xMax, x);
		}
		for (int y: yList) {
			yMin = Math.min(yMin, y);
			yMax = Math.max(yMax, y);
		}
		short xBase = (short) Math.ceil((xMax + xMin)/2);
		short yBase = (short) Math.ceil((yMax + yMin)/2);
		byte bitSize = (byte) Math.max(Math.ceil(Math.log(xMax - xBase + 1)/Math.log(2)) + 1,
					                   Math.ceil(Math.log(yMax - yBase + 1)/Math.log(2)) + 1);
		
		
		length = (short) (19 + 2 * (int)Math.ceil(bitSize*count/8.0) + 2*count);

		if (transformFlags == 3) {
			length += 4;
		} else if (transformFlags == 5) {
			length += 8;
		}
		
		preWrite(dos);
		dos.writeByte(count);
		dos.writeByte(bitSize);
		dos.writeShort(xBase);
		dos.writeShort(yBase);
		dos.write(GeometryObject.toHex(xList, bitSize, xBase));
		dos.write(GeometryObject.toHex(yList, bitSize, yBase));
		dos.write(GeometryObject.toHex(imageIDs, (byte)16, (short)0));
	}
	
	@Override
	public short[] doAbs(List<GameObject> objects) {
		short[] abs = super.doAbs(objects);
		trueX = new int[count];
		trueY = new int[count];
		for (int i = 0; i < count; i++) {
			trueX[i] = xList[i] + xAbs;
			trueY[i] = yList[i] + yAbs;
		}
		return abs;
	}

	@Override
	public void onClick(Controller controller) {
		super.onClick(controller);
		if (controller.subSelected.get() != -1) {
			controller.hBox.getChildren().add(new Label(String.valueOf(imageIDs[controller.subSelected.get()])));
		}
	}
	
	@Override
	public List<Node> getShapes(Controller controller) {
		sprites.clear();
		ArrayList<Node> shapes = new ArrayList<Node>();
		if (imageIDs.length == 0 || imageIDs[0] == 358) return shapes;
		for (int i = 0; i < count; i++) {
			Group sprite = controller.getImageById((short) imageIDs[i]);
			sprite.setLayoutX(controller.levelXtoViewX(trueX[i]));
			sprite.setLayoutY(controller.levelYtoViewY(trueY[i]));
			shapes.add(sprite);
			sprites.add(sprite);
		}
		return shapes;
	}

	@Override
	public String getExport() {
		StringBuilder export = new StringBuilder(super.getExport());
		for (int i = 0; i < imageIDs.length; i++) {
			export.append("\n\t");
			int imageId = imageIDs[i];
			export.append("imagePos: (").append(trueX[i]).append(", ").append(trueY[i]).append(")\n");
			ImageMap myImage = Controller.imageMap.get((short) imageId);
			if (myImage != null) {
				export.append(myImage.toString("\t\t"));
			} else {
				for (SubSprite subspr: Controller.compounds.get((short) imageId)) {
					export.append("\t\tsubPos: (").append(subspr.drawX).append(", ").append(subspr.drawY).append(")\n");
					export.append(Controller.imageMap.get((short) subspr.image).toString("\t\t\t")).append("\n");
				}
			}

		}
		return export.toString();
	}
}