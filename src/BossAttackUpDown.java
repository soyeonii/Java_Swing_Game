public class BossAttackUpDown extends BossAttack {
	public BossAttackUpDown(int x) {
		super(x, -56);
	}

	@Override
	public void fire() {
		setY(getY() + 5);
	}
}
