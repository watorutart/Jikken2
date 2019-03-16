package relay;

/**
 * @author Takumi Suzuki
 *
 */
public class LockTimer {
	private Lock lock = new Lock();
	private long start = 0;
	private int limit = 0;

	/**
	 * lockの値をを指定するコンストラクタです。
	 * 
	 * @param lock
	 *            - 鍵の値
	 */
	public LockTimer(Boolean lock) {
		setLock(lock);
	}

	/**
	 * lockの値とタイマーの時間を指定するコンストラクタです。
	 * 
	 * @param lock
	 *            - 鍵の値
	 * @param time
	 *            - タイマーの時間（秒）
	 */
	public LockTimer(Boolean lock, int time) {
		setLock(lock);
		setLimit(time);
	}

	/**
	 * lockに値を代入します。
	 * 
	 * @param lock
	 *            - 鍵の値
	 */
	public void setLock(Boolean lock) {
		this.lock.setLock(lock);
	}

	/**
	 * lockの値がtrueかつ、時間制限が解かれている場合にtrueを返します。
	 * 
	 * @return - 鍵が解かれている場合はtrue
	 */
	public Boolean getLock() {
		return lock.getLock() && isNoTimeLimit();
	}

	/**
	 * 通信を許可しない時間（単位：秒）を設定します。
	 * 
	 * @param time
	 *            - タイマーにセットする時間（秒）
	 */
	public void setLimit(int time) {
		start = System.currentTimeMillis();
		limit = time;
	}

	/**
	 * タイマーにセットした時間が経過して、時間制限が解かれている場合にtrueを返します。
	 * 
	 * @return - 時間制限が解かれている場合にtrue
	 */
	private Boolean isNoTimeLimit() {
		int dif = limit - getTimeSec();
		if (dif > 0) {
			System.out.println("time limits " + dif + "s"); // debug
			return false;
		} else
			return true;
	}

	/**
	 * タイマーの残り時間を返します。
	 * 
	 * @return - タイマーの残り時間（秒）
	 */
	private int getTimeSec() {
		long end = System.currentTimeMillis();
		long dif = end - start;
		int res = (int) (dif / 1000);
		return res;
	}

}
