package relay;

/**
 * @author Suzuki Takumi
 *
 */
public class Lock {
	private Boolean lock = false;

	/**
	 * lockの値を返します。
	 * 
	 * @return - 共有変数の値
	 */
	Boolean getLock() {
		return this.lock;
	}

	/**
	 * lockの値を代入します。
	 * 
	 * @param lock
	 *            - 共有変数
	 */
	void setLock(Boolean lock) {
		this.lock = lock;
	}

}
