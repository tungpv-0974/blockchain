package com.myblockchain.services.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblockchain.model.Block;
import com.myblockchain.model.Msg;
import com.myblockchain.model.Transaction;
import com.myblockchain.model.TransactionPool;
import com.myblockchain.services.blockchain.BlockChain;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Data
public class P2pServer implements Runnable {

    static HashMap<String, P2pClient> clients = new HashMap<>();
    private List<Connection> connections = new LinkedList<>();
    private String ip;
    private int port = 8888;
    private ServerSocket ss;
    private boolean run;

    private TransactionPool pool;
    private BlockChain blockchain;

    public P2pServer() {}

    public P2pServer(int port, BlockChain blockchain, TransactionPool pool) {
        this.port = port;
        this.blockchain = blockchain;
        this.pool = pool;
    }

    /**
     * Pairing with the the new P2P server
     * @param peerIp
     */
    public static void Pair(String peerIp) {
        try {
            if (clients.containsKey(peerIp)) {
                return;
            }
            System.out.println(peerIp);
            P2pClient client = new P2pClient(peerIp, 8888);
            clients.put(peerIp, client);

            String localIp = InetAddress.getLocalHost().getHostAddress();
            client.sendMsg(new Msg("Registration", localIp));

        } catch (IOException e) {

        }
    }

    /**
     * Broadcast the pair request to all subnet ip address
     */
    private void findPeer() {
        try {
            String broadcastAddr = "";
            InetAddress address = InetAddress.getLocalHost();
            String ip = address.getHostAddress();
            NetworkInterface netInterface = NetworkInterface.getByInetAddress(address);
            if (!netInterface.isLoopback() && netInterface.isUp()) {
                List<InterfaceAddress> interfaceAddresses = netInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    if (interfaceAddress.getBroadcast() == null) {
                        continue;
                    }
                    broadcastAddr = interfaceAddress.getBroadcast().getHostAddress();
                }
            }
            Msg d = new Msg("Registration", ip);
            String[] part = broadcastAddr.trim().split("\\.");
            for (int i = 0; i < 255; i++) {
                for (int j = 0; j < 255; j++) {
                    for (int k = 0; k < 255; k++) {
                        for (int l = 0; l < 255; l++) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(part[0].equals("255") ? i : part[0] + ".");
                            sb.append(part[1].equals("255") ? j : part[1] + ".");
                            sb.append(part[2].equals("255") ? k : part[2] + ".");
                            sb.append(part[3].equals("255") ? l : part[3]);
                            String receiver = sb.toString();
                            if (receiver.equals(ip)) {
                                continue;
                            }
                            // System.out.println(receiver);
                            P2pClient s = new P2pClient(receiver, port);
                            s.sendMsg(d);
                            if (!part[3].equals("255")) {
                                break;
                            }
                        }
                        if (!part[2].equals("255")) {
                            break;
                        }
                    }
                    if (!part[1].equals("255")) {
                        break;
                    }
                }
                if (!part[0].equals("255")) {
                    break;
                }
            }

        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Broadcast the updated blockchain to peers
     */
    public void broadcastChains(List<Block> chain) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, chain);
            broadcastMsg(new Msg("chain", new String(out.toByteArray())));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Broadcast the latest transaction to peers
     */
    public void broadcastTransaction(Transaction t) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            broadcastMsg(new Msg("transaction", mapper.writeValueAsString(t)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    /**
     * Broadcast to clear the transaction pool to peers
     */
    public void broadcastClearTransaction(ArrayList<Transaction> validTransactions) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, validTransactions);
            broadcastMsg(new Msg("clear", new String(out.toByteArray())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void broadcastMsg(Msg msg) {
        clients.forEach((peerIp, client) -> {
            try {
                client.sendMsg(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Starting the P2P server
     */
    public void init() {
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        run = true;
        System.out.println("P2P server is listening on port: " + port);
        Thread t = new Thread(this);
        t.start();
        findPeer();
    }

    /**
     * Stoping the P2P server
     */
    public void close() {
        run = false;
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread entrance
     */
    public void run() {
        try {
            while (run) {
                if (ss.isClosed()) {
                    return;
                }
                Socket s = ss.accept();
                Connection c = new Connection(s, ss, pool, blockchain);
                connections.add(c);
                //TODO: check connections status
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

