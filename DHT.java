package teste;

import java.util.ArrayList;

public class DHT {
	public ArrayList<DHT_Item> DhtList;
	
	//Construtor padrao
	public DHT() {
		this.DhtList = new ArrayList<DHT_Item>();
	}
	
	//Inicia um DHT novo a partir de uma mensagem recebida, entao combina com os dados do DHT do supernodo atual.
	public DHT(byte[] message, DHT dht) {
		this.DhtList = messageToList(message);
		mergeDHT(dht);
	}
	
	public byte[] listToMessage(){
		String concat = "";
		for (DHT_Item i : DhtList) {
			concat = concat + i.toString();
		}
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

	private ArrayList<DHT_Item> messageToList(byte[] message){
		String stringMessage = new String(message);
		ArrayList<DHT_Item> list = new ArrayList<DHT_Item>();
		
		for(String s : stringMessage.split(";")) {
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

	public void printDHT() {
		for(DHT_Item i : DhtList) {
			System.out.println(i.toString());
		}
	}
}