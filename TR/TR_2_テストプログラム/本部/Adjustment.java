package hq;

/**
 * 加工命令:Enum
 * @author 秋山和哉
 * 12行
 */

	enum Adjustment {//Enum : 加工命令
		sendObs,//障害状況の送信命令
		obsClientInfo//障害状況と依頼情報の加工命令
	};