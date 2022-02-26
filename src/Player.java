import java.awt.Image;

public class Player {
	public static final int HP_MAX = 25;
	
	private Image image;
	
	private int x, y;
	private int width, height;
	private int speed = 7;
	private int hp = 25;
	
	private boolean isBuff, isShield, isDamaged;
	private int buffPreCount, shieldPreCount, damagePreCount;

	public Player(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Image getImage() {
		return image;
	}
	
	public void setImage(Image image) {
		this.image = image;
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
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public int getHp() {
		return hp;
	}
	
	public void setHp(int hp) {
		this.hp = hp;
	}
	
	public boolean getIsBuff() {
		return isBuff;
	}
	
	public void setIsBuff(boolean isBuff) {
		this.isBuff = isBuff;
	}
	
	public int getBuffPreCount() {
		return buffPreCount;
	}
	
	public void setBuffPreCount(int buffPreCount) {
		this.buffPreCount = buffPreCount;
	}
	
	public boolean getIsShield() {
		return isShield;
	}
	
	public void setIsShield(boolean isShield) {
		this.isShield = isShield;
	}
	
	public int getShieldPreCount() {
		return shieldPreCount;
	}
	
	public void setShieldPreCount(int shieldPreCount) {
		this.shieldPreCount = shieldPreCount;
	}
	
	public boolean getIsDamaged() {
		return isDamaged;
	}
	
	public void setIsDamaged(boolean isDamaged) {
		this.isDamaged = isDamaged;
	}
	
	public int getDamagePreCount() {
		return damagePreCount;
	}
	
	public void setDamagePreCount(int damagePreCount) {
		this.damagePreCount = damagePreCount;
	}
}
