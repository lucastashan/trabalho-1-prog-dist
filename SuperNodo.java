import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SuperNodo {
  static SuperPeer sp;
  public static void main(String[] args) throws IOException, InterruptedException {
    String nome = args[0];
    File snFile = new File("supernodos.txt");
    Scanner reader = new Scanner(snFile);
    SuperPeer next_sp = null;
    boolean starter = true;
    boolean last = false;

    // inicia o super nodo passado por parametro e o nodo seguinte do anel
    while(reader.hasNextLine()){
      String line = reader.nextLine();
      String[] parans = line.split("\\s");
      if(parans[0].equals(nome)) {
        sp = new SuperPeer(nome, parans[1], parans[2], parans[3]);
        if(reader.hasNextLine()){
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
    byte[] receiveData = new byte[1024];

    while(true){ 
      if(last && starter){
        byte[] message = dht.listToMessage();
        DatagramSocket nextNodo = new DatagramSocket();
        DatagramPacket dPacket = new DatagramPacket(message, message.length, IPAddress, Integer.parseInt(next_sp.getPort()));
        nextNodo.send(dPacket);
        nextNodo.close();
        System.out.println("Enviei o DTH");
        starter = false;
      }
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      // declara o pacote a ser recebido
      sup_nodo_dt_socket.receive(receivePacket);
      System.out.println("Recebi o DTH");

      //String dataPacket = new String(receivePacket.getData(), "UTF-8").replaceAll("\\x00*", "");
      byte[] message = dht.listToMessage();
      DatagramSocket nextNodo = new DatagramSocket();
      DatagramPacket dPacket = new DatagramPacket(message, message.length, IPAddress, Integer.parseInt(next_sp.getPort()));
      TimeUnit.SECONDS.sleep(1);
      nextNodo.send(dPacket);
      nextNodo.close();
      System.out.println("Enviei o DTH");
    }
  }
}