package worker;
// this code is based on  https://github.com/amoazeni75/fuzzy-C-mean-clustering/blob/master/src/FuzzyClustering.java

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FuzzyClustering {
    public ArrayList<ArrayList<Float>> data;
    public ArrayList<ArrayList<Float>> new_data ;
    public ArrayList<ArrayList<Float>> clusterCenters;
    public float u[][];
    private float u_pre[][];
    private int clusterCount;
    private int iteration;
    private int dimension;
    private int fuzziness;
    private float gamma;
    private float alpha;
    private double epsilon;
    public double finalError;
    private int width ;
    private int height ;

    public FuzzyClustering(ArrayList<ArrayList<Float>> data ,  int clusterNumber,int iter, float gamma , float alpha){
    	this.width = data.get(0).size();
    	this.height= data.size();
    	int size = data.size() * data.get(0).size();
    	int k =0;
    	this.data = new ArrayList<ArrayList<Float>>();
    	for(int i = 0 ; i< height; ++i) {
    		for(int j = 0 ; j< width;++j) {
    			this.data.add( new ArrayList<Float>());
    			
    			this.data.get(k).add(data.get(i).get(j));
    			k++;
    		}
    	}
    	this.data = data;
    		
        
        clusterCenters = new ArrayList<>();
        fuzziness = 2;
        epsilon = 0.01;
        this.dimension= 1; 
        this.clusterCount = clusterNumber;
        this.iteration = iter;
         
        this.alpha = alpha;
        
        this.gamma = gamma;
    }

    public void  run(){
        

        //start algorithm
        //1 assign initial membership values
        assignInitialMembership();

        for (int i = 0; i < this.iteration; i++) {
            //2 calculate cluster centers
            calculateClusterCenters();

            //3
            updateMembershipValues();

            //4
            modifyMembershipValues();
            //5
            finalError = checkConvergence();
            if(finalError <= epsilon)
                break;
        }
        
        this.new_data =new ArrayList<>();
        int k = 0;
        for(int i = 0 ; i< height; ++i) {
    		for(int j = 0 ; j< width;++j) {
    			
    			this.new_data.add( new ArrayList<Float>());
    			this.new_data.get(i).add( data.get(k).get(0) );
    			k++;
    		}
    	}
      
        data = null;
        data = new_data;
        
        
        
        
        
    }

    private void modifyMembershipValues() {
    	for (int i = 0; i < data.size(); i++) {
            float big = 0;
            int big_j = 0;
            for (int j = 0; j < clusterCount; j++) {
                if( u[i][j] > big) {
                	big = u[i][j];
                	big_j = j;	
                }
            }
            
            for (int j = 0; j < clusterCount; j++) {
            	if( j == big_j) {
            		
                    u[i][j] =  1 - this.gamma + this.gamma*u[i][j];
            		
            	}
            	if ( j != big_j) {
            		u[i][j] = this.gamma * u[i][j];
            	}
            }
        }
    }

    /**
     * this function generate membership value for each data
     */
    private void assignInitialMembership(){
        u = new float[data.size()][clusterCount];
        u_pre = new float[data.size()][clusterCount];
        Random r = new Random();
        for (int i = 0; i < data.size(); i++) {
            float sum = 0;
            for (int j = 0; j < clusterCount; j++) {
                u[i][j] = r.nextFloat() * 10 + 1;
                sum += u[i][j];
            }
            for (int j = 0; j < clusterCount; j++) {
                u[i][j] = u[i][j] / sum;
            }
        }
    }

    /**
     * in this function we calculate value of each cluster
     */
    private void calculateClusterCenters(){
        clusterCenters.clear();
        int interval = 9;
        for (int i = 0; i < clusterCount; i++) {
            ArrayList<Float> tmp = new ArrayList<>();
            for (int j = 0; j < dimension; j++) {
                float cluster_ij;
                float sum1 = 0;
                float sum2 = 0;
                
                
                for (int k = 0; k < data.size(); k++) {
                	float mean = 0;
                	for ( int x = -3 ; x<=3 ; x++) {
                		for (int y = -3 ; y<=3 ; y++) {
                			if ( j+ x <0 || j+x >= dimension) {
                    			continue;
                    		}
                			
                			if ( k + y < 0 || k +y>= data.size()) {
                				continue;
                			}
                			
                			mean += data.get(k+y).get(j+x);
                		}
                		
                		
                	}
                	mean = (float)data.get(k).stream().reduce(0.0f,(a,b) -> a+b);
                	
                	mean = mean / dimension;
                	
                	
                	

                	
                    double tt = Math.pow(u[k][i], fuzziness);
                    sum1 += tt * (data.get(k).get(j)  + this.alpha * mean);
                    sum2 += tt;
                   

                }
                cluster_ij = sum1/(sum2 * (1+ (float)this.alpha ));
                tmp.add(cluster_ij);
            }
            clusterCenters.add(tmp);
        }
    }

    /**
     * in this function we will update membership value
     */
    private void updateMembershipValues(){
    	int interval =  9 ;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < clusterCount; j++) {
                u_pre[i][j] = u[i][j];
                float sum = 0;


                float upper = Distance(data.get(i), clusterCenters.get(j));
                
                
                float mean = 0;
                
                mean = (float)data.get(i).stream().reduce(0.0f,(a,b) -> a+b);
            	
            	mean = mean / dimension;
            	
            	
            	upper += this.alpha * ( Distance(mean , clusterCenters.get(j)));
            	upper = (float) Math.pow(upper, -1/(fuzziness -1));
                
            	
                for (int k = 0; k < clusterCount; k++) {
                    float lower = Distance(data.get(i), clusterCenters.get(k));
                    lower +=  this.alpha * ( Distance(mean , clusterCenters.get(k)));
                    sum += Math.pow(lower, -1/(fuzziness -1));
                }
                u[i][j] = upper/sum;
            }
        }
    }

    /**
     * get norm 2 of two point
     * @param p1
     * @param p2
     * @return
     */
    private float Distance(ArrayList<Float> p1, ArrayList<Float> p2){
        float sum = 0;
        for (int i = 0; i < p1.size(); i++) {
            sum += Math.pow(p1.get(i) - p2.get(i), 2);
        }
        sum = (float) Math.sqrt(sum);
        return sum;
    }
    private float Distance(float p1, ArrayList<Float> p2){
        float sum = 0;
        for (int i = 0; i < p2.size(); i++) {
            sum += Math.pow(p1 - p2.get(i), 2);
        }
        sum = (float) Math.sqrt(sum);
        return sum;
    }
    private float Distance(ArrayList<Float> p1, float p2){
        float sum = 0;
        for (int i = 0; i < p1.size(); i++) {
            sum += Math.pow(p1.get(i) - p2, 2);
        }
        sum = (float) Math.sqrt(sum);
        return sum;
    }
    
    /**
     * we calculate norm 2 of ||U - U_pre||
     * @return
     */
    private double checkConvergence(){
        double sum = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < clusterCount; j++) {
                sum += Math.pow(u[i][j] - u_pre[i][j], 2);
            }
        }
        return Math.sqrt(sum);
    }

    /**
     * write random generated data to file for visualizing
     * @throws IOException
     */
    public void writeDataToFile(ArrayList<ArrayList<Float>> inpData, String fileName) throws IOException {

        FileWriter fileWriter = new FileWriter("./" + fileName + ".csv");
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (int i = 0; i < inpData.size(); i++) {
             String res = "";
            for (int j = 0; j < inpData.get(i).size(); j++) {
                if(j == inpData.get(i).size() - 1)
                    res += inpData.get(i).get(j);
                else
                    res += inpData.get(i).get(j) +",";
            }
            printWriter.println(res);
        }
        printWriter.close();
    }
    
    
    public void writeDataToFile(float[][] inpData, String fileName) throws IOException {

        FileWriter fileWriter = new FileWriter("./" + fileName + ".csv");
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (int i = 0; i < inpData.length; i++) {
             String res = "";
            for (int j = 0; j < inpData[i].length; j++) {
                if(j == inpData[i].length - 1)
                    res += inpData[i][j];
                else
                    res += inpData[i][j] +",";
            }
            printWriter.println(res);
        }
        printWriter.close();
    }
    
    
     public static ArrayList<ArrayList<Float>> createRandomData(int numberOfData, int dimension, int minRange, int maxRange, int clusterCount){
    	 ArrayList<ArrayList<Float>> data = new ArrayList<>();
        ArrayList<ArrayList<Integer>> centroids = new ArrayList<>();
        centroids.add(new ArrayList<Integer>());
        int[] numberOfDataInEachArea = new int[clusterCount];
        int range = maxRange - minRange + 1;
        int step = range / (clusterCount + 1);
        for (int i = 1; i <= clusterCount; i++) {
            centroids.get(0).add(minRange + i * step);
        }

        for (int i = 0; i < dimension - 1; i++) {
            centroids.add((ArrayList<Integer>) centroids.get(0).clone());
        }
        double variance = (centroids.get(0).get(1) - centroids.get(0).get(0))/ 2.5;
        for (int i = 0; i < dimension; i++) {
            Collections.shuffle(centroids.get(i));
        }
        Random r = new Random();
        int sum = 0;
        for (int i = 0; i < clusterCount; i++) {
            int rg = r.nextInt(50) + 10;
            numberOfDataInEachArea[i] = (rg);
            sum += rg;
        }
        for (int i = 0; i < clusterCount; i++)
            numberOfDataInEachArea[i] = (int)((((double)numberOfDataInEachArea[i]) / sum) * numberOfData);

        Random fRandom = new Random();
        for (int i = 0; i < clusterCount; i++) {
            for (int j = 0; j < numberOfDataInEachArea[i]; j++) {
                ArrayList<Float> tmp = new ArrayList<>();
                for (int k = 0; k < dimension; k++) {
                    tmp.add((float)(centroids.get(k).get(i) + fRandom.nextGaussian() * variance));
                }
                data.add(tmp);
            }
        }
        return data;
    }
}
