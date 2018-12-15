package chess;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Chess extends Frame {
	private Timer timer = null; // Ÿ�̸�

	private final int sql = 90; // ü������ ���簢�� �� ���� ����
	private final int w = 1230, h = 1430; // ���� ���� ����
	piece board[][] = new piece[8][8]; // ���� �÷� ���� ����

	private boolean[][] onClick = new boolean[8][8]; // Ŭ���Ǿ����� Ȯ��
	private boolean firstClick = true; // ù ��° Ŭ������ Ȯ��
	private boolean Moveable[][] = new boolean[8][8]; // Ư���� ���� ������ �� �ִ� �������� Ȯ��

	private boolean booleanundo = true; // �ڷΰ��� ��� ����(����� ���� ����)
	private boolean bqc, bkc, wqc, wkc; // ĳ���� ���� ����(bqc�� ��� ���� ĳ���� �̶�� �ǹ�)

	private String turn = "white"; // ������ �������� ��Ÿ���� ����
	private String[] NumtoAl = { "a", "b", "c", "d", "e", "f", "g", "h" }; // ���� �̵��� ǥ���ϱ� ���� �迭
	private String[] pieceName = { "pawn", "knight", "bishop", "rook", "queen", "king" };
	private String windata = "";

	private int ci, cj;
	private int MaxTime = 300; // �ִ� �ð� ����
	private int Inc = 1; // �� ������ �߰��Ǵ� �ð�
	private int wtime, btime; // ��� ���� ���� ���� �ð�
	private int nmoves; // �� ������ Ƚ��

	private Label lLastMove = null; // ������ ���� ��Ÿ���� ��
	private Label lWhiteTimer = null; // ���� ���� �ð��� ��Ÿ���� ��
	private Label lBlackTimer = null; // ���� ���� �ð��� ��Ÿ���� ��
	private JTextArea lwindata = null;

	private JButton bUndo = null; // �ڷΰ��� ��ư
	private JButton bForfeit = null; // �׺� ��ư
	private JButton bDraw = null; // ���º� ���� ��ư
	private JButton bStart = null; // ����� ��ư
	private JButton bSettime = null; // �ð� ���� ��ư
	private JButton bSetundo = null; // ������ ���� ��ư
	private boolean save = false;
	private JButton bBoard[][] = new JButton[8][8]; // ��ü ����

	private ImageIcon icon_light = new ImageIcon("pic/light.png"); // ���� ĭ�� ��Ÿ���� icon
	private ImageIcon icon_dark = new ImageIcon("pic/dark.png"); // ��ο� ĭ�� ��Ÿ���� icon

	class BoardState implements Serializable { // ü������ ���¸� ������ ���� ���� Ŭ����
		int board[][] = new int[8][8];
		String turn, lastmove;
		boolean bqc, bkc, wqc, wkc;
	}

	BoardState[] bstate = new BoardState[600];

	abstract class piece implements Serializable { // �� Ŭ���� ����
		int i, j; // ���� ��ġ
		String color, boardcolor, name; // ���� ��, ���� ��ġ�� ���� ĭ�� ��, ���� ����
		int ind; // ���� ������ ��Ÿ���� ���� (BoardState Ŭ������ ����)
		ImageIcon Icon, clickIcon; // �׳� ���� �� ������, Ŭ���Ǿ��� �� ������

		piece(int a, int b, String c, int ind) {
			this.i = a;
			this.j = b;
			this.color = c;
			this.ind = ind;
			this.name = pieceName[ind - 1];
			if ((this.i + this.j) % 2 == 0)
				this.boardcolor = "light";
			else
				this.boardcolor = "dark";
			Icon = new ImageIcon("pic/" + this.color + "_" + this.name + "_" + this.boardcolor + ".png");
			clickIcon = new ImageIcon("pic/" + this.color + "_" + this.name + "_clicked.png");
		}

		void move(int a, int b) { // ���� Ư�� ��ġ�� �̵�
			this.i = a;
			this.j = b;
			if ((this.i + this.j) % 2 == 0)
				this.boardcolor = "light";
			else
				this.boardcolor = "dark";

			Icon = new ImageIcon("pic/" + this.color + "_" + this.name + "_" + this.boardcolor + ".png");
		}

		abstract void setMoveable(); // ������ �� �ִ� ���� ����
	}

	class Pawn extends piece { // �� Ŭ���� ����
		Pawn(int a, int b, String c) { // �����ڷ� �ʱⰪ ����
			super(a, b, c, 1);
		}

		@Override
		void move(int a, int b) { // ���θ�� ������ ���� Overriding
			this.i = a;
			this.j = b;
			if ((this.i + this.j) % 2 == 0)
				this.boardcolor = "light";
			else
				this.boardcolor = "dark";

			if (this.i == 0 || this.i == 7) { // ������ ���� �������� �� ���ϴ� ���� ��ȯ
				Object[] promotion = { "Knight", "Bishop", "Rook", "Queen" };
				Label label = new Label("Promote to another piece: ");
				label.setFont(new Font("Arial", Font.PLAIN, 20));
				String s = (String) JOptionPane.showInputDialog(null, label, "Promotion", JOptionPane.PLAIN_MESSAGE,
						null, promotion, "Queen");
				promote(this.i, this.j, s, this.color);
			}
			Icon = new ImageIcon("pic/" + this.color + "_" + this.name + "_" + this.boardcolor + ".png");
		}

		void setMoveable() {
			if (this.color == "black") {
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if (this.i + 1 == i) {
							if (board[i][j] == null) {
								if (j == this.j)
									Moveable[i][j] = true; // ���� ������ �� ĭ ������ �� �ִ�
								else {
								}
							} else if (board[i][j].color == "white" && (Math.abs(this.j - j) == 1))
								Moveable[i][j] = true; // ���� ��� ���� ���� ������ �밢������ �� ĭ �̵��Ѵ�
							else
								Moveable[i][j] = false;
						} else
							Moveable[i][j] = false;
					}
				}
				if (this.i == 1 && board[3][j] == null)
					Moveable[3][j] = true; // ó�� �����̴� ���� �� ĭ ������ �� �ִ�
			} else {
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if (this.i - 1 == i) {
							if (board[i][j] == null) {
								if (j == this.j)
									Moveable[i][j] = true;
								else {
								}
							} else if (board[i][j].color == "black" && (Math.abs(this.j - j) == 1))
								Moveable[i][j] = true;
							else
								Moveable[i][j] = false;
						} else
							Moveable[i][j] = false;
					}
					if (this.i == 6 && board[4][j] == null)
						Moveable[4][j] = true;
				}
			}
		}
	}

	class Knight extends piece { // ����Ʈ Ŭ���� ����
		Knight(int a, int b, String c) { // �����ڷ� �ʱⰪ ����
			super(a, b, c, 2);
		}

		void setMoveable() {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if ((Math.abs(this.i - i) == 2 && Math.abs(this.j - j) == 1)
							|| (Math.abs(this.i - i) == 1 && Math.abs(this.j - j) == 2)) { // ����Ʈ�� �� �������� �� ĭ �̵��ϰ�
																							// ������ �������� �� ĭ �̵��Ѵ�
						if (board[i][j] == null)
							Moveable[i][j] = true;
						else if ((board[i][j].color == "white" && this.color == "black")
								|| (board[i][j].color == "black" && this.color == "white"))
							Moveable[i][j] = true; // �̵��� ���� ��ġ�� ��� �⹰�� ������ ���� �� �ִ�
						else
							Moveable[i][j] = false;
					} else
						Moveable[i][j] = false;
				}
			}
		}
	}

	class Bishop extends piece { // ��� Ŭ���� ����
		Bishop(int a, int b, String c) { // �����ڸ� ���� �ʱⰪ ����
			super(a, b, c, 3);
			ind = 3;
		}

		void setMoveable() {
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
					Moveable[i][j] = false;

			int[] di = { 1, 1, -1, -1 };
			int[] dj = { 1, -1, 1, -1 };

			for (int k = 0; k < 4; k++) {
				int i = this.i + di[k], j = this.j + dj[k];
				while (0 <= i && i < 8 && 0 <= j && j < 8) {
					if (board[i][j] == null)
						Moveable[i][j] = true;
					else if ((board[i][j].color == "black" && this.color == "black")
							|| (board[i][j].color == "white" && this.color == "white"))
						break;
					else {
						Moveable[i][j] = true;
						break;
					}
					i += di[k];
					j += dj[k];
				}
			}
		}
	}

	class Rook extends piece { // �� Ŭ���� ����
		Rook(int a, int b, String c) { // �����ڸ� ���� �ʱⰪ ����
			super(a, b, c, 4);
		}

		@Override
		void move(int a, int b) { // ĳ���� ���� ��Ģ�� ���� Overriding
			if (this.j == 0 && this.color == "black")
				bqc = false; // ���� �����̸� �� �������δ� ĳ������ �� ����
			if (this.j == 7 && this.color == "black")
				bkc = false;
			if (this.j == 0 && this.color == "white")
				wqc = false;
			if (this.j == 0 && this.color == "white")
				wkc = false;

			this.i = a;
			this.j = b;
			if ((this.i + this.j) % 2 == 0)
				this.boardcolor = "light";
			else
				this.boardcolor = "dark";

			Icon = new ImageIcon("pic/" + this.color + "_" + this.name + "_" + this.boardcolor + ".png");
		}

		void setMoveable() { // ���� ���༱���� ���ϴ� ��ŭ �̵��� �� �ִ�. ��, �տ� ���� ���θ��� ������ �̵��� �� ���� ��� ���̸� �� ���� ���� �� �ִ�
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
					Moveable[i][j] = false;

			int[] di = { 1, -1, 0, 0 };
			int[] dj = { 0, 0, 1, -1 };

			for (int k = 0; k < 4; k++) {
				int i = this.i + di[k], j = this.j + dj[k];
				while (0 <= i && i < 8 && 0 <= j && j < 8) {
					if (board[i][j] == null)
						Moveable[i][j] = true;
					else if ((board[i][j].color == "black" && this.color == "black")
							|| (board[i][j].color == "white" && this.color == "white"))
						break;
					else {
						Moveable[i][j] = true;
						break;
					}
					i += di[k];
					j += dj[k];
				}
			}
		}
	}

	class Queen extends piece { // �� Ŭ���� ����
		Queen(int a, int b, String c) { // �����ڸ� ���� �ʱⰪ ����
			super(a, b, c, 5);
		}

		void setMoveable() { // ���� ���༱����, �밢������ ���ϴ� ��ŭ �̵��� �� �ִ�. ��, �տ� ���� ���θ��� ������ �̵��� �� ���� ��� ���̸� �� ���� ���� ��
								// �ִ�
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
					Moveable[i][j] = false;

			int[] di = { 1, 1, -1, -1, 1, -1, 0, 0 };
			int[] dj = { 1, -1, 1, -1, 0, 0, 1, -1 };

			for (int k = 0; k < 8; k++) {
				int i = this.i + di[k], j = this.j + dj[k];
				while (0 <= i && i < 8 && 0 <= j && j < 8) {
					if (board[i][j] == null)
						Moveable[i][j] = true;
					else if ((board[i][j].color == "black" && this.color == "black")
							|| (board[i][j].color == "white" && this.color == "white"))
						break;
					else {
						Moveable[i][j] = true;
						break;
					}
					i += di[k];
					j += dj[k];
				}
			}
		}
	}

	class King extends piece { // ŷ Ŭ���� ����
		King(int a, int b, String c) { // �����ڸ� ���� �ʱⰪ ����
			super(a, b, c, 6);
		}

		@Override
		void move(int a, int b) { // ĳ���� ������ ���� Overriding. ŷ�� �� �� �����̸� �� �̻� ĳ���� �� �� ����
			if (this.color == "black") {
				bqc = false;
				bkc = false;
			}
			if (this.color == "white") {
				wqc = false;
				wkc = false;
			}

			this.i = a;
			this.j = b;
			if ((this.i + this.j) % 2 == 0)
				this.boardcolor = "light";
			else
				this.boardcolor = "dark";

			Icon = new ImageIcon("pic/" + this.color + "_" + this.name + "_" + this.boardcolor + ".png");
		}

		void setMoveable() { // ŷ�� �ڽ� �ֺ� 1ĭ���� �̵��� �� �ִ�
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
					Moveable[i][j] = false;
			for (int i = this.i - 1; i <= this.i + 1; i++) {
				for (int j = this.j - 1; j <= this.j + 1; j++) {
					if (0 <= i && i < 8 && 0 <= j && j < 8) {
						if (board[i][j] == null)
							Moveable[i][j] = true;
						else if ((board[i][j].color == "white" && this.color == "black")
								|| (board[i][j].color == "black" && this.color == "white"))
							Moveable[i][j] = true;
						else
							continue;
					}
				}
			}
			if (bqc && board[0][1] == null && board[0][2] == null && board[0][3] == null)
				Moveable[0][2] = true;
			if (bkc && board[0][5] == null && board[0][6] == null)
				Moveable[0][6] = true;
		}
	}

	Chess() {
		makeGUI(); // GUI �����
		initGame(); // ���� �ʱ�ȭ
	}

	void makeGUI() { // GUI ����
		setSize(w, h); // Frame ũ�� ����

		Panel controls = new Panel(); // ��ư���� ��ġ�� panel
		Panel labels = new Panel(); // �󺧵��� ��ġ�� panel
		add(controls, BorderLayout.SOUTH); // ��ư���� �Ʒ���
		add(labels, BorderLayout.NORTH); // �󺧵��� ����

		Font font = new Font("Arial", Font.PLAIN, 20); // ��Ʈ ����
		try (BufferedReader br = new BufferedReader(new FileReader("windata.txt"))) {
			String l;
			while ((l = br.readLine()) != null)
				windata += (l + "\r\n");
		} catch (IOException e) {
		}

		lLastMove = new Label("Last Move:                  "); // �ʱ� �ؽ�Ʈ ����
		lWhiteTimer = new Label("White:                    ");
		lBlackTimer = new Label("Black:                    ");
		lwindata = new JTextArea(windata);
		lLastMove.setSize(new Dimension(200, 50)); // ũ�� ����
		lWhiteTimer.setSize(new Dimension(200, 50));
		lBlackTimer.setSize(new Dimension(200, 50));
		lwindata.setSize(new Dimension(20,20));
		lLastMove.setFont(font); // ��Ʈ ����
		lWhiteTimer.setFont(font);
		lBlackTimer.setFont(font);
		lwindata.setFont(font);

		bUndo = new JButton("Undo"); // �ǵ����� Button
		bForfeit = new JButton("Forfeit"); // �׺� Button
		bDraw = new JButton("Draw"); // ���º� Button
		bStart = new JButton("Start New Game"); // ���� ���� ��ư
		bSettime = new JButton("Time Settings"); // �ð� ���� ���� ��ư
		bSetundo = new JButton("Undo Settings"); // �ٷΰ��� ���� ����
		bUndo.setPreferredSize(new Dimension(150, 50)); // ũ�� ����
		bUndo.setFont(font); // ��Ʈ ����
		bForfeit.setPreferredSize(new Dimension(150, 50));
		bForfeit.setFont(font);
		bDraw.setPreferredSize(new Dimension(150, 50));
		bDraw.setFont(font);
		bStart.setPreferredSize(new Dimension(300, 50));
		bStart.setFont(font);
		bSettime.setPreferredSize(new Dimension(200, 50));
		bSettime.setFont(font);
		bSetundo.setPreferredSize(new Dimension(200, 50));
		bSetundo.setFont(font);

		labels.add(lLastMove); // panel�� �߰�
		labels.add(lWhiteTimer);
		labels.add(lBlackTimer);
		controls.add(bUndo);
		controls.add(bForfeit);
		controls.add(bDraw);
		controls.add(bStart);
		controls.add(bSettime);
		controls.add(bSetundo);

		Panel board = new Panel(); // ü������ ��Ÿ�� panel
		add(board); // Frame�� �߰�
		add(lwindata, BorderLayout.EAST);
		board.setLayout(null);
		

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				bBoard[i][j] = new JButton(); // ü���� �� ĭ
				bBoard[i][j].setSize(sql, sql); // �� ĭ�� ũ�� ����
				bBoard[i][j].setLocation(j * sql + 240, i * sql + 10); // �� ĭ�� ��ġ ����
				board.add(bBoard[i][j]); // board panel�� �߰�

				bBoard[i][j].addActionListener(new ClickListener(i, j)); // Listener �߰�
			}
		}

		setVisible(true); // ���̱�

		addWindowListener(new MyWindowAdapter()); // â�ݱ� ��ư

		timer = new Timer(1000, new ActionListener() { // Ÿ�̸� ����. 1�ʸ��� �ð� 1�� ����
			@Override
			public void actionPerformed(ActionEvent e) {
				if (turn == "white")
					wtime--;
				else
					btime--;
				updateTime();
			}
		});

		bStart.addActionListener(new ActionListener() { // �ٽ��ϱ� ��ư ����
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Restart?");
				label.setFont(font);
				int restart = JOptionPane.showConfirmDialog(null, label); // �ٽ� �� �� ����� JOptionPane ����
				if (restart == 0) {
					initGame(); // �����
				}
			}
		});

		bForfeit.addActionListener(new ActionListener() { // �׺� ��ư ����
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("ForFeit?");
				label.setFont(font);
				int forfeit = JOptionPane.showConfirmDialog(null, label); // �׺��� �� ����� JOptionPane ����
				if (forfeit == 0) {
					Label forfeitLabel = null;
					if (turn == "white") {
						forfeitLabel = new Label("White Forfeits");
						windata += "Black Win            \r\n";
					} else {
						forfeitLabel = new Label("Black Forfeits");
						windata += "White Win            \r\n";
					}
					forfeitLabel.setFont(font);
					JOptionPane.showMessageDialog(null, forfeitLabel, "Game Over !!", JOptionPane.INFORMATION_MESSAGE); // ������
																														// �����ٰ�
																														// �˸�
					initGame(); // �����
				}
			}
		});

		bUndo.addActionListener(new ActionListener() { // �ڷΰ��� ��ư ����
			@Override
			public void actionPerformed(ActionEvent e) {
				if (booleanundo)
					undo(); // �ڷΰ��Ⱑ ������ �����̸� �ڷΰ���
			}
		});

		bDraw.addActionListener(new ActionListener() { // ���º� ��ư ����
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Accept Draw?");
				label.setFont(font);
				int draw = JOptionPane.showConfirmDialog(null, label); // ���ºθ� �޾Ƶ��� ������ ���
				if (draw == 0) {
					label.setText("Draw!!");
					windata += "Draw            \r\n";
					JOptionPane.showMessageDialog(null, label, "Draw", JOptionPane.INFORMATION_MESSAGE); // ���º����� ��Ÿ��
					initGame(); // �����
				} else if (draw == 1) {
					label.setText("Draw declined");
					JOptionPane.showMessageDialog(null, label, "Draw declined", JOptionPane.INFORMATION_MESSAGE); // ���ºΰ�
																													// �ƴ���
																													// ��Ÿ��
				} else {
				}
			}
		});

		bSettime.addActionListener(new ActionListener() { // �ð� ����
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Maximum time in seconds: "); // �ִ� �ð��� �� ������ ����
				label.setFont(font);
				String s = (String) JOptionPane.showInputDialog(null, label, "Set time", JOptionPane.PLAIN_MESSAGE,
						null, null, "");
				try { // ������ �ƴ� ���� �Է����� ���� ����
					MaxTime = Integer.parseInt(s);
				} catch (Exception e1) {
				}
				label.setText("Increment in seconds: "); // increment(�� ������ �ð� ������)�� �� ������ ����
				s = (String) JOptionPane.showInputDialog(null, label, "Set increment", JOptionPane.PLAIN_MESSAGE, null,
						null, "");
				try {
					Inc = Integer.parseInt(s);
				} catch (Exception e2) {
				}
			}
		});

		bSetundo.addActionListener(new ActionListener() { // �ڷΰ��� ���� ��ư ����
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Allow undo?"); // �ڷΰ��⸦ Ȱ��ȭ��Ű������� ���
				label.setFont(font);
				int undo = JOptionPane.showConfirmDialog(null, label);
				if (undo == 0) {
					label.setText("Undo activated");
					JOptionPane.showMessageDialog(null, label, "Undo settings", JOptionPane.INFORMATION_MESSAGE); // Ȱ��ȭ
																													// �˸�
					booleanundo = true;
				} else if (undo == 1) {
					label.setText("Undo inactivated");
					JOptionPane.showMessageDialog(null, label, "Undo settings", JOptionPane.INFORMATION_MESSAGE); // ��Ȱ��ȭ
																													// �˸�
					booleanundo = false;
				} else {
				}
			}
		});
	}

	boolean checkCheck(String s) { // Ư�� ���� ���� üũ���� Ȯ���ϴ� �޼ҵ�
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] != null)
					if (board[i][j].color == s) {
						board[i][j].setMoveable();
						for (int k = 0; k < 8; k++) {
							for (int l = 0; l < 8; l++) {
								if (Moveable[k][l] && board[k][l] != null)
									if (board[k][l].name == "king")
										return true; // ������ �� �ְ� �� �ڸ��� ŷ�� ������ true ��ȯ
							}
						}
					}
			}
		}
		return false; // ������ false ��ȯ
	}

	void updateLastMove(String s) {
		lLastMove.setText("Last Move: " + s); // ������ �� �� ������Ʈ
	}

	void updateTime() { // �ð� ������Ʈ
		lWhiteTimer.setText("White: " + wtime / 60 + " min " + wtime % 60 + " sec"); // ���� �ð� ǥ��
		lBlackTimer.setText("Black: " + btime / 60 + " min " + btime % 60 + " sec");
		if (wtime == 0 || btime == 0) { // �� �� �ð��� �� �Ǿ��� ��
			Label label;
			if (wtime == 0) {
				label = new Label("White loses on time");
				windata += "Black Win            \r\n";
			}
			else {
				label = new Label("Black loses on time");
				windata += "White Win            \r\n";
			}
			label.setFont(new Font("Arial", Font.PLAIN, 20));
			JOptionPane.showMessageDialog(null, label, "Game Over!", JOptionPane.INFORMATION_MESSAGE); // ���� �����ٰ� ǥ��
			initGame(); // �����
		}
	}

	void undo() { // �ڷΰ��� �޼ҵ�
		if (nmoves >= 1) { // �̹� �� �� ������ ���¿����� �ڷΰ��� ����
			bstate[nmoves--] = null; // bstate �迭�� ����� ������ ��������
			bqc = bstate[nmoves].bqc;
			bkc = bstate[nmoves].bkc;
			wqc = bstate[nmoves].wqc;
			wkc = bstate[nmoves].wkc;

			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if (bstate[nmoves].board[i][j] == 0) {
						board[i][j] = null;
					} else if (bstate[nmoves].board[i][j] / 10 == 0) {
						if (bstate[nmoves].board[i][j] % 10 == 1)
							board[i][j] = new Pawn(i, j, "black");
						if (bstate[nmoves].board[i][j] % 10 == 2)
							board[i][j] = new Knight(i, j, "black");
						if (bstate[nmoves].board[i][j] % 10 == 3)
							board[i][j] = new Bishop(i, j, "black");
						if (bstate[nmoves].board[i][j] % 10 == 4)
							board[i][j] = new Rook(i, j, "black");
						if (bstate[nmoves].board[i][j] % 10 == 5)
							board[i][j] = new Queen(i, j, "black");
						if (bstate[nmoves].board[i][j] % 10 == 6)
							board[i][j] = new King(i, j, "black");
					} else if (bstate[nmoves].board[i][j] / 10 == 1) {
						if (bstate[nmoves].board[i][j] % 10 == 1)
							board[i][j] = new Pawn(i, j, "white");
						if (bstate[nmoves].board[i][j] % 10 == 2)
							board[i][j] = new Knight(i, j, "white");
						if (bstate[nmoves].board[i][j] % 10 == 3)
							board[i][j] = new Bishop(i, j, "white");
						if (bstate[nmoves].board[i][j] % 10 == 4)
							board[i][j] = new Rook(i, j, "white");
						if (bstate[nmoves].board[i][j] % 10 == 5)
							board[i][j] = new Queen(i, j, "white");
						if (bstate[nmoves].board[i][j] % 10 == 6)
							board[i][j] = new King(i, j, "white");
					}
				}
			}
			for (int i = 0; i < 8; i++) { // ������ ������Ʈ
				for (int j = 0; j < 8; j++) {
					if (board[i][j] == null) {
						if ((i + j) % 2 == 0)
							bBoard[i][j].setIcon(icon_light);
						else
							bBoard[i][j].setIcon(icon_dark);
					} else
						bBoard[i][j].setIcon(board[i][j].Icon);
				}
			}
			turn = bstate[nmoves].turn;
			updateLastMove(bstate[nmoves].lastmove);
		}
	}

	void initGame() { // ���� ���� �޼ҵ�

		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				board[i][j] = null; // ��� ���� �ʱ�ȭ
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				onClick[i][j] = false;
		turn = "white";
		bqc = true;
		bkc = true;
		wqc = true;
		wkc = true;
		updateLastMove("");

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				onClick[i][j] = false;
			}
		}

		wtime = MaxTime;
		btime = MaxTime;
		nmoves = 0;
		bstate[0] = new BoardState();
		bstate[0].lastmove = "";

		for (int i = 0; i < 8; i++) { // �� �ʱ� ��ġ ����
			board[1][i] = new Pawn(1, i, "black");
			board[6][i] = new Pawn(6, i, "white");
		}
		board[0][0] = new Rook(0, 0, "black");
		board[7][0] = new Rook(7, 0, "white");
		board[0][7] = new Rook(0, 7, "black");
		board[7][7] = new Rook(7, 7, "white"); // ��
		board[0][1] = new Knight(0, 1, "black");
		board[7][1] = new Knight(7, 1, "white");
		board[0][6] = new Knight(0, 6, "black");
		board[7][6] = new Knight(7, 6, "white"); // ����Ʈ
		board[0][2] = new Bishop(0, 2, "black");
		board[7][2] = new Bishop(7, 2, "white");
		board[0][5] = new Bishop(0, 5, "black");
		board[7][5] = new Bishop(7, 5, "white"); // ���
		board[0][3] = new Queen(0, 3, "black");
		board[0][4] = new King(0, 4, "black"); // ��� ŷ&��
		board[7][3] = new Queen(7, 3, "white");
		board[7][4] = new King(7, 4, "white"); // ��� ŷ&��

		for (int i = 0; i < 8; i++) { // ������ ����
			for (int j = 0; j < 8; j++) {
				if (board[i][j] == null && (i + j) % 2 == 0)
					bBoard[i][j].setIcon(icon_light);
				else if (board[i][j] == null && (i + j) % 2 == 1)
					bBoard[i][j].setIcon(icon_dark);
				else
					bBoard[i][j].setIcon(board[i][j].Icon);
			}
		}

		timer.start(); // Ÿ�̸� ����
	}

	void promote(int a, int b, String s, String c) { // ���θ�� �޼ҵ�
		if (s == "Queen")
			board[a][b] = new Queen(a, b, c);
		if (s == "Rook")
			board[a][b] = new Rook(a, b, c);
		if (s == "Bishop")
			board[a][b] = new Bishop(a, b, c);
		if (s == "Knight")
			board[a][b] = new Knight(a, b, c);

		bBoard[a][b].setIcon(board[a][b].Icon);
	}

	class ClickListener implements ActionListener {
		private int i, j;

		ClickListener(int i, int j) { // �����ڷ� ���빰 ����
			this.i = i;
			this.j = j;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (onClick[i][j]) { // ���� ���� �� �� Ŭ���ϸ�
				onClick[i][j] = false; // Ŭ������ ���� ���·� ���ư�
				firstClick = true;
				bBoard[i][j].setIcon(board[i][j].Icon);
			} else if (firstClick == true) { // ù ��° Ŭ���̸�
				if (board[i][j] != null) { // ���� ���� �ִ� ��쿡��
					if (board[i][j].color == turn) { // ���ʰ� �´� ��쿡��
						firstClick = false; // ���� Ŭ���� �� ��° Ŭ��
						onClick[i][j] = true; // �̹� Ŭ���ߴٰ� ǥ��
						ci = i; // Ŭ���� �� ����
						cj = j;
						board[i][j].setMoveable(); // ������ �� �ִ� ���� ����
						bBoard[i][j].setIcon(board[i][j].clickIcon); // Ŭ���ߴٰ� ������ ����
					}
				}
			} else if (Moveable[i][j] == true) { // �� ��° Ŭ������ ������ �� �ִ� ������ ���ϸ�
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						bstate[nmoves].bqc = bqc; // ���� ���¸� bstate �迭�� ����
						bstate[nmoves].bkc = bkc;
						bstate[nmoves].wqc = wqc;
						bstate[nmoves].wkc = wkc;
						for (int x = 0; x < 8; x++) {
							for (int y = 0; y < 8; y++) {
								if (board[x][y] != null) {
									bstate[nmoves].board[x][y] = board[x][y].ind;
									if (board[x][y].color == "white")
										bstate[nmoves].board[x][y] += 10;
								} else
									bstate[nmoves].board[x][y] = 0;
							}
						}
						bstate[nmoves].turn = turn;
						nmoves++; // �� �� ������

						if (board[ci][cj].name == "king") { // ĳ���� ���� ó��(�� ���� �� ���� ���� �̵��ϹǷ�)
							if (board[ci][cj].color == "black") {
								if (bqc && i == 0 && j == 2) {
									board[0][3] = board[0][0];
									board[0][0] = null;
									board[0][3].move(0, 3);
									bBoard[0][3].setIcon(board[0][3].Icon);
									bBoard[0][0].setIcon(icon_light);
									bqc = false;
								}
								if (bkc && i == 0 && j == 6) {
									board[0][5] = board[0][7];
									board[0][7] = null;
									board[0][5].move(0, 5);
									bBoard[0][5].setIcon(board[0][5].Icon);
									bBoard[0][7].setIcon(icon_dark);
									bkc = false;
								}
							}
							if (board[ci][cj].color == "white") {
								if (wqc && i == 7 && j == 2) {
									board[7][3] = board[7][0];
									board[7][0] = null;
									board[7][3].move(7, 3);
									bBoard[7][3].setIcon(board[7][3].Icon);
									bBoard[7][0].setIcon(icon_dark);
									wqc = false;
								}
								if (wkc && i == 7 && j == 6) {
									board[7][5] = board[7][7];
									board[7][7] = null;
									board[7][5].move(7, 5);
									bBoard[7][5].setIcon(board[7][5].Icon);
									bBoard[7][7].setIcon(icon_light);
									wkc = false;
								}
							}
						}

						board[i][j] = board[ci][cj]; // �� Ŭ���� �̵�
						board[ci][cj] = null; // ������ �ִ� �ڸ��� null
						board[i][j].move(i, j); // Ŭ���� ���� �� ����

						bBoard[i][j].setIcon(board[i][j].Icon); // ������ ����
						if ((ci + cj) % 2 == 0)
							bBoard[ci][cj].setIcon(icon_light);
						else
							bBoard[ci][cj].setIcon(icon_dark);
						firstClick = true; // Ŭ������ ���� ���·� ���ư�
						onClick[ci][cj] = false;
						onClick[i][j] = false;

						updateLastMove(NumtoAl[cj] + (8 - ci) + " to " + NumtoAl[j] + (8 - i)); // ������ �� ������Ʈ

						if (checkCheck(turn)) { // ���� üũ�̸�
							Label check = new Label("Check!");
							check.setFont(new Font("Arial", Font.PLAIN, 20));
							JOptionPane.showMessageDialog(null, check, "Check!!!", JOptionPane.INFORMATION_MESSAGE);
						}
						if (turn == "white") {
							wtime += Inc;
							updateTime();
							turn = "black";
						} else {
							btime += Inc;
							updateTime();
							turn = "white";
						}

						bstate[nmoves] = new BoardState(); // �� BoardState�� ����
						bstate[nmoves].lastmove = NumtoAl[cj] + (8 - ci) + " to " + NumtoAl[j] + (8 - i); // ���� �� ����

						if (checkCheck(turn)) { // ���� �ڽ��� ŷ�� üũ ���°� �Ǹ� �߸��� ��
							Label illegal = new Label("Illegal Move!");
							illegal.setFont(new Font("Arial", Font.PLAIN, 20));
							JOptionPane.showMessageDialog(null, illegal, "Illegal Move!!!",
									JOptionPane.INFORMATION_MESSAGE); // �߸��� ������ �˷���
							undo(); // �ڷΰ���
						}
					}
				});
				thread.start();
			}
		}
	}

	class MyWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			save();
			System.exit(0);
		}
	}

	void save() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("windata.txt"))) {
			bw.write(windata);
		} catch (IOException e) {
		}
	}
}