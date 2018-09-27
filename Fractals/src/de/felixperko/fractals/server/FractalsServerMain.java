package de.felixperko.fractals.server;

import de.felixperko.fractals.server.network.NetworkManager;
import de.felixperko.fractals.server.stateholders.MainStateHolder;

public class FractalsServerMain {
	
	public static MainStateHolder mainStateHolder;
	public static NetworkManager networkManager;

	public static void main(String[] args) {
		mainStateHolder = new MainStateHolder();
		networkManager = new NetworkManager();
	}

}
