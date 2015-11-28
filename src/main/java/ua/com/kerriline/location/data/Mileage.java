package ua.com.kerriline.location.data;

public class Mileage {
	
	private String tankNumber = "";
	private String mileage = "";
	private String mileageDate = "";
	private String restMileage = "";
	
	public String getMileageDate() {
		return mileageDate;
	}

	public String getRestMileage() {
		return restMileage;
	}

	public void setTankNumber(String tankNumber) {
		this.tankNumber = tankNumber;
	}


	public Mileage(String tankNumber) {
		this.tankNumber = tankNumber;
	}

	public String getTankNumber() {
		return tankNumber;		
	}

	public String getMileage() {
		return mileage;
	}

	public void setMileage(String mileage) {
		this.mileage = mileage;
	}

	public void setMileageDate(String mileageDate) {
		this.mileageDate = mileageDate;		
	}

	public void setRestMileage(String restMileage) {
		this.restMileage = restMileage;
	}

	@Override
	public String toString() {
		return "Mileage [tankNumber=" + tankNumber + ", mileage=" + mileage + ", mileageDate=" + mileageDate + ", restMileage="
				+ restMileage + "]";
	}

}
