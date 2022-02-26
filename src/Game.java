import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;

public class Game extends Thread {
	private int delay = 20; // 0.02��
	private long pretime;
	private int count = 0;
	private int score = 0;

	private int weapon; // 0: ��, 1: ��
	private int difficulty; // 0: ����, 1: �����

	private boolean up, down, left, right, attack, useItem; // Ű���� ����
	private boolean isMusicOn, isPaused, isCountdown, isGameOver, isPressed, isVictory; 
	// ���� ���, �Ͻ�����, ī��Ʈ, ���� ����, Ű���� ����(������ ���), �¸�

	private Player player = new Player(100, 325);
	private LaserAttack laserAttack = new LaserAttack();
	private Boss boss = new Boss(900, 250);
	private SoundManager soundManager = new SoundManager(2);

	private ArrayList<PlayerAttack> playerAttackList = new ArrayList<PlayerAttack>();
	private ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
	private ArrayList<EnemyAttack> enemyAttackList = new ArrayList<EnemyAttack>();
	private ArrayList<EnemyExplosion> enemyExplosionList = new ArrayList<EnemyExplosion>();
	private ArrayList<Item> itemList = new ArrayList<Item>();
	private ArrayList<Item> itemStorage = new ArrayList<Item>();
	private ArrayList<BossAttack> bossAttackList = new ArrayList<BossAttack>();

	private PlayerAttack playerAttack; // ArrayList ��ҿ� ���� ������ �� �ֵ���
	private Enemy enemy;
	private EnemyAttack enemyAttack;
	private EnemyExplosion enemyExplosion;
	private Item item;
	private BossAttack bossAttack;

	private int x1, x2; // ��� �̹����� �̾ �帣����

	public Game(int weapon, int difficulty) {
		this.weapon = weapon;
		this.difficulty = difficulty;
		this.isMusicOn = true;
		this.isPaused = false;
		this.isGameOver = false;
		this.isCountdown = false;
		this.isPressed = false;
		this.x1 = 0;
		this.x2 = 1280; // �̹��� ũ��
	}

