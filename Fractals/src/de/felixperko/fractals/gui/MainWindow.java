package de.felixperko.fractals.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalRendererSWT;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Controls.KeyListenerControls;
import de.felixperko.fractals.Tasks.Task;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.threading.IterationPositionThread;
import de.felixperko.fractals.Tasks.threading.ThreadManager;
import de.felixperko.fractals.Tasks.threading.WorkerThread;
import de.felixperko.fractals.state.DiscreteState;
import de.felixperko.fractals.state.RangeState;
import de.felixperko.fractals.state.State;
import de.felixperko.fractals.state.StateChangeAction;
import de.felixperko.fractals.state.StateChangeListener;
import de.felixperko.fractals.state.StateListener;
import de.felixperko.fractals.state.SwitchState;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.Logger;
import de.felixperko.fractals.util.Message;
import de.felixperko.fractals.util.Position;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.custom.StyledText;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.TraverseEvent;

public class MainWindow {
	
	static CategoryLogger inputLogger = new CategoryLogger("command", Color.GREEN);

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
	
	List<ProgressBar> performanceBars = new ArrayList<>();

	/**
	 * Open the window.
	 * @param renderer 
	 */
	public void open(FractalRendererSWT renderer) {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		
		setMainRenderer(renderer);
		renderer.init();
		FractalsMain.taskManager = new TaskManager(renderer.getDataDescriptor(), renderer.getDataContainer());
		FractalsMain.threadManager = new ThreadManager();
		FractalsMain.threadManager.setThreadCount(FractalsMain.HELPER_THREAD_COUNT);
		FractalsMain.threadManager.addTaskProvider(FractalsMain.taskProvider);
		FractalsMain.taskProvider.setDataDescriptor(renderer.getDataDescriptor());
		FractalsMain.taskManager.generateTasks();
		FractalsMain.performanceMonitor.startPhase();
		mainRenderer.startIterationPositionThread();
		
		while (!shell.isDisposed()) {
			
			tick();
			
			if (mainRenderer != null && mainRenderer.isRedraw()) {
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

		//TODO remove
//		testProgressBar(progressBar);
//		testProgressBar(progressBar_1);
		
//		setText(lbl_disp_dim, mainRenderer.disp_img.getBounds().width+"x"+mainRenderer.disp_img.getBounds().height);
//		setText(lbl_draw_dim, mainRenderer.draw_img.getBounds().width+"x"+mainRenderer.draw_img.getBounds().height);
		lblStatus.setText(FractalsMain.taskManager.isFinished() ? "fertig" : ""+FractalsMain.taskManager.getFinishedDepth());
	}

//	private void testProgressBar(ProgressBar pb) {
//		int s = pb.getSelection()+ ((Math.random() > 0.5) ? 1 : -1);
//		if (s < pb.getMinimum() || s > pb.getMaximum())
//			return;
//		pb.setSelection(s);
//	}

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
	private Text text_commandline;
	private Text text_filter;

	private Composite composite_performance_bars;

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		setupShell();
		
		Composite composite_6 = new Composite(shell, SWT.NO_REDRAW_RESIZE);
		composite_6.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(composite_6, SWT.NONE);
		
		setupMainRenderer(sashForm);
		
		TabFolder tabFolder = new TabFolder(sashForm, SWT.NONE);
		
		setupPropertyTab(tabFolder);
		setupPerformanceTab(tabFolder);
		setupLogTab(tabFolder);
		
		setupContextMenu(tabFolder);
		
		sashForm.setWeights(new int[] {2, 1});

	}

	private void setupShell() {
		shell = new Shell();
		if (display.getMonitors().length > 1) {
			Rectangle monitorBounds = display.getMonitors()[1].getBounds();
			Rectangle shellBounds = shell.getBounds();
			shell.setLocation(monitorBounds.x+monitorBounds.width-shellBounds.width, monitorBounds.y+monitorBounds.height-shellBounds.height);
		}
		shell.setMaximized(true);
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
			}
		});
