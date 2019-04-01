package packages;

public class Circle extends Graph{
	private int[] param;
	public Circle(int x,int y,int radius){
		setType(Circle.TYPE_CIRCLE);
		param=new int[3];
		param[0]=x;
		param[1]=y;
		param[2]=radius;
	}
	@Override
	public int[] getParam() {
		// TODO 自动生成的方法存根
		return this.param;
	}

}
