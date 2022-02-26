import java.awt.Image;

import javax.swing.ImageIcon;

public class EnemyAttack {
	private Image image;
	
	private int x, y;
	private int width, height;
	private int ATK;

	public EnemyAttack(int x, int y) {
		this.image = new ImageIcon(getClass().getClassLoader().getResource("enemy_projectile.png")).getImage();
		this.x = x;
		this.y = y;
		this.width = image.getWidth(null);
		this.height = image.getHeight(null);
		this.ATK = 5;
	}

	public void fire() {
		this.x -= 10;
	}
	
	public Image getImage() {
		return image;
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
	
	public int getATK() {
		return ATK;
	}
}
