import java.awt.Image;
import java.awt.Toolkit;

public class Boss {
	private Image image;
	
	private int x, y;
	private int width, height;
	private int hp;
	private int appearTime;

	public Boss(int x, int y) {
		this.image = Toolkit.getDefaultToolkit().createImage(getClass().getClassLoader().getResource("boss.gif"));
		this.x = x;
		this.y = y;
		this.width = 256;
		this.height = 256;
		this.hp = 100;
		this.appearTime = 1000;
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
	
	public int getAppearTime() {
		return appearTime;
	}
}
