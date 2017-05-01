package williamhill.ssbt.angelia.betplacement.workflow;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import williamhill.ssbt.angelia.betplacement.domain.PotentialSlip;

@Component
public class BetPlacementStateMachine {

	@Autowired
	private StateMachine<BetPlacementStates, BetPlacementEvents> stateMachine;

	@Configuration
	@EnableStateMachine
	private static class BetPlacementStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<BetPlacementStates, BetPlacementEvents>{
		
		private static final Logger LOGGER = LoggerFactory.getLogger(BetPlacementStateMachineConfiguration.class);

		@SuppressWarnings("unused")
		public BetPlacementStateMachineConfiguration() {
			super();			
		}
		
		@Override
		public void configure(StateMachineConfigurationConfigurer<BetPlacementStates, BetPlacementEvents> config)
				throws Exception {

			config
				.withConfiguration()
					.machineId("BetPlacement")
					.autoStartup(true)
					.listener(listener())
			;
		}

		@Override
		public void configure(StateMachineStateConfigurer<BetPlacementStates, BetPlacementEvents> states) throws Exception {

			states
				.withStates()
					.states(EnumSet.allOf(BetPlacementStates.class))
					.initial(BetPlacementStates.INITIAL)
					.choice(BetPlacementStates.UNVALIDATED_POTENTIAL_SLIP)
			;
		}
		
		@Override
		public void configure(StateMachineTransitionConfigurer<BetPlacementStates, BetPlacementEvents> transitions)
				throws Exception {

			transitions
				.withExternal()
					.source(BetPlacementStates.INITIAL)
					.event(BetPlacementEvents.POTENTIAL_SLIP_RECEIVED)
					.target(BetPlacementStates.UNVALIDATED_POTENTIAL_SLIP)
					.action(actionValidatePotentialSlip())
				.and()
				.withChoice()
					.source(BetPlacementStates.UNVALIDATED_POTENTIAL_SLIP)
					.first(BetPlacementStates.VALID_POTENTIAL_SLIP, guardPotentialSlipIsValid())
					.last(BetPlacementStates.INVALID_POTENTIAL_SLIP)
			;
		}
		
	    @Bean
	    public StateMachineListener<BetPlacementStates, BetPlacementEvents> listener() {
	    	
	        return new StateMachineListenerAdapter<BetPlacementStates, BetPlacementEvents>() {
	        	
	        	@Override
	        	public void stateMachineStarted(StateMachine<BetPlacementStates, BetPlacementEvents> stateMachine) {
	            	LOGGER.info("stateMachineStarted: {}", stateMachine);
	        	}
	        	
	            @Override
	            public void stateChanged(State<BetPlacementStates, BetPlacementEvents> from, State<BetPlacementStates, BetPlacementEvents> to) {
	            	LOGGER.info("stateChanged: {}->{}", from==null ? "null" : from.getId(), to.getId());
	            }
	            
	            @Override
	            public void eventNotAccepted(Message<BetPlacementEvents> event) {
	            	LOGGER.info("eventNotAccepted: {}", event);
	            }
	            
	        	@Override
	        	public void stateMachineStopped(StateMachine<BetPlacementStates, BetPlacementEvents> stateMachine) {
	            	LOGGER.info("stateMachineStopped: {}", stateMachine);
	        	}
	        };
	    }
	    
	    @Bean
	    public Action<BetPlacementStates, BetPlacementEvents> actionValidatePotentialSlip() {
	    	
	    	return new Action<BetPlacementStates, BetPlacementEvents>() {

				@Override
				public void execute(StateContext<BetPlacementStates, BetPlacementEvents> context) {
	            	LOGGER.info("actionValidatePotentialSlip");
					PotentialSlip potentialSlip = (PotentialSlip)context.getMessageHeader("potentialSlip");
					potentialSlip.validate();
				}
			};
	    }
	    
	    @Bean
	    public Guard<BetPlacementStates, BetPlacementEvents> guardPotentialSlipIsValid() {
	    	
	    	return new Guard<BetPlacementStates, BetPlacementEvents>() {

				@Override
				public boolean evaluate(StateContext<BetPlacementStates, BetPlacementEvents> context) {
					PotentialSlip potentialSlip = (PotentialSlip)context.getMessageHeader("potentialSlip");
					boolean result = potentialSlip.isValid();
	            	LOGGER.info("guardPotentialSlipIsValid={}", result);
					return result;
				}
	    	};
	    }
	}
	
	public BetPlacementStateMachine() {
	}

	public void reset() {
		stateMachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<BetPlacementStates,BetPlacementEvents>>() {
			
			@Override
			public void apply(StateMachineAccess<BetPlacementStates, BetPlacementEvents> function) {
				function.resetStateMachine(null);
			}
		});
	}

	public void receivePotentialSlip(PotentialSlip potentialSlip) {

	    Message<BetPlacementEvents> message = MessageBuilder
	            .withPayload(BetPlacementEvents.POTENTIAL_SLIP_RECEIVED)
	            .setHeader("potentialSlip", potentialSlip)
	            .build();
		stateMachine.sendEvent(message);
	}

	public BetPlacementStates getCurrentState() {
		return stateMachine.getState().getId();
	}
}
