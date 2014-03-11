package application;

public class CustomFilterEvent extends Event {

	private int[] mFilter;
	
	public CustomFilterEvent(int[] filter) {
		mFilter = filter;
	}
	
	public int[] getFilter(){
		return mFilter;
	}
	@Override
	public void notify(Listener l) {
		((CustomFilterListener) l).onCustomFilter(this);

	}

}
