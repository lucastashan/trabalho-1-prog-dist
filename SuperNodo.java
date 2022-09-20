import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SuperNodo {
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
		datagramSocket.close();
		while (true) {
			peer.alive(datagramPacket);

		}
	}

	public static void superPeer(String nome) throws NumberFormatException, IOException, InterruptedException {
		File snFile = new File("supernodos.txt");
		Scanner reader = new Scanner(snFile);
		SuperPeer next_sp = null;
		boolean starter = true;
		boolean last = false;

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
			String[] split = messageReceive.split(";");
			String type = split[split.length - 1].replaceAll("\u0000.*", "");
			System.out.println(".");
			if (type.equals("peer"))
				for (int i = 0; i < split.length - 3; i++)
					dht.DhtList.add(new DHT_Item(split[i], split[split.length - 3], split[split.length - 2]));

			if (type.equals("superpeer")) {
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
}