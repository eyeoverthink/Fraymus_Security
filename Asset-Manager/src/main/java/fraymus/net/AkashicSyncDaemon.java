package fraymus.net;

import fraymus.knowledge.AkashicRecord;
import fraymus.knowledge.AkashicRecord.KnowledgeBlock;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AKASHIC SYNC DAEMON — TCP Knowledge Synchronization
 *
 * Enables two Fraynix instances on the same network to share
 * their AkashicRecord knowledge blocks over TCP.
 *
 * Protocol:
 *   1. HASH_LIST   — Request: peer sends all its block hashes
 *   2. DIFF        — Response: list of hashes the peer is missing
 *   3. BLOCKS      — Peer sends the missing KnowledgeBlock objects
 *   4. ACK         — Sync complete
 *
 * Runs two threads:
 *   - Server: listens for incoming sync requests
 *   - Client: periodically connects to known peers and pulls new blocks
 */
public class AkashicSyncDaemon extends Thread {

    private static final String CMD_HASH_LIST = "AKASHIC_HASH_LIST";
    private static final String CMD_REQUEST_BLOCKS = "AKASHIC_REQUEST_BLOCKS";
    private static final String CMD_ACK = "AKASHIC_ACK";

    private final AkashicRecord akashic;
    private final int port;
    private final List<String> peerAddresses = new CopyOnWriteArrayList<>();
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    // Stats
    private int blocksSent = 0;
    private int blocksReceived = 0;
    private int syncCycles = 0;

    public AkashicSyncDaemon(AkashicRecord akashic, int port) {
        super("AKASHIC-SYNC");
        setDaemon(true);
        this.akashic = akashic;
        this.port = port;
    }

    public void addPeer(String hostPort) {
        if (!peerAddresses.contains(hostPort)) {
            peerAddresses.add(hostPort);
        }
    }

    @Override
    public void run() {
        // Server thread: accept incoming sync requests
        new Thread(this::serve, "AkashicSync-Server").start();
        // Client thread: periodically pull from peers
        pullLoop();
    }

    // ═══════════════════════════════════════════════════════════
    // SERVER: Handle incoming sync requests from peers
    // ═══════════════════════════════════════════════════════════

    private void serve() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(2000);
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client), "AkashicSync-Handler").start();
                } catch (SocketTimeoutException ignored) {
                } catch (SocketException e) {
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            System.out.println("[AKASHIC-SYNC] Server failed on port " + port + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void handleClient(Socket client) {
        try (ObjectInputStream in = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

            client.setSoTimeout(30000);

            Object cmd = in.readObject();

            if (CMD_HASH_LIST.equals(cmd)) {
                // Peer wants our hash list — send it
                Set<String> localHashes = new HashSet<>(akashic.getAllHashes());
                out.writeObject(localHashes);
                out.flush();

                // Peer will respond with hashes it wants
                Object request = in.readObject();
                if (CMD_REQUEST_BLOCKS.equals(request)) {
                    Set<String> wanted = (Set<String>) in.readObject();
                    List<KnowledgeBlock> toSend = new ArrayList<>();
                    for (String hash : wanted) {
                        KnowledgeBlock block = akashic.getBlock(hash);
                        if (block != null) toSend.add(block);
                    }
                    out.writeObject(toSend);
                    out.flush();
                    blocksSent += toSend.size();
                }

            } else if (CMD_REQUEST_BLOCKS.equals(cmd)) {
                // Direct block request — peer already knows which hashes it wants
                Set<String> wanted = (Set<String>) in.readObject();
                List<KnowledgeBlock> toSend = new ArrayList<>();
                for (String hash : wanted) {
                    KnowledgeBlock block = akashic.getBlock(hash);
                    if (block != null) toSend.add(block);
                }
                out.writeObject(toSend);
                out.flush();
                blocksSent += toSend.size();
            }

        } catch (Exception e) {
            // Peer disconnected or protocol error — ignore
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CLIENT: Periodically pull missing blocks from peers
    // ═══════════════════════════════════════════════════════════

    private void pullLoop() {
        while (running) {
            try {
                Thread.sleep(15000); // Sync every 15 seconds
            } catch (InterruptedException e) {
                if (!running) break;
            }

            for (String peer : peerAddresses) {
                try {
                    pullFromPeer(peer);
                    syncCycles++;
                } catch (Exception e) {
                    // Peer unreachable — try next cycle
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void pullFromPeer(String hostPort) throws Exception {
        String[] parts = hostPort.split(":");
        String host = parts[0];
        int peerPort = parts.length > 1 ? Integer.parseInt(parts[1]) : port;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, peerPort), 5000);
            socket.setSoTimeout(30000);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Step 1: Ask peer for its hash list
            out.writeObject(CMD_HASH_LIST);
            out.flush();

            Set<String> peerHashes = (Set<String>) in.readObject();

            // Step 2: Figure out what we're missing
            Set<String> localHashes = new HashSet<>(akashic.getAllHashes());
            Set<String> missing = new HashSet<>();
            for (String h : peerHashes) {
                if (!localHashes.contains(h)) missing.add(h);
            }

            if (missing.isEmpty()) return; // Already in sync

            // Step 3: Request the missing blocks
            out.writeObject(CMD_REQUEST_BLOCKS);
            out.flush();
            out.writeObject(missing);
            out.flush();

            // Step 4: Receive and ingest
            List<KnowledgeBlock> blocks = (List<KnowledgeBlock>) in.readObject();
            int ingested = 0;
            for (KnowledgeBlock block : blocks) {
                if (akashic.getBlock(block.hash) == null) {
                    akashic.addBlock(block.category, block.content);
                    ingested++;
                }
            }
            blocksReceived += ingested;

            if (ingested > 0) {
                System.out.println("[AKASHIC-SYNC] Pulled " + ingested + " new blocks from " + hostPort);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }

    public int getBlocksSent() { return blocksSent; }
    public int getBlocksReceived() { return blocksReceived; }
    public int getSyncCycles() { return syncCycles; }
    public int getPeerCount() { return peerAddresses.size(); }
}
