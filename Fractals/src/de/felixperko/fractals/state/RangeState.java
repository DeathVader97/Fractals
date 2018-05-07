package de.felixperko.fractals.state;

public class RangeState extends State<Integer> {
	
	Integer min, max;
	Integer step;
	
	public RangeState(String name, Integer value) {
		super(name, value);
	}
	
	public void setProperties(Integer min, Integer max, Integer step){
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}
}
