package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UnifiedStateLattice - Unified State Management
 * 
 * This class provides a single source of truth for all agent states across
 * the entire Fraynix Nexus system. It enables state synchronization between
 * all agencies (KAI, VEX, AUM, NEX, COR) and maintains phi-harmonic
 * coherence across the entire system.
 * 
 * PHI-HARMMONIC FOUNDATION:
 * - State transitions follow phi-harmonic patterns
 * - Phi constant (φ = 1.618033988749895) governs state evolution
 * - State coherence is measured by phi-resonance
 * 
 * @author Vaughn Scott
 * @date May 5, 2026
 * @version 1.0
 */
public class UnifiedStateLattice {
    
    // Phi constant for harmonic calculations
    private static final double PHI = 1.618033988749895;
    
    // State lattice structure
    private Map<String, LatticeNode> lattice;
    private Map<String, StateSnapshot> snapshots;
    
    // Agency states
    private Map<String, AgencyState> agencyStates;
    
    // Phi-harmonic coherence tracking
    private double globalCoherence;
    private Map<String, Double> agencyCoherence;
    
    // State synchronization
    private long lastSyncTime;
    private long syncInterval;
    
    /**
     * Initialize the unified state lattice
     */
    public UnifiedStateLattice() {
        this.lattice = new ConcurrentHashMap<>();
        this.snapshots = new ConcurrentHashMap<>();
        this.agencyStates = new ConcurrentHashMap<>();
        this.agencyCoherence = new ConcurrentHashMap<>();
        this.globalCoherence = 1.0;
        this.syncInterval = 100; // 100ms sync interval (phi-harmonic)
        this.lastSyncTime = System.currentTimeMillis();
        
        initializeAgencies();
    }
    
    /**
     * Initialize all 5 agencies in the state lattice
     */
    private void initializeAgencies() {
        String[] agencies = {"KAI", "VEX", "AUM", "NEX", "COR"};
        for (String agency : agencies) {
            agencyStates.put(agency, new AgencyState(agency));
            agencyCoherence.put(agency, 1.0);
        }
    }
    
    /**
     * Get or create a lattice node
     * 
     * @param nodeId The node identifier
     * @return The lattice node
     */
    public LatticeNode getOrCreateNode(String nodeId) {
        return lattice.computeIfAbsent(nodeId, k -> new LatticeNode(k));
    }
    
    /**
     * Get a lattice node
     * 
     * @param nodeId The node identifier
     * @return The lattice node, or null if not found
     */
    public LatticeNode getNode(String nodeId) {
        return lattice.get(nodeId);
    }
    
    /**
     * Update a lattice node's state
     * 
     * @param nodeId The node identifier
     * @param state The new state
     * @param phiResonance The phi-resonance of the update
     */
    public void updateNode(String nodeId, Object state, double phiResonance) {
        LatticeNode node = getOrCreateNode(nodeId);
        node.setState(state);
        node.setPhiResonance(phiResonance);
        node.setLastUpdate(System.currentTimeMillis());
        
        // Trigger phi-harmonic propagation
        propagateStateChange(nodeId, phiResonance);
    }
    
    /**
     * Propagate state changes through the lattice using phi-harmonic principles
     * 
     * @param sourceNodeId The source node
     * @param phiResonance The phi-resonance of the change
     */
    private void propagateStateChange(String sourceNodeId, double phiResonance) {
        // Find connected nodes
        List<String> connectedNodes = findConnectedNodes(sourceNodeId);
        
        // Propagate with phi-harmonic decay
        for (String targetNodeId : connectedNodes) {
            LatticeNode targetNode = getNode(targetNodeId);
            if (targetNode != null) {
                double decay = Math.pow(PHI, -calculateDistance(sourceNodeId, targetNodeId));
                double influence = phiResonance * decay;
                
                if (influence > 0.1) { // Threshold for propagation
                    targetNode.addInfluence(influence);
                }
            }
        }
        
        // Update global coherence
        updateGlobalCoherence();
    }
    
    /**
     * Find nodes connected to a given node
     * 
     * @param nodeId The node identifier
     * @return List of connected node IDs
     */
    private List<String> findConnectedNodes(String nodeId) {
        List<String> connected = new ArrayList<>();
        
        // Simple heuristic: nodes with similar IDs are connected
        // In a full implementation, this would use a graph structure
        for (String id : lattice.keySet()) {
            if (!id.equals(nodeId) && areConnected(nodeId, id)) {
                connected.add(id);
            }
        }
        
        return connected;
    }
    
    /**
     * Check if two nodes are connected
     * 
     * @param id1 First node ID
     * @param id2 Second node ID
     * @return true if connected
     */
    private boolean areConnected(String id1, String id2) {
        // Simple heuristic: same prefix = connected
        String prefix1 = id1.split(":")[0];
        String prefix2 = id2.split(":")[0];
        return prefix1.equals(prefix2);
    }
    
