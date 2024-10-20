package model;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.Node;

public class TrampolineObject extends GameObject {
	public short imageIDs;
	public byte push;
	
	public TrampolineObject(short id) {
		super(id, (byte) 12);
	}
	

	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		imageIDs = dis.readShort();
		push = dis.readByte();
		nbRead = length;
		return nbRead;
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		//short image = controller.selectedID.get() == id ? imageIDs : controller.animated.get(imageIDs).get(0);
		short image = controller.animated.get(imageIDs).get(0);
		Node sprite = controller.getImageById(image);
		sprite.setLayoutX(controller.transX(xAbs));
		sprite.setLayoutY(controller.transY(yAbs));
		return Arrays.asList(new Node[] { sprite });
	}
}
