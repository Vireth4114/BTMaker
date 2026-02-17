package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import BTMaker.BTMaker.Controller;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import javax.swing.text.Position;

public class GameObject {
	public short length;
	public short id;
	public short initialID;
	public byte type;
	public short parentID;
	public short previousID;
	public byte transformFlags;
	public double rotation;
	public double xScale;
	public double yScale;
	public short xPos;
	public short yPos;
	public short xAbs;
	public short yAbs;
	public boolean absSet = false;
	public int flags;
	public int zcoord;
	public int noDraw;
	public short nbRead;
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" [id=" + id + ", type=" + type + ", parentID=" + parentID +
				", previousID=" + previousID + ", length=" + length + ", transformFlags=" + transformFlags +
				", xPos=" + xPos + ", yPos=" + yPos + ", xAbs=" + xAbs +
				", yAbs=" + yAbs + ", flags=" + flags + ", zcoord=" +
				zcoord + ", noDraw=" + noDraw + "]";
	}
	
	public GameObject(short id, byte type) {
		this.id = id;
		if (Controller.level != null) {			
			this.initialID = (short) (Controller.level.deletedObjects.size() + id);
		} else {
			this.initialID = id;
		}
		this.type = type;
	}
	
	public short read(DataInputStream dis) throws IOException {
		length = dis.readShort();
		parentID = dis.readShort();
		previousID = dis.readShort();
		transformFlags = dis.readByte();
		nbRead = 13;
		if ((transformFlags & 7) == 7) {
			//This flag is only used by the first cannon in Ch11 for no reason
			//There may never be any use of it due to the impracticality of scaling
			dis.skip(8); 
			xPos = (short)(dis.readInt() >> 16);
			dis.skip(8);
			yPos = (short)(dis.readInt() >> 16);
			rotation = 0.0;
			xScale = -1.0;
			yScale = 1.0;
			nbRead += 20;
		} else {
			if ((transformFlags & 1) > 0) {
				xPos = dis.readShort();
				yPos = dis.readShort();
				rotation = 0.0;
				xScale = 1.0;
				yScale = 1.0;
			}
			if ((transformFlags & 2) > 0) {
				nbRead += 4;
				rotation = Math.round(Math.toDegrees(dis.readInt()/65536.0) * 100.0) / 100.0;
			}
			if ((transformFlags & 4) > 0) {
				nbRead += 8;
				xScale = dis.readInt()/65536.0;
				yScale = dis.readInt()/65536.0;
			}
		}
		flags = dis.readInt();
		zcoord = flags & 0x1f;
		noDraw = flags & 0x80;
		return nbRead;
	}
	
	public void skip(DataInputStream dis) throws IOException {
		dis.skip(length-nbRead);
	}
	
	public int preWrite(DataOutputStream dos) throws IOException {
		dos.writeByte(type);
		if (transformFlags == 7) {
			length -= 12;
		}
		int offset = 0;
		dos.writeShort(length);
		dos.writeShort(parentID);
		dos.writeShort(previousID);
		if (transformFlags == 7) {
			dos.writeByte(5);
			offset = 12;
		} else {
			dos.writeByte(transformFlags);
		}
		dos.writeShort(xPos);
		dos.writeShort(yPos);
		if (transformFlags == 3) {
			dos.writeInt((int) Math.round(Math.toRadians(rotation)*65536));
			offset += 4;
		} else if (transformFlags == 5 || transformFlags == 7) {
			dos.writeInt((int) Math.round(xScale*65536));
			dos.writeInt((int) Math.round(yScale*65536));
			offset += 8;
		}
		dos.writeInt(flags);
		return offset;
	}
	
	public void write(DataOutputStream dos, DataInputStream disOG) throws IOException {
		int offset = preWrite(dos);
		disOG.skip(16+offset);
		dos.write(disOG.readNBytes(transformFlags != 7 ? length-13-offset : 3));
		if (transformFlags == 7) {
			transformFlags = 5;	
		}
	}
	
	public short[] doAbs(List<GameObject> objects) {
		if (!absSet) {
			absSet = true;
			if (id == 0 || parentID == 0) {
				xAbs = xPos;
				yAbs = yPos;
			} else {
				short[] parentXY = objects.get(parentID).doAbs(objects);
				xAbs = (short) (parentXY[0] + xPos);
				yAbs = (short) (parentXY[1] + yPos);
			}
		}
		return new short[]{xAbs, yAbs};
	}

	public List<Node> getShapes(Controller controller) {
		return new ArrayList<>();
	}

	public List<Node> getOverlay(Controller controller) {
		ArrayList<Node> shapes = new ArrayList<>();
		if (controller.ctrlHeld) {
			double x = controller.levelXtoViewX(xAbs);
			double y = controller.levelYtoViewY(yAbs);
			shapes.add(new PositionCircle(x, y, this));
		}
		return shapes;
	}
	
	public void onClick(Controller controller) {
		VBox vBox = new VBox();
		vBox.setId("scaleBox");

		for (String s: new String[] {"X", "Y"}) {
			HBox hBox = new HBox();
			hBox.setAlignment(Pos.CENTER);
			hBox.setSpacing(5);
			hBox.getChildren().add(new Label(s+": "));
			TextField field = new TextField();
            try {
                field.setText(getClass().getField(s.toLowerCase()+"Scale").get(this).toString());
			} catch (IllegalAccessException | NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
			field.textProperty().addListener((obs, prevV, newV) -> {
				try {getClass().getField(s.toLowerCase()+"Scale").set(this, Double.parseDouble(newV));}
				catch (NumberFormatException e) {field.setText(prevV); controller.draw();}
				catch (IllegalAccessException | NoSuchFieldException e) {throw new RuntimeException(e);}
			});
			field.setMinWidth(70);
			field.getStyleClass().add("left-field");
			hBox.getChildren().add(field);
			vBox.getChildren().add(hBox);
		}
		controller.hBox.getChildren().add(new TitledPane("Scale", vBox));
	}

	public String getExport() {
		String str = getClass().getSimpleName();
		str += "\n\tid: "+id;
		str += "\n\tpos: ("+xPos+", "+yPos+")";
		return str;
	}

	public void createParams() {}
}
