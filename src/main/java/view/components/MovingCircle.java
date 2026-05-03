package view.components;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.GameObject;

public class MovingCircle extends Circle {
	public int id;
	public GameObject object;
	
	public MovingCircle(double x, double y, int id, GameObject object) {
		super(x, y, 6);
		setFill(Color.ORANGE);
		setStroke(Color.WHITE);
		this.id = id;
		this.object = object;
	}
}
