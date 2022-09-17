public class SuperPeer {
  private String nome;
  private String addr;
  private String port;
  private String next;

  public SuperPeer(String nome, String addr, String port, String next) {
    this.nome = nome;
    this.addr = addr;
    this.port = port;
    this.next = next;
  }

  public String getNome(){
    return nome;
  }

  public String getAddr(){
    return addr;
  }

  public String getPort(){
    return port;
  }

  public String getNext(){
    return next;
  }
}
