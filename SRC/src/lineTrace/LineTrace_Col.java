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

public class LineTrace_Col {
	/**
	 * LineTraceクラスのインスタンス
	 */
	LineTrace lt = new LineTrace(); //ライントレースクラスのインスタンスを作成
	/**
	 * ターゲット値。搬送路に対し外側にあるモーターに適用されます。
	 */
	private static final float outTargetVal = 0.2f; //搬送路に対し外側にあるモーターのターゲット値
	/**
	 * ターゲット値。搬送路に対し内側にあるモーターに適用されます。
	 */
	private static final float inTargetVal = 0.4f; //搬送路に対し内側にあるモーターのターゲット値

	/**
	 * 衝突回避地点まで移動するメソッド
	 */
	public void ToCollision(){
		lt.resetPid();
		lt.distLineTrace(500, inTargetVal, outTargetVal, 170f, 200f, 1f);
		float color = lt.getRoadColor(15);
		
		//colorが0.1～0.4なら停止
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(50, inTargetVal, outTargetVal, 170f, 200f, 1f);
			color = lt.getRoadColor(15);
		}
		//最初の灰色へ到達
		Delay.msDelay(100);
		lt.resetPid();
		lt.distLineTrace(1200, inTargetVal, outTargetVal, 160f, 200f, 1f);
		color = lt.getRoadColor(15);
		//colorが0.1なら停止
		while(!(color <= 0.1)){
			lt.distLineTrace(20, inTargetVal, outTargetVal, 160f, 200f, 1f);
			color = lt.getRoadColor(15);
		}
		//衝突回避地点へ到達
	}

	/**
	 * 衝突回避地点から中継所まで移動するメソッド
	 */
	public void ToRelay(){
		float color;
		lt.distStraight(100,60);
		color = lt.getRoadColor(-15);
		while(!(color <= 0.1)){
			lt.rotateDeg(-40);
			color = lt.getRoadColor(-15);
		}
		Delay.msDelay(100);
		lt.resetPid();

		lt.distLineTrace(350, outTargetVal, inTargetVal, 200f, 200f, 0.7f);

		color = lt.getRoadColor(-15);
		//colorが0.1~0.3なら停止
		while(!(color <= 0.3 && color >= 0.1)){
			lt.distLineTrace(50, outTargetVal, inTargetVal, 200f, 200f, 0.7f);
			color = lt.getRoadColor(-15);
		}
		//中継所へ到達
	}

	/**
	 * 中継所から衝突回避地点を脱出するまで移動するメソッド
	 */
	public void ExitCollision(){
		float color;
		lt.rotateDeg(330);
		color = lt.getRoadColor(15);
		//colorが0.4以下で回転停止
		while(!(color <= 0.4)){
			lt.rotateDeg(10);
			color = lt.getRoadColor(15);
		}
		Delay.msDelay(100);
		lt.resetPid();
		lt.distLineTrace(650, inTargetVal, outTargetVal, 200f, 200f, 0.7f);
		//衝突回避地点から脱出
	}

	/**
	 * 衝突回避地点を脱出してから受付所まで移動するメソッド
	 */
	public void ToRecep(){
		float color;
		lt.getRoadColor(10);
		Delay.msDelay(100);
		lt.resetPid();
		lt.distLineTrace(3850,inTargetVal, outTargetVal,240f,220f,0.9f);
		//lt.stopMotor();
		Delay.msDelay(100);
		lt.rotateDeg(10);
		color = lt.getRoadColor(15);
		Delay.msDelay(100);
		lt.resetPid();
		//colorが0.1~0.4で停止
		while(!(color <= 0.4 && color >= 0.1)){
			lt.distLineTrace(50, inTargetVal, outTargetVal, 180f,200f,0.6f);
			//lt.resetPid();
			color = lt.getRoadColor(15);
		}
		Delay.msDelay(100);
		lt.resetPid();
		lt.distLineTrace(110,inTargetVal, outTargetVal,180f,200f,0.6f);
		lt.rotateDeg(170);
		lt.distStraight(50,20);
		lt.rotateDeg(170);
		color = lt.getRoadColor(15);
		//colorが0.4以下で回転停止
		while(!(color <= 0.4)){
			lt.rotateDeg(10);
			color = lt.getRoadColor(15);
		}
		//受付所へ到達
	}
}
;
