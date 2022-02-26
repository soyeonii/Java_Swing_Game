import java.awt.Image;

import javax.swing.ImageIcon;

public abstract class BossAttack {
	private Image image = new ImageIcon(getClass().getClassLoader().getResource("enemy_2.png")).getImage();
	
	private int x, y;
	private int width;
	private int height = image.getHeight(null);
	private int ATK;
	
	public BossAttack(int x, int y) {
		this.x = x;
		this.y = y;
		this.width = image.getWidth(null);
		this.ATK = 5;
	}
	
	public abstract void fire();
	
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
	
	public void setY(int y) {
		this.y = y;
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
