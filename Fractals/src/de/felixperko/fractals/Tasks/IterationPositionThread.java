package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.util.Position;

public class IterationPositionThread extends Thread {
	
	DataDescriptor dataDescriptor;
	ArrayList<Position> positions = new ArrayList<>();
	Position startPos = null;
	Position currentPos = null;
	int maxIterations = 0;
	int iterations = 0;
	SampleCalculator sampleCalculator = new SampleCalculator(null, null);
	int jobId;
	
	@Override
	public void run() {
		while (!Thread.interrupted()){
			while (iterations >= maxIterations){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			while (iterations < maxIterations){
				synchronized (this) {
					
					if (sampleCalculator.getDescriptor() != dataDescriptor)
						sampleCalculator.setDescriptor(dataDescriptor);
					
					currentPos = sampleCalculator.getIterationForPosition(startPos, currentPos);
					positions.add(currentPos);
					iterations++;
				}
			}
		}
	}

	public DataDescriptor getDataDescriptor() {
		return dataDescriptor;
	}

	public void setDataDescriptor(DataDescriptor dataDescriptor) {
		this.dataDescriptor = dataDescriptor;
	}

	public Position getStartPos() {
		return startPos;
	}

	public synchronized void setParameters(Position startPos, DataDescriptor dataDescriptor, int maxIterations) {
		this.startPos = startPos;
		this.currentPos = null;
		this.dataDescriptor = dataDescriptor;
		this.maxIterations = maxIterations;
		this.positions.clear();
		this.iterations = 0;
		this.jobId++;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public ArrayList<Position> getPositions() {
		return new ArrayList<>(positions);
	}

	public int getIterations() {
		return iterations;
	}

	public int getJobID() {
		return jobId;
	}
}
