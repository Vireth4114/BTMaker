package model;

import java.util.List;

public class GameObject {
	public short length;
	public short id;
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
	
	@Override
	public String toString() {
		return "GameObject [id=" + id + ", type=" + type + ", parentID=" + parentID + 
				", xPos=" + xPos + ", yPos=" + yPos + ", xAbs=" + xAbs +
				", yAbs=" + yAbs + ", flags=" + flags + ", zcoord=" +
				zcoord + ", noDraw=" + noDraw + "]";
	}
	
	public short[] doAbs(List<GameObject> objects) {
		if (!absSet) {
			absSet = true;
			if (id == 0 || parentID == 0) {
				xAbs = xPos;
				yAbs = yPos;
			} else {
				for (GameObject obj: objects) {
					if (obj.id == parentID) {
						short[] parentXY = obj.doAbs(objects);
						xAbs = (short) (parentXY[0] + xPos);
						yAbs = (short) (parentXY[1] + yPos);
					}
				}
			}
		}
		return new short[]{xAbs, yAbs};
	}
}
