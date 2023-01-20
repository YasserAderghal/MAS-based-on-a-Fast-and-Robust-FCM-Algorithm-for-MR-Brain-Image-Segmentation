package worker;


import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

public class WorkerContainer {

	public static void main(String[] args) {
		try {
			int num = 0 + (int)(Math.random() * ((50 - 0) + 1));
			Runtime runtime = Runtime.instance();
			ProfileImpl profileImp = new ProfileImpl(false);
			profileImp.setParameter(Profile.MAIN_HOST, "localhost");
			profileImp.setParameter(Profile.CONTAINER_NAME, "Worker Cotnainer - "+num);
			AgentContainer agentContainer = runtime.createAgentContainer(profileImp);
			
			AgentController agentController = agentContainer.createNewAgent("Worker_"+num, "worker.WorkerAgent", new Object[] {});
			agentController.start();
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
