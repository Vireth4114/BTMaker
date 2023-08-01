package model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MovingCircle extends Circle {
	public int id;
	
	public MovingCircle(double x, double y, int id) {
		super(x, y, 6);
		setFill(Color.ORANGE);
		setStroke(Color.WHITE);
		this.id = id;
	}
}
