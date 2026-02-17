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
		Line pathLine = new Line(controller.levelXtoViewX(xAbs + path.getStartX()),
							   controller.levelYtoViewY(yAbs + path.getStartY()),
							   controller.levelXtoViewX(xAbs + path.getEndX()),
							   controller.levelYtoViewY(yAbs + path.getEndY()));
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
		sprite.setLayoutX(controller.levelXtoViewX(xAbs));
		sprite.setLayoutY(controller.levelYtoViewY(yAbs));
		if (controller.selectedID.get() != id) return Arrays.asList(sprite);
		return Arrays.asList(sprite, pathLine, m1, m2);
	}

	@Override
	public String getExport() {
		String str = super.getExport();
		str += "\n\t" + (enemy == 0 ? "Spinner" : "Mole");
		str += "\n\tpath: ";
		str += "\n\t\t("+(xAbs + path.getStartX())+", "+(yAbs + path.getStartY())+")";
		str += "\n\t\t("+(xAbs + path.getEndX())+", "+(yAbs + path.getEndY())+")";
		return str;
	}
}
