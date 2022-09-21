import java.io.*;
import java.net.*;
import java.util.*;

public class p2pPeerHeartbeat extends Thread {
	protected DatagramSocket socket = null;
	protected DatagramPacket packet = null;
	protected InetAddress peerAddr = null;
	protected InetAddress superPeerAddr = null;
	protected int peerPort;
	protected int superPeerPort;
	protected byte[] data = new byte[1024];

	public p2pPeerHeartbeat(String peerIp, String peerPort, String superPeerIp, String superPeerPort) throws IOException {
		// envia um packet
		// String vars[] = args[1].split("\\s");
		peerAddr = InetAddress.getByName(peerIp);
		superPeerAddr = InetAddress.getByName(superPeerIp);
		this.peerPort = Integer.parseInt(peerPort);
		this.superPeerPort = Integer.parseInt(superPeerPort);
		data = (peerAddr + ";" + peerPort + ";heartbeat").getBytes();
		// cria um socket datagrama
		socket = new DatagramSocket(this.peerPort+100);
	}

	public void run() {
		while (true) {
			try {
				packet = new DatagramPacket(data, data.length, superPeerAddr, superPeerPort);
				socket.send(packet);
			} catch (IOException e) {
				socket.close();
			}
			
			try {
				Thread.sleep(3000);
			} catch(InterruptedException e) {
			}
//			System.out.println("\npulse!");
		}
	}
}
