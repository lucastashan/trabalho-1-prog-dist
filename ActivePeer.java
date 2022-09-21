public class ActivePeer {
    private String ip;
    private int port;
    public long lastTime;

    public ActivePeer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.lastTime = System.currentTimeMillis();
    }
}
