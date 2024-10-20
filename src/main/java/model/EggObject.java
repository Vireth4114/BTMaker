package model;

import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.Node;

public class EggObject extends GameObject {
	public EggObject(short id) {
		super(id, (byte) 13);
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		Node sprite = controller.getImageById((short) 208);
		sprite.setLayoutX(controller.transX(xAbs));
		sprite.setLayoutY(controller.transY(yAbs));
		return Arrays.asList(new Node[] { sprite });
	}
}
