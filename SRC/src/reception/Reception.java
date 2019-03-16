package reception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import fragile.ClientInfo;
import fragile.Fragile;
import fragile.deliRecord.DeliStats;
import fragile.deliRecord.ObsStats;
import reception.Save.save;
import telecommunication.Receiver;
import telecommunication.Telecommunication;
import telecommunication.ThreadState;
import telecommunication.code.Reception_Collector;
import telecommunication.code.Reception_HQ;

/**
 * 受付所のクラス
 * @author bp16052 鈴木亘
 */
public class Reception {

	private Queue<Fragile> fragile = new LinkedList<Fragile>();		//配達する荷物の待ち行列

	private ReceptionMonitor recpMonitor = new ReceptionMonitor();	//受付所画面のインスタンス

	private Fragile deliWaitFrgl;	//中継所引き渡し完了待ち荷物

	private Telecommunication teleToHQ = new Telecommunication();	//本部との通信に用いるフィールド

	private Telecommunication teleToCllct = new Telecommunication();	//収集ロボットとの通信に用いるフィールド

	private RecpProtocol rcpPrtcl = new RecpProtocol();		//プロトコル

	private ThreadState state = ThreadState.Death;		//通信の状態を表すフィールド

	private Queue<String> sendContentToHQ = new LinkedList<String>(); // 本部へ送れなかったデータを格納する待ち行列

	private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	/**
	 *  コントロールメソッド
	 *  受付所の一連の動作を行うメインメソッド
	 *  荷物の依頼　→　収集ロボットに荷物を渡す　→　中継所引き渡し報告を受け取る　→　本部に報告　→　荷物の依頼　→　...	の順で繰り返す
	 * @param args
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws IllegalArgumentException, IOException, InterruptedException {
		Reception rcp = new Reception();
		Calendar deliWaitStart = Calendar.getInstance();		//収集ロボットに荷物を渡した時間を記録する変数
		final int deliWaitTime = 8;			//収集ロボットに荷物を渡した後から通信を行うまでの待ち時間
		boolean waitFlag = true;		//deliWaitStartの更新をするかどうかを判定する変数
        boolean deliWaitFlag = false;	//中継所引き渡し報告を受け取る動作を行うか判定する変数

        while(true){

            System.out.println("-------------------入力待ち中----------------------------------------");

			// 荷物の依頼を行う
			rcp.requestFrgl(deliWaitFlag);

            System.out.println("-------------------処理中-------------------------------------------");        	//以下はタイムアウトしてもしなくても動作する処理

            // 荷物を渡す
			if(rcp.sendFragile()){
				if(waitFlag){
					deliWaitStart = Calendar.getInstance();
					deliWaitStart.add(Calendar.MINUTE, deliWaitTime);	//荷物を渡したdeliWaitTime分後に中継所引き渡し報告を受け取るを行う
					waitFlag = false;
				}
			}

    		// 中継所引き渡し結果を受け取る
    		if(rcp.deliWaitFrgl != null){
    			deliWaitFlag = deliWaitStart.before(Calendar.getInstance());
    			if(deliWaitFlag){
    				rcp.receivePassingResult();		//中継所引き渡し報告の受取
    				if(rcp.deliWaitFrgl == null){
    					waitFlag = true;
    					deliWaitFlag = false;
    				}
    			}
    		}

    		// 本部へ報告する
    		rcp.sendDataToHQ();

        	Thread.sleep(1000);
        }//ここまでwhile
	}//ここまでmain

	/**
	 * 荷物の依頼を行うメソッド
	 * 依頼情報を入力データから作成し、荷物を作成する
	 * @param flag :荷物送信後、収集ロボットと通信を行うか判定するデータ（trueなら収集ロボットと通信可能）
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void requestFrgl(boolean flag) throws IOException, InterruptedException {
		String ans = "";
		int waitTime = 0;

        if((this.fragile.isEmpty() || this.deliWaitFrgl != null) && sendContentToHQ.isEmpty() && !flag){		//やるべき動作がなければ、依頼の入力を長時間待つ
        		waitTime = 60;
        }
        else waitTime = 10;			//やるべき動作があるなら待ち時間を10秒に設定

        //Enterが押されず waitTime秒経ったらcatch内へ
        Calendar targetTime=Calendar.getInstance();
        targetTime.add(Calendar.SECOND, waitTime);

        System.out.println("依頼情報を入力するにはEnterを押してください");

		while(targetTime.after(Calendar.getInstance())){
			if(this.reader.ready()){
				ans = this.reader.readLine();
				break;
			}
        	Thread.sleep(100);
		}

		//入力の処理
		if(ans == ""){					//タイムアウトしたら次の処理へ
			System.out.println("タイムアウトしました");
			return;
		}
		else if(ans.equals("end")){		//endを入力したら動作終了
			System.out.println("プログラムを終了します");
			System.exit(0);
		}
		else {							//上記以外なら依頼情報の入力へ
			System.out.println("accepted");
			ClientInfo clientInfo = this.recpMonitor.demandClientInfo();

			if (clientInfo != null && !this.judgeClient(clientInfo)) {
				this.recpMonitor.displayErr("依頼情報が間違っています");
			} else {
				Calendar recpTime = Calendar.getInstance();
				long frglNum = this.calcFrglNum(recpTime);
				Fragile frgl = new Fragile(recpTime, clientInfo, frglNum,DeliStats.awaiting, ObsStats.none);
				this.fragile.add(frgl);
				this.recpMonitor.displayAccepted();
				this.recpMonitor.displayFrglNum(frgl.getFrglNum());
			}
		}
	}


	/**
	 * 荷物を渡すメソッド
	 * @return 荷物番号送信の成否（成功したらtrue, 失敗ならfalse）
	 * @throws IllegalArgumentException
	 * @throws RuntimeException
	 */
	private boolean sendFragile() throws IllegalArgumentException, RuntimeException {
		if (!this.fragile.isEmpty() && this.deliWaitFrgl == null) {
			String frglData = this.adjustInfo(Reception_Collector.syncFrglNum.toString());
			System.out.println(frglData);
			Fragile sendFrgl = this.fragile.peek();
			this.state = this.teleToCllct.getThreadState_onlyOnce();

			if(this.state == ThreadState.Death && this.rcpPrtcl.makeProtocol()){
				System.out.println("荷物番号送信中");
				try{
					if (this.teleToCllct.sendSignal(frglData, Receiver.collector,Receiver.reception, 10)) {
						System.out.println("荷物番号送信完了");

						//現在時刻を取得し、String型へ変換
						Calendar currentTime = Calendar.getInstance();
						SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy|MM|dd|HH|mm");
						String sendTime = sdf1.format(currentTime.getTime());//#=> 201106150449

						sendFrgl.setDeliStats(DeliStats.delivering);
						sendFrgl.setObsStats(ObsStats.none);
						sendFrgl.saveTime("sendTime", sendTime);
						this.deliWaitFrgl = sendFrgl;
						this.fragile.remove();
						String sendContent = this.adjustInfo(Reception_HQ.makeFragile.toString());

						System.out.println("収集ロボットに渡した荷物の情報を本部へ報告中");
						if (!this.teleToHQ.sendSignal(sendContent, Receiver.hq,Receiver.reception, 10)) {
							this.sendContentToHQ.add(sendContent);
						}
						return true;
					}
				}catch(IOException ioe){
					System.out.println(ioe);
				}
			}
		}
		return false;
	}

	/**
	 * 中継所引き渡し結果を受け取るメソッド
	 * @throws IOException
	 * @throws RuntimeException
	 */
	private void receivePassingResult() throws IOException, RuntimeException {
		//String sendContent = "";
		this.state = this.teleToCllct.getThreadState_onlyOnce();

		if(this.state == ThreadState.Death && this.rcpPrtcl.makeProtocol()){
			System.out.println("中継所引き渡し報告受取中");
			String recvContent = this.teleToCllct.receiveSignal(Receiver.collector,Receiver.reception, 10);

			//ThreadStateを確認して、その状態から処理を決定する
			this.state = this.teleToCllct.getThreadState_onlyOnce();
			if(this.state == ThreadState.Death);
			else if(this.state == ThreadState.Success)
				recvContent = this.teleToCllct.getReceiveDetail_onlyOnce();
			else return;

			if(recvContent == null || recvContent.equals(""))return;

			System.out.println("受信内容：" + recvContent);

			if(this.save(recvContent, save.deliCompFrglNum, this.deliWaitFrgl)){	//受信内容が中継所引き渡し報告か判定
				System.out.println("中継所引き渡し報告受取成功");
				if (this.deliWaitFrgl.getObsStats() == ObsStats.failedPassing) {
					// 中継所引き渡し失敗の場合
					Fragile frgl = this.pickFragile();	//中継所引き渡し完了待ち荷物を荷物の待ち行列に追加
					frgl.setDeliStats(DeliStats.awaiting);
					this.fragile.add(frgl);
					return;
				} else {
					// 中継所引き渡し完了の場合
					this.deliWaitFrgl = null;
					return;
				}

			}
			System.out.println("中継所引き渡し報告受取失敗");
			return;
		}
		return;
	}



	/**
	 *  本部へ報告する
	 * @throws IOException
	 * @throws RuntimeException
	 */
	private void sendDataToHQ() throws IOException, RuntimeException{
		if (!this.sendContentToHQ.isEmpty()) {
			System.out.println("送信に失敗した情報を本部に報告中");
			if (this.teleToHQ.sendSignal(this.sendContentToHQ.peek(), Receiver.hq, Receiver.reception, 10)) {
				this.sendContentToHQ.remove();
			}
		}
	}

	/**
	 * 依頼情報が適切か判定するメソッド
	 * すべての情報がnullでないかと住所が正しいかを判定する
	 * @param info :判定する依頼情報
	 * @return 依頼情報が適切ならtrue, 不適切ならfalse
	 */
	private Boolean judgeClient(ClientInfo info) {
		if(info.getClientName() == null || info.getClientTel() == null || info.getHouseName() == null || info.getHouseTel() == null)
			return false;
		else if (!judgeAddress(info.getClientAddr()))
			return false;
		else if (!judgeAddress(info.getHouseAddr()))
			return false;
		else
			return true;
	}

	/**
	 *  住所が正しいか判定するメソッド
	 *  @param str :判定する文字列
	 *  @return 住所として適切ならtrue, 不適切ならfalse
	 */
	public boolean judgeAddress(String str) {
		if (str.length() == 3) {
			if (isNumber(String.valueOf(str.charAt(0)))
					&& isNumber(String.valueOf(str.charAt(2)))
					&& str.charAt(1) == '-') {

				// i1,i2 に住所の番号を格納する
				int i1 = Integer.parseInt(String.valueOf(str.charAt(0)));
				int i2 = Integer.parseInt(String.valueOf(str.charAt(2)));

				// 住所の番号の値が範囲内（0 から 3）か判定する
				if (0 <= i1 && i1 <= 3 && 0 <= i2 && i2 <= 3) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 文字列が数値かどうか判定するメソッド
	 * @param str :判定する文字列
	 * @return 文字列が数値ならtrue, それ以外ならfalse
	 */
	public boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 荷物番号を計算するメソッド
	 * @param cal :依頼を受け付けた時間
	 * @return 年月日時分秒までの時間をlong型へ変換した数値
	 */
	private long calcFrglNum(Calendar cal) {
		long frglNum;
		frglNum = cal.get(Calendar.SECOND);
		frglNum += 100 * cal.get(Calendar.MINUTE);
		frglNum += 10000 * cal.get(Calendar.HOUR_OF_DAY);
		frglNum += 1000000 * cal.get(Calendar.DATE);
		frglNum += 100000000 * (cal.get(Calendar.MONTH) + 1);
		frglNum += 10000000000L * (Long.valueOf(cal.get(Calendar.YEAR)));
		return frglNum;
	}


	/**
	 * 中継所引き渡し待ち荷物を取り出すメソッド
	 * このメソッドを使用した場合、中継所引き渡し完了待ち荷物にはnullが代入される
	 * @return 中継所引き渡し完了待ち荷物（deliWaitFrgl）に入っていた荷物
	 */
	private Fragile pickFragile() {
		Fragile frgl = this.deliWaitFrgl;
		this.deliWaitFrgl = null;
		return frgl;
	}

	/**
	 * 加工した情報を記録するメソッド
	 * @param content :加工する内容
	 * @param order :命令番号
	 * @param frgl :中継所引き渡し完了待ち荷物
	 * @return 加工する内容の先頭にある命令番号が正しければtrue,それ以外はfalseを返す
	 */
	private boolean save(String content, save order, Fragile frgl) {
		String[] str = content.split("\\|", 0);
		if (str[0].equals(Reception_Collector.setDeliCompFrglNum.toString())) {		//命令番号が正しいか判定
			if(str[2].equals("true"))
				frgl.setDeliStats(DeliStats.delivering);
			else if(str[2].equals("false"))
				frgl.setObsStats(ObsStats.failedPassing);
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * 情報を加工するメソッド
	 * @param order :命令番号
	 * @return 各命令番号の手段で加工されたデータ
	 */
	private String adjustInfo(String order) {
		String data = null;
		long num = 0;


		switch (order) {

		// 荷物番号を送るための情報を文字列にまとめる処理
		case "syncFrglNum":
			num = (this.fragile.peek()).getFrglNum();
			String frglNum = String.valueOf(num);
			data = order + "|" + frglNum;
			break;

		case "makeFragile":
			String[] clientInfo = this.deliWaitFrgl.getClientInfo();
			String[] houseInfo = this.deliWaitFrgl.getHouseInfo();
				data = order
						+ "|"
						+ String.valueOf(this.deliWaitFrgl.getFrglNum())
						+ "|"
						+ clientInfo[0]
						+ "|"
						+ clientInfo[1]
						+ "|"
						+ clientInfo[2]
						+ "|"
						+ houseInfo[0]
						+ "|"
						+ houseInfo[1]
						+ "|"
						+ houseInfo[2]
						+ "|"
						+ (this.deliWaitFrgl.getStrTime("receptionTime"))
						+ "|"
						+ (this.deliWaitFrgl.getStrTime("sendTime"));
			break;
		}

		return data;
	}




	/*
	//テストプログラム　
	public static void main(String args[]) throws IOException, RuntimeException{
		Reception rcp = new Reception();
		Queue<String> sendContentToHQ = new LinkedList<String>(); // 本部へ送れなかったデータを格納するメソッド
		String sendContent = null;
		//Calendar receptionTime, ClientInfo clientInfo, long frglNum, DeliStats deliStats, ObsStats obsStats
		Calendar testTime = Calendar.getInstance();
		ClientInfo clInfo = rcp.recpMonitor.demandClientInfo();
		//rcp.requestFrgl();
		//sendContent = rcp.sendFragile();
		//System.out.println(sendContent);
		rcp.deliWaitFrgl = new Fragile(testTime, clInfo, rcp.calcFrglNum(testTime), DeliStats.delivering, ObsStats.none);
   		// 中継所引き渡し結果を受け取る
		while (rcp.deliWaitFrgl != null) {
			sendContent = rcp.receivePassingResult();
			System.out.println(sendContent);
			if (sendContent != null) {
				sendContentToHQ.add(sendContent);
			}
		}
	}
	*/

	/*
	//本部に荷物作成命令を出すテストプログラム
	public static void main(String args[]) throws IOException, RuntimeException{
		Reception rcp = new Reception();
		Queue<String> sendContentToHQ = new LinkedList<String>(); // 本部へ送れなかったデータを格納するメソッド
		String sendContent = null;

		Calendar testTime = Calendar.getInstance();

		ClientInfo clInfo = rcp.recpMonitor.demandClientInfo();

		rcp.deliWaitFrgl = new Fragile(testTime, clInfo, rcp.calcFrglNum(testTime), DeliStats.delivering, ObsStats.none);

		sendContent = rcp.adjustInfo(Reception_HQ.makeFragile.toString());

		System.out.println(sendContent);

		boolean flag = false;

   		// 中継所引き渡し結果を受け取る
		while (!flag) {
			flag = rcp.teleToHQ.sendSignal(sendContent, Receiver.hq, Receiver.reception, 10);
			if(flag)System.out.println("送信済み：" + sendContent);
		}
	}
	*/
}