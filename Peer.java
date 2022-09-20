import java.util.ArrayList;

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

    public String getSpIp(){
        return spIp;
    }

    public int getSpPort() {
        return spPort;
    }

    public byte[] getRegisterMessage() {
        String concat = "";
		for (String archive : listArchive) {
			concat = concat + archive + ";";
		}
        concat = concat + this.ip + ";" + this.port + ";peer" ;
		return concat.getBytes();
    }
    
}