//		shell.setMaximized(true);
		shell.setText("SWT Application");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
	}

	private void setupMainRenderer(SashForm sashForm) {
		Composite composite_5 = new Composite(sashForm, SWT.NONE);
		composite_5.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		canvas = new Canvas(composite_5, SWT.DOUBLE_BUFFERED);
		canvas.addKeyListener(new KeyListenerControls(this));
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (mainRenderer == null)
					return;
				double factor = e.button == 1 ? 0.5 : (e.button == 3 ? 2 : 1);
				if (factor != 0) {
					mainRenderer.updateLocation(e.x, e.y, factor);
					canvas.redraw();
				}
			}
		});
		canvas.addPaintListener(e -> {
			if (mainRenderer != null)
				mainRenderer.render(e, save);
			}
		);
		canvas.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("resize canvas");
				if (mainRenderer != null)
					mainRenderer.resized();
			}
		});
		canvas.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				
				if (FractalsMain.mainWindow.mainRenderer == null)
					return;
				
				//Mouse moved on canvas of MainRenderer
				DataDescriptor dd = FractalsMain.taskManager.getDataContainer().getDescriptor();
				FractalsMain.mainStateHolder.getState("cursor position", Position.class).setValue(new Position(e.x, e.y));
				
				//timing of visualization refreshs
				//TODO implement "buffered completion scheduling" (cooldown,...)
				long t = System.nanoTime();
				IterationPositionThread ips = FractalsMain.threadManager.getIterationWorkerThread();
//				if (ips.getIterations() == ips.getMaxIterations())
//					finishedDrawingTimer++;
//				if (finishedDrawingTimer == 1){
////				if (mainRenderer.allowRedraw){
//					visRefreshTime = t;
//					visRedraw = true;
//				}
//				visMouseMoveCouter++;
				
				visRedraw = true;
				//visualization refresh
				if (visRedraw){
//					visRedraw = false;
					finishedDrawingTimer = 0;
					State<Position> stateCursorImagePosition = FractalsMain.mainStateHolder.getState("cursor image position", Position.class);
					stateCursorImagePosition.setValue(new Position(dd.getStart_x()+(e.x/(double)dd.getDim_goal_x())*dd.getDelta_x(), dd.getStart_y()+(e.y/(double)dd.getDim_goal_y())*dd.getDelta_y()));
					ips.setParameters(stateCursorImagePosition.getValue(), dd, FractalsMain.mainStateHolder.getState("visulization steps", Integer.class).getValue());
				}
			}
		});
	}

	private void setupPropertyTab(TabFolder tabFolder) {
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
//				save = true;
//				System.out.println("save -> true");
//				canvas.redraw();
				new SaveImageDialog(shell, SWT.NONE);
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
		
		setupStateTable(composite_7);
	}

	private void setupStateTable(Composite composite_7) {
		//TODO transfer hardcoded states to stateholder
		ArrayList<State<?>> applicableStates = new ArrayList<>();
		applicableStates.addAll(FractalsMain.mainStateHolder.getStates());
		
		for (State<?> state : applicableStates) {
			
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
	}
	
	public void resetPerformanceBars() {
		//TODO dispose StateChangeListeners?! extend ProgressBar?
		for (ProgressBar pb : performanceBars) {
			pb.dispose();
		}
		performanceBars.clear();
		
		ThreadManager threadManager = FractalsMain.threadManager;
		for (WorkerThread thread : threadManager.getThreads()) {
			
			ProgressBar bar = new ProgressBar(composite_performance_bars, SWT.SMOOTH);
			performanceBars.add(bar);
			State<Integer> state = thread.getStateHolder().getStateIterationsPerSecond();
			StateChangeListener<Integer> stateChangeListener = new StateChangeListener<Integer>(state).addStateChangeAction(new StateChangeAction() {
				@Override
				public void update() {
					int val = (Integer)getState().getValue();
					for (ProgressBar pb : performanceBars) {
						if (pb.getMaximum() < val) {
							pb.setMaximum(val);
						}
					}
					bar.setSelection(val);
				}
			});
			state.addStateListener(stateChangeListener);
			addStateChangeListener(stateChangeListener);
		}
	}

	private void setupPerformanceTab(TabFolder tabFolder) {
		TabItem tbtmPerformance = new TabItem(tabFolder, SWT.NONE);
		tbtmPerformance.setText("Performance");
		
		ScrolledComposite scrolledComposite_3 = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tbtmPerformance.setControl(scrolledComposite_3);
		scrolledComposite_3.setExpandHorizontal(true);
		scrolledComposite_3.setExpandVertical(true);
		
		composite_performance_bars = new Composite(scrolledComposite_3, SWT.NONE);
		composite_performance_bars.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(composite_performance_bars, SWT.NONE);
		lblNewLabel.setText("New Label");
		
		Label lblNewLabel_1 = new Label(composite_performance_bars, SWT.NONE);
		lblNewLabel_1.setText("New Label");
		
		scrolledComposite_3.setContent(composite_performance_bars);
		scrolledComposite_3.setMinSize(composite_performance_bars.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void setupLogTab(TabFolder tabFolder) {
		TabItem tbtmLog = new TabItem(tabFolder, SWT.NONE);
		tbtmLog.setText("Log");
		
		ScrolledComposite scrolledComposite_2 = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		tbtmLog.setControl(scrolledComposite_2);
		scrolledComposite_2.setExpandHorizontal(true);
		scrolledComposite_2.setExpandVertical(true);
		
		Composite composite_10 = new Composite(scrolledComposite_2, SWT.NONE);
		GridLayout gl_composite_10 = new GridLayout(1, false);
		gl_composite_10.verticalSpacing = 3;
		gl_composite_10.marginHeight = 1;
		gl_composite_10.marginWidth = 1;
		composite_10.setLayout(gl_composite_10);
		
		text_filter = new Text(composite_10, SWT.BORDER);
		final StyledText styledText_log = new StyledText(composite_10, SWT.BORDER);
		text_filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_filter.setMessage("Filter");
		text_filter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				updateLog(styledText_log, text_filter.getText());
			}
		});
		
		styledText_log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		styledText_log.setSize(65, 15);
		styledText_log.setEditable(false);
		styledText_log.setFont(new org.eclipse.swt.graphics.Font(display, "Lucida Sans Typewriter", 10, SWT.NONE));
		
		text_commandline = new Text(composite_10, SWT.BORDER);
		text_commandline.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				inputLogger.log("Entered: "+text_commandline.getText());
				text_commandline.setText("");
			}
		});
		text_commandline.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		text_commandline.setSize(76, 21);
		text_commandline.setMessage("Enter commands here");
		
		scrolledComposite_2.setContent(composite_10);
		scrolledComposite_2.setMinSize(composite_10.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		StateChangeListener<Integer> scl = new StateChangeListener<Integer>(Logger.state);
		scl.addStateChangeAction(new StateChangeAction() {
			@Override
			public void update() {
				updateLog(styledText_log, text_filter.getText());
			}
		});
		Logger.state.addStateListener(scl);
		stateChangeListeners.add(scl);
		
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);
	}

	private void setupContextMenu(TabFolder tabFolder) {
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
		renderer.init();
		CategoryLogger.INFO.log("mainwindow", "set main renderer");
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

	private void updateLog(final StyledText styledText_log, String filter) {
		display.syncExec(new Runnable(){
			@Override
			public void run() {
				StringBuilder text = new StringBuilder();
				ArrayList<StyleRange> ranges = new ArrayList<>();
				for (Message msg : new ArrayList<>(Logger.getLog())){
					if (filter != null && filter.length() > 0) {
						if (!msg.getCategoryPrefix().contains(filter))
							continue;
					}
					
					StyleRange sr = new StyleRange();
					sr.start = text.length();
					
					sr.length = msg.getCategory().getName().length()+3;
					if (msg.getPrefix() != null)
						sr.length += msg.getPrefix().length();
					
					text.append(msg.getLogString()).append("\r\n");
					
					sr.foreground = msg.getCategory().getColor();
					ranges.add(sr);
				}
				styledText_log.setText(text.toString());
				for (int i = 0  ; i < ranges.size() ; i++){
					styledText_log.setStyleRange(ranges.get(i));
				}
				styledText_log.setTopIndex(styledText_log.getLineCount()-1);
			}
		});
	}
}
