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
	private Timer timer = null; // 타이머

	private final int sql = 90; // 체스판의 정사각형 한 변의 길이
	private final int w = 1230, h = 1430; // 가로 세로 설정
	piece board[][] = new piece[8][8]; // 말을 올려 놓을 보드

	private boolean[][] onClick = new boolean[8][8]; // 클릭되었는지 확인
	private boolean firstClick = true; // 첫 번째 클릭인지 확인
	private boolean Moveable[][] = new boolean[8][8]; // 특정한 말이 움직일 수 있는 공간인지 확인

	private boolean booleanundo = true; // 뒤로가기 허용 여부(사용자 설정 가능)
	private boolean bqc, bkc, wqc, wkc; // 캐슬링 가능 여부(bqc는 흑색 퀸쪽 캐슬링 이라는 의미)

	private String turn = "white"; // 누구의 순서인지 나타내는 변수
	private String[] NumtoAl = { "a", "b", "c", "d", "e", "f", "g", "h" }; // 말의 이동을 표기하기 위한 배열
	private String[] pieceName = { "pawn", "knight", "bishop", "rook", "queen", "king" };
	private String windata = "";

	private int ci, cj;
	private int MaxTime = 300; // 최대 시간 제한
	private int Inc = 1; // 매 수마다 추가되는 시간
	private int wtime, btime; // 백과 흑이 각각 남은 시간
	private int nmoves; // 총 움직임 횟수

	private Label lLastMove = null; // 마지막 수를 나타내는 라벨
	private Label lWhiteTimer = null; // 백의 남은 시간을 나타내는 라벨
	private Label lBlackTimer = null; // 흑의 남은 시간을 나타내는 라벨
	private JTextArea lwindata = null;

	private JButton bUndo = null; // 뒤로가기 버튼
	private JButton bForfeit = null; // 항복 버튼
	private JButton bDraw = null; // 무승부 권유 버튼
	private JButton bStart = null; // 재시작 버튼
	private JButton bSettime = null; // 시간 설정 버튼
	private JButton bSetundo = null; // 물리기 유무 버튼
	private boolean save = false;
	private JButton bBoard[][] = new JButton[8][8]; // 전체 보드

	private ImageIcon icon_light = new ImageIcon("pic/light.png"); // 밝은 칸을 나타내는 icon
	private ImageIcon icon_dark = new ImageIcon("pic/dark.png"); // 어두운 칸을 나타내는 icon

	class BoardState implements Serializable { // 체스판의 상태를 저장해 놓기 위한 클래스
		int board[][] = new int[8][8];
		String turn, lastmove;
		boolean bqc, bkc, wqc, wkc;
	}

	BoardState[] bstate = new BoardState[600];

	abstract class piece implements Serializable { // 말 클래스 구현
		int i, j; // 말의 위치
		String color, boardcolor, name; // 말의 색, 말이 위치한 보드 칸의 색, 말의 종류
		int ind; // 말의 종류를 나타내는 정수 (BoardState 클래스를 위해)
		ImageIcon Icon, clickIcon; // 그냥 있을 때 아이콘, 클릭되었을 때 아이콘

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

		void move(int a, int b) { // 말을 특정 위치로 이동
			this.i = a;
			this.j = b;
			if ((this.i + this.j) % 2 == 0)
				this.boardcolor = "light";
			else
				this.boardcolor = "dark";

			Icon = new ImageIcon("pic/" + this.color + "_" + this.name + "_" + this.boardcolor + ".png");
		}

		abstract void setMoveable(); // 움직일 수 있는 공간 결정
	}

	class Pawn extends piece { // 폰 클래스 구현
		Pawn(int a, int b, String c) { // 생성자로 초기값 결정
			super(a, b, c, 1);
		}

		@Override
		void move(int a, int b) { // 프로모션 설정을 위한 Overriding
			this.i = a;
			this.j = b;
			if ((this.i + this.j) % 2 == 0)
				this.boardcolor = "light";
			else
				this.boardcolor = "dark";

			if (this.i == 0 || this.i == 7) { // 보드의 끝에 도달했을 때 원하는 말로 교환
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
									Moveable[i][j] = true; // 폰은 앞으로 한 칸 전진할 수 있다
								else {
								}
							} else if (board[i][j].color == "white" && (Math.abs(this.j - j) == 1))
								Moveable[i][j] = true; // 폰은 상대 말을 잡을 때에는 대각선으로 한 칸 이동한다
							else
								Moveable[i][j] = false;
						} else
							Moveable[i][j] = false;
					}
				}
				if (this.i == 1 && board[3][j] == null)
					Moveable[3][j] = true; // 처음 움직이는 폰은 두 칸 움직일 수 있다
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

	class Knight extends piece { // 나이트 클래스 구현
		Knight(int a, int b, String c) { // 생성자로 초기값 설정
			super(a, b, c, 2);
		}

		void setMoveable() {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if ((Math.abs(this.i - i) == 2 && Math.abs(this.j - j) == 1)
							|| (Math.abs(this.i - i) == 1 && Math.abs(this.j - j) == 2)) { // 나이트는 한 방향으로 두 칸 이동하고
																							// 수직한 방향으로 한 칸 이동한다
						if (board[i][j] == null)
							Moveable[i][j] = true;
						else if ((board[i][j].color == "white" && this.color == "black")
								|| (board[i][j].color == "black" && this.color == "white"))
							Moveable[i][j] = true; // 이동한 최종 위치에 상대 기물이 있으면 잡을 수 있다
						else
							Moveable[i][j] = false;
					} else
						Moveable[i][j] = false;
				}
			}
		}
	}

	class Bishop extends piece { // 비숍 클래스 구현
		Bishop(int a, int b, String c) { // 생성자를 통한 초기값 설정
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

	class Rook extends piece { // 룩 클래스 구현
		Rook(int a, int b, String c) { // 생성자를 통한 초기값 설정
			super(a, b, c, 4);
		}

		@Override
		void move(int a, int b) { // 캐슬링 관련 규칙을 위한 Overriding
			if (this.j == 0 && this.color == "black")
				bqc = false; // 룩이 움직이면 그 방향으로는 캐슬링할 수 없다
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

		void setMoveable() { // 룩은 평행선으로 원하는 만큼 이동할 수 있다. 단, 앞에 말이 가로막고 있으면 이동할 수 없고 상대 말이면 그 말을 잡을 수 있다
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

	class Queen extends piece { // 퀸 클래스 구현
		Queen(int a, int b, String c) { // 생성자를 통한 초기값 설정
			super(a, b, c, 5);
		}

		void setMoveable() { // 퀸은 평행선으로, 대각선으로 원하는 만큼 이동할 수 있다. 단, 앞에 말이 가로막고 있으면 이동할 수 없고 상대 말이면 그 말을 잡을 수
								// 있다
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

	class King extends piece { // 킹 클래스 구현
		King(int a, int b, String c) { // 생성자를 통한 초기값 설정
			super(a, b, c, 6);
		}

		@Override
		void move(int a, int b) { // 캐슬링 규정을 위한 Overriding. 킹이 한 번 움직이면 더 이상 캐슬링 할 수 없다
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

		void setMoveable() { // 킹은 자신 주변 1칸으로 이동할 수 있다
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
		makeGUI(); // GUI 만들기
		initGame(); // 게임 초기화
	}

	void makeGUI() { // GUI 제작
		setSize(w, h); // Frame 크기 설정

		Panel controls = new Panel(); // 버튼들이 위치할 panel
		Panel labels = new Panel(); // 라벨들이 위치할 panel
		add(controls, BorderLayout.SOUTH); // 버튼들은 아래쪽
		add(labels, BorderLayout.NORTH); // 라벨들은 위쪽

		Font font = new Font("Arial", Font.PLAIN, 20); // 폰트 설정
		try (BufferedReader br = new BufferedReader(new FileReader("windata.txt"))) {
			String l;
			while ((l = br.readLine()) != null)
				windata += (l + "\r\n");
		} catch (IOException e) {
		}

		lLastMove = new Label("Last Move:                  "); // 초기 텍스트 설정
		lWhiteTimer = new Label("White:                    ");
		lBlackTimer = new Label("Black:                    ");
		lwindata = new JTextArea(windata);
		lLastMove.setSize(new Dimension(200, 50)); // 크기 설정
		lWhiteTimer.setSize(new Dimension(200, 50));
		lBlackTimer.setSize(new Dimension(200, 50));
		lwindata.setSize(new Dimension(20,20));
		lLastMove.setFont(font); // 폰트 설정
		lWhiteTimer.setFont(font);
		lBlackTimer.setFont(font);
		lwindata.setFont(font);

		bUndo = new JButton("Undo"); // 되돌리기 Button
		bForfeit = new JButton("Forfeit"); // 항복 Button
		bDraw = new JButton("Draw"); // 무승부 Button
		bStart = new JButton("Start New Game"); // 게임 시작 버튼
		bSettime = new JButton("Time Settings"); // 시간 제한 설정 버튼
		bSetundo = new JButton("Undo Settings"); // 바로가기 유무 조절
		bUndo.setPreferredSize(new Dimension(150, 50)); // 크기 설정
		bUndo.setFont(font); // 폰트 설정
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

		labels.add(lLastMove); // panel에 추가
		labels.add(lWhiteTimer);
		labels.add(lBlackTimer);
		controls.add(bUndo);
		controls.add(bForfeit);
		controls.add(bDraw);
		controls.add(bStart);
		controls.add(bSettime);
		controls.add(bSetundo);

		Panel board = new Panel(); // 체스판을 나타낼 panel
		add(board); // Frame에 추가
		add(lwindata, BorderLayout.EAST);
		board.setLayout(null);
		

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				bBoard[i][j] = new JButton(); // 체스판 한 칸
				bBoard[i][j].setSize(sql, sql); // 한 칸의 크기 설정
				bBoard[i][j].setLocation(j * sql + 240, i * sql + 10); // 한 칸의 위치 설정
				board.add(bBoard[i][j]); // board panel에 추가

				bBoard[i][j].addActionListener(new ClickListener(i, j)); // Listener 추가
			}
		}

		setVisible(true); // 보이기

		addWindowListener(new MyWindowAdapter()); // 창닫기 버튼

		timer = new Timer(1000, new ActionListener() { // 타이머 설정. 1초마다 시간 1씩 감소
			@Override
			public void actionPerformed(ActionEvent e) {
				if (turn == "white")
					wtime--;
				else
					btime--;
				updateTime();
			}
		});

		bStart.addActionListener(new ActionListener() { // 다시하기 버튼 설정
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Restart?");
				label.setFont(font);
				int restart = JOptionPane.showConfirmDialog(null, label); // 다시 할 지 물어보는 JOptionPane 띄우기
				if (restart == 0) {
					initGame(); // 재시작
				}
			}
		});

		bForfeit.addActionListener(new ActionListener() { // 항복 버튼 설정
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("ForFeit?");
				label.setFont(font);
				int forfeit = JOptionPane.showConfirmDialog(null, label); // 항복할 지 물어보는 JOptionPane 띄우기
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
					JOptionPane.showMessageDialog(null, forfeitLabel, "Game Over !!", JOptionPane.INFORMATION_MESSAGE); // 게임이
																														// 끝났다고
																														// 알림
					initGame(); // 재시작
				}
			}
		});

		bUndo.addActionListener(new ActionListener() { // 뒤로가기 버튼 설정
			@Override
			public void actionPerformed(ActionEvent e) {
				if (booleanundo)
					undo(); // 뒤로가기가 가능한 상태이면 뒤로가기
			}
		});

		bDraw.addActionListener(new ActionListener() { // 무승부 버튼 설정
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Accept Draw?");
				label.setFont(font);
				int draw = JOptionPane.showConfirmDialog(null, label); // 무승부를 받아들일 것인지 물어봄
				if (draw == 0) {
					label.setText("Draw!!");
					windata += "Draw            \r\n";
					JOptionPane.showMessageDialog(null, label, "Draw", JOptionPane.INFORMATION_MESSAGE); // 무승부임을 나타냄
					initGame(); // 재시작
				} else if (draw == 1) {
					label.setText("Draw declined");
					JOptionPane.showMessageDialog(null, label, "Draw declined", JOptionPane.INFORMATION_MESSAGE); // 무승부가
																													// 아님을
																													// 나타냄
				} else {
				}
			}
		});

		bSettime.addActionListener(new ActionListener() { // 시간 설정
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Maximum time in seconds: "); // 최대 시간을 초 단위로 받음
				label.setFont(font);
				String s = (String) JOptionPane.showInputDialog(null, label, "Set time", JOptionPane.PLAIN_MESSAGE,
						null, null, "");
				try { // 정수가 아닌 값을 입력했을 때를 위해
					MaxTime = Integer.parseInt(s);
				} catch (Exception e1) {
				}
				label.setText("Increment in seconds: "); // increment(매 수마다 시간 증가분)을 초 단위로 받음
				s = (String) JOptionPane.showInputDialog(null, label, "Set increment", JOptionPane.PLAIN_MESSAGE, null,
						null, "");
				try {
					Inc = Integer.parseInt(s);
				} catch (Exception e2) {
				}
			}
		});

		bSetundo.addActionListener(new ActionListener() { // 뒤로가기 설정 버튼 설정
			@Override
			public void actionPerformed(ActionEvent e) {
				Label label = new Label("Allow undo?"); // 뒤로가기를 활성화시키고싶은지 물어봄
				label.setFont(font);
				int undo = JOptionPane.showConfirmDialog(null, label);
				if (undo == 0) {
					label.setText("Undo activated");
					JOptionPane.showMessageDialog(null, label, "Undo settings", JOptionPane.INFORMATION_MESSAGE); // 활성화
																													// 알림
					booleanundo = true;
				} else if (undo == 1) {
					label.setText("Undo inactivated");
					JOptionPane.showMessageDialog(null, label, "Undo settings", JOptionPane.INFORMATION_MESSAGE); // 비활성화
																													// 알림
					booleanundo = false;
				} else {
				}
			}
		});
	}

	boolean checkCheck(String s) { // 특정 색에 대해 체크인지 확인하는 메소드
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] != null)
					if (board[i][j].color == s) {
						board[i][j].setMoveable();
						for (int k = 0; k < 8; k++) {
							for (int l = 0; l < 8; l++) {
								if (Moveable[k][l] && board[k][l] != null)
									if (board[k][l].name == "king")
										return true; // 움직일 수 있고 그 자리에 킹이 있으면 true 반환
							}
						}
					}
			}
		}
		return false; // 없으면 false 반환
	}

	void updateLastMove(String s) {
		lLastMove.setText("Last Move: " + s); // 마지막 수 라벨 업데이트
	}

	void updateTime() { // 시간 업데이트
		lWhiteTimer.setText("White: " + wtime / 60 + " min " + wtime % 60 + " sec"); // 남은 시간 표시
		lBlackTimer.setText("Black: " + btime / 60 + " min " + btime % 60 + " sec");
		if (wtime == 0 || btime == 0) { // 한 쪽 시간이 다 되었을 때
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
			JOptionPane.showMessageDialog(null, label, "Game Over!", JOptionPane.INFORMATION_MESSAGE); // 게임 끝났다고 표시
			initGame(); // 재시작
		}
	}

	void undo() { // 뒤로가기 메소드
		if (nmoves >= 1) { // 이미 한 번 움직인 상태에서만 뒤로가기 가능
			bstate[nmoves--] = null; // bstate 배열에 저장된 내용을 가져오기
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
			for (int i = 0; i < 8; i++) { // 아이콘 업데이트
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

	void initGame() { // 게임 시작 메소드

		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				board[i][j] = null; // 모든 값을 초기화
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

		for (int i = 0; i < 8; i++) { // 폰 초기 위치 설정
			board[1][i] = new Pawn(1, i, "black");
			board[6][i] = new Pawn(6, i, "white");
		}
		board[0][0] = new Rook(0, 0, "black");
		board[7][0] = new Rook(7, 0, "white");
		board[0][7] = new Rook(0, 7, "black");
		board[7][7] = new Rook(7, 7, "white"); // 룩
		board[0][1] = new Knight(0, 1, "black");
		board[7][1] = new Knight(7, 1, "white");
		board[0][6] = new Knight(0, 6, "black");
		board[7][6] = new Knight(7, 6, "white"); // 나이트
		board[0][2] = new Bishop(0, 2, "black");
		board[7][2] = new Bishop(7, 2, "white");
		board[0][5] = new Bishop(0, 5, "black");
		board[7][5] = new Bishop(7, 5, "white"); // 비숍
		board[0][3] = new Queen(0, 3, "black");
		board[0][4] = new King(0, 4, "black"); // 흑색 킹&퀸
		board[7][3] = new Queen(7, 3, "white");
		board[7][4] = new King(7, 4, "white"); // 백색 킹&퀸

		for (int i = 0; i < 8; i++) { // 아이콘 설정
			for (int j = 0; j < 8; j++) {
				if (board[i][j] == null && (i + j) % 2 == 0)
					bBoard[i][j].setIcon(icon_light);
				else if (board[i][j] == null && (i + j) % 2 == 1)
					bBoard[i][j].setIcon(icon_dark);
				else
					bBoard[i][j].setIcon(board[i][j].Icon);
			}
		}

		timer.start(); // 타이머 시작
	}

	void promote(int a, int b, String s, String c) { // 프로모션 메소드
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

		ClickListener(int i, int j) { // 생성자로 내용물 설정
			this.i = i;
			this.j = j;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (onClick[i][j]) { // 같은 곳을 두 번 클릭하면
				onClick[i][j] = false; // 클릭하지 않은 상태로 돌아감
				firstClick = true;
				bBoard[i][j].setIcon(board[i][j].Icon);
			} else if (firstClick == true) { // 첫 번째 클릭이면
				if (board[i][j] != null) { // 말이 위에 있는 경우에만
					if (board[i][j].color == turn) { // 차례가 맞는 경우에만
						firstClick = false; // 다음 클릭은 두 번째 클릭
						onClick[i][j] = true; // 이미 클릭했다고 표기
						ci = i; // 클릭한 값 저장
						cj = j;
						board[i][j].setMoveable(); // 움직일 수 있는 범위 저장
						bBoard[i][j].setIcon(board[i][j].clickIcon); // 클릭했다고 아이콘 변경
					}
				}
			} else if (Moveable[i][j] == true) { // 두 번째 클릭에서 움직일 수 있는 공간을 택하면
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						bstate[nmoves].bqc = bqc; // 현재 상태를 bstate 배열에 저장
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
						nmoves++; // 한 수 움직임

						if (board[ci][cj].name == "king") { // 캐슬링 관련 처리(한 번에 두 개의 말을 이동하므로)
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

						board[i][j] = board[ci][cj]; // 말 클래스 이동
						board[ci][cj] = null; // 기존에 있던 자리는 null
						board[i][j].move(i, j); // 클래스 내부 값 변경

						bBoard[i][j].setIcon(board[i][j].Icon); // 아이콘 변경
						if ((ci + cj) % 2 == 0)
							bBoard[ci][cj].setIcon(icon_light);
						else
							bBoard[ci][cj].setIcon(icon_dark);
						firstClick = true; // 클릭하지 않은 상태로 돌아감
						onClick[ci][cj] = false;
						onClick[i][j] = false;

						updateLastMove(NumtoAl[cj] + (8 - ci) + " to " + NumtoAl[j] + (8 - i)); // 마지막 수 업데이트

						if (checkCheck(turn)) { // 만약 체크이면
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

						bstate[nmoves] = new BoardState(); // 새 BoardState를 만들어서
						bstate[nmoves].lastmove = NumtoAl[cj] + (8 - ci) + " to " + NumtoAl[j] + (8 - i); // 이전 수 저장

						if (checkCheck(turn)) { // 만약 자신의 킹이 체크 상태가 되면 잘못된 수
							Label illegal = new Label("Illegal Move!");
							illegal.setFont(new Font("Arial", Font.PLAIN, 20));
							JOptionPane.showMessageDialog(null, illegal, "Illegal Move!!!",
									JOptionPane.INFORMATION_MESSAGE); // 잘못된 수임을 알려줌
							undo(); // 뒤로가기
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