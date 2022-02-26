import java.awt.Image;

import javax.swing.ImageIcon;

public class Enemy {
	private Image[] images;
	
	private int x, y;
	private int width, height;
	private int hp;
	
	public Enemy(int x, int y) {
		this.images = new Image[] {
				new ImageIcon(getClass().getClassLoader().getResource("enemy_0.png")).getImage(),
				new ImageIcon(getClass().getClassLoader().getResource("enemy_1.png")).getImage(),
				new ImageIcon(getClass().getClassLoader().getResource("enemy_2.png")).getImage(),
				new ImageIcon(getClass().getClassLoader().getResource("enemy_3.png")).getImage(),
				new ImageIcon(getClass().getClassLoader().getResource("enemy_4.png")).getImage()
		};
		this.x = x;
		this.y = y;
		this.width = 70;
		this.height = 70;
		this.hp = 10;
	}
	
	public void move() {
		this.x -= 5;
	}
	
	public Image getImage(int index) {
		return images[index];
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getHp() {
		return hp;
	}
	
	public void setHp(int hp) {
		this.hp = hp;
	}
}
