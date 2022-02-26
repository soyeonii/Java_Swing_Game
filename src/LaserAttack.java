import java.awt.Image;

import javax.swing.ImageIcon;

public class LaserAttack {
	private Image laserImage, warningImage;
	
	private int y;
	private int laserHeight, warningHeight;
	private int ATK;
	
	private boolean isLaunched;	// 레이저가 발사되었는지
	private boolean isTouched;	// 플레이어에 닿았는지
	
	public LaserAttack() {
		this.laserImage = new ImageIcon(getClass().getClassLoader().getResource("laser.png")).getImage();
		this.warningImage = new ImageIcon(getClass().getClassLoader().getResource("laser_warning.png")).getImage();
		this.laserHeight = laserImage.getHeight(null);
		this.warningHeight = warningImage.getHeight(null);
		this.ATK = 10;
		this.isLaunched = false;
		this.isTouched = false;
	}
	
	public Image getLaserImage() {
		return laserImage;
	}
	
	public Image getWarningImage() {
		return warningImage;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getLaserHeight() {
		return laserHeight;
	}
	
	public int getWarningHeight() {
		return warningHeight;
	}
	
	public int getATK() {
		return ATK;
	}
	
	public boolean getIsLaunched() {
		return isLaunched;
	}
	
	public void setIsLaunched(boolean isLaunched) {
		this.isLaunched = isLaunched;
	}
	
	public boolean getIsTouched() {
		return isTouched;
	}
	
	public void setIsTouched(boolean isTouched) {
		this.isTouched = isTouched;
	}
}
