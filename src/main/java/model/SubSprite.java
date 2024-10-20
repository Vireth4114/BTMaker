package model;

import java.io.DataInputStream;
import java.util.function.Function;

public class SubSprite {
	public int drawX;
	public int drawY;
	public int image;
	
	public SubSprite(DataInputStream dis, Function<DataInputStream, Integer> func) {
		drawX = func.apply(dis);
		drawY = func.apply(dis);
		image = func.apply(dis);
	}
}
