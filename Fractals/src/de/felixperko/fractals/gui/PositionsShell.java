package de.felixperko.fractals.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Location;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class PositionsShell extends Shell {
	private Text txtName;

//	/**
//	 * Launch the application.
//	 * @param args
//	 */
//	public static void main(String args[]) {
//		try {
//			Display display = Display.getDefault();
//			PositionsShell shell = new PositionsShell(display, new Point(display.getBounds().width/2, display.getBounds().height/2));
//			while (!shell.isDisposed()) {
//				if (!display.readAndDispatch()) {
//					display.sleep();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public PositionsShell(Display display, Point location) {
		super(display, SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.TITLE);
		
		setLayout(new GridLayout(3, false));
		
		Label lblChoosePosition = new Label(this, SWT.NONE);
		lblChoosePosition.setText("Choose Position:");
		
		CCombo combo = new CCombo(this, SWT.BORDER);
		GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_combo.widthHint = 221;
		combo.setLayoutData(gd_combo);
		ArrayList<Location> locs = FractalsMain.locationHolder.getLocations();
		if (!locs.isEmpty()) {
			locs.forEach(l -> combo.add(l.getName()));
			combo.setText(locs.get(0).getName());
		}
		
		Button btnGoTo = new Button(this, SWT.NONE);
		btnGoTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FractalsMain.mainWindow.mainRenderer.setLocation(FractalsMain.locationHolder.getLocations().get(combo.getSelectionIndex()));
			}
		});
		btnGoTo.setText("Go to");
		
		Label lblSaveCurrentPosition = new Label(this, SWT.NONE);
		lblSaveCurrentPosition.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSaveCurrentPosition.setText("Save current Position:");
		
		txtName = new Text(this, SWT.BORDER);
		GridData gd_txtName = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtName.widthHint = 218;
		txtName.setLayoutData(gd_txtName);
		txtName.setMessage("Name");
		
		Button btnSave = new Button(this, SWT.NONE);
		btnSave.setText("Save");
		createContents(location);
		open();
		layout();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents(Point location) {
		setText("Positions");
		setSize(411, 108);
		setLocation(location.x - getBounds().width/2, location.y - getBounds().height/2);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
