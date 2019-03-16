package telecommunication.code;

/**
 * RelayとHQ間の通信プロトコル(命令番号)
 * @author 三森
 */
public enum Relay_HQ {
	getObs,syncObs,setRelayArrive,setFailedPassing,setStartDeli,setWrgHouse,setAbsent,reportDeliComp
}
