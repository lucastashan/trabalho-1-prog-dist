import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Main {
	static SuperPeer sp;

	public static void main(String[] args) throws IOException, InterruptedException {
		String type = args[0];
		if (type.equals("SuperPeer"))
			superPeer(args[1]);
		else if (type.equals("Peer"))
			peer(args);
		else
			System.err.println("Usage: java SuperNodo <type> <name>.");
	}

	public static void peer(String[] args) throws IOException, InterruptedException {
		Peer peer = new Peer(args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
		new p2pPeerHeartbeat(args[1], args[2], args[3], args[4]).start();
		InetAddress IPAddress = InetAddress.getByName(peer.getSpIp());
		byte[] buffer = peer.getRegisterMessage();
		DatagramSocket datagramSocket = new DatagramSocket();
		DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, IPAddress, peer.getSpPort());
		datagramSocket.send(datagramPacket);
		Scanner input = new Scanner(System.in);
		while (true) {
			System.out.println("Digite um nome de recurso ou exit para sair:");
			String resource = input.next();
			if(resource.equals("exit"))
				break;
			resource = resource + ";request";
			buffer = resource.getBytes();
			datagramPacket = new DatagramPacket(buffer, buffer.length, IPAddress, peer.getSpPort());
			datagramSocket.send(datagramPacket);
			byte[] receiveBuffer = new byte[1024];
			DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length, IPAddress, peer.getPort());
			datagramSocket.receive(receiveDatagram);
			String peerDestination = new String(receiveBuffer);
			peerDestination = peerDestination.replaceAll("\u0000.*", "");
			System.out.println(peerDestination);
		}
		datagramSocket.close();
		input.close();
	}

	public static void superPeer(String nome) throws NumberFormatException, IOException, InterruptedException {
		File snFile = new File("supernodos.txt");
		Scanner reader = new Scanner(snFile);
		SuperPeer next_sp = null;
		boolean fullTurn = false;
		ArrayList<ActivePeer> activePeers = new ArrayList<ActivePeer>();
		ArrayList<Request> requests = new ArrayList<Request>();
		InetAddress addr;
		int port;
		byte[] response = new byte[1024];
		DatagramPacket packet;

		// inicia o super nodo passado por parametro e o nodo seguinte do anel
		while (reader.hasNextLine()) {
			String line = reader.nextLine();
			String[] parans = line.split("\\s");
			if (parans[0].equals(nome)) {
				sp = new SuperPeer(nome, parans[1], parans[2], parans[3]);
				if (reader.hasNextLine()) {
					line = reader.nextLine();
				} else {
					reader = new Scanner(snFile);
					line = reader.nextLine();
				}
				parans = line.split("\\s");
				next_sp = new SuperPeer(parans[0], parans[1], parans[2], parans[3]);
				break;
			}
		}
		reader.close();

		// inicia o DHT do super nodo
		DHT dht = new DHT();

		// obtem endereco IP do proximo super nodo
		InetAddress IPAddress = InetAddress.getByName(next_sp.getAddr());
		// super nodo vai ficar escutando na porta
		DatagramSocket sup_nodo_dt_socket = new DatagramSocket(Integer.parseInt(sp.getPort()));

		while (true) {
			byte[] receiveData = new byte[1024];
			packet = new DatagramPacket(receiveData, receiveData.length);
			// declara o pacote a ser recebido
			sup_nodo_dt_socket.receive(packet);
			String messageReceive = new String(packet.getData());
			addr = packet.getAddress();
			port = packet.getPort();

			String[] split = messageReceive.split(";");
			String type = split[split.length - 1].replaceAll("\u0000.*", "");
			System.out.println(".");

			if (type.equals("peer")) {
				for (int i = 0; i < split.length - 3; i++)
					dht.addItem(new DHT_Item(split[i], split[split.length - 3], split[split.length - 2]));

				InetAddress address = packet.getAddress();
				ActivePeer activePeer = new ActivePeer(split[split.length - 3], Integer.parseInt(split[split.length - 2]));
				System.out.println(address.getHostName());
				activePeers.add(activePeer);
			}

			if(type.equals("heartbeat")) {
				System.out.println("recebi heartbeat!");
				System.out.println("Ip: "+split[0]);
				System.out.println("Porta: "+split[1]);
				if(activePeers.size()>=1){
					System.out.println(split[0].split("/")[0] + " : "+activePeers.get(0).ip);
					System.out.println(split[1] + " : "+activePeers.get(0).port);
				}
				else{
					System.out.println("Nenhum peer ativo!");
				}
				for(ActivePeer p: activePeers){
					if(split[0].split("/")[0].equals(p.ip) && split[1].equals(Integer.toString(p.port))){
						p.lastTime = System.currentTimeMillis();
						System.out.println("Dei up em um tempo");
					}
				}
			}

			if (type.equals("request")) {
				System.out.println("ip: " + addr);
				System.out.println("port: " + port);
				requests.add(new Request(addr, port, Integer.toString(split[0].hashCode())));
				response = dht.listToMessage(addr, port);
				packet = new DatagramPacket(response, response.length, IPAddress,
						Integer.parseInt(next_sp.getPort()));
				sup_nodo_dt_socket.send(packet);
			}

			if (type.equals("superpeer")) {
				boolean flag = true;
				for (Request request : requests) {
					if ( request.addr == InetAddress.getByName(split[split.length-2]) && request.port == Integer.parseInt(split[split.length-3]) ) {
						String[] arrItens = Arrays.copyOfRange(split, 0, split.length - 3);
						ArrayList<DHT_Item> itens = new ArrayList<DHT_Item>();
						boolean notFound = true;
						for (String item : arrItens) {
							itens.add(new DHT_Item(item));
						}
						for (DHT_Item item : itens) {
							if(item.getHash().equals(request.hash)){
								System.out.println("encontrei o arquivo, ta no ip: " + item.getIp());
								response = item.getIp().getBytes();
								packet = new DatagramPacket(response, response.length,
										InetAddress.getByName(split[split.length-2]), Integer.parseInt(split[split.length-3]));
								sup_nodo_dt_socket.send(packet);
								System.out.println("Enviei o pacote!");
								notFound = false;
							}
						}
						if(notFound){
							response = "Arquivo não encontrado!".getBytes();
							DatagramPacket responsedatagramPacket = new DatagramPacket(response, response.length,
									packet.getAddress(), packet.getPort());
							sup_nodo_dt_socket.send(responsedatagramPacket);
						}
						flag = false;
					}
				}
				if(flag) {
					String[] copyArr = Arrays.copyOfRange(split, 0, split.length - 4);
					for (String s : split) {
						
						System.out.println("split: "+s);
					}
					DHT dhtTemp = new DHT(copyArr, dht);
					for (String s : copyArr) {
						System.out.println(s);
					}
					response = dhtTemp.listToMessage(InetAddress.getByName(split[split.length-3].substring(1)), Integer.parseInt(split[split.length-2]));
					packet = new DatagramPacket(response, response.length, IPAddress,
							Integer.parseInt(next_sp.getPort()));
					sup_nodo_dt_socket.send(packet);
					dhtTemp.printDHT();
				}
			}

			//Confere peers ativos e remove inativos
			for(int i=0; i<activePeers.size(); i++){
				if((System.currentTimeMillis() - activePeers.get(i).lastTime)/1000 >= 5){
					System.out.println("Removi um inativo do DHT: " + activePeers.get(i).port);
					dht.removeItems(activePeers.get(i));
					activePeers.remove(activePeers.get(i));
				}
			}

			TimeUnit.SECONDS.sleep(1);
			dht.printDHT();
		}
	}
}