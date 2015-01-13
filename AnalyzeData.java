import java.util.*;
import java.io.*;

public class AnalyzeData {
	static Map<String, List<Taxi>> findTaxi = new HashMap<String, List<Taxi>>(); // helper structure

	public static void main(String[] args) {
		BufferedReader reader = null;
		int totalDepartures = 0; // total departures from pixel
		int totalArrivals = 0; // total arrivals from pixel
		Map<String, List<Trip>> dTaxis = new HashMap<String, List<Trip>>(); // raw departure info from file
		Map<String, List<Trip>> aTaxis = new HashMap<String, List<Trip>>(); // raw arrival info from file
		List<Taxi> finalTaxis = new ArrayList<Taxi>(); // saves the final minimum amount of taxis

		try {
			String line;
			reader = new BufferedReader(new FileReader(args[0])); //departure file should be 1st argument
			
			// read each line in file
			while ((line = reader.readLine()) != null) {
				String[] rawData = line.split(","); //split the data
				
				// get the size of the trip
				int riders = Integer.parseInt(rawData[17]);

				int size = 0; // determine the size of the taxi
				if (riders <= 2)
					size = 2;

				else if (riders > 2 && riders <= 6)
					size = 6;

				else if (riders > 6 && riders <= 15)
					size = 15;

				else if (riders > 15 && riders <= 50)
					size = 50;

				else
					size = 150;


				String pixel = rawData[1] + ", "  + rawData[2]; //get origin pixel
				String pixel2 = rawData[22] + ", " + rawData[23]; //get destination pixel
				Trip trip = new Trip(pixel, Double.parseDouble(rawData[3]), pixel2, Double.parseDouble(rawData[24]), Double.parseDouble(rawData[18]), size); // get the required trip data from line
				Taxi taxi = new Taxi(size);
				taxi.addTrip(trip);

				finalTaxis.add(taxi); // start with each final taxi only having 1 trip

				// save departure trip information
				List<Trip> dTrips;
				if (dTaxis.containsKey(pixel2)) {
					dTrips = dTaxis.get(pixel2);
				}
				else {
					dTrips = new ArrayList<Trip>();
				}
				
				dTrips.add(trip);
				dTaxis.put(pixel2, dTrips);

				List<Taxi> taxis;
				if (findTaxi.containsKey(pixel2))
					taxis = findTaxi.get(pixel2);
				else
					taxis = new ArrayList<Taxi>();

				taxis.add(taxi);
				findTaxi.put(pixel2, taxis);

				totalDepartures++;
			}

			/*----------------------------------------------------------------*/

			reader = new BufferedReader(new FileReader(args[1])); //arrival file should be 2nd argument
			while ((line = reader.readLine()) != null) {
				String[] rawData = line.split(","); //split the data

				// get the size of the trip
				int riders = Integer.parseInt(rawData[17]);

				int size = 0; // determine the size of the taxi
				if (riders <= 2)
					size = 2;

				else if (riders > 2 && riders <= 6)
					size = 6;

				else if (riders > 6 && riders <= 15)
					size = 15;

				else if (riders > 15 && riders <= 50)
					size = 50;

				else
					size = 150;

				String pixel = rawData[1] + ", "  + rawData[2]; //origin pixel
				String pixel2 = rawData[22] + ", " + rawData[23]; //get destination pixel
				Trip trip = new Trip(pixel, Double.parseDouble(rawData[3]), pixel2, Double.parseDouble(rawData[24]), Double.parseDouble(rawData[18]), size); // get the required trip data from line

				// save arrival information
				List<Trip> aTrips;
				if (aTaxis.containsKey(pixel))
					aTrips = aTaxis.get(pixel);
				
				else
					aTrips = new ArrayList<Trip>();

				aTrips.add(trip);
				aTaxis.put(pixel, aTrips);

				totalArrivals++;
			}
		}

		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null){
				try {reader.close();} catch (Exception e) {}
			}
		}

		System.out.println("Number of pixels, to which the taxis depart: " + dTaxis.size());
		System.out.println("Number of taxis if each trip has its own taxi: " + totalDepartures);
		System.out.println("Total trips into pixel: " + totalArrivals);

		finalTaxis = compare(dTaxis, aTaxis, finalTaxis);

		System.out.println("Number of taxis taking out returning arrivals: " + finalTaxis.size());
		System.out.println("Number of foreign taxis used: " + (totalDepartures - finalTaxis.size()));

		Collections.sort(finalTaxis, new CustomComparator()); // sort final list of departing taxis by departure time
		finalTaxis = update(finalTaxis);

		System.out.println("Min number of cycling taxis needed: " + finalTaxis.size());

		int taxi2 = 0;
		int taxi6 = 0;
		int taxi15 = 0;
		int taxi50 = 0;
		int other = 0;
		int totalTrips = 0;

		for (Taxi t : finalTaxis) {
			if (t.size() == 2)
				taxi2++;

			else if (t.size() == 6)
				taxi6++;

			else if (t.size() == 15)
				taxi15++;

			else if (t.size() == 50)
				taxi50++;

			else
				other++;

			totalTrips += t.totalTrips();
		}

		System.out.println("Number of 2 passenger taxis needed: " + taxi2);
		System.out.println("Number of 2-6 passenger taxis needed: " + taxi6);
		System.out.println("Number of 6-15 passenger taxis needed: " + taxi15);
		System.out.println("Number of 15-50 passenger taxis needed: " + taxi50);
		System.out.println("Number of >50 passenger taxis needed: " + other);
		System.out.println("Total trips covered by taxis: " + totalTrips);
	}

	// compare arrival taxis to departure taxis (if a taxi arrives within 10 min before a departure, rider will take the arrival taxi back instaed of a new departure taxi)
	public static List<Taxi> compare(Map<String, List<Trip>> dTaxis, Map<String, List<Trip>> aTaxis, List<Taxi> fTaxis) {
		// make a copy of the arrival taxi information
		Map<String, List<Trip>> arrivals = new HashMap<String, List<Trip>>();
		arrivals.putAll(aTaxis);

		Set<String> departures = dTaxis.keySet();

		// for each pixel a taxi is departing to
		for (String departTo : departures)
			if (arrivals.containsKey(departTo)) { // see if there is a taxi arriving from that pixel
				List<Trip> dTrips = dTaxis.get(departTo);
				List<Trip> aTrips = aTaxis.get(departTo);

				// get all the departure times
				List<Double> dTimes = new ArrayList<Double>();
				for (Trip trip : dTrips) {
					dTimes.add(trip.departTime());
				}
				
				// get all the arrival times
				List<Double> aTimes = new ArrayList<Double>(); 
				for (Trip trip : aTrips) {
					aTimes.add(trip.arriveTime());
				}

				// for each departure trip
				for (Trip dTrip : dTrips) {
					for (Trip trip : aTrips) { //compare with all the arrival trips
						if (trip.arriveTime() <= dTrip.departTime() && trip.arriveTime() >= (dTrip.departTime()-600)) //if within the 10 min before departure
							if (aTimes.contains(trip.arriveTime())) { // if the time hasnt already been found by another taxi

								//find the taxi corresponding to the departure trip, from the total list of departing taxis
								List<Taxi> taxis = findTaxi.get(departTo);
								for (Taxi taxi : taxis)
									if (taxi.contains(dTrip) && trip.size() >= taxi.size()) { // if it is the corresponding taxi, and the arrival size can fit the departing size
										if (fTaxis.contains(taxi)) { // replace the departing taxi with the returning taxi
											fTaxis.remove(taxi);
											aTimes.remove(trip.arriveTime()); // remove the arrival time, so that it cant be found by another departing taxi
										}
									}

							}
					}
				}
			}

		return fTaxis; //return updated list of departing taxi
	}


	// update the final taxi information, so that each taxi has multiple trips (instead of just one)
	public static List<Taxi> update(List<Taxi> fTaxis) {
		Map<Double, List<Taxi>> returnTaxis = new TreeMap<Double, List<Taxi>>(); //list of returned taxis
		List<Taxi> finalTaxis = new ArrayList<Taxi>();

		// for each departing taxis
		for (Taxi taxi : fTaxis) {
			Set<Double> returnTimes = returnTaxis.keySet(); // get the return times of the taxis that have returned thus far
			
			//marker variable to know which taxi to remove from return taxis list
			Taxi current = null;

			for (double time : returnTimes) {
				if (time <= taxi.startTime()) { //check if any taxis have returned, before this taxi departs
					// get taxis associated with return time
					List<Taxi> currentTaxis = returnTaxis.get(time);
					
					// check if any taxis associated with departure time are big enough to carry the current departing taxi
					for (Taxi t : currentTaxis) {
						if (t.size() == taxi.size()) {
							current = t; // update marker
							taxi.combineTaxi(t); // add trips from other taxi to this taxi
							finalTaxis.remove(t); // remove other taxi from final list of departing taxis
							break;
						}
					}
				}

				if (current != null) break;
			}

			if (current != null) {
				List<Taxi> currentTaxis = returnTaxis.get(current.endTime());
				currentTaxis.remove(current);

				if (currentTaxis.size() == 0) returnTaxis.remove(current.endTime());
			}

			// add current taxi to final list of departing taxis
			finalTaxis.add(taxi);

			// update the list of return times
			List<Taxi> rTaxis;
			if (returnTaxis.containsKey(taxi.endTime()))
				rTaxis = returnTaxis.get(taxi.endTime());
			else
				rTaxis = new ArrayList<Taxi>();

			rTaxis.add(taxi);
			returnTaxis.put(taxi.endTime(), rTaxis);
		}

		return finalTaxis;
	}
	
}

// comparator to sort the list of taxis by departure time
class CustomComparator implements Comparator<Taxi> {
	@Override
	public int compare(Taxi t1, Taxi t2) {
		return t1.startTime().compareTo(t2.startTime());
	}
}