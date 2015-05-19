package com.geag.engine.decide;

public class GeagDecisionEngine {

	/*
	 * w: amount of computation 
	 * sm: speed of mobile system 
	 * ss: speed of server
	 * di: input data 
	 * bw: bandwidth 
	 * pm: power on mobile system 
	 * pc: power required to send data from mobile system over the network 
	 * pi: power required to poll the network interface while waiting for the result of the offloaded computation
	 */
	public boolean offloadingImprovesPerformance(double w, double di, double bw,
			double ss, double sm) {

		double eq1 = w / sm;
		double eq2 = (di / bw) + (w / ss);

		if (eq1 > eq2) {
			return true;
		}

		return false;
	}

	public boolean offloadingSavesEnergy(double pm, double w, double sm, double pc,
			double di, double bw, double pi, double ss) {

		double eq4 = pm * (w / sm);
		double eq5 = pc * (di / bw) + pi * (w / ss);

		if (eq4 > eq5) {
			return true;
		}

		return false;
	}

}
