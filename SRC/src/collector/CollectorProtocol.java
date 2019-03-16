package collector;

//パッケージとインポートは適宜行う
import java.io.IOException;

import lejos.utility.Delay;
import telecommunication.Receiver;
//import Deliver.Adjustment;
import telecommunication.Telecommunication;

class CollectorProtocol {
	// Telecommunication telecommunication = new Telecommunication();

	Telecommunication telecommunication;

	CollectorProtocol(Telecommunication telecom) {
		telecommunication = telecom;
	}

	//////////////////////////////////////////////////////////////////////////////////
	//                               送信側  send protocol                            //
	//////////////////////////////////////////////////////////////////////////////////

	//プロトコル通信　partner:希望する通信相手
	//protocol communication, partner:partner user wishes
	public String makeProtocol(Receiver partner) {
		String syncDetail = "";
		boolean flag = false;

		if (partner == Receiver.reception)
			syncDetail = "protocol|reception"/*adjust(Adjustment.relayProtocol)*/;
		else if (partner == Receiver.relay)
			syncDetail = "protocol|relay"/*adjust(Adjustment.houseProtocol)*/;

		//Delay.msDelay(10);

		//サブシステム名を要求する
		//demand opponent after established connection
		do {
			try {
				flag = this.telecommunication.sendSignal(syncDetail, partner, Receiver.collector, 0);
			} catch (IOException ioe) {
				continue;
			}
		} while (flag == false);



		System.out.println("protocol send done");
		Delay.msDelay(1000);

		//通信相手が正しいかを知る
		//judge who communicate with
		do {
			try {
				System.out.println("partner: " + partner.toString());
				syncDetail = this.telecommunication.receiveSignal(partner, Receiver.collector, 0);
			} catch (IOException ioe) {
				continue;
			}
		} while (syncDetail.equals(""));

		System.out.println(syncDetail);
		System.out.println("protocol receive done");

		return syncDetail;

		/*
		//ここで、trueなら次の処理に進む。falseならプロトコル通信を再度繰り返す。
		if(partner==Receiver.relay) this.exeRelayOrder(syncDetail);
		else if(partner==Receiver.house) this.exeHouseOrder(syncDetail);
		else{
			System.err.println("Illegal input on isProtocol(): Input relay or house.");
		}
		*/
	}

	////////////////////////////////////////////////////////////////////////////////////
	//                               受信側  receive protocol                           //
	////////////////////////////////////////////////////////////////////////////////////

	//protocol communication
	//boolean か void かは　書き方によるかもしれません。
	public boolean makeProtocol() {
		String syncDetail = "";
		boolean flag = false;

		//サブシステム名送信要求を受信
		//"protocol|relay"を配達ロボットから受信
		do {
			try {
				syncDetail = this.telecommunication.receiveSignal(Receiver.deliver, Receiver.relay, 20);
			} catch (IOException ioe) {
				continue;
			}
		} while (syncDetail.equals(""));

		//ここで命令番号分岐→命令分岐メソッド内に加工保存のプログラムを書く
		//例.  this.executeOrder();

		//ここで、サブシステム名が合っていれば、"protocol|true"　間違っていれば、"protocol|false"を String adjust()を使って書く
		//例.  syncDetail=this.adjust(Adjustment.issueProtocol);

		//サブシステム名を送り返す
		//send subsystem name established with opponent
		do {
			try {
				flag = this.telecommunication.sendSignal(syncDetail, Receiver.deliver, Receiver.house, 3);
			} catch (IOException ioe) {
				continue;
			}
		} while (flag == false);

		return flag;
	}

}
