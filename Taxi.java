import java.util.*;

public class Taxi implements Comparable {
	private String originPixel;
	private int size;
	private double startTime;
	private double endTime;
	private List<Trip> trips;

	public Taxi(int tripSize) {
		size = tripSize;
		startTime = Double.POSITIVE_INFINITY;
		endTime = 0;
		trips = new ArrayList<Trip>();
	}

	public void addTrip(Trip trip) {
		// make sure the trip originates from the origin pixel of this taxi
		if (originPixel != null) {
			if (!trip.originPixel().equals(originPixel))
				return;
		}
		else {
			originPixel = trip.originPixel();
		}

		if (trip.size() > size) return; // if trip size is too big for taxi...
		
		trips.add(trip);

		// update the start and end time of taxi, if needed
		if (trip.departTime() < startTime)
			startTime = trip.departTime();

		if (trip.returnTime() > endTime)
			endTime = trip.returnTime();

	}

	// add the trips in the other taxi to this taxi
	public void combineTaxi(Taxi taxi) {
		for (Trip trip : taxi.trips) {
			addTrip(trip);
		}
	}

	public Double startTime() {
		return (Double)startTime;
	}

	public int size() {
		return size;
	}

	public Double endTime() {
		return (Double)endTime;
	}

	public boolean contains(Trip trip) {
		return trips.contains(trip);
	}

	public int compareTo(Object obj) {
		Taxi other = (Taxi) obj;
		return ((Double)startTime).compareTo(other.startTime());
	}

	public int totalTrips() {
		return trips.size();
	}

	public void show() {
		System.out.println("Taxi of size " + size + "leaving at " + startTime + " and returning at " + endTime);
		System.out.println("Itinerary:\n=================");
		System.out.println();

		for (Trip trip : trips) {
			System.out.println("Trip from " + trip.originPixel() + " to " + trip.destinationPixel());
			System.out.println("===============================");
			System.out.println("Depart time: " + trip.departTime() + "\nReturn time: " + trip.returnTime());
		}

		System.out.println("\n");
	}


}