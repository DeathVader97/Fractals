package de.felixperko.fractals;

public class FractalsMain {
	
	private static WindowHandler windowHandler;
	
	public static void main(String[] args) {
		windowHandler = new WindowHandler();
		
		startRendering();
	}

	private static void startRendering() {
		while (!Thread.interrupted())
			windowHandler.render();
	}

	public static WindowHandler getWindowHandler() {
		return windowHandler;
	}
}
