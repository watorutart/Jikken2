import fragile.Fragile;
//障害状況の判定に使用
//use for obstacle judgement
import fragile.deliRecord.ObsStats;
import telecommunication.Telecommunication;
//通信クラスのメソッドで引数として必要
//use for Telecommunication methods
import telecommunication.Receiver;
//命令番号の列挙型
//Commands as enumeration
import telecommunication.code.*;
import lejos.utility.Delay;
//時間計測に必要
//needs for time measurement
import lejos.utility.Stopwatch;
//通信時の例外キャッチに必要
//needs in try-catch
import java.io.IOException;

class TestPassFrglCaseOne{
	private int reciTime = 0;
	private boolean hasFragile =true;
	private int countGray = 6;
	//加工保存の際に共有変数の保持をする
	//hold lock in method save()
	private boolean lock=false;
	//加工保存された宛先情報をif分岐で使用
	//use for saved correctHouse judgement
	private boolean correctHouse=false;
	//時間計測に必要
	//needs for time measurement
	//参考文献 Reference: https://qiita.com/amapyon/items/7853b070240a86de5efa
	Stopwatch stopwatch=null;
	//住所から移動すべきx-y座標を特定するための変数
	//x-y axis for moving to supposed address
	private int x, y;

	private Fragile fragile=new Fragile();////////////////////////////////////////
	private Telecommunication telecommunication=new Telecommunication();
	
				
	private void passFragile() {
		//送受信の内容を保持する変数
		//hold send/receive details
		String syncDetail;

		//通信時に使用するフラグを保持するための変数
		//hold telecommunication flag
		boolean flag=false;
			
			
		//do{
			syncDetail=this.adjust(Adjustment.clientHouseNameAddr);
				
			Delay.msDelay(10);

			try{
				flag=this.telecommunication.sendSignal(syncDetail, Receiver.house, Receiver.deliver, 120);
			}
			catch(IOException ioe){
				System.out.println("House "+this.x+"-"+this.y+" did not reply.  Recorded Absent...");
			}
		//} while(flag==false);

		if(flag){
			do{
				Delay.msDelay(10);
				try{
					syncDetail=this.telecommunication.receiveSignal(Receiver.house, Receiver.deliver, 2);
				}
				catch(IOException ioe){
					continue;
				}
			}while(syncDetail=="");

			this.exeHouseOrder(syncDetail);

			if(this.correctHouse==false) this.fragile.setObsStats(ObsStats.wrongHouse);
			else{
				this.fragile.setObsStats(ObsStats.none);

				syncDetail=this.adjust(Adjustment.adjustFragile);

				do{
					Delay.msDelay(10);
					try{
						flag=this.telecommunication.sendSignal(syncDetail, Receiver.house, Receiver.deliver, 2);
					}
					catch(IOException ioe){
						continue;
					}
				} while(flag==false);

				do{
					Delay.msDelay(10);
					try{
						syncDetail=this.telecommunication.receiveSignal(Receiver.house, Receiver.deliver, 2);
					}
					catch(IOException ioe){
						continue;
					}
				} while(syncDetail=="");

				this.exeHouseOrder(syncDetail);

				//任意時間のため除く
				//excluded for time uncertain
				//this.stopTime();
			}
		}
		else this.fragile.setObsStats(ObsStats.absent);
	}
		
	/*
	//任意時間のため除く
	//excluded for time uncertain
	private void startTime() {
		//最初のnullの状態でインスタンスを作成し、2回目以降はリセットすればひとつのインスタンスを使い回せる
		//make instance for the first time (null) and recycle it from next time with method reset()
		if(this.stopwatch==null){
			//インスタンスを作ると時間の計測が始まる
			//starts time measurement when made a new instance.
			this.stopwatch=new Stopwatch();
		}
		else this.stopwatch.reset();  //経過時間をリセット reset passed time
	}

	private void stopTime() {
		//返される時間の単位はミリ秒
		//returned time is millisecond
		this.reciTime=this.stopwatch.elapsed();
	}*/
		
