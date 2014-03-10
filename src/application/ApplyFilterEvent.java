package application;

public class ApplyFilterEvent extends Event {

	private String mType;
	private int mFactor;

	public ApplyFilterEvent(String type, int factor) {
		mFactor = factor;
		mType = type;
	}

	public String getType() {
		return mType;
	}

	public int getFactor() {
		return mFactor;
	}

	@Override
	public void notify(Listener l) {
		((ApplyFilterListener) l).onApplyFilter(this);
	}

}
