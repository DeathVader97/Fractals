package de.felixperko.fractals.client.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.data.Location;

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
		
		Composite compositeGoTo = new Composite(this, SWT.NONE);
		Button btnGoTo = new Button(compositeGoTo, SWT.NONE);
		
		GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_combo.widthHint = 221;
		combo.setLayoutData(gd_combo);
		ArrayList<Location> locs = FractalsMain.locationHolder.getLocations();
		if (!locs.isEmpty()) {
			locs.forEach(l -> combo.add(l.getName()));
			combo.setText(locs.get(0).getName());
		}
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean validSelection = combo.getSelectionIndex() != -1;
				if (validSelection) {
					compositeGoTo.setToolTipText(null);
					btnGoTo.setEnabled(true);
				} else {
					compositeGoTo.setToolTipText("No position selected");
					btnGoTo.setEnabled(false);
				}
			}
		});

		boolean validSelection = combo.getSelectionIndex() != -1;
		if (validSelection) {
			compositeGoTo.setToolTipText(null);
			btnGoTo.setEnabled(true);
		} else {
			compositeGoTo.setToolTipText("No position selected");
			btnGoTo.setEnabled(false);
		}
		btnGoTo.setSize(41, 25);
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
		
		Composite compositeSave = new Composite(this, SWT.NONE);
		Button btnSave = new Button(compositeSave, SWT.NONE);
		
		GridData gd_txtName = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtName.widthHint = 218;
		txtName.setLayoutData(gd_txtName);
		txtName.setMessage("Name");
		txtName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				boolean empty = txtName.getText().length() == 0;
				boolean nameExists = FractalsMain.locationHolder.getLocations().stream().anyMatch(l -> l.getName().equals(txtName.getText()));
				if (empty) {
					compositeSave.setToolTipText("Please enter a name first.");
					btnSave.setEnabled(false);
				} else if (nameExists) {
					compositeSave.setToolTipText("Name is already in use.");
					btnSave.setEnabled(false);
				} else {
					compositeSave.setToolTipText(null);
					btnSave.setEnabled(true);
				}
			}
		});
		btnSave.setSize(36, 25);
		
		btnSave.setText("Save");
		compositeSave.setToolTipText("Please enter a name first.");
		btnSave.setEnabled(false);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FractalsMain.locationHolder.addLocation(FractalsMain.mainWindow.mainRenderer.getLocation(txtName.getText()));

				combo.removeAll();
				ArrayList<Location> locs = FractalsMain.locationHolder.getLocations();
				if (!locs.isEmpty()) {
					locs.forEach(l -> combo.add(l.getName()));
					combo.setText(locs.get(0).getName());
				}
				btnGoTo.setEnabled(combo.getSelectionIndex() != -1);
			}
		});
		createContents(location);
		open();
		layout();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents(Point location) {
		setText("Positions");
		setSize(402, 101);
		setLocation(location.x - getBounds().width/2, location.y - getBounds().height/2);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
