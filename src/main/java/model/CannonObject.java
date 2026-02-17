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
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.NumberStringConverter;

public class CannonObject extends GameObject {
	public short playerId;
	public byte power;
	
	public CannonObject(short id) {
		super(id, (byte) 11);
	}

	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		playerId = dis.readShort();
		power = dis.readByte();
		nbRead = length;
		return nbRead;
	}
	
	public void write(DataOutputStream dos, DataInputStream disOG) throws IOException {
		disOG.skip(length+3);	
		preWrite(dos);
		dos.writeShort(playerId);
		dos.writeByte(power);
	}	

	@Override
	public void onClick(Controller controller) {
		super.onClick(controller);
		TextField numberField = new TextField();
		numberField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
		numberField.setText(String.valueOf(power));
		numberField.textProperty().addListener((obs, prevV, newV) -> {
			try {power = Byte.parseByte(newV);}
			catch (NumberFormatException e)	{numberField.setText(prevV);}
		});
		controller.hBox.getChildren().add(numberField);
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		ArrayList<Node> shapes = new ArrayList<Node>();
		Node sprite = controller.getImageById((short) 48);
		sprite.setLayoutX(controller.levelXtoViewX(xAbs));
		sprite.setLayoutY(controller.levelYtoViewY(yAbs));
		for (GeometryObject gObj: controller.cannonShapes) {
			gObj.xAbs = xAbs;
			gObj.yAbs = yAbs;
			gObj.doAbs(controller.level.objects);
			shapes.addAll(gObj.getShapes(controller));
		}
		shapes.add(sprite);
		Group g = new Group(shapes);
		g.setTranslateX((1 - xScale)* (controller.levelXtoViewX(xAbs) - g.getLayoutBounds().getCenterX()));
		g.setTranslateY((1 - yScale)* (controller.levelYtoViewY(yAbs) - g.getLayoutBounds().getCenterY()));
		return Arrays.asList(g);
	}

	public void createParams() {
		playerId = Controller.level.bounceObject;
		power = 80;
		length = 16;
	}

	@Override
	public String getExport() {
		return super.getExport() + "\n\tpower: "+power;
	}
}
