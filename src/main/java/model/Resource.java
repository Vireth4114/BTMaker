package model;

public class Resource {
	public String name;
	public int offset;
	
	public Resource(String name, int offset) {
		this.name = name;
		this.offset = offset;
	}
	
	@Override
	public String toString() {
		return name+" "+offset;
	}
}