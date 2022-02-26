import java.awt.Image;

import javax.swing.ImageIcon;

public class PlayerAttack {
	private Image image;
	
	private int x, y;
	private int width, height;
	private int ATK;
	
	public PlayerAttack(String image, int x, int y, int ATK) {
		this.image = new ImageIcon(getClass().getClassLoader().getResource(image)).getImage();		
		this.x = x;
		this.y = y;
		this.width = this.image.getWidth(null);
		this.height = this.image.getHeight(null);
		this.ATK = ATK;
	}
	
	public void fire() {
		this.x += 10;
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
