package de.felixperko.fractals.client.gui;

import static de.felixperko.fractals.server.util.performance.PerfInstance.createNewSubInstanceAndBegin;
import static de.felixperko.fractals.client.controls.KeyListenerControls.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.sun.jmx.snmp.tasks.ThreadService;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.controls.Console;
import de.felixperko.fractals.client.controls.KeyListenerControls;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.client.stateholders.ClientStateHolder;
import de.felixperko.fractals.client.threads.IterationPositionThread;
import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.FractalsServerMain;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.data.Grid;
import de.felixperko.fractals.server.state.DiscreteState;
import de.felixperko.fractals.server.state.RangeState;
import de.felixperko.fractals.server.state.State;
import de.felixperko.fractals.server.state.StateChangeAction;
import de.felixperko.fractals.server.state.StateChangeListener;
import de.felixperko.fractals.server.state.StateListener;
import de.felixperko.fractals.server.state.SwitchState;
import de.felixperko.fractals.server.tasks.WorkerPhase;
import de.felixperko.fractals.server.tasks.WorkerPhaseChange;
import de.felixperko.fractals.server.threads.PerformanceThread;
import de.felixperko.fractals.server.threads.ThreadManager;
import de.felixperko.fractals.server.threads.WorkerThread;
import de.felixperko.fractals.server.util.CategoryLogger;
import de.felixperko.fractals.server.util.Logger;
import de.felixperko.fractals.server.util.Message;
import de.felixperko.fractals.server.util.Position;
import de.felixperko.fractals.server.util.performance.PerfInstance;
import de.felixperko.fractals.server.util.performance.PerformanceMonitor;
import sun.misc.Perf;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.layout.RowData;

public class MainWindow implements PerformanceThread{
	
	public static int w = 500;
	public static int h = 400;
	
	WorkerPhase phase = DEFAULT_PHASE;
	
	List<WorkerPhaseChange> phaseChanges = new ArrayList<>();
//	List<PhaseProgressionCanvas> phaseProgressionCanvases = new ArrayList<>();
	
	PerformanceMonitor monitor;

	protected CategoryLogger log_thread;

	public MainWindow() {
		log_thread = CategoryLogger.INFO.createSubLogger("threads/main");
	}
	
	@Override
	public void setPhase(WorkerPhase phase) {
		if (phase == this.phase)
			return;
		this.phase = phase;
		phaseChanges.add(new WorkerPhaseChange(phase));
		for (PhaseProgressionCanvas canvas : phaseProgressionCanvases) {
			if (canvas.thread == this){
				canvas.getDisplay().syncExec(() -> {
					canvas.setRedraw(true);
					canvas.update();
				});
			}
		}
	}
	
	@Override
	public WorkerPhase getPhase() {
		return phase;
	}
	
	@Override
	public List<WorkerPhaseChange> getPhaseChanges(){
		return phaseChanges;
	}
	
	@Override
	public void setPhaseProgressionCanvas(PhaseProgressionCanvas canvas) {
		phaseProgressionCanvases.add(canvas);
		canvas.setPhaseChanges(getPhaseChanges());
	}

	@Override
	public void removePhaseProgressionCanvas(PhaseProgressionCanvas canvas) {
		phaseProgressionCanvases.remove(canvas);
	}
	
	@Override
	public String getName() {
		return "Main";
	}
	
	
	
	CategoryLogger log_gui = new CategoryLogger("GUI/window", Color.BLUE);

	public Shell shell;
	private Display display;
	
	Label lbl_disp_dim;
	private Label lblStatus;
	private Label lbl_draw_dim;
	
	double quality = 1;
	private Label qualitylbl;

	public Canvas canvas;
	Renderer mainRenderer;
	
	public boolean save = false;
	boolean redraw = false;
	Queue<RedrawInfo> redrawInfos = new PriorityQueue<>();

	private ArrayList<StateChangeListener<?>> stateChangeListeners = new ArrayList<>();

	int visMouseMoveCouter = 0;
	long visRefreshTime = 0;
	boolean visRedraw = true;
	
	List<ProgressBar> performanceBars = new ArrayList<>();
	
	List<PhaseProgressionCanvas> phaseProgressionCanvases = new ArrayList<>();

	/**
	 * Open the window.
	 * @param renderer 
	 */
	public void open(Renderer renderer) {
		setMainRenderer(renderer);
		
		display = Display.getDefault();
		createContents();

		shell.open();
		shell.layout();
		renderer.init();
		
	}
	
	public void windowLoop() {
		
		mainRenderer.startIterationPositionThread();
		setRedraw(true);
		
		while (!shell.isDisposed()) {
			
//			long t1 = System.nanoTime();
			setPhase(PHASE_WORKING);
			
			while (display.readAndDispatch()) {}
			tick();
			
			setPhase(PHASE_IDLE);
//			long t2 = System.nanoTime();
//			System.out.println("FRAMETIME: "+NumberUtil.getRoundedDouble((t2-t1)*NumberUtil.NS_TO_S, 3));
			
			display.sleep();
		}
	}
	
	int lastVisIterations = 0;
	int lastVisJobID = -1;
	
	long lastTime = 0;
	
	private void tick() {
		PerfInstance perf = new PerfInstance("windowloop").start();
		
		PerfInstance listeners = createNewSubInstanceAndBegin("listeners", perf);
		stateChangeListeners.forEach(l -> l.updateIfChanged(true));
		listeners.end();
		
		for(PhaseProgressionCanvas phaseProgressionCanvas : phaseProgressionCanvases) {
			phaseProgressionCanvas.redraw();
		}
		
		boolean updateTime = true;
		if (!shift.equals(nullPos)) {
			double dt = (System.nanoTime() - lastTime)*NumberUtil.NS_TO_S;
			if (dt > 0.05)
				dt = 0.05;
			if (dt < 0.01)
				updateTime = false;
			else {
				PerfInstance shiftPerf = createNewSubInstanceAndBegin("shift", perf);
				FractalsMain.mainWindow.shiftScaled(new Position(shift.getX()*dt, shift.getY()*dt));
				shiftPerf.end();
			}
		}
		if (updateTime)
			lastTime = System.nanoTime();
		
		boolean completeRedraw = isRedrawReset();
		if (completeRedraw || !redrawInfos.isEmpty()) {
			PerfInstance redraw = createNewSubInstanceAndBegin("redraw", perf);
//			if (completeRedraw){
				canvas.redraw();
				redrawInfos.clear();
//			} else {
//				while (!redrawInfos.isEmpty()){
//					RedrawInfo info = redrawInfos.poll();
//					if (info == null)
//						break;
//					canvas.redraw(info.getX(), info.getY(), info.getW(), info.getH(), true);
//				}
//			}
			canvas.update();
			redraw.end();
		}
//		IterationPositionThread ips = FractalsMain.threadManager.getIterationWorkerThread();
//		int it = ips.getIterations();
//		if (it > lastVisIterations || (lastVisJobID != ips.getJobID() && it > 0)){
//			lastVisJobID = ips.getJobID();
//			lastVisIterations = it;
//		}

		//TODO remove
//		testProgressBar(progressBar);
//		testProgressBar(progressBar_1);
		
//		setText(lbl_disp_dim, mainRenderer.disp_img.getBounds().width+"x"+mainRenderer.disp_img.getBounds().height);
//		setText(lbl_draw_dim, mainRenderer.draw_img.getBounds().width+"x"+mainRenderer.draw_img.getBounds().height);
		lblStatus.setText(FractalsMain.taskManager.getStateText());
		perf.end();
//		perf.printSecondsToLog(3, true, 0.1);
	}

	public boolean isRedrawReset() {
		boolean redraw = isRedraw();
		this.redraw = false;
		return redraw;
	}

//	boolean firstRedraw = true;
//	
//	private boolean needsFirstRedraw() {
//		if (firstRedraw) {
//			firstRedraw = false;
//			return true;
//		}
//		return false;
//	}

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

//	private ProgressBar progressBar;

//	private ProgressBar progressBar_1;
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
		
		tabFolder_1 = new TabFolder(sashForm, SWT.NONE);
		
		setupPropertyTab(tabFolder_1);
		setupPerformanceTab(tabFolder_1);
		setupLogTab(tabFolder_1);
		
		setupContextMenu(tabFolder_1);
		
		sashForm.setWeights(new int[] {2, 1});

	}

	private void setupShell() {
		shell = new Shell();
		shell.setSize(w, h);
		if (display.getMonitors().length > 1) {
			Rectangle monitorBounds = display.getMonitors()[1].getBounds();
			Rectangle shellBounds = shell.getBounds();
			shell.setLocation(monitorBounds.x+monitorBounds.width-shellBounds.width, monitorBounds.y+monitorBounds.height-shellBounds.height);
		}
//		shell.setMaximized(true);
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
			if (mainRenderer != null && FractalsMain.taskManager != null)
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
				DataDescriptor dd = mainRenderer.getDataDescriptor();
//				MainStateHolder mainStateHolder = FractalsServerMain.mainStateHolder;
				ClientStateHolder clientStateHolder = FractalsMain.clientStateHolder;
				GridRenderer gridRenderer = ((GridRenderer)mainRenderer);
				Grid grid = gridRenderer.getGrid();
				
				Position screenPos = new Position(e.x, e.y);
				Position gridPos = grid.getGridPosition(screenPos);
				Position spacePos = grid.getSpacePosition(gridPos);
				
				clientStateHolder.stateCursorPosition.setValue(screenPos);
				clientStateHolder.stateCursorGridPosition.setValue(gridPos);
				clientStateHolder.stateCursorImagePosition.setValue(spacePos);
				
				//timing of visualization refreshs
				//TODO implement "buffered completion scheduling" (cooldown,...)
//				long t = System.nanoTime();
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
					State<Position> stateCursorImagePosition = FractalsMain.clientStateHolder.stateCursorImagePosition;
					//TODO warning if dd is null?
					if (dd != null) {
//						stateCursorImagePosition.setValue(new Position(dd.getStart_x()+(e.x/(double)dd.getDim_goal_x())*dd.getDelta_x(), dd.getStart_y()+(e.y/(double)dd.getDim_goal_y())*dd.getDelta_y()));
						ips.setParameters(stateCursorImagePosition.getValue(), dd, FractalsMain.clientStateHolder.stateVisualizationSteps.getValue());
					}
				}
			}
		});
	}

	private void setupPropertyTab(TabFolder tabFolder) {
		TabItem tbtmStatus = new TabItem(tabFolder, SWT.NONE);
		tbtmStatus.setText("Eigenschaften");
		
		Composite composite = new Composite(tabFolder_1, SWT.NONE);
		tbtmStatus.setControl(composite);
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		RowData rd_composite_2 = new RowData(SWT.DEFAULT, SWT.DEFAULT);
		rd_composite_2.exclude = true;
		composite_2.setLayoutData(rd_composite_2);
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
		
		setupStateTable(composite_7);
	}

	private void setupStateTable(Composite composite_7) {
		//TODO transfer hardcoded states to stateholder
		ArrayList<State<?>> applicableStates = new ArrayList<>();
		applicableStates.addAll(FractalsServerMain.mainStateHolder.getStates());
		applicableStates.addAll(FractalsMain.clientStateHolder.getStates());
		applicableStates.addAll(FractalsMain.mainWindow.mainRenderer.getRendererStateHolder().getStates());
		applicableStates.addAll(FractalsMain.rendererStateHolder.getStates());
		
		for (State<?> state : applicableStates) {
			
			if (!state.isVisible())
				continue;
			
			Label stateNameLabel = new Label(composite_7, SWT.NONE);
			stateNameLabel.setText(state.getName()+": ");
			
			Label stateValueLabel = new Label(composite_7, SWT.NONE);

			if (state.isShowValueLabel())
				stateValueLabel.setText(state.getValueString());
			
			StateChangeListener<?> changeListener = new StateChangeListener<>(state);
			state.addStateListener(addStateChangeListener(changeListener));
			changeListener.addStateChangeAction(new StateChangeAction() {
				@Override
				public void update() {
					canvas.setFocus();
					if (!state.isShowValueLabel())
						return;
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
						canvas.setFocus();
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
						canvas.setFocus();
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
							canvas.setFocus();
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
							canvas.setFocus();
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
			if (state instanceof SelectionState) {
				@SuppressWarnings("rawtypes")
				SelectionState sstate = (SelectionState<?>)state;
				Combo combo = new Combo(stateComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
				combo.setItems(sstate.getOptionNames());
				combo.select(sstate.getCurrentSelectionIndex());
				combo.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setStateValue(e);
						canvas.setFocus();
					}
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						setStateValue(e);
						canvas.setFocus();
					}
					
					@SuppressWarnings("unchecked")
					private void setStateValue(SelectionEvent e) {
						sstate.setValue(sstate.getOption(combo.getSelectionIndex()));
					}
				});
			}
		}
	}
	
	public void resetPerformanceBars() {
//		//TODO dispose StateChangeListeners?! extend ProgressBar?
//		for (ProgressBar pb : performanceBars) {
//			pb.dispose();
//		}
//		performanceBars.clear();
//		
//		ThreadManager threadManager = FractalsMain.threadManager;
//		for (WorkerThread thread : threadManager.getThreads()) {
//			
//			ProgressBar bar = new ProgressBar(composite_performance_bars, SWT.SMOOTH);
//			performanceBars.add(bar);
//			State<Integer> state = thread.getStateHolder().getStateIterationsPerSecond();
//			StateChangeListener<Integer> stateChangeListener = new StateChangeListener<Integer>(state).addStateChangeAction(new StateChangeAction() {
//				@Override
//				public void update() {
//					int val = (Integer)getState().getValue();
//					for (ProgressBar pb : performanceBars) {
//						if (pb.getMaximum() < val) {
//							pb.setMaximum(val);
//						}
//					}
//					bar.setSelection(val);
//				}
//			});
//			state.addStateListener(stateChangeListener);
//			addStateChangeListener(stateChangeListener);
//		}
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
		
//		Label lblNewLabel = new Label(composite_performance_bars, SWT.NONE);
//		lblNewLabel.setText("CalcPixelThread");
		
		refreshProgressionThreads();
		
//		Label lblNewLabel_1 = new Label(composite_performance_bars, SWT.NONE);
//		lblNewLabel_1.setText("New Label");
//		PhaseProgressionCanvas canvas = new PhaseProgressionCanvas(composite_performance_bars, SWT.DOUBLE_BUFFERED);
//		FractalsMain.threadManager.getCalcPixelThread().setPhaseProgressionCanvas(canvas);
//		setPhaseProgressionCanvas(canvas);
//		canvas.redraw();
//		phaseProgressionCanvases.add(canvas);
		
		scrolledComposite_3.setContent(composite_performance_bars);
		scrolledComposite_3.setMinSize(composite_performance_bars.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private List<PerformanceThread> getProgressionThreads(){
		List<PerformanceThread> threads = new ArrayList<>();
		
		threads.add(this);
		
		if (FractalsMain.taskManager != null && FractalsMain.taskManager instanceof PerformanceThread)
			threads.add(((PerformanceThread)FractalsMain.taskManager));
		
		threads.add(FractalsMain.threadManager.getCalcPixelThread(mainRenderer));
		
		if (FractalsMain.threadManager.getServerConnectThread() != null)
			threads.add(FractalsMain.threadManager.getServerConnectThread());
		
		if (FractalsMain.threadManager.getClientThread() != null)
			threads.add(FractalsMain.threadManager.getClientThread());
		
		for (PerformanceThread serverThread : FractalsMain.threadManager.getServerThreads())
			threads.add(serverThread);
		
		for (WorkerThread wt : FractalsMain.threadManager.getThreads())
			threads.add(wt);
		
		return threads;
	}
	
	List<Widget> progressionThreadWidgets = new ArrayList<>();
	private TabFolder tabFolder_1;
	
	public void refreshProgressionThreads(){
		if (display == null){
			return;
		}
		display.asyncExec(() -> {
			for (Widget w : progressionThreadWidgets){
				if (w instanceof PhaseProgressionCanvas){
					PhaseProgressionCanvas c = ((PhaseProgressionCanvas)w);
					if (c.thread != this)
						phaseProgressionCanvases.remove(c);
				}
				w.dispose();
			}
			
			for (PerformanceThread thread : getProgressionThreads()){
				
				Label lblNewLabel = new Label(composite_performance_bars, SWT.NONE);
				lblNewLabel.setText(thread.getName());
				progressionThreadWidgets.add(lblNewLabel);
				
				PhaseProgressionCanvas canvas = new PhaseProgressionCanvas(composite_performance_bars, SWT.DOUBLE_BUFFERED, thread);
//				thread.setPhaseProgressionCanvas(canvas);
				canvas.redraw();
				progressionThreadWidgets.add(canvas);
				if (thread != this)
					phaseProgressionCanvases.add(canvas);
			}
		});
	}

	private void setupLogTab(TabFolder tabFolder) {
		TabItem tbtmLog = new TabItem(tabFolder, SWT.NONE);
		tbtmLog.setText("Log");
		
		ScrolledComposite scrolledComposite_2 = new ScrolledComposite(tabFolder, SWT.NONE);
		scrolledComposite_2.setExpandHorizontal(true);
		scrolledComposite_2.setExpandVertical(true);
		tbtmLog.setControl(scrolledComposite_2);
		
		Composite composite_10 = new Composite(scrolledComposite_2, SWT.NONE);
		GridLayout gl_composite_10 = new GridLayout(1, false);
		gl_composite_10.verticalSpacing = 3;
		gl_composite_10.marginHeight = 1;
		gl_composite_10.marginWidth = 1;
		composite_10.setLayout(gl_composite_10);
		
		text_filter = new Text(composite_10, SWT.BORDER);
		final StyledText styledText_log = new StyledText(composite_10, SWT.BORDER);
		styledText_log.setAlwaysShowScrollBars(false);
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
				Console.enteredCommand(text_commandline.getText());
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

	public void setMainRenderer(Renderer renderer) {
		mainRenderer = renderer;
		CategoryLogger.INFO.log("mainwindow", "set main renderer");
	}

	public Renderer getMainRenderer() {
		return mainRenderer;
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
	
	public void addRedrawInfo(RedrawInfo info){
		redrawInfos.add(info);
	}

	private void updateLog(final StyledText styledText_log, String filter) {
		display.syncExec(new Runnable(){
			@Override
			public void run() {
				if (Logger.addNewMessages() == 0)
					return;
				StringBuilder text = new StringBuilder();
				ArrayList<StyleRange> ranges = new ArrayList<>();
				int j = -1;
				List<Message> list = Logger.getLog();
				for (Message msg : list){
					j++;
					if (msg == null)
						continue;
					if (filter != null && filter.length() > 0) {
						if (!msg.getCategoryPrefix().contains(filter))
							continue;
					}

					//TODO debugging message crash wip
					try {
						StyleRange sr = new StyleRange();
						sr.start = text.length();
						
						sr.length = msg.getCategory().getName().length()+3;
						if (msg.getPrefix() != null)
							sr.length += msg.getPrefix().length();
						
						text.append(msg.getLogString()).append("\r\n");
						
						sr.foreground = msg.getCategory().getColor();
						ranges.add(sr);
					} catch (Exception e){
						e.printStackTrace();
						System.out.println("crash message: ["+j+"/"+list.size()+"] "+msg);
						System.out.println(list.toString());
					}
				}
				styledText_log.setText(text.toString());
				for (int i = 0  ; i < ranges.size() ; i++){
					styledText_log.setStyleRange(ranges.get(i));
				}
				styledText_log.setTopIndex(styledText_log.getLineCount()-1);
			}
		});
	}

	public void shift(Position shift) {
		mainRenderer.shiftView(shift);
	}
	
	private void shiftScaled(Position shift) {
		mainRenderer.shiftView(shift.mult(((GridRenderer)mainRenderer).getGrid().getScale()));
	}
}