package hq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import fragile.ClientInfo;
import fragile.Fragile;
import fragile.deliRecord.DeliStats;
import fragile.deliRecord.ObsStats;
import telecommunication.Receiver;
import telecommunication.Telecommunication;
import telecommunication.ThreadState;

/**
 * 本部クラス
 * @author 秋山和哉
 * 678行
 */

public class HQ {
	HQMonitor HQmonitor = new HQMonitor();
	protected static ArrayList<Fragile> fragileFile = new ArrayList<Fragile>();//荷物型のArrayList
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	//staticをつけたらクラス名.でかかないといけない、staticつけない場合はnewしないといけない
	//staticは共通の値になる、newしなくてOK、クラス名.で使える。パッケージが一緒だったらimportしないといけない
	//importはあくまで長ったらしくかかなくて済むための
	ThreadState state = ThreadState.Death;//現在通信中の相手の状態(中継所)受信」
	ThreadState state1 = ThreadState.Death;//送信

	/**
	 * コントロールメソッド（配達情報を参照できるThreadを稼働させて、中継所、受付所に対して交互に受信を行う）
	 *
	 */
	public void executeMain() {
		//Threadのインスタンス作成
		ShowDeliRecordThread showDeliThread = new ShowDeliRecordThread();
		//◆テスト　荷物の作成、最初から荷物を持っている
		/*
		String info = "yajuusenapi|20181212333333|依頼氏名|08012345678|1-1|受取氏名|09012345678|1-2|201812311010|201812341111";
		String[] M = info.split("\\|", -1);
		int junban;
			//「命令enum|荷物番号|依氏名|sms|add|受氏名|sms|add|受付time[8]|発送time[9]」
			//荷物クラスのsetTime()に合わせるために加工
			String receptTime_String = M[8].substring(0, 4) + "|" + M[8].substring(4, 6) + "|" + M[8].substring(6, 8)
					+ "|" + M[8].substring(8, 10) + "|" + M[8].substring(10, 12);
			String sendTime_String = M[9].substring(0, 4) + "|" + M[9].substring(4, 6) + "|" + M[9].substring(6, 8)
					+ "|" + M[9].substring(8, 10) + "|" + M[9].substring(10, 12);
			ClientInfo client_joho = new ClientInfo();
			client_joho.setClientInfo(M[2], M[3], M[4]);
			client_joho.setHouseInfo(M[5], M[6], M[7]);
			long frglNum = Long.parseLong(M[1]);
			Fragile frgl = new Fragile(frglNum, client_joho, receptTime_String, sendTime_String);
			fragileFile.add(frgl);//荷物を作成したds
			junban = searchFragile(frglNum);
			String initializeCalendar = "2000|10|11|11|11";
			fragileFile.get(junban).saveTime("relayArriveTime", initializeCalendar);
			fragileFile.get(junban).saveTime("deliStartTime", initializeCalendar);
			fragileFile.get(junban).saveTime("receiveTime", initializeCalendar);
			fragileFile.get(junban).saveTime("deliFinishTime", initializeCalendar);
			fragileFile.get(junban).setObsStats(ObsStats.none);//障害なしへ更新。
			fragileFile.get(junban).setDeliStats(DeliStats.awaiting);//配達待ちへ更新
			*/

		String ReceptionMessage_String;//受付所からもらう情報[String]
		String RelayMessage_String;//中継所からもらう情報[String]

		//交互に通信
		showDeliThread.start();//配達記録を」参照できるThreadの開始
		while (true) {
			//受信してそこからdeterminOrder()呼び出すメソッドが必要
			System.out.println("受付所から受信を受け付けています（4秒）");
			ReceptionMessage_String = telecommunicationReceiveFromReception();//受付所から受信するメソッド（4秒)
			if (!ReceptionMessage_String.isEmpty()) {
				determineOrder(ReceptionMessage_String);
				ReceptionMessage_String = null;
			}
			System.out.println();
			System.out.println("中継所から受信を受け付けています（4秒）");
			RelayMessage_String = telecommunicationReceiveFromRelay();//中継所から受信するメソッド（4秒)
			if (!RelayMessage_String.isEmpty()) {
				try {
				Thread.sleep(1500);
				}catch(Exception e) {
					e.printStackTrace();
				}
				determineOrder(RelayMessage_String);
				RelayMessage_String = null;
			}
			System.out.println();
			System.out.println("＊　　　　　　　　　　　　　＊定期表示＊　　　　　　　　　　　　　＊");
			System.out.println("＊通信中も荷物の配達状況を参照できるスレッドが並行動作しています。＊");
			System.out.println("＊依頼者の場合0, システム管理者の場合は1 を入力すると参照できます｡＊");
			System.out.println("＊　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　＊");
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//(インスタンス)中継所と本部の通信で使う「（共有クラス）通信」
	private Telecommunication boundaryRelay = new Telecommunication();

	/**
	 * (受信) 中継所から本部が情報を受信するメソッド
	 * @return 文字列の受信情報
	 */
	private String telecommunicationReceiveFromRelay() {
		state = this.boundaryRelay.getThreadState_onlyOnce();
		if (state != ThreadState.Death) {
			if (state == ThreadState.Success) {
				System.out.printf("インターバル後の受信成功\n");
				String info = this.boundaryRelay.getReceiveDetail_onlyOnce();
				System.out.printf("受信内容 : %s \n", info);
				state = ThreadState.Death;
				return info;
			} else if (state == ThreadState.Fail) {
				System.out.printf("インターバル後の受信失敗\n");
				state = ThreadState.Death;
				return "";
			}
			return "";
		}
		try {
			String tmp = this.boundaryRelay.receiveSignal(Receiver.relay, Receiver.hq, 4);
			if (tmp != "") {
				System.out.printf("受信成功 : %s\n", tmp);//送信成功
				return tmp;
			} else {
				System.out.printf("受信失敗\n");//送信失敗
				return "";
			}
		} catch (IOException e) {//インターバルが必要
			//どういうときに、IOException→1分立たないとBluetoothが使えない
			//PC側のAPIの仕様で1分たたないと仕様出来ない。
			System.out.printf(e.getLocalizedMessage() + "\n");
			state = ThreadState.Run_receive;
		}
		return "";
	}

	/**
	 *(送信) 本部が中継所に情報を送信するメソッド(IOExceptionが起きる)
	 * @param SendDetail:送りたい文字列の情報
	 * @return true:送信成功, false:送信失敗
	 */
	private Boolean telecommunicationSendToRelay(String SendDetail) {
		state1 = this.boundaryRelay.getThreadState_onlyOnce();//裏でIOEが動いてて、
		//今スレッドの状態がどうなのか、Runなのかとかを返してくれる
		if (state1 != ThreadState.Death) {//Deathだとここの処理をまるまる飛ばす
			if (state1 == ThreadState.Success) {//Successは通信成功
				System.out.printf("インターバル後の送信成功\n");
				state1 = ThreadState.Death;
				return true;
				//センドだから遅れたか遅れなかったかわかればOK
			} else if (state1 == ThreadState.Fail) {
				System.out.printf("インターバル後の送信失敗\n");
				state1 = ThreadState.Death;
				return false;
			} else {
				return false;
			}
		}

		try {
			if (this.boundaryRelay.sendSignal(SendDetail, Receiver.relay, Receiver.hq, 4)) {
				System.out.printf("送信成功\n");//送信成功
				return true;
			} else {
				System.out.printf("送信失敗");//送信失敗
				return false;
			}
		} catch (IOException e) {//インターバルが必要
			System.out.printf(e.getLocalizedMessage() + "\n");
			state = ThreadState.Run_send;
		}
		return false;
	}

	//(インスタンス)受付所と本部の通信で使う「（共有クラス）通信」
	private Telecommunication boundaryRecept = new Telecommunication();

	/**
	 * (受信) 受付所から本部が情報を受信する通信メソッド
	 * @return 文字列の受信した情報
	 */
	private String telecommunicationReceiveFromReception() {
		String tmp;//もらう情報
		try {
			tmp = boundaryRecept.receiveSignal(Receiver.reception, Receiver.hq, 4);
			if (tmp != "") {//受信成功
				System.out.printf("受信成功：%s \n", tmp);
				return tmp;
			} else {//受信失敗
				System.out.printf("受信失敗 \n");
				return "";
			}
		} catch (IOException e) {//インターバル60秒
			System.out.println(e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * (送信) 本部から受付所に情報を送信するメソッド
	 * @param SendDetail:送りたい文字列の情報
	 * @return true:送信成功, false:送信失敗
	 */




	/**受信したStringから命令を識別する（情報：String）（クラス図にないので作成)
	 *
	 * @param 受信した文字列の情報
	 */
	public void determineOrder(String receive) {
		//命令を識別する、文字列を分割する。
		//2番目の引数が負なので分割回数は制限なし。

		// 1. 命令：障害状況の参照、該当ユースケース：配達記録を送信する。(中継所ー本部)
		//if (OrderMessage[0].equals("getObs")) {
		//	sendDeliRecord(receive);
		//}
	/*else if (OrderMessage[0].equals("collePassPoint")) {
			//● 中継から収集ロボットが衝突回避地点を抜けた報告を本部が受けたらそれを本部が受付所に伝える"
			//受付所にコネクションを要求して、collePassPointを送信する。
			Boolean TelecomResult_Boolean = telecommunicationSendToRelay(OrderMessage[0]);
			if (TelecomResult_Boolean) {//送信成功
				System.out.printf("受付所に情報：%s を送信しました。\n", OrderMessage[0]);
			} else {//送信失敗
				while (true) {
					try {
						System.out.println("送信失敗！配達記録を参照できます。");
						System.out.println("しない場合はまた受付所に情報の送信を行います。");
						TelecomResult_Boolean = telecommunicationSendToRelay(OrderMessage[0]);
						if (TelecomResult_Boolean) {
							System.out.println("collePassPoint 送信成功");
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} //ここまでwhile
			} //ここまでelse
			} else if (OrderMessage[0].equals("deliPassPoint")) {
			//● 中継から配達ロボットが荷物を持って衝突回避地点を抜けた報告を本部が受けたら、本部がそれを受取人宅に伝える"deliPassPoint"
			//Houseにコネクションを要求して、deliPassPointを送信する。
			Boolean TelecomResult_Boolean = telecommunicationSendToHouse((OrderMessage[0]));
			if (TelecomResult_Boolean) {//送信成功
				System.out.printf("受取人宅に情報：%s を送信しました。\n", OrderMessage[0]);
			} else {//送信失敗
				while (true) {
					try {
						System.out.println("送信失敗！配達記録を参照できます。");
						System.out.println("しない場合はまた受取人宅に情報の送信を行います。");
						TelecomResult_Boolean = telecommunicationSendToHouse((OrderMessage[0]));
						if (TelecomResult_Boolean) {
							System.out.println("deliPassPoint 送信成功");
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} //ここまでwhile
			} //ここまでelse
			}*/ //else {
			// 2. 命令：荷物の作成命令, 該当ユースケース：配達記録を更新する。（受付所ー本部）
			// 3. 命令：中継所到着時間の更新命令, 該当ユースケース：配達記録を更新する。（中継所ー本部）
			// 4. 命令：中継所引き渡し失敗へ更新命令, 該当ユースケース：配達記録を更新する。（受付所ー本部）
			// 5. 命令：配達開始時間の更新命令, 該当ユースケース：配達記録を更新する。（中継所ー本部）
			// 6. 命令：宛先間違いに更新命令, 該当ユースケース：配達記録を更新する。（中継所ー本部）
			// 7. 命令：受取人不在に更新命令, 該当ユースケース：配達記録を更新する。（中継所ー本部）
			// 8. 命令：配達完了, 該当ユースケース：配達記録を更新する。（中継所ー本部）
			setDeliRecord(receive);
		}


	/**- 配達記録を送信する(情報 : String) : void
	 * 中継所から障害状況を参照されたときに行うメソッド
	 * @param info:受信した情報の文字列
	 */
	private void sendDeliRecord(String info) {
		//info具体例：「障害状況の参照 | 201811191720」
		String[] OrderMessage = info.split("\\|", -1);
		long frglNum = Long.parseLong(OrderMessage[1]);
		//fragile.deliRecord.ObsStats enum_Obs = fragileFile.get(junban).getObsStats();//junban番目のリストの障害状況を参照している。
		Boolean TelecomResult_Boolean;
		String SendDetail_String;
		//switch (enum_Obs) {
		//case none://障害無し
			//中継所にコネクションを要求して、「依頼人、受取人情報と障害状況を送信する処理
			SendDetail_String = adjust(Adjustment.obsClientInfo, frglNum);//加工命令：依頼,受取,障害
			TelecomResult_Boolean = telecommunicationSendToRelay(SendDetail_String);//中継所と通信し、成功したか判定するやつ。
			if (TelecomResult_Boolean) {//送信成功
				System.out.printf("中継所に情報：%s を送信しました。\n", SendDetail_String);
				System.out.println();
				System.out.println("Thread.sleep(5000)`実行中");
				try {
					Thread.sleep(5000);
				}catch(InterruptedException e) {
					e.printStackTrace();
				}

			} else {//送信失敗
				System.out.println("送信失敗！再送作業に移行します。");
				System.out.println("＊再送作業中も０または１を入力すると配達情報を参照できます。");
				while (true) {
					try {
						TelecomResult_Boolean = telecommunicationSendToRelay(SendDetail_String);
						if (TelecomResult_Boolean) {
							System.out.println("中継所に" + SendDetail_String + "を送信成功");
							System.out.println();
							System.out.println("Thread.sleep(5000)`実行中");
							try {
								Thread.sleep(5000);
							}catch(InterruptedException e) {
								e.printStackTrace();
							}
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} //ここまでwhile

			} //ここまでelse
			//break;
		/*default://受取人不在
			//中継所にコネクションを要求して障害状況（「受取人不在」）のみを送信する処理
			//中継所に送信する情報を加工する。
			SendDetail_String = adjust(Adjustment.sendObs, frglNum);//加工命令：障害状況（受取人不在）の送信
			//中継所にコネクションを要求して、障害状況:受取人不在を送信する。
			TelecomResult_Boolean = telecommunicationSendToRelay(SendDetail_String);
			if (TelecomResult_Boolean) {//送信成功
				System.out.printf("中継所に情報：%s を送信しました。\n", SendDetail_String);
				System.out.println();
			} else {//送信失敗
				System.out.println("送信失敗！再送作業に移行します。");
				System.out.println("＊再送作業中も０または１を入力すると配達情報を参照できます。");
				while (true) {
					try {
						TelecomResult_Boolean = telecommunicationSendToRelay(SendDetail_String);
						if (TelecomResult_Boolean) {
							System.out.println("中継所に" + SendDetail_String + "を送信成功");
							System.out.println();
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} //ここまでwhile
			}
			return;
		}*/
	}

	/**- 配達記録を更新する(情報 : String) : void
	 * 情報を加工して記録するsaveメソッドを呼び出す
	 * @param info:受信した文字列の情報
	 */
	private void setDeliRecord(String info) {
		//文字列を分割する。2番目の引数が負なので分割回数は制限なし。
		String[] OrderMessage = info.split("\\|", -1);
		//先頭のEnumで命令を識別
		//まず受付所の処理をする。
		//判定に使うEnum変数
		//makeFragileならこのif文を実行
		switch (OrderMessage[0]) {
		case "makeFragile":
			save(info, Save.deliReciTimeClientInfo);
			break;
		case "setFailedPassing":
			save(info, Save.setFailedPassing);
			break;
		case "setRelayArrive"://中継所到着時間の更新命令
			save(info, Save.relayTime);
			break;
		case "setStartDeli"://配達開始時間の更新命令
			save(info, Save.startDeli);
			break;
		case "setWrgHouse":
			save(info, Save.setWrgHouse);
			break;
		case "setAbsent"://受取人不在に更新命令
			save(info, Save.setAbsent);
			break;
		case "reportDeliComp"://配達完了
			save(info, Save.deliCompReciTime);
			break;
		default:
			break;
		}
	}


	/**
	 * 情報を加工する(命令 : 加工命令) 中継所に情報を送信するときに加工が必要でその時に使う
	 * @param order_enum:Adjustment(Enum)に入っている加工命令
	 * @param frglNum:荷物番号
	 * @return 加工された文字列
	 */
	private String adjust(Adjustment order_enum, long frglNum) {
		int junban = searchFragile(frglNum);
		String AdjustReturnData;
		switch (order_enum) {
		case obsClientInfo://中継所への依頼,受取,障害状況の送信するための加工命令。
			//依頼人情報を取得する
			String frglNum_Str = String.valueOf(frglNum);
			String ClientInfo[] = fragileFile.get(junban).getClientInfo();
			//受取人情報を取得する
			String HouseInfo[] = fragileFile.get(junban).getHouseInfo();
			AdjustReturnData = "syncObs"+ "|" + frglNum_Str + "|" + ClientInfo[0] + "|" + ClientInfo[1] + "|" + ClientInfo[2] + "|" + HouseInfo[0] + "|"
					+ HouseInfo[1] + "|" + HouseInfo[2];
			return AdjustReturnData;

		case sendObs://中継所への障害状況の送信するための加工。
			AdjustReturnData = "absent";
			return AdjustReturnData;
		}
		return null;
	}

	/**
	 * - 情報を加工して記録する(受信内容 : String, 加工内容 : 加工保存命令) : void
	 * @param info:受信した文字列
	 * @param saveOrder_enum:加工保存命令のEnum
	 */
	private void save(String info, Save saveOrder_enum) {
		//MはMessage,受け取ったStringを配列に分割した値を持つフィールド
		String[] M = info.split("\\|", -1);
		int junban;
		String now;
		String receiveTime;
		switch (saveOrder_enum) {
		case deliReciTimeClientInfo://(受付所)発送時間と受付時間と依頼情報を加工して記録命令.add();
			//「命令enum|荷物番号|依氏名|sms|add|受氏名|sms|add|受付time[8]|発送time[9]」
			//荷物クラスのsetTime()に合わせるために加工
			String receptTime_String = M[8].substring(0, 4) + "|" + M[8].substring(4, 6) + "|" + M[8].substring(6, 8)
					+ "|" + M[8].substring(8, 10) + "|" + M[8].substring(10, 12);
			String sendTime_String = M[9].substring(0, 4) + "|" + M[9].substring(4, 6) + "|" + M[9].substring(6, 8)
					+ "|" + M[9].substring(8, 10) + "|" + M[9].substring(10, 12);
			ClientInfo client_joho = new ClientInfo();
			client_joho.setClientInfo(M[2], M[3], M[4]);
			client_joho.setHouseInfo(M[5], M[6], M[7]);
			long frglNum = Long.parseLong(M[1]);
			Fragile frgl = new Fragile(frglNum, client_joho, receptTime_String, sendTime_String);
			fragileFile.add(frgl);//荷物を作成した
			junban = searchFragile(frglNum);
			String initializeCalendar = "2000|10|11|11|11";
			fragileFile.get(junban).saveTime("relayArriveTime", initializeCalendar);
			fragileFile.get(junban).saveTime("deliStartTime", initializeCalendar);
			fragileFile.get(junban).saveTime("receiveTime", initializeCalendar);
			fragileFile.get(junban).saveTime("deliFinishTime", initializeCalendar);
			fragileFile.get(junban).setObsStats(ObsStats.none);//障害なしへ更新。
			fragileFile.get(junban).setDeliStats(DeliStats.awaiting);//配達待ちへ更新
			/*
			//中継所に収集ロボットが出発したことを送信する
			Boolean telecomResult_Boolean;
			String startColle_String = "startColle";
			telecomResult_Boolean = telecommunicationSendToRelay(startColle_String);//中継所と通信し、成功したか判定するやつ。
			if (telecomResult_Boolean) {//送信成功
				System.out.printf("中継所に情報：%s を送信しました。\n", startColle_String);
			} else {//送信失敗
				while (true) {
					try {
						System.out.println("送信失敗！配達記録を参照できます。");
						System.out.println("しない場合はまた中継所に情報の送信を行います。");
						telecomResult_Boolean = telecommunicationSendToRelay(startColle_String);
						if (telecomResult_Boolean) {
							System.out.println("中継所に　startColle　を送信成功");
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} //ここまでwhile
			}*/

			break;
		case setFailedPassing://(受付所)中継所受け渡し失敗へ更新命令
			//受信情報：「中継所引き渡し失敗へ更新命令 | 201811191720 | 中継所引き渡し失敗」
			junban = searchFragile(Long.parseLong(M[1]));
			fragileFile.get(junban).setObsStats(ObsStats.failedPassing);
			break;
		case relayTime://(中継所)中継所到着時間の更新命令
			//受信情報：「中継所到着時間の更新命令 | 201811191720 | 201612281255」
			now = getCurrentTime();
			junban = searchFragile(Long.parseLong(M[1]));
			String relayArriveTime_String = now.substring(0, 4) + "|" + now.substring(4, 6) + "|"
					+ now.substring(6, 8) + "|" + now.substring(8, 10) + "|" + now.substring(10, 12);
			fragileFile.get(junban).saveTime("relayArriveTime", relayArriveTime_String);
			fragileFile.get(junban).setObsStats(ObsStats.none);
			fragileFile.get(junban).setDeliStats(DeliStats.awaiting);//戻ってきたから配達待ちに更新
			sendDeliRecord(info);
			break;
		case startDeli://(中継所)配達開始時間の更新命令
			//受信情報：「配達開始時間の更新命令 | 201811191720 | 2016,1,30, 12, 0, 0」
			//配達開始
			now = getCurrentTime();
			junban = searchFragile(Long.parseLong(M[1]));
			String deliStartTime_String = now.substring(0, 4) + "|" + now.substring(4, 6) + "|" + now.substring(6, 8)
					+ "|" + now.substring(8, 10) + "|" + now.substring(10, 12);
			fragileFile.get(junban).saveTime("deliStartTime", deliStartTime_String);
			fragileFile.get(junban).setDeliStats(DeliStats.delivering);//配達中に更新
			break;
		case setAbsent://(中継所)受取人不在に更新命令
			//受信情報：「受取人不在に更新命令 | 201811191720」
			junban = searchFragile(Long.parseLong(M[1]));
			fragileFile.get(junban).setObsStats(ObsStats.absent);
			fragileFile.get(junban).setDeliStats(DeliStats.awaiting);//戻ってきたから配達待ちに更新
			break;
		case deliCompReciTime://(中継所)配達完了
			System.out.println(info + "を中継所から受信しました。");
			//受信情報：「配達完了 | 201811191720 | 1」
			//後ろから、配達完了時間、受取人が受取時間
			//受取時間が＋分して時間を作る、配達完了時間をgetInstance
			receiveTime = getReceiveTime_String(Integer.parseInt(M[2]));
			now = getCurrentTime();
			String receiveTime_String = receiveTime.substring(0, 4) + "|" + receiveTime.substring(4, 6) + "|" + receiveTime.substring(6, 8)
					+ "|" + receiveTime.substring(8, 10) + "|" + receiveTime.substring(10, 12);
			String deliFinishTime_String = now.substring(0, 4) + "|" + now.substring(4, 6) + "|"
					+ now.substring(6, 8)
					+ "|" + now.substring(8, 10) + "|" + now.substring(10, 12);
			junban = searchFragile(Long.parseLong(M[1]));
			fragileFile.get(junban).setObsStats(ObsStats.none);//障害なしに戻す
			fragileFile.get(junban).setDeliStats(DeliStats.delivered);//配達完了に更新
			fragileFile.get(junban).saveTime("receiveTime", receiveTime_String);
			fragileFile.get(junban).saveTime("deliFinishTime", deliFinishTime_String);
			break;
		case setWrgHouse://あて先間違い
			//受信情報：「宛先間違いに更新命令｜201811191720」
			junban = searchFragile(Long.parseLong(M[1]));
			fragileFile.get(junban).setObsStats(ObsStats.wrongHouse);
			fragileFile.get(junban).setDeliStats(DeliStats.awaiting);//戻ってきたから配達待ちに更新
			break;
		}
	}

	/**
	 * 現在時刻を取得し、「201912301200」のような形で返すメソッド
	 * @return 現在時刻をStringでMINUTEまでつなげたものを返す
	 */
	public String getCurrentTime() {
		Calendar cal = Calendar.getInstance();//現在時刻の取得
		String time_Str;
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
		time_Str = sdf1.format(cal.getTime());//#=> 201106150449
		return time_Str;
	}

	/**
	 * 配達完了時刻に受け取った分のint型で分を引いて受取時間を出す
	 * @param minute:配達開始から受取完了するまでにかかった時間（分）
	 * @return 受取完了時刻のString（「201912301200」なフォーマットで返ってくる）
	 */
	public String getReceiveTime_String(int minute) {
		Calendar cal = Calendar.getInstance();
		String time_Str;
		cal.add(Calendar.MINUTE, -minute);
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
		time_Str = sdf1.format(cal.getTime());//#=> 201106150449
		return time_Str;
	}

	/*
	public String reader_ver2() throws Exception {
		String message = "";//初期化

		Calendar targetTime = Calendar.getInstance();
		targetTime.add(Calendar.SECOND, 3);

		while(targetTime.after(Calendar.getInstance())){
			if (reader.ready() == true) {//streamが空かどうかを確かめてる　空じゃなかったらtrue
				//streamが空ならreadLineを呼んではいけない
				message = reader.readLine();
				if (message.contentEquals("")) {
					return null;
				}
				else {
					return message;
				}
			}
		}
		System.out.println("通信処理に移行します。");
		System.out.println();
		return null;
	}
	*/

	/*
	//- 配達記録を公開する() : void
	//多分HQmonitorのコントロールメソッド
	//◆未完,HQmonitorとHQの問題
	private void showDeliRecord() {
		System.out.printf(" 荷物を参照できます(3秒受付)。参照する人は誰ですか？（依頼者の場合0, システム管理者の場合は1,参照せず通信を続行する場合は２）\n");
		System.out.printf("=> ");
		Boolean verify_Boolean;
		try {
			String tmp = null;
			if ((tmp = reader_ver2()) == null) {
				return;
			}

			int Person = Integer.parseInt(tmp);
			switch (Person) {
			case 0://依頼者が参照する。
				verify_Boolean = HQmonitor.verifyClient();
				if (verify_Boolean) {//認証に成功したら
					int junban = searchFragile(HQmonitor.getfrglNum());
					HQmonitor.displayDeliInfo(junban);
					break;
				} else {//認証に失敗したら。
					System.out.println("認証に失敗しました。通信を再開します。");
					System.out.println();
					break;
				}
			case 1://システム管理者が参照する。
					//全ての配達記録、荷物番号を表示する処理
				HQmonitor.displayDeliIndex();
				break;
			case 2://参照せず通信を続行
				return;
			default:
				return;
			}
		} catch (Exception e) {
			System.out.println("BufferedReader, readLineメソッドのIOExceptionが発生しました。");
			return;
		}
	}
	*/

	/**
	 * - 荷物番号をもとに該当する荷物を荷物台帳から検索する(荷物番号 : long) : 荷物
	 * @param frglNum:荷物番号
	 * @return 該当する荷物が入っているArrayListの場所、順番を返す。
	 */
	public Integer searchFragile(long frglNum) {
		int junban;
		Iterator iterator = fragileFile.iterator();
		try {
			for (junban = 0; iterator.hasNext(); junban++) {
				Fragile fragile = (Fragile) iterator.next();
				if (fragile.getFrglNum() == frglNum) {
					return junban;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	private static class Worker implements Callable<String> {

		@Override
		public String call() throws Exception {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			return reader.readLine();
		}

	}
	/*
	// + 荷物台帳を参照する() : void
	private void seeFragile() {
	//- 命令を受信して配達情報の処理をする() : void
	private void executeDeli() {

	}
	}*/

}
