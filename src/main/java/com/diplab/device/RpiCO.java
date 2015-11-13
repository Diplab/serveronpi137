package com.diplab.device;

import com.pi4j.wiringpi.Spi;

public class RpiCO {
	
	/************************Hardware Related Macros************************************/
	private static final int MQ_PIN = 2;     //define which analog input channel you are going to use
	private static final double RL_VALUE  = 5;     //define the load resistance on the board, in kilo ohms
	private static final double RO_CLEAN_AIR_FACTOR    = 9.21;  //RO_CLEAR_AIR_FACTOR=(Sensor resistance in clean air)/RO,
	                                                     //which is derived from the chart in datasheet
	 
	/***********************Software Related Macros************************************/
	private static final int CALIBARAION_SAMPLE_TIMES  = 1;    //define how many samples you are going to take in the calibration phase
	private static final long CALIBRATION_SAMPLE_INTERVAL  = 500;   //define the time interal(in milisecond) between each samples in the
	                                                     //cablibration phase
	private static final long READ_SAMPLE_INTERVAL  = 50;    //define how many samples you are going to take in normal operation
	private static final int READ_SAMPLE_TIMES    = 1;     //define the time interal(in milisecond) between each samples in 
	                                                     //normal operation
	 
	/**********************Application Related Macros**********************************/
	private static final int GAS_CO      = 0;
	 
	/*****************************Globals***********************************************/
	private static double[] COCurve  =  {2,0,-1.53};    //two points are taken from the curve. 
	                                                    //with these two points, a line is formed which is "approximately equivalent" 
	                                                    //to the original curve.
	                                                    //data format:{ x, y, slope}; point1: (lg100, lg1), point2: (lg4000,  lg0.09) 
	                                                   
	private static double           Ro           =  10;                 //Ro is initialized to 10 kilo ohms
	
	static {
		Spi.wiringPiSPISetup(Spi.CHANNEL_0, 500000);
	}

	public static void main(String[] args) throws InterruptedException {

		while (true) {
			System.out.format("co = %-10.3f ppm%n", RpiCO.get());
			Thread.sleep(1000);

		}
	}

	static double readadc_MQ7(int ch) {

		byte data[] = { 1, (byte) ((8 + ch) << 4), 0 };
		Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, data);

		return ((data[1] & 0x03) << 8) + data[2];

	}
	
	

	/****************** MQResistanceCalculation ****************************************
	Input:   raw_adc - raw value read from adc, which represents the voltage
	Output:  the calculated sensor resistance
	Remarks: The sensor and the load resistor forms a voltage divider. Given the voltage
	         across the load resistor and its resistance, the resistance of the sensor
	         could be derived.
	************************************************************************************/ 
	static double MQResistanceCalculation(double raw_adc)
	{
	  return ( ((double)RL_VALUE*(1023-raw_adc)/raw_adc));
	}
	
	/***************************** MQCalibration ****************************************
	Input:   mq_pin - analog channel
	Output:  Ro of the sensor
	Remarks: This function assumes that the sensor is in clean air. It use  
	         MQResistanceCalculation to calculates the sensor resistance in clean air 
	         and then divides it with RO_CLEAN_AIR_FACTOR. RO_CLEAN_AIR_FACTOR is about 
	         10, which differs slightly between different sensors.
	************************************************************************************/ 
	static double MQCalibration(int mq_pin)
	{
	  int i;
	  double val=0;
	 
	  for (i=0;i<CALIBARAION_SAMPLE_TIMES;i++) {            //take multiple samples
	    val += MQResistanceCalculation(readadc_MQ7(mq_pin));
	    try {
			Thread.sleep(CALIBRATION_SAMPLE_INTERVAL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	  }
	  val = val/CALIBARAION_SAMPLE_TIMES;                   //calculate the average value
	 
	  val = val/RO_CLEAN_AIR_FACTOR;                        //divided by RO_CLEAN_AIR_FACTOR yields the Ro 
	                                                        //according to the chart in the datasheet 
	 
	  return val; 
	}
	/*****************************  MQRead *********************************************
	Input:   mq_pin - analog channel
	Output:  Rs of the sensor
	Remarks: This function use MQResistanceCalculation to caculate the sensor resistenc (Rs).
	         The Rs changes as the sensor is in the different consentration of the target
	         gas. The sample times and the time interval between samples could be configured
	         by changing the definition of the macros.
	************************************************************************************/ 
	static double MQRead(int mq_pin)
	{
	  int i;
	  double rs=0;
	 
	  for (i=0;i<READ_SAMPLE_TIMES;i++) {
	    rs += MQResistanceCalculation(readadc_MQ7(mq_pin));
	    try {
	    	Thread.sleep(READ_SAMPLE_INTERVAL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	  }
	 
	  rs = rs/READ_SAMPLE_TIMES;
	 
	  return rs;  
	}
	
	/*****************************  MQGetPpm ******************************************
	Input:   rs_ro_ratio - Rs divided by Ro
	         pcurve      - pointer to the curve of the target gas
	Output:  ppm of the target gas
	Remarks: By using the slope and a point of the line. The x(logarithmic value of ppm) 
	         of the line could be derived if y(rs_ro_ratio) is provided. As it is a 
	         logarithmic coordinate, power of 10 is used to convert the result to non-logarithmic 
	         value.
	************************************************************************************/ 
	static double MQGetPpm(double rs_ro_ratio, double[] pcurve)
	{
	  return Math.pow(10,( ((Math.log(rs_ro_ratio)-pcurve[1])/pcurve[2]) + pcurve[0]));
	}
	 
	/*****************************  MQGetGasPercentage **********************************
	Input:   rs_ro_ratio - Rs divided by Ro
	         gas_id      - target gas type
	Output:  ppm of the target gas
	Remarks: This function passes different curves to the MQGetPercentage function which 
	         calculates the ppm (parts per million) of the target gas.
	************************************************************************************/ 
	static double MQGetGasPercentage(double rs_ro_ratio, int gas_id)
	{
	  if ( gas_id == GAS_CO ) {
	     return MQGetPpm(rs_ro_ratio,COCurve);
	  }    
	 
	  return 0;
	}
	
	
	
	public static double get(){
		Ro = 0.167406;
		double Rs = MQRead(MQ_PIN);
		
		return MQGetGasPercentage(Rs/Ro,GAS_CO);
		
	}

}
