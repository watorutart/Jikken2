package collector;

public enum Adjustment {
	sendLock, // 共有変数の値を送信する命令
	setLockFalse, // 共有変数を0にする命令
	frglNumDeliComp, // 荷物番号と中継所引き渡し完了の加工命令
	adjustFrglNum, // 荷物番号の加工命令

}
