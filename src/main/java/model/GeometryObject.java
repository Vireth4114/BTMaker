package model;

import java.util.Arrays;

import javafx.scene.paint.Color;

public class GeometryObject extends GameObject {
	public short angles;
	public short polygons;
	public Color color;
	public int[] xList;
	public int[] yList;
	public int[] trueX;
	public int[] trueY;
	public int[] indexBuffer;
	
	@Override
	public String toString() {
		return "GeometryObject [angles=" + angles + ", polygons=" + polygons + ", color=" + color + ", xList="
				+ Arrays.toString(xList) + ", yList=" + Arrays.toString(yList) + ", indexBuffer="
				+ Arrays.toString(indexBuffer) + ", toString()=" + super.toString() + "]";
	}
	
}
