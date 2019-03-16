package house;

import java.io.IOException;
import java.util.LinkedList;

import lejos.utility.Delay;
import Classmodel.Fragile.Fragile;
import Classmodel.Telecommunication.Receiver;
import Classmodel.Telecommunication.Telecommunication;
import Classmodel.Telecommunication.ThreadState;
import Classmodel.Telecommunication.Code.Deliver_House;

/**
 * 受取人宅クラス
 * @author ナパット 
 */
public class test_getFrafile_fromRobot {

	private final int num=16;

	//private OrderedSet＜String_ address = 0-0, 0-1, 0-2, 0-3, 0-4, 1-0, 1-1, 1-2, 1-3, 1-4, 2-0, 2-1, 2-2, 2-3, 2-4, 3-0, 3-1, 3-2, 3-3, 3-4, 4-0, 4-1, 4-2, 4-3, 4-4;
	final LinkedList<String> address = new LinkedList<String>();
	
	//private OrderedSet_Integer_tel = 04800000000, 04800000001, 04800000002, 04800000003, 04800010000, 04800010001, 04800010002, 04800010003, 04800020000, 04800020001, 04800020002, 04800020003, 04800030000, 04800030001, 04800030002, 04800030003;
	final LinkedList<String> tel = new LinkedList<String>();
	
	
	//private OrderedSet＜String_ name = Ren Sato, Yuduki Suzuki, Sou Takahashi, Yua Tanaka, Taiga Ito, Yuna Watanabe, Yamato Yamamoto, Ann Nakamura, Akito Kobayashi, Sakura Kato, Yuma Yoshida, Rin Yamada, Tatsuki Sasaki, Mei Yamaguchi, Yota Matsumoto, Aoi Inoue;
	final LinkedList<String> name = new LinkedList<String>();
	
	
	//private OrderedSet_荷物_ fragile = null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null;	
	LinkedList<Fragile> fragile = new LinkedList<Fragile>();

	private Telecommunication tele=new Telecommunication();
	
	
	
	public test_getFrafile_fromRobot(){
		final String address_tmp[]={"0-0", "0-1", "0-2", "0-3", "0-4", "1-0", "1-1", "1-2", "1-3", "1-4", "2-0", "2-1", "2-2", "2-3", "2-4", "3-0", "3-1", "3-2", "3-3", "3-4", "4-0", "4-1", "4-2", "4-3", "4-4"};
		final String name_tmp[]={"Ren Sato", "Yuduki Suzuki", "Sou Takahashi", "Yua Tanaka", "Taiga Ito", "Yuna Watanabe", "Yamato Yamamoto", "Ann Nakamura", "Akito Kobayashi", "Sakura Kato", "Yuma Yoshida", "Rin Yamada", "Tatsuki Sasaki", "Mei Yamaguchi", "Yota Matsumoto", "Aoi Inoue"};
		final String tel_tmp[]={"04800000000", "04800000001", "04800000002", "04800000003", "04800010000", "04800010001", "04800010002", "04800010003", "04800020000", "04800020001", "04800020002", "04800020003", "04800030000", "04800030001", "04800030002", "04800030003"};
		//final Fragile fragile_tmp[]={null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
				
		for(int i=0;i<this.num;i++){	
			this.address.add(address_tmp[i]);
			this.name.add(name_tmp[i]);
			this.tel.add(tel_tmp[i]);
			//this.fragile.set(i, fragile_tmp[i]);			
		}			
	}
	
	ThreadState state=ThreadState.Death;//現在通信中の相手の状態
	Receiver partner;//現在通信中の相手を保持
	boolean isProtocol;
	
	//＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊
		//荷物を渡すには受取人宅の情報を判定し、正しかったら受け取る。
		public void takeFrglfromDeli() {
			int i=0;
			String reciDetail;
			
			
			while(true){
											
				//依頼人と受取人宅の住所と氏名を受け取る		
				this.recieveClientInfo();
				
				//受取人住所、受取人氏名を照合する
				//照合結果を受信する（syncJudge+judgeFlag）
				Boolean result = sendHouseInfo();
				
				System.out.println(++i+""+result);
				
				//荷物番号を受取、照合結果がtrueだったら、syncHasReceived+trueと返す
					if(result == true){
						do{
						takeFragile();
						}while(this.isProtocol == false);
					}
				}
			}
		
