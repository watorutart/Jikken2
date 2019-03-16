import java.io.IOException;
import fragile.Fragile;
import lejos.utility.Delay;
import lineTrace.LineTrace_Deli;
import telecommunication.Telecommunication;
import telecommunication.Receiver;
import telecommunication.code.Relay_Deliver;

class TestMoveRelay{
	private int reciTime = 0;
	private boolean hasFragile =false;
	//private int countGray = 0;
	//加工保存の際に共有変数の保持をする
	//hold lock in method save()
	private boolean lock=false;
	//加工保存された宛先情報をif分岐で使用
	//use for saved correctHouse judgement
	private boolean correctHouse=false;
	private Fragile fragile=new Fragile();
	private LineTrace_Deli lineTrace=new LineTrace_Deli();
	private Telecommunication telecommunication=new Telecommunication();
	//プロトコルが正しいかを判定
	//judge whether protocol is correct
	private boolean isProtocol;
	
	
	private void lock() {
		//送受信の内容を保持する変数
		//hold send/receive details
		String syncDetail;

		//通信時に使用するフラグを保持するための変数
		//hold telecommunication flag
		boolean flag=false;

		do{
			syncDetail=this.adjust(Adjustment.odrNumLock);

			do{
				try{
					flag=this.telecommunication.sendSignal(syncDetail, Receiver.relay, Receiver.deliver, 2);
				}
				catch(IOException ioe){
					continue;
				}
			}while(flag==false);

			do{
				try{
					syncDetail=this.telecommunication.receiveSignal(Receiver.relay, Receiver.deliver, 2);
				}
				catch(IOException ioe){
					continue;
				}
			}while(syncDetail=="");

			this.exeRelayOrder(syncDetail);
		} while(this.lock);
	}
	
	private void moveRelay() {
		//excluded when organized test
		this.lineTrace.WaitingToCollision();

		//相手識別
		//recognize opponent
		do{
			this.makeProtocol(Receiver.relay);
		} while(this.isProtocol==false);
		
		this.lock();
		
		//excluded when organized test
		this.lineTrace.ToRelay();
	}
	
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
			//detail+="|none|";//cancel
			detail+=String.valueOf(this.reciTime);
		}
		else if(order==Adjustment.odrNumLock){
			detail+="sendLock";
		}
		else if(order==Adjustment.lockFalse){
			if(this.hasFragile) detail+="goHouse";
			else detail+="backWaitingArea";
			this.lock=false;
			//detail+=String.valueOf(this.lock);  //delete here
		}
		else if(order==Adjustment.clientHouseNameAddr){
			//依頼情報の内容を保持するための変数
			//String array for holding Client Info
			String[] tmpClient, tmpHouse;////////////////////////////////////////
			
			tmpClient=this.fragile.getClientInfo();//////////////////////////////
			tmpHouse=this.fragile.getHouseInfo();///////////////////////////////
			
			//テスト時に使用
			//for test usage
			//detail="personAddrName|Ren Sato|0-0|Yuduki Suzuki|0-1";
			
			detail="personAddName|"+tmpClient[0]+"|"+tmpClient[2]+"|"+tmpHouse[0]+"|"+tmpHouse[2];
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
			this.isProtocol=Boolean.valueOf(tmp[1]);
		}
		else if(order==Save.hasFragile){
			this.hasFragile=Boolean.valueOf(tmp[1]);
			//if(tmp.length==2) this.fragile.setFrglNum(Long.valueOf(tmp[2]));
		}
		else if(order==Save.frglInfo){
			this.fragile.setFrglNum(Long.valueOf(tmp[1]));
			this.fragile.setClientInfo(tmp[2], null, tmp[3]);
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
	
	private void exeRelayOrder(String syncDetail){
		String[] tmp=syncDetail.split("\\|");
		Relay_Deliver code=Relay_Deliver.valueOf(tmp[0]);
		
		switch(code){
		case protocol:
			this.save(syncDetail, Save.issueProtocol);
			break;
		case sendLock:
			System.out.println("Received code, sendLock but not used...");
			break;
		case syncLock:
			this.save(syncDetail, Save.saveLock);
			break;
		case sendHasFrgl:
			System.out.println("Received code, sendHasFrgl but not used...");
			break;
		case syncHasFrgl:
			this.save(syncDetail, Save.hasFragile);
			break;
		case syncFrglInfo:
			this.save(syncDetail, Save.frglInfo);
			break;
		case setLockFalse:
			System.out.println("Received code, setLockFalse but not used...");
			break;
		/*case backWaitingArea:
			System.out.println("Received code, backWaitingArea but not used...");
			break;
		case goHouse:
			System.out.println("Received code, backWaitingArea but not used...");*/
		case reportDeliFail:
			break;
		case reportDeliResult:
			System.out.println("Received code, reportDeliResult but not used...");
			break;
		default:
			System.err.println("Error: Received unexpected code.");
			this.isProtocol=false;
			break;
		}
	}
	
	void makeProtocol(Receiver partner){
		String syncDetail="";
		boolean flag=false;
		
		
		if(partner==Receiver.relay) syncDetail=adjust(Adjustment.relayProtocol);
		else if(partner==Receiver.house) syncDetail=adjust(Adjustment.houseProtocol);
		
		Delay.msDelay(10);
		
		//サブシステム名を要求する
		//demand opponent after established connection
		do{
			try{
				System.out.println("Debug: protocol send");///////////////////////
				flag=this.telecommunication.sendSignal(syncDetail, partner, Receiver.deliver, 120);
			}
			catch(IOException ioe){
				continue;
			}
		} while(flag==false);
		
		Delay.msDelay(10);
		
		//通信相手が正しいかを知る
		//judge who communicate with
		do{
			try{
				syncDetail=this.telecommunication.receiveSignal(partner, Receiver.deliver, 20);
			}
			catch(IOException ioe){
				continue;
			}
		} while(syncDetail.equals(""));
		
		if(partner==Receiver.relay) this.exeRelayOrder(syncDetail);
		//else if(partner==Receiver.house) this.exeHouseOrder(syncDetail);
		else{
			System.err.println("Illegal input on isProtocol(): Input relay or house.");
		}
		
	}
	
	public void control(){
		this.moveRelay();
	}
	
	public static void main(String args[]){
		TestMoveRelay EV3=new TestMoveRelay();
		
		EV3.control();
	}
}