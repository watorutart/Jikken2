package reception;


import java.io.IOException;
import lejos.utility.Delay;
import telecommunication.Telecommunication;
import telecommunication.Receiver;
import telecommunication.ThreadState;
import java.util.Calendar;

/**
 * 受付所のEV3との通信プロトコルを定義したクラス
 * @author bp16052 鈴木亘
 */
class RecpProtocol {
	Telecommunication telecommunication = new Telecommunication();

	// //////////////////////////////////////////////////////////////////////////////////
	// 受信側 receive protocol //
	// //////////////////////////////////////////////////////////////////////////////////

	// protocol communication
	/**
	 * 収集ロボットが通信時に誤った通信相手に情報を受信または送信しないためのメソッド
	 * @return 通信相手の成否（通信相手が正しいならtrue, 間違っていたらfalse）
	 */
	boolean makeProtocol() {
		String syncDetail = "";
		boolean flag = false;
		ThreadState state = ThreadState.Death;
		Calendar start = Calendar.getInstance();	//プロトコルの開始時間を取得

		// Delay.msDelay(10);

		// if(state != ThreadState.Death)return false;

		while (syncDetail.equals("")) {
			state = this.telecommunication.getThreadState_onlyOnce();
			// サブシステム名送信要求を受信
			// "protocol|reception"を収集ロボットから受信
			if (state != ThreadState.Death) {
				// state = this.telecommunication.getThreadState_onlyOnce();
				if (state == ThreadState.Success) {
					System.out.println("インターバル後の受信成功");
					syncDetail = this.telecommunication.getReceiveDetail_onlyOnce();
					System.out.println("受信内容" + syncDetail);
					state = ThreadState.Death;
					break;
				} else if (state == ThreadState.Fail) {
					System.out.println("インターバル後の受信失敗");
					state = ThreadState.Death;
					continue;
				}
				//1分経ったらfalseを返す
				Calendar end = Calendar.getInstance();
				if (end.getTimeInMillis() - start.getTimeInMillis() >= 60000) {		
					System.out.println("プロトコル：タイムアウト");
					return false;
				}
				continue;
			}

			//1分経ったらfalseを返す
			Calendar end = Calendar.getInstance();
			if (end.getTimeInMillis() - start.getTimeInMillis() >= 60000) {		
				System.out.println("プロトコル：タイムアウト");
				return false;
			}
			
			//Delay.msDelay(1500);
			
			//通信部分
			try {
				syncDetail = this.telecommunication.receiveSignal(Receiver.collector, Receiver.reception, 10);
				if (syncDetail == null)
					System.out.println("receive: null");
				else
					System.out.println("receive: " + syncDetail);
			} catch (IOException ioe) {
				System.out.println(ioe.getLocalizedMessage());
				state = ThreadState.Run_receive;
				// continue;
			}
			/*
			 * catch(RuntimeException re){ continue; }
			 */
		}

		System.out.println("受信内容：" + syncDetail);

		// ここで命令番号分岐→命令分岐メソッド内に加工保存のプログラムを書く
		// 例. this.executeOrder();

		// ここで、サブシステム名が合っていれば、"protocol|true"　間違っていれば、"protocol|false"を String
		// adjust()を使って書く
		// 例. syncDetail=this.adjust(Adjustment.issueProtocol);
		if (syncDetail.equals("protocol|reception")) {
			syncDetail = "protocol|true";
		} else
			syncDetail = "protocol|false";

		//Delay.msDelay(10);

		// サブシステム名を送り返す
		// send subsystem name established with opponent
		while (flag == false) {
			if (state != ThreadState.Death) {
				state = this.telecommunication.getThreadState_onlyOnce();
				if (state == ThreadState.Success) {
					System.out.println("インターバル後の送信成功");
					state = ThreadState.Death;
					flag = true;
					break;
				} else if (state == ThreadState.Fail) {
					System.out.println("インターバル後の送信失敗");
					state = ThreadState.Death;
					continue;
				}
				continue;
			}

			//Delay.msDelay(1500);
			
			try {
				System.out.println(syncDetail);
				if (this.telecommunication.sendSignal(syncDetail,
						Receiver.collector, Receiver.reception, 10)) {
					System.out.println("送信内容" + syncDetail);
					if(syncDetail.equals("protocol|true"))
						flag = true;
					else if(syncDetail.equals("protocol|false"))
						flag = false;
					break;
				} else {
					System.out.println("送信失敗");
					continue;
				}
			} catch (IOException ioe) {
				System.out.println(ioe);
				state = ThreadState.Run_send;
				continue;
			}
			/*
			 * catch(RuntimeException re){
			 * System.out.println("Runtime Exception" + re); continue; }
			 */
		}

		// Delay.msDelay(10);

		System.out.println("プロトコル終了");
		return flag;
	}

}
