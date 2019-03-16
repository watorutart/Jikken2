package collector;

import lineTrace.LineTrace_Col;
import telecommunication.Receiver;
import telecommunication.Telecommunication;
import telecommunication.code.Collector_Relay;
import telecommunication.code.Reception_Collector;

/**
 * Collectorクラスは収集ロボットのクラスです。
 * @author Kohei Akiyama
 * @version 1.0
 */

public class Collector {
	public static final int MAX_WHILE = 10;

	private long frglNum = 0; // 荷物番号
	private boolean deliComp = false; // 中継引き渡し完了
	private boolean isLock = false; // 収集ロボットが持つ共有変数
	private boolean goToRelay = false; // 中継所への移動許可

	private LineTrace_Col line_trace = new LineTrace_Col(); // ライントレースプログラムのインスタンス
	private Telecommunication telecommunication = new Telecommunication(); // 通信プログラムのインスタンス
	private CollectorProtocol collector_protocol = new CollectorProtocol(telecommunication); // プロトコルのインスタンス

	/**
	 * 受付所に移動するメソッド
	 */
	public void moveReception() {
		// 中継所から衝突回避地点に向かう
		line_trace.ExitCollision();
		// 共有変数を開放する
		this.freeLock();
		// 中継所に向かう
		line_trace.ToRecep();
	}

	/**
	 * 荷物の引き渡しを報告する
	 */
	public void reportDeli() {
		// コネクション確立
		while (true) {
			String receive_message_reception = collector_protocol.makeProtocol(Receiver.reception);
			System.out.println(receive_message_reception);

			if (receive_message_reception.equals("protocol|true")) {
				System.out.println("protocol|true");
			} else if (receive_message_reception.equals("protocol|false")) {
				System.out.println("false");
				continue;
			} else {
				System.out.println("error");
				continue;
			}

			boolean isSend = false;

			// 荷物番号と中継所引き渡し完了の加工命令
			String send_message = this.adjust(Adjustment.frglNumDeliComp);

			try {
				// シグナルを送信する
				isSend = telecommunication.sendSignal(send_message, Receiver.reception, Receiver.collector, 10);
			} catch (Exception e) {
				System.out.println(e);
			}

			if (isSend == false) {
				continue;
			}
			break;
		}

		deliComp = false;
		frglNum = 0;

		System.out.println(deliComp);
	}

	/**
	 * 荷物を受け取る
	 */
	public void takeFragile() {

		String receive_message = "";

		// プロトコル確立
		while (true) {
			String receive_message_reception = collector_protocol.makeProtocol(Receiver.reception);

			if (receive_message_reception.equals("protocol|true")) {
				System.out.println("true");
			} else if (receive_message_reception.equals("protocol|false")) {
				System.out.println("false");
				continue;
			} else {
				System.out.println("error");
				continue;
			}

			// Delay.msDelay(10);

			System.out.println("fragile num receiving...");
			System.out.println("receive_message: " + receive_message);

			try {
				receive_message = telecommunication.receiveSignal(Receiver.reception, Receiver.collector, 10);
			} catch (Exception e) {
				System.out.println(e);
			}

//			if (receive_message.equals("")) {
//				continue;
//			}
			break;
		}

		System.out.println("ok3");
		System.out.println(receive_message);
		this.save(receive_message, Save.saveFrglNum);

		System.out.println(frglNum);

		// 中継所引き渡しを未完了にする
		deliComp = false;
	}

	/**
	 * 中継所へ移動する
	 */
	public void moveRelay() {
		// 衝突回避地点まで移動する
		line_trace.ToCollision();
		this.lock();
		line_trace.ToRelay();
	}

	/**
	 * 荷物を渡す
	 */
	public void passFragile() {
		// 荷物を渡す
		String send_message = "";

		while (true) {
			String receive_message_relay = collector_protocol.makeProtocol(Receiver.relay);

			if (receive_message_relay.equals("protocol|true")) {
				System.out.println("protocol|true");
			} else if (receive_message_relay.equals("protocol|false")) {
				System.out.println("false");
				continue;
			} else {
				System.out.println("error");
				continue;
			}

			send_message = this.adjust(Adjustment.adjustFrglNum);

			//deliComp = false;

			Boolean isSend = false;

			try {
				System.out.println(send_message);
				isSend = telecommunication.sendSignal(send_message, Receiver.relay, Receiver.collector, 0);
			} catch (Exception e) {
				System.out.println(e);
			}

			// 収集ロボットが所有している荷物を破棄

			break;
		}

		deliComp = true;
	}

