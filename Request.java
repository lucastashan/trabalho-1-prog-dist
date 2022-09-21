import java.net.InetAddress;

public class Request {
    public InetAddress addr;
    public int port;
    public String hash;

    public Request(InetAddress addr, int port, String hash) {
        this.addr = addr;
        this.port = port;
        this.hash = hash;
    }

}
