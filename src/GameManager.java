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
	private Image bufferImage; // �������� ���ֱ� ����
	private Graphics screenGraphic; // ���� ���۸� ��� ���

	private Image mainScreen = new ImageIcon(getClass().getClassLoader().getResource("main_screen.png")).getImage();
	private Image loadingScreen;
	private Image gameScreen = new ImageIcon(getClass().getClassLoader().getResource("game_screen.png")).getImage();
	private Image gameBossScreen = Toolkit.getDefaultToolkit().createImage(getClass().getClassLoader().getResource("game_screen_2.gif"));

	private boolean isMainScreen, isLoadingScreen, isGameScreen, isEndingScreen; // ȭ�� ��Ʈ��

	private Game game;
	private DBManager dbManager = new DBManager();
	private SoundManager backgroundSoundManager = new SoundManager(-2);
	private SoundManager effectSoundManager = new SoundManager(2);

	private int weapon, difficulty;

	private boolean isOpened; // �Է�â�� �� ���� ��������

	private boolean isCountDown = false; // pause ����
	private int countdown;

	public GameManager() {
		// ���� ȭ��
		isMainScreen = true;
		isLoadingScreen = isGameScreen = isEndingScreen = false;

		addKeyListener(new KeyListener());
		addMouseListener(new MyMouseListener());

		backgroundSoundManager.play("main_screen.wav", true); // ���� ȭ�� ����

		setUndecorated(true);
		setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void gameStart() {
		// �ε� ȭ��
		loadingScreen = Toolkit.getDefaultToolkit().createImage(getClass().getClassLoader().getResource("loading_screen.gif"));
		game = new Game(weapon, difficulty); // ������� �� �� �����ϸ� ����� �Ұ�
		isMainScreen = false;
		isLoadingScreen = true;
		isOpened = false;

		backgroundSoundManager.stop();
		backgroundSoundManager.play("loading_screen.wav", true); // �ε� ȭ�� ����

		Timer loadingTimer = new Timer(); // Ÿ�̸�
		TimerTask loadingTask = new TimerTask() {
			@Override
			public void run() {
				// ���� ȭ��
				isLoadingScreen = false;
				isGameScreen = true;
				game.start(); // Game Ŭ������ ������ ����
				backgroundSoundManager.stop();
				backgroundSoundManager.play("game_screen.wav", true); // ���� ȭ�� ����
			}
		};
		loadingTimer.schedule(loadingTask, 6000); // 6�� ��
	}

	// ���� ����
	private void gameStop() {
		// ���� ȭ��
		isMainScreen = true;
		isEndingScreen = false;
		game.interrupt(); // Game Ŭ������ ������ ���ͷ�Ʈ
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
		if (isMainScreen) { // ���� ȭ��
			g.drawImage(mainScreen, 0, 0, null);
		} else if (isLoadingScreen) { // �ε� ȭ��
			g.drawImage(loadingScreen, 0, 0, 1280, 720, null);
		} else if (isGameScreen) { // ���� ȭ��
			if (game.getCount() < game.getBossAppearTime()) { // ���� ���� ��
				g.drawImage(gameScreen, game.getX1(), 0, null);
				g.drawImage(gameScreen, game.getX2(), 0, null);
			} else { // ���� ���� ��
				if (game.getCount() == game.getBossAppearTime()) {
					if (game.getIsMusicOn()) {
						backgroundSoundManager.stop();
						backgroundSoundManager.play("game_boss_screen.wav", true); // ���� ���� ȭ�� ����
					}
				}
				g.drawImage(gameBossScreen, 0, 0, 1280, 720, null);
			}
			game.gameDraw(g);
			// �Ͻ����� ȭ�� ī��Ʈ�ٿ�
			if (isCountDown) {
				g.setColor(Color.BLACK);
				g.setFont(new Font("�ձٸ��", Font.BOLD, 300));
				g.drawString(Integer.toString(countdown), Main.SCREEN_WIDTH / 2 - 70, Main.SCREEN_HEIGHT / 2 + 70);
				g.setColor(Color.WHITE);
				g.setFont(new Font("�ձٸ��", Font.BOLD, 300));
				g.drawString(Integer.toString(countdown), Main.SCREEN_WIDTH / 2 - 90, Main.SCREEN_HEIGHT / 2 + 70);
			}
			if (game.getIsGameOver()) { // ������ ���� ���
				isGameScreen = false;
				isEndingScreen = true;
			}
		}
		if (isEndingScreen) { // ���� ȭ��
			if (!isOpened) { // �� ���� ��������
				backgroundSoundManager.stop();
				backgroundSoundManager.play("ending_screen.wav", true); // ���� ����
				if (game.getIsVictory()) // �¸��� ���
					effectSoundManager.play("victory.wav", false); // �¸� ����
				else // �й��� ���
					effectSoundManager.play("defeat.wav", false); // �й� ����
				new InsertDialog(); // ���� ��� â
				isOpened = true;
			}
			if (game.getIsVictory()) // �¸��� ���
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("ending_screen_0.png")).getImage(), 0, 0, null);
			else // �й��� ���
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("ending_screen_1.png")).getImage(), 0, 0, null);
		}
		repaint();
	}

	// Ű���� �̺�Ʈ
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
									timer.cancel(); // Ÿ�̸� ����
									backgroundSoundManager.replay(); // ���� ������ ���� �ٽ� ���
								}
							}
						};
						timer.schedule(task, 1000, 1000); // ���� Task, 1�ʵ� ����, 1�ʸ��� �ݺ�
						effectSoundManager.play("pause_out.wav", false); // �Ͻ����� ��� ����
					} else if (!pause && !isCountDown) {
						game.setIsPaused(!pause);
						backgroundSoundManager.pause(); // ���� �Ͻ�����
						effectSoundManager.play("pause_in.wav", false); // �Ͻ����� ����
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

	// ���콺 Ŭ�� �̺�Ʈ
	class MyMouseListener implements MouseListener {
		String[] button = { "���", "Ȯ��" };

		@Override
		public void mouseClicked(MouseEvent e) {
			if (isMainScreen) { // ���� ȭ��
				if (e.getX() >= 250 && e.getX() <= 1020 && e.getY() >= 300 && e.getY() <= 410) { // ���� ����
					effectSoundManager.play("button.wav", false);
					new MenuDialog();
				}
				if (e.getX() >= 250 && e.getX() <= 620 && e.getY() >= 440 && e.getY() <= 530) { // ���� ����
					effectSoundManager.play("button.wav", false);
					new RankingDialog();
				}
				if (e.getX() >= 660 && e.getX() <= 1020 && e.getY() >= 440 && e.getY() <= 530) { // ���� ����
					effectSoundManager.play("button.wav", false);
					UIManager.put("OptionPane.messageFont", new Font("�ձٸ��", Font.PLAIN, 15));
					UIManager.put("OptionPane.buttonFont", new Font("�ձٸ��", Font.PLAIN, 15));
					if (JOptionPane.showOptionDialog(null, "������ �����Ͻðڽ��ϱ�?", "���� ����", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, button, "���") == 1)
						System.exit(0);
				}
			}
			if (isGameScreen) { // ���� ȭ��
				if (e.getX() >= 1210 && e.getX() <= 1240 && e.getY() >= 30 && e.getY() <= 60) { // �Ͻ����� ������ Ŭ�� ��
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
									timer.cancel(); // Ÿ�̸� ����
									backgroundSoundManager.replay(); // ���� ������ ���� �ٽ� ���
								}
							}
						};
						timer.schedule(task, 1000, 1000); // ���� Task, 1�ʵ� ����, 1�ʸ��� �ݺ�
						effectSoundManager.play("pause_out.wav", false); // �Ͻ����� ��� ����
					} else if (!pause && !isCountDown) {
						game.setIsPaused(!pause);
						backgroundSoundManager.pause(); // ���� �Ͻ�����
						effectSoundManager.play("pause_in.wav", false); // �Ͻ����� ����
					}
				}
				if (e.getX() >= 1110 && e.getX() <= 1140 && e.getY() >= 30 && e.getY() <= 60) { // ���� ��� ������ Ŭ�� ��
					if (game.getIsMusicOn()) // ���� ON�� ���
						backgroundSoundManager.stop();
					else { // ���� OFF�� ���
						if (game.getCount() < game.getBossAppearTime())
							backgroundSoundManager.play("game_screen.wav", true); // ���� ȭ�� ����
						else
							backgroundSoundManager.play("game_boss_screen.wav", true); // ���� ���� ȭ�� ����
					}
					game.setIsMusicOn(!game.getIsMusicOn());
				}
				if (game.getIsPaused()) { // �Ͻ������� ���
					if (e.getX() >= 610 && e.getX() <= 670 && e.getY() >= 370 && e.getY() <= 430) { // ���� ȭ�� ������ Ŭ�� ��
						backgroundSoundManager.play("main_screen.wav", true); // ���� ȭ�� ����
						gameStop(); // ���� ����
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

	// ���� ���� ���� ���̾�α�
	class MenuDialog extends JDialog {
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) { // �׸��� �Լ�
				super.paintComponent(g);
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("menu_background.png")).getImage(), 0, 0, 500, 500, this);
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("menu_decorate.png")).getImage(), 0, 0, 500, 500, this);
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("menu_frame.png")).getImage(), 0, 0, 500, 500, this);
			}
		};
		JLabel menuLabel = new JLabel("���� ����");
		JLabel weaponLabel = new JLabel("<< ���� ���� >>");
		JLabel weaponImageLabel = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("player_0.png")));
		JLabel difficultyLabel = new JLabel("<< ���̵� ���� >>");
		JLabel difficultyExplainLabel = new JLabel("����� �ʰ� ��� �� �ֽ��ϴ�!");
		JRadioButton fire = new JRadioButton("���̾�");
		JRadioButton gun = new JRadioButton("��");
		JRadioButton easy = new JRadioButton("����");
		JRadioButton hard = new JRadioButton("�����");
		JButton startButton = new JButton("���� ����");

		public MenuDialog() {
			panel.setLayout(new GridBagLayout());

			menuLabel.setFont(new Font("�ձٸ��", Font.BOLD, 25));
			weaponLabel.setFont(new Font("�ձٸ��", Font.BOLD, 25));
			difficultyLabel.setFont(new Font("�ձٸ��", Font.BOLD, 25));
			difficultyExplainLabel.setFont(new Font("�ձٸ��", Font.PLAIN, 15));
			fire.setFont(new Font("�ձٸ��", Font.PLAIN, 15));
			gun.setFont(new Font("�ձٸ��", Font.PLAIN, 15));
			easy.setFont(new Font("�ձٸ��", Font.PLAIN, 15));
			hard.setFont(new Font("�ձٸ��", Font.PLAIN, 15));
			startButton.setFont(new Font("�ձٸ��", Font.PLAIN, 15));
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
						difficultyExplainLabel.setText("����� �ʰ� ��� �� �ֽ��ϴ�!");
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
						difficultyExplainLabel.setText("������ ������ ���� �ż����ϴ�!");
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

	// ���� ���� ���̾�α�
	class RankingDialog extends JDialog {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("BEST PUNCHER", JLabel.CENTER);
		String[] columnNames = { "RANK", "NAME", "SCORE", "DIFFICULTY" };
		Object data[][] = dbManager.Select();
		JTable table = new JTable(new DefaultTableModel(data, columnNames) { // ������ �Ұ����ϵ���
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		JScrollPane scrollPane = new JScrollPane(table);
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		JButton button = new JButton("Ȯ��");

		public RankingDialog() {
			panel.setLayout(new BorderLayout());
			panel.add(label, BorderLayout.NORTH);
			panel.add(scrollPane, BorderLayout.CENTER);
			panel.add(button, BorderLayout.SOUTH);
			add(panel);

			label.setFont(new Font("�ձٸ��", Font.PLAIN, 50));
			label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

			dtcr.setHorizontalAlignment(SwingConstants.CENTER);
			table.setFont(new Font("�ձٸ��", Font.PLAIN, 20));
			table.getTableHeader().setFont(new Font("�ձٸ��", Font.PLAIN, 20));
			table.getTableHeader().setDefaultRenderer(dtcr);
			table.getColumn("RANK").setPreferredWidth(10);
			table.getColumn("RANK").setCellRenderer(dtcr);
			table.getColumn("NAME").setCellRenderer(dtcr);
			table.getColumn("SCORE").setCellRenderer(dtcr);
			table.getColumn("DIFFICULTY").setCellRenderer(dtcr);
			table.setRowHeight(40);
			table.getTableHeader().setReorderingAllowed(false); // �̵� �Ұ�
			table.getTableHeader().setResizingAllowed(false); // ũ�� ���� �Ұ�

			button.setFont(new Font("�ձٸ��", Font.PLAIN, 20));
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

	// ���� ���� ��� ���̾�α�
	class InsertDialog extends JDialog {
		String[] difficulty_levels = { "EASY", "HARD" };
		JLabel label = new JLabel("ENTER YOUR NAME!", JLabel.CENTER);
		JTextField textField = new JTextField();
		JButton button = new JButton("Ȯ��");

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
			
			label.setFont(new Font("�ձٸ��", Font.PLAIN, 30));
			textField.setHorizontalAlignment(JTextField.CENTER);
			textField.setFont(new Font("�ձٸ��", Font.PLAIN, 30));
			textField.setDocument((new JTextFieldLimit(3))); // �Է� ���� �� ����
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
			button.setFont(new Font("�ձٸ��", Font.PLAIN, 20));
			button.requestFocus(true);

			setLayout(new GridLayout(3, 1));
			setSize(400, 200);
			setUndecorated(true);
			setLocation((Main.SCREEN_WIDTH - this.getWidth()) / 2, Main.SCREEN_HEIGHT - this.getHeight() - 20);
			setVisible(true);
		}
	}

	// �Է� ����
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
