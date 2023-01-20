package master;

import static java.lang.System.out;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MasterAgent extends Agent {
	private String file;
	private AID requester;
	private List<AID> workers = new ArrayList<>();
	private int size ;

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		
		
		System.out.println("Master agent : "+this.getAID().getName());

		addBehaviour(new CyclicBehaviour(){
			@Override
			public void action() {
				// TODO Auto-generated method stub
				size = 0;
				try {
					MessageTemplate template = MessageTemplate.or( 
									MessageTemplate.MatchPerformative(ACLMessage.CONFIRM ), 
									MessageTemplate.MatchPerformative(ACLMessage.REQUEST ) );
					ACLMessage aclMessage = receive(template);
					
					
					if(aclMessage!=null) {
						switch(aclMessage.getPerformative()) {
						case ACLMessage.REQUEST : {
							System.out.println("From: " + aclMessage.getSender().getName());
							file = aclMessage.getContent();
							requester = aclMessage.getSender();
							
							workers = chercherServices(myAgent, "image-processing");
							size = workers.size();
							
							for(AID aid:workers) {
								out.println("====" + aid.getName());
							}
							
							BufferedImage[] imgs = splitImage(file, size);
							
							
							
							
							
							for( int i = 0 ; i < size ; i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
								ImageIcon img = new ImageIcon(imgs[i]);
								
								msg.setContentObject( img );
								
								msg.addReceiver( workers.get(i));
								msg.setConversationId(String.valueOf(i));
								send(msg);
								out.println("Message is sent to: " + workers.get(i).getName());
								
							}
							
							
							
							
							break;
						}
						case ACLMessage.CONFIRM: {
							
							out.println("Confirmed from worker agent: " + aclMessage.getSender().getName());
							out.println("Path: " + aclMessage.getContentObject());
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
	
	public List<AID> chercherServices(Agent agent, String type) {
		List<AID> workers = new ArrayList<>();
		DFAgentDescription agentDescription = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(type);
		agentDescription.addServices(serviceDescription);
		try {
			DFAgentDescription[] descriptions = DFService.search( agent , agentDescription);
			for( DFAgentDescription dfad:descriptions) {
				workers.add(dfad.getName());
			}
		}catch( FIPAException e) {
			e.printStackTrace();
		}
		return workers;
	}
	
	
	public BufferedImage[] splitImage(String file , int size) {
		
		BufferedImage image = null;
	        try {
	        	image = ImageIO.read(new File(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
		// initalizing rows and columns
        int rows = size;
        int columns = size;

        // initializing array to hold subimages
        BufferedImage imgs[] = new BufferedImage[16];

        // Equally dividing original image into subimages
        int subimage_Width = image.getWidth() / columns;
        int subimage_Height = image.getHeight() / rows;
        
        int current_img = 0;
        
        // iterating over rows and columns for each sub-image
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < columns; j++)
            {
                // Creating sub image
                imgs[current_img] = new BufferedImage(subimage_Width, subimage_Height, image.getType());
                Graphics2D img_creator = imgs[current_img].createGraphics();

                // coordinates of source image
                int src_first_x = subimage_Width * j;
                int src_first_y = subimage_Height * i;

                // coordinates of sub-image
                int dst_corner_x = subimage_Width * j + subimage_Width;
                int dst_corner_y = subimage_Height * i + subimage_Height;
                
                img_creator.drawImage(image, 0, 0, subimage_Width, subimage_Height, src_first_x, src_first_y, dst_corner_x, dst_corner_y, null);
                current_img++;
            }
        }
        
      //writing sub-images into image files
        for (int i = 0; i < 16; i++)
        {
            File outputFile = new File("/home/yasser/Project/Java/eclipse-workflow/MAS-based-on-a-Fast-and-Robust-FCM-Algorithm-for-MR-Brain-Image-Segmentation/images/" + "sub-img" + i + ".jpg");
            try {
            	ImageIO.write(imgs[i], "jpg", outputFile);
            }catch(Exception e) {
            	e.printStackTrace();
            }
        }
        System.out.println("Sub-images have been created.");
        
        return imgs;
	}
}
