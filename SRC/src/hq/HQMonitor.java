package hq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import fragile.Fragile;

/**
 * 配達情報を参照する
 * @author 秋山和哉
 * 227行
 */

public class HQMonitor {
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * エンターが押されたらまた入力を受け付けるメソッド
	 * @return 入力された文字列を返す
	 * @throws IOException:BufferedReaderのreadLineメソッドの例外
	 */
	public String reader_ver2() throws IOException {
		String message = reader.readLine();
		while (message.equals("")) {
			message = reader.readLine();
		}
		return message;
	}

	private long frglNum;//荷物番号を一時的に格納する変数
	private long frglTelNum;//依頼人の電話番号を一時的に格納する変数

	/**
	 * 荷物番号のゲッター
	 * @return 荷物番号
	 */
	public long getfrglNum() {
		return frglNum;
	}

	/**
	 * 依頼人電話番号のゲッター
	 * @return 依頼人電話番号
	 */
	public long getfrglTelNum() {
		return frglTelNum;
	}

	/**荷物番号のセッター
	 *
	 * @param num:荷物番号
	 */
	public void setfrglNum(long num) {
		frglNum = num;
	}

	/**
	 * 依頼人電話番号のセッター
	 * @param num:依頼人電話番号
	 */
	public void setfrglTelNum(long num) {
		frglTelNum = num;
	}

	/*public void test() {
		System.out.printf("HQモニターを10秒間実行中です　\n");
		//10秒待つ
		try {
			Thread.sleep(5 * 1000);//5*1000ミリ秒待ちます。
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.printf("HQモニター十秒間終わりました。");
		}
	}*/

	/*
		// + 荷物番号、電話番号の要求を表示する() : void
		public void displayFrglTelNum() {
		}*/

	/**
	 *  + 荷物番号、電話番号の要求を表示する() : void
	 * @return true:認証成功, false:認証失敗
	 */
	public boolean verifyClient() {
		try {
			System.out.printf("荷物番号を入力してください。 => ");
			setfrglNum(Long.parseLong(reader_ver2()));
			System.out.printf("電話番号を入力してください。 => ");
			setfrglTelNum(Long.parseLong(reader_ver2()));
		} catch (IOException e) {
			System.out.println("IOException");
			return false;
		}

		Iterator iterator = HQ.fragileFile.iterator();
		try {
			while(iterator.hasNext()) {
				Fragile fragile = (Fragile) iterator.next();
				String[] str = fragile.getClientInfo();
				if (frglNum == fragile.getFrglNum() && frglTelNum == Long.parseLong(str[1])) {
					return true;
				}
			}
		} catch (Exception e) {
			System.out.println("エラー、荷物がない可能性があります。");
		}
		return false;
	}

	/**
	 *  + 配達記録を表示する() : void
	 * @param junban:表示したいArrayListの場所の配達情報を表示する
	 */
	public void displayDeliInfo(int junban) {//これはたぶん依頼者が参照した場合、該当する荷物番号の配達記録を表示する。
		String ReceptionTime, SendTime, RelayArriveTime, DeliStartTime, ReceiveTime, DeliFinishTime;
		ReceptionTime = HQ.fragileFile.get(junban).getStrTime("receptionTime");
		SendTime = HQ.fragileFile.get(junban).getStrTime("sendTime");
		RelayArriveTime = HQ.fragileFile.get(junban).getStrTime("relayArriveTime");
		DeliStartTime = HQ.fragileFile.get(junban).getStrTime("deliStartTime");
		ReceiveTime = HQ.fragileFile.get(junban).getStrTime("receiveTime");
		DeliFinishTime = HQ.fragileFile.get(junban).getStrTime("deliFinishTime");
		//表示する
		System.out.println("依頼者参照");
		System.out.println("荷物番号：" + HQ.fragileFile.get(junban).getFrglNum());
		System.out.println("受付時間：" + ReceptionTime.substring(0, 4) + "/" + ReceptionTime.substring(4, 6) + "/"
				+ ReceptionTime.substring(6, 8) + " " + ReceptionTime.substring(8, 10) + ":"
				+ ReceptionTime.substring(10, 12));
		System.out.println("発送時間：" + SendTime.substring(0, 4) + "/" + SendTime.substring(4, 6) + "/"
				+ SendTime.substring(6, 8) + " " + SendTime.substring(8, 10) + ":" + SendTime.substring(10, 12));

		if(!RelayArriveTime.contentEquals("200011111111")) {
			System.out.println("中継所到着時間：" + RelayArriveTime.substring(0, 4) + "/" + RelayArriveTime.substring(4, 6) + "/"
					+ RelayArriveTime.substring(6, 8) + " " + RelayArriveTime.substring(8, 10) + ":"
					+ RelayArriveTime.substring(10, 12));}

			if(!DeliStartTime.contentEquals("200011111111")) {
			System.out.println("配達開始時間：" + DeliStartTime.substring(0, 4) + "/" + DeliStartTime.substring(4, 6) + "/"
					+ DeliStartTime.substring(6, 8) + " " + DeliStartTime.substring(8, 10) + ":"
					+ DeliStartTime.substring(10, 12));}

			if(!ReceiveTime.contentEquals("200011111111")) {
			System.out.println("受取時間：" + ReceiveTime.substring(0, 4) + "/" + ReceiveTime.substring(4, 6) + "/"
					+ ReceiveTime.substring(6, 8) + " " + ReceiveTime.substring(8, 10) + ":"
					+ ReceiveTime.substring(10, 12));}

			if(!DeliFinishTime.contentEquals("200011111111")) {
			System.out.println("配達完了時間：：" + DeliFinishTime.substring(0, 4) + "/" + DeliFinishTime.substring(4, 6) + "/"
					+ DeliFinishTime.substring(6, 8) + " " + DeliFinishTime.substring(8, 10) + ":"
					+ DeliFinishTime.substring(10, 12));}
		System.out.println("配達状況：" + HQ.fragileFile.get(junban).getDeliStats());
		System.out.println("障害状況：" + HQ.fragileFile.get(junban).getObsStats());
	}


