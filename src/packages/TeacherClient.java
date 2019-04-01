package packages;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TeacherClient extends JFrame{
	private List<UserThread> usersList=null;
	private ConcurrentLinkedQueue<String> msgList=null;
	private ArrayList<Graph> graList=null;
	
	private ServerSocket serverSocket=null;
	private Socket socket;
	private int PORT=9096;
	
	private JButton drawButton=null;
	private JButton clearButton=null;
	private JSlider clearSizeChooser=null;
	private int mode=0;
	private final int MODE_WRITE=0;
	private final int MODE_CLEAR=1;
	private MyCanvas myCanvas=null;
	private int clearSize=50;
	private JTextArea jTextArea=null;
	private JTextField inputField=null;
	private JButton sendButton=null;
	private JLabel chooserJLabel=null;
	private JScrollPane jScrollPane=null;
	private JButton clearAllButton=null;
	
	public TeacherClient() {
		new Thread(new ServerThread()).start();
		msgList=new ConcurrentLinkedQueue<String>();
		usersList=new ArrayList<UserThread>();
		graList=new ArrayList<Graph>();
		inputField=new JTextField(30);
		clearAllButton=new JButton("ClearAll");
		clearAllButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO 自动生成的方法存根
				graList.clear();
				myCanvas.setBackground(Color.WHITE);
				myCanvas.repaint();
				String msgString=Command.CLEAR_ALL+"&";
				msgList.add(msgString);
			}
		});
		sendButton=new JButton("Answer");
		sendButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO 自动生成的方法存根
				String msg=inputField.getText();
				if (!msg.equals("")){
					jTextArea.append("Teacher:"+msg+"\n");
					inputField.setText("");
					msg=Command.TEACHER_MSG+"&"+msg;
					msgList.add(msg);
				}
			}
		});
		jTextArea=new JTextArea();
		jScrollPane=new JScrollPane(jTextArea);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jTextArea.setEditable(false);
		jTextArea.setPreferredSize(new Dimension(900,100));
		clearSizeChooser=new JSlider(JSlider.HORIZONTAL,10,300,50);
		clearSizeChooser.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent event) {
				// TODO 自动生成的方法存根
				clearSize=clearSizeChooser.getValue();
				chooserJLabel.setText(String.valueOf(clearSizeChooser.getValue()));
			}
		});
		chooserJLabel=new JLabel();
		chooserJLabel.setText(String.valueOf(clearSizeChooser.getValue()));
		myCanvas=new MyCanvas();
		myCanvas.setPreferredSize(new Dimension(1000,800));
		drawButton=new JButton("Write");
		drawButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO 自动生成的方法存根
				mode=MODE_WRITE;
			}
		});
		clearButton=new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO 自动生成的方法存根
				mode=MODE_CLEAR;
			}
		});
		this.setTitle("Teacher");
		this.setLayout(new FlowLayout());
		this.add(myCanvas);
		this.add(drawButton);
		this.add(clearButton);
		this.add(clearAllButton);
		this.add(clearSizeChooser);
		this.add(chooserJLabel);
		this.add(jScrollPane);
		this.add(inputField);
		this.add(sendButton);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(1000,1020);
		this.setResizable(false);
		this.setVisible(true);
		
		new Thread(new MsgProcessThread()).start();
	}
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// TODO 自动生成的方法存根
		TeacherClient client=new TeacherClient();
	}
	private class MsgProcessThread implements Runnable{

		public void run() {
			// TODO 自动生成的方法存根
			System.out.println("Msg Handler Thread Started...");
			while (!Thread.currentThread().isInterrupted()){
				try {
					while (!msgList.isEmpty()){
						String msg=msgList.remove();
						for (UserThread user:usersList){
							PrintStream p=user.getPrintStream();
							p.println(msg);
							p.flush();
							//jTextArea.append(msg+"\n");
						}
					}
					Thread.sleep(50);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private class ServerThread implements Runnable{
		private BufferedReader bufferedReader=null;
		public void run() {
			// TODO 自动生成的方法存根
			try {
				serverSocket=new ServerSocket(PORT);
				System.out.println("Server Thread Started...");
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
				while (!Thread.currentThread().isInterrupted()){
					try {
						socket=serverSocket.accept();
						bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
						System.out.println("one student connected...");
						String nameString=bufferedReader.readLine();
						UserThread userThread=new UserThread(socket,nameString);
						usersList.add(userThread);
						new Thread(userThread).start();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
		}
		
	}
	private class UserThread implements Runnable{
		private String name;
		private Socket userSocket;
		private BufferedReader bufferedReader=null;
		private String msg=null;
		private PrintStream ps=null;
		
		public UserThread(Socket socket,String name){
			this.userSocket=socket;
			this.name=name;
			try {
				bufferedReader=new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
				ps=new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		public PrintStream getPrintStream(){
			return this.ps;
		}
		public void run() {
			// TODO 自动生成的方法存根
			System.out.println("User Thread Started...");
			while (!Thread.currentThread().isInterrupted()){
				try {
					msg=bufferedReader.readLine();
					if (!msg.equals("")){
						String[] msgs=msg.split("&");
						if (msgs[0].equals(Command.STU_MSG)){
							jTextArea.append(msgs[1]+"\n");
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
	}
	
	private class MyCanvas extends JPanel implements MouseMotionListener,MouseListener{
		private int lastX;
		private int lastY;
		private boolean firstIn=true;
		
		public MyCanvas() {
			// TODO 自动生成的构造函数存根
			setBackground(Color.WHITE);
			addMouseMotionListener(this);
			addMouseListener(this);
		}
		private void paintGrap(Graph graph){
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
		@Override
		public void paint(Graphics arg0) {
			// TODO 自动生成的方法存根
			super.paint(arg0);
			for (Graph graph:graList){
				paintGrap(graph);
			}
		}
		public void mouseDragged(MouseEvent e) {
			// TODO 自动生成的方法存根
			Graphics g=getGraphics();
			if (mode==0){
				if (firstIn){
					lastX=e.getX();
					lastY=e.getY();
					firstIn=false;
				}else{
					g.setColor(Color.BLACK);
					int x=e.getX();
					int y=e.getY();
					//g.drawLine(lastX, lastY, x, y);
					Line line=new Line(lastX, lastY, x, y);
					graList.add(line);
					paintGrap(line);
					String msg=Command.DRAW_LINE+"&"+lastX+"&"+lastY+"&"+x+"&"+y;
					msgList.add(msg);
					lastX=x;
					lastY=y;
				}
			}else{
				int x=e.getX();
				int y=e.getY();
				g.setColor(Color.WHITE);
				//g.drawOval(x-clearSize/2,y-clearSize/2,clearSize,clearSize);
				//g.fillOval(x-clearSize/2,y-clearSize/2,clearSize,clearSize);
				Circle circle=new Circle(x, y, clearSize);
				graList.add(circle);
				paintGrap(circle);
				String msg=Command.CLEAR+"&"+x+"&"+y+"&"+clearSize;
				msgList.add(msg);
			}
		}

		public void mouseMoved(MouseEvent e) {
			// TODO 自动生成的方法存根
		}

		public void mouseClicked(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			
		}

		public void mouseEntered(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			
		}

		public void mouseExited(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			
		}

		public void mousePressed(MouseEvent e) {
			// TODO 自动生成的方法存根
		}

		public void mouseReleased(MouseEvent e) {
			// TODO 自动生成的方法存根
			firstIn=true;
		}
	}
}