	/**
	 * 衝突回避のために共有変数をロックする
	 */
	public void lock() {
		// プロトコル確立
		while (true) {
			String receive_message_relay = collector_protocol.makeProtocol(Receiver.relay);

			if (receive_message_relay.equals("protocol|true")) {
				System.out.println("true");
			} else if (receive_message_relay.equals("protocol|false")) {
				System.out.println("false");
				continue;
			} else {
				System.out.println("error");
				continue;
			}

			// 情報を加工する(共有変数の値を送信する命令)
			String send_message = this.adjust(Adjustment.sendLock);
			Boolean isSuccess = false;

			try {
				isSuccess = telecommunication.sendSignal(send_message, Receiver.relay, Receiver.collector, 0);
			} catch (Exception e) {
				System.out.println(e);
			}

			String receive_message = "";

			try {
				receive_message = telecommunication.receiveSignal(Receiver.relay, Receiver.collector, 0);
			} catch (Exception e) {
				System.out.println(e);
			}

			System.out.println(receive_message);

			this.save(receive_message, Save.saveLock);

			// 中継所移動許可が出ていない場合、もう一度
			if (goToRelay == false) {
				continue;
			}

			break;
		}
	}

	/**
	 * 衝突回避のために共有変数を開放する
	 */
	public void freeLock() {
		// プロトコル確立
		while (true) {
			String receive_message_relay = collector_protocol.makeProtocol(Receiver.relay);

			if (receive_message_relay.equals("protocol|true")) {
				System.out.println("protocol|true");
			} else if (receive_message_relay.equals("protocol|false")) {
				System.out.println("false");
				continue;
			} else {
				System.out.println("error");
				continue;
			}

			// 情報を加工する(共有変数を0にする命令)
			String send_message = this.adjust(Adjustment.setLockFalse);
			boolean isSuccess = false; // 共有変数開放が成功または失敗

			try {
				isSuccess = telecommunication.sendSignal(send_message, Receiver.relay, Receiver.collector, 0);
			} catch (Exception e) {
				System.out.println(e);
			}

			// 中継所エリア進入許可解除
			goToRelay = false;

			break;
		}
	}

	/**
	 * 情報を加工する
	 * @param order 命令
	 * @return send_message 加工された送信メッセージ
	 */
	private String adjust(Adjustment order) {
		String send_message = "";

		// 共有変数の値を送信する命令
		if (order == Adjustment.sendLock) {
			send_message = Collector_Relay.sendLock.toString();
		}

		// 共有変数を0にする命令
		if (order == Adjustment.setLockFalse) {
			send_message = Collector_Relay.setLockFalse.toString();
		}

		// 荷物番号と中継所引き渡し完了の加工命令
		if (order == Adjustment.frglNumDeliComp) {
			send_message = Reception_Collector.setDeliCompFrglNum + "|" + frglNum + "|" + deliComp;
		}

		// 荷物番号の加工命令
		if (order == Adjustment.adjustFrglNum) {
			send_message = Collector_Relay.sendFrglNum + "|" + frglNum;
		}

		return send_message;
	}

	// 情報を加工して記録する(受信内容 String, 加工内容 加工保存命令)

	/**
	 * 情報を加工して記録する
	 * @param receive_message 受信内容
	 * @param order 加工内容
	 */
	private void save(String receive_message, Save order) {
		// 共有変数の値
		if (order == Save.saveLock) {
			String[] receive_message_array = receive_message.split("\\|", 0);
			System.out.println("receive from relay: " + receive_message);
			if (receive_message_array[1].equals("true")) {
				// 中継所への移動が不可能
				goToRelay = false;
			} else {
				// 中継所への移動が可能
				goToRelay = true;
			}
		}

		// 荷物番号を加工して記録命令
		if (order == Save.saveFrglNum) {
			String[] receive_message_array = receive_message.split("\\|", 0);
			if (receive_message_array[0].equals("syncFrglNum")) {
				frglNum = Long.parseLong(receive_message_array[1]);
			} else if (receive_message_array[0].equals("setDeliCompFrglNum")) {
				deliComp = true;
			}
		}
	}

	/**
	 * 中継所から受付所へ報告をしに戻る(コントロールメソッド)
	 */
	public void reportReception() {
		this.moveReception();
		this.reportDeli();
	}

	/**
	 * 荷物を中継所に運ぶ(コントロールメソッド)
	 */
	public void carryFragile() {
		this.takeFragile();
		this.moveRelay();
		this.passFragile();
	}

	/**
	 * CollectorMainで実行するメソッド(ライントレースなし)
	 */
	public void executeMainWithoutLineTrace() {
		while (true) {
			this.takeFragile();
			this.lock();
			this.passFragile();
			this.freeLock();
			this.reportDeli();
		}
	}

	/**
	 * CollectorMainで実行するメソッド
	 */
	public void executeMain() {
		while (true) {
			this.takeFragile();
			this.moveRelay();
			this.passFragile();
			this.moveReception();
			this.reportDeli();
		}
	}
}
