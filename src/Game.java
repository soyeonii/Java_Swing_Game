import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;

public class Game extends Thread {
	private int delay = 20; // 0.02초
	private long pretime;
	private int count = 0;
	private int score = 0;

	private int weapon; // 0: 불, 1: 총
	private int difficulty; // 0: 쉬움, 1: 어려움

	private boolean up, down, left, right, attack, useItem; // 키보드 관련
	private boolean isMusicOn, isPaused, isCountdown, isGameOver, isPressed, isVictory; 
	// 음악 재생, 일시정지, 카운트, 게임 종료, 키보드 눌림(아이템 사용), 승리

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

	private PlayerAttack playerAttack; // ArrayList 요소에 쉽게 접근할 수 있도록
	private Enemy enemy;
	private EnemyAttack enemyAttack;
	private EnemyExplosion enemyExplosion;
	private Item item;
	private BossAttack bossAttack;

	private int x1, x2; // 배경 이미지가 이어서 흐르도록

	public Game(int weapon, int difficulty) {
		this.weapon = weapon;
		this.difficulty = difficulty;
		this.isMusicOn = true;
		this.isPaused = false;
		this.isGameOver = false;
		this.isCountdown = false;
		this.isPressed = false;
		this.x1 = 0;
		this.x2 = 1280; // 이미지 크기
	}

