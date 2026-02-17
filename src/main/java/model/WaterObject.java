package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.converter.NumberStringConverter;

public class WaterObject extends RectangleObject {
	public byte gravityTop;
	public byte gravityRight;
	public byte gravityBottom;
	public byte gravityLeft;
	public boolean isWater;
	
	public WaterObject(short id) {
		super(id, (byte) 10);
	}

	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		gravityTop = dis.readByte();
		gravityRight = dis.readByte();
		gravityBottom = dis.readByte();
		gravityLeft = dis.readByte();
		dis.skip(1);
		isWater = (dis.readByte() | dis.readByte() | dis.readByte()) != 0;
		nbRead = length;
		return nbRead;
	}

	public void write(DataOutputStream dos, DataInputStream disOG) throws IOException {
		disOG.skip(length+3);	
		preWrite(dos);
		dos.writeShort(minX);
		dos.writeShort(maxY);
		dos.writeShort(maxX);
		dos.writeShort(minY);
		dos.writeByte(gravityTop);
		dos.writeByte(gravityRight);
		dos.writeByte(gravityBottom);
		dos.writeByte(gravityLeft);
		if (isWater) dos.writeInt(0x441111EE);
		else 		 dos.writeInt(0x44000000);
	}
	
	

	@Override
	public void onClick(Controller controller) {
		super.onClick(controller);
		GridPane grid = new GridPane();
		TextField topField = new TextField();
		topField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
		topField.setText(String.valueOf(gravityTop));
		topField.textProperty().addListener((obs, prevV, newV) -> {
			try {gravityTop = Byte.parseByte(newV);}
			catch (NumberFormatException e)	{topField.setText(prevV);}
		});
		grid.add(topField, 1, 0);
		TextField rightField = new TextField();
		rightField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
		rightField.setText(String.valueOf(gravityRight));
		rightField.textProperty().addListener((obs, prevV, newV) -> {
			try {gravityRight = Byte.parseByte(newV);}
			catch (NumberFormatException e)	{rightField.setText(prevV);}
		});
		grid.add(rightField, 2, 1);
		TextField bottomField = new TextField();
		bottomField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
		bottomField.setText(String.valueOf(gravityBottom));
		bottomField.textProperty().addListener((obs, prevV, newV) -> {
			try {gravityBottom = Byte.parseByte(newV);}
			catch (NumberFormatException e)	{bottomField.setText(prevV);}
		});
		grid.add(bottomField, 1, 2);
		TextField leftField = new TextField();
		leftField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
		leftField.setText(String.valueOf(gravityLeft));
		leftField.textProperty().addListener((obs, prevV, newV) -> {
			try {gravityLeft = Byte.parseByte(newV);}
			catch (NumberFormatException e)	{leftField.setText(prevV);}
		});
		grid.add(leftField, 0, 1);
		controller.hBox.getChildren().add(grid);
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		double x1 = controller.levelXtoViewX(xAbs + minX);
		double x2 = controller.levelXtoViewX(xAbs + maxX);
		double y1 = controller.levelYtoViewY(yAbs + minY);
		double y2 = controller.levelYtoViewY(yAbs + maxY);
		Rectangle r = new Rectangle(x1, y1, x2-x1, y2-y1);
		if (isWater) r.setFill(Color.rgb(17, 17, 238, 68.0/255.0));
		else r.setFill(new Color(1, 0, 0, 68.0/255.0));
		return Arrays.asList(r);
	}

	@Override
	public String getExport() {
		return super.getExport() +
				"\n\tgravityTop: "+gravityTop +
				"\n\tgravityRight: "+gravityRight +
				"\n\tgravityBottom: "+gravityBottom +
				"\n\tgravityLeft: "+gravityLeft +
				"\n\tisWater: "+isWater;
	}
}
