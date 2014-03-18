package application;

public class ApplyFilterEvent extends Event {

	private String mType;
	private float mFactor;

	public ApplyFilterEvent(String type, float f) {
		mFactor = f;
		mType = type;
	}

	public String getType() {
		return mType;
	}

	public float getFactor() {
		return mFactor;
	}

	@Override
	public void notify(Listener l) {
		((ApplyFilterListener) l).onApplyFilter(this);
	}

}
