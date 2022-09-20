public class DHT_Item {
	private String hash;
	private String ip;
	private String porta;
	
	//Construtor padrao, ja converte o nome do arquivo para hash e salva como String
	public DHT_Item(String name, String ip, String porta) {
		this.hash = Integer.toString(name.hashCode());
		this.ip = ip;
		this.porta = porta;
	}
	
	//Construtor usado internamente no DHT
	public DHT_Item(String line) {
		String[] split = line.split(",");
		this.hash = split[0];
		this.ip = split[1];
		this.porta = split[2];
	}
	
	public String getHash() {
		return hash;
	}

	public String getIp() {
		return ip;
	}

	public String getPorta() {
		return porta;
	}

	@Override
	public String toString() {
		return hash + "," + ip + "," + porta + ";";
	}
}
