package relay;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Takumi Suzuki
 *
 */
public class ControlFragileState {
	HashMap<Long, FragileState> map = new HashMap<>();

	/**
	 * 指定された荷物番号の荷物の状態を書き換えます。
	 * 
	 * @param num
	 *            - 荷物番号
	 * @param state
	 *            - 荷物の状態
	 */
	void setState(Long num, FragileState state) {
		map.put(num, state);
	}

	/**
	 * 指定された状態の荷物を検索して、荷物番号を返します。
	 * 
	 * @param state
	 *            - 荷物の状態
	 * @return 荷物番号
	 */
	Long findFragile(FragileState state) {
		for (Entry<Long, FragileState> entry : map.entrySet()) {
			if (entry.getValue().equals(state))
				return entry.getKey();
		}
		return null;
	}

	/**
	 * 配達可能な荷物がある場合にtrueを返します。
	 * 
	 * @return 配達可能な荷物の有無
	 */
	Boolean hasDeliverableFragile() {
		if (findFragile(FragileState.DELIVERABLE) == null)
			return false;
		else
			return true;
	}

	/**
	 * 荷物番号とその状態を画面表示します。
	 */
	void printState() {
		for (Entry<Long, FragileState> entry : map.entrySet()) {
			System.out.print(entry.getKey() + ":");
			System.out.println(entry.getValue());
		}
	}
}
