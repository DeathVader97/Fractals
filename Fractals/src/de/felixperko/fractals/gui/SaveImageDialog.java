package de.felixperko.fractals.gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Label;
import swing2swt.layout.BorderLayout;
import swing2swt.layout.FlowLayout;
import swing2swt.layout.BoxLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Control;

public class SaveImageDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text_render_res_width;
	private Text text_render_res_height;
	private Text text_image_res_width;
	private Text text_image_res_height;
	private Text text_scale;
	private Text text_saveLocation;
	protected Dialog thisDialog = this;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SaveImageDialog(Shell parent, int style) {
		super(parent, style);
		setText("Save image...");
		open();
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.CLOSE | SWT.MIN | SWT.TITLE);
//		shell.setSize(325, 202);
		shell.setText(getText());
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite_column = new Composite(shell, SWT.NONE);
		RowLayout rl_composite_column = new RowLayout(SWT.VERTICAL);
		rl_composite_column.justify = true;
		composite_column.setLayout(rl_composite_column);
		
		Composite composite_row_saveLocation = new Composite(composite_column, SWT.NONE);
		composite_row_saveLocation.setLayoutData(new RowData(263, SWT.DEFAULT));
		GridLayout gl_composite_row_saveLocation = new GridLayout(3, false);
		gl_composite_row_saveLocation.verticalSpacing = 0;
		gl_composite_row_saveLocation.marginWidth = 0;
		gl_composite_row_saveLocation.marginHeight = 0;
		composite_row_saveLocation.setLayout(gl_composite_row_saveLocation);
		
		Label lbl_saveLocation = new Label(composite_row_saveLocation, SWT.NONE);
		lbl_saveLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl_saveLocation.setText("Save location");
		
		text_saveLocation = new Text(composite_row_saveLocation, SWT.BORDER);
		text_saveLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btn_saveLocation = new Button(composite_row_saveLocation, SWT.NONE);
		btn_saveLocation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
		        FileDialog fd = new FileDialog(shell);
		        fd.setText("Select save location...");
		        fd.setFilterPath("C:/");
		        String[] filterExt = { "*.png", "*.jpg", "*.*" };
		        fd.setFilterExtensions(filterExt);
//		        String selected = fd.getFileName();
//		        System.out.println(selected);
			}
		});
		btn_saveLocation.setText("Browse...");
		composite_row_saveLocation.setTabList(new Control[]{btn_saveLocation, text_saveLocation});
		
		Button btnRecalculate = new Button(composite_column, SWT.CHECK);
		btnRecalculate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnRecalculate.setText("Recalculate");
		
		Composite composite_row_scale = new Composite(composite_column, SWT.NONE);
		RowLayout rl_composite_row_scale = new RowLayout(SWT.HORIZONTAL);
		rl_composite_row_scale.marginTop = 0;
		rl_composite_row_scale.marginRight = 0;
		rl_composite_row_scale.marginLeft = 0;
		rl_composite_row_scale.marginBottom = 0;
		rl_composite_row_scale.center = true;
		composite_row_scale.setLayout(rl_composite_row_scale);
		
		Button btn_scale = new Button(composite_row_scale, SWT.CHECK);
		btn_scale.setText("scale by");
		
		text_scale = new Text(composite_row_scale, SWT.BORDER);
		text_scale.setLayoutData(new RowData(26, SWT.DEFAULT));
		
		Button btn_scaleIncrease = new Button(composite_row_scale, SWT.NONE);
		btn_scaleIncrease.setText("+");
		
		Button button_scaleDecrease = new Button(composite_row_scale, SWT.NONE);
		button_scaleDecrease.setText("-");
		
		Composite composite_row_res = new Composite(composite_column, SWT.NONE);
		GridLayout gl_composite_row_res = new GridLayout(4, false);
		gl_composite_row_res.marginLeft = 2;
		gl_composite_row_res.marginRight = 2;
		gl_composite_row_res.marginHeight = 0;
		gl_composite_row_res.verticalSpacing = 3;
		gl_composite_row_res.marginWidth = 0;
		gl_composite_row_res.horizontalSpacing = 3;
		composite_row_res.setLayout(gl_composite_row_res);
		
		Label lbl_render_res = new Label(composite_row_res, SWT.NONE);
		lbl_render_res.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl_render_res.setText("Render resolution");
		
		text_render_res_width = new Text(composite_row_res, SWT.BORDER);
		text_render_res_width.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lbl_render_res_separator = new Label(composite_row_res, SWT.NONE);
		lbl_render_res_separator.setText("x");
		
		text_render_res_height = new Text(composite_row_res, SWT.BORDER);
		text_render_res_height.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lbl_image_res = new Label(composite_row_res, SWT.NONE);
		lbl_image_res.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl_image_res.setText("Image resolution");
		
		text_image_res_width = new Text(composite_row_res, SWT.BORDER);
		text_image_res_width.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lbl_image_res_separator = new Label(composite_row_res, SWT.NONE);
		lbl_image_res_separator.setText("x");
		
		text_image_res_height = new Text(composite_row_res, SWT.BORDER);
		text_image_res_height.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_buttons = new Composite(composite_column, SWT.NONE);
		RowLayout rl_composite_buttons = new RowLayout(SWT.HORIZONTAL);
		rl_composite_buttons.marginTop = 0;
		rl_composite_buttons.marginLeft = 0;
		rl_composite_buttons.marginRight = 0;
		rl_composite_buttons.marginBottom = 0;
		composite_buttons.setLayout(rl_composite_buttons);
		
		Button btnCancel = new Button(composite_buttons, SWT.NONE);
		btnCancel.setText("Cancel");
		
		Button btnSave = new Button(composite_buttons, SWT.NONE);
		btnSave.setText("Save");
		composite_column.setTabList(new Control[]{btnRecalculate, composite_row_saveLocation, composite_row_scale, composite_row_res, composite_buttons});
		shell.setTabList(new Control[]{composite_column});

	}
}
