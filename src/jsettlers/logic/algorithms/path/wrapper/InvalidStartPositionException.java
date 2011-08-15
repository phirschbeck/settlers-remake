package jsettlers.logic.algorithms.path.wrapper;

public class InvalidStartPositionException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	public InvalidStartPositionException(int x, int y) {
		super("invalid start position (" + x + "|" + y + ")");
	}
}