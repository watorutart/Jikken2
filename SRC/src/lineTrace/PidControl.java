package lineTrace;

/**
 * Pid制御用のクラスです。
 * @author bp16013 上田達也
 *
 */
public class PidControl {

	/**
	 * 計算時の単位時間
	 */
	public static final float DELTA_T = 0.001f;
	
	/**
	 * 偏差取得用
	 */
    float[] diff = new float[2];
    /**
     * 積分値
     */
    float integral;

    /**
     * @param sensor_val:センサー値　float
     * @param target_val:目標値 float
     * @param KP P制御係数:float
     * @param KI I制御係数:float
     * @param KD D制御係数:float
     * @return
     */
    public float pid_sample(float sensor_val, float target_val,float KP,float KI,float KD) {
        float p, i, d;
        diff[0] = diff[1];
        diff[1] = sensor_val - target_val; // 偏差を取得
        integral += (diff[1] + diff[0]) / 2.0  * DELTA_T;

        p = KP * diff[1];
        i = KI * integral;
        d = KD * (diff[1] - diff[0]) / DELTA_T;


        // 最大・最小値を制限
        return math_limit(p + i + d, -300, 300);
    }

    /**
     * 操作量の最大・最小値を制限するメソッド
     * @param val:計算値 float
     * @param min:最小値.計算値がこれを下回る場合にはこれを返します. float
     * @param max:最大値.計算値がこれを上回る場合にはこれを返します. float
     * @return
     */
    public float math_limit(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        }
        return val;
    }
    /**
     * 値の初期化
     * @param sample 今回は利用しません
     */
    public void reset(float sample){
    	diff[0] = 0;
    	diff[1] = 0;
    	integral = 0;
    }

}
