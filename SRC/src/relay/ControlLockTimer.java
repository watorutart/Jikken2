package relay;

import telecommunication.Receiver;

/**
 * @author Takumi Suzuki
 *
 */
public class ControlLockTimer {
	private LockTimer lTimerColle = new LockTimer(true, 60); // 収集ロボットと受付所の通信が終わるまで待機
	private LockTimer lTimerDeli = new LockTimer(false); // 初期状態では配達ロボットとの通信は行わない
	private LockTimer lTimerHQ = new LockTimer(true, 0);

	/**
	 * 指定されたサブシステムのlockの値を返します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @return - lockの値
	 */
	Boolean getLock(Receiver receiver) {
		if (receiver == Receiver.collector)
			return lTimerColle.getLock();
		else if (receiver == Receiver.deliver)
			return lTimerDeli.getLock();
		else if (receiver == Receiver.hq)
			return lTimerHQ.getLock();
		else
			return false;
	}

	/**
	 * 指定されたサブシステムのlockに値を代入します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param lock
	 *            - 共有変数
	 */
	void setLock(Receiver receiver, Boolean lock) {
		if (receiver == Receiver.collector)
			lTimerColle.setLock(lock);
		else if (receiver == Receiver.deliver)
			lTimerDeli.setLock(lock);
		else if (receiver == Receiver.hq)
			lTimerHQ.setLock(lock);
	}

	/**
	 * 指定されたサブシステムの時間制限を設定します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param time
	 *            - 通信を許可しない時間（秒）
	 */
	void setLimit(Receiver receiver, int time) {
		if (receiver == Receiver.collector)
			lTimerColle.setLimit(time);
		else if (receiver == Receiver.deliver)
			lTimerDeli.setLimit(time);
		else if (receiver == Receiver.hq)
			lTimerHQ.setLimit(time);
	}

}
