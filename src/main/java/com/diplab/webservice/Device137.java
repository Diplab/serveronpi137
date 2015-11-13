package com.diplab.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.Endpoint;

import com.diplab.service.COService;
import com.diplab.service.SmokeService;
import com.diplab.service.TemperatureService;
import com.diplab.serviceImp.COServiceImpl;
import com.diplab.serviceImp.SmokeServiceImpl;
import com.diplab.serviceImp.TemperatureServiceImpl;

@WebService
@SOAPBinding(style = Style.RPC)
public class Device137 {

	COService coService = new COServiceImpl();
	SmokeService smokeService = new SmokeServiceImpl();
	TemperatureService temperatureService = new TemperatureServiceImpl();


	@WebMethod
	public double COppm() {
		return coService.COppm();
	}

	@WebMethod
	public double getSmokePpm() {
		return smokeService.getSmokePpm();
	}
	
	@WebMethod
	public double readTemperature() {
		return temperatureService.readTemperature();
	}

	public static void main(String[] args) {
		Endpoint.publish("http://0.0.0.0:9005/webservice/Device137",
				new Device137());

		System.out.println("open webservice137");

	}

}
