package packages;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class StudentClient extends JFrame{
	private BufferedReader bufferedReader=null;
	private PrintStream pStream=null;
	
	private Socket socket=null;
	private MyCanvas canvas=null;
	private JTextArea jTextArea=null;
	private JTextField jTextField=null;
	private JButton sendButton=null;
	
	private ArrayList<Graph> graphs=null;
	
	public StudentClient() {
		graphs=new ArrayList<Graph>();
		
		canvas=new MyCanvas();
		jTextArea=new JTextArea();
		jTextArea.setPreferredSize(new Dimension(900,100));
		jTextArea.setEditable(false);
		jTextField=new JTextField(20);
		sendButton=new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO 自动生成的方法存根
				String msg=jTextField.getText();
				if (!msg.equals("")){
					jTextArea.append(msg+"\n");
					msg=Command.STU_MSG+"&"+msg;
					pStream.println(msg);
					pStream.flush();
					jTextField.setText("");
				}
			}
		});
		this.setTitle("Student");
		this.setLayout(new FlowLayout());
		this.add(canvas);
		this.add(jTextArea);
		this.add(jTextField);
		this.add(sendButton);
		this.setSize(1000,1020);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		new Thread(new MsgHandleThread()).start();
	}
	public static void main(String[] args) {
		// TODO 自动生成的方法存根
		StudentClient studentClient=new StudentClient();
	}
	private class MyCanvas extends JPanel{
		Graphics graphics;
		public MyCanvas() {
			// TODO 自动生成的构造函数存根
			this.setBackground(Color.WHITE);
			this.setPreferredSize(new Dimension(1000,800));
			this.setVisible(true);
		}
		
		@Override
		public void paint(Graphics arg0) {
			// TODO 自动生成的方法存根
			super.paint(arg0);
			for (Graph graph:graphs){
				paintGraph(graph);
			}
		}

		public void paintGraph(Graph graph){
			Graphics graphics=getGraphics();
			int type=graph.getType();
			int[] param=graph.getParam();
			
			if (type==Graph.TYPE_LINE){
				graphics.setColor(Color.BLACK);
				graphics.drawLine(param[0], param[1], param[2], param[3]);
			}else{
				graphics.setColor(Color.WHITE);
				graphics.drawOval(param[0]-param[2]/2,param[1]-param[2]/2,param[2],param[2]);
				graphics.fillOval(param[0]-param[2]/2,param[1]-param[2]/2,param[2],param[2]);
			}
		}
	}
	
	private class MsgHandleThread implements Runnable{
		private String msgString;
		private String[] msgs=null;
		
		public void run() {
			// TODO 自动生成的方法存根
			while (socket==null){
				try {
					socket=new Socket("localhost",9096);
					bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
					pStream=new PrintStream(socket.getOutputStream());
					pStream.println("null");
					pStream.flush();
					Thread.sleep(1000);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			while (!Thread.currentThread().isInterrupted()){
				try {
					msgString=bufferedReader.readLine();
					msgs=msgString.split("&");
					int type=Integer.parseInt(msgs[0]);
					switch (type) {
					case 2:
						int x1=Integer.parseInt(msgs[1]);
						int y1=Integer.parseInt(msgs[2]);
						int x2=Integer.parseInt(msgs[3]);
						int y2=Integer.parseInt(msgs[4]);
						Line line=new Line(x1, y1, x2, y2);
						graphs.add(line);
						canvas.paintGraph(line);
						break;
					case 3:
						int x=Integer.parseInt(msgs[1]);
						int y=Integer.parseInt(msgs[2]);
						int clearSize=Integer.parseInt(msgs[3]);
						Circle circle=new Circle(x, y, clearSize);
						graphs.add(circle);
						canvas.paintGraph(circle);
						break;
					case 1:
						jTextArea.append("Teacher:"+msgs[1]+"\n");
						break;
					case 5:
						graphs.clear();
						canvas.repaint();
						break;
					default:
						break;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
	}

}
