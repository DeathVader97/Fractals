package de.felixperko.fractals.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.custom.SashForm;
import swing2swt.layout.BoxLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.wb.swt.SWTResourceManager;

import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalRenderer;
import de.felixperko.fractals.FractalRendererSWT;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Controls.KeyListenerControls;
import de.felixperko.fractals.Controls.MouseControls;
import de.felixperko.fractals.Tasks.IterationPositionThread;
import de.felixperko.fractals.state.DiscreteState;
import de.felixperko.fractals.state.RangeState;
import de.felixperko.fractals.state.State;
import de.felixperko.fractals.state.StateChangeAction;
import de.felixperko.fractals.state.StateChangeListener;
import de.felixperko.fractals.state.StateListener;
import de.felixperko.fractals.state.SwitchState;
import de.felixperko.fractals.util.Logger;
import de.felixperko.fractals.util.NumberUtil;
import de.felixperko.fractals.util.Position;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Slider;

public class MainWindow {

	public Shell shell;
	
	Label lbl_disp_dim;
	
	double quality = 1;

	private Label qualitylbl;
	
	FractalRendererSWT mainRenderer;

	private Display display;

	public Canvas canvas;
	
	public boolean save = false;
	boolean redraw = false;

	private Label lblStatus;

	private Label lbl_draw_dim;

	private ArrayList<StateChangeListener<?>> stateChangeListeners = new ArrayList<>();

	int visMouseMoveCouter = 0;
	long visRefreshTime = 0;
	boolean visRedraw = true;
//	/**
//	 * Launch the application.
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		try {
//			MainWindow window = new MainWindow();
//			window.open();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		int i = 0;
		while (!shell.isDisposed()) {
			
			tick();
			
			if (mainRenderer.isRedraw()) {
				redraw = false;
				canvas.redraw();
			}
			
			if (!display.readAndDispatch()) {
//				display.sleep();
				try {
					Thread.sleep(8,333333);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	int lastVisIterations = 0;
	int lastVisJobID = -1;
	
	private void tick() {
		stateChangeListeners.forEach(l -> l.updateIfChanged(true));
		
		IterationPositionThread ips = FractalsMain.threadManager.getIterationWorkerThread();
		int it = ips.getIterations();
		if (it > lastVisIterations || (lastVisJobID != ips.getJobID() && it > 0)){
			lastVisJobID = ips.getJobID();
			lastVisIterations = it;
			canvas.redraw();
		}
		
		testProgressBar(progressBar);
		testProgressBar(progressBar_1);
		
		setText(lbl_disp_dim, mainRenderer.disp_img.getBounds().width+"x"+mainRenderer.disp_img.getBounds().height);
//		setText(lbl_draw_dim, mainRenderer.draw_img.getBounds().width+"x"+mainRenderer.draw_img.getBounds().height);
		lblStatus.setText(FractalsMain.taskManager.isFinished() ? "fertig" : ""+FractalsMain.taskManager.getFinishedDepth());
	}

	private void testProgressBar(ProgressBar pb) {
		int s = pb.getSelection()+ ((Math.random() > 0.5) ? 1 : -1);
		if (s < pb.getMinimum() || s > pb.getMaximum())
			return;
		pb.setSelection(s);
	}

	public void setText(Label label, String text) {
		if (label.getText().equals(text))
			return;
		label.setText(text);
		label.requestLayout();
	}
	
	public void setText(Label label, String text, Color color) {
		org.eclipse.swt.graphics.Color newColor = new org.eclipse.swt.graphics.Color(display, new RGB(color.getRed(), color.getGreen(), color.getBlue()));
		if (!label.getForeground().equals(newColor))
			label.setForeground(newColor);
		setText(label, text);
	}
	
	int finishedDrawingTimer = 0;

	private ProgressBar progressBar;

	private ProgressBar progressBar_1;

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(800, 500);
		if (display.getMonitors().length > 1) {
			Rectangle monitorBounds = display.getMonitors()[1].getBounds();
			Rectangle shellBounds = shell.getBounds();
			shell.setLocation(monitorBounds.x+monitorBounds.width-shellBounds.width, monitorBounds.y+monitorBounds.height-shellBounds.height);
		}
//		shell.setSize(497, 405);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				FractalsMain.shutdown();
			}
		});
		shell.addListener(SWT.RESIZE, new Listener() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("shell resized");
//				mainRenderer.resized();
			}
		});
