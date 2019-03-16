package telecommunication;

/**
 * 通信相手(EV3のMacアドレス,EV3の名前はここに定義する)
 * @author 三森
 */
public enum Receiver {
	/*
	 * addressはEV3のMacアドレス
	 * ただし、bluecoveのアドレス指定では
	 * Macアドレスの前に"btspp://"と後に":1"を書き足す
	 * (例)Macアドレス : 00:16:53:4B:88:B9の場合
	 * 指定するアドレス = "btspp://0016534B88B9:1"
	 * */
	reception,
	collector("btspp://0016534DB4C6:1","Collector2"/*"00:16:53:4D:B4:C6"*/),
	relay("btspp://0016535DF5AD:1"),
	deliver("btspp://0016535DCAFE:1","Delivery2"/*"00:16:53:5D:CA:FE"*/),
	house,
	hq;

	private String MacAddress;
	private String EV3_name;
	private Receiver(){
		this.MacAddress=null;
		this.EV3_name=null;
	}
	private Receiver(String MacAddress){
		this();
		this.MacAddress=MacAddress;
	}
	private Receiver(String MacAddress,String EV3_name){
		this(MacAddress);
		this.EV3_name=EV3_name;
	}

	public String getMacAddress() throws RuntimeException{
		if(this.MacAddress==null)
			throw new RuntimeException("MacAddressは定義されていません");
		return this.MacAddress;
	}
	public String getEV3_name() throws RuntimeException{
		if(this.EV3_name==null)
			throw new RuntimeException("EV3の名前は定義されていません");
		return this.EV3_name;
	}
}
