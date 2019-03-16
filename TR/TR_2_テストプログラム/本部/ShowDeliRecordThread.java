package hq;



import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 配達情報を参照するThread
 * @author 秋山和哉
 * 72行
 */


public class ShowDeliRecordThread extends Thread {
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	HQMonitor HQmonitor = new HQMonitor();
	HQ hq = new HQ();
	@Override
	public void run() {
		while(true) {
			showDeliRecord();
		}
	}
	/**
	 * 配達情報を表示する
	 */
	private void showDeliRecord() {
		Boolean verify_Boolean;
		try {
			String tmp = null;
			if ((tmp = reader_ver2()) == null) {
				return;
			}
			int Person = Integer.parseInt(tmp);
			switch (Person) {
			case 0://依頼者が参照する。
				verify_Boolean = HQmonitor.verifyClient();
				if (verify_Boolean) {//認証に成功したら
					int junban = hq.searchFragile(HQmonitor.getfrglNum());
					HQmonitor.displayDeliInfo(junban);
					break;
				} else {//認証に失敗したら。
					System.out.println("認証に失敗しました。通信を再開します。");
					break;
				}
			case 1://システム管理者が参照する。
					//全ての配達記録、荷物番号を表示する処理
				HQmonitor.displayDeliIndex();
				break;
			/*case 2://参照せず通信を続行
				return;*/
			default:
				return;
			}
		} catch (Exception e) {
			System.out.println("BufferedReader, readLineメソッドのIOExceptionが発生しました。");
			return;
		}
	}

	public String reader_ver2() throws Exception {
		String message = "";//初期化
		message = reader.readLine();

		while(message.contentEquals("")){
			message = reader.readLine();
			}
		return message;
	}

}