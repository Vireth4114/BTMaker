package model;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import BTMaker.BTMaker.Controller;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public enum EventCommand {
	MESSAGE {
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Textbox (s "+dis.readShort()+")");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			dos.writeShort(-1);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	OBJ_ANIMATE{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Start animation of {"+dis.readShort()+"}");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	EVENT_TERMINATE{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Destroy event ["+dis.readByte()+"]");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	EVENT_CANCEL{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Enable event ["+dis.readByte()+"]");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	EVENT_START{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Start event ["+dis.readByte()+"]");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	EVENT_PAUSE{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Pause event ["+dis.readByte()+"]");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	WAIT{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Wait for (s "+dis.readShort()+")ms");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			short time = Short.parseShort(((TextField)hBox.getChildren().get(1)).getText());
			dos.writeShort(time);
			dos.writeShort(time);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	VAR_SET{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			int newVal = dis.readInt();
			if (varNum == 0) {
				if (newVal == 1) return toBox("Kill Bounce");
				if (newVal == 2) return toBox("Finish Level");
			}
			if (varNum == 1) {
				if (newVal == 3) return toBox("Froze Gameplay");
				if (newVal == 0) return toBox("Restore Gameplay");
			}
			return toBox("Set variable (b "+varNum+") to (i "+newVal+")");
		}
		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			String text = (((Label)hBox.getChildren().get(0)).getText());
			byte varNum;
			int newVal;
			switch (text) {
				case "Kill Bounce":	     varNum = 0; newVal = 1; break;
				case "Finish Level":     varNum = 0; newVal = 2; break;
				case "Froze Gameplay":   varNum = 1; newVal = 3; break;
				case "Restore Gameplay": varNum = 1; newVal = 0; break;
				default:
					varNum = Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText());
					newVal = Integer.parseInt(((TextField)hBox.getChildren().get(3)).getText());
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(newVal);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	VAR_ADD{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			return toBox("Add (i "+dis.readInt()+") to variable (s "+varNum+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
			int opVal = Integer.parseInt(((TextField)hBox.getChildren().get(1)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(opVal);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	VAR_SUB{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			return toBox("Subtract (i "+dis.readInt()+") to variable (s "+varNum+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
			int opVal = Integer.parseInt(((TextField)hBox.getChildren().get(1)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(opVal);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	VAR_MUL{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			return toBox("Multiply (i "+dis.readInt()+") to variable (s "+varNum+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
			int opVal = Integer.parseInt(((TextField)hBox.getChildren().get(1)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(opVal);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	VAR_DIV{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			return toBox("Divide variable (s "+varNum+") by (i "+dis.readInt()+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText());
			int opVal = Integer.parseInt(((TextField)hBox.getChildren().get(3)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(opVal);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	BRANCH_IF_NE{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			int cmp = dis.readInt();
			return toBox("Go to step (b "+dis.readByte()+") if variable (s "+varNum+") ≠ (i "+cmp+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
			int cmpVal = Integer.parseInt(((TextField)hBox.getChildren().get(5)).getText());
			byte step = Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(cmpVal);
			dos.writeByte(step);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	BRANCH_IF_EQ{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			int cmp = dis.readInt();
			return toBox("Go to step (b "+dis.readByte()+") if variable (s "+varNum+") = (i "+cmp+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
			int cmpVal = Integer.parseInt(((TextField)hBox.getChildren().get(5)).getText());
			byte step = Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(cmpVal);
			dos.writeByte(step);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	BRANCH_IF_GEQ{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			int cmp = dis.readInt();
			return toBox("Go to step (b "+dis.readByte()+") if variable (s "+varNum+") >= (i "+cmp+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
			int cmpVal = Integer.parseInt(((TextField)hBox.getChildren().get(5)).getText());
			byte step = Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(cmpVal);
			dos.writeByte(step);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	BRANCH_IF_LEQ{
		public HBox parse(DataInputStream dis) throws IOException {
			dis.skip(1);
			byte varNum = dis.readByte();
			dis.skip(1);
			int cmp = dis.readInt();
			return toBox("Go to step (b "+dis.readByte()+") if variable (s "+varNum+") <= (i "+cmp+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			byte varNum = Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
			int cmpVal = Integer.parseInt(((TextField)hBox.getChildren().get(5)).getText());
			byte step = Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(2);
			dos.writeByte(varNum);
			dos.writeByte(32);
			dos.writeInt(cmpVal);
			dos.writeByte(step);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	OBJ_MOVE{
		public HBox parse(DataInputStream dis) throws IOException {
			short obj = dis.readShort();
			int x = dis.readInt();
			int y = dis.readInt();
			int duration = dis.readInt();
			return toBox("Move object {"+obj+"} by x=(i "+(int)Math.round((x*duration)/65536.0)+") y=(i "+(int)Math.round((y*duration)/65536.0)+") in (i "+duration+")ms");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			short object = Short.parseShort(((TextField)hBox.getChildren().get(1)).getText());
			int duration = Integer.parseInt(((TextField)hBox.getChildren().get(7)).getText());
			int x = Integer.parseInt(((TextField)hBox.getChildren().get(3)).getText());
			x = (x*65536)/duration;
			int y = Integer.parseInt(((TextField)hBox.getChildren().get(5)).getText());
			y = (y*65536)/duration;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(object);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(duration);
			dos.writeInt(duration);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	OBJ_ROTATE{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Rotate object {"+dis.readShort()+"} by (f "+Math.round(100 * Math.toDegrees(dis.readInt()/65536.0)) / 100.0+")° in (i "+dis.readInt()+")ms");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			short object = Short.parseShort(((TextField)hBox.getChildren().get(1)).getText());
			int rotation = (int) (Math.toRadians(Double.parseDouble(((TextField)hBox.getChildren().get(3)).getText())) * 65536);
			int duration = Integer.parseInt(((TextField)hBox.getChildren().get(5)).getText());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(object);
			dos.writeInt(rotation);
			dos.writeInt(duration);
			dos.writeInt(duration);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	OBJ_SETPOS{
		public HBox parse(DataInputStream dis) throws IOException {
			short dest = dis.readShort();
			short src = dis.readShort();
			if (src < 0)
				return toBox("Set position of object {"+dest+"} to x=(s "+Math.round(dis.readInt()/65536.0)+") y=(s "+Math.round(dis.readInt()/65536.0)+")");
			return toBox("Set position of object {"+dest+"} from object {"+src+"}");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			if (hBox.getChildren().size() == 4) {
				dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(3)).getText()));
			} else {
				dos.writeShort(-1);
				dos.writeInt(Integer.parseInt(((TextField)hBox.getChildren().get(3)).getText()) * 65536);
				dos.writeInt(Integer.parseInt(((TextField)hBox.getChildren().get(5)).getText()) * 65536);
			}
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	OBJ_ATTACH{
		public HBox parse(DataInputStream dis) throws IOException {
			short dest = dis.readShort();
			short src = dis.readShort();
			return toBox("Attach object {"+dest+"} to object {"+src+"}");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(3)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	OBJ_DETACH{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Detach object {"+dis.readShort()+"}");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	BRANCH{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Go to step (b "+dis.readByte()+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	NOP{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Uhhh what");
		}
	},
	END{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("End event");
		}
	},
	WAIT_ACTOR_GONE{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Wait for object {"+dis.readShort()+"} to be gone");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	CHECKPOINT{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Checkpoint");
		}
	},
	PUSH{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Push Bounce by x=(s "+dis.readShort()+") y=(s "+dis.readShort()+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Controller.instance.level.bounceObject);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(3)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	GRAVITATE{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Change Bounce gravity by x=(s "+dis.readShort()+") y=(s "+dis.readShort()+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Controller.instance.level.bounceObject);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(3)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	ACCELERATE{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Accelerate Bounce by x=(s "+dis.readShort()+") y=(s "+dis.readShort()+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Controller.instance.level.bounceObject);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(3)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	OBJ_SET_FLAGS{
		public HBox parse(DataInputStream dis) throws IOException {
			short obj = dis.readShort();
			int flags = dis.readInt();
			int values = dis.readInt();
			String str = "";
			if ((flags & 1) != 0) {
				str = "Set object {"+obj+"} depth to (b "+(values & 0x1f)+")";
			}
			if ((flags & 0x20) != 0) {
				if ((values & 0x20) != 0) {
					if (str.length() != 0)
						str += "and disable its collisions";
					else
						str = "Disable collisions of object {"+obj+"}";
				}else {
					if (str.length() != 0)
						str += "and enable its collisions";
					else
						str = "Enable collisions of object {"+obj+"}";
				}
			}
			if ((flags & 0x80) != 0) {
				if ((values & 0x80) != 0) {
					if (str.length() != 0)
						str += "and make it invisible";
					else
						str += "Make object {"+obj+"} invisible";
				} else {
					if (str.length() != 0)
						str += "and make it visible";
					else
						str += "Make object {"+obj+"} visible";
				}
			}
			return toBox(str);
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			int flags = 0;
			int values = 0;
			for (Node node: hBox.getChildren()) {
				if (node instanceof Label) {
					Label l = (Label) node;
					if (l.getText().contains("Set")) {
						flags |= 1;
						values |= Byte.parseByte(((TextField)hBox.getChildren().get(3)).getText());
					}
					if (l.getText().contains("collisions")) {
						flags |= 0x20;
					}
					if (l.getText().contains("isable")) {
						values |= 0x20;
					}
					if (l.getText().contains("visible")) {
						flags |= 0x80;
					}
					if (l.getText().contains("invisible")) {
						values |= 0x80;
					}
				}
			}
			dos.writeInt(flags);
			dos.writeInt(values);
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	CAMERA_TARGET{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Focus camera on object {"+dis.readShort()+"}");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(1)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	CAMERA_SETPARAM{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Snap=(b "+dis.readByte()+") BounceFactor=(s "+dis.readShort()+") StabilizeSpeed=(s "+dis.readShort()+")");
		}

		public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(Byte.parseByte(((TextField)hBox.getChildren().get(1)).getText()));
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(3)).getText()));
			dos.writeShort(Short.parseShort(((TextField)hBox.getChildren().get(5)).getText()));
			return Arrays.asList(ArrayUtils.toObject(baos.toByteArray()));
		}
	},
	CAMERA_SETPARAM_DEFAULT{
		public HBox parse(DataInputStream dis) throws IOException {
			return toBox("Reset camera");
		}
	};

	public HBox parse(DataInputStream dis) throws IOException {return new HBox();}
	
	public HBox toBox(String str) {
		HBox hBox = new HBox();
		hBox.setSpacing(2);
		StringBuilder sb = new StringBuilder();
		boolean finished = false;
		int current = 0;
		for (char c: str.toCharArray()) {
			if (c == '(' || c == '[' || c == '{' || c == ')' || c == ']' || c == '}')
				finished = true;
			else
				sb.append(c);
			
			if (finished) {
				if (current == 0) {
					Label l = new Label(sb.toString());
					l.setMinWidth(Region.USE_PREF_SIZE);
					hBox.getChildren().add(l);
				} else if (current == 1) {
					String[] s = sb.toString().split(" ");
					TextField field = new TextField();
					field.setText(s[1]);
					switch (s[0]) {
						case "b":
							field.textProperty().addListener((obs, prevV, newV) -> {
								if (EventInstance.doChecks(hBox)) return;
								try {Byte.parseByte(newV);}
								catch (NumberFormatException e)	{field.setText(prevV);}
							});
							break;
						case "s":
							field.textProperty().addListener((obs, prevV, newV) -> {
								if (EventInstance.doChecks(hBox)) return;
								try {Short.parseShort(newV);}
								catch (NumberFormatException e)	{field.setText(prevV);}
							});
							break;
						case "i":
							field.textProperty().addListener((obs, prevV, newV) -> {
								if (EventInstance.doChecks(hBox)) return;
								try {Integer.parseInt(newV);}
								catch (NumberFormatException e)	{field.setText(prevV);}
							});
							break;
						case "f":
							field.textProperty().addListener((obs, prevV, newV) -> {
								if (EventInstance.doChecks(hBox)) return;
								try {Float.parseFloat(newV);}
								catch (NumberFormatException e)	{field.setText(prevV);}
							});
							break;
					}
					hBox.getChildren().add(field);
				} else if (current == 2) {
					hBox.getChildren().add(new FieldEvent(Byte.parseByte(sb.toString())));
				} else if (current == 3) {
					hBox.getChildren().add(new FieldObject(Short.parseShort(sb.toString())));
				}
				switch (c) {
					case '(': current = 1; break;
					case '[': current = 2; break;
					case '{': current = 3; break;
					default:  current = 0; break;
				}
				sb.setLength(0);
				finished = false;
			}
		}
		if (sb.length() != 0) {
			Label l = new Label(sb.toString());
			l.setMinWidth(Region.USE_PREF_SIZE);
			hBox.getChildren().add(l);
		}
		hBox.setAlignment(Pos.CENTER_LEFT);
		return hBox;
	}
	
	public List<Byte> toData(HBox hBox) throws NumberFormatException, IOException {
		return new ArrayList<Byte>();
	}
}
