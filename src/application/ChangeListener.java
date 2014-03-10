package application;

public interface ChangeListener extends Listener{

	void onStoreChange(ChangeEvent changeEvent);

	void onUndoChange(ChangeEvent changeEvent);

	void onRedoChange(ChangeEvent changeEvent);

}
