package de.felixperko.fractals.server;

import de.felixperko.fractals.server.data.DataContainer;
import de.felixperko.fractals.server.network.NetworkManager;
import de.felixperko.fractals.server.stateholders.JobStateHolder;
import de.felixperko.fractals.server.stateholders.MainStateHolder;

public class FractalsServerMain {
	
	public static MainStateHolder mainStateHolder;
	public static JobStateHolder jobStateHolder;
	public static NetworkManager networkManager;
	public static DataContainer dataContainer;

	public static void main(String[] args) {
		mainStateHolder = new MainStateHolder();
		jobStateHolder = new JobStateHolder();
		networkManager = new NetworkManager();
		dataContainer = new DataContainer();
	}

}
