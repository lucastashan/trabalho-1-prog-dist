import java.net.InetAddress;

public class Request {
    public String addr;
    public int port;
    public String hash;

    public Request(String addr, int port, String hash) {
        this.addr = addr;
        this.port = port;
        this.hash = hash;
    }

    public boolean equals(String addr, String port, String hash){
        if(this.addr.equals(addr) && this.port == Integer.parseInt(port) && this.hash.equals(hash))
            return true;
        else
            return false;
    }

    public void printRequest(){
        System.out.println(addr + " | " + port + " | " + hash );
    }

}
