public class BossAttackLeftRight extends BossAttack {
	public BossAttackLeftRight(int y) {
		super(Main.SCREEN_WIDTH, y);
	}

	@Override
	public void fire() {
		setX(getX() - 5);
	}
}