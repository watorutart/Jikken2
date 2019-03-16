package fragile;

/**
 * 依頼情報を表すクラス
 * パッケージ外から依頼情報の取得、更新を行う場合はこのクラスを利用します
 * @author bp16013 上田達也
 */
public class ClientInfo {
	
	/**
	 * 依頼人氏名
	 */
	private String clientName = null;

	/**
	 * 依頼人電話番号
	 */
	private String clientTel = null;

	/**
	 * 依頼人住所
	 */
	private String clientAddr = null;

	/**
	 * 受取人氏名
	 */
	private String houseName = null;

	/**
	 * 受取人電話番号
	 */
	private String houseTel = null;

	/**
	 * 受取人住所
	 */
	private String houseAddr = null;

	/**
	 * @依頼人情報 not null, 空文字
	 */
	/**依頼人情報を更新する
	 * @param clientName:依頼人氏名 String
	 * @param clientTel:依頼人電話番号 String
	 * @param clientAddr:依頼人住所 String
	 */
	public void setClientInfo(String clientName,String clientTel,String clientAddr){
		//フィールドを更新する
		this.clientName = clientName;
		this.clientTel =  clientTel;
		this.clientAddr = clientAddr;
	}

	/**
	 * @受取人情報 not null, 空文字
	 */
	/**受取人情報を更新する
	 * @param houseName:受取人氏名 String
	 * @param houseTel:受取人電話番号 String
	 * @param houseAddr:受取人住所 String
	 */
	public void setHouseInfo(String houseName,String houseTel,String houseAddr){
		//フィールドを更新する
		this.houseName = houseName;
		this.houseTel =  houseTel;
		this.houseAddr = houseAddr;
	}

	/**依頼人の名前を返す
	 * @return clientName:依頼人氏名 String
	 */
	public String getClientName(){
		return this.clientName;
	}
	
	/**依頼人の電話番号を返す 
	 * @return clientTel:依頼人電話番号 String
	 */
	public String getClientTel(){
		return this.clientTel;
	}
	
	/**依頼人の住所を返す
	 * @return clientAddr:依頼人住所 String
	 */
	public String getClientAddr(){
		return this.clientAddr;
	}
	
	/**受取人の氏名を返す
	 * @return houseName:受取人氏名 String
	 */
	public String getHouseName(){
		return this.houseName;
	}
	
	/**受取人の電話番号を返す
	 * @return houseTel:受取人電話番号 String
	 */
	public String getHouseTel(){
		return this.houseTel;
	}
	
	/**受取人の住所を返す
	 * @return houseAddr:受取人住所 String
	 */
	public String getHouseAddr(){
		return this.houseAddr;
	}
	
	/**依頼人情報を返す
	 * @return clientName|clientTel|clientAddr:依頼人氏名|依頼人電話番号|依頼人住所 String
	 */
	public String getClientInfo() {
		return this.clientName+"|"+this.clientTel+"|"+this.clientAddr;
	}
	
	/**受取人情報を返す
	 * @return houseName|houseTel|houseAddr:受取人氏名|受取人電話番号|受取人住所 String
	 */
	public String getHouseInfo(){
		return this.houseName+"|"+this.houseTel+"|"+this.houseAddr;
	}

}
