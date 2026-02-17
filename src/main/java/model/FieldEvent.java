package model;

import BTMaker.BTMaker.Controller;
import javafx.scene.control.TextField;

public class FieldEvent extends TextField {
	public FieldEvent(byte value) {
		super();
		setText(String.valueOf(value));
		textProperty().addListener((obs, prevV, newV) -> {
			if (newV == "") return;
			try {
				byte v = Byte.parseByte(newV);
				if (0 > v || v >= Controller.level.countEvent)
					setText(prevV);
			} catch (NumberFormatException e) {
				setText(prevV);
			}
		});
	}
}