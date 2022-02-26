import java.awt.Image;

import javax.swing.ImageIcon;

public class EnemyExplosion {
	private Image image;
	
	private int x, y;
	private int count;
	
	public EnemyExplosion(int x, int y, int count) {
		this.image = new ImageIcon(getClass().getClassLoader().getResource("enemy_explosion.png")).getImage();
		this.x = x;
		this.y = y;
		this.count = count;
	}
	
	public Image getImage() {
		return image;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getCount() {
		return count;
	}
}
