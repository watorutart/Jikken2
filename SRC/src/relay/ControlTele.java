package relay;

import java.io.IOException;
import java.util.Scanner;

import lejos.utility.Delay;
import telecommunication.Receiver;
import telecommunication.Telecommunication;

/**
 * @author Takumi Suzuki
 *
 */
public class ControlTele {
	private Telecommunication tele = new Telecommunication();

	/**
	 * 各サブシステムに文字列を送信します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param sendDetail
	 *            - 送信する文字列
	 * @return - 送信が成功した場合はtrue
	 */
	Boolean send(Receiver receiver, String sendDetail) {
		int waitTime = 8;
		if (receiver == Receiver.hq)
			waitTime = 0;
		Boolean connection = false;
		for (int i = 0; i < 3; i++) {
			try {
				connection = tele.sendSignal(sendDetail, receiver, Receiver.relay, waitTime);
			} catch (IOException | RuntimeException e) {
				e.printStackTrace();
			}
			if (connection) {
				break;
			}
			Delay.msDelay(1500); // 時間をおく
		}
		return connection;
	}

	/**
	 * 各サブシステムから文字列を受信します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @return - 受信した文字列
	 */
	String receive(Receiver receiver) {
		int waitTime = 8;
		if (receiver == Receiver.hq)
			waitTime = 0;
		String receDetail = "";
		for (int i = 0; i < 3; i++) {
			try {
				receDetail = tele.receiveSignal(receiver, Receiver.relay, waitTime);
			} catch (IOException | RuntimeException e) {
				e.printStackTrace();
			}
			if (!receDetail.equals("")) {
				break;
			}
			Delay.msDelay(1500); // 時間をおく
		}
		return receDetail;
	}

	/**
	 * コンソールデバッグ用の文字列を送信するメソッドです。 サブシステムとの通信を疑似的に再現します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param sendDetail
	 *            - 送信する文字列
	 * @param debug
	 *            - 適当な文字列
	 * @return - 送信が成功した場合はtrue
	 */
	Boolean send(Receiver receiver, String sendDetail, String debug) {
		System.out.print("[S:" + receiver + "]'" + sendDetail + "'");
		System.out.print(" push [f] or any -> ");
		Scanner scanner = new Scanner(System.in);
		String result = scanner.nextLine();
		return !result.equals("f"); // f:送信失敗
	}

	/**
	 * コンソールデバッグ用の文字列を受信するメソッドです。 サブシステムとの通信を疑似的に再現します。
	 * 
	 * @param receiver
	 *            - サブシステム
	 * @param debug
	 *            - 適当な文字列
	 * @return - 受信した文字列
	 */
	String receive(Receiver receiver, String debug) {
		System.out.print("[R:" + receiver + "] -> ");
		Scanner scanner = new Scanner(System.in);
		String receDetail = scanner.nextLine();
		return receDetail;
	}
}
