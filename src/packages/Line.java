package packages;

public class Line extends Graph{
	private int[] param;
	public Line(int x1,int y1,int x2,int y2){
		param=new int[4];
		setType(Line.TYPE_LINE);
		this.param[0]=x1;
		this.param[1]=y1;
		this.param[2]=x2;
		this.param[3]=y2;
	}
	@Override
	public int[] getParam() {
		// TODO 自动生成的方法存根
		return this.param;
	}
	
}