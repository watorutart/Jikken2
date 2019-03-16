
package deliver;

//import lejos.utility.Delay;
import telecommunication.Telecommunication;
import telecommunication.Receiver;

import java.io.IOException;

/**
 * コネクション確立クラス EstConnection Class
 * @author bp16110 渡辺亮一 Ryoichi Watanabe
 */
public class EstConnection extends Thread{
	private Telecommunication tele;
	private String sendDetail;
	
	/**
	 * コネクション確立クラスのコンストラクタ.
	 * @param tele 配達ロボットのクラスで使用する通信クラスを使うこと.  そうでなければ, 後の通信は失敗する.
	 */
	public EstConnection(Telecommunication tele){
		this.tele=tele;
	}
	
	/**
	 * スレッドを開始するメソッド.  送信の準備のために, 
	 * フィールドにあるsendDetailを配達ロボットのクラスと同期する.
	 * @param sendDetail 送信情報.  ここでは, 依頼人情報と受取人情報.
	 */
	 public void start(String sendDetail){
		 this.sendDetail=sendDetail;
		 this.start();
	}
	
	 /**
	  * ２つのうちの１つのスレッドを実行する.  受取人宅にコネクションを確立しに行き, 
	  * 後の処理を速やかに行うために用意した.
	  */
	public void run(){
		this.tele.setSyncThread();
		try {
			this.tele.sendSignal(this.sendDetail, Receiver.house, Receiver.deliver, 0);//waitTime=0
			
			//System.out.printf("send(wait) : %s\n",this.sendDetail);//////////////////////////////
		} catch (IOException e) {
			;
		}
		this.tele.resetSyncThread();
	}
}