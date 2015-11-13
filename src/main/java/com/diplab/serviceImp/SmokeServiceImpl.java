package com.diplab.serviceImp;

import com.diplab.device.RpiSmoke;
import com.diplab.service.SmokeService;

public class SmokeServiceImpl implements SmokeService {

	@Override
	public double getSmokePpm() {
		return RpiSmoke.get();
	}

}
