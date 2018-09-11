package de.felixperko.fractals.Controls;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Tasks.calculators.infra.AbstractCalculator;
import de.felixperko.fractals.renderer.GridRenderer;
import de.felixperko.fractals.state.stateholders.MainStateHolder;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.Position;
import de.felixperko.fractals.util.Position.SingleOperation;

public class Console {
	
	static CategoryLogger logInfo = CategoryLogger.INFO.createSubLogger("command");
	static CategoryLogger logError = CategoryLogger.ERROR.createSubLogger("command");
	
	public static void enteredCommand(String command) {
		if (command.equalsIgnoreCase("getPosition")) {
			Position pos = FractalsMain.mainStateHolder.stateCursorImagePosition.getValue();
			logInfo.log("position: "+pos);
		} else if (command.equalsIgnoreCase("debugPosition")) {
			Position cursorSpacePos = FractalsMain.mainStateHolder.stateCursorImagePosition.getValue();
			GridRenderer renderer = ((GridRenderer)(FractalsMain.mainWindow.getMainRenderer()));
			Position cursorGridPos = renderer.getGrid().spaceToGrid(cursorSpacePos);
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
