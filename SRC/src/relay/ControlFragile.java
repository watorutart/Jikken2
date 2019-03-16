package relay;

import java.util.HashMap;
import java.util.LinkedList;

import fragile.ClientInfo;
import fragile.Fragile;
import fragile.deliRecord.DeliStats;
import fragile.deliRecord.ObsStats;

/**
 * @author Takumi Suzuki
 *
 */
public class ControlFragile {

	private LinkedList<Long> nList = new LinkedList<Long>();
	private FragileList fList = new FragileList();
	private HashMap<Long, Integer> hMap = new HashMap<>();
	private ControlFragileState cfs = new ControlFragileState();

	/**
	 * 荷物番号を荷物番号リストに格納して、荷物の状態を更新します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	void saveFragileNum(Long num) {
		nList.add(num);
		cfs.setState(num, FragileState.NEED_SEND_ARRIVED);
	}

	/**
	 * 荷物番号を荷物番号リストから削除して、荷物の状態を更新します。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	void setNeedInfo(Long num) {
		nList.remove(num);
		cfs.setState(num, FragileState.NEEDINFO);
	}

	/**
	 * 指定された値をもとに、荷物を作成してリストに追加します。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @param cname
	 *            - 依頼人氏名
	 * @param caddr
	 *            - 依頼人住所
	 * @param hname
	 *            - 受取人氏名
	 * @param haddr
	 *            - 受取人住所
	 */
	void saveFragileInfo(Long num, String cname, String caddr, String hname, String haddr) {
		ClientInfo cInfo = new ClientInfo();
		cInfo.setClientInfo(cname, null, caddr);
		cInfo.setHouseInfo(hname, null, haddr);
		nList.poll();
		fList.addFragile(new Fragile(cInfo, num, DeliStats.awaiting, ObsStats.none));
		cfs.setState(num, FragileState.DELIVERABLE);
	}

	/**
	 * 指定された番号の荷物の状態を「配達中」に書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	void setOnDeliver(Long num) {
		fList.setStats(num, DeliStats.delivering);
		cfs.setState(num, FragileState.ONDELIVER);
	}

	/**
	 * 指定された番号の荷物の状態を「配達中（報告済）」に書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	void setReportedPassing(Long num) {
		cfs.setState(num, FragileState.ONDELIVER_REPORTED);
	}

	/**
	 * 指定された番号の荷物の状態を「配達失敗」に書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @param oStats
	 *            - 障害状況
	 */
	void setFailed(Long num, ObsStats oStats) {
		fList.setStats(num, DeliStats.delivered, oStats);
		cfs.setState(num, FragileState.RETURNED);
	}

	/**
	 * 指定された番号の荷物の状態を「配達済」に書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @param minTime
	 *            - 計測時間（分）
	 */
	void setCompleted(Long num, Integer minTime) {
		fList.setStats(num, DeliStats.delivered);
		hMap.put(num, minTime);
		cfs.setState(num, FragileState.RETURNED);
	}

	/**
	 * 指定された番号の荷物の状態を「配達可能」に書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	void reWaiting(Long num) {
		fList.setStats(num, DeliStats.awaiting, ObsStats.none);
		cfs.setState(num, FragileState.DELIVERABLE);
	}

	/**
	 * 指定された番号の荷物の状態を「報告済」に書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	void setReported(Long num) {
		cfs.setState(num, FragileState.REPORTED);
		distribute(num);
	}

	/**
	 * 荷物番号リストの先頭（最初の要素）を取得しますが、削除はしません。
	 * 
	 * @return - 荷物番号
	 */
	Long getNum() {
		return nList.peek();
	}

	/**
	 * 「依頼情報待ち」の荷物の番号を返します。
	 * 
	 * @return - 荷物番号
	 */
	Long getNeedInfo() {
		return cfs.findFragile(FragileState.NEEDINFO);
	}

	/**
	 * 「依頼情報待ち」の荷物がある場合はtrueを返します。
	 * 
	 * @return - 有無
	 */
	Boolean hasNeedInfo() {
		if (getNeedInfo() == null)
			return false;
		else
			return true;
	}

	/**
	 * 配達可能な荷物がある場合は、荷物番号を返します。
	 * 
	 * @return - 荷物番号
	 */
	Long getDeliverable() {
		return cfs.findFragile(FragileState.DELIVERABLE);
	}

	/**
	 * 配達可能な荷物がある場合は、荷物を返します。
	 * 
	 * @return - 荷物
	 */
	Fragile getDeliverableFragile() {
		Long num = getDeliverable();
		return fList.getFragile(num);
	}

	/**
	 * 配達可能な荷物がある場合はtrueを返します。
	 * 
	 * @return - 有無
	 */
	Boolean hasDeliverableFragile() {
		return cfs.hasDeliverableFragile();
	}

	/**
	 * 配達中の荷物がある場合は、荷物番号を返します。
	 * 
	 * @return - 荷物番号
	 */
	Long getOnDeliver() {
		return cfs.findFragile(FragileState.ONDELIVER);
	}

	/**
	 * 中継所に戻ってきた荷物がある場合は、荷物番号を返します。
	 * 
	 * @return - 荷物番号
	 */
	Long getReturned() {
		return cfs.findFragile(FragileState.RETURNED);
	}

	/**
	 * 中継所到着を本部に報告済みである荷物がある場合は、荷物番号を返します。
	 * 
	 * @return - 荷物番号
	 */
	Long getReported() {
		return cfs.findFragile(FragileState.REPORTED);
	}

	/**
	 * 指定された番号の荷物の障害状況を返します。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @return - 障害状況
	 */
	ObsStats getObs(Long num) {
		return fList.getObs(num);
	}

	/**
	 * 指定された番号の荷物の配達時間を返します。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @return - 配達時間（分）
	 */
	Integer getDeliTime(Long num) {
		return hMap.get(num);
	}

	/**
	 * 中継所に戻ってきた荷物の障害状況を参照して、状態を書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 */
	private void distribute(Long num) {
		if (num != null) {
			ObsStats oStats = getObs(num);
			if (oStats == ObsStats.absent) {
				cfs.setState(num, FragileState.DELIVERABLE);
			} else if (oStats == ObsStats.wrongHouse) {
				cfs.setState(num, FragileState.REPORTED);
			}
		}
	}

	/**
	 * 荷物番号とその状態を画面表示します。
	 */
	void printList() {
		cfs.printState();
	}
}