	private String adjust(Adjustment order) {
		//加工した内容を返すための変数
		//String for returning detail
		String detail="";
		
		
		if(order==Adjustment.relayProtocol){
			detail+="protocol|relay";
		}
		else if(order==Adjustment.houseProtocol){
			detail+="protocol|house";
		}
		else if(order==Adjustment.odrNumHasFragile){
			detail+="sendHasFrgl";
		}
		else if(order==Adjustment.odrFrglNumObs){
			detail+="reportDeliFail|";
			detail+=String.valueOf(this.fragile.getFrglNum());
			detail+="|";
			detail+=String.valueOf(this.fragile.getObsStats());
		}
		else if(order==Adjustment.odrFrglNumObsReciTime){
			detail+="reportDeliResult|";
			detail+=String.valueOf(this.fragile.getFrglNum());
			detail+=String.valueOf(this.reciTime);
		}
		else if(order==Adjustment.odrNumLock){
			detail+="sendLock";
		}
		else if(order==Adjustment.lockFalse){
			detail+="setLockFalse|";
			this.lock=false;
			detail+=String.valueOf(this.lock);
		}
		else if(order==Adjustment.clientHouseNameAddr){
			//依頼情報の内容を保持するための変数
			//String array for holding Client Info
			//String[] tmpClient, tmpHouse;///////////////////////////////////
			
			//tmpClient=this.fragile.getClientInfo();//////////////////////////////
			//tmpHouse=this.fragile.getHouseInfo();///////////////////////////////
			
			//テストケース使用
			//made up for test case
			detail="personAddrName|Ren Sato|0-0|Yuduki Suzuki|0-1";
			
			//detail="personAddName|"+tmpClinet[0]+"|"+tmpClient[2]+"|"+tmpHouse[0]+"|"+tmpHouse[2];
		}
		else if(order==Adjustment.adjustFragile){
			detail+="syncFrglNum|";
			detail+=String.valueOf(this.fragile.getFrglNum());
		}
		else System.err.println("Warning!!  Error when adjust.");

		return detail;
	}
	
	private void save(String reciDetail, Save order) {
		//命令番号をよけるために使う
		//for avoiding code
		String[] tmp=reciDetail.split("\\|");
		
		if(order==Save.issueProtocol){
			//this.isProtocol=Boolean.valueOf(tmp[1]);
		}
		else if(order==Save.hasFragile){
			this.hasFragile=Boolean.valueOf(tmp[1]);
			if(tmp.length==2) this.fragile.setFrglNum(Long.valueOf(tmp[2]));
		}
		else if(order==Save.frglInfo){
			this.fragile.setFrglNum(Long.valueOf(tmp[1]));
			this.fragile.setClientInfo(tmp[1], null, tmp[3]);
			this.fragile.setHouseInfo(tmp[4], null, tmp[5]);
		}
		else if(order==Save.saveLock){
			this.lock=Boolean.valueOf(tmp[1]);
		}
		else if(order==Save.houseInfo){
			this.correctHouse=Boolean.valueOf(tmp[1]);
		}
		else if(order==Save.takenConf){
			//荷物の受取確認に使用
			//use for fragile passed confirmation
			boolean hasConfirmed;
			hasConfirmed=Boolean.valueOf(tmp[1]);
			if(hasConfirmed) System.out.println("Delivery Completed!!  Go back to Relay area!!");
		}
		else System.err.println("Warning!!  Error when save.");
	}
	
	//受取人宅間の命令番号を参照し、然るべき操作をする
	//See code of Deliver_House, execute appropriate operation
	private void exeHouseOrder(String syncDetail){
		String[] tmp=syncDetail.split("\\|");
		//Debug
		System.out.println("Debug: "+tmp[0]);
			
		Deliver_House code=Deliver_House.valueOf(tmp[0]);
			
		switch(code){
		case protocol:
			this.save(syncDetail, Save.issueProtocol);
			break;
		case personAddrName:
			System.out.println("Received code, personAddrName but not used...");
			break;
		case syncJudge:
			this.save(syncDetail, Save.houseInfo);
			break;
		case syncFrglNum:
			System.out.println("Received code, syncFrglNum but not used...");
			break;
		case syncHasReceived:
			this.save(syncDetail, Save.takenConf);
			break;
		default:
			System.err.println("Error: Received unexpected code.");
			//this.isProtocol=false;
			break;
		}
	}
	
	//コントロールメソッド
	//control method
	//荷物が正しく渡せるかどうかをテストする
	//test whether fragile will be passed
	public void control(){
		int test=0;
		
		
		while(this.hasFragile){
			this.passFragile();
					
			//宛先が正しいかどうかで分岐。なお、荷物が渡せたかは、exeHouseOrder(String syncDetail)メソッド、および、
			//save(String reciDetail, Save order)メソッドにて把握できる。
			//judge whether house is correct.  When recognizing fragile passing, see 
			//exeHouseOrder(String syncDetail) and save(String reciDetail, Save order).
			if(this.correctHouse/*==false*/){
					test++;
					System.out.println("OK: Test cleared "+test+" time(s).");
					//Delay.msDelay(5000);  //for test
			}
		}
	}
	
	public static void main(String args[]){
		TestPassFrglCaseOne EV3=new TestPassFrglCaseOne();
		
		EV3.control();
	}
}