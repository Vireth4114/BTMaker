package model;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import btmaker.Controller;
import btmaker.resources.ResourceManager;
import javafx.scene.Node;

public class TrampolineObject extends GameObject {
	public short imageIDs;
	public byte push;
	
	public TrampolineObject(short id) {
		super(id, (byte) 12);
	}
	

	public short read(DataInputStream dis) throws IOException {
		super.read(dis);
		imageIDs = dis.readShort();
		push = dis.readByte();
		nbRead = length;
		return nbRead;
	}

	@Override
	public List<Node> getShapes(Controller controller) {
		Node sprite = ResourceManager.INSTANCE.getSpriteById(imageIDs);
		sprite.setLayoutX(controller.levelXtoViewX(xAbs));
		sprite.setLayoutY(controller.levelYtoViewY(yAbs));
		return Arrays.asList(new Node[] { sprite });
	}

	@Override
	public String getExport() {
		return super.getExport() +
				"\n\tpush: " + push;
	}
}
