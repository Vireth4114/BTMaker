package model;

import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BounceObject extends GameObject {
	public BounceObject(short id) {
		super(id, (byte) 8);
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		double x = controller.levelXtoViewX(xAbs);
		double y = controller.levelYtoViewY(yAbs);
		Circle bounce = new Circle(x, y, 20 * controller.size);
		bounce.setFill(Color.RED);
		return Arrays.asList(new Node[] { bounce });
	}
}
