package de.felixperko.fractals.server.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class LocationHolder{
	
	File file = new File("locations.txt");
	
	ArrayList<Location> locations = new ArrayList<>();
	
	int currentPos = 0;
	
	public LocationHolder() {
		load();
	}
	
	public void save() {
		ArrayList<String> lines = new ArrayList<>();
		
		for (Location location : locations)
			lines.add(location.serialize());
		
		try {
			Files.write(file.toPath(), lines);
			System.out.println("saved "+locations.size()+" locations.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		if (!file.exists())
			return;
		locations.clear();
		try {
			for (String s : Files.readAllLines(file.toPath()))
				locations.add(new Location(s));
			System.out.println("loaded "+locations.size()+" locations.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Location getLocation() {
		return locations.get(currentPos);
	}
	
	public Location getNextLocation() {
		if (++currentPos > locations.size()-1)
			currentPos = 0;
		return getLocation();
	}
	
	public Location getPreviousLocation() {
		if (--currentPos < 0)
			currentPos = locations.size()-1;
		return getLocation();
	}

	public void addLocation(Location location) {
		locations.add(location);
		save();
	}

	public ArrayList<Location> getLocations() {
		return locations;
	}
}
