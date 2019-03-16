package telecommunication;

/**
 * 通信クラスで使用するスレッドの状態(PC同士の通信以外の場合、「動く」クラスはマルチスレッドで処理が行われる。
 * そのスレッドの状態を表す)
 * @author 三森
 */
public enum ThreadState {
	Death,Run_send,Run_receive,Success,Fail
}
