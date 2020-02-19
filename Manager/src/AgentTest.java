import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AgentTest {
	
	public static class TestObject implements Serializable{
		public int ID;
		public int time;
		public char[] ip;
		
	}
	
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DatagramSocket socket = new DatagramSocket(9998);
		InetAddress IPAddress = InetAddress.getByName("localhost");
		
		//String s = "TestingSend";
		
		TestObject t = new TestObject();
		t.ID = 1;
		t.time = 2;
		
		Integer t1 = 97;
		Integer t2 = 98;
		Integer t3 = 10;
		Integer t4 = 11;
		Integer t5 = 12;
		t1.byteValue();
		t2.byteValue();
		char[] temp ={'1','3','4','6'}; 
		t.ip = temp;
		
		byte[] ad = new byte[5];
		ad[0] = t1.byteValue();
		ad[1] = t2.byteValue();
		ad[2] = t3.byteValue();
		ad[3] = t4.byteValue();
		ad[4] = t5.byteValue();
		
		
		DatagramPacket packetSend = new DatagramPacket(ad, ad.length,IPAddress,9997);
		socket.send(packetSend);
	//	ad[0] = new Integer(1).byteValue();
		//ad[1] = new Integer(2).byteValue();
		
		//ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		//outputStream.
		
		//ObjectOutputStream os = new ObjectOutputStream(outputStream);
		//os.writeObject(t);
		//os.flush();
		//byte[] data = outputStream.toByteArray();
		
		//DatagramPacket packetSend = new DatagramPacket(data,data.length,IPAddress,9997);
		//socket.send(packetSend);
	//	System.out.println("sent packet");
		
	//	byte[] send = s.getBytes();
		//DatagramPacket packetSend = new DatagramPacket(send,send.length,IPAddress,9997);
		//socket.send(packetSend);
		socket.close();
	}

}
