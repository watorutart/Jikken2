package hq;
/**
 * 保存命令:Enum
 * @author 秋山和哉
 *23行
 */

	enum Save{//加工保存命令
		//saveOdrNum,//命令番号を加工して記録命令
		deliReciTimeClientInfo,//+ 発送時間と受付時間と依頼情報を加工して記録命令 : Integer,//makeFragile
		relayTime,//+ 中継所到着時間を加工して記録命令 : Integer
	//	deliReceptionTime,//+ 発送時間と受付時間を加工して記録命令 : Integer
		startDeli,//+ 配達開始時間を加工して記録命令 : Integer
	//	saveObs,//+ 障害状況を加工して記録命令 : Integer
		deliCompReciTime,//+ 配達完了時間と受取時間を加工して記録命令 : Integer
		//◆◆以下追加した：加工保存命令
		setFailedPassing,//中継所受け渡し失敗に更新命令
		setAbsent,
		setWrgHouse


	};
