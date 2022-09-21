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
		DatagramSocket datagramSocket = new DatagramSocket(peer.getPort());
		DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, IPAddress, peer.getSpPort());
		datagramSocket.send(datagramPacket);
		Scanner input = new Scanner(System.in);
		while (true) {
			System.out.println("Digite um nome de recurso ou exit para sair:");
			String resource = input.next();
			if (resource.equals("exit"))
				break;
			resource = resource + ";" + peer.getIp() + ";" + peer.getPort() + ";request";
			buffer = resource.getBytes();
			datagramPacket = new DatagramPacket(buffer, buffer.length, IPAddress, peer.getSpPort());
			datagramSocket.send(datagramPacket);

			System.out.println("Estou esperando");
			byte[] receiveBuffer = new byte[1024];
			DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length, IPAddress,
					peer.getPort());
			datagramSocket.receive(receiveDatagram);
			String[] split = (new String(receiveBuffer)).split(";");
			if(split.length==1)
				System.out.println("Não encontrou meu arquivo. =(");
			else
				System.out.println("meu arquivo ta no ip: "+split[0]+" ,porta: "+split[1]);
				
			//peerDestination = peerDestination.replaceAll("\u0000.*", "");
			//System.out.println(peerDestination);
			// if (peerDestination.length() > 0) {
			// 	System.out.println("Opa");
			// 	System.out.println(peerDestination);
			// 	break;
			// }
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
		sup_nodo_dt_socket.setSoTimeout(100);

		while (true) {
			byte[] receiveData = new byte[1024];
			packet = new DatagramPacket(receiveData, receiveData.length);

			try {
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
					ActivePeer activePeer = new ActivePeer(split[split.length - 3],
							Integer.parseInt(split[split.length - 2]));
					System.out.println(address.getHostName());
					activePeers.add(activePeer);
				} else if (type.equals("heartbeat")) {
					System.out.println("recebi heartbeat!");
					System.out.println("Ip: " + split[0]);
					System.out.println("Porta: " + split[1]);
					if (activePeers.size() >= 1) {
						System.out.println(split[0].split("/")[0] + " : " + activePeers.get(0).ip);
						System.out.println(split[1] + " : " + activePeers.get(0).port);
					} else {
						System.out.println("Nenhum peer ativo!");
					}
					for (ActivePeer p : activePeers) {
						if (split[0].split("/")[0].equals(p.ip) && split[1].equals(Integer.toString(p.port))) {
							p.lastTime = System.currentTimeMillis();
							System.out.println("Dei up em um tempo");
						}
					}
				} else if (type.equals("request")) {

					// System.out.println("----------------------");
					System.out.println("Recebi uma request");
					// System.out.println("ip SuperPeer: " + sp.getAddr());
					// System.out.println("port SuperPeer: " + sp.getPort());
					// System.out.println("ip Peer: " + split[split.length-3]);
					// System.out.println("port Peer: " + split[split.length-2]);
					// System.out.println("----------------------");

					requests.add(new Request(split[split.length - 3], Integer.parseInt(split[split.length - 2]),
							Integer.toString(split[0].hashCode())));

					System.out.println("Salvei a seguinte requisicao");
					requests.get(0).printRequest();

					response = dht.listToMessage(sp.getAddr(), sp.getPort(), split[split.length - 3],
							split[split.length - 2], Integer.toString(split[0].hashCode()));

					System.out.println(new String(response));

					packet = new DatagramPacket(response, response.length, IPAddress,
							Integer.parseInt(next_sp.getPort()));
					sup_nodo_dt_socket.send(packet);
				} else if (type.equals("superpeer")) {

					System.out.println();
					System.out.println("Recebi uma mensagem de outro super peer");

					for (String s : split) {
						System.out.print(s + " | ");
					}
					System.out.println();

					String[] arrItens = Arrays.copyOfRange(split, 0, split.length - 6);
					DHT temp = new DHT(arrItens, dht);

					if (split[split.length - 6].equals(sp.getAddr()) && split[split.length - 5].equals(sp.getPort())) {

						System.out.println();
						System.out.println("Sou o destino final e irei encaminhar a mensagem para o Peer");
						System.out.println();

						boolean mensagemNaoEnviada = false;
						for (int i = 0; i < requests.size(); i++) {
							if (requests.get(i).equals(split[split.length - 4], split[split.length - 3],
									split[split.length - 2])) {

								System.out.println();
								System.out.println("Encontrei a mensagem que tenho de encaminhar");
								System.out.println();

								requests.remove(i);
								ArrayList<DHT_Item> foundItems = temp.getItemsByHash(split[split.length - 2]);

								if (foundItems.size() >= 1) {
									System.out.println("Encontrei o arquivo, ta no ip: " + foundItems.get(0).getIp() +
											" porta: " + foundItems.get(0).getPorta());
									response = (foundItems.get(0).getIp() + ";" + foundItems.get(0).getPorta())
											.getBytes();
								} else {
									System.out.println("O arquivo nao pode ser encontrado!");
									response = "O arquivo nao pode ser encontrado!".getBytes();
								}

								packet = new DatagramPacket(response, response.length,
										InetAddress.getByName(foundItems.get(0).getIp()),
										Integer.parseInt(foundItems.get(0).getPorta()));
								sup_nodo_dt_socket.send(packet);
								System.out.println("Enviei o pacote!");
								mensagemNaoEnviada = true;
							}
						}
						if (!mensagemNaoEnviada) {
							System.out.println();
							System.out.println("Nao enconhei em minhas requisicoes, abortar!");
							System.out.println();
						}
					} else {
						response = temp.listToMessage(split[split.length - 6], split[split.length - 5],
								split[split.length - 4], split[split.length - 3], split[split.length - 2]);

						System.out.println();
						System.out.println("Nao sou o destino final e estou enviando: " + new String(response));
						System.out.println();

						packet = new DatagramPacket(response, response.length, IPAddress,
								Integer.parseInt(next_sp.getPort()));
						sup_nodo_dt_socket.send(packet);
						temp.printDHT();
					}
				}
			} catch (java.net.SocketTimeoutException e) {
				System.out.println(".");
				// Confere peers ativos e remove inativos
				for (int i = 0; i < activePeers.size(); i++) {
					if ((System.currentTimeMillis() - activePeers.get(i).lastTime) / 1000 >= 6) {
						System.out.println("Removi um inativo do DHT: " + activePeers.get(i).port);
						dht.removeItems(activePeers.get(i));
						activePeers.remove(activePeers.get(i));
					}
				}
			}

			TimeUnit.SECONDS.sleep(1);
			dht.printDHT();
		}
	}
}