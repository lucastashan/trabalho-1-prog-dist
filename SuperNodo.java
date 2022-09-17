import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class SuperNodo {
  static SuperPeer sp;
  public static void main(String[] args) throws IOException {
    String nome = args[0];
    File snFile = new File("supernodos.txt");
    Scanner reader = new Scanner(snFile);
    while(reader.hasNextLine()){
      String line = reader.nextLine();
      String[] parans = line.split("\\s");
      if(parans[0].equals(nome)) {
        sp = new SuperPeer(nome, parans[1], parans[2], parans[3]);
        break;
      }
    }
    reader.close();
    
    DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(sp.getPort()));
    byte[] receiveData = new byte[1024];
    while(true){ 
      //DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      // declara o pacote a ser recebido
      //serverSocket.receive(receivePacket);

      //String dataPacket = new String(receivePacket.getData(), "UTF-8").replaceAll("\\x00*", "");
      System.out.println("estou escutando na porta " + sp.getPort());
    }
  }
}