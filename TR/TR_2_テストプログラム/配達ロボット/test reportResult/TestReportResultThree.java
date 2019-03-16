//通信時の例外キャッチに必要
//needs in try-catch
import java.io.IOException;

import fragile.Fragile;
//障害状況の判定に使用
//use for obstacle judgement
import fragile.deliRecord.ObsStats;
import lejos.utility.Delay;
//時間計測に必要
//needs for time measurement
import lejos.utility.Stopwatch;
import lineTrace.LineTrace_Deli;
//通信クラスのメソッドで引数として必要
//use for Telecommunication methods
import telecommunication.Receiver;
import telecommunication.Telecommunication;
//命令番号の列挙型
//Commands as enumeration
import telecommunication.code.Deliver_House;
import telecommunication.code.Relay_Deliver;

class TestReportResultThree{
	private int reciTime = 0;
	private boolean hasFragile = false;
	// private int countGray = 0;
	// 加工保存の際に共有変数の保持をする
	// hold lock in method save()
	private boolean lock = false;
	// 加工保存された宛先情報をif分岐で使用
	// use for saved correctHouse judgement
	private boolean correctHouse = false;
	// 時間計測に必要
	// needs for time measurement
	// 参考文献 Reference: https://qiita.com/amapyon/items/7853b070240a86de5efa
	Stopwatch stopwatch = null;
	// 住所から移動すべきx-y座標を特定するための変数
	// x-y axis for moving to supposed address
	private int x, y;

	private Fragile fragile = new Fragile();////////////////////////////////////////
	private LineTrace_Deli lineTrace = new LineTrace_Deli();
	private Telecommunication telecommunication = new Telecommunication();
	// プロトコルが正しいかを判定
	// judge whether protocol is correct
	private boolean isProtocol;
	
	private void reportResult() {
		// 送受信の内容を保持する変数
		// hold send/receive details
		String syncDetail;

		// 通信時に使用するフラグを保持するための変数
		// hold telecommunication flag
		boolean flag = false;

		if (this.fragile.getObsStats() != ObsStats.none) {
			syncDetail = this.adjust(Adjustment.odrFrglNumObs);

			do {
				try {
					flag = this.telecommunication.sendSignal(syncDetail, Receiver.relay, Receiver.deliver, 0);
				} catch (IOException ioe) {
					continue;
				}
			} while (flag == false);
		} else {
			syncDetail = this.adjust(Adjustment.odrFrglNumObsReciTime);

			do {
				try {
					flag = this.telecommunication.sendSignal(syncDetail, Receiver.relay, Receiver.deliver, 0);
				} catch (IOException ioe) {
					continue;
				}
			} while (flag == false);
		}

		// this.hasFragile=this.takeFragile();
	}
	
	private String adjust(Adjustment order) {
		// 加工した内容を返すための変数
		// String for returning detail
		String detail = "";

		if (order == Adjustment.relayProtocol) {
			detail += "protocol|relay";
		} else if (order == Adjustment.houseProtocol) {
			detail += "protocol|house";
		} else if (order == Adjustment.odrNumHasFragile) {
			detail += "sendHasFrgl";
		} else if (order == Adjustment.odrFrglNumObs) {
			detail += "reportDeliFail|";
			detail += "0";
			detail += "|";
			detail += String.valueOf(this.fragile.getObsStats());
		} else if (order == Adjustment.odrFrglNumObsReciTime) {
			detail += "reportDeliResult|";
			detail += String.valueOf(this.fragile.getFrglNum());
			// detail+="|none|";//cancel
			detail += String.valueOf(this.reciTime);
		} else if (order == Adjustment.odrNumLock) {
			detail += "sendLock";
		} else if (order == Adjustment.lockFalse) {
			if (this.hasFragile)
				detail += "setLockFalse";
			else
				detail += "setLockFalse";
			this.lock = false;
			// detail+=String.valueOf(this.lock); //delete here
		} else if (order == Adjustment.clientHouseNameAddr) {
			// 依頼情報の内容を保持するための変数
			// String array for holding Client Info
			String[] tmpClient, tmpHouse;////////////////////////////////////////

			tmpClient = this.fragile.getClientInfo();//////////////////////////////
			tmpHouse = this.fragile.getHouseInfo();///////////////////////////////

			// テスト時に使用
			// for test usage
			// detail="personAddrName|Ren Sato|0-0|Yuduki Suzuki|0-1";

			detail = "personAddrName|" + tmpClient[0] + "|" + tmpClient[2] + "|" + tmpHouse[0] + "|" + tmpHouse[2];
		} else if (order == Adjustment.adjustFragile) {
			detail += "syncFrglNum|";

			//String[] tmpClient, tmpHouse;////////////////////////////////////////

			//tmpClient = this.fragile.getClientInfo();//////////////////////////////
			//tmpHouse = this.fragile.getHouseInfo();///////////////////////////////

			// テスト時に使用
			// for test usage
			// detail="personAddrName|Ren Sato|0-0|Yuduki Suzuki|0-1";

			//detail += tmpClient[0] + "|" + tmpClient[2] + "|" + tmpHouse[0] + "|" + tmpHouse[2];
			detail += String.valueOf(this.fragile.getFrglNum());
		} else
			System.err.println("Warning!!  Error when adjust.");

		return detail;
	}
	
	public void control(){
		int test=0;
		
		while(true){
			this.reportResult();
			
			test++;
			System.out.println("test OK "+test+" time(s).");
		}
	}
	
	public static void main(){
		TestReportResultThree EV3=new TestReportResultThree();
		
		EV3.control();
	}
}