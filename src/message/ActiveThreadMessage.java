package message;

//激活线程的信息类
public class ActiveThreadMessage {
	private static final ActiveThreadMessage instance = new ActiveThreadMessage();
	
	private ActiveThreadMessage() {}
	
	public static ActiveThreadMessage getInstance() {
		return ActiveThreadMessage.instance;
	}
}
