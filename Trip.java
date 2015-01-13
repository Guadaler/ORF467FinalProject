public class Trip {
	private String originPixel;
	private String destinationPixel;
	private double departTime;
	private double arriveTime;
	private double returnTime;
	private int tripSize;
	
	public Trip(String pixel, double dTime, String pixel2, double eTime, double distance, int size) {
		originPixel = pixel;
		destinationPixel = pixel2;
		tripSize = size;
		departTime = dTime;
		arriveTime = eTime;
		returnTime = arriveTime + (1.2 * distance * 3600 / 30) + 600; // returns after 10 minutes and takes however long it took to get to the other pixel to get back
	}

	public double departTime() {
		return departTime;
	}

	public int size() {
		return tripSize;
	}

	public String originPixel() {
		return originPixel;
	}

	public String destinationPixel() {
		return destinationPixel;
	}

	public double returnTime() {
		return returnTime;
	}

	public double arriveTime() {
		return arriveTime;
	}
}