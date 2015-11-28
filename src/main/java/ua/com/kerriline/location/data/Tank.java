package ua.com.kerriline.location.data;

import java.util.Map;

public class Tank {
	@Override
	public String toString() {
		return "Tank [tankNumber=" + tankNumber + ", mileage=" + mileage + "]";
	}

	private String tankNumber;
	private Mileage mileage;

	public Tank(String tankNumber) {
		this.tankNumber = tankNumber;
	}

	public String getTankNumber() {
		return tankNumber;
	}

	public void setTankNumber(String tankNumber) {
		this.tankNumber = tankNumber;
	}

	public static Tank parse(Map<String, String> e) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMileage(Mileage mileage) {
		this.mileage = mileage;
	}
}