		//＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊
			
	
	/*------------------------------------------------------------------------------------------------*/
	
	//プロトコル通信
	//protocol communication
	void receiveProtocol(){
		String receive="";//受信内容
		state = ThreadState.Death;
		
		receive=this.receiveInfo();
		this.save(receive, Save.issueProtocol);

	}
	
	/*------------------------------------------------------------------------------------------------*/
	
	//通信相手があっているかを判定する
	
	private boolean isTruePartner(){
		if(this.partner==Receiver.house) return true;
		else return false;
	}
	
	/*------------------------------------------------------------------------------------------------*/
	//プロトコルを送信する
	private void sendProtocol(){
		String sendDetail="";
		
		
		if(this.isTruePartner()){
			sendDetail=this.adjust(Adjustment.rightProtocol);
			this.isProtocol=true;
		}
		else{
			sendDetail=this.adjust(Adjustment.wrongProtocol);
			this.isProtocol=false;
		}			
		
		//相手に確立したサブシステム名を送信
		//send subsystem name established with opponent
		this.sendInfo(sendDetail);
		
	}
	
	/*------------------------------------------------------------------------------------------------*/
	
	//依頼人と受取人宅の住所と氏名をreceiveInfoで受け取る				
	private int recieveClientInfo() {
		
		String receive="";//受信内容		
		state = ThreadState.Death;		
		
		receive=this.receiveInfo();
		this.save(receive,Save.recordNameAddr);
				
		return 0;//仮で0番地
	}
	
	
	/*------------------------------------------------------------------------------------------------*/
	// 照合結果を受信する（syncJudge+judgeFlag）
	private Boolean sendHouseInfo() {
		this.judgeFlag=this.judgeHouseInfo();
		String contentSend=this.adjust(Adjustment.odrNumResult);	//送信
		this.sendInfo(contentSend);
		return this.judgeFlag;
	}
	
	/*------------------------------------------------------------------------------------------------*/
	private void takeFragile() {
		
		//照合結果がtrueだったら、syncHasReceived+trueと返す

		String receive="";//受信内容
		state = ThreadState.Death;
		
		while(receive==""){
					
			if(state!=ThreadState.Death){
				state=this.tele.getThreadState_onlyOnce();
				if(state==ThreadState.Success){
					System.out.printf("インターバル後の受信成功\n");
					receive=this.tele.getReceiveDetail_onlyOnce();
					System.out.printf("受信内容 : %s\n",receive);
					state=ThreadState.Death;
					break;
				}
				else if(state==ThreadState.Fail){
					System.out.printf("インターバル後の受信失敗\n");
					state=ThreadState.Death;
					continue;
				}
				continue;
			}

				
			try {
				receive=tele.receiveSignal(Receiver.deliver, Receiver.house, 3);
				partner=Receiver.collector;
			
			} catch (IOException e){//インターバルが必要
				System.out.printf(e.getLocalizedMessage()+"\n");
				this.state=ThreadState.Run_receive;
			}
		}
		
		this.save(receive,Save.odrFrglNum);
		
		String contentConfirm=this.adjust(Adjustment.recieveCheck);
		
		this.sendInfo(contentConfirm);
	}

	/*------------------------------------------------------------------------------------------------*/

	//受取人住所、受取人氏名を照合する
	
	boolean judgeFlag;
	private boolean judgeHouseInfo() {
		Fragile f=this.fragile.getLast();
		String content[]=f.getHouseInfo();//content[0]=name,content[1]=tel(null),content[2]=addr					
				
		for(int i=0;i<this.num;i++){
			
			if(content[2].equals(this.address.get(i))){
				
				if(content[0].equals(this.name.get(i))) return true;
				
			}
								
		}					
		
		return false;
	}
	
	
	/*------------------------------------------------------------------------------------------------*/
	
	
	//ロボットに送信する情報を加工してから返す
	private String adjust(Adjustment order) {
		
		switch(order){
		case odrNumResult:
		
			return Deliver_House.syncJudge+"|"+judgeFlag;
			
		case recieveCheck:			
						
			return Deliver_House.syncHasReceived+"|true";
			
		case rightProtocol:			
			
			return Deliver_House.protocol+"|true";		
			
		case wrongProtocol:
			
			return Deliver_House.protocol+"|false";
			
		}		
		
		return null;
	}

