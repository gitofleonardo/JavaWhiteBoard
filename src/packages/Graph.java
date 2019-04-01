package packages;

import java.awt.Color;

public abstract class Graph {
	public static final int TYPE_LINE=0;
	public static final int TYPE_CIRCLE=1;
	private int type;
	
	public int getType() {
		return this.type;
	}
	public abstract int[] getParam();
	public  void setType(int type) {
		this.type=type;
	}
}
