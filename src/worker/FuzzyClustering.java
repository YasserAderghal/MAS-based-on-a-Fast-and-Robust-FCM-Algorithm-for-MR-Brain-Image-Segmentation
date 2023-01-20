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
    public ArrayList<ArrayList<Float>> clusterCenters;
    private float u[][];
    private float u_pre[][];
    private int clusterCount;
    private int iteration;
    private int dimension;
    private int fuzziness;
    private double epsilon;
    public double finalError;

    public FuzzyClustering(ArrayList<ArrayList<Float>> data , int dimension, int clusterNumber,int iter){
        this.data = data;
        clusterCenters = new ArrayList<>();
        fuzziness = 2;
        epsilon = 0.01;
        this.dimension= dimension; 
        this.clusterCount = clusterNumber;
        this.iteration = iter;
    }

    public void run(){
        
        

        //start algorithm
        //1 assign initial membership values
        assignInitialMembership();

        for (int i = 0; i < this.iteration; i++) {
            //2 calculate cluster centers
            calculateClusterCenters();

            //3
            updateMembershipValues();

            //4
            finalError = checkConvergence();
            if(finalError <= epsilon)
                break;
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
        for (int i = 0; i < clusterCount; i++) {
            ArrayList<Float> tmp = new ArrayList<>();
            for (int j = 0; j < dimension; j++) {
                float cluster_ij;
                float sum1 = 0;
                float sum2 = 0;
                for (int k = 0; k < data.size(); k++) {
                    double tt = Math.pow(u[k][i], fuzziness);
                    sum1 += tt * data.get(k).get(j);
                    sum2 += tt;
                }
                cluster_ij = sum1/sum2;
                tmp.add(cluster_ij);
            }
            clusterCenters.add(tmp);
        }
    }

    /**
     * in this function we will update membership value
     */
    private void updateMembershipValues(){
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < clusterCount; j++) {
                u_pre[i][j] = u[i][j];
                float sum = 0;
                float upper = Distance(data.get(i), clusterCenters.get(j));
                for (int k = 0; k < clusterCount; k++) {
                    float lower = Distance(data.get(i), clusterCenters.get(k));
                    sum += Math.pow((upper/lower), 2/(fuzziness -1));
                }
                u[i][j] = 1/sum;
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
    
    
    

}