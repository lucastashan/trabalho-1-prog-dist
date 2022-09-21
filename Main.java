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
		InetAddress IPAddress = InetAddress.getByName(peer.getSpIp());
		byte[] buffer = peer.getRegisterMessage();
		DatagramSocket datagramSocket = new DatagramSocket();
		DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, IPAddress, peer.getSpPort());
		datagramSocket.send(datagramPacket);
		Scanner input = new Scanner(System.in);
		while (true) {
			//peer.alive(IPAddress);
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
		boolean starter = true;
		boolean last = false;
		boolean fullTurn = false;

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
					last = true;
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
			if (last && starter) {
				byte[] message = dht.listToMessage();
				DatagramSocket nextNodo = new DatagramSocket();
				DatagramPacket dPacket = new DatagramPacket(message, message.length, IPAddress,
						Integer.parseInt(next_sp.getPort()));
				nextNodo.send(dPacket);
				nextNodo.close();
				starter = false;
			}
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			// declara o pacote a ser recebido
			sup_nodo_dt_socket.receive(receivePacket);
			String messageReceive = new String(receivePacket.getData());

			if (messageReceive.equals("alive"))
				return;

			String[] split = messageReceive.split(";");
			String type = split[split.length - 1].replaceAll("\u0000.*", "");
			System.out.println(".");

			if (type.equals("peer")) {
				for (int i = 0; i < split.length - 3; i++)
					dht.DhtList.add(new DHT_Item(split[i], split[split.length - 3], split[split.length - 2]));

				InetAddress address = receivePacket.getAddress();
				Thread peer = new Thread();

			}

			if (type.equals("request")) {
				String hashName = Integer.toString(split[0].hashCode());
				byte[] message = dht.listToMessage();
				DatagramSocket datagramSocket = new DatagramSocket();
				DatagramPacket datagramPacket = new DatagramPacket(message, message.length, IPAddress,
						Integer.parseInt(next_sp.getPort()));
				datagramSocket.send(datagramPacket);
				byte[] receiveBuffer = new byte[1024];
				DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				sup_nodo_dt_socket.receive(receiveDatagram);
				byte[] peerRequester = (receiveDatagram.getAddress().getHostAddress()+":"+receiveDatagram.getPort()).getBytes();
				// System.out.println("o request deu uma volta anel");
				String sItens = new String(receiveDatagram.getData());
				String[] arrItens = sItens.split(";");
				String[] requestResult = Arrays.copyOfRange(arrItens, 0, arrItens.length - 2);
				ArrayList<DHT_Item> itens = new ArrayList<DHT_Item>();
				for (String item : requestResult) {
					itens.add(new DHT_Item(item));
				}
				for (DHT_Item item : itens) {
					if(item.getHash().equals(hashName)){
						System.out.println("encontrei o arquivo, ta no ip: " + item.getIp());
						DatagramPacket ResponsedatagramPacket = new DatagramPacket(peerRequester, peerRequester.length,
								receivePacket.getAddress(), receivePacket.getPort());
						datagramSocket.send(ResponsedatagramPacket);
						System.out.println("Enviei o pacote!");
					}
				}
				// String ipDest = dhtTemp.getIpDestByHash(hashName);
				// System.out.println("O IP DESTINO: " + ipDest);
				// System.out.println("O HASHNAME: " + hashName + "!");
				// for (String item : requestResult) {
				// 	if(hashName.equals(item))
				// 		System.out.println("Encontrei o arquivo.");
				// }
			} else if (type.equals("superpeer")) {
				DHT dhtTemp = new DHT(Arrays.copyOfRange(split, 0, split.length - 2), dht);
				byte[] message = dhtTemp.listToMessage();
				DatagramSocket nextPeer = new DatagramSocket();
				DatagramPacket datagramPacket = new DatagramPacket(message, message.length, IPAddress,
						Integer.parseInt(next_sp.getPort()));
				nextPeer.send(datagramPacket);
				dhtTemp.printDHT();
			}

			TimeUnit.SECONDS.sleep(1);
			dht.printDHT();

			// String dataPacket = new String(receivePacket.getData(),
			// "UTF-8").replaceAll("\\x00*", "");
			// byte[] message = dht.listToMessage();
			// DatagramSocket nextNodo = new DatagramSocket();
			// DatagramPacket dPacket = new DatagramPacket(message, message.length,
			// IPAddress,
			// Integer.parseInt(next_sp.getPort()));
			// TimeUnit.SECONDS.sleep(1);
			// nextNodo.send(dPacket);
			// nextNodo.close();
			// System.out.println("Enviei o DTH");
		}
	}

	public void timerAlive(InetAddress IPAddress) {
		IPAddress.getHostAddress();
		new Thread() {
			@Override
			public void run() {

				TimerTask count = new TimerTask() {
					Integer count = 0;

					public void run() {
						count++;
						System.out.println(getCount());
						if (count >= 10) {
							return;
						}
					}

					public Integer getCount() {
						return count;
					}
				};
				Timer timer = new Timer();
				timer.schedule(count, 1000);
			}
		}.start();

	}
}