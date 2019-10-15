package lineTrace;
import lejos.utility.Delay;

/**
 * あくまでサンプルですが、メソッド自体はこれで動きます。
 * ライントレースクラスのインスタンスを作成する位置についてはお任せしますが、各メソッド内でnewすると正常に動かないかもしれません。
 * ターゲット値は変更しないでください。
 * このクラスをそのまま使っても一応動くとは思います。
 * プログラムの最後で必ずライントレースクラスのcloseメソッドを呼んでください。(呼ばないとそのうちバグが出ます)
 * closeメソッドは、例えばロボットを何度も往復させる場合、1往復ごとなどに呼んでしまうとエラーが出るので、必ず最後に呼んでください。
 * @author bp16013 上田達也
 */

public class LineTrace_Deli {
	/**
	 * LineTraceクラスのインスタンス
	 */
	LineTrace lt = new LineTrace(); //ライントレースクラスのインスタンス
	/**
	 * ターゲット値。搬送路に対し外側にあるモーターに適用されます。
	 */
	private static final float outTargetVal = 0.2f; //搬送路に対し外側にあるモーターのターゲット値
	/**
	 * ターゲット値。搬送路に対し内側にあるモーターに適用されます。
	 */
	private static final float inTargetVal = 0.4f; //搬送路に対し内側にあるモーターのターゲット値
	/**
	 * 搬送路の色取得の際回転する角度を指定します。
	 */
	private static final int rotateDeg = 20; //搬送路の色を取得する際回転する角度

