package relay;

import fragile.Fragile;
import telecommunication.code.Relay_Deliver;
import telecommunication.code.Relay_HQ;

/**
 * @author Takumi Suzuki
 *
 */
public class InfoEditor {
	/**
	 * 宣言に含まれるとおりのenum定数の名前を返します。
	 * 
	 * @param order
	 *            - 命令番号
	 * @return - このenum定数の名前
	 */
	String adjustInfo(Relay_Deliver order) {
		return order.toString();
	}

	/**
	 * 宣言に含まれるとおりのenum定数とlongの値を'|'で連結して返します。
	 * 
	 * @param order
	 *            - 命令番号
	 * @param num
	 *            - 荷物番号
	 * @return - 要素を'|'で連結した文字列
	 */
	String adjustInfo(Relay_HQ order, Long num) {
		String[] tmp = { order.toString(), num.toString() };
		return combineInfo(tmp);
	}

	/**
	 * 宣言に含まれるとおりのenum定数とLongの値、Integerの値を'|'で連結して返します。
	 * 
	 * @param order
	 *            - 命令番号
	 * @param num
	 *            - 荷物番号
	 * @param time
	 *            - 計測時間
	 * @return - 要素を"|"で連結した文字列
	 */
	String adjustInfo(Relay_HQ order, Long num, Integer time) {
		String[] tmp = { order.toString(), num.toString(), time.toString() };
		return combineInfo(tmp);
	}

	/**
	 * 宣言に含まれるとおりのenum定数とFragileの属性を'|'で連結して返します。
	 * 
	 * @param order
	 *            - 命令番号
	 * @param send
	 *            - 荷物
	 * @return - 配達ロボットに送信する荷物の情報を'|'で連結した文字列
	 */
	String adjustInfo(Relay_Deliver order, Fragile send) {
		String[] tmpC = send.getClientInfo();
		String[] tmpH = send.getHouseInfo();
		String num = String.valueOf(send.getFrglNum());
		String[] tmp = { order.toString(), num, tmpC[0], tmpC[2], tmpH[0], tmpH[2] };
		return combineInfo(tmp);
	}

	/**
	 * 配列内の要素を'|'で連結した文字列を返します。
	 * 
	 * @param strings
	 *            文字列配列
	 * @return - '|'で連結した文字列
	 */
	private String combineInfo(String[] strings) {
		String combined = "";
		int tail = strings.length - 1;
		for (int i = 0; i < tail; i++)
			combined += strings[i] + "|";
		return combined + strings[tail];
	}

}
