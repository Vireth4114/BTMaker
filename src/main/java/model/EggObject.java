package model;

import java.util.Arrays;
import java.util.List;

import btmaker.Controller;
import btmaker.resources.ResourceManager;
import javafx.scene.Node;

public class EggObject extends GameObject {
	public EggObject(short id) {
		super(id, (byte) 13);
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		Node sprite = ResourceManager.INSTANCE.getSpriteById((short) 208);
		sprite.setLayoutX(controller.levelXtoViewX(xAbs));
		sprite.setLayoutY(controller.levelYtoViewY(yAbs));
		return Arrays.asList(new Node[] { sprite });
	}
}
