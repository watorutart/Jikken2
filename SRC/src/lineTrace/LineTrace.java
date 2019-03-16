package lineTrace;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

public class LineTrace {
	private final static double wheelR = 28; //タイヤ半径:28mm
	/*
	private EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A); //左タイヤ
	private EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D); //右タイヤ
	*/
	private EV3ColorSensor cSensor = new EV3ColorSensor(SensorPort.S4); //カラーセンサー
	private EV3GyroSensor gSensor = new EV3GyroSensor(SensorPort.S1); //ジャイロセンサー
	private SensorMode light = cSensor.getRedMode(); //カラーセンサーのモード
	private SensorMode gyro = gSensor.getMode(1); //ジャイロセンサーのモード
	float[] cSample = new float[light.sampleSize()]; //カラーセンサーの値の格納用配列
	float[] gSample = new float[gyro.sampleSize()]; //ジャイロセンサーの値の格納用配列
	private PidControl pid = new PidControl(); //pidコントロールのクラス 左モーター用

	public LineTrace(){
		resetPid();
	}
	/*搬送路に沿って一定距離ライントレースする
	  targetDis:走行距離
	  borderLeft:左タイヤの目標値
	  borderRight:右タイヤの目標値
	  Kp:p制御の定数
	  Ki:i制御の定数
	  Kd:d制御の定数
	*/
	public void distLineTrace(double targetDis,float borderLeft,float borderRight,float Kp,float Ki,float Kd){
		double moveDis = 0;
		double motorPos = 0;
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A); //左タイヤ
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D); //右タイヤ
		while(moveDis < targetDis){
			motorPos = leftMotor.getPosition();
			light.fetchSample(cSample, 0);
			leftMotor.setSpeed((int)pid.pid_sample(cSample[0],borderLeft,Kp,Ki,Kd));
			rightMotor.setSpeed((int)pid.pid_sample(cSample[0],borderRight,Kp,Ki,Kd));
			if(borderLeft > borderRight){
				leftMotor.forward();
				rightMotor.forward();
			}else{
				rightMotor.forward();
				leftMotor.forward();
			}
			moveDis += calcDis(leftMotor.getPosition() - motorPos);
		}
		//this.stopMotor();
		this.stopMotor(leftMotor,rightMotor);
		Delay.msDelay(1000);
		leftMotor.close();
		rightMotor.close();
		Delay.msDelay(1000);
	}

	/*指定された早さで指定された距離だけ直進する
	  speed:早さ
	  targetDis:走行距離
	 */
	public void distStraight(int speed,double targetDis){
		double moveDis = 0;
		double motorPos = 0;
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A); //左タイヤ
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D); //右タイヤ
		while(moveDis < targetDis){
			motorPos = leftMotor.getPosition();
			leftMotor.setSpeed(speed);
			rightMotor.setSpeed(speed);
			leftMotor.forward();
			rightMotor.forward();
			moveDis += calcDis(leftMotor.getPosition() - motorPos);
		}
		//this.stopMotor();
		this.stopMotor(leftMotor,rightMotor);
		Delay.msDelay(1000);
		leftMotor.close();
		rightMotor.close();
		Delay.msDelay(1000);
		this.resetPid();
	}
	/*
	public void stopMotor(){
		leftMotor.stop(true);
		rightMotor.stop();
	}
	*/

	public void stopMotor(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor){
		leftMotor.stop(true);
		rightMotor.stop();
	}
	/*指定された角度だけ回転する
	  　正:左回り 負:右回り
	 */
	public void rotateDeg(int deg){
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A); //左タイヤ
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D); //右タイヤ
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.rotate(-deg,true);
		rightMotor.rotate(deg);
		Delay.msDelay(1000);
		leftMotor.close();
		rightMotor.close();
		Delay.msDelay(1000);
		this.resetPid();
	}

	//pidの変数をリセットする
	public void resetPid(){
		light.fetchSample(cSample, 0);
		pid.reset(0);
	}

	/*現在地の搬送路の色を返す
	  　搬送路に向かって角度を変える
	  deg:回転する角度(正:左回り,負:右回り)
	*/
	public float getRoadColor(int deg){
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A); //左タイヤ
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D); //右タイヤ
		Delay.msDelay(100);
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.rotate(-deg,true);
		rightMotor.rotate(deg);
		light.fetchSample(cSample, 0);
		leftMotor.rotate(deg,true);
		rightMotor.rotate(-deg);
		Delay.msDelay(1000);
		leftMotor.close();
		rightMotor.close();
		Delay.msDelay(1000);
		return cSample[0];
	}

	//現在の機体の角度を返す
	public float getGyro(){
		gyro.fetchSample(gSample, 0);
		return gSample[0];
	}

	//機体の角度をリセットする
	public void resetGyro(){
		gSensor.reset();
	}

	//Close処理
	public void close(){
		this.cSensor.close();
		this.gSensor.close();
	}

	/*移動距離を角度から測定する
	  deg:動いた角度
	*/
	private static double calcDis(double deg){
		double distance; //移動距離(mm)
		distance = 2*Math.PI*wheelR*(deg/360);
		return distance;
	}

}
