package house;

import java.io.IOException;
import java.util.LinkedList;

import lejos.utility.Delay;

import Classmodel.Fragile.Fragile;
import Classmodel.Telecommunication.Receiver;
import Classmodel.Telecommunication.Telecommunication;
import Classmodel.Telecommunication.ThreadState;


public class test_receive_clientInfo {
	
	
	private final int num=16;

	//private OrderedSet＜String_ address = 0-0, 0-1, 0-2, 0-3, 0-4, 1-0, 1-1, 1-2, 1-3, 1-4, 2-0, 2-1, 2-2, 2-3, 2-4, 3-0, 3-1, 3-2, 3-3, 3-4, 4-0, 4-1, 4-2, 4-3, 4-4;
	final LinkedList<String> address = new LinkedList<String>();
	
	//private OrderedSet_Integer_tel = 04800000000, 04800000001, 04800000002, 04800000003, 04800010000, 04800010001, 04800010002, 04800010003, 04800020000, 04800020001, 04800020002, 04800020003, 04800030000, 04800030001, 04800030002, 04800030003;
	final LinkedList<String> tel = new LinkedList<String>();
	
	
	//private OrderedSet＜String_ name = Ren Sato, Yuduki Suzuki, Sou Takahashi, Yua Tanaka, Taiga Ito, Yuna Watanabe, Yamato Yamamoto, Ann Nakamura, Akito Kobayashi, Sakura Kato, Yuma Yoshida, Rin Yamada, Tatsuki Sasaki, Mei Yamaguchi, Yota Matsumoto, Aoi Inoue;
	final LinkedList<String> name = new LinkedList<String>();
	
	
	//private OrderedSet_荷物_ fragile = null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null;	
	LinkedList<Fragile> fragile = new LinkedList<Fragile>();
	
	ThreadState state=ThreadState.Death;//現在通信中の相手の状態
	
	private Telecommunication tele=new Telecommunication();
	Receiver partner;//現在通信中の相手を保持
	
	public test_receive_clientInfo(){
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
	
	
	//荷物を渡すには受取人宅の情報を判定し、正しかったら受け取る。
			public void takeFrglfromDeli() {
				
																										
					//依頼人と受取人宅の住所と氏名を受け取る		
					this.recieveClientInfo();		
					
			}
					
			
	//依頼人と受取人宅の住所と氏名をreceiveInfoで受け取る				
		private int recieveClientInfo() {
			
			String receive="";//受信内容		
			state = ThreadState.Death;		
			
			receive=this.receiveInfo();
			this.save(receive,Save.recordNameAddr);
					
			return 0;//仮で0番地
		}
						
	
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
	
				
		//情報を加工して記録する(受信内容,氏名と住宅を記録する)
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
			
			
			default:
				break;
				}
			}
	
	
		public static void main(){
			
		}
}
