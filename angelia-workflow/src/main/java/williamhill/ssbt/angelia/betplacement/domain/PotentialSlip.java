package williamhill.ssbt.angelia.betplacement.domain;

public class PotentialSlip {

	private int stake;
	private boolean valid;
	
	public PotentialSlip(int stake) {
		this.stake = stake;
	}
	
	public int getStake() {
		return stake;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void validate() {
		valid = (stake > 0);
	}
}