//		shell.setMaximized(true);
		shell.setText("SWT Application");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite_6 = new Composite(shell, SWT.NO_REDRAW_RESIZE);
		composite_6.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(composite_6, SWT.NONE);
		
		Composite composite_5 = new Composite(sashForm, SWT.NONE);
		composite_5.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		canvas = new Canvas(composite_5, SWT.DOUBLE_BUFFERED);
		canvas.addKeyListener(new KeyListenerControls(this));
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				double factor = e.button == 1 ? 0.5 : (e.button == 3 ? 2 : 1);
				if (factor != 0) {
					mainRenderer.updateLocation(e.x, e.y, factor);
					canvas.redraw();
				}
			}
		});
		canvas.addPaintListener(e -> {mainRenderer.render(e, save);});
		canvas.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("resize canvas");
				mainRenderer.resized();
			}
		});
		canvas.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				
				//Mouse moved on canvas of MainRenderer
				DataDescriptor dd = FractalsMain.taskManager.getDataContainer().getDescriptor();
				FractalsMain.mainStateHolder.getState("cursor position", Position.class).setValue(new Position(e.x, e.y));
				
				//timing of visualization refreshs
				//TODO schedule redraw, probably though main loop execution of redraw
				long t = System.nanoTime();
				IterationPositionThread ips = FractalsMain.threadManager.getIterationWorkerThread();
				if (ips.getIterations() == ips.getMaxIterations())
					finishedDrawingTimer++;
				if (finishedDrawingTimer == 1){
//				if (mainRenderer.allowRedraw){
					visRefreshTime = t;
					visRedraw = true;
				}
				visMouseMoveCouter++;
				
				//visualization refresh
				if (visRedraw){
					visRedraw = false;
					finishedDrawingTimer = 0;
					State<Position> stateCursorImagePosition = FractalsMain.mainStateHolder.getState("cursor image position", Position.class);
					stateCursorImagePosition.setValue(new Position(dd.getStart_x()+e.x*dd.getSpacing(), dd.getStart_y()+e.y*dd.getSpacing()));
					ips.setParameters(stateCursorImagePosition.getValue(), dd, FractalsMain.mainStateHolder.getState("visulization steps", Integer.class).getValue());
				}
			}
		});
		
		TabFolder tabFolder = new TabFolder(sashForm, SWT.NONE);
		
		TabItem tbtmStatus = new TabItem(tabFolder, SWT.NONE);
		tbtmStatus.setText("Eigenschaften");
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		tbtmStatus.setControl(scrolledComposite);
		scrolledComposite.setExpandVertical(true);
		
		Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		Button btnNeuZeichnen = new Button(composite_2, SWT.NONE);
		btnNeuZeichnen.setText("Neu zeichnen");
		btnNeuZeichnen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				canvas.redraw();
			}
		});
		
		Button btnPositionen = new Button(composite_2, SWT.NONE);
		btnPositionen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Point sl = shell.getLocation();
				Point location = new Point(sl.x+shell.getBounds().width/2, sl.y+shell.getBounds().height/2);
				new PositionsShell(display, location);
			}
		});
		btnPositionen.setText("Positionen...");
		
		Button btnBildSpeichern = new Button(composite_2, SWT.NONE);
		btnBildSpeichern.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				save = true;
				System.out.println("save -> true");
				canvas.redraw();
			}
		});
		btnBildSpeichern.setText("Bild speichern...");
		
		Composite composite_8 = new Composite(composite, SWT.NONE);
		composite_8.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite_7 = new Composite(composite_8, SWT.NONE);
		GridLayout gl_composite_7 = new GridLayout(3, false);
		gl_composite_7.horizontalSpacing = 15;
		composite_7.setLayout(gl_composite_7);
		
		Label lblStatusName = new Label(composite_7, SWT.NONE);
		lblStatusName.setText("Status:");
		
		lblStatus = new Label(composite_7, SWT.NONE);
		lblStatus.setText("Vorbereitung");
		new Label(composite_7, SWT.NONE);
		
		Label lblRendergre_1 = new Label(composite_7, SWT.NONE);
		lblRendergre_1.setText("Darstellungsgr\u00F6\u00DFe:");
		
		lbl_disp_dim = new Label(composite_7, SWT.NONE);
		lbl_disp_dim.setText("300x300");
		new Label(composite_7, SWT.NONE);
		
		Label lblBerechnungsgre = new Label(composite_7, SWT.NONE);
		lblBerechnungsgre.setText("Berechnungsgr\u00F6\u00DFe");
		
		lbl_draw_dim = new Label(composite_7, SWT.NONE);
		lbl_draw_dim.setText("300x300");
		new Label(composite_7, SWT.NONE);
		
		Label lblQualitt_1 = new Label(composite_7, SWT.NONE);
		lblQualitt_1.setText("Qualit\u00E4t:");
		
		qualitylbl = new Label(composite_7, SWT.NONE);
		qualitylbl.setText("1.0x");
		
		Composite composite_9 = new Composite(composite_7, SWT.NONE);
		RowLayout rl_composite_9 = new RowLayout(SWT.HORIZONTAL);
		rl_composite_9.center = true;
		rl_composite_9.marginTop = 0;
		rl_composite_9.marginRight = 0;
		rl_composite_9.marginLeft = 0;
		rl_composite_9.marginBottom = 0;
		composite_9.setLayout(rl_composite_9);
		
		Button button_2 = new Button(composite_9, SWT.NONE);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				setQuality(getQuality()*2);
			}
		});
		button_2.setText("+");
		
		Button button_3 = new Button(composite_9, SWT.NONE);
		button_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				setQuality(getQuality()*0.5);
			}
		});
		button_3.setText("-");
		
		
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		
		for (State<?> state : FractalsMain.mainStateHolder.getStates()) {
			
			if (!state.isVisible())
				continue;
			
			Label stateNameLabel = new Label(composite_7, SWT.NONE);
			stateNameLabel.setText(state.getName()+": ");
			
			Label stateValueLabel = new Label(composite_7, SWT.NONE);
			stateValueLabel.setText(state.getValueString());
			
			StateChangeListener<?> changeListener = new StateChangeListener<>(state);
			state.addStateListener(addStateChangeListener(changeListener));
			changeListener.addStateChangeAction(new StateChangeAction() {
				@Override
				public void update() {
					stateValueLabel.setText(state.getValueString());
					stateValueLabel.requestLayout();
				}
			});
			Composite stateComposite = new Composite(composite_7, SWT.NONE);
			RowLayout stateLayout = new RowLayout(SWT.HORIZONTAL);
			stateLayout.center = true;
			stateLayout.marginTop = 0;
			stateLayout.marginRight = 0;
			stateLayout.marginLeft = 0;
			stateLayout.marginBottom = 0;
			stateComposite.setLayout(stateLayout);
			if (state instanceof SwitchState) {
				SwitchState switchState = (SwitchState)state;
				Button btnCheckButton = new Button(stateComposite, SWT.CHECK);
				btnCheckButton.setSelection(switchState.getValue());
				btnCheckButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						switchState.flip();
					}
				});
			}
			if (state instanceof RangeState){
				RangeState rangeState = (RangeState)state;
				Scale scale = new Scale(stateComposite, SWT.NONE);
				scale.setMinimum(rangeState.getMin());
				scale.setMaximum(rangeState.getMax());
				scale.setPageIncrement(rangeState.getStep());
				scale.setSelection(rangeState.getValue());
				scale.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						rangeState.setValue(scale.getSelection());
					}
				});
			}
			if (state instanceof DiscreteState<?>) {
				DiscreteState<?> discreteState = (DiscreteState<?>) state;
				if (discreteState.isIncrementable()) {
					Button btnIncrementState = new Button(stateComposite, SWT.NONE);
					btnIncrementState.setText("+");
					btnIncrementState.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseDown(MouseEvent e) {
							discreteState.setNext();
						}
					});
					changeListener.addStateChangeAction(new StateChangeAction() {
						@Override
						public void update() {
							btnIncrementState.setEnabled(discreteState.getNext() != null);
						}
					});
				}
				
				if (discreteState.isDecrementable()) {
					Button btnDecrementState = new Button(stateComposite, SWT.NONE);
					btnDecrementState.setText("-");
					btnDecrementState.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseDown(MouseEvent e) {
							discreteState.setPrevious();
						}
					});
					changeListener.addStateChangeAction(new StateChangeAction() {
						@Override
						public void update() {
							btnDecrementState.setEnabled(discreteState.getPrevious() != null);
						}
					});
				}
			}
		}
		
		
		TabItem tbtmPerformance = new TabItem(tabFolder, SWT.NONE);
		tbtmPerformance.setText("Performance");
		
		ScrolledComposite scrolledComposite_3 = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tbtmPerformance.setControl(scrolledComposite_3);
		scrolledComposite_3.setExpandHorizontal(true);
		scrolledComposite_3.setExpandVertical(true);
		
		Composite composite_3 = new Composite(scrolledComposite_3, SWT.NONE);
		composite_3.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(composite_3, SWT.NONE);
		lblNewLabel.setText("New Label");
		
		progressBar = new ProgressBar(composite_3, SWT.SMOOTH);
		
		Label lblNewLabel_1 = new Label(composite_3, SWT.NONE);
		lblNewLabel_1.setText("New Label");
		
		progressBar_1 = new ProgressBar(composite_3, SWT.SMOOTH);
		scrolledComposite_3.setContent(composite_3);
		scrolledComposite_3.setMinSize(composite_3.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		TabItem tbtmLog = new TabItem(tabFolder, SWT.NONE);
		tbtmLog.setText("Log");
		
		ScrolledComposite scrolledComposite_2 = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tbtmLog.setControl(scrolledComposite_2);
		scrolledComposite_2.setExpandHorizontal(true);
		scrolledComposite_2.setExpandVertical(true);
		
		Composite composite_1 = new Composite(scrolledComposite_2, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		final StyledText styledText = new StyledText(composite_1, SWT.H_SCROLL | SWT.V_SCROLL);
		styledText.setAlwaysShowScrollBars(false);
		styledText.setEditable(false);
		StateChangeListener<Integer> scl = new StateChangeListener<Integer>(Logger.state);
		scl.addStateChangeAction(new StateChangeAction() {
			@Override
			public void update() {
				StringBuilder text = new StringBuilder();
				for (String str : new ArrayList<>(Logger.getLog()))
					text.append(str).append("\r\n");
				styledText.setText(text.toString());
				styledText.setTopIndex(styledText.getLineCount()-1);
			}
		});
		Logger.state.addStateListener(scl);
		stateChangeListeners.add(scl);
		
		scrolledComposite_2.setContent(composite_1);
		scrolledComposite_2.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);
		
		Menu menu_2 = new Menu(tabFolder);
		tabFolder.setMenu(menu_2);
		
		MenuItem mntmDatei = new MenuItem(menu_2, SWT.CASCADE);
		mntmDatei.setText("Datei");
		
		Menu menu_1 = new Menu(mntmDatei);
		mntmDatei.setMenu(menu_1);
		
		MenuItem mntmNeuZeichnen = new MenuItem(menu_1, SWT.NONE);
		mntmNeuZeichnen.setText("Neu zeichnen");
		
		MenuItem mntmBeenden = new MenuItem(menu_1, SWT.NONE);
		mntmBeenden.setText("Beenden");
		
		MenuItem mntmNetzwerk = new MenuItem(menu_2, SWT.CASCADE);
		mntmNetzwerk.setText("Netzwerk");
		
		Menu menuNetzwerk = new Menu(mntmNetzwerk);
		mntmNetzwerk.setMenu(menuNetzwerk);
		
		MenuItem mntmHost = new MenuItem(menuNetzwerk, SWT.NONE);
		mntmHost.setText("Host");
		mntmHost.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FractalsMain.threadManager.startServer();
			}
		});
		
		MenuItem mntmClient = new MenuItem(menuNetzwerk, SWT.NONE);
		mntmClient.setText("Client");
		mntmClient.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FractalsMain.threadManager.startClient();
			}
		});
		sashForm.setWeights(new int[] {2, 1});

	}

	private <T> StateListener<T> addStateChangeListener(StateChangeListener<T> stateChangeListener) {
		stateChangeListeners.add(stateChangeListener);
		return stateChangeListener;
	}

	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
		qualitylbl.setText(quality+"x");
		mainRenderer.setQuality(quality);
	}

	public Display getDisplay() {
		return display;
	}

	public void setMainRenderer(FractalRendererSWT renderer) {
		mainRenderer = renderer;
	}


	public void loopColor(float additionalOffset) {
		mainRenderer.addColorOffset(additionalOffset);
	}

	public void jumpToSavedLocation(boolean backwards) {
		mainRenderer.setLocation(backwards ? FractalsMain.locationHolder.getPreviousLocation() : FractalsMain.locationHolder.getNextLocation());
	}

	public boolean isRedraw() {
		return redraw;
	}

	public void setRedraw(boolean redraw) {
		this.redraw = redraw;
	}

	public FractalRendererSWT getMainRenderer() {
		return mainRenderer;
	}
}
