package com.diplab.serviceImp;

import com.diplab.device.RpiTemperature;
import com.diplab.service.TemperatureService;

public class TemperatureServiceImpl implements TemperatureService {

	@Override
	public double readTemperature() {
		return RpiTemperature.getTemperature();
	}

}
