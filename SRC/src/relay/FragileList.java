package relay;

import java.util.ArrayList;
import java.util.Iterator;

import fragile.Fragile;
import fragile.deliRecord.DeliStats;
import fragile.deliRecord.ObsStats;

/**
 * @author Takumi Suzuki
 *
 */
public class FragileList {
	private ArrayList<Fragile> fragiles = new ArrayList<Fragile>();

	/**
	 * 荷物リストの最後に、指定された要素を追加します。
	 * 
	 * @param fragile
	 *            - 荷物
	 */
	void addFragile(Fragile fragile) {
		fragiles.add(fragile);
	}

	/**
	 * 指定された荷物番号の荷物が格納されているインデックスを返します。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @return - 荷物が格納されているインデックス
	 */
	@SuppressWarnings("null")
	private int getIndex(Long num) {
		int i = 0;
		for (Iterator<Fragile> itr = fragiles.iterator(); itr.hasNext();) {
			Fragile fragile = itr.next();
			if (fragile.getFrglNum() == num) {
				return i;
			}
			i++;
		}
		return (Integer) null;
	}

	/**
	 * 指定された荷物番号の荷物を返します。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @return - 荷物
	 */
	Fragile getFragile(Long num) {
		try {
			int i = getIndex(num);
			return fragiles.get(i);
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}

	/**
	 * 指定された番号の荷物の障害状況を返します。
	 * 
	 * @param num
	 * @return - 障害状況
	 */
	ObsStats getObs(Long num) {
		return getFragile(num).getObsStats();
	}

	/**
	 * 指定された番号の荷物の配達状況を書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @param dStats
	 *            - 配達状況
	 */
	void setStats(Long num, DeliStats dStats) {
		int i = getIndex(num);
		Fragile tmp = fragiles.get(i);
		tmp.setDeliStats(dStats);
		fragiles.set(i, tmp);
	}

	/**
	 * 指定された番号の荷物の障害状況を書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @param oStats
	 *            - 障害状況
	 */
	void setStats(Long num, ObsStats oStats) {
		int i = getIndex(num);
		Fragile tmp = fragiles.get(i);
		tmp.setObsStats(oStats);
		fragiles.set(i, tmp);
	}

	/**
	 * 指定された番号の荷物の配達状況と障害状況を書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @param dStats
	 *            - 配達状況
	 * @param obsStats
	 *            - 障害状況
	 */
	void setStats(Long num, DeliStats dStats, ObsStats obsStats) {
		int i = getIndex(num);
		Fragile tmp = fragiles.get(i);
		tmp.setDeliStats(dStats);
		tmp.setObsStats(obsStats);
		fragiles.set(i, tmp);
	}

	/**
	 * 荷物リストが空でない場合にtrueを返します。
	 * 
	 * @return - このリストに要素が含まれている場合はtrue
	 */
	Boolean isNotEmpty() {
		return true ^ fragiles.isEmpty();
	}

}