    /**
     * Calculate distance between two nodes (phi-harmonic)
     * 
     * @param id1 First node ID
     * @param id2 Second node ID
     * @return Distance (0.0 to 1.0)
     */
    private double calculateDistance(String id1, String id2) {
        // Simple heuristic: edit distance normalized
        int maxLen = Math.max(id1.length(), id2.length());
        if (maxLen == 0) return 0.0;
        
        int distance = levenshteinDistance(id1, id2);
        return (double) distance / maxLen;
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return Edit distance
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[len1][len2];
    }
    
    /**
     * Update global coherence based on all node states
     */
    private void updateGlobalCoherence() {
        if (lattice.isEmpty()) {
            globalCoherence = 1.0;
            return;
        }
        
        double totalPhi = 0.0;
        int nodeCount = 0;
        
        for (LatticeNode node : lattice.values()) {
            totalPhi += node.getPhiResonance();
            nodeCount++;
        }
        
        globalCoherence = totalPhi / nodeCount;
        
        // Update agency coherence
        for (String agency : agencyStates.keySet()) {
            updateAgencyCoherence(agency);
        }
    }
    
    /**
     * Update coherence for a specific agency
     * 
     * @param agency The agency name
     */
    private void updateAgencyCoherence(String agency) {
        double totalPhi = 0.0;
        int nodeCount = 0;
        
        for (LatticeNode node : lattice.values()) {
            if (node.getNodeId().startsWith(agency + ":")) {
                totalPhi += node.getPhiResonance();
                nodeCount++;
            }
        }
        
        double coherence = nodeCount > 0 ? totalPhi / nodeCount : 1.0;
        agencyCoherence.put(agency, coherence);
    }
    
    /**
     * Get agency state
     * 
     * @param agency The agency name
     * @return The agency state
     */
    public AgencyState getAgencyState(String agency) {
        return agencyStates.get(agency.toUpperCase());
    }
    
    /**
     * Update agency state
     * 
     * @param agency The agency name
     * @param state The state key
     * @param value The state value
     */
    public void updateAgencyState(String agency, String state, Object value) {
        AgencyState agencyState = agencyStates.get(agency.toUpperCase());
        if (agencyState != null) {
            agencyState.setState(state, value);
            agencyState.setLastUpdate(System.currentTimeMillis());
        }
    }
    
    /**
     * Get agency coherence
     * 
     * @param agency The agency name
     * @return Coherence (0.0 to 1.0)
     */
    public double getAgencyCoherence(String agency) {
        return agencyCoherence.getOrDefault(agency.toUpperCase(), 0.0);
    }
    
    /**
     * Get global coherence
     * 
     * @return Global coherence (0.0 to 1.0)
     */
    public double getGlobalCoherence() {
        return globalCoherence;
    }
    
    /**
     * Create a state snapshot
     * 
     * @param snapshotId The snapshot identifier
     * @return The snapshot
     */
    public StateSnapshot createSnapshot(String snapshotId) {
        StateSnapshot snapshot = new StateSnapshot(snapshotId);
        
        // Copy all node states
        for (LatticeNode node : lattice.values()) {
            snapshot.addNodeState(node.getNodeId(), node.getState(), node.getPhiResonance());
        }
        
        // Copy all agency states
        for (AgencyState agency : agencyStates.values()) {
            snapshot.addAgencyState(agency.getAgency(), agency.getStates());
        }
        
        snapshot.setGlobalCoherence(globalCoherence);
        snapshot.setTimestamp(System.currentTimeMillis());
        
        snapshots.put(snapshotId, snapshot);
        return snapshot;
    }
    
    /**
     * Restore a state snapshot
     * 
     * @param snapshotId The snapshot identifier
     * @return true if restored successfully
     */
    public boolean restoreSnapshot(String snapshotId) {
        StateSnapshot snapshot = snapshots.get(snapshotId);
        if (snapshot == null) {
            return false;
        }
        
        // Restore node states
        for (Map.Entry<String, Object> entry : snapshot.getNodeStates().entrySet()) {
            String nodeId = entry.getKey();
            Object state = entry.getValue();
            double phiResonance = snapshot.getNodePhiResonance(nodeId);
            updateNode(nodeId, state, phiResonance);
        }
        
        // Restore agency states
        for (Map.Entry<String, Map<String, Object>> entry : snapshot.getAgencyStates().entrySet()) {
            String agency = entry.getKey();
            Map<String, Object> states = entry.getValue();
            for (Map.Entry<String, Object> stateEntry : states.entrySet()) {
                updateAgencyState(agency, stateEntry.getKey(), stateEntry.getValue());
            }
        }
        
        return true;
    }
    
