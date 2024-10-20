package model;

import BTMaker.BTMaker.Controller;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class FieldObject extends TextField {
	public FieldObject(short value) {
		super();
		setText(String.valueOf(value));
		textProperty().addListener((obs, prevV, newV) -> {
			if (newV == "") return;
			try {
				short v = Short.parseShort(newV);
				if (0 > v || v >= Controller.instance.level.objects.size())
					setText(prevV);
			} catch (NumberFormatException e) {
				setText(prevV);
			}
		});
		Tooltip t = new Tooltip();
		Group graphic = new Group(Controller.instance.level.objects.get(value).getShapes(Controller.instance));
		graphic.setScaleX(5);
		graphic.setScaleY(5);
		t.setGraphic(graphic);
		setTooltip(t);
	}
}