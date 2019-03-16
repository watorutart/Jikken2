package telecommunication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.OutputConnection;

import lejos.remote.nxt.BTConnection;
import lejos.remote.nxt.BTConnector;
import lejos.utility.Delay;

/**
 * 通信クラス(共有クラス)
 * @author 三森
 * @version 2.0
 */
public final class Telecommunication{
	/**
     * コンストラクタ
     */
    public Telecommunication(){
    	this.threadState=ThreadState.Death;
    	this.resetSyncThread();
    }

	/**
	 * Bluetoothを用いた通信において、openメソッドを使用する場合のマルチスレッド処理用クラス
	 * @author 三森
	 */
	private class Com_ConnectorFunOpenUser extends Thread{
		private String address;

		/**
		 * マルチスレッドによる通信開始
		 * @param address :EV3のMacアドレス
		 * @param ts :スレッドの状態(Run_send or Run_receive)
		 */
		public void start(final String address,final ThreadState ts){
			if(ts!=ThreadState.Run_send && ts!=ThreadState.Run_receive)
				return;

			this.address=address;
			threadState=ts;
			this.start();
		}
		public void run(){
			try{
				connection = Connector.open(this.address);//接続失敗後、再接続に約1分を要する=>ハードウェアの仕様上どうしようもない
				synchronized(this){
					if(connection==null){
						threadState=ThreadState.Fail;
						return;
					}

					if(threadState==ThreadState.Run_send)
						dos = new DataOutputStream(((OutputConnection)connection).openOutputStream());
					else if(threadState==ThreadState.Run_receive)
						dis = new DataInputStream(((InputConnection)connection).openInputStream());
					System.out.printf("%s : Connection ok\n",this.address);

					if(threadState==ThreadState.Run_send){
						if(!send())
							threadState=ThreadState.Fail;
					}
					else if(threadState==ThreadState.Run_receive){
						if(!receive())
							threadState=ThreadState.Fail;
					}

					try{
						connection.close();
						if(threadState==ThreadState.Run_send)
							dos.close();
						else if(threadState==ThreadState.Run_receive)
							dis.close();
					}catch(IOException ioe){
						System.err.println(ioe.getMessage());
						threadState=ThreadState.Fail;
						return;
					}
				}
			}
			catch(IOException e){
				synchronized(this){
					connection=null;
					dos=null;
					dis=null;
					threadState=ThreadState.Fail;
					return;
				}
			}
			threadState=ThreadState.Success;
		}
	}
	private Com_ConnectorFunOpenUser com_cfou;
	/**
	 * Com_ConnectorFunOpenUserクラスを使用する場合、「Connector.open(address)」を実行中の通信先を保持する
	 */
	private Receiver active_ComPartner;
	/**
	 * Com_ConnectorFunOpenUserクラスのスレッドの状態を保持
	 */
	private ThreadState threadState;

	/* EV3の表示に関して
	 * LCD.clear() & LCD.drawString("wait connection", 0, 1) は文字の削除が可能。但し自動スクロール機能なし(import lejos.hardware.lcd.LCD;)
	 * System.out.println("")　は文字の削除不可能(システムが勝手に表示しているのはコレ)。自動スクロール機能あり
	 */

	/**
	 * 秒数の単位変換用
	 */
	private final short CHANGE_S=1000;

	/* 通信用インスタンスの変数の準備 */
	private BTConnection bt_connection;
	private Connection connection;
    private DataInputStream dis;
    private DataOutputStream dos;

    /**
     * 送信する文字列
     */
    private String sendDetail;
    /**
     * 受信する文字列
     */
    private String receiveDetail;

    /**
     * 送信を行う
     * @return　送信結果(true:成功,false:失敗)
     */
    private synchronized boolean send(){
    	final int WAIT=(int)(0.2f*this.CHANGE_S);//送信前に少し待つ
    	Delay.msDelay(WAIT);
    	try{
    		this.dos.writeUTF(this.sendDetail);
    		this.dos.flush();
    	}catch(IOException ioe){
    		System.err.println(ioe.getMessage());
    		return false;
    	}
    	System.out.printf("send : %s\n",this.sendDetail);
    	return true;
    }
    /**
     * 受信を行う
     * @return 受信結果(true:成功,false:失敗)
     */
    private synchronized boolean receive() throws RuntimeException{

    	final int WAIT=(int)(0.05f*this.CHANGE_S);//受信前に少し待つ
    	Delay.msDelay(WAIT);
		try{
			this.receiveDetail = this.dis.readUTF();
		}catch(IOException ioe){
			System.err.println(ioe.getMessage());
			return false;
		}
		System.out.printf("receive : %s\n",this.receiveDetail);
		return true;

		/*
    	///////////////////デバック用/////////////////////////////////////////////////////
    	System.out.println("receive...");
    	final int wt=5;
    	DebugReceive debug=new DebugReceive();
    	debug.start();
    	try {
			debug.join(wt*this.CHANGE_S);//受信が終了するか、wt秒経過するまで待つ
		} catch (InterruptedException e) {
			System.err.println(e);
			return false;
		}
    	if(debug.isAlive()){
    		final int error_time=3;
    		System.out.printf("error wait "+error_time+"s...");
    		Delay.msDelay(error_time*this.CHANGE_S);//error_time秒待つ事で、コネクションを確立した両方で例外を投げるようにする
    		throw new RuntimeException("Receive : Debug Exception "+wt+"s");
    	}
    	return true;
    	*/
    }
    //////////////////////デバック用↓/////////////////////////////////////////////////////
    /*private class DebugReceive extends Thread{
    	public void run(){
    		final int WAIT=(int)(0.05f*CHANGE_S);//受信前に少し待つ
        	Delay.msDelay(WAIT);
    		try{
    			receiveDetail = dis.readUTF();
    		}catch(IOException ioe){
    			System.err.println(ioe.getMessage());
    			return;
    		}
    		System.out.printf("receive : %s\n",receiveDetail);
    	}
    }*/
    /////////////////////デバック用↑//////////////////////////////////////////////////////

    /**
     * 送受信時の初期化処理
     * @throws RuntimeException :マルチスレッドによる処理の完了前に次の送受信を呼び出した場合に投げられる
     */
	private void init() throws RuntimeException{
		if(this.threadState!=ThreadState.Death)//スレッドが終了していない
			throw new RuntimeException(this.active_ComPartner.name()+" : getThreadState_onlyOnce()メソッドで実行状況をgetして下さい");
		if(this.receiveDetail!=null)//受信内容を呼び出し元に渡していない(受信内容を呼び出し元がgetしていない)
			throw new RuntimeException("受信内容をgetしていません。getReceiveDetail_onlyOnce()メソッドで受信内容をgetして下さい");

		this.sendDetail=this.receiveDetail=null;
		this.bt_connection=null;
		this.connection=null;
		this.dis=null;
		this.dos=null;
	}

	/**
	 * open()メソッドを使用し、コネクションを確立しにいく
	 * @param receiver :通信相手
	 * @param waitTime :待機時間
	 * @param threadState :送信 or 受信
	 * @return 送受信結果(true:成功,false:失敗)
	 * @throws IOException :待機時間以内にコネクション確立処理が終了しなかった場合に投げられる
	 */
	private boolean getConnect(final Receiver receiver,final int waitTime,final ThreadState threadState) throws IOException{
		Delay.msDelay(1*this.CHANGE_S);//コネクションを連続で確立しようとすると、エラーの原因になるので少し待つ

		this.active_ComPartner=receiver;
		this.com_cfou=new Com_ConnectorFunOpenUser();
		this.com_cfou.start(receiver.getMacAddress(),threadState);//スレッド開始
		Delay.msDelay(waitTime*this.CHANGE_S);
		synchronized(this){
			if(this.com_cfou.isAlive()){
				System.out.printf("コネクションの確立に失敗しました。1分程度以内のインターバル後、%sに接続&通信を自動的に試みます。それまで他の通信は行えません。通信状態はgetThreadState_onlyOnce()メソッドで確認して下さい\n",this.active_ComPartner.name());
				throw new IOException(this.active_ComPartner.name());//インターバル後にコネクションを試みる通信相手を例外として返す
			}

			switch(this.getThreadState_onlyOnce()){
			case Death:
			case Fail:
				System.out.printf("%sとのコネクションの確立に失敗 or 通信に失敗しました。\n",this.active_ComPartner.name());
				return false;
			case Success:
				return true;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * waitForConnection()メソッドを使用し、コネクションが確立するのを待つ
	 * @param waitTime :待機時間
	 * @param threadState :送信 or 受信
	 * @return　送受信結果(true:成功,false:失敗)
	 */
	private boolean waitConnect(final int waitTime,final ThreadState threadState){
		if(threadState!=ThreadState.Run_send && threadState!=ThreadState.Run_receive)
			return false;

		Delay.msDelay(1*this.CHANGE_S);//コネクションを連続で確立しようとすると、エラーの原因になるので少し待つ

		BTConnector connector = new BTConnector();
	    System.out.printf("wait connection\n");
	    this.bt_connection = connector.waitForConnection(waitTime*this.CHANGE_S, BTConnection.RAW);
        if(this.bt_connection==null){//時間内にコネクションが確立出来なかった場合は自動的にconnector.close();が呼ばれているが、再度ソケットを確保してしまうため、もう一度呼ぶ必要性がある
        	connector.close();
        	System.out.printf("timeout connection : %dS\n",waitTime);
		    return false;
        }
        System.out.printf("connection!\n");

        this.waitThread();

        if(threadState==ThreadState.Run_send)
        	this.dos = this.bt_connection.openDataOutputStream();
        else if(threadState==ThreadState.Run_receive)
        	this.dis = this.bt_connection.openDataInputStream();

        if(threadState==ThreadState.Run_send){
        	if(!this.send())
        		return false;
        }
        else if(threadState==ThreadState.Run_receive){
        	if(!this.receive())
        		return false;
        }

        try{
        	if(threadState==ThreadState.Run_send)
        		this.dos.close();
        	else if(threadState==ThreadState.Run_receive)
        		this.dis.close();
        	this.bt_connection.close();
			connector.close();

        }catch(IOException ioe){
    		System.out.printf("closing error!\n");
    		return false;
        }
        return true;
	}

	/**
	 * connect()メソッドを使用し、コネクションを確立しにいく
	 * @param receiver :通信相手
	 * @param waitTime :待機時間
	 * @param threadState :送信 or 受信
	 * @return　送受信結果(true:成功,false:失敗)
	 */
	private boolean getConnectToEV3(final Receiver receiver,final int waitTime,final ThreadState threadState){
		if(threadState!=ThreadState.Run_send && threadState!=ThreadState.Run_receive)
			return false;

		Delay.msDelay(1*this.CHANGE_S);//コネクションを連続で確立しようとすると、エラーの原因になるので少し待つ

		BTConnector connector = new BTConnector();
	    System.out.printf("Search connect\n");

	    Calendar targetTime=Calendar.getInstance();
		targetTime.add(Calendar.SECOND, waitTime);
		while(true){
			try{
				this.bt_connection = connector.connect(receiver.getEV3_name(), BTConnection.RAW);
			}
			catch(Exception e){
				System.out.printf("error connection\n");
				connector.close();//ソケット解放の為に、ここはcloseメソッドを呼び出してもOK
				if(!targetTime.before(Calendar.getInstance()))//waitTime秒経過していない
					continue;
				else
					return false;
			}
			if(this.bt_connection==null){
				if(targetTime.before(Calendar.getInstance())){//waitTime秒経過
					System.out.printf("timeout connection : %dS\n",waitTime);
					return false;
				}
				else
					continue;
			}
			else
				break;
		}
        System.out.printf("connection!\n");

        if(threadState==ThreadState.Run_send)
        	this.dos = this.bt_connection.openDataOutputStream();
        else if(threadState==ThreadState.Run_receive)
        	this.dis = this.bt_connection.openDataInputStream();

        if(threadState==ThreadState.Run_send){
        	if(!this.send())
        		return false;
        }
        else if(threadState==ThreadState.Run_receive){
        	if(!this.receive())
        		return false;
        }

        try{
        	if(threadState==ThreadState.Run_send)
        		this.dos.close();
        	else if(threadState==ThreadState.Run_receive)
        		this.dis.close();
        	this.bt_connection.close();
			//(注意)connect側は通信終了時にconnectorのclose()メソッドは呼び出さないこと

        }catch(IOException ioe){
    		System.out.printf("closing error!\n");
    		return false;
        }
        return true;
	}

	private final int RECEPTION_HQ_PORT=5000;
	private final int HOUSE_HQ_PORT=6000;
	/**
	 * ソケット通信におけるポート番号
	 */
	private int PORT;
	/**
	 * ソケット通信におけるアドレス
	 */
	private final String IP_ADDRESS="localhost";//サーバ側とクライアント側で同じパソコンで実行

	/**
	 * ソケット通信におけるサーバ側
	 * @param waitTime :待機時間
	 * @param threadState :送信 or 受信
	 * @return 送受信結果(true:成功,false:失敗)
	 */
	private boolean serverConnect(final int waitTime,final ThreadState threadState){
		if(threadState!=ThreadState.Run_send && threadState!=ThreadState.Run_receive)
			return false;

		ServerSocket servsock=null;
		Socket sock=null;
		final int NUM_CON=2;//同時接続可能数
		try{
			servsock=new ServerSocket(this.PORT,NUM_CON);
			servsock.setSoTimeout(waitTime*this.CHANGE_S);
			sock=servsock.accept();

	        if(threadState==ThreadState.Run_send)
	        	this.dos = new DataOutputStream(sock.getOutputStream());
	        else if(threadState==ThreadState.Run_receive)
	        	this.dis = new DataInputStream(sock.getInputStream());

	        if(threadState==ThreadState.Run_send){
	        	if(!this.send()){
	        		sock.close();
	            	servsock.close();
	        		return false;
	        	}
	        }
	        else if(threadState==ThreadState.Run_receive){
	        	if(!this.receive()){
	        		sock.close();
	            	servsock.close();
	        		return false;
	        	}
	        }

        	if(threadState==ThreadState.Run_send)
        		this.dos.close();
        	else if(threadState==ThreadState.Run_receive)
        		this.dis.close();

        	sock.close();
        	servsock.close();
		}
		catch(SocketTimeoutException e){//タイムアウト例外
			System.out.printf("%d秒以内にクライアント側からのコネクションの確立要求がありませんでした\n",waitTime);
			try{//ここのclose()処理は入れないと繰り返し処理の際のエラーとなる
				servsock.close();
			}
			catch(IOException e2){
				return false;
			}
			return false;
		}
		catch(IOException e){
			return false;
		}
		return true;
	}

	/**
	 * ソケット通信におけるクライアント側
	 * @param waitTime :待機時間
	 * @param threadState :送信 or 受信
	 * @return 送受信結果(true:成功,false:失敗)
	 */
	private boolean clientConnect(final int waitTime,final ThreadState threadState){
		if(threadState!=ThreadState.Run_send && threadState!=ThreadState.Run_receive)
			return false;

		Socket sock=null;
		Calendar targetTime=Calendar.getInstance();
		targetTime.add(Calendar.SECOND, waitTime);
		while(true){
			try{
				sock=new Socket(this.IP_ADDRESS,this.PORT);
				break;
			}
			catch(IOException e){
				if(targetTime.before(Calendar.getInstance())){//waitTime秒経過
					System.out.printf("%d秒以内にサーバとコネクション確立出来ませんでした\n",waitTime);
					return false;
				}
				continue;
			}
		}

		try{
			if(threadState==ThreadState.Run_send)
				this.dos = new DataOutputStream(sock.getOutputStream());
			else if(threadState==ThreadState.Run_receive)
				this.dis = new DataInputStream(sock.getInputStream());

			if(threadState==ThreadState.Run_send){
	        	if(!this.send()){
	        		sock.close();
	        		return false;
	        	}
	        }
	        else if(threadState==ThreadState.Run_receive){
	        	if(!this.receive()){
	        		sock.close();
	        		return false;
	        	}
	        }

        	if(threadState==ThreadState.Run_send)
        		this.dos.close();
        	else if(threadState==ThreadState.Run_receive)
        		this.dis.close();

        	sock.close();
		}
		catch(IOException e){
			return false;
		}
		return true;
	}

	/**
	 * 通信クラスで どのprivateメソッドを呼ぶかのプロトコル一覧
	 */
	enum Protocol {BT_open,BT_wait,BT_connect,Socket_server,Socket_client};
	/**
	 * 通信を行うシステムの組み合わせによって決定される 適切なプロトコルを返す
	 * @param pair :通信相手
	 * @param issued :通信を行うシステム
	 * @return 適切なプロトコル
	 * @throws RuntimeException :定義されていない組み合わせが引数で渡された場合に投げられる
	 */
	private Protocol getProtocol (final Receiver pair,final Receiver issued) throws RuntimeException{
		final String errMessage="定義されていない組み合わせです!";

		switch(issued){
		case reception:
			switch(pair){
			case collector:
				return Protocol.BT_open;
			case hq:
				this.PORT=this.RECEPTION_HQ_PORT;
				return Protocol.Socket_client;
			default:
				throw new RuntimeException(errMessage);
			}
		case collector:
			switch(pair){
			case reception:
			case relay:
				return Protocol.BT_wait;
			default:
				throw new RuntimeException(errMessage);
			}
		case relay:
			switch(pair){
			case collector:
			case deliver:
				return Protocol.BT_connect;
			case hq:
				return Protocol.BT_wait;
			default:
				throw new RuntimeException(errMessage);
			}
		case deliver:
			switch(pair){
			case relay:
			case house:
				return Protocol.BT_wait;
			default:
				throw new RuntimeException(errMessage);
			}
		case house:
			switch(pair){
			case deliver:
				return Protocol.BT_open;
			case hq:
				this.PORT=this.HOUSE_HQ_PORT;
				return Protocol.Socket_client;
			default:
				throw new RuntimeException(errMessage);
			}
		case hq:
			switch(pair){
			case reception:
				this.PORT=this.RECEPTION_HQ_PORT;
				return Protocol.Socket_server;
			case house:
				this.PORT=this.HOUSE_HQ_PORT;
				return Protocol.Socket_server;
			case relay:
				return Protocol.BT_open;
			default:
				throw new RuntimeException(errMessage);
			}
		default:
			throw new RuntimeException(errMessage);
		}
	}

	/* ユーザによる入力や表示要求にリアルタイムで対応出来るようにした*/
	/**
	 * 送信する。送信した結果が返り値として帰ってくる。
	 * もし待機時間以内に通信が終了しなかった場合はIOExceptionが投げられてくる。この場合インターバル後(約1分後)にコネクションの確立と通信を自動的に試みる為(マルチスレッド処理)、getThreadState_onlyOnce()メソッドで通信状態を得る事が出来る。
	 * 例外を返した場合、このメソッドを呼ばない限り、再度このメソッドを使用する事は出来ない。
	 *
	 * なお、上記を気にしなくてはいけないのは、EV3と通信を行う(Bluetoothを使用する)場合に「動く」となっているPCの送信の場合のみである。(EV3同士の通信は関係無し)
	 * @param sendDetail :送信内容
	 * @param receiver :送信先
	 * @param issued :送信を行うシステム
	 * @param waitTime :待機時間
	 * @return 送信結果(true:成功,false:失敗)
	 * @throws IOException :待機時間以内にコネクション確立の処理が終了しなかった場合に投げられる。この場合は、裏でマルチスレッドによって通信処理が動いている。
	 * @throws RuntimeException :マルチスレッドによる処理結果をgetしていないのに、再度このメソッドを呼んだ場合に投げられる。
	 */
	public boolean sendSignal(final String sendDetail,final Receiver receiver,final Receiver issued,final int waitTime) throws IOException,RuntimeException{
		this.init();
		this.sendDetail=sendDetail;

		switch(this.getProtocol(receiver, issued)){
		case BT_open:
			return this.getConnect(receiver, waitTime, ThreadState.Run_send);
		case BT_wait:
			return this.waitConnect(waitTime, ThreadState.Run_send);
		case BT_connect:
			return this.getConnectToEV3(receiver, waitTime, ThreadState.Run_send);
		case Socket_server:
			return this.serverConnect(waitTime, ThreadState.Run_send);
		case Socket_client:
			return this.clientConnect(waitTime, ThreadState.Run_send);
		default:
			return false;
		}
	}
	/**
	 * 受信する。受信した結果が返り値として帰ってくる。
	 * もし待機時間以内に通信が終了しなかった場合はIOExceptionが投げられてくる。この場合インターバル後(約1分後)にコネクションの確立と通信を自動的に試みる為(マルチスレッド処理)、getThreadState_onlyOnce()メソッドで通信状態を得る事が出来る。
	 * また、成功した場合はgetReceive_onlyOnce()メソッドで受信内容を得る事が出来る。
	 * 例外を返した場合、これらのメソッドを呼ばない限り、再度このメソッドを使用する事は出来ない。
	 *
	 * なお、上記を気にしなくてはいけないのは、EV3と通信を行う(Bluetoothを使用する)場合に「動く」となっているPCの受信の場合のみである。(EV3同士の通信は関係無し)
	 * @param sender :送信元
	 * @param issued :受信を行うシステム
	 * @param waitTime :待機時間
	 * @return 送信結果(受信内容:成功,空文字 "":失敗)
	 * @throws IOException :待機時間以内にコネクション確立の処理が終了しなかった場合に投げられる。この場合は、裏でマルチスレッドによって通信処理が動いている。
	 * @throws RuntimeException :マルチスレッドによる処理結果,受信内容をgetしていないのに、再度このメソッドを呼んだ場合に投げられる。
	 */
	public String receiveSignal(final Receiver sender,final Receiver issued,final int waitTime) throws IOException,RuntimeException{
		this.init();
		final String FALSE_STRING="";//受信失敗時に返す文字列

		switch(this.getProtocol(sender, issued)){
		case BT_open:
			if(this.getConnect(sender, waitTime, ThreadState.Run_receive))
				return this.getReceiveDetail_onlyOnce();
			break;
		case BT_wait:
			if(this.waitConnect(waitTime, ThreadState.Run_receive))
				return this.getReceiveDetail_onlyOnce();
			break;
		case BT_connect:
			if(this.getConnectToEV3(sender, waitTime, ThreadState.Run_receive))
				return this.getReceiveDetail_onlyOnce();
			break;
		case Socket_server:
			if(this.serverConnect(waitTime, ThreadState.Run_receive))
				return this.getReceiveDetail_onlyOnce();
			break;
		case Socket_client:
			if(this.clientConnect(waitTime, ThreadState.Run_receive))
				return this.getReceiveDetail_onlyOnce();
			break;
		default:
			return FALSE_STRING;
		}
		return FALSE_STRING;
	}

	/**
	 * マルチスレッドの状態を返す。
	 * このメソッドは1スレッド(1ループ)での処理で2回以上呼ばないこと。
	 * 例=>「if(getThreadState_onlyOnce()) else if(getThreadState_onlyOnce())」 -----> 「tmp=getThreadState_onlyOnce(); if(tmp) else if(tmp)」
	 * @return スレッドの状態
	 * @throws RuntimeException :未定義の状態がセットされている場合
	 */
	public synchronized ThreadState getThreadState_onlyOnce() throws RuntimeException{
		switch(this.threadState){
		case Run_send:
		case Run_receive:
		case Death:
			return this.threadState;
		case Success:
		case Fail:
			ThreadState tmp=this.threadState;
			this.threadState=ThreadState.Death;
			return tmp;
		default:
			throw new RuntimeException("定義されていない状態がthreadStateに格納されています");
		}
	}

	/**
	 * 受信内容を返す。
	 * このメソッドは1スレッド(1ループ)での処理で2回以上呼ばないこと。
	 * 例=>「if(getReceiveDetail_onlyOnce()) else if(getReceiveDetail_onlyOnce())」 -----> 「tmp=getReceiveDetail_onlyOnce(); if(tmp) else if(tmp)」
	 * @return 受信内容
	 * @throws RuntimeException :何も受信していない場合に投げられる
	 */
	public synchronized String getReceiveDetail_onlyOnce() throws RuntimeException{
		if(this.receiveDetail==null)
			throw new RuntimeException("何も受信していません");

		String tmp=this.receiveDetail;
		this.receiveDetail=null;
		return tmp;
	}


	/*スレッドの同期処理実装*/
	/**
	 * スレッドのwait・notifyを実行するかどうか。
	 */
	private boolean syncThread;
	/**
	 * スレッドをwaitしたかどうか
	 */
	private boolean isWaitThread;
	/**
	 * スレッドによるコネクション確立時に、コネクション確立後にスレッド処理を止めると設定する。(これはwaitConnectメソッドのみに影響あり)
	 * 送受信を開始するには、notifyThread()メソッドを呼ぶ。
	 */
	public void setSyncThread(){
		this.syncThread=true;
		this.isWaitThread=false;
	}
	/**
	 * スレッドによるコネクション確立時に、コネクション確立後にスレッド処理を止めずに送受信処理まで行う と設定する。(これはwaitConnectメソッドのみに影響あり)
	 */
	public void resetSyncThread(){
		this.syncThread=false;
	}
	/**
	 * setSyncThread()メソッドを呼んでいた場合、wait()メソッドでスレッドを停止させる
	 */
	private synchronized void waitThread(){
		if(this.syncThread){
    		try {
    			this.isWaitThread=true;
    			this.wait();
    		} catch (InterruptedException e){
    			System.err.println("wait()メソッドは実行出来ませんでした");
    			this.isWaitThread=false;
    			return;
    		}
    	}
	}

	/**
	 * 送受信処理を開始する。
	 * @exception RuntimeException :設定をONにしていないのに、このメソッドを呼び出した場合に投げられる
	 */
	public synchronized void notifyThread() throws RuntimeException{
		if(!this.syncThread)
			throw new RuntimeException("スレッド処理を止める設定になっていません");
		this.notify();
	}
	/**
	 * スレッドをwaitしたかどうかを返す
	 * @return :waitしたかどうか(true:waitした,false:waitしていない)
	 */
	public synchronized boolean isWaitThread(){
		return this.isWaitThread;
	}
}
