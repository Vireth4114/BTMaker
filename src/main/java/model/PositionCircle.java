package model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PositionCircle extends Circle {
	public GameObject object;

	public PositionCircle(double x, double y, GameObject object) {
		super(x, y, 6);
		setFill(Color.BLUE);
		setStroke(Color.WHITE);
		this.object = object;
	}
}
