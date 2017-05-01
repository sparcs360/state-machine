package williamhill.ssbt.angelia;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import williamhill.ssbt.angelia.betplacement.domain.PotentialSlip;
import williamhill.ssbt.angelia.betplacement.workflow.BetPlacementStateMachine;
import williamhill.ssbt.angelia.betplacement.workflow.BetPlacementStates;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Autowired
	private BetPlacementStateMachine betPlacementStateMachine;
	
	@Before
	public void before() {
		
		betPlacementStateMachine.reset();
	}
	
	@Test
	public void when_ValidPotentialSlipRecieved_then_TransitionToValidPotentialSlip() {
		
		PotentialSlip potentialSlip = new PotentialSlip(100);
		
		betPlacementStateMachine.receivePotentialSlip(potentialSlip);

		assertThat(betPlacementStateMachine.getCurrentState(), is(BetPlacementStates.VALID_POTENTIAL_SLIP));
	}

	@Test
	public void when_InvalidPotentialSlipRecieved_then_TransitionToInvalidPotentialSlip() {
		
		PotentialSlip potentialSlip = new PotentialSlip(0);
		
		betPlacementStateMachine.receivePotentialSlip(potentialSlip);
		
		assertThat(betPlacementStateMachine.getCurrentState(), is(BetPlacementStates.INVALID_POTENTIAL_SLIP));
	}
}