	/*------------------------------------------------------------------------------------------------*/
	//受け取った文字列をsaveによって加工する
	private void save(String receive, Save save) {
		String /*OrderNum,*/clientName,houseAddr,houseName,clientAddr,frglNum;
		switch(save){
		case recordNameAddr:
			String[] message = receive.split("\\|",0); 
			 //OrderNum = message[0];
			clientName = message[1];
			clientAddr = message[2];			 
			 houseName = message[3];
			 houseAddr = message[4];
			Fragile f= new Fragile();//
			f.setClientInfo(clientName,null,clientAddr);
			f.setHouseInfo(houseName,null,houseAddr);
				
			this.fragile.add(f);//set(i,f);
			break;
		case odrFrglNum:
			String[] message1 = receive.split("\\|",0); 
			 //OrderNum = message1[0];
			 frglNum = message1[1];
			 Fragile f2=this.fragile.getLast();
			 f2.setFrglNum(Long.valueOf(frglNum));
			 break;
		case issueProtocol:
			String[] message2 = receive.split("\\|",0);
			System.out.println("Debug: 0="+message2[0]+"1="+message2[1]);
			this.partner=Receiver.valueOf(message2[1]);
			break;
		case confirmProtocol:
			String[] message3 = receive.split("\\|",0);
			this.isProtocol=Boolean.valueOf(message3[1]);
			break;
		default:
			break;
			}
		}
	
	/*------------------------------------------------------------------------------------------------*/
	
	
	/*//long型かどうかを判定する
	private boolean isLong(String frglNum){
		try{
			Long.parseLong(frglNum);
		}
		catch(NumberFormatException nfe){
			return false;
		}
		
		return true;
	}*/
	
	//情報を送信する
	private void sendInfo(String contentSend){
		
		Receiver partner;//現在通信中の相手を保持
		ThreadState state=ThreadState.Death;//現在通信中の相手の状態

		System.out.println("送信予定：                                              "+contentSend);/////////////////////////
				
			while(true){
				if(state!=ThreadState.Death){
						state=this.tele.getThreadState_onlyOnce();
						if(state==ThreadState.Success){
							System.out.printf("インターバル後の送信成功\n");
							state=ThreadState.Death;
							break;
						}
						else if(state==ThreadState.Fail){
							System.out.printf("インターバル後の送信失敗\n");
							state=ThreadState.Death;
							continue;
						}
						continue;
					}
						
				Delay.msDelay(500);////////////////////////////////////////////////
				
					try{
						partner=Receiver.deliver;
						if(this.tele.sendSignal(contentSend, Receiver.deliver, Receiver.house,3)){
							System.out.printf("送信成功\n");//送信成功
							break;
						}
						else{
							System.out.printf("%sと送信失敗\n",partner.name());//送信失敗
							continue;
						}
					}
					catch(IOException e){//インターバルが必要
						System.out.printf(e.getLocalizedMessage()+"\n");
						state=ThreadState.Run_send;
					}
			}	
		}
	

/*------------------------------------------------------------------------------------------------*/
	//情報を受信する
	private String receiveInfo() {
		String contentReceive="";   //受信内容
		state = ThreadState.Death;
		
		while(contentReceive.equals("")){
			if(state!=ThreadState.Death){
				state=this.tele.getThreadState_onlyOnce();
				if(state==ThreadState.Success){
					System.out.printf("インターバル後の受信成功\n");
					contentReceive=this.tele.getReceiveDetail_onlyOnce();
					System.out.printf("受信内容 : %s\n",contentReceive);
					state=ThreadState.Death;
					if(!contentReceive.equals("")) break;
				}
				else if(state==ThreadState.Fail){
					System.out.printf("インターバル後の受信失敗\n");
					state=ThreadState.Death;
					continue;
				}
				continue;
			}
	
			Delay.msDelay(500);//////////////////////////////////////////////////////
				
			try {
				contentReceive=tele.receiveSignal(Receiver.deliver, Receiver.house, 3);
				partner=Receiver.deliver;
			
			} catch (IOException e){//インターバルが必要
				System.out.printf(e.getLocalizedMessage()+"\n");
				this.state=ThreadState.Run_receive;
			}
			
		}
		
		return contentReceive;
		
	}
	
	public static void main(){
		
	}
}


