package message;

//�����̵߳���Ϣ��
public class ActiveThreadMessage {
	private static final ActiveThreadMessage instance = new ActiveThreadMessage();
	
	private ActiveThreadMessage() {}
	
	public static ActiveThreadMessage getInstance() {
		return ActiveThreadMessage.instance;
	}
}
