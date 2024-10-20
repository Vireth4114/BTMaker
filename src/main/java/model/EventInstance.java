package model;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class EventInstance {
	public EventCommand command;
	public HBox hBox;
	
	public EventInstance(EventCommand command, DataInputStream data) {
		this.command = command;
		try {
			hBox = command.parse(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Byte> toData() {
		List<Byte> bytes = new ArrayList<Byte>();
		try {
			bytes = command.toData(hBox);
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
	public static List<EventInstance> getAll() {
		ArrayList<EventInstance> list = new ArrayList<EventInstance>();
		for (EventCommand event: EventCommand.values()) {
			List<byte[]> listBytes = new ArrayList<byte[]>();
			switch (event) {
				case NOP:
					continue;
				case OBJ_SET_FLAGS:
					listBytes.add(new byte[] {0, 0, 0, 0, 0, 32, 0, 0, 0, 32});
					listBytes.add(new byte[] {0, 0, 0, 0, 0, 32, 0, 0, 0, 0});
					listBytes.add(new byte[] {0, 0, 0, 0, 0, -128, 0, 0, 0, -128});
					listBytes.add(new byte[] {0, 0, 0, 0, 0, -128, 0, 0, 0, 0});
					listBytes.add(new byte[] {0, 0, 0, 0, 0, -96, 0, 0, 0, -96});
					listBytes.add(new byte[] {0, 0, 0, 0, 0, -96, 0, 0, 0, 0});
					break;
				case VAR_SET:
					listBytes.add(new byte[] {2, 0, 32, 0, 0, 0, 1});
					listBytes.add(new byte[] {2, 0, 32, 0, 0, 0, 2});
					listBytes.add(new byte[] {2, 1, 32, 0, 0, 0, 0});
					listBytes.add(new byte[] {2, 1, 32, 0, 0, 0, 3});
					break;
				case OBJ_SETPOS:
					listBytes.add(new byte[] {0, 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0});
					break;
				default:
					break;
			}
			if (event != EventCommand.OBJ_SET_FLAGS)
				listBytes.add(new byte[30]);
			for (byte[] bytes: listBytes) {
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
				EventInstance myEvent = new EventInstance(event, dis);
				for (Node child: myEvent.hBox.getChildren()) {
					if (child instanceof TextField) {
						((TextField)child).setText("");
						((TextField)child).setDisable(true);
					}
				}
				list.add(myEvent);
			}
		}
		return list;
	}
	
	public boolean doChecks() {
		return doChecks(hBox);
	}
	
	public static boolean doChecks(HBox hBox) {
		boolean returnValue = false;
		for (Node child: hBox.getChildren()) {
			if (child instanceof TextField) {
				String str = ((TextField)child).getText();
				returnValue |= str == "" || str == "-";
			}
		}
		return returnValue;
	}
}
