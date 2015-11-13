package com.diplab.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RpiTemperature {
	
	//This directory created by 1-wire kernel modules
	  static String w1DirPath = "/sys/bus/w1/devices";
	private static BufferedReader br;
	private static String filename = "28-000006c59f4d";
	  
	  public static double getTemperature(){
		  double value = 0;
//		  System.out.print(filename + ": ");
		   // Device data in w1_slave file
		          String filePath = w1DirPath + "/" + filename + "/w1_slave";
		          File f = new File(filePath);
		          try {
					br = new BufferedReader(new FileReader(f));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		          try {
		            String output = br.readLine();
		            if (output.endsWith("YES")) {
		                String tempLine = br.readLine();
		                int equals = tempLine.indexOf('=');
		                double tempC = Double.parseDouble(tempLine.substring(equals + 1));
		                value = 0.001 * tempC;
//		                System.out.format("T = %.3f %n", value);
		            }
		          }
		          catch (IOException e) {
		              e.printStackTrace();
		          }
//		    br.close();
			return value;
	  }
	  
	  public static void main(String[] args) throws Exception {
		  
		  System.out.println("Test");
		  System.out.format("T = %.3f %n", RpiTemperature.getTemperature());
		  
	  }

}
