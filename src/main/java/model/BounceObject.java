package model;

import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class BounceObject extends GameObject {
	public BounceObject(short id) {
		super(id, (byte) 8);
	}

	@Override
	public List<Shape> getShapes(Controller controller) {
		double x = (xAbs - controller.level.xMin + controller.xOffset) * controller.size;
		double y = (controller.level.yMax - yAbs + controller.yOffset) * controller.size;
		Circle bounce = new Circle(x, y, 20 * controller.size);
		bounce.setFill(Color.RED);
		return Arrays.asList(new Shape[] { bounce });
	}
}
