import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Manager {
	
	static class Agent {
		public int ID;
		public int StartTime; 
		public int PrevTimeRec;
		public int LastTimeReceived; // Same as above. Get the time on the manager side.
		public int Interval;
		public int ListenCount;
		public int CmdPort;
		public String IP;
		
		public Agent() {
			ListenCount = 0;
		}
	}
	
	static class AgentMonitor implements Runnable{ //Both AgentMonitor and BeaconListener
		Map<Integer,Agent> agentList;
		Map<Integer,Agent> deadAgentList;
		
		public AgentMonitor() {
			agentList = new HashMap<>();
			deadAgentList = new HashMap<>();
		}
		
		public void add(Agent agent) {

			if(deadAgentList.containsKey(agent.ID)) {
				System.out.println("Resurrected agent with ID: " + agent.ID);
				agentList.put(agent.ID, agent);
			}
			else if((agentList.containsKey(agent.ID) && agentList.get(agent.ID).StartTime != agent.StartTime)) {
			
				System.out.println("Replaced Agent with ID: " + agent.ID + " has been created. Restarting Listen Counter.");
				agentList.replace(agent.ID, agent);
				
				//This thread should return the GetLocalOS and GetLocalTime from the Agent.
				Thread newAgent = new Thread(new ClientAgent());
				newAgent.run();
			
			}
			else if(agentList.containsKey(agent.ID)) {
				System.out.println("Added listenCount to agent:  " + agent.ID);
				//Add to listen count
				agentList.get(agent.ID).ListenCount++;
				agentList.get(agent.ID).PrevTimeRec = agentList.get(agent.ID).LastTimeReceived;
				agentList.get(agent.ID).LastTimeReceived = agent.LastTimeReceived;
				System.out.println("ListenCount = " + agentList.get(agent.ID).ListenCount);
			}
			else {
				System.out.println("New Agent with ID: " + agent.ID);
				agent.ListenCount = 1;
				agentList.put(agent.ID, agent);
				Thread newAgent = new Thread(new ClientAgent());
				newAgent.run();
				
			}
		}
		
		private void deleteAgent(int id) {
			agentList.remove(id); 
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			while(true) {
				
				try {
					//periodically check the life time of the agentlist.
					if(agentList.isEmpty()) {
						System.out.println("No Active Agents.");
					}
					else {
						for(Map.Entry<Integer, Agent> entry: agentList.entrySet()) {
							
							int seconds = (int) (System.currentTimeMillis()/1000);
							if(seconds - entry.getValue().LastTimeReceived >= 120) {
								System.out.println("Deleting Agent with ID: " + entry.getKey());
								deadAgentList.put(entry.getKey(), entry.getValue());
								deleteAgent(entry.getKey());
							}
						}
					}
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	//TCP
	static class ClientAgent implements Runnable{
		
		private DataInputStream in = null;
		private DataOutputStream out = null;
		
		@Override
		public void run() {
			
			try {
				Socket socket = new Socket("127.0.0.1",9992);
				
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				
				char getOS = '1';
				System.out.println((byte)getOS);
				out.write(getOS);
				out.flush();
				
				int[] a = {1,2,3,4};
				
				byte[] os = new byte[10];
				in.read(os);
				String osName = new String(os);
				System.out.println(osName);
				socket.close();
				
				socket = new Socket("127.0.0.1",9992);
				
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				
				char getTime = '2';
				out.write((byte)getTime);
				out.flush();
				byte[] time = new byte[24];
				in.read(time);
				System.out.println("Received time");
				String t = new String(time,0,time.length-1);
				System.out.println("The Operating System Name is: " + osName + " and the other client's local time is: " + t);
				
				socket.close();
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
			
		}
		
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		DatagramSocket inSocket = new DatagramSocket(9993);
		
		AgentMonitor agentMonitor = new AgentMonitor();
		
		Thread aMonitorThread = new Thread(agentMonitor);
		aMonitorThread.start();
		
		byte[] buf = new byte[1024];
		
		while(true) {
			
			DatagramPacket incoming = new DatagramPacket(buf,buf.length);
			System.out.println("Waiting to receive packet...");
			inSocket.receive(incoming);
			System.out.println("Received packet.");
			byte[] data = incoming.getData();
			Agent ag = new Agent();			
			System.out.println("length of packet: " + data.length);
			ag.ID = (0xFF & data[0]) | ((0xFF & data[1]) << 8) | ((0xFF & data[2]) << 16) | ((0xFF & data[3]) << 24);
			ag.Interval = 60;
			ag.StartTime = (0xFF & data[4]) | ((0xFF & data[5]) << 8) | ((0xFF & data[6]) << 16) | ((0xFF & data[7]) << 24);
			ag.IP = Integer.toString(data[8]) + "." + Integer.toString(data[9]) + "." + Integer.toString(data[10]) + "." + Integer.toString(data[11]);
			ag.CmdPort= (0xFF & data[12]) | ((0xFF & data[13]) << 8) | ((0xFF & data[14]) << 16) | ((0xFF & data[15]) << 24);
			ag.LastTimeReceived = (int) (System.currentTimeMillis()/1000);
			System.out.println("Agent Receieved. ID: "+ag.ID + " StartTime:" + ag.StartTime + " CmdPort:" + ag.CmdPort + " Ip:" + ag.IP);
			
			agentMonitor.add(ag);
		}		
	}
}
