package master;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class MasterContainer {
	public static void main(String[] args) {
		try {
			Runtime runtime = Runtime.instance();
			ProfileImpl profileImpl = new ProfileImpl(false);
			
			profileImpl.setParameter( ProfileImpl.MAIN_HOST, "localhost");
			profileImpl.setParameter(ProfileImpl.CONTAINER_NAME, "Master");
			AgentContainer agentContainer = runtime.createAgentContainer(profileImpl);
			
			
			AgentController agentController = agentContainer.createNewAgent("MasterAgent", "master.MasterAgent", new Object[] {});

			agentController.start();
		}catch( ControllerException e) {
			e.printStackTrace();
		}
	}
}
