package de.felixperko.fractals.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class UtilTest {
	
	@Test
	public void testComplexSquared() {
		Position p = new Position(0, 1);
		Position pSq = new Position(-1, 0);
		assertEquals(p.performOperation(Position.complexSquared), pSq);
	}
	
	@Test
	public void testAdd() {
		Position p = new Position(1, 0);
		Position p2 = new Position(0.25, 1.25);
		Position p3 = new Position(1.25, 1.25);
		assertEquals(p.performOperation(Position.addNew, p2), p3);
	}
}
