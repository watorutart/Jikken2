package relay;

import fragile.Fragile;
import fragile.deliRecord.ObsStats;
import lejos.utility.Delay;
import telecommunication.Receiver;
import telecommunication.code.Collector_Relay;
import telecommunication.code.Relay_Deliver;
import telecommunication.code.Relay_HQ;

/**
 * @author Takumi Suzuki
 *
 */
public class Relay {
	private ControlFragile cf = new ControlFragile();
	private ControlLockTimer clt = new ControlLockTimer();
	private ControlOrder co = new ControlOrder(this);
	private ControlTele ct = new ControlTele();
	private InfoEditor ie = new InfoEditor();
	private Lock lock = new Lock();

	/**
	 * 中継所システムを稼働させます。
	 */
	void execute() {
		System.out.println("Start relay!");
		while (true) {
			wait(Receiver.deliver);
			wait(Receiver.collector);
			scanFragileList();
			Delay.msDelay(1000);
		}
	}

	/**
	 * 指定されたサブシステムとの通信が許可されている場合にtrueを返します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @return - 通信が許可されている場合はtrue
	 */
	private Boolean isAllowedToConnect(Receiver receiver) {
		return clt.getLock(receiver);
	}

	/**
	 * 共有変数に値を代入します。
	 * 
	 * @param lock
	 *            - 共有変数
	 */
	void setLock(Boolean lock) {
		this.lock.setLock(lock);
	}

	/**
	 * 各サブシステムとの通信の許可を制御する鍵に値を代入します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param lock
	 *            - 共有変数
	 */
	void setLock(Receiver receiver, Boolean lock) {
		clt.setLock(receiver, lock);
	}

	/**
	 * 各サブシステムとの通信を禁止する時間を設定します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param time
	 *            - 時間（秒）
	 */
	void setLimit(Receiver receiver, int time) {
		clt.setLimit(receiver, time);
	}

	/**
	 * 指定されたサブシステムからのコネクションを待ちます。文字列を受信した後、命令を識別して処理を行います。
	 * 
	 * @param receiver
	 *            - サブシステム
	 */
	private void wait(Receiver receiver) {
		if (!isAllowedToConnect(receiver)) {
			return;
		}
		System.out.println("[W:" + receiver + "]");
		String receDetail = receive(receiver);
		if (!receDetail.equals("")) {
			Delay.msDelay(3 * 1000);
			co.idtOrder(receiver, receDetail);
		}
	}

	/**
	 * 指定されたサブシステムに文字列を送信します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param sendDetail
	 *            - 送信する文字列
	 * @return - 送信が成功した場合はtrue
	 */
	private Boolean send(Receiver receiver, String sendDetail) {
		if (!isAllowedToConnect(receiver))
			return false;
		// return ct.send(receiver, sendDetail);
		return ct.send(receiver, sendDetail, "debug");
	}

	/**
	 * 指定されたサブシステムから文字列を受信します。 必ずwaitメソッドから呼び出してください。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @return - 受信した文字列
	 */
	private String receive(Receiver receiver) {
		// return ct.receive(receiver);
		return ct.receive(receiver, "debug");
	}

	/**
	 * 荷物リストを走査して、本部へ報告するメソッドを呼び出します。
	 */
	private void scanFragileList() {
		cf.printList();
		sendArrive(cf.getNum());
		if (cf.hasNeedInfo())
			wait(Receiver.hq);
		sendStart(cf.getOnDeliver());
		sendReturned(cf.getReturned());
	}

	/**
	 * 本部に、中継所に荷物が到着したことを報告します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	private void sendArrive(Long num) {
		if (num == null)
			return;
		String sendDetail = ie.adjustInfo(Relay_HQ.setRelayArrive, num);
		if (send(Receiver.hq, sendDetail)) {
			cf.setNeedInfo(num);
			wait(Receiver.hq);
		}
	}

	/**
	 * 本部に、荷物の配達が開始されたことを報告します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	private void sendStart(Long num) {
		if (num == null)
			return;
		String sendDetail = ie.adjustInfo(Relay_HQ.setStartDeli, num);
		if (send(Receiver.hq, sendDetail))
			cf.setReportedPassing(num);
	}

	/**
	 * 本部に、配達が完了した荷物の障害状況を報告します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	private void sendReturned(Long num) {
		if (num == null)
			return;
		ObsStats oStats = cf.getObs(num);
		if (oStats == ObsStats.absent) {
			sendAbsent(num);
		} else if (oStats == ObsStats.wrongHouse) {
			sendWrongHouse(num);
		} else {
			sendComplete(num);
		}
	}

	/**
	 * 本部に、受取人不在を報告します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	private void sendAbsent(Long num) {
		if (num == null)
			return;
		String sendDetail = ie.adjustInfo(Relay_HQ.setAbsent, num);
		if (send(Receiver.hq, sendDetail))
			cf.reWaiting(num);
	}

	/**
	 * 本部に、宛先間違いを報告します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	private void sendWrongHouse(Long num) {
		if (num == null)
			return;
		String sendDetail = ie.adjustInfo(Relay_HQ.setWrgHouse, num);
		if (send(Receiver.hq, sendDetail))
			cf.setReported(num);
	}

	/**
	 * 本部に、配達完了を報告します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	private void sendComplete(Long num) {
		if (num == null)
			return;
		String sendDetail = ie.adjustInfo(Relay_HQ.reportDeliComp, num, cf.getDeliTime(num));
		if (send(Receiver.hq, sendDetail))
			cf.setReported(num);
	}

	/**
	 * 荷物番号を保存します。
	 * 
	 * @param numStr
	 *            - 荷物番号
	 */
	void saveFragileNum(String numStr) {
		Long num = Long.parseLong(numStr);
		cf.saveFragileNum(num);
	}

	/**
	 * 荷物を保存します。
	 * 
	 * @param numStr
	 *            - 荷物番号
	 * @param cname
	 *            - 依頼人氏名
	 * @param caddr
	 *            - 依頼人住所
	 * @param hname
	 *            - 受取人氏名
	 * @param haddr
	 *            - 受取人住所
	 */
	void saveFrglInfo(String numStr, String cname, String caddr, String hname, String haddr) {
		Long num = Long.parseLong(numStr);
		cf.saveFragileInfo(num, cname, caddr, hname, haddr);
	}

	/**
	 * 搬送担当ロボットに共有変数の値を送信します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 */
	void sendLock(Receiver receiver) {
		String sendDetail = Collector_Relay.syncLock + "|" + lock.getLock();
		if (send(receiver, sendDetail))
			if (!lock.getLock())
				lock.setLock(true);
	}

	/**
	 * 配達可能である荷物の有無を判別して処理の分岐を行います。
	 */
	void prepareFrgl() {
		if (cf.hasDeliverableFragile()) {
			sendFragile();
		} else {
			sendNoFrgl();
		}
	}

	/**
	 * 配達ロボットに、配達可能な荷物がないことを送信します。
	 */
	private void sendNoFrgl() {
		String sendDetail = ie.adjustInfo(Relay_Deliver.noFrgl);
		send(Receiver.deliver, sendDetail);
	}

	/**
	 * 配達ロボットに荷物を送信します。
	 */
	private void sendFragile() {
		Fragile tmp = cf.getDeliverableFragile();
		String sendDetail = ie.adjustInfo(Relay_Deliver.syncFrglInfo, tmp);
		if (send(Receiver.deliver, sendDetail))
			cf.setOnDeliver(tmp.getFrglNum());
	}

	/**
	 * 配達失敗を記録します。
	 * 
	 * @param numStr
	 *            - 荷物番号
	 * @param oStatsStr
	 *            - 障害状況
	 */
	void saveDeliFail(String numStr, String oStatsStr) {
		Long num = Long.parseLong(numStr);
		ObsStats oStats = ObsStats.valueOf(oStatsStr);
		cf.setFailed(num, oStats);
	}

	/**
	 * 配達完了を記録します。
	 * 
	 * @param numStr
	 *            - 荷物番号
	 * @param timeStr
	 *            - 計測時間（分）
	 */
	void saveDeliComp(String numStr, String timeStr) {
		Long num = Long.parseLong(numStr);
		Integer time = Integer.parseInt(timeStr);
		cf.setCompleted(num, time);
	}

	/**
	 * 通信相手が正しいかどうかを判別します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param msg
	 *            - 受信したサブシステム名
	 */
	void isCorrectConnection(Receiver receiver, String msg) {
		if (msg.equals("relay")) {
			sendCorrectConnection(receiver);
		} else {
			sendWrongConnection(receiver);
		}
	}

	/**
	 * コネクションが正しいことを送信します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 */
	private void sendCorrectConnection(Receiver receiver) {
		while (!send(receiver, "protocol|true"))
			;
		setLock(receiver, true);
		wait(receiver);

	}

	/**
	 * コネクションが誤っていることを送信します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 */
	private void sendWrongConnection(Receiver receiver) {
		while (!send(receiver, "protocol|false"))
			;
	}
}
