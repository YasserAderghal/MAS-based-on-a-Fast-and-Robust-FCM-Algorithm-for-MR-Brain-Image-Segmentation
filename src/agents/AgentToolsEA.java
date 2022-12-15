package agents;

import java.util.Iterator;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public final class AgentToolsEA {

	public AgentToolsEA() {
	}
	
	public static void register(final Agent myAgent , final String typeService , final String nameService) {
		final DFAgentDescription model = new DFAgentDescription();
		final ServiceDescription service = new ServiceDescription();
		
		service.setType(typeService);
		service.setName(nameService);
		model.addServices(service);
		
		try {
			DFService.register(myAgent, model);
		}catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	public static AID[] searchAgents(final Agent myAgent, final String typeService, final String nameService) {
		final DFAgentDescription model = new DFAgentDescription();
		final ServiceDescription service = new ServiceDescription();
		
		service.setName(nameService);
		service.setType(typeService);
		model.addServices(service);
		
		int nbOthers = 0 ;
		AID[] result = null;
		
		try {
			final DFAgentDescription[] agentsDescription = DFService.search(myAgent, model);
			if ( agentsDescription != null) {
				result = new AID[agentsDescription.length];
				for (int i = 0; i < agentsDescription.length; i++) {
					final AID otherAID = agentsDescription[i].getName();
					
					if (!otherAID.equals(myAgent.getAID())) {
						result[nbOthers++] = otherAID;
					}
				}
			}
		} catch( FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		return result;
		
	}
	
}