	/**
	 * + 配達記録の一覧を表示する() : void
	 */
	public void displayDeliIndex() {//これはたぶんシステム管理者が参照した場合、すべての配達記録を表示する。
		System.out.println("システム管理者参照");
		String ReceptionTime, SendTime, RelayArriveTime, DeliStartTime, ReceiveTime, DeliFinishTime;
		Iterator iterator = HQ.fragileFile.iterator();
		Boolean hasNext_Boolean = iterator.hasNext();
		if (hasNext_Boolean) {
			try {
				for (int i = 1; iterator.hasNext(); i++) {
					Fragile fragile = (Fragile) iterator.next();
					ReceptionTime = fragile.getStrTime("receptionTime");
					SendTime = fragile.getStrTime("sendTime");
					RelayArriveTime = fragile.getStrTime("relayArriveTime");
					DeliStartTime = fragile.getStrTime("deliStartTime");
					ReceiveTime = fragile.getStrTime("receiveTime");
					DeliFinishTime = fragile.getStrTime("deliFinishTime");
					System.out.println(i + "番目の荷物");
					System.out.println("荷物番号：" + fragile.getFrglNum());
					System.out.println("受付時間：" + ReceptionTime.substring(0, 4) + "/" + ReceptionTime.substring(4, 6)
							+ "/" + ReceptionTime.substring(6, 8) + " " + ReceptionTime.substring(8, 10) + ":"
							+ ReceptionTime.substring(10, 12));
					System.out.println("発送時間：" + SendTime.substring(0, 4) + "/" + SendTime.substring(4, 6) + "/"
							+ SendTime.substring(6, 8) + " " + SendTime.substring(8, 10) + ":"
							+ SendTime.substring(10, 12));

					if(!RelayArriveTime.contentEquals("200011111111")) {
						System.out.println("中継所到着時間：" + RelayArriveTime.substring(0, 4) + "/" + RelayArriveTime.substring(4, 6) + "/"
								+ RelayArriveTime.substring(6, 8) + " " + RelayArriveTime.substring(8, 10) + ":"
								+ RelayArriveTime.substring(10, 12));}

						if(!DeliStartTime.contentEquals("200011111111")) {
						System.out.println("配達開始時間：" + DeliStartTime.substring(0, 4) + "/" + DeliStartTime.substring(4, 6) + "/"
								+ DeliStartTime.substring(6, 8) + " " + DeliStartTime.substring(8, 10) + ":"
								+ DeliStartTime.substring(10, 12));}

						if(!ReceiveTime.contentEquals("200011111111")) {
						System.out.println("受取時間：" + ReceiveTime.substring(0, 4) + "/" + ReceiveTime.substring(4, 6) + "/"
								+ ReceiveTime.substring(6, 8) + " " + ReceiveTime.substring(8, 10) + ":"
								+ ReceiveTime.substring(10, 12));}

						if(!DeliFinishTime.contentEquals("200011111111")) {
						System.out.println("配達完了時間：：" + DeliFinishTime.substring(0, 4) + "/" + DeliFinishTime.substring(4, 6) + "/"
								+ DeliFinishTime.substring(6, 8) + " " + DeliFinishTime.substring(8, 10) + ":"
								+ DeliFinishTime.substring(10, 12));}

					System.out.println("配達状況：" + fragile.getDeliStats());
					System.out.println("障害状況：" + fragile.getObsStats());
					System.out.println();
				}
			} catch (Exception e) {
				System.out.println("エラー、荷物がない可能性があります。");
			}
		} else {
			System.out.println("エラー、荷物がない可能性があります。");
		}
	}

	/*//+ 「荷物番号または電話番号が間違っています」を表示する() : void
	public void displayErr() {
		System.out.printf("荷物番号または電話番号が間違っています。 \n");
	}
	* + 配達状況を表示する() : void
	public void displayDeliStats() {

	}*/
}
