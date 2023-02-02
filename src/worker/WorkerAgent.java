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
							BufferedImage image = toBufferedImage(img_temp);
							int image_type = image.getType();
							int width = image.getWidth();
							int height = image.getHeight();
							
							out.println(width + ":" + height);
							
							
							ArrayList<ArrayList<Float>> data = getMatrixOfImage(image);
							
							
//							out.println(data);
							FuzzyClustering FCM = new FuzzyClustering( data,1);
							data = null;
							
							ArrayList<Integer> segmentation = FCM.run(3,50);
							
							for(int i =0; i< segmentation.size();i++)
								out.println(segmentation.get(i));
							
//							image = null;
							image = getImageOfMatrix(segmentation, image_type, width , height);

							
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
	
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	private ArrayList<ArrayList<Float>> getMatrixOfImage(BufferedImage bufferedImage) {
	    int width = bufferedImage.getWidth();
	    int height = bufferedImage.getHeight();
	    ArrayList<ArrayList<Float>> pixels = new ArrayList<>();
	
	    int k = 0;

	    for(int i = 0 ; i< height; ++i) {
    		for(int j = 0 ; j< width;++j) {
    			pixels.add( new ArrayList<Float>());
    			
    			pixels.get(k).add( new Float(  bufferedImage.getRGB(i, j)) );
    			k++;
    		}
    	}
	    
	    out.println(width + ":" + height);
	   
	    out.println("size "+ pixels.size());

	    out.println("finished here");
	    return pixels;
	}
	
	private BufferedImage getImageOfMatrix(ArrayList<Integer> image, int type , int width , int height) {
		int k = 0;
	    BufferedImage pixels = new BufferedImage(width, height , type);
	    for (int i = 0; i < width; i++) {
	    	out.println(i + ": " + width + " : " + height);
	        for (int j = 0; j < height; j++) {
	            pixels.setRGB(j, i, image.get(k++) %256);
	            
	        }
	    }

	    return pixels;
	}
}
