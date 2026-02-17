package BTMaker.BTMaker;

import java.util.HashMap;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;

public class SpriteAnimation extends AnimationTimer {
	public HashMap<Short, Group> sprites = new HashMap<Short, Group>();
	public HashMap<Group, Short> objSprites = new HashMap<Group, Short>();
	public Controller controller;
	
	SpriteAnimation(Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public void handle(long now) {
		for (short id: sprites.keySet()) {
			Group sprite = sprites.get(id);
			if (!sprite.getChildren().isEmpty())
				controller.unregisterShape(sprite.getChildren().get(0));
			sprite.getChildren().clear();
			List<Short> images = Controller.animated.get(id);
			Double beat = Controller.animationSpeed.get(id);
			if (beat == null) beat = 17.0;
			sprite.getChildren().add(controller.getImageById(images.get((int) (now/(beat*1_000_000)) % images.size())));
			controller.registerShape(sprite.getChildren().get(0), objSprites.get(sprite));
		}
	}
}
