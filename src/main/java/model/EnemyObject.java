package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class EnemyObject extends GameObject {
	public Line path;
	public byte enemy;
	
	public EnemyObject(short id) {
		super(id, (byte) 15);
	}
	

	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		path = new Line();
		path.setStartX(dis.readShort());
		path.setStartY(dis.readShort());
		path.setEndX(dis.readShort());
		path.setEndY(dis.readShort());
		enemy = dis.readByte();
		zcoord = -1;
		nbRead = length;
		return nbRead;
	}
	
	public void write(DataOutputStream dos, DataInputStream disOG) throws IOException {
		disOG.skip(length+3);		
		preWrite(dos);
		dos.writeShort((short)path.getStartX());
		dos.writeShort((short)path.getStartY());
		dos.writeShort((short)path.getEndX());
		dos.writeShort((short)path.getEndY());
		dos.writeByte(enemy);
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		Line pathLine = new Line(controller.transX(xAbs + path.getStartX()),
							   controller.transY(yAbs + path.getStartY()),
							   controller.transX(xAbs + path.getEndX()),
							   controller.transY(yAbs + path.getEndY()));
		pathLine.setStroke(Color.BLUE);
		pathLine.setStrokeWidth(5);
		MovingCircle m1 = new MovingCircle(pathLine.getStartX(), pathLine.getStartY(), 0, this);
		MovingCircle m2 = new MovingCircle(pathLine.getEndX(), pathLine.getEndY(), 1, this);
		short image = 0;
		switch (enemy) {
			case 0: image = 467; break;
			case 2: image = 504; break;
		}
		Node sprite = controller.getImageById(image, id);
		sprite.setLayoutX(controller.transX(xAbs));
		sprite.setLayoutY(controller.transY(yAbs));
		if (controller.selectedID.get() != id) return Arrays.asList(sprite);
		return Arrays.asList(sprite, pathLine, m1, m2);
	}
}