    /**
     * Synchronize all states (phi-harmonic synchronization)
     */
    public void synchronize() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastSyncTime < syncInterval) {
            return; // Not time to sync yet
        }
        
        lastSyncTime = currentTime;
        
        // Perform phi-harmonic synchronization
        for (String agency : agencyStates.keySet()) {
            synchronizeAgency(agency);
        }
        
        // Update coherence
        updateGlobalCoherence();
    }
    
    /**
     * Synchronize a specific agency
     * 
     * @param agency The agency name
     */
    private void synchronizeAgency(String agency) {
        AgencyState agencyState = agencyStates.get(agency);
        if (agencyState == null) return;
        
        // Apply phi-harmonic smoothing
        double targetCoherence = getAgencyCoherence(agency);
        double currentCoherence = agencyCoherence.get(agency);
        double smoothing = (targetCoherence - currentCoherence) / PHI;
        
        agencyCoherence.put(agency, currentCoherence + smoothing);
    }
    
    /**
     * Get lattice statistics
     * 
     * @return Formatted statistics string
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== UNIFIED STATE LATTICE STATISTICS ===\n");
        sb.append("Total Nodes: ").append(lattice.size()).append("\n");
        sb.append("Total Snapshots: ").append(snapshots.size()).append("\n");
        sb.append("Global Coherence: ").append(String.format("%.3f", globalCoherence)).append("\n");
        sb.append("\n=== AGENCY COHERENCE ===\n");
        
        for (String agency : agencyStates.keySet()) {
            sb.append(agency).append(": ").append(String.format("%.3f", getAgencyCoherence(agency))).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Lattice Node - represents a node in the state lattice
     */
    public static class LatticeNode {
        private String nodeId;
        private Object state;
        private double phiResonance;
        private long lastUpdate;
        private double accumulatedInfluence;
        
        public LatticeNode(String nodeId) {
            this.nodeId = nodeId;
            this.phiResonance = 1.0;
            this.lastUpdate = System.currentTimeMillis();
            this.accumulatedInfluence = 0.0;
        }
        
        public String getNodeId() {
            return nodeId;
        }
        
        public Object getState() {
            return state;
        }
        
        public void setState(Object state) {
            this.state = state;
        }
        
        public double getPhiResonance() {
            return phiResonance;
        }
        
        public void setPhiResonance(double phiResonance) {
            this.phiResonance = phiResonance;
        }
        
        public long getLastUpdate() {
            return lastUpdate;
        }
        
        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
        
        public void addInfluence(double influence) {
            this.accumulatedInfluence += influence;
            // Apply influence to phi-resonance
            this.phiResonance = Math.max(0.0, Math.min(1.0, this.phiResonance + influence / 10.0));
        }
        
        public double getAccumulatedInfluence() {
            return accumulatedInfluence;
        }
        
        public void resetInfluence() {
            this.accumulatedInfluence = 0.0;
        }
    }
    
    /**
     * Agency State - represents the state of an agency
     */
    public static class AgencyState {
        private String agency;
        private Map<String, Object> states;
        private long lastUpdate;
        
        public AgencyState(String agency) {
            this.agency = agency;
            this.states = new ConcurrentHashMap<>();
            this.lastUpdate = System.currentTimeMillis();
        }
        
        public String getAgency() {
            return agency;
        }
        
        public Map<String, Object> getStates() {
            return states;
        }
        
        public void setState(String key, Object value) {
            states.put(key, value);
        }
        
        public Object getState(String key) {
            return states.get(key);
        }
        
        public long getLastUpdate() {
            return lastUpdate;
        }
        
        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
    
    /**
     * State Snapshot - represents a point-in-time state snapshot
     */
    public static class StateSnapshot {
        private String snapshotId;
        private Map<String, Object> nodeStates;
        private Map<String, Double> nodePhiResonance;
        private Map<String, Map<String, Object>> agencyStates;
        private double globalCoherence;
        private long timestamp;
        
        public StateSnapshot(String snapshotId) {
            this.snapshotId = snapshotId;
            this.nodeStates = new ConcurrentHashMap<>();
            this.nodePhiResonance = new ConcurrentHashMap<>();
            this.agencyStates = new ConcurrentHashMap<>();
        }
        
        public String getSnapshotId() {
            return snapshotId;
        }
        
        public Map<String, Object> getNodeStates() {
            return nodeStates;
        }
        
        public void addNodeState(String nodeId, Object state, double phiResonance) {
            nodeStates.put(nodeId, state);
            nodePhiResonance.put(nodeId, phiResonance);
        }
        
        public double getNodePhiResonance(String nodeId) {
            return nodePhiResonance.getOrDefault(nodeId, 1.0);
        }
        
        public Map<String, Map<String, Object>> getAgencyStates() {
            return agencyStates;
        }
        
        public void addAgencyState(String agency, Map<String, Object> states) {
            agencyStates.put(agency, states);
        }
        
        public double getGlobalCoherence() {
            return globalCoherence;
        }
        
        public void setGlobalCoherence(double globalCoherence) {
            this.globalCoherence = globalCoherence;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        
        // Test node creation
        lattice.updateNode("KAI:thought", "Hello world", 0.9);
        lattice.updateNode("VEX:image", "nebula.png", 0.8);
        lattice.updateNode("AUM:speech", "Hello", 0.7);
        
        System.out.println(lattice.getStatistics());
        
        // Test snapshot
        StateSnapshot snapshot = lattice.createSnapshot("test_snapshot");
        System.out.println("\nSnapshot created: " + snapshot.getSnapshotId());
        
        // Test agency state
        lattice.updateAgencyState("KAI", "current_task", "reasoning");
        lattice.updateAgencyState("VEX", "current_task", "visualization");
        
        System.out.println("\nKAI state: " + lattice.getAgencyState("KAI").getStates());
    }
}
