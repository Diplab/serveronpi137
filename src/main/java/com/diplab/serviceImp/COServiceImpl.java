package com.diplab.serviceImp;

import com.diplab.device.RpiCO;
import com.diplab.service.COService;

public class COServiceImpl implements COService {

	@Override
	public double COppm() {
		return RpiCO.get();
	}

}
