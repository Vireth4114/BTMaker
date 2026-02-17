package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import BTMaker.BTMaker.PolygonTriangulation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

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
		int[] bigXList = toIndex(dis.readNBytes(fullSize), bitSize, base);
		xList = new int[angles];
		System.arraycopy(bigXList, 0, xList, 0, angles);
		base = dis.readShort();
		int[] bigYList = toIndex(dis.readNBytes(fullSize), bitSize, base);
		yList = new int[angles];
		System.arraycopy(bigYList, 0, yList, 0, angles);
		bitSize = dis.readByte();
		fullSize = (int)Math.ceil(bitSize*polygons/8.0);
		base = 0;
		int[] bigIndexBuffer = toIndex(dis.readNBytes(fullSize), bitSize, base);
		indexBuffer = new int[polygons];
		System.arraycopy(bigIndexBuffer, 0, indexBuffer, 0, polygons);
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
		short xBase = (short) Math.ceil((double) (xMax + xMin) /2);
		short yBase = (short) Math.ceil((double) (yMax + yMin) /2);
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
		ArrayList<Node> shapes = new ArrayList<>();
		for (int i = 0; i < polygons; i += 3) {
			double listCorners[] = new double[6];
			for (int j = 0; j < 3; j++) {
				int x = trueX[indexBuffer[i + j]];
				int y = trueY[indexBuffer[i + j]];
				listCorners[j*2] = controller.levelXtoViewX(x);
				listCorners[j*2 + 1] = controller.levelYtoViewY(y);
			}
			Polygon p = new Polygon(listCorners);
			Color c = color;
			p.setFill(c);
			p.setStroke(controller.showTriangulation ? Color.WHITE : c);
			shapes.add(p);
		}
		Group g = new Group(shapes);
		g.setTranslateX((1 - xScale)*(controller.levelXtoViewX(xAbs) - g.getLayoutBounds().getCenterX()));
		g.setTranslateY((1 - yScale)*(controller.levelYtoViewY(yAbs) - g.getLayoutBounds().getCenterY()));
		return List.of(g);
	}

	@Override
	public List<Node> getOverlay(Controller controller) {
		ArrayList<Node> shapes = new ArrayList<>(super.getOverlay(controller));
		for (int i = 0; i < angles; i++) {
			double startX = controller.levelXtoViewX(trueX[i]);
			double startY = controller.levelYtoViewY(trueY[i]);
			double endX = controller.levelXtoViewX(trueX[(i + 1) % angles]);
			double endY = controller.levelYtoViewY(trueY[(i + 1) % angles]);
			Line l = new Line(startX, startY, endX, endY);
			l.setStrokeWidth(5);
			l.setStroke(Color.WHITE);
			shapes.add(l);
			double midX = (startX + endX) / 2.0;
			double midY = (startY + endY) / 2.0;
			double deltaX = startX - endX;
			double deltaY = startY - endY;
			double factor = 10 / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			double x2 = midX - deltaY * factor;
			double y2 = midY + deltaX * factor;
			Line l2 = new Line(midX, midY, x2, y2);
			Polygon arrowHead = new Polygon(
				1.5, 0.0,
				-2.5, -2.0,
				-2.5,  2.0
			);

			arrowHead.setLayoutX(x2);
			arrowHead.setLayoutY(y2);

			double angle = Math.toDegrees(Math.atan2(y2 - midY, x2 - midX));
			arrowHead.setRotate(angle);

			for (Shape arrow : Arrays.asList(l2, arrowHead)) {
				arrow.setStrokeWidth(2);
				arrow.setStroke(Color.BLACK);
				arrow.setFill(Color.BLACK);
				shapes.add(arrow);
			}
			if (i != 0) {
				MovingCircle target = new MovingCircle(startX, startY, i, this);
				shapes.add(target);
			}
		}
		double startX = controller.levelXtoViewX(trueX[0]);
		double startY = controller.levelYtoViewY(trueY[0]);
		MovingCircle target = new MovingCircle(startX, startY, 0, this);
		shapes.add(target);
		Group g = new Group(shapes);
		g.setTranslateX((1 - xScale)*(controller.levelXtoViewX(xAbs) - g.getLayoutBounds().getCenterX()));
		g.setTranslateY((1 - yScale)*(controller.levelYtoViewY(yAbs) - g.getLayoutBounds().getCenterY()));
		return Arrays.asList(g);
	}
	
	@Override
	public void onClick(Controller controller) {
		super.onClick(controller);
		ColorPicker button = new ColorPicker(color);
		button.setPrefHeight(30);
		button.setPrefWidth(150);
		button.setOnAction(evt -> {color = button.getValue(); controller.draw();});
		button.getStyleClass().add("button");
		controller.hBox.getChildren().add(button);
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

	@Override
	public String getExport() {
		StringBuilder str = new StringBuilder(super.getExport());
		str.append("\n\tcorners: ");
		for (int i = 0; i < trueX.length; i++) {
			str.append("\n\t\t");
			str.append("(");
			str.append(trueX[i]);
			str.append(", ");
			str.append(trueY[i]);
			str.append(")");
		}
		str.append("\n\tcolor: ").append(color);
		return str.toString();
	}

	public void createParams() {
		nbRead = 0;
		angles = 4;
		polygons = 6;
		color = Color.BLACK;
		xList = new int[] {-50, 50, 50, -50};
		yList = new int[] {50, 50, -50, -50};
		indexBuffer = new int[] {1, 0, 2, 3, 2, 0};
	}

	public void addVertex(int prevVertex, int x, int y) {
		int[] newXList = new int[angles + 1];
		int[] newYList = new int[angles + 1];
		System.arraycopy(xList, 0, newXList, 0, prevVertex + 1);
		System.arraycopy(yList, 0, newYList, 0, prevVertex + 1);
		newXList[prevVertex + 1] = x;
		newYList[prevVertex + 1] = y;
		System.arraycopy(xList, prevVertex + 1, newXList, prevVertex + 2, angles - prevVertex - 1);
		System.arraycopy(yList, prevVertex + 1, newYList, prevVertex + 2, angles - prevVertex - 1);
		xList = newXList;
		yList = newYList;
		angles++;

		int[] newIndexBuffer = new int[indexBuffer.length + 3];
		System.arraycopy(indexBuffer, 0, newIndexBuffer, 0, indexBuffer.length);
		newIndexBuffer[newIndexBuffer.length - 3] = prevVertex;
		newIndexBuffer[newIndexBuffer.length - 2] = (prevVertex + 1) % angles;
		newIndexBuffer[newIndexBuffer.length - 1] = (prevVertex + 2) % angles;
		indexBuffer = newIndexBuffer;
		polygons += 3;
		this.doAbs(Controller.level.objects);
		triangulate();
	}

	public void removeVertex(int vertex) {
		if (angles <= 3) return;
		int[] newXList = new int[angles - 1];
		int[] newYList = new int[angles - 1];
		System.arraycopy(xList, 0, newXList, 0, vertex);
		System.arraycopy(yList, 0, newYList, 0, vertex);
		System.arraycopy(xList, vertex + 1, newXList, vertex, angles - vertex - 1);
		System.arraycopy(yList, vertex + 1, newYList, vertex, angles - vertex - 1);
		xList = newXList;
		yList = newYList;
		angles--;
		triangulate();
		this.doAbs(Controller.level.objects);
	}

	public void triangulate() {
		indexBuffer = PolygonTriangulation.triangulate(xList, yList);
		polygons = (short) indexBuffer.length;
	}
}
