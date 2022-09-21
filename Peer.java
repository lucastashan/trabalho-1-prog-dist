import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.SocketException;

public class Peer {
    private String ip;
    private int port;
    private String spIp;
    private int spPort;
    private ArrayList<String> listArchive;

    public Peer(String ip, int port, String spIp, int spPort) {
        this.ip = ip;
        this.port = port;
        this.spIp = spIp;
        this.spPort = spPort;
        this.listArchive = new ArrayList<>();
        listArchive.add("cinderela.mp4");
        listArchive.add("senhas.txt");
    }

    public String getSpIp() {
        return spIp;
    }

    public int getSpPort() {
        return spPort;
    }

    public int getPort() {
        return port;
    }

    public void alive(InetAddress IPAddress) throws IOException, InterruptedException {
        DatagramSocket datagramSocket = new DatagramSocket();
        byte[] buffer = getAliveMessage();
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, IPAddress, getSpPort());
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
        System.out.println("Eu packet IP: " + getSpIp() + " Port: " + getSpPort() + " estou vivo!");
        Thread.sleep(5000);
    }

    public byte[] getRegisterMessage() {
        String concat = "";
        for (String archive : listArchive) {
            concat = concat + archive + ";";
        }
        concat = concat + this.ip + ";" + this.port + ";peer";
        return concat.getBytes();
    }

    public byte[] getAliveMessage() {
        String alive_message = "alive";
        return alive_message.getBytes();
    }

}
