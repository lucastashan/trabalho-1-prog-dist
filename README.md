# Instrucoes para inicializar

## Abrir o terminal

- javac \*.java

## Inicie Super Peers:

- java Main SuperPeer sp1
- java Main SuperPeer sp2
- java Main SuperPeer sp3

### inicie um PEER:

- java Main Peer [ip_Peer] [port_Peer] [ip_SuperPeer] [port_SuperPeer]

### Exemplo, em terminais diferentes executar 

- java Main SuperPeer sp1
- java Main SuperPeer sp2
- java Main SuperPeer sp3
- java Main Peer localhost 6779 localhost 4949
- java Main Peer localhost 6767 localhost 4942

# Entao basta buscar por um recurso em um peer e observar o fluxo das mensagens percorrendo o anel e juntanto as informacoes do DHT, digitando qualquer string
