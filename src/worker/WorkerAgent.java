package worker;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import jade.core.AID;
import jade.core.Agent;
import jade.core.FEConnectionManager;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;


import static java.lang.System.out;


public class WorkerAgent extends Agent {
	protected void setup() {
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(this.getAID());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("image-processing");
		serviceDescription.setName("image-processing");
		
		agentDescription.addServices(serviceDescription);
		try {
			DFService.register(this, agentDescription);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Agent : "+this.getAID().getName());
		addBehaviour(new CyclicBehaviour(){
			@Override
			public void action() {
				// TODO Auto-generated method stub 
				try {
					MessageTemplate template = MessageTemplate.or( 
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM ), 
							MessageTemplate.MatchPerformative(ACLMessage.REQUEST ) );
					ACLMessage aclMessage = receive(template);
					
					
					if(aclMessage!=null) {
						switch(aclMessage.getPerformative()) {
						case ACLMessage.REQUEST: {
							
							out.println("Worker agent: " + getName() + "Recieved a job from: " + aclMessage.getSender().getName());
							ImageIcon img = (ImageIcon) aclMessage.getContentObject();
							Image img_temp = img.getImage();
//							BufferedImage image = (BufferedImage) img_temp;
							BufferedImage image = convertToBufferedImage(img_temp);
							int image_type = image.getType();
							ArrayList<ArrayList<Float>> data = getMatrixOfImage(image);
							
							FuzzyClustering FCM = new FuzzyClustering( data , 3, 10 , 2.f , 0.5f);
							data = null;
							data = FCM.run();
							
							image = null;
							image = getImageOfMatrix(data, image_type);
//							data = FCM.data;
//							out.print(data);
//						    for(int i=0; i< data.size() ; i++) {
//						        for(int j=0; j< data.get(0).size(); j++) {
//						            
//						            int a = data.get(i).get(j).intValue();
//						            
//						            Color newColor = new Color(a,a,a);
//						            image.setRGB(j,i,newColor.getRGB());
//						        }
//						    }
//							
							try {
		    	 				ImageIO.write(image, "jpg",new File("imgFilter1.jpg"));    	 		        
		  					} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    	 			
		    	 			//send to master

							ACLMessage reply=aclMessage.createReply();
							reply.setPerformative(ACLMessage.CONFIRM);
							
							reply.setContentObject(new ImageIcon(image));
							
							myAgent.send(reply);
							break;
						}
						}
						
													
					}
					else block();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage convertToBufferedImage(Image image)
	{
	    BufferedImage newImage = new BufferedImage(
	        image.getWidth(null), image.getHeight(null),
	        BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = newImage.createGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	    return newImage;
	}
	
	
	private ArrayList<ArrayList<Float>> getMatrixOfImage(BufferedImage bufferedImage) {
	    int width = bufferedImage.getWidth(null);
	    int height = bufferedImage.getHeight(null);
	    ArrayList<ArrayList<Float>> pixels = new ArrayList<>();
	    for (int i = 0; i < width; i++) {
	    	pixels.add( new ArrayList<Float>());
	        for (int j = 0; j < height; j++) {	
	            pixels.get(i).add(new Float(  bufferedImage.getRGB(i, j)));
	            
	        }
	    }

	    return pixels;
	}
	
	private BufferedImage getImageOfMatrix(ArrayList<ArrayList<Float>> image, int type) {
	    int width = image.get(0).size();
	    int height = image.size();
	    BufferedImage pixels = new BufferedImage(width, height , type);
	    for (int i = 0; i < width; i++) {
	    	out.println(i + ": " + width + " : " + height);
	        for (int j = 0; j < height; j++) {
	            pixels.setRGB(j, i, image.get(i).get(j).intValue() %256);
	            
	        }
	    }

	    return pixels;
	}
}
