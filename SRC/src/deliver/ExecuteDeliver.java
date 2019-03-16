package deliver;

/**
 * 配達ロボット実行クラス ExecuteDeliver Class
 * @author bp16110 渡辺亮一 Ryoichi Watanabe
 */
class ExecuteDeliver{
	/**
	 * 配達ロボットのサブシステムを実行するためのメインメソッド.
	 * EV3で実行するには, このクラスを用いること.
	 * @param args 外部入力.  本システムでは特に意味はなく, 入力に何かを書き込んでも動作に変化はない.
	 */
	public static void main(String[] args){
		Deliver EV3=new Deliver();
		
		EV3.control();
	}
}