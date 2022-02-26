import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class GameManager extends JFrame {
	private Image bufferImage; // 깜빡임을 없애기 위해
	private Graphics screenGraphic; // 더블 버퍼링 기법 사용

	private Image mainScreen = new ImageIcon(getClass().getClassLoader().getResource("main_screen.png")).getImage();
	private Image loadingScreen;
	private Image gameScreen = new ImageIcon(getClass().getClassLoader().getResource("game_screen.png")).getImage();
	private Image gameBossScreen = Toolkit.getDefaultToolkit().createImage(getClass().getClassLoader().getResource("game_screen_2.gif"));

	private boolean isMainScreen, isLoadingScreen, isGameScreen, isEndingScreen; // 화면 컨트롤

	private Game game;
	private DBManager dbManager = new DBManager();
	private SoundManager backgroundSoundManager = new SoundManager(-2);
	private SoundManager effectSoundManager = new SoundManager(2);

	private int weapon, difficulty;

	private boolean isOpened; // 입력창이 한 번만 열리도록

	private boolean isCountDown = false; // pause 관련
	private int countdown;

	public GameManager() {
		// 메인 화면
		isMainScreen = true;
		isLoadingScreen = isGameScreen = isEndingScreen = false;

		addKeyListener(new KeyListener());
		addMouseListener(new MyMouseListener());

		backgroundSoundManager.play("main_screen.wav", true); // 메인 화면 사운드

		setUndecorated(true);
		setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void gameStart() {
		// 로딩 화면
		loadingScreen = Toolkit.getDefaultToolkit().createImage(getClass().getClassLoader().getResource("loading_screen.gif"));
		game = new Game(weapon, difficulty); // 쓰레드는 한 번 종료하면 재시작 불가
		isMainScreen = false;
		isLoadingScreen = true;
		isOpened = false;

		backgroundSoundManager.stop();
		backgroundSoundManager.play("loading_screen.wav", true); // 로딩 화면 사운드

		Timer loadingTimer = new Timer(); // 타이머
		TimerTask loadingTask = new TimerTask() {
			@Override
			public void run() {
				// 게임 화면
				isLoadingScreen = false;
				isGameScreen = true;
				game.start(); // Game 클래스의 쓰레드 시작
				backgroundSoundManager.stop();
				backgroundSoundManager.play("game_screen.wav", true); // 게임 화면 사운드
			}
		};
		loadingTimer.schedule(loadingTask, 6000); // 6초 후
	}

	// 게임 종료
	private void gameStop() {
		// 메인 화면
		isMainScreen = true;
		isEndingScreen = false;
		game.interrupt(); // Game 클래스의 쓰레드 인터럽트
	}

	@Override
	public void paint(Graphics g) {
		bufferImage = createImage(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		screenGraphic = bufferImage.getGraphics();
		super.paintComponents(screenGraphic);
		screenDraw(screenGraphic);
		g.drawImage(bufferImage, 0, 0, null);
	}

	public void screenDraw(Graphics g) {
		if (isMainScreen) { // 메인 화면
			g.drawImage(mainScreen, 0, 0, null);
		} else if (isLoadingScreen) { // 로딩 화면
			g.drawImage(loadingScreen, 0, 0, 1280, 720, null);
		} else if (isGameScreen) { // 게임 화면
			if (game.getCount() < game.getBossAppearTime()) { // 보스 등장 전
				g.drawImage(gameScreen, game.getX1(), 0, null);
				g.drawImage(gameScreen, game.getX2(), 0, null);
			} else { // 보스 등장 후
				if (game.getCount() == game.getBossAppearTime()) {
					if (game.getIsMusicOn()) {
						backgroundSoundManager.stop();
						backgroundSoundManager.play("game_boss_screen.wav", true); // 게임 보스 화면 사운드
					}
				}
				g.drawImage(gameBossScreen, 0, 0, 1280, 720, null);
			}
			game.gameDraw(g);
			// 일시정지 화면 카운트다운
			if (isCountDown) {
				g.setColor(Color.BLACK);
				g.setFont(new Font("둥근모꼴", Font.BOLD, 300));
				g.drawString(Integer.toString(countdown), Main.SCREEN_WIDTH / 2 - 70, Main.SCREEN_HEIGHT / 2 + 70);
				g.setColor(Color.WHITE);
				g.setFont(new Font("둥근모꼴", Font.BOLD, 300));
				g.drawString(Integer.toString(countdown), Main.SCREEN_WIDTH / 2 - 90, Main.SCREEN_HEIGHT / 2 + 70);
			}
			if (game.getIsGameOver()) { // 게임이 끝난 경우
				isGameScreen = false;
				isEndingScreen = true;
			}
		}
		if (isEndingScreen) { // 엔딩 화면
			if (!isOpened) { // 한 번만 열리도록
				backgroundSoundManager.stop();
				backgroundSoundManager.play("ending_screen.wav", true); // 엔딩 사운드
				if (game.getIsVictory()) // 승리한 경우
					effectSoundManager.play("victory.wav", false); // 승리 사운드
				else // 패배한 경우
					effectSoundManager.play("defeat.wav", false); // 패배 사운드
				new InsertDialog(); // 점수 기록 창
				isOpened = true;
			}
			if (game.getIsVictory()) // 승리한 경우
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("ending_screen_0.png")).getImage(), 0, 0, null);
			else // 패배한 경우
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("ending_screen_1.png")).getImage(), 0, 0, null);
		}
		repaint();
	}

	// 키보드 이벤트
	class KeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				game.setUp(true);
				break;
			case KeyEvent.VK_DOWN:
				game.setDown(true);
				break;
			case KeyEvent.VK_LEFT:
				game.setLeft(true);
				break;
			case KeyEvent.VK_RIGHT:
				game.setRight(true);
				break;
			case KeyEvent.VK_X:
				if (!game.getIsPaused())
					game.setAttack(true);
				break;
			case KeyEvent.VK_Z:
				if (!game.getIsPressed()) {
					game.setUseItem(true);
					game.setIsPressed(true);
				}
				break;
			case KeyEvent.VK_ESCAPE:
				if (isGameScreen) {
					boolean pause = game.getIsPaused();
					if (pause && !isCountDown) {
						countdown = 3;
						isCountDown = true;
						game.setIsCountdown(isCountDown);
						Timer timer = new Timer();
						TimerTask task = new TimerTask() {
							@Override
							public void run() {
								if (countdown > 1) { // 3, 2, 1
									countdown--;
								} else {
									game.setIsPaused(!pause);
									isCountDown = false;
									game.setIsCountdown(isCountDown);
									timer.cancel(); // 타이머 종료
									backgroundSoundManager.replay(); // 멈춘 곳에서 음악 다시 재생
								}
							}
						};
						timer.schedule(task, 1000, 1000); // 실행 Task, 1초뒤 실행, 1초마다 반복
						effectSoundManager.play("pause_out.wav", false); // 일시정지 취소 사운드
					} else if (!pause && !isCountDown) {
						game.setIsPaused(!pause);
						backgroundSoundManager.pause(); // 음악 일시정지
						effectSoundManager.play("pause_in.wav", false); // 일시정지 사운드
					}
				}
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				game.setUp(false);
				break;
			case KeyEvent.VK_DOWN:
				game.setDown(false);
				break;
			case KeyEvent.VK_LEFT:
				game.setLeft(false);
				break;
			case KeyEvent.VK_RIGHT:
				game.setRight(false);
				break;
			case KeyEvent.VK_X:
				game.setAttack(false);
				break;
			case KeyEvent.VK_Z:
				game.setIsPressed(false);
				break;
			}
		}
	}

	// 마우스 클릭 이벤트
	class MyMouseListener implements MouseListener {
		String[] button = { "취소", "확인" };

		@Override
		public void mouseClicked(MouseEvent e) {
			if (isMainScreen) { // 메인 화면
				if (e.getX() >= 250 && e.getX() <= 1020 && e.getY() >= 300 && e.getY() <= 410) { // 게임 시작
					effectSoundManager.play("button.wav", false);
					new MenuDialog();
				}
				if (e.getX() >= 250 && e.getX() <= 620 && e.getY() >= 440 && e.getY() <= 530) { // 게임 순위
					effectSoundManager.play("button.wav", false);
					new RankingDialog();
				}
				if (e.getX() >= 660 && e.getX() <= 1020 && e.getY() >= 440 && e.getY() <= 530) { // 게임 종료
					effectSoundManager.play("button.wav", false);
					UIManager.put("OptionPane.messageFont", new Font("둥근모꼴", Font.PLAIN, 15));
					UIManager.put("OptionPane.buttonFont", new Font("둥근모꼴", Font.PLAIN, 15));
					if (JOptionPane.showOptionDialog(null, "게임을 종료하시겠습니까?", "게임 종료", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, button, "취소") == 1)
						System.exit(0);
				}
			}
			if (isGameScreen) { // 게임 화면
				if (e.getX() >= 1210 && e.getX() <= 1240 && e.getY() >= 30 && e.getY() <= 60) { // 일시정지 아이콘 클릭 시
					boolean pause = game.getIsPaused();
					if (pause && !isCountDown) {
						countdown = 3;
						isCountDown = true;
						game.setIsCountdown(isCountDown);
						Timer timer = new Timer();
						TimerTask task = new TimerTask() {
							@Override
							public void run() {
								if (countdown > 1) { // 3, 2, 1
									countdown--;
								} else {
									game.setIsPaused(!pause);
									isCountDown = false;
									game.setIsCountdown(isCountDown);
									timer.cancel(); // 타이머 종료
									backgroundSoundManager.replay(); // 멈춘 곳에서 음악 다시 재생
								}
							}
						};
						timer.schedule(task, 1000, 1000); // 실행 Task, 1초뒤 실행, 1초마다 반복
						effectSoundManager.play("pause_out.wav", false); // 일시정지 취소 사운드
					} else if (!pause && !isCountDown) {
						game.setIsPaused(!pause);
						backgroundSoundManager.pause(); // 음악 일시정지
						effectSoundManager.play("pause_in.wav", false); // 일시정지 사운드
					}
				}
				if (e.getX() >= 1110 && e.getX() <= 1140 && e.getY() >= 30 && e.getY() <= 60) { // 음악 재생 아이콘 클릭 시
					if (game.getIsMusicOn()) // 음악 ON인 경우
						backgroundSoundManager.stop();
					else { // 음악 OFF인 경우
						if (game.getCount() < game.getBossAppearTime())
							backgroundSoundManager.play("game_screen.wav", true); // 게임 화면 사운드
						else
							backgroundSoundManager.play("game_boss_screen.wav", true); // 게임 보스 화면 사운드
					}
					game.setIsMusicOn(!game.getIsMusicOn());
				}
				if (game.getIsPaused()) { // 일시정지인 경우
					if (e.getX() >= 610 && e.getX() <= 670 && e.getY() >= 370 && e.getY() <= 430) { // 메인 화면 아이콘 클릭 시
						backgroundSoundManager.play("main_screen.wav", true); // 메인 화면 사운드
						gameStop(); // 게임 종료
					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	// 게임 시작 설정 다이얼로그
	class MenuDialog extends JDialog {
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) { // 그리는 함수
				super.paintComponent(g);
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("menu_background.png")).getImage(), 0, 0, 500, 500, this);
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("menu_decorate.png")).getImage(), 0, 0, 500, 500, this);
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("menu_frame.png")).getImage(), 0, 0, 500, 500, this);
			}
		};
		JLabel menuLabel = new JLabel("게임 설정");
		JLabel weaponLabel = new JLabel("<< 무기 선택 >>");
		JLabel weaponImageLabel = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("player_0.png")));
		JLabel difficultyLabel = new JLabel("<< 난이도 선택 >>");
		JLabel difficultyExplainLabel = new JLabel("어렵지 않게 즐길 수 있습니다!");
		JRadioButton fire = new JRadioButton("파이어");
		JRadioButton gun = new JRadioButton("총");
		JRadioButton easy = new JRadioButton("쉬움");
		JRadioButton hard = new JRadioButton("어려움");
		JButton startButton = new JButton("게임 시작");

		public MenuDialog() {
			panel.setLayout(new GridBagLayout());

			menuLabel.setFont(new Font("둥근모꼴", Font.BOLD, 25));
			weaponLabel.setFont(new Font("둥근모꼴", Font.BOLD, 25));
			difficultyLabel.setFont(new Font("둥근모꼴", Font.BOLD, 25));
			difficultyExplainLabel.setFont(new Font("둥근모꼴", Font.PLAIN, 15));
			fire.setFont(new Font("둥근모꼴", Font.PLAIN, 15));
			gun.setFont(new Font("둥근모꼴", Font.PLAIN, 15));
			easy.setFont(new Font("둥근모꼴", Font.PLAIN, 15));
			hard.setFont(new Font("둥근모꼴", Font.PLAIN, 15));
			startButton.setFont(new Font("둥근모꼴", Font.PLAIN, 15));
			startButton.setPreferredSize(new Dimension(100, 35));

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			gbc.weighty = 1;
			panel.add(menuLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			gbc.weighty = 1;
			panel.add(weaponLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			panel.add(fire, gbc);

			gbc.gridx = 1;
			gbc.gridy = 2;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			panel.add(gun, gbc);

			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			panel.add(weaponImageLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 4;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			panel.add(difficultyLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 5;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			panel.add(easy, gbc);

			gbc.gridx = 1;
			gbc.gridy = 5;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			panel.add(hard, gbc);

			gbc.gridx = 0;
			gbc.gridy = 6;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			panel.add(difficultyExplainLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 7;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			panel.add(startButton, gbc);

			weapon = difficulty = 0;
			ButtonGroup weaponGroup = new ButtonGroup();
			weaponGroup.add(fire);
			weaponGroup.add(gun);
			fire.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (fire.isSelected()) {
						effectSoundManager.play("player_attack_0.wav", false);
						weaponImageLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("player_0.png")));
						if (weapon == 1)
							weapon = 0;
					}
				}
			});
			gun.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (gun.isSelected()) {
						effectSoundManager.play("player_attack_1.wav", false);
						weaponImageLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("player_1.png")));
						if (weapon == 0)
							weapon = 1;
					}
				}
			});
			fire.setSelected(true);
			ButtonGroup difficultyGroup = new ButtonGroup();
			difficultyGroup.add(easy);
			difficultyGroup.add(hard);
			easy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (easy.isSelected()) {
						effectSoundManager.play("button.wav", false);
						difficultyExplainLabel.setText("어렵지 않게 즐길 수 있습니다!");
						if (difficulty == 1)
							difficulty = 0;
					}
				}
			});
			hard.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					effectSoundManager.play("button.wav", false);
					if (hard.isSelected()) {
						difficultyExplainLabel.setText("보스의 공격이 더욱 거세집니다!");
						if (difficulty == 0)
							difficulty = 1;
					}
				}
			});
			easy.setSelected(true);
			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					effectSoundManager.play("button.wav", false);
					gameStart();
					dispose();
				}
			});

			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getX() >= 465 && e.getX() <= 485 && e.getY() >= 20 && e.getY() <= 50) {
						effectSoundManager.play("button.wav", false);
						dispose();
					}
				}
			});

			add(panel);
			setModal(true);
			setSize(500, 500);
			setLocationRelativeTo(null);
			setUndecorated(true);
			setVisible(true);
		}
	}

	// 게임 순위 다이얼로그
	class RankingDialog extends JDialog {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("BEST PUNCHER", JLabel.CENTER);
		String[] columnNames = { "RANK", "NAME", "SCORE", "DIFFICULTY" };
		Object data[][] = dbManager.Select();
		JTable table = new JTable(new DefaultTableModel(data, columnNames) { // 수정이 불가능하도록
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		JScrollPane scrollPane = new JScrollPane(table);
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		JButton button = new JButton("확인");

		public RankingDialog() {
			panel.setLayout(new BorderLayout());
			panel.add(label, BorderLayout.NORTH);
			panel.add(scrollPane, BorderLayout.CENTER);
			panel.add(button, BorderLayout.SOUTH);
			add(panel);

			label.setFont(new Font("둥근모꼴", Font.PLAIN, 50));
			label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

			dtcr.setHorizontalAlignment(SwingConstants.CENTER);
			table.setFont(new Font("둥근모꼴", Font.PLAIN, 20));
			table.getTableHeader().setFont(new Font("둥근모꼴", Font.PLAIN, 20));
			table.getTableHeader().setDefaultRenderer(dtcr);
			table.getColumn("RANK").setPreferredWidth(10);
			table.getColumn("RANK").setCellRenderer(dtcr);
			table.getColumn("NAME").setCellRenderer(dtcr);
			table.getColumn("SCORE").setCellRenderer(dtcr);
			table.getColumn("DIFFICULTY").setCellRenderer(dtcr);
			table.setRowHeight(40);
			table.getTableHeader().setReorderingAllowed(false); // 이동 불가
			table.getTableHeader().setResizingAllowed(false); // 크기 조절 불가

			button.setFont(new Font("둥근모꼴", Font.PLAIN, 20));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					effectSoundManager.play("button.wav", false);
					dispose();
				}
			});
			button.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

			setModal(true);
			setSize(530, 575);
			setUndecorated(true);
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}

	// 게임 점수 기록 다이얼로그
	class InsertDialog extends JDialog {
		String[] difficulty_levels = { "EASY", "HARD" };
		JLabel label = new JLabel("ENTER YOUR NAME!", JLabel.CENTER);
		JTextField textField = new JTextField();
		JButton button = new JButton("확인");

		public InsertDialog() {
			add(label);
			add(textField);
			add(button);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = textField.getText();
					if (!name.equals(""))
						dbManager.Insert(name, game.getScore(), difficulty_levels[difficulty]);
					else
						dbManager.Insert("???", game.getScore(), difficulty_levels[difficulty]);
					backgroundSoundManager.stop();
					effectSoundManager.stop();
					backgroundSoundManager.play("main_screen.wav", true);
					gameStop();
					dispose();
				}
			});
			
			label.setFont(new Font("둥근모꼴", Font.PLAIN, 30));
			textField.setHorizontalAlignment(JTextField.CENTER);
			textField.setFont(new Font("둥근모꼴", Font.PLAIN, 30));
			textField.setDocument((new JTextFieldLimit(3))); // 입력 글자 수 제한
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					char ch = e.getKeyChar();
					if (Character.isLetter(ch) || Character.isISOControl(ch)) {
						textField.setEditable(true);
					} else {
						textField.setEditable(false);
					}
				}
			});
			label.requestFocus(false);
			button.setFont(new Font("둥근모꼴", Font.PLAIN, 20));
			button.requestFocus(true);

			setLayout(new GridLayout(3, 1));
			setSize(400, 200);
			setUndecorated(true);
			setLocation((Main.SCREEN_WIDTH - this.getWidth()) / 2, Main.SCREEN_HEIGHT - this.getHeight() - 20);
			setVisible(true);
		}
	}

	// 입력 제한
	class JTextFieldLimit extends PlainDocument {
		private int limit;

		JTextFieldLimit(int limit) {
			super();
			this.limit = limit;
		}

		@Override
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			if (str == null)
				return;
			if ((getLength() + str.length()) <= limit) {
				super.insertString(offset, str.toUpperCase(), attr);
			}
		}
	}
}
