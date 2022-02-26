import java.awt.Image;

import javax.swing.ImageIcon;

public class Item {
	private Image image;
	
	private int x, y;
	private int width, height;
	private int number;
	
	public Item(int x, int y, int number) {
		this.image = new ImageIcon(getClass().getClassLoader().getResource("item_" + number + ".png")).getImage();
		this.x = x;
		this.y = y;
		this.width = this.image.getWidth(null);
		this.height = this.image.getHeight(null);
		this.number = number;
	}
	
	public void move() {
		x -= 3;
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
	
	public int getNumber() {
		return number;
	}
}
