package telecommunication.code;

/**
 * RelayとDeliver間の通信プロトコル(命令番号)
 * @author 三森
 */
public enum Relay_Deliver {
	sendLock,syncLock,sendHasFrgl,syncHasFrgl,syncFrglInfo,setLockFalse,reportDeliResult,reportDeliFail,protocol,noFrgl
}
