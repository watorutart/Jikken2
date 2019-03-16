package relay;

import telecommunication.Receiver;
import telecommunication.code.Collector_Relay;
import telecommunication.code.Relay_Deliver;
import telecommunication.code.Relay_HQ;

/**
 * @author Takumi Suzuki
 *
 */
public class ControlOrder {
	private Relay relay;

	public ControlOrder(Relay relay) {
		this.relay = relay;
	}

	/**
	 * 受信した文字列を'|'毎に区切って配列に格納し、各サブシステムからの命令を実行します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param receDetail
	 *            - 受信した文字列
	 */
	void idtOrder(Receiver receiver, String receDetail) {
		if (receDetail.equals(""))
			return;

		String[] msg = receDetail.split("\\|", 0);

		try {
			switch (receiver) {
			case deliver:
				Relay_Deliver deliOdr = Relay_Deliver.valueOf(msg[0]);
				exeOrder(deliOdr, msg);
				break;

			case collector:
				Collector_Relay ColleOdr = Collector_Relay.valueOf(msg[0]);
				exeOrder(ColleOdr, msg);
				break;

			case hq:
				Relay_HQ hqOder = Relay_HQ.valueOf(msg[0]);
				exeOrder(hqOder, msg);
				break;

			default:
				break;
			}
		} catch (Exception e) {
			System.err.println(e);
		}

	}

	/**
	 * 指定された命令と文字列配列により処理を行います。
	 * 
	 * @param odrNum
	 *            - 命令番号（収集ロボット・中継所間）
	 * @param msg
	 *            - 受信した文字列配列
	 */
	private void exeOrder(Collector_Relay odrNum, String[] msg) {
		switch (odrNum) {
		// "protocol|relay"
		case protocol:
			relay.isCorrectConnection(Receiver.collector, msg[1]);
			break;

		// "sendFrglNum|20190113012832"
		case sendFrglNum:
			relay.saveFragileNum(msg[1]);
			relay.setLock(Receiver.collector, false);
			break;

		case sendLock:
			relay.sendLock(Receiver.collector);
			break;

		case setLockFalse:
			relay.setLock(false);
			relay.setLimit(Receiver.collector, 300); // 収集ロボットが、再度共有変数を要求するまでの通信をしない
			relay.setLock(Receiver.deliver, true); // 収集ロボットが衝突回避を終えて、共有変数をfalseにしたら通信を許可
			break;

		default:
			break;
		}
	}

	/**
	 * 指定された命令と文字列配列により処理を行います。
	 * 
	 * @param odrNum
	 *            - 命令番号（中継所・配達ロボット間）
	 * @param msg
	 *            - 受信した文字列配列
	 */
	private void exeOrder(Relay_Deliver odrNum, String[] msg) {
		switch (odrNum) {
		// "protocol|relay"
		case protocol:
			relay.isCorrectConnection(Receiver.deliver, msg[1]);
			break;

		case sendHasFrgl:
			relay.prepareFrgl();
			break;

		case sendLock:
			relay.sendLock(Receiver.deliver);
			break;

		case setLockFalse:
			relay.setLock(false);
			relay.setLimit(Receiver.deliver, 500); // 配達ロボットが、再度共有変数を要求するまで通信をしない
			break;

		// "reportDeliFail|200012241224|absent"
		case reportDeliFail:
			relay.saveDeliFail(msg[1], msg[2]);
			break;

		// "reportDeliResult|200012241224|5"
		case reportDeliResult:
			relay.saveDeliComp(msg[1], msg[2]);
			break;

		default:
			break;
		}
	}

	/**
	 * 指定された命令と文字列配列により処理を行います。
	 * 
	 * @param odrNum
	 *            - 命令番号（中継所・本部間）
	 * @param msg
	 *            - 受信した文字列配列
	 */
	private void exeOrder(Relay_HQ odrNum, String[] m) {
		switch (odrNum) {
		// syncObs|200012241224|clientman|09064758475284632|2-2|houseman|090244867442749563|1-3
		case syncObs:
			relay.saveFrglInfo(m[1], m[2], m[4], m[5], m[7]);
			relay.setLock(Receiver.collector, true);
			break;

		default:
			break;
		}
	}
}
