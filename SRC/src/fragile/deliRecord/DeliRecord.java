package fragile.deliRecord;

import java.util.Calendar;

/**
 * 配達記録を表すクラス.パッケージ外からの直接操作はしないでください。
 * @author bp16013 上田達也
 */
public class DeliRecord {

	/**
	 * 配達状況
	 */
	private DeliStats deliStats = null; //配達状況

	/**
	 * 障害状況
	 */
	private ObsStats obsStats = null; //障害状況

	/**
	 * 中継所到着時間
	 */
	private Calendar relayArriveTime = Calendar.getInstance(); //中継所到着時間

	/**
	 * 受付時間
	 */
	private Calendar receptionTime = Calendar.getInstance(); //受付時間

	/**
	 * 受取時間
	 */
	private Calendar receiveTime = Calendar.getInstance(); //受取時間

	/**
	 * 発送時間
	 */
	private Calendar sendTime = Calendar.getInstance(); //発送時間

	/**
	 * 配達完了時間
	 */
	private Calendar deliFinishTime = Calendar.getInstance(); //配達完了時間

	/**
	 * 配達開始時間
	 */
	private Calendar deliStartTime = Calendar.getInstance(); //配達開始時間

	////////////////////////////////////setter//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**配達状況を更新する
	 * @param deliStats DeliStats
	 */
	public void setDeliStats(DeliStats deliStats) {
		this.deliStats = deliStats;
	}

	/**障害状況を更新する
	 * @param obsStats ObsStats
	 */
	public void setObsStats(ObsStats obsStats) {
		this.obsStats = obsStats;
	}

	/**
	 * 時間を更新する
	 * @param setTime:更新するフィールド名 String
	 * @param time:更新する値 String
	 * @throws IllegalArgumentExcetion:setTimeが存在しない場合にthrowされます
	 */
	public void saveTime(String setTime, String time) throws IllegalArgumentException {
		//指定されたフィールド名でフィールドを更新する
		switch (setTime) {
		case "relayArriveTime":
			this.setTime(relayArriveTime, time);
			break;
		case "receptionTime":
			this.setTime(receptionTime, time);
			break;
		case "receiveTime":
			this.setTime(receiveTime, time);
			break;
		case "sendTime":
			this.setTime(sendTime, time);
			break;
		case "deliFinishTime":
			this.setTime(deliFinishTime, time);
			break;
		case "deliStartTime":
			this.setTime(deliStartTime, time);
			break;
		default:
			//フィールド名が存在しない場合
			throw new IllegalArgumentException("指定された時間フィールドが存在しません");
		}
	}

	/**
	 * 時間を更新する
	 * @param setTime:更新するフィールド名 String
	 * @param time:更新する値 Calendar
	 * @throws IllegalArgumentExcetion:フィールドが存在しない場合にthrowされます
	 */
	public void saveTime(String setTime, Calendar time) throws IllegalArgumentException {
		//指定されたフィールド名でフィールドを更新する
		switch (setTime) {
		case "relayArriveTime":
			this.relayArriveTime = time;
			break;
		case "receptionTime":
			this.receptionTime = time;
			break;
		case "receiveTime":
			this.receiveTime = time;
			break;
		case "sendTime":
			this.sendTime = time;
			break;
		case "deliFinishTime":
			this.deliFinishTime = time;
			break;
		case "deliStartTime":
			this.deliStartTime = time;
			break;
		default:
			//フィールド名が存在しない場合
			throw new IllegalArgumentException("指定された時間フィールドが存在しません");
		}
	}

	/**指定されたフィールドの時間を更新するメソッド
	 * @param c:更新するフィールド Calendar
	 * @param time:年月日時分の情報 String
	 * @throws IllegalArgumentExcetion:フィールドが存在しない場合にthrowされます
	 */
	private void setTime(Calendar c, String time) throws IllegalArgumentException {
		String[] date;
		//文字列を区切り文字で分割
		date = time.split("\\|", 0);
		if (date.length != 5) {
			throw new IllegalArgumentException("時間のフォーマットが不適切です");
		}
		//指定されたフィールドに時間を格納
		c.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]),
				Integer.parseInt(date[3]), Integer.parseInt(date[4]));
	}

	////////////////////////////////////getter//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 配達状況を取得する
	 * @return DeliStats
	 */
	public DeliStats getDeliStats() {
		return this.deliStats;
	}

	/**
	 * 障害状況を取得する
	 * @return ObsStats
	 */
	public ObsStats getObsStats() {
		return this.obsStats;
	}

	/**
	 * 配達時間をカレンダー型で取得する
	 * @param getTime:取得したい時間のフィールド名 String
	 * @return Calendar
	 * @throws IllegalArgumentException:フィールドが存在しない場合にthrowされます
	 */
	public Calendar getCalTime(String getTime) throws IllegalArgumentException {
		//指定されたフィールド名の時間を返す
		switch (getTime) {
		case "relayArriveTime":
			return this.relayArriveTime;
		case "receptionTime":
			return this.receptionTime;
		case "receiveTime":
			return this.receiveTime;
		case "sendTime":
			return this.sendTime;
		case "deliFinishTime":
			return this.deliFinishTime;
		case "deliStartTime":
			return this.deliStartTime;
		default:
			//フィールド名が存在しない場合
			throw new IllegalArgumentException("指定された時間フィールドが存在しません");
		}
	}
	/**
	 * 配達時間をString型で取得する
	 * @param getTime:取得したい時間のフィールド名 String
	 * @return String
	 * @throws IllegalArgumentException:フィールドが存在しない場合にthrowされます
	 */
	public String getStrTime(String getTime) throws IllegalArgumentException {
		switch (getTime) {
		case "relayArriveTime":
			return this.makeTimeStr(this.relayArriveTime);
		case "receptionTime":
			return this.makeTimeStr(this.receptionTime);
		case "receiveTime":
			return this.makeTimeStr(this.receiveTime);
		case "sendTime":
			return this.makeTimeStr(this.sendTime);
		case "deliFinishTime":
			return this.makeTimeStr(this.deliFinishTime);
		case "deliStartTime":
			return this.makeTimeStr(this.deliStartTime);
		default:
			//フィールド名が存在しない場合
			throw new IllegalArgumentException("指定された時間フィールドが存在しません");
		}
	}

	/**
	 * 文字列型の時間情報を生成する
	 * @param time Calendar
	 * @return String
	 */
	private String makeTimeStr(Calendar time) {

		String str = "";
		str = str.concat(String.valueOf(time.get(Calendar.YEAR)));

		if (time.get(Calendar.MONTH) < 9) {
			str = str.concat("0");
		}
		str = str.concat(String.valueOf(time.get(Calendar.MONTH) + 1));
		if (time.get(Calendar.DATE) < 10) {
			str = str.concat("0");
		}
		str = str.concat(String.valueOf(time.get(Calendar.DATE)));
		if (time.get(Calendar.HOUR_OF_DAY) < 10) {
			str = str.concat("0");
		}
		str = str.concat(String.valueOf(time.get(Calendar.HOUR_OF_DAY)));
		if (time.get(Calendar.MINUTE) < 10) {
			str = str.concat("0");
		}
		str = str.concat(String.valueOf(time.get(Calendar.MINUTE)));
		return str;
	}
}
