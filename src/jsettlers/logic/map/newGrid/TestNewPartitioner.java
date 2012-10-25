package jsettlers.logic.map.newGrid;

import java.util.LinkedList;
import java.util.List;

import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.logging.MilliStopWatch;
import jsettlers.common.logging.StopWatch;
import jsettlers.common.map.shapes.MapCircle;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.resources.ResourceManager;
import jsettlers.common.utils.collections.FilterIterator;
import jsettlers.common.utils.collections.IPredicate;
import jsettlers.common.utils.partitioning.PartitionCalculator;
import jsettlers.graphics.ISettlersGameDisplay;
import jsettlers.graphics.action.Action;
import jsettlers.graphics.action.EActionType;
import jsettlers.graphics.map.MapInterfaceConnector;
import jsettlers.graphics.map.UIState;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.swing.SwingResourceLoader;
import jsettlers.graphics.swing.SwingResourceProvider;
import jsettlers.main.swing.SwingManagedJSettlers;

public class TestNewPartitioner {
	static { // sets the native library path for the system dependent jogl libs
		SwingResourceLoader.setupSwingPaths();
	}

	private static final short HEIGHT = 400;
	private static final short WIDTH = 400;
	private static final byte DEFAULT_PLAYER = 3;

	private static List<TestOccupyingBuilding> buildings = new LinkedList<TestOccupyingBuilding>();

	public static void main(String args[]) {
		ImageProvider.getInstance().startPreloading();
		ResourceManager.setProvider(new SwingResourceProvider());

		final MainGrid grid = new MainGrid(WIDTH, HEIGHT);

		grid.fogOfWar.toggleEnabled();

		// fill landscape
		for (short y = 0; y < HEIGHT; y++) {
			for (short x = 0; x < WIDTH; x++) {
				grid.setLandscapeTypeAt(x, y, ELandscapeType.GRASS);
				// grid.movablePathfinderGrid.changePlayerAt(getPos(x, y), DEFAULT_PLAYER);
			}
		}
		grid.bordersThread.start();

		// start GUI
		ISettlersGameDisplay gui = SwingManagedJSettlers.getGui();
		MapInterfaceConnector connector = gui.showGameMap(grid.graphicsGrid, null);
		connector.loadUIState(new UIState(0, getPos(140, 100)));
		connector.fireAction(new Action(EActionType.TOGGLE_DEBUG));
		connector.fireAction(new Action(EActionType.ZOOM_OUT));
		connector.fireAction(new Action(EActionType.ZOOM_OUT));
		connector.fireAction(new Action(EActionType.ZOOM_OUT));
		connector.fireAction(new Action(EActionType.ZOOM_OUT));
		connector.fireAction(new Action(EActionType.ZOOM_OUT));
		connector.fireAction(new Action(EActionType.ZOOM_OUT));

		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}

				// execute tests
				executeTests(grid);
			}
		}.start();
	}

	private static void executeTests(MainGrid grid) {
		StopWatch watch = new MilliStopWatch();

		final int radius = 100;
		final int xPos = 200;
		final int yPos = 200;
		final ShortPoint2D center = getPos(xPos, yPos);

		IPredicate<ShortPoint2D> predicate = new IPredicate<ShortPoint2D>() {
			MapCircle circle1 = new MapCircle(center, radius / 4);
			MapCircle circle2 = new MapCircle(getPos(xPos + radius / 2, yPos - radius / 4), radius / 2);
			MapCircle circle3 = new MapCircle(getPos(xPos - radius, yPos), radius);
			MapCircle circle4 = new MapCircle(getPos(xPos - radius / 2, yPos), radius / 4);

			@Override
			public boolean evaluate(ShortPoint2D pos) {
				return !circle3.contains(pos) && !circle2.contains(pos) && !circle1.contains(pos) || circle4.contains(pos);
			}
		};

		FilterIterator<ShortPoint2D> filtered = new FilterIterator<ShortPoint2D>(new MapCircle(center, radius), predicate);

		// for (ShortPoint2D curr : filtered) {
		// grid.movablePathfinderGrid.changePlayerAt(curr, (byte) 1);
		// }

		double xFactor = 1.2;
		double yFactor = 1.2;

		int minX = (int) (xPos - radius * xFactor);
		int minY = (int) (yPos - radius * yFactor);

		int maxX = (int) (xPos + radius * xFactor);
		int maxY = (int) (yPos + radius * yFactor);

		PartitionCalculator partitioner = new PartitionCalculator(filtered, minX, minY, maxX, maxY);
		partitioner.calculatePartitions();

		int width = partitioner.getWidth();
		minX--;
		minY--;

		for (int y = 0; y < partitioner.getHeight(); y++) {
			for (int x = 0; x < width; x++) {
				grid.partitionsGrid.setPartitionAndPlayerAt((short) (x + minX), (short) (y + minY), (byte) (partitioner.getPartitionAt(x, y) + 1));
			}
		}

		watch.stop("the test needed");
	}

	private static ShortPoint2D getPos(int x, int y) {
		return new ShortPoint2D(x, y);
	}

}
