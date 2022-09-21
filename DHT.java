import java.net.InetAddress;
import java.util.ArrayList;

public class DHT {
	public ArrayList<DHT_Item> DhtList;
	
	//Construtor padrao
	public DHT() {
		this.DhtList = new ArrayList<DHT_Item>();
	}

	public DHT(ArrayList<DHT_Item> arr){
		this.DhtList = arr;
	}
	
	//Inicia um DHT novo a partir de uma mensagem recebida, entao combina com os dados do DHT do supernodo atual.
	public DHT(String[] message, DHT dht) {
		this.DhtList = messageToList(message);
		mergeDHT(dht);
	}
	
	public byte[] listToMessage(){
		String concat = "";
		for (DHT_Item i : DhtList) {
			concat = concat + i.toString();
		}
		concat = concat + ";superpeer";
		return concat.getBytes();
	}

	public byte[] listToMessage(String SPaddr, String SPport, String Paddr, String Pport,  String hash){
		String concat = "";
		for (DHT_Item i : DhtList) {
			concat = concat + i.toString();
		}
		concat = concat + SPaddr + ";" + SPport + ";" + Paddr + ";" + Pport + ";" + hash + ";superpeer";
		return concat.getBytes();
	}
	
	//Retorna os items no DHT com o mesmo hash informado por um peer, podemos ter o mesmo hash registrado para portas e ips diferentes
	public ArrayList<DHT_Item> getItemsByHash (String hash){
		ArrayList<DHT_Item> list = new ArrayList<DHT_Item>();
		
		for(DHT_Item i : DhtList) {
			if (i.getHash().equals(hash)) list.add(i);
		}
		return list;
	}

	public String getIpDestByHash (String hash){
		
		for(DHT_Item i : DhtList) {
			if (i.getHash().equals(hash)) 
				return i.getIp();
		}
		return "";
	}

	private ArrayList<DHT_Item> messageToList(String[] message){
		ArrayList<DHT_Item> list = new ArrayList<DHT_Item>();
		
		for(String s : message) {
			list.add(new DHT_Item(s));
		}
		
		return list;
	}
	
	//Combina os items de um DHT com este DHT
	private void mergeDHT(DHT dht) {
		for(DHT_Item i : dht.DhtList) {
			if(!contains(i)) DhtList.add(i);
		}
	}
	
	private boolean contains(DHT_Item item) {
		for(DHT_Item i : DhtList) {
			if (i.getHash().equals(item.getHash()) && i.getIp().equals(item.getIp()) && i.getPorta().equals(item.getPorta())) {
				return true;
			}
		}
		return false;
	}

	public void addItem(DHT_Item item){
		if(!contains(item)){
			DhtList.add(item);
		}
	}

	public void removeItems(ActivePeer p){
		while(true){
			if(!removeItem(p))
				break;
		}
	}

	public boolean removeItem(ActivePeer p){
		for(int i =0; i<DhtList.size();i++){
			System.out.println("Item: " + DhtList.get(i).toString());
			if(DhtList.get(i).getIp().equals(p.ip) && DhtList.get(i).getPorta().equals(Integer.toString(p.port))){
				DhtList.remove(i);
				return true;
			}
		}
		return false;
	}

	public void printDHT() {
		if(DhtList.size()==0){
			System.out.println("Nenhum item no DHT.");
		} else {
			for(DHT_Item i : DhtList) {
				System.out.println(i.toString());
			}
		}
	}
}