	@Override
	public void run() {
		while (!isGameOver) {
			pretime = System.currentTimeMillis();
			try {
				Thread.sleep(Math.max(0, delay - (System.currentTimeMillis() - pretime))); // �����ϰ� count�� �����ϵ���
				if (!isPaused) {
					keyProcess();
					playerProcess();
					playerAttackProcess();
					if (count < boss.getAppearTime()) { // ���� ���� ��
						x1--;
						x2--;
						if (x1 <= -1280)
							x1 = 1280;
						else if (x2 <= -1280)
							x2 = 1280;
						enemyProcess();
						enemyAttackProcess();
						itemProcess();
						laserAttackProcess();
					} else { // ���� ���� ��
						if (count == boss.getAppearTime()) { // �缳��
							player.setX(100);
							player.setY((Main.SCREEN_HEIGHT - player.getHeight()) / 2);
							playerAttackList.clear();
							enemyList.clear();
							enemyAttackList.clear();
							itemList.clear();
							itemStorage.clear();
							if (player.getIsBuff())
								player.setIsBuff(false);
							if (player.getIsShield())
								player.setIsShield(false);
						}
						bossAttackProcess();
					}
					count++;
					score++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void keyProcess() {
		if (up && player.getY() - player.getSpeed() > 0) // �� (ȭ���� ����� �ʵ���)
			player.setY(player.getY() - player.getSpeed());
		if (down && player.getY() + player.getSpeed() + player.getHeight() < Main.SCREEN_HEIGHT) // �� (ȭ���� ����� �ʵ���)
			player.setY(player.getY() + player.getSpeed());
		if (left && player.getX() - player.getSpeed() > 0) // �� (ȭ���� ����� �ʵ���)
			player.setX(player.getX() - player.getSpeed());
		if (right && player.getX() + player.getSpeed() + player.getWidth() < Main.SCREEN_WIDTH) // �� (ȭ���� ����� �ʵ���)
			player.setX(player.getX() + player.getSpeed());
		if (attack && count % 10 == 0) { // X (0.2�ʸ��� ����)
			if (isMusicOn) // �������
				soundManager.play("player_attack_" + weapon + ".wav", false); // ���� ����
			// �÷��̾� ���� ���� (�̹����� ���� ��ġ ����)
			if (!player.getIsBuff()) {
				if (weapon == 0)
					playerAttackList.add(new PlayerAttack("projectile_" + weapon + ".png", player.getX() + 50,
							player.getY() + 37, 5));
				else
					playerAttackList.add(new PlayerAttack("projectile_" + weapon + ".png", player.getX() + 60,
							player.getY() + 33, 5));
			} else {
				if (weapon == 0)
					playerAttackList.add(new PlayerAttack("projectile_buff_" + weapon + ".png",
							player.getX() + 50, player.getY() + 22, 10));
				else
					playerAttackList.add(new PlayerAttack("projectile_buff_" + weapon + ".png",
							player.getX() + 60, player.getY() + 32, 10));
			}
		}
		if (useItem && count < boss.getAppearTime()) { // Z (���� ���� ������ ����� ������ ��� ����)
			if (itemStorage.size() > 0) {
				item = itemStorage.get(0); // ȹ���� ������� ������ ���
				if (isMusicOn)
					soundManager.play("item_" + item.getNumber() + ".wav", false); // ������ ��� ����
				if (item.getNumber() == 0) { // ȸ�� ������
					if (player.getHp() <= Player.HP_MAX - 5)
						player.setHp(player.getHp() + 5);
				} else if (item.getNumber() == 1) { // ��ȭ ������
					player.setIsBuff(true);
					player.setBuffPreCount(count); // ��� �ð� ����
				} else if (item.getNumber() == 2) { // ���� ������
					player.setIsShield(true);
					player.setShieldPreCount(count); // ��� �ð� ����
				}
				itemStorage.remove(item); // ����� ������ ����
			} else { // ������ ����Ұ� ����ִ� ���
				if (isMusicOn)
					soundManager.play("item_empty.wav", false); // ����Ұ� �� ����
			}
			useItem = false;
		}
	}

	private void playerProcess() {
		if (player.getHp() <= 0) { // �÷��̾��� ü���� 0 ���ϰ� �� ���
			isGameOver = true;
			isVictory = false;
		}
	}

	// �÷��̾� ���� ó��
	private void playerAttackProcess() {
		for (Iterator<PlayerAttack> playerAttackIterator = playerAttackList.iterator(); playerAttackIterator
				.hasNext();) {
			playerAttack = playerAttackIterator.next();
			playerAttack.fire(); // �÷��̾� ���� �߻�
			if (count < boss.getAppearTime()) { // ���� ���� ��
				for (Iterator<Enemy> enemyIterator = enemyList.iterator(); enemyIterator.hasNext();) {
					enemy = enemyIterator.next();
					if (playerAttack.getX() + playerAttack.getWidth() >= enemy.getX() // �÷��̾� ���ݰ� ���� ���� ���
							&& playerAttack.getX() <= enemy.getX() + enemy.getWidth()
							&& playerAttack.getY() + playerAttack.getHeight() >= enemy.getY()
							&& playerAttack.getY() <= enemy.getY() + enemy.getHeight()) {
						enemy.setHp(enemy.getHp() - playerAttack.getATK()); // �� ������
						playerAttackIterator.remove(); // ���� ���� �÷��̾� ���� ����
						if (enemy.getHp() <= 0) { // ���� ü���� 0 ������ ���
							int random = new Random().nextInt(6); // 2���� 1 Ȯ��
							if (random >= 0 && random <= 2)
								itemList.add(new Item(enemy.getX() + enemy.getWidth() / 2 - 14,
										enemy.getY() + enemy.getHeight() / 2 - 14, random)); // ���� óġ�� �ڸ��� ������ ���
							if (isMusicOn)
								soundManager.play("enemy_explosion.wav", false); // ���� ����
							enemyExplosionList.add(new EnemyExplosion(enemy.getX(), enemy.getY(), count)); // �� ����
							enemyIterator.remove(); // �� ����
							score += 1000; // ���� �߰�
						}
						break;
					}
				}
			} else { // ���� ���� ��
				if (playerAttack.getX() + playerAttack.getWidth() >= boss.getX()
						&& playerAttack.getX() <= boss.getX() + boss.getWidth()
						&& playerAttack.getY() + playerAttack.getHeight() >= boss.getY()
						&& playerAttack.getY() <= boss.getY() + boss.getHeight()) { // �÷��̾� ���ݰ� ������ ���� ���
					boss.setHp(boss.getHp() - playerAttack.getATK()); // ���� ������
					playerAttackIterator.remove(); // �÷��̾� ���� ����
					if (boss.getHp() <= 0) { // ������ ü���� 0 ������ ���
						if (difficulty == 0) // ���̵��� ���� ���� �߰�
							score += 10000;
						else
							score += 20000;
						isGameOver = true;
						isVictory = true;
						break;
					}
				}
			}
			if (playerAttack.getX() > Main.SCREEN_WIDTH) // ȭ���� ��� ���
				playerAttackIterator.remove(); // �÷��̾� ���� ����
		}
	}

	// �� ����, �̵� ó��
	private void enemyProcess() {
		if (count % 40 == 0) // 0.8�ʸ��� �� ����
			enemyList.add(new Enemy(Main.SCREEN_WIDTH, (int) (Math.random() * (Main.SCREEN_HEIGHT - 100) + 30)));
		for (Iterator<Enemy> enemyIterator = enemyList.iterator(); enemyIterator.hasNext();) {
			enemy = enemyIterator.next();
			enemy.move(); // �� �̵�
			if (enemy.getX() + enemy.getWidth() < 0) // ȭ���� ��� ���
				enemyIterator.remove(); // �� ����
		}
	}

	// �� ���� ó��
	private void enemyAttackProcess() {
		if (count % 30 == 0) // 0.6�ʸ��� �� ���� ����
			enemyAttackList.add(new EnemyAttack(enemy.getX() - 80, enemy.getY() + 35));
		for (Iterator<EnemyAttack> enemyAttackIterator = enemyAttackList.iterator(); enemyAttackIterator.hasNext();) {
			enemyAttack = enemyAttackIterator.next();
			enemyAttack.fire(); // �� ���� �߻�
			if (player.getX() + player.getWidth() >= enemyAttack.getX()
					&& player.getX() <= enemyAttack.getX() + enemyAttack.getWidth()
					&& player.getY() + player.getHeight() >= enemyAttack.getY()
					&& player.getY() + 12 <= enemyAttack.getY() + enemyAttack.getHeight()) { // �� ���ݰ� �÷��̾ ���� ���
				if (!player.getIsShield()) { // ���� ������ ȿ���� ���� ���� ���
					if (isMusicOn)
						soundManager.play("player_damage.wav", false); // �÷��̾� ������ ����
					player.setHp(player.getHp() - enemyAttack.getATK()); // �÷��̾� ������
					player.setIsDamaged(true); // �÷��̾� ������ ȭ�� ó���� ����
					player.setDamagePreCount(count); // �÷��̾� ������ ȭ�� ó���� ����
				}
				enemyAttackIterator.remove();
			}
			if (enemyAttack.getX() + enemyAttack.getWidth() < 0) // ȭ���� ��� ���
				enemyAttackIterator.remove(); // �� ���� ����
		}
	}

	// ���� ���� ó��
	private void bossAttackProcess() {
		if (count % 10 == 0) { // 0.2�ʸ��� ���� ���� ���� (����)
			bossAttackList.add(new BossAttackUpDown(new Random().nextInt(Main.SCREEN_WIDTH - 56))); // ��ĳ����
			if (difficulty == 1) // ����� ��� (�¿�)
				bossAttackList.add(new BossAttackLeftRight(new Random().nextInt(Main.SCREEN_HEIGHT - 48))); // ��ĳ����
		}
		for (Iterator<BossAttack> bossAttackIterator = bossAttackList.iterator(); bossAttackIterator.hasNext();) {
			bossAttack = bossAttackIterator.next();
			bossAttack.fire(); // ���� ���� �߻�
			if (player.getX() + player.getWidth() >= bossAttack.getX()
					&& player.getX() <= bossAttack.getX() + bossAttack.getWidth()
					&& player.getY() + player.getHeight() >= bossAttack.getY()
					&& player.getY() + 12 <= bossAttack.getY() + bossAttack.getHeight()) { // ���� ���ݰ� �÷��̾ ���� ���
				if (isMusicOn)
					soundManager.play("player_damage.wav", false); // �÷��̾� ������ ����
				player.setHp(player.getHp() - bossAttack.getATK()); // �÷��̾� ������
				player.setIsDamaged(true); // �÷��̾� ������ ȭ�� ó���� ����
				player.setDamagePreCount(count); // �÷��̾� ������ ȭ�� ó���� ����
				bossAttackIterator.remove(); // ���� ���� ����
			}
			if (bossAttack.getY() > Main.SCREEN_HEIGHT || bossAttack.getX() + bossAttack.getWidth() < 0) // ȭ���� ��� ���
				bossAttackIterator.remove(); // ���� ���� ����
		}
	}

	// ������ ó��
	private void itemProcess() {
		for (Iterator<Item> itemIterator = itemList.iterator(); itemIterator.hasNext();) {
			item = itemIterator.next();
			item.move(); // ������ �̵�
			if (player.getX() + player.getWidth() >= item.getX() && player.getX() <= item.getX() + item.getWidth()
					&& player.getY() + player.getHeight() >= item.getY()
					&& player.getY() + 12 <= item.getY() + item.getHeight()) { // �����۰� �÷��̾ ���� ���
				if (itemStorage.size() < 2) // ������ ����ҿ� �ڸ��� �ִ� ���
					itemStorage.add(new Item(item.getX(), item.getY(), item.getNumber())); // ������ ����ҿ� ������ �߰�
				if (isMusicOn)
					soundManager.play("item_get.wav", false); // ������ ȹ�� ����
				itemIterator.remove(); // ������ ����
			}
			if (item.getX() + item.getWidth() < 0) // ȭ���� ��� ���
				itemIterator.remove(); // ������ ����
		}
		if (player.getIsBuff() && count - player.getBuffPreCount() >= 250) // 5�� ���� ��ȭ ������ ȿ��
			player.setIsBuff(false);
		if (player.getIsShield() && count - player.getShieldPreCount() >= 250) // 5�� ���� ���� ������ ȿ��
			player.setIsShield(false);
	}

	// ������ ���� ó��
	private void laserAttackProcess() {
		if (count % 400 == 0) // 8�ʸ��� ������ ���� ����
			laserAttack.setY(new Random().nextInt(Main.SCREEN_HEIGHT - laserAttack.getLaserHeight()));
		if (laserAttack.getIsLaunched() && !laserAttack.getIsTouched()) { // �������� �߻�ǰ� �� ���� ���� ���� ���
			if (player.getY() + player.getHeight() >= laserAttack.getY()
					&& player.getY() + 12 <= laserAttack.getY() + laserAttack.getLaserHeight()) { // �������� �÷��̾ ���� ���
				if (!player.getIsShield()) { // ���� ������ ȿ���� ���� ���� ���
					if (isMusicOn)
						soundManager.play("player_damage.wav", false); // �÷��̾� ������ ����
					player.setHp(player.getHp() - laserAttack.getATK()); // �÷��̾� ������
					player.setIsDamaged(true); // �÷��̾� ������ ȭ�� ó���� ����
					player.setDamagePreCount(count); // �÷��̾� ������ ȭ�� ó���� ����
				}
				laserAttack.setIsTouched(true); // �� ���� �������� �Ե���
			}
		}
	}

	// ���� ��� �׸���
	public void gameDraw(Graphics g) {
		playerDraw(g);
		if (count < boss.getAppearTime()) {
			enemyDraw(g);
			itemDraw(g);
			laserDraw(g);
		} else {
			bossDraw(g);
		}
		playerDamageDraw(g);
		musicDraw(g);
		pauseDraw(g);
		scoreDraw(g);
	}

	// �÷��̾�, �÷��̾� ���� �׸���
	public void playerDraw(Graphics g) {
		g.setColor(Color.RED);
		// �÷��̾� �̹��� ����
		if (!player.getIsBuff()) {
			if (attack) {
				player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_attack_" + weapon + ".png")).getImage());
			} else {
				player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_" + weapon + ".png")).getImage());
			}
			g.fillRect(player.getX() - 33, player.getY() - 20, player.getHp() * 4, 10); // ü���� �����ŭ (ü�¹�)
		} else {
			if (weapon == 1) {
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("player_buff_1.png")).getImage(), player.getX() - 15,
						player.getY() + 20, null);
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("player_buff_2.png")).getImage(), player.getX() + 20,
						player.getY() + 20, null);
				if (attack) {
					player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_attack_1.png")).getImage());
				} else {
					player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_1.png")).getImage());
				}
			} else {
				if (attack) {
					player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_buff_attack_0.png")).getImage());
				} else {
					player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_buff_0.png")).getImage());
				}
			}
			g.fillRect(player.getX() - 30, player.getY() - 20, player.getHp() * 4, 10); // ü���� �����ŭ (ü�¹�)
		}
		player.setWidth(player.getImage().getWidth(null)); // �÷��̾� �ʺ� ����
		player.setHeight(player.getImage().getHeight(null)); // �÷��̾� ���� ����
		g.drawImage(player.getImage(), player.getX(), player.getY(), null); // �÷��̾� �׸���
		// �÷��̾� ���� �׸���
		for (int i = 0; i < playerAttackList.size(); i++) {
			playerAttack = playerAttackList.get(i);
			g.drawImage(playerAttack.getImage(), playerAttack.getX(), playerAttack.getY(), null);
		}
	}

	// �÷��̾� ������ �׸���
	public void playerDamageDraw(Graphics g) {
		if (player.getIsDamaged()) { // �÷��̾ �������� ���� ���
			if (count - player.getDamagePreCount() < 15) { // 0.3�� ����
				g.setColor(new Color(255, 0, 0, 128)); // ������ (����)
				g.fillRect(0, 0, 1280, 720);
			} else
				player.setIsDamaged(false);
		}
	}

	// ��, �� ����, �� ���� �׸���
	public void enemyDraw(Graphics g) {
		// �� �׸���
		for (int i = 0; i < enemyList.size(); i++) {
			enemy = enemyList.get(i);
			if (count % 25 < 5)
				g.drawImage(enemy.getImage(0), enemy.getX(), enemy.getY(), null);
			else if (count % 25 < 10)
				g.drawImage(enemy.getImage(1), enemy.getX(), enemy.getY(), null);
			else if (count % 25 < 15)
				g.drawImage(enemy.getImage(2), enemy.getX(), enemy.getY(), null);
			else if (count % 25 < 20)
				g.drawImage(enemy.getImage(3), enemy.getX(), enemy.getY(), null);
			else
				g.drawImage(enemy.getImage(4), enemy.getX(), enemy.getY(), null);
			g.setColor(Color.GREEN);
			g.fillRect(enemy.getX() - 20, enemy.getY() - 20, enemy.getHp() * 10, 10); // ü���� �����ŭ (ü�¹�)
		}
		// �� ���� �׸���
		for (int i = 0; i < enemyAttackList.size(); i++) {
			enemyAttack = enemyAttackList.get(i);
			g.drawImage(enemyAttack.getImage(), enemyAttack.getX(), enemyAttack.getY(), null);
		}
		// �� ���� �׸���
		for (Iterator<EnemyExplosion> enemyExplosionIterator = enemyExplosionList.iterator(); enemyExplosionIterator
				.hasNext();) {
			enemyExplosion = enemyExplosionIterator.next();
			g.drawImage(enemyExplosion.getImage(), enemyExplosion.getX(), enemyExplosion.getY(), null);
			if (count - enemyExplosion.getCount() >= 10) // 0.2�� ���� �� ����
				enemyExplosionIterator.remove(); // �� ���� ����
		}
	}

