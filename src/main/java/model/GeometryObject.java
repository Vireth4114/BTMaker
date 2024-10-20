package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class GeometryObject extends GameObject {

	public short angles;
	public short polygons;
	public Color color;
	public int[] xList;
	public int[] yList;
	public int[] trueX;
	public int[] trueY;
	public int[] indexBuffer;
	
	public GeometryObject(short id) {
		super(id, (byte)4);
	}
	
	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		angles = dis.readShort();
		polygons = dis.readShort();
		dis.skip(1);
		color = Color.rgb(dis.read(), dis.read(), dis.read());
		byte bitSize = dis.readByte();
		int fullSize = (int)Math.ceil(bitSize*angles/8.0);
		short base = dis.readShort();
		xList = toIndex(dis.readNBytes(fullSize), bitSize, base);
		base = dis.readShort();
		yList = toIndex(dis.readNBytes(fullSize), bitSize, base);
		bitSize = dis.readByte();
		fullSize = (int)Math.ceil(bitSize*polygons/8.0);
		base = 0;
		indexBuffer = toIndex(dis.readNBytes(fullSize), bitSize, base);
		nbRead = (short) (length - 2);
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
		int idxMax = -1;
		for (int idx: indexBuffer) {
			idxMax = Math.max(idxMax, idx);
		}
		byte idxBitSize = (byte) (Math.ceil(Math.log(idxMax + 1)/Math.log(2)) + 1);
		length = (short) (29 + 2 * (int) Math.ceil(bitSize*angles/8.0) + 
								   (int) Math.ceil(idxBitSize*polygons/8.0));

		if (transformFlags == 3) {
			length += 4;
		} else if (transformFlags == 5) {
			length += 8;
		}
		
		preWrite(dos);
		dos.writeShort(angles);
		dos.writeShort(polygons);
		dos.writeByte(-1);
		dos.writeByte((byte) Math.round(color.getRed()*255));
		dos.writeByte((byte) Math.round(color.getGreen()*255));
		dos.writeByte((byte) Math.round(color.getBlue()*255));
		dos.writeByte(bitSize);
		dos.writeShort(xBase);
		dos.write(toHex(xList, bitSize, xBase));
		dos.writeShort(yBase);
		dos.write(toHex(yList, bitSize, yBase));
		dos.writeByte(idxBitSize);
		dos.write(toHex(indexBuffer, idxBitSize, (short)0));
		dos.writeShort(-1);
	}
	
	@Override
	public String toString() {
		return super.toString() + "[angles=" + angles + ", polygons=" + polygons + ", color=" + color + ", xList="
				+ Arrays.toString(xList) + ", yList=" + Arrays.toString(yList) + ", indexBuffer="
				+ Arrays.toString(indexBuffer) + "]";
	}
	
	@Override
	public List<Node> getShapes(Controller controller) {
		ArrayList<Node> shapes = new ArrayList<Node>();
		for (int i = 0; i < polygons; i += 3) {
			double listCorners[] = new double[6];
			for (int j = 0; j < 3; j++) {
				int x = trueX[indexBuffer[i + j]];
				int y = trueY[indexBuffer[i + j]];
				listCorners[j*2] = controller.transX(x);
				listCorners[j*2 + 1] = controller.transY(y);
			}
			Polygon p = new Polygon(listCorners);
			Color c = color;
			p.setFill(c);
			p.setStroke(c);
			shapes.add(p);
		}
		Group g = new Group(shapes);
		g.setTranslateX((1 - xScale)* (controller.transX(xAbs) - g.getLayoutBounds().getCenterX()));
		g.setTranslateY((1 - yScale)* (controller.transY(yAbs) - g.getLayoutBounds().getCenterY()));
		return Arrays.asList(g);
	}
	
	@Override
	public void onClick(Controller controller) {
		controller.addTargets();
		ColorPicker button = new ColorPicker(color);
		button.setPrefHeight(30);
		button.setPrefWidth(150);
		button.setOnAction(evt -> {color = button.getValue(); controller.draw();});
		button.getStyleClass().add("button");
		controller.hBox.getChildren().add(button);
		System.out.println(xAbs+" "+yAbs+" "+(xAbs - trueX[0]));
	}
	
	@Override
	public short[] doAbs(List<GameObject> objects) {
		short[] abs = super.doAbs(objects);
		trueX = new int[angles];
		trueY = new int[angles];
		for (int i = 0; i < angles; i++) {
			trueX[i] = xList[i] + xAbs;
			trueY[i] = yList[i] + yAbs;
		}
		return abs;
	}
	
	public static int[] toIndex(byte[] byteList, byte n, short base) {
		if (byteList.length == 0) return new int[] {};
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
		if (intList.length == 0) return new byte[] {};
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