	@Override
	public void run() {
		while (!isGameOver) {
			pretime = System.currentTimeMillis();
			try {
				Thread.sleep(Math.max(0, delay - (System.currentTimeMillis() - pretime))); // 일정하게 count가 증가하도록
				if (!isPaused) {
					keyProcess();
					playerProcess();
					playerAttackProcess();
					if (count < boss.getAppearTime()) { // 보스 등장 전
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
					} else { // 보스 등장 후
						if (count == boss.getAppearTime()) { // 재설정
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
		if (up && player.getY() - player.getSpeed() > 0) // ↑ (화면을 벗어나지 않도록)
			player.setY(player.getY() - player.getSpeed());
		if (down && player.getY() + player.getSpeed() + player.getHeight() < Main.SCREEN_HEIGHT) // ↓ (화면을 벗어나지 않도록)
			player.setY(player.getY() + player.getSpeed());
		if (left && player.getX() - player.getSpeed() > 0) // ← (화면을 벗어나지 않도록)
			player.setX(player.getX() - player.getSpeed());
		if (right && player.getX() + player.getSpeed() + player.getWidth() < Main.SCREEN_WIDTH) // → (화면을 벗어나지 않도록)
			player.setX(player.getX() + player.getSpeed());
		if (attack && count % 10 == 0) { // X (0.2초마다 공격)
			if (isMusicOn) // 배경음악
				soundManager.play("player_attack_" + weapon + ".wav", false); // 공격 사운드
			// 플레이어 공격 저장 (이미지에 따라 위치 설정)
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
		if (useItem && count < boss.getAppearTime()) { // Z (보스 등장 전까지 저장된 아이템 사용 가능)
			if (itemStorage.size() > 0) {
				item = itemStorage.get(0); // 획득한 순서대로 아이템 사용
				if (isMusicOn)
					soundManager.play("item_" + item.getNumber() + ".wav", false); // 아이템 사용 사운드
				if (item.getNumber() == 0) { // 회복 아이템
					if (player.getHp() <= Player.HP_MAX - 5)
						player.setHp(player.getHp() + 5);
				} else if (item.getNumber() == 1) { // 강화 아이템
					player.setIsBuff(true);
					player.setBuffPreCount(count); // 사용 시간 저장
				} else if (item.getNumber() == 2) { // 쉴드 아이템
					player.setIsShield(true);
					player.setShieldPreCount(count); // 사용 시간 저장
				}
				itemStorage.remove(item); // 사용한 아이템 삭제
			} else { // 아이템 저장소가 비어있는 경우
				if (isMusicOn)
					soundManager.play("item_empty.wav", false); // 저장소가 빈 사운드
			}
			useItem = false;
		}
	}

	private void playerProcess() {
		if (player.getHp() <= 0) { // 플레이어의 체력이 0 이하가 된 경우
			isGameOver = true;
			isVictory = false;
		}
	}

	// 플레이어 공격 처리
	private void playerAttackProcess() {
		for (Iterator<PlayerAttack> playerAttackIterator = playerAttackList.iterator(); playerAttackIterator
				.hasNext();) {
			playerAttack = playerAttackIterator.next();
			playerAttack.fire(); // 플레이어 공격 발사
			if (count < boss.getAppearTime()) { // 보스 등장 전
				for (Iterator<Enemy> enemyIterator = enemyList.iterator(); enemyIterator.hasNext();) {
					enemy = enemyIterator.next();
					if (playerAttack.getX() + playerAttack.getWidth() >= enemy.getX() // 플레이어 공격과 적이 닿은 경우
							&& playerAttack.getX() <= enemy.getX() + enemy.getWidth()
							&& playerAttack.getY() + playerAttack.getHeight() >= enemy.getY()
							&& playerAttack.getY() <= enemy.getY() + enemy.getHeight()) {
						enemy.setHp(enemy.getHp() - playerAttack.getATK()); // 적 데미지
						playerAttackIterator.remove(); // 적에 닿은 플레이어 공격 삭제
						if (enemy.getHp() <= 0) { // 적의 체력이 0 이하인 경우
							int random = new Random().nextInt(6); // 2분의 1 확률
							if (random >= 0 && random <= 2)
								itemList.add(new Item(enemy.getX() + enemy.getWidth() / 2 - 14,
										enemy.getY() + enemy.getHeight() / 2 - 14, random)); // 적이 처치된 자리에 아이템 드롭
							if (isMusicOn)
								soundManager.play("enemy_explosion.wav", false); // 폭발 사운드
							enemyExplosionList.add(new EnemyExplosion(enemy.getX(), enemy.getY(), count)); // 적 폭발
							enemyIterator.remove(); // 적 삭제
							score += 1000; // 점수 추가
						}
						break;
					}
				}
			} else { // 보스 등장 후
				if (playerAttack.getX() + playerAttack.getWidth() >= boss.getX()
						&& playerAttack.getX() <= boss.getX() + boss.getWidth()
						&& playerAttack.getY() + playerAttack.getHeight() >= boss.getY()
						&& playerAttack.getY() <= boss.getY() + boss.getHeight()) { // 플레이어 공격과 보스가 닿은 경우
					boss.setHp(boss.getHp() - playerAttack.getATK()); // 보스 데미지
					playerAttackIterator.remove(); // 플레이어 공격 제거
					if (boss.getHp() <= 0) { // 보스의 체력이 0 이하인 경우
						if (difficulty == 0) // 난이도에 따라 점수 추가
							score += 10000;
						else
							score += 20000;
						isGameOver = true;
						isVictory = true;
						break;
					}
				}
			}
			if (playerAttack.getX() > Main.SCREEN_WIDTH) // 화면을 벗어난 경우
				playerAttackIterator.remove(); // 플레이어 공격 제거
		}
	}

	// 적 생성, 이동 처리
	private void enemyProcess() {
		if (count % 40 == 0) // 0.8초마다 적 생성
			enemyList.add(new Enemy(Main.SCREEN_WIDTH, (int) (Math.random() * (Main.SCREEN_HEIGHT - 100) + 30)));
		for (Iterator<Enemy> enemyIterator = enemyList.iterator(); enemyIterator.hasNext();) {
			enemy = enemyIterator.next();
			enemy.move(); // 적 이동
			if (enemy.getX() + enemy.getWidth() < 0) // 화면을 벗어난 경우
				enemyIterator.remove(); // 적 제거
		}
	}

	// 적 공격 처리
	private void enemyAttackProcess() {
		if (count % 30 == 0) // 0.6초마다 적 공격 생성
			enemyAttackList.add(new EnemyAttack(enemy.getX() - 80, enemy.getY() + 35));
		for (Iterator<EnemyAttack> enemyAttackIterator = enemyAttackList.iterator(); enemyAttackIterator.hasNext();) {
			enemyAttack = enemyAttackIterator.next();
			enemyAttack.fire(); // 적 공격 발사
			if (player.getX() + player.getWidth() >= enemyAttack.getX()
					&& player.getX() <= enemyAttack.getX() + enemyAttack.getWidth()
					&& player.getY() + player.getHeight() >= enemyAttack.getY()
					&& player.getY() + 12 <= enemyAttack.getY() + enemyAttack.getHeight()) { // 적 공격과 플레이어가 닿은 경우
				if (!player.getIsShield()) { // 쉴드 아이템 효과가 있지 않은 경우
					if (isMusicOn)
						soundManager.play("player_damage.wav", false); // 플레이어 데미지 사운드
					player.setHp(player.getHp() - enemyAttack.getATK()); // 플레이어 데미지
					player.setIsDamaged(true); // 플레이어 데미지 화면 처리를 위한
					player.setDamagePreCount(count); // 플레이어 데미지 화면 처리를 위한
				}
				enemyAttackIterator.remove();
			}
			if (enemyAttack.getX() + enemyAttack.getWidth() < 0) // 화면을 벗어난 경우
				enemyAttackIterator.remove(); // 적 공격 제거
		}
	}

	// 보스 공격 처리
	private void bossAttackProcess() {
		if (count % 10 == 0) { // 0.2초마다 보스 공격 생성 (상하)
			bossAttackList.add(new BossAttackUpDown(new Random().nextInt(Main.SCREEN_WIDTH - 56))); // 업캐스팅
			if (difficulty == 1) // 어려움 모드 (좌우)
				bossAttackList.add(new BossAttackLeftRight(new Random().nextInt(Main.SCREEN_HEIGHT - 48))); // 업캐스팅
		}
		for (Iterator<BossAttack> bossAttackIterator = bossAttackList.iterator(); bossAttackIterator.hasNext();) {
			bossAttack = bossAttackIterator.next();
			bossAttack.fire(); // 보스 공격 발사
			if (player.getX() + player.getWidth() >= bossAttack.getX()
					&& player.getX() <= bossAttack.getX() + bossAttack.getWidth()
					&& player.getY() + player.getHeight() >= bossAttack.getY()
					&& player.getY() + 12 <= bossAttack.getY() + bossAttack.getHeight()) { // 보스 공격과 플레이어가 닿은 경우
				if (isMusicOn)
					soundManager.play("player_damage.wav", false); // 플레이어 데미지 사운드
				player.setHp(player.getHp() - bossAttack.getATK()); // 플레이어 데미지
				player.setIsDamaged(true); // 플레이어 데미지 화면 처리를 위한
				player.setDamagePreCount(count); // 플레이어 데미지 화면 처리를 위한
				bossAttackIterator.remove(); // 보스 공격 제거
			}
			if (bossAttack.getY() > Main.SCREEN_HEIGHT || bossAttack.getX() + bossAttack.getWidth() < 0) // 화면을 벗어난 경우
				bossAttackIterator.remove(); // 보스 공격 제거
		}
	}

	// 아이템 처리
	private void itemProcess() {
		for (Iterator<Item> itemIterator = itemList.iterator(); itemIterator.hasNext();) {
			item = itemIterator.next();
			item.move(); // 아이템 이동
			if (player.getX() + player.getWidth() >= item.getX() && player.getX() <= item.getX() + item.getWidth()
					&& player.getY() + player.getHeight() >= item.getY()
					&& player.getY() + 12 <= item.getY() + item.getHeight()) { // 아이템과 플레이어가 닿은 경우
				if (itemStorage.size() < 2) // 아이템 저장소에 자리가 있는 경우
					itemStorage.add(new Item(item.getX(), item.getY(), item.getNumber())); // 아이템 저장소에 아이템 추가
				if (isMusicOn)
					soundManager.play("item_get.wav", false); // 아이템 획득 사운드
				itemIterator.remove(); // 아이템 제거
			}
			if (item.getX() + item.getWidth() < 0) // 화면을 벗어난 경우
				itemIterator.remove(); // 아이템 제거
		}
		if (player.getIsBuff() && count - player.getBuffPreCount() >= 250) // 5초 동안 강화 아이템 효과
			player.setIsBuff(false);
		if (player.getIsShield() && count - player.getShieldPreCount() >= 250) // 5초 동안 쉴드 아이템 효과
			player.setIsShield(false);
	}

	// 레이저 공격 처리
	private void laserAttackProcess() {
		if (count % 400 == 0) // 8초마다 레이저 공격 생성
			laserAttack.setY(new Random().nextInt(Main.SCREEN_HEIGHT - laserAttack.getLaserHeight()));
		if (laserAttack.getIsLaunched() && !laserAttack.getIsTouched()) { // 레이저가 발사되고 한 번도 닿지 않은 경우
			if (player.getY() + player.getHeight() >= laserAttack.getY()
					&& player.getY() + 12 <= laserAttack.getY() + laserAttack.getLaserHeight()) { // 레이저와 플레이어가 닿은 경우
				if (!player.getIsShield()) { // 쉴드 아이템 효과가 있지 않은 경우
					if (isMusicOn)
						soundManager.play("player_damage.wav", false); // 플레이어 데미지 사운드
					player.setHp(player.getHp() - laserAttack.getATK()); // 플레이어 데미지
					player.setIsDamaged(true); // 플레이어 데미지 화면 처리를 위한
					player.setDamagePreCount(count); // 플레이어 데미지 화면 처리를 위한
				}
				laserAttack.setIsTouched(true); // 한 번만 데미지를 입도록
			}
		}
	}

	// 게임 요소 그리기
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

	// 플레이어, 플레이어 공격 그리기
	public void playerDraw(Graphics g) {
		g.setColor(Color.RED);
		// 플레이어 이미지 설정
		if (!player.getIsBuff()) {
			if (attack) {
				player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_attack_" + weapon + ".png")).getImage());
			} else {
				player.setImage(new ImageIcon(getClass().getClassLoader().getResource("player_" + weapon + ".png")).getImage());
			}
			g.fillRect(player.getX() - 33, player.getY() - 20, player.getHp() * 4, 10); // 체력의 배수만큼 (체력바)
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
			g.fillRect(player.getX() - 30, player.getY() - 20, player.getHp() * 4, 10); // 체력의 배수만큼 (체력바)
		}
		player.setWidth(player.getImage().getWidth(null)); // 플레이어 너비 설정
		player.setHeight(player.getImage().getHeight(null)); // 플레이어 높이 설정
		g.drawImage(player.getImage(), player.getX(), player.getY(), null); // 플레이어 그리기
		// 플레이어 공격 그리기
		for (int i = 0; i < playerAttackList.size(); i++) {
			playerAttack = playerAttackList.get(i);
			g.drawImage(playerAttack.getImage(), playerAttack.getX(), playerAttack.getY(), null);
		}
	}

	// 플레이어 데미지 그리기
	public void playerDamageDraw(Graphics g) {
		if (player.getIsDamaged()) { // 플레이어가 데미지를 입은 경우
			if (count - player.getDamagePreCount() < 15) { // 0.3초 동안
				g.setColor(new Color(255, 0, 0, 128)); // 빨갛게 (투명)
				g.fillRect(0, 0, 1280, 720);
			} else
				player.setIsDamaged(false);
		}
	}

	// 적, 적 공격, 적 폭발 그리기
	public void enemyDraw(Graphics g) {
		// 적 그리기
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
			g.fillRect(enemy.getX() - 20, enemy.getY() - 20, enemy.getHp() * 10, 10); // 체력의 배수만큼 (체력바)
		}
		// 적 공격 그리기
		for (int i = 0; i < enemyAttackList.size(); i++) {
			enemyAttack = enemyAttackList.get(i);
			g.drawImage(enemyAttack.getImage(), enemyAttack.getX(), enemyAttack.getY(), null);
		}
		// 적 폭발 그리기
		for (Iterator<EnemyExplosion> enemyExplosionIterator = enemyExplosionList.iterator(); enemyExplosionIterator
				.hasNext();) {
			enemyExplosion = enemyExplosionIterator.next();
			g.drawImage(enemyExplosion.getImage(), enemyExplosion.getX(), enemyExplosion.getY(), null);
			if (count - enemyExplosion.getCount() >= 10) // 0.2초 동안 적 폭발
				enemyExplosionIterator.remove(); // 적 폭발 제거
		}
	}

	// 보스, 보스 공격 그리기
	public void bossDraw(Graphics g) {
		// 보스 공격 그리기
				for (int i = 0; i < bossAttackList.size(); i++) {
					bossAttack = bossAttackList.get(i);
					g.drawImage(bossAttack.getImage(), bossAttack.getX(), bossAttack.getY(), null);
				}
		// 보스 그리기
		g.drawImage(boss.getImage(), boss.getX(), boss.getY(), null);
		g.setColor(Color.BLUE);
		g.fillRect(boss.getX() - 70, boss.getY() - 40, boss.getHp() * 4, 10); // 체력의 배수만큼 (체력바)	
	}

	// 아이템, 아이템 효과, 아이템 저장소 그리기
	public void itemDraw(Graphics g) {
		// 아이템 그리기
		for (int i = 0; i < itemList.size(); i++) {
			item = itemList.get(i);
			g.drawImage(item.getImage(), item.getX(), item.getY(), null);
		}
		// 쉴드 아이템 효과 그리기
		if (!player.getIsBuff() && player.getIsShield())
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("shield.png")).getImage(), player.getX() - 32, player.getY() - 10, null);
		else if (player.getIsBuff() && player.getIsShield()) {
			if (weapon == 0)
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("shield.png")).getImage(), player.getX() - 29, player.getY() - 9, null);
			else
				g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("shield.png")).getImage(), player.getX() - 30, player.getY() - 9, null);
		}
		// 아이템 저장소 그리기
		for (int i = 0; i < 2; i++)
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("item_storage.png")).getImage(), 1110 + (i * 60), 630, null);
		// 아이템 저장소 내의 아이템 그리기
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

	// 레이저, 경고 그리기
	public void laserDraw(Graphics g) {
		if (count > 400) {
			// 경고 그리기
			if ((count % 400 > 0 && count % 400 <= 20) || (count % 400 >= 40 && count % 400 <= 60)
					|| (count % 400 >= 80 && count % 400 <= 100)) { // 0.4초 동안 3번
				if (isMusicOn && !isPaused)
					soundManager.play("warning.wav", false); // 경고 사운드
				g.drawImage(laserAttack.getWarningImage(), 1100,
						laserAttack.getY() + (laserAttack.getLaserHeight() - laserAttack.getWarningHeight()) / 2, null);
			}
			// 레이저 그리기
			if (count % 400 >= 120 && count % 400 <= 180) { // 1.2초 동안
				g.drawImage(laserAttack.getLaserImage(), 0, laserAttack.getY(), null);
				if (!laserAttack.getIsLaunched()) {
					if (isMusicOn)
						soundManager.play("laser.wav", false); // 레이저 사운드
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

	// 점수 그리기
	public void scoreDraw(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("둥근모꼴", Font.PLAIN, 50));
		g.drawString("Score: " + score, 30, 60);
	}

	// 일시정지 그리기
	public void pauseDraw(Graphics g) {
		g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("icon_pause.png")).getImage(), 1200, 20, null);
		if (isPaused && !isCountdown) {
			g.setColor(Color.BLACK);
			g.setFont(new Font("둥근모꼴", Font.BOLD, 100));
			g.drawString("P A U S E", Main.SCREEN_WIDTH / 2 - 230, Main.SCREEN_HEIGHT / 2 - 50);
			g.setColor(Color.WHITE);
			g.setFont(new Font("둥근모꼴", Font.BOLD, 100));
			g.drawString("P A U S E", Main.SCREEN_WIDTH / 2 - 240, Main.SCREEN_HEIGHT / 2 - 50);
			g.drawImage(new ImageIcon(getClass().getClassLoader().getResource("icon_home.png")).getImage(), 580, 350, null);
		}
	}

	// 음악 재생 아이콘 그리기
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
