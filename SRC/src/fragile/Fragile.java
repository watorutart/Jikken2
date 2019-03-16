package fragile;

import java.util.Calendar;

import fragile.deliRecord.DeliRecord;
import fragile.deliRecord.DeliStats;
import fragile.deliRecord.ObsStats;

/**
 * 荷物を表すクラス
 * パッケージ外からの荷物に関する操作はこのクラスを介して行います。
 * @author bp16013 上田達也
 *
 */
public class Fragile {

	/**
	 * 荷物番号
	 * not 負の値
	 */
	private long frglNum = 0; //荷物番号

	/**
	 * 依頼情報
	 */
	private ClientInfo clientInfo = new ClientInfo(); //依頼情報

	/**
	 * 配達記録
	 * not Enumで定義されていない値
	 */
	private DeliRecord deliRecord = new DeliRecord(); //配達記録


	////////////////////////////////////コンストラクタ//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 受付所から呼ぶコンストラクタ
	 * @param receptionTime Calendar
	 * @param clientInfo ClientInfo
	 * @param frglNum long
	 * @param deliStats DeliStats
	 * @param obsStats ObsStats
	 */
	public Fragile(Calendar receptionTime, ClientInfo clientInfo, long frglNum, DeliStats deliStats, ObsStats obsStats) {
		this.deliRecord.saveTime("receptionTime",receptionTime);
		try{
			this.clientInfo = clientInfo;
		}catch(NullPointerException e){
			System.out.println("依頼情報が不適切です");
		}
		this.setFrglNum(frglNum);
		this.deliRecord.setDeliStats(deliStats);
		this.deliRecord.setObsStats(obsStats);
	}

	/**
	 * 中継所から呼ぶコンストラクタ
	 * @param clientInfo ClientInfo
	 * @param frglNum long
	 * @param deliStats DeliStats
	 * @param obsStats ObsStats
	 */
	public Fragile(ClientInfo clientInfo, long frglNum, DeliStats deliStats, ObsStats obsStats) {
		this.frglNum = frglNum;
		this.clientInfo = clientInfo;
		this.deliRecord.setDeliStats(deliStats);
		this.deliRecord.setObsStats(obsStats);
	}

	/**
	 * 受取人宅から呼ぶコンストラクタ
	 */
	public Fragile(){
	}

	/**
	 * 本部から呼ぶコンストラクタ
	 * @param frglNum long
	 * @param clientInfo ClientInfo
	 * @param receptionTime String
	 * @param sendTime String
	 */
	public Fragile(long frglNum, ClientInfo clientInfo, String receptionTime, String sendTime){
		this.frglNum = frglNum;
		this.clientInfo = clientInfo;
		this.saveTime("receptionTime",receptionTime);
		this.saveTime("sendTime",sendTime);
	}

	////////////////////////////////////setter//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 荷物番号を記録する
	 * @param frglNum long
	 */
	public void setFrglNum(long frglNum){
		this.frglNum = frglNum;
	}

	/**
	 * 時間を更新する
	 * @param setTime:更新するフィールド名 String
	 * @param time:更新する値 String
	 */
	public void saveTime(String setTime, String time) {
		deliRecord.saveTime(setTime, time);
	}

	/**配達状況を更新する
	 * @param deliStats DeliStats
	 */
	public void setDeliStats(DeliStats deliStats){
		deliRecord.setDeliStats(deliStats);
	}

	/**依頼人情報を更新する
	 * @param clientName String
	 * @param clientTel String
	 * @param clientAddr String
	 */
	public void setClientInfo(String clientName,String clientTel,String clientAddr) {
		this.clientInfo.setClientInfo(clientName,clientTel,clientAddr);
	}

	/**受取人情報を更新する
	 * @param houseName String
	 * @param houseTel String
	 * @param houseAddr String
	 */
	public void setHouseInfo(String houseName,String houseTel,String houseAddr) {
		this.clientInfo.setHouseInfo(houseName,houseTel,houseAddr);
	}

	/**障害状況を更新する
	 * @param obsStats ObsStats
	 */
	public void setObsStats(ObsStats obsStats){
		try{
			this.deliRecord.setObsStats(obsStats);
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException("未定義の配達状況です");
		}
	}

	////////////////////////////////////getter//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 荷物番号を返す
	 * @return long
	 */
	public long getFrglNum() {
		return this.frglNum;
	}

	/**
	 * 配達状況を取得する
	 * @return DeliStats
	 */
	public DeliStats getDeliStats() {
		return this.deliRecord.getDeliStats();
	}

	/**
	 * 依頼人情報を取得する
	 * @return 依頼情報の配列 String[]
	 */
	public String[] getClientInfo() {
		String[] str = new String[3];
		str[0] = this.clientInfo.getClientName();
		str[1] = this.clientInfo.getClientTel();
		str[2] = this.clientInfo.getClientAddr();
		return str;
	}

	/**
	 * 受取人情報を取得する
	 * @return 受取人情報の配列 String[]
	 */
	public String[] getHouseInfo() {
		String[] str = new String[3];
		str[0] = this.clientInfo.getHouseName();
		str[1] = this.clientInfo.getHouseTel();
		str[2] = this.clientInfo.getHouseAddr();
		return str;
	}

	/**
	 * 障害状況を取得する
	 * @return ObsStats
	 */
	public ObsStats getObsStats() {
		return this.deliRecord.getObsStats();
	}

	/**
	 * 配達時間をカレンダー型で取得する
	 * @param getTime:取得したい時間のフィールド名 String
	 * @return Calendar
	 */
	public Calendar getCalTime(String getTime){
		return this.deliRecord.getCalTime(getTime);
	}

	/**
	 * 配達時間をString型で取得する
	 * @param getTime:取得したい時間のフィールド名 String
	 * @return 時間の情報 String
	 */
	public String getStrTime(String getTime){
		return this.deliRecord.getStrTime(getTime);
	}
}
