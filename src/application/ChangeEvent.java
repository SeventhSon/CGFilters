package application;

public class ChangeEvent extends Event {

	private String mType;

	public ChangeEvent(String type) {
		mType = type;
	}

	@Override
	public void notify(Listener l) {
		ChangeListener listener = (ChangeListener) l;
		switch (mType) {
		case "Store":
			listener.onStoreChange(this);
			break;
		case "Undo":
			listener.onUndoChange(this);
			break;
		case "Redo":
			listener.onRedoChange(this);
			break;
		default:
			break;
		}
	}

}
