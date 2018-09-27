package de.felixperko.fractals.client.controls;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.server.calculators.infrastructure.AbstractCalculator;
import de.felixperko.fractals.server.util.CategoryLogger;
import de.felixperko.fractals.server.util.Position;

public class Console {
	
	static CategoryLogger logInfo = CategoryLogger.INFO.createSubLogger("command");
	static CategoryLogger logError = CategoryLogger.ERROR.createSubLogger("command");
	
	public static void enteredCommand(String command) {
		if (command.equalsIgnoreCase("getPosition")) {
			Position pos = FractalsMain.clientStateHolder.stateCursorImagePosition.getValue();
			logInfo.log("position: "+pos);
		} else if (command.equalsIgnoreCase("debugPosition")) {
//			Position cursorSpacePos = FractalsMain.clientStateHolder.stateCursorImagePosition.getValue();
			GridRenderer renderer = ((GridRenderer)(FractalsMain.mainWindow.getMainRenderer()));
//			Position cursorGridPos = renderer.getGrid().spaceToGrid(cursorSpacePos);
			Position cursorGridPos = FractalsMain.clientStateHolder.stateCursorGridPosition.getValue();
			int cursorChunkIndex = renderer.getGrid().getIndexFromGridPos(cursorGridPos);
			Position cursorChunkGridPos = new Position((int)cursorGridPos.getX(), (int)cursorGridPos.getY());
//			double newX = ;
//			cursorChunkGridPos.setX(cursorChunkGridPos.getX() % 1);
//			cursorChunkGridPos.setY(cursorChunkGridPos.getY() % 1);
			AbstractCalculator.setDebug(cursorChunkGridPos, cursorChunkIndex);
			logInfo.log("set debug to: "+cursorChunkGridPos+" "+cursorChunkIndex);
			renderer.getGrid().reset();
		}
		else {
			logError.log("Command not found: "+command);
		}
	}
}
