package de.felixperko.fractals.server.state;

public class SwitchState extends State<Boolean>{
	
	public SwitchState(String name, Boolean value) {
		super(name, value);
	}

	public void flip() {
		setValue(!getValue());
	}
}
