import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MainWindow {

	private JFrame frame;
	private JPanel terminal, CodingArea, RobotArea, CodingAreaOfBlocks;
	private JTextArea terminalText;
	private JTextArea CodingAreaWithCode;
	private HashMap<String, Integer> VariableMap = new HashMap<String, Integer>();
	private ArrayList<String> movement = new ArrayList<String>();
	private HashSet<String> Math = new HashSet<String>();
	private static int robotX, robotY, direction; // direction 0 up, 1 right, 2
													// down, 3 left

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		// initialize the code HashSet
		movement.add("Forward");
		movement.add("Backward");
		movement.add("TurnLeft");
		movement.add("TurnRight");
		Math.add("+");
		Math.add("-");
		Math.add("*");
		Math.add("/");
		robotX = 0;
		robotY = 0;
		direction = 0;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Project Wall");
		frame.setBounds(100, 100, 900, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container pane = frame.getContentPane();

		RobotArea = new RobotPanel();
		RobotArea.setPreferredSize(new Dimension(500, 600));

		pane.add(RobotArea, BorderLayout.CENTER);

		terminal = new JPanel();
		terminal.setBounds(101, 650, 900, 50);
		terminal.setPreferredSize(new Dimension(500, 100));
		terminalText = new JTextArea();
		terminalText.setText("Everything is set and ready to go!");
		terminal.add(terminalText);
		pane.add(terminal, BorderLayout.SOUTH);

		CodingArea = new JPanel();
		CodingArea.setBounds(0, 0, 100, 1000);
		CodingArea.setBackground(Color.BLUE);
		CodingArea.setPreferredSize(new Dimension(350, 300));

		CodingArea.setLayout(new BoxLayout(CodingArea, BoxLayout.Y_AXIS));
		CodingAreaOfBlocks = new JPanel();
		CodingAreaOfBlocks.setBounds(0, 650, 100, 500);
		CodingAreaOfBlocks.setBackground(Color.CYAN);
		// coding area with code
		CodingAreaWithCode = new JTextArea(20, 23);
		CodingAreaOfBlocks.add(CodingAreaWithCode);

		JTextArea CodingAreaInstruction = new JTextArea(5, 23);
		String CodingInstruction = "Movement: Forward, Backward, TurnLeft, TurnRight \n";
		CodingInstruction += "loop: FOR 2x .. END   WHILE LOGIC... DO ... END  IF LOGIC... DO ... END\n";
		CodingInstruction += "Variable: SET A = 5, Set A = A + 5 \n";
		CodingInstruction += "Math: +-*/";
		CodingInstruction += "Binary Logic: > < =";

		CodingAreaInstruction.setText(CodingInstruction);
		CodingAreaInstruction.setEditable(false);
		CodingAreaOfBlocks.add(CodingAreaInstruction);

		CodingArea.add(CodingAreaOfBlocks);
		// run button
		JButton runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				String inputCode = CodingAreaWithCode.getText();
				String inputCode = "";
				File codeFile = new File("rfid.txt");
				File codeBlockFile = new File("codeBlock.txt");
				BufferedReader br = null, br2 = null;
				try {
					br = new BufferedReader(new FileReader(codeFile));
					br2 = new BufferedReader(new FileReader(codeBlockFile));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				
				// connect rfid code to actual for while code block
				HashMap<String, String> codeBlock = new HashMap<String, String>();

				String st;
				try {
					while ((st = br2.readLine()) != null){
						codeBlock.put(st.split(" ")[0], st.split(" ")[1]);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				String st2;
				try {
					while ((st2 = br.readLine()) != null){
						String convertedCode = codeBlock.get(st2);
						if (convertedCode.equals("WHILE")){
							convertedCode += " Destination";
						}
						convertedCode += "\n";
						inputCode += convertedCode;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println(inputCode);
				runCode(inputCode);
				frame.repaint();
				System.out.println("DONE");
				restoreState();
			}
		});
		CodingArea.add(runButton);

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				terminalText.setText("");
				VariableMap.clear();
				robotX = 0;
				robotY = 0;
				direction = 0;
				CodingAreaWithCode.setText("");
				frame.repaint();
			}
		});

		CodingArea.add(resetButton);
		pane.add(CodingArea, BorderLayout.EAST);

		int gameLevel = 1;
		switch (gameLevel) {
		case 1:
			levelOneGame();
			break;
		case 2:
			levelTwoGame();
			break;
		case 3:
			levelThreeGame();
			break;
		}

	}

	private void runCode(String inputCode) {
		String[] codeLinesTemp = inputCode.split("\n");
		ArrayList<String> codeLines = new ArrayList<String>(Arrays.asList(codeLinesTemp));
		// parse code
		while (codeLines.size() > 0) {
			String code = codeLines.remove(0);
			if (movement.contains(code)) {
				moveRobot(code);
				continue;
			}
			if (code.split(" ")[0].equals("FOR")) {
				String ForCode = "";
				String temp = codeLines.remove(0);
				while (!temp.equals("END")) {
					ForCode += temp + "\n";
					temp = codeLines.remove(0);
				}
				int loopNum = Integer.parseInt(code.split(" ")[1].split("x")[0]);
				for (int i = 0; i < loopNum; i++) {
					runCode(ForCode);
				}
				continue;
			}
			if (code.split(" ")[0].equals("SET")) {
				VariableMap.put(code.substring(4, code.length()).split("=")[0].replace(" ", ""),
						parseValue(code.split("=")[1].replace(" ", "")));
				continue;
			}
			if (code.split(" ")[0].equals("IF")) {
				String logic = code.substring(3, code.length()).replace(" ", "");
				String IfCode = "";
				String temp = codeLines.remove(0);
				while (!temp.equals("END")) {
					IfCode += temp + "\n";
					temp = codeLines.remove(0);
				}
				if (parseLogic(logic))
					runCode(IfCode);
				continue;
			}
			if (code.split(" ")[0].equals("WHILE")) {
				String logic = code.substring(6, code.indexOf("DO")).replace(" ", "");
				String WhileCode = "";
				String temp = codeLines.remove(0);
				while (!temp.equals("END")) {
					WhileCode += temp + "\n";
					temp = codeLines.remove(0);
				}
				while (parseLogic(logic))
					runCode(WhileCode);
				continue;
			}
		}
	}

	private boolean parseLogic(String s) {
		if (s.contains("Destination")) {
			s.split("Destination");
			return robotX != 3 && robotY != 3;
		} else if (s.contains(">")) {
			String[] temp = s.split(">");
			return parseValue(temp[0]) > parseValue(temp[1]);
		} else if (s.contains("<")) {
			String[] temp = s.split("<");
			return parseValue(temp[0]) < parseValue(temp[1]);
		} else if (s.contains("=")) {
			String[] temp = s.split("=");
			return parseValue(temp[0]) == parseValue(temp[1]);
		} else
			return false;
	}

	private Integer parseValue(String s) {
		int left, right;
		if (s.contains("+")) {
			String[] temp = s.split("\\+");
			if (VariableMap.containsKey(temp[0])) {
				left = VariableMap.get(temp[0]);
			} else {
				left = Integer.parseInt(temp[0]);
			}
			if (VariableMap.containsKey(temp[1])) {
				right = VariableMap.get(temp[1]);
			} else {
				right = Integer.parseInt(temp[1]);
			}
			return left + right;
		} else if (s.contains("-")) {
			String[] temp = s.split("\\-");
			if (VariableMap.containsKey(temp[0])) {
				left = VariableMap.get(temp[0]);
			} else {
				left = Integer.parseInt(temp[0]);
			}
			if (VariableMap.containsKey(temp[1])) {
				right = VariableMap.get(temp[1]);
			} else {
				right = Integer.parseInt(temp[1]);
			}
			return left - right;
		} else if (s.contains("*")) {
			String[] temp = s.split("\\*");
			if (VariableMap.containsKey(temp[0])) {
				left = VariableMap.get(temp[0]);
			} else {
				left = Integer.parseInt(temp[0]);
			}
			if (VariableMap.containsKey(temp[1])) {
				right = VariableMap.get(temp[1]);
			} else {
				right = Integer.parseInt(temp[1]);
			}
			return left * right;
		} else if (s.contains("/")) {
			String[] temp = s.split("\\/");
			if (VariableMap.containsKey(temp[0])) {
				left = VariableMap.get(temp[0]);
			} else {
				left = Integer.parseInt(temp[0]);
			}
			if (VariableMap.containsKey(temp[1])) {
				right = VariableMap.get(temp[1]);
			} else {
				right = Integer.parseInt(temp[1]);
			}
			return left / right;
		}
		// only one symbol or value
		if (VariableMap.containsKey(s)) {
			return VariableMap.get(s);
		} else {
			return Integer.parseInt(s);
		}
	}

	private void moveRobot(String code) {

		System.out.println(code);

		switch (code) {
		case "Forward":
			if (direction == 0) {
				robotY += 1;
			} else if (direction == 1) {
				robotX += 1;
			} else if (direction == 2) {
				robotY -= 1;
			} else if (direction == 3) {
				robotX -= 1;
			}
			break;
		case "Backward":
			if (direction == 0) {
				robotY -= 1;
			} else if (direction == 1) {
				robotX -= 1;
			} else if (direction == 2) {
				robotY += 1;
			} else if (direction == 3) {
				robotX += 1;
			}
			break;
		case "TurnLeft":
			direction = (direction + 1) % 4;
			break;
		case "TurnRight":
			direction = (direction + 3) % 4;
			break;
		}
//		frame.repaint();

		if (robotX < 0 || robotX > 5 || robotY < 0 || robotY > 5) {
			System.err.println("robot out of bound");
			terminalText.setText("robot out of bound");
		}
	}

	private void restoreState() {
		// robotX = 0;
		// robotY = 0;
		// direction = 0;
		if (robotX == 3 && robotY == 3) {
			JOptionPane.showMessageDialog(frame, "Destination arrived.");
		}
		VariableMap.clear();
//		System.out.println("State Restored");
	}

	/**
	 * level one game World of four blocks need to add three go right in coding
	 * panel
	 */
	private void levelOneGame() {
		RobotArea.setLayout(new GridLayout(1, 3));
		// JPanel block = new JPanel();
		// terminal.setBounds(0, 0, 1000, 1000);
		// block.setBackground(Color.RED);
		// RobotArea.add(block);
	}

	private void levelThreeGame() {
		// TODO Auto-generated method stub

	}

	private void levelTwoGame() {
		// TODO Auto-generated method stub

	}

	private static class RobotPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Image board, robot0, robot1, robot2, robot3, target;

		public RobotPanel() {
			board = Toolkit.getDefaultToolkit().getImage("board.png");
			robot0 = Toolkit.getDefaultToolkit().getImage("robot0.png");
			robot1 = Toolkit.getDefaultToolkit().getImage("robot1.png");
			robot2 = Toolkit.getDefaultToolkit().getImage("robot2.png");
			robot3 = Toolkit.getDefaultToolkit().getImage("robot3.png");
			target = Toolkit.getDefaultToolkit().getImage("target.png");
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);	
			g.drawRect(230, 80, 10, 10);
			g.setColor(Color.RED);
			g.fillRect(230, 80, 10, 10);
			// draw the board

			g.drawImage(board, 100, 75, this);
			g.drawImage(target, 285, 260, this);
			if (direction == 0)
				g.drawImage(robot0, 112 + robotX * 59, 80 + robotY * 59, this);
			else if (direction == 1)
				g.drawImage(robot1, 106 + robotX * 59, 85 + robotY * 59, this);
			else if (direction == 2)
				g.drawImage(robot2, 112 + robotX * 59, 80 + robotY * 59, this);
			else if (direction == 3)
				g.drawImage(robot3, 106 + robotX * 59, 85 + robotY * 59, this);

		}

	}

}