	// ����, ���� ���� �׸���
	public void bossDraw(Graphics g) {
		// ���� ���� �׸���
				for (int i = 0; i < bossAttackList.size(); i++) {
					bossAttack = bossAttackList.get(i);
					g.drawImage(bossAttack.getImage(), bossAttack.getX(), bossAttack.getY(), null);
				}
		// ���� �׸���
		g.drawImage(boss.getImage(), boss.getX(), boss.getY(), null);
		g.setColor(Color.BLUE);
		g.fillRect(boss.getX() - 70, boss.getY() - 40, boss.getHp() * 4, 10); // ü���� �����ŭ (ü�¹�)	
	}

	// ������, ������ ȿ��, ������ ����� �׸���
	public void itemDraw(Graphics g) {
		// ������ �׸���
		for (int i = 0; i < itemList.size(); i++) {
			item = itemList.get(i);
			g.drawImage(item.getImage(), item.getX(), item.getY(), null);
		}
		// ���� ������ ȿ�� �׸���
		if (!player.getIsBuff() && player.getIsShield())
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("shield.png")).getImage(), player.getX() - 32, player.getY() - 10, null);
		else if (player.getIsBuff() && player.getIsShield()) {
			if (weapon == 0)
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("shield.png")).getImage(), player.getX() - 29, player.getY() - 9, null);
			else
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("shield.png")).getImage(), player.getX() - 30, player.getY() - 9, null);
		}
		// ������ ����� �׸���
		for (int i = 0; i < 2; i++)
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("item_storage.png")).getImage(), 1110 + (i * 60), 630, null);
		// ������ ����� ���� ������ �׸���
		for (int i = 0; i < itemStorage.size(); i++) {
			item = itemStorage.get(i);
			if (item.getNumber() == 0)
				g.drawImage(item.getImage(), 1120 + (i * 60), 640, null);
			else if (item.getNumber() == 1)
				g.drawImage(item.getImage(), 1118 + (i * 60), 638, null);
			else
				g.drawImage(item.getImage(), 1122 + (i * 60), 640, null);
		}
	}

	// ������, ��� �׸���
	public void laserDraw(Graphics g) {
		if (count > 400) {
			// ��� �׸���
			if ((count % 400 > 0 && count % 400 <= 20) || (count % 400 >= 40 && count % 400 <= 60)
					|| (count % 400 >= 80 && count % 400 <= 100)) { // 0.4�� ���� 3��
				if (isMusicOn && !isPaused)
					soundManager.play("warning.wav", false); // ��� ����
				g.drawImage(laserAttack.getWarningImage(), 1100,
						laserAttack.getY() + (laserAttack.getLaserHeight() - laserAttack.getWarningHeight()) / 2, null);
			}
			// ������ �׸���
			if (count % 400 >= 120 && count % 400 <= 180) { // 1.2�� ����
				g.drawImage(laserAttack.getLaserImage(), 0, laserAttack.getY(), null);
				if (!laserAttack.getIsLaunched()) {
					if (isMusicOn)
						soundManager.play("laser.wav", false); // ������ ����
					laserAttack.setIsLaunched(true);
				}
			} else {
				if (laserAttack.getIsLaunched()) {
					laserAttack.setIsLaunched(false);
					laserAttack.setIsTouched(false);
				}
			}
		}
	}

	// ���� �׸���
	public void scoreDraw(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("�ձٸ��", Font.PLAIN, 50));
		g.drawString("Score: " + score, 30, 60);
	}

	// �Ͻ����� �׸���
	public void pauseDraw(Graphics g) {
		g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("icon_pause.png")).getImage(), 1200, 20, null);
		if (isPaused && !isCountdown) {
			g.setColor(Color.BLACK);
			g.setFont(new Font("�ձٸ��", Font.BOLD, 100));
			g.drawString("P A U S E", Main.SCREEN_WIDTH / 2 - 230, Main.SCREEN_HEIGHT / 2 - 50);
			g.setColor(Color.WHITE);
			g.setFont(new Font("�ձٸ��", Font.BOLD, 100));
			g.drawString("P A U S E", Main.SCREEN_WIDTH / 2 - 240, Main.SCREEN_HEIGHT / 2 - 50);
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("icon_home.png")).getImage(), 580, 350, null);
		}
	}

	// ���� ��� ������ �׸���
	public void musicDraw(Graphics g) {
		if (isMusicOn) {
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("icon_musicOn.png")).getImage(), 1100, 17, null);
		} else {
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("icon_musicOff.png")).getImage(), 1100, 17, null);
		}
	}

	public void setUp(boolean up) {
		this.up = up;
	}

	public void setDown(boolean down) {
		this.down = down;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setAttack(boolean attack) {
		this.attack = attack;
	}

	public void setUseItem(boolean useItem) {
		this.useItem = useItem;
	}

	public boolean getIsPressed() {
		return isPressed;
	}

	public void setIsPressed(boolean isPressed) {
		this.isPressed = isPressed;
	}

	public boolean getIsMusicOn() {
		return isMusicOn;
	}

	public void setIsMusicOn(boolean isMusicOn) {
		this.isMusicOn = isMusicOn;
	}

	public boolean getIsPaused() {
		return isPaused;
	}

	public void setIsPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	public void setIsCountdown(boolean isCountdown) {
		this.isCountdown = isCountdown;
	}

	public int getX1() {
		return x1;
	}

	public int getX2() {
		return x2;
	}

	public int getCount() {
		return count;
	}

	public int getScore() {
		return score;
	}

	public boolean getIsGameOver() {
		return isGameOver;
	}

	public int getBossAppearTime() {
		return boss.getAppearTime();
	}

	public boolean getIsVictory() {
		return isVictory;
	}
}
