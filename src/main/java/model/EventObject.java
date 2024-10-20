package model;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class EventObject extends RectangleObject {
	public byte state;
	public byte triggerLeave;
	public byte repeatable;
	public short triggerId;
	public byte eventId;
	public List<Byte> eventList = new ArrayList<Byte>();
	public List<ArrayList<Byte>> eventData = new ArrayList<ArrayList<Byte>>();
	public int holding = -1;
	
	public EventObject(short id) {
		super(id, (byte) 6);
	}

	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		short tmp = minY; 
		minY = maxY;
		maxY = tmp;
		state = dis.readByte();
		triggerLeave = dis.readByte();
		repeatable = dis.readByte();
		triggerId = dis.readShort();
		byte eventCount = dis.readByte();
		for (int i = 0; i < eventCount; i++) {
			byte dataCount = dis.readByte();
			ArrayList<Byte> bytes = new ArrayList<Byte>();
			eventList.add(dis.readByte());
			for (int j = 1; j < dataCount; j++)
				bytes.add(dis.readByte());
			eventData.add(bytes);
		}
		eventId = Level.nextEvent++;
		nbRead = length;
		return nbRead;
	}
	
	public void write(DataOutputStream dos, DataInputStream disOG) throws IOException {
		disOG.skip(length+3);
		length = (short) (27 + eventData.stream().map(e -> e.size()).mapToInt(Integer::intValue).sum() + 2*eventList.size());
		if (transformFlags == 3)      length += 4;
		else if (transformFlags == 5) length += 8;
		preWrite(dos);
		dos.writeShort(minX);
		dos.writeShort(minY);
		dos.writeShort(maxX);
		dos.writeShort(maxY);
		dos.writeByte(state);
		dos.writeByte(triggerLeave);
		dos.writeByte(repeatable);
		dos.writeShort(triggerId);
		dos.writeByte(eventList.size());
		for (int i = 0; i < eventList.size(); i++) {
			dos.writeByte(eventData.get(i).size() + 1);
			dos.writeByte(eventList.get(i));
			for (byte b: eventData.get(i))
				dos.writeByte(b);
		}
	}
	
	@Override
	public void onClick(Controller controller) {
		controller.hBox.getChildren().add(new Label(String.valueOf(eventId)));
		GridPane simpleGrid = new GridPane();
		simpleGrid.setAlignment(Pos.CENTER);
		simpleGrid.setHgap(10);
		simpleGrid.setVgap(20);
		simpleGrid.add(new Label("State: "), 0, 0);
		ChoiceBox<String> stateField = new ChoiceBox<String>();
		stateField.getItems().addAll("On touch", "Disabled", "On level start");
		stateField.setValue(stateField.getItems().get(state));
		stateField.valueProperty().addListener((obs, prevV, newV) -> state = (byte) stateField.getItems().indexOf(newV));
		simpleGrid.add(stateField, 1, 0);
		simpleGrid.add(new Label("Repeatable: "), 0, 1);
		CheckBox repeatableField = new CheckBox();
		repeatableField.setSelected(repeatable != 0);
		repeatableField.selectedProperty().addListener((obs, prevV, newV) -> repeatable = (byte) (newV ? 1 : 0));
		simpleGrid.add(repeatableField, 1, 1);
		ColumnConstraints constraint = new ColumnConstraints();
		constraint.setHalignment(HPos.RIGHT);
		simpleGrid.getColumnConstraints().add(constraint);
		controller.hBox.getChildren().add(simpleGrid);
		VBox listContainer = new VBox();
		listContainer.setSpacing(10);
		listContainer.setPadding(new Insets(10));
		ScrollPane scPane = new ScrollPane();
		scPane.setFitToWidth(true);
		scPane.setContent(listContainer);
		controller.hBox.getChildren().add(scPane);
		for (int i = 0; i < eventList.size(); i++) {
			int i2 = i;
			int val = eventList.get(i);
			HBox row = new HBox();
			row.setAlignment(Pos.CENTER_LEFT);
			row.setSpacing(10);
			row.setOnMousePressed(e -> holding = listContainer.getChildren().indexOf(row));
			row.setOnMouseReleased(e -> {
				ArrayList<Node> nodes = new ArrayList<Node>(listContainer.getChildren());
				for (int c = 0; c < nodes.size(); c++) {
					Node child = nodes.get(c);
					if (!(child instanceof HBox)) continue;
					if (child.localToScene(child.getBoundsInLocal()).contains(e.getSceneX(), e.getSceneY())) {
						listContainer.getChildren().add(c, listContainer.getChildren().remove(holding));
						eventData.add(c, eventData.remove(holding));
						eventList.add(c, eventList.remove(holding));
					}
				}
				for (int v = 0; v < listContainer.getChildren().size(); v++) {
					if (!(listContainer.getChildren().get(v) instanceof HBox)) continue;
					((HBox)listContainer.getChildren().get(v)).getChildren().set(0, new Label(String.valueOf(v+1)));
				}
			});
			Label l = new Label(String.valueOf(i+1));
			l.setMinWidth(20);
			row.getChildren().add(l);
			byte[] bytes = new byte[eventData.get(i).size()];
			for (int j = 0; j < eventData.get(i).size(); j++) bytes[j] = eventData.get(i).get(j);
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
			EventInstance event = new EventInstance(EventCommand.values()[val], dis);
			HBox currEvent = event.hBox;
			currEvent.setMinWidth(400);
			for (Node child: event.hBox.getChildren()) {
				if (!(child instanceof TextField)) continue;
				TextField field = (TextField) child;
				field.textProperty().addListener((obs, prevV, newV) -> {
					if (event.doChecks()) return;
					eventData.get(i2).clear();
					eventData.get(i2).addAll(event.toData());
				});
			}
			row.getChildren().add(currEvent);
			Button button = new Button("Change Event...");
			button.setMinWidth(Region.USE_PREF_SIZE);
			button.setOnMouseClicked(e -> {
				Stage stage = new Stage();
				GridPane eventGrid = new GridPane();
				eventGrid.setVgap(10);
				eventGrid.setHgap(10);
				eventGrid.setPadding(new Insets(10));
				eventGrid.setStyle("-fx-background-color: #303030;");
				List<EventInstance> events = EventInstance.getAll();
				for (int j = 0; j < events.size(); j++) {
					EventInstance eventLinked = events.get(j);
					int commandId = Arrays.asList(EventCommand.values()).indexOf(eventLinked.command);
					HBox eventToShow = eventLinked.hBox;
					eventToShow.setOnMouseClicked(mouseEvent -> {
						for (Node child: eventToShow.getChildren()) {
							if (child instanceof TextField) {
								TextField field = (TextField) child;
								field.setDisable(false);
								field.textProperty().addListener((obs, prevV, newV) -> {
									if (eventLinked.doChecks()) return;
									eventList.set(i2, (byte) commandId);
									eventData.get(i2).clear();
									eventData.get(i2).addAll(eventLinked.toData());
								});
							}
						}
						if (eventToShow.getChildren().size() == 1) {
							eventList.set(i2, (byte) commandId);
							eventData.get(i2).clear();
							eventData.get(i2).addAll(eventLinked.toData());
						}
						eventToShow.setMinWidth(400);
						row.getChildren().set(1, eventToShow);
						stage.close();
					});
					eventGrid.add(eventToShow, j%3, j/3);
				}
				Scene scene = new Scene(eventGrid);
				scene.getStylesheets().add("/test.css");
				stage.setScene(scene);
				stage.show();
			});
			row.getChildren().add(button);
			Button deleteButton = new Button("🗙");
			deleteButton.setMinWidth(Region.USE_PREF_SIZE);
			deleteButton.setStyle("-fx-background-color: red; -fx-background-radius: 1000;");
			deleteButton.setOnMouseClicked(e -> {
				int indexToBeSure = listContainer.getChildren().indexOf(row);
				listContainer.getChildren().remove(row);
				eventList.remove(indexToBeSure);
				eventData.remove(indexToBeSure);
				for (int v = 0; v < listContainer.getChildren().size(); v++) {
					if (!(listContainer.getChildren().get(0) instanceof HBox)) continue;
					((HBox)listContainer.getChildren().get(0)).getChildren().set(0, new Label(String.valueOf(v+1)));
				}
			});
			row.getChildren().add(deleteButton);
			listContainer.getChildren().add(row);
		}
		Button addButton = new Button("Add Step");
		addButton.setOnMouseClicked(mouseEvent -> {
			eventList.add((byte) 0);
			eventData.add(new ArrayList<Byte>(Arrays.asList((byte) 0, (byte) 1, (byte) -1, (byte) -1)));
			controller.hBox.getChildren().clear();
			onClick(controller);
		});
		listContainer.getChildren().add(addButton);
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		double x1 = controller.transX(xAbs + minX);
		double x2 = controller.transX(xAbs + maxX);
		double y1 = controller.transY(yAbs + minY);
		double y2 = controller.transY(yAbs + maxY);
		Rectangle r = new Rectangle(x1, y1, x2-x1, y2-y1);
		r.setFill(Color.TRANSPARENT);
		r.setStroke(Color.BLUE);
		r.setStrokeWidth(3);
		return Arrays.asList(r);
	}

	@Override
	public String toString() {
		return "EventObject [state=" + state + ", triggerLeave=" + triggerLeave + ", repeatable=" + repeatable
				+ ", triggerId=" + triggerId + ", eventList=" + eventList
				+ ", eventData=" + eventData + "] "+super.toString();
	}
}