	/**
	 * 待機所から衝突回避地点へ移動するメソッド
	 */
	public void WaitingToCollision(){
		lt.resetPid();
		lt.distLineTrace(600,inTargetVal,outTargetVal,100f,150f,1.1f);
		float color = lt.getRoadColor(rotateDeg);
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(20, inTargetVal, outTargetVal, 100f, 150f, 0.9f);
			color = lt.getRoadColor(rotateDeg);
		}
		lt.distStraight(100,50);
		//衝突回避地点へ到達
	}

	/**
	 * 衝突回避地点から中継所へ移動するメソッド
	 */
	public void ToRelay(){
		float color;
		lt.distLineTrace(300, inTargetVal,outTargetVal,100f,150f,1.1f);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color <= 0.1)){
			lt.distLineTrace(20,inTargetVal,outTargetVal,100f,150f,0.9f);
			color = lt.getRoadColor(-rotateDeg);
		}
		lt.distStraight(100,40);
		lt.rotateDeg(-170);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color >= 0.5)){
			lt.rotateDeg(-10);
			color = lt.getRoadColor(-rotateDeg);
		}
		lt.resetPid();
		lt.distLineTrace(240,inTargetVal,outTargetVal,230f,200f,0.9f);
		color = lt.getRoadColor(rotateDeg);
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(20, inTargetVal, outTargetVal, 230f, 200f, 0.7f);
			color = lt.getRoadColor(rotateDeg);
		}
		lt.distLineTrace(150, inTargetVal, outTargetVal, 230f, 200f, 0.9f);
		//中継所へ到達
	}

	/**
	 * 中継所から衝突回避地点へ移動するメソッド
	 */
	public void RelayToCollision(){
		float color;
		//180度回転
		lt.rotateDeg(-350);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color <= 0.4)){
			lt.rotateDeg(-10);
			color = lt.getRoadColor(-rotateDeg);
		}
		
		lt.resetPid();
		lt.distLineTrace(490,outTargetVal,inTargetVal,230f, 200f, 0.9f);
		color = lt.getRoadColor(rotateDeg);
		//黒or灰色の線を検出するまでループ
		while(!(color <= 0.3)){
			lt.distLineTrace(10,outTargetVal,inTargetVal,100f, 150f, 0.7f);
			color = lt.getRoadColor(rotateDeg);
		}
		lt.distStraight(100, 40);
		lt.rotateDeg(170);
		color = lt.getRoadColor(rotateDeg);
		//白を検出するまでループ
		while(!(color >= 0.5)){
			lt.rotateDeg(10);
			color = lt.getRoadColor(rotateDeg);
		}
		lt.resetPid();
		lt.distLineTrace(200, outTargetVal,inTargetVal,190f,150f,1.1f);
		color = lt.getRoadColor(-rotateDeg);
		//灰色の線を検出するまでループ
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(20,outTargetVal,inTargetVal,150f,150f,0.9f);
			color = lt.getRoadColor(-rotateDeg);
		}
		//衝突回避地点から脱出
	}

	/**受取人宅へ移動するメソッド
	 * @param Y:列方向住所 int
	 * @param X:行方向住所 int
	 */
	public void ToHouse(int Y,int X){
		float color;
		int distN = 250; //横方向灰色から灰色までの距離
		int distV = 250; //縦方向灰色から灰色までの距離
		lt.distStraight(100,70);
		lt.rotateDeg(170);
		color = lt.getRoadColor(rotateDeg);
		while(!(color >= 0.4)){
			lt.rotateDeg(10);
			color = lt.getRoadColor(rotateDeg);
		}
		lt.resetPid();
		lt.distLineTrace(300, outTargetVal, inTargetVal, 220f, 220f, 0.7f);
		lt.distLineTrace(1000, outTargetVal, inTargetVal, 200f, 150f, 1.2f);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(20,outTargetVal,inTargetVal,200f, 150f, 0.9f);
			color = lt.getRoadColor(-rotateDeg);
		}
		lt.distStraight(100, 70);
		lt.rotateDeg(170);
		color = lt.getRoadColor(rotateDeg);
		while(!(color >= 0.4)){
			lt.rotateDeg(10);
			color = lt.getRoadColor(rotateDeg);
		}
		//住宅エリアへ到達

		for(int i = 0; i < Y; i++){
			lt.resetPid();
			//lt.rotateDeg(-5);
			lt.distLineTrace(distV, outTargetVal, inTargetVal, 200f, 150f, 1.2f);
			color = lt.getRoadColor(-rotateDeg);
			while(!(color <= 0.4 && color >= 0.1)){
				lt.distLineTrace(30, outTargetVal, inTargetVal, 200f, 150f, 0.9f);
				color = lt.getRoadColor(-rotateDeg);
				//Delay.msDelay(200);
			}
			lt.distStraight(100,80);
		}
		lt.distStraight(100, 20);
		lt.rotateDeg(-170);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color <= 0.35)){
			//Delay.msDelay(200);
			lt.rotateDeg(-10);
			color = lt.getRoadColor(-rotateDeg);
		}
		for(int i = 3; i-X > 0; i--){
			lt.resetPid();
			//lt.rotateDeg(-5);
			lt.distLineTrace(distN, outTargetVal, inTargetVal, 200f, 150f, 1.2f);
			color = lt.getRoadColor(-rotateDeg);
			while(!(color <= 0.4 && color >= 0.1)){
				lt.distLineTrace(30, outTargetVal, inTargetVal, 200f, 150f, 0.9f);
				color = lt.getRoadColor(-rotateDeg);
			}
			lt.distStraight(100,80);
		}

		lt.resetPid();
		//lt.rotateDeg(-10);
		lt.distLineTrace(distV-100, outTargetVal, inTargetVal, 200f, 150f, 1.2f);

		//受取人宅へ到達
	}

	/**
	 * 受取人宅から衝突回避地点まで移動するメソッド
	 * @param Y:列方向住所 int
	 * @param X:行方向住所 int
	 */
	public void ReturnToRelay(int Y,int X){
		float color;
		int distN = 250; //横方向灰色から灰色までの距離
		int distV = 250; //縦方向灰色から灰色までの距離
		lt.rotateDeg(350);
		color = lt.getRoadColor(rotateDeg);
		while(!(color <= 0.35)){
			//Delay.msDelay(200);
			lt.rotateDeg(10);
			color = lt.getRoadColor(rotateDeg);
		}
		lt.resetPid();
		//lt.rotateDeg(5);
		lt.distLineTrace(distN-200, inTargetVal, outTargetVal, 200f, 100f, 1f);
		color = lt.getRoadColor(rotateDeg);
		while(!(color <= 0.35 && color >= 0.1)){
			lt.resetPid();
			lt.distLineTrace(30, inTargetVal, outTargetVal, 200f, 100f, 1f);
			//lt.stopMotor();
			color = lt.getRoadColor(rotateDeg);
			//Delay.msDelay(200);
		}
		lt.distStraight(100,80);
		for(int i = 3; i-X > 0; i--){
			lt.resetPid();
			//lt.rotateDeg(5);
			lt.distLineTrace(distN, inTargetVal, outTargetVal, 200f, 100f, 1f);
			color = lt.getRoadColor(rotateDeg);
			while(!(color <= 0.4 && color >= 0.1)){
				lt.distLineTrace(30, inTargetVal, outTargetVal, 200f, 100f, 1f);
				//lt.stopMotor();
				color = lt.getRoadColor(rotateDeg);
				//Delay.msDelay(200);
			}
			lt.distStraight(100,80);
		}
		//lt.stopMotor();
		Delay.msDelay(500);
		lt.distStraight(100,20);
		lt.rotateDeg(140);
		color = lt.getRoadColor(rotateDeg);
		while(!(color <= 0.35)){
			//Delay.msDelay(200);
			lt.rotateDeg(10);
			color = lt.getRoadColor(rotateDeg);
		}
		for(int i = 0; i < Y; i++){
			lt.resetPid();
			//lt.rotateDeg(5);
			lt.distLineTrace(distV, inTargetVal, outTargetVal, 200f, 100f, 1f);
			color = lt.getRoadColor(rotateDeg);
			while(!(color <= 0.4 && color >= 0.1)){
				//lt.resetPid();
				lt.distLineTrace(30, inTargetVal, outTargetVal, 200f, 100f, 1f);
				//lt.stopMotor();
				color = lt.getRoadColor(rotateDeg);
				//Delay.msDelay(200);
			}
			lt.distStraight(100,80);
		}
		lt.rotateDeg(-170);
		color = lt.getRoadColor(-rotateDeg);
		while(color <= 0.35){
			//Delay.msDelay(200);
			lt.rotateDeg(-10);
			color = lt.getRoadColor(-rotateDeg);
		}
		//住宅エリアから脱出

		Delay.msDelay(300);
		lt.resetPid();
		//lt.rotateDeg(10);
		lt.distLineTrace(1000,inTargetVal,outTargetVal,230f, 200f, 1f);
		//lt.rotateDeg(10);
		lt.distLineTrace(500, inTargetVal, outTargetVal, 250f, 220f, 0.7f);
		color = lt.getRoadColor(rotateDeg);
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(20, inTargetVal, outTargetVal, 180f, 180f, 0.7f);
			color = lt.getRoadColor(rotateDeg);
		}
		lt.distStraight(100,80);
		lt.rotateDeg(-170);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color >= 0.4)){
			lt.rotateDeg(-10);
			color = lt.getRoadColor(-rotateDeg);
		}
		//衝突回避地点へ到達
	}

	/**
	 * 衝突回避地点から待機所へ戻るメソッド
	 */
	public void ReturnToWaiting(){
		float color;
		//lt.rotateDeg(-10);
		lt.distLineTrace(700, outTargetVal, inTargetVal, 250f, 150f, 1.2f);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(20, outTargetVal, inTargetVal, 250f, 150f, 0.9f);
			color = lt.getRoadColor(-rotateDeg);
		}
		//lt.rotateDeg(-10);
		lt.distLineTrace(150, outTargetVal, inTargetVal, 200f, 150f, 0.7f);
		lt.rotateDeg(350);
		color = lt.getRoadColor(-rotateDeg);
		while(!(color <= 0.3)){
			lt.rotateDeg(10);
			color = lt.getRoadColor(-rotateDeg);
		}
		//待機所へ到達
	}
}
