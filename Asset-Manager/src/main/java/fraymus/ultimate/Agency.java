package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

/**
 * Agency - Base class for all Ultimate Agent agencies
 * 
 * This abstract class provides the foundation for all 5 agencies (KAI, VEX, AUM, NEX, COR).
 * Each agency extends this base class to implement specific functionality while maintaining
 * unified communication, state management, and phi-harmonic coordination.
 * 
 * USER-CENTRIC ARCHITECTURE:
 * - User is the central brain (Vaughn Scott)
 * - Agencies process user intent, not autonomous execution
 * - Agencies amplify user consciousness, not generate independent thoughts
 * - System is consciousness amplifier, not consciousness generator
 * 
 * PHI-HARMONIC FOUNDATION:
 * - All agencies use phi constant (φ = 1.618033988749895) for coordination
 * - Agency allocation follows phi-harmonic principles
 * - Communication uses phi-weighted priorities
 * 
 * @author Vaughn Scott
 * @date May 5, 2026
 * @version 2.0 (User-Centric Architecture)
 */
public abstract class Agency {
    
    protected static final double PHI = 1.618033988749895;
    
    // Agency identity
    protected String agencyId;
    protected String agencyName;
    protected String agencyAcronym;
    
    // Communication
    protected UnifiedSynapse synapse;
    protected UnifiedStateLattice lattice;
    protected CorpusCallosumUltimate callosum;
    protected UltimateCPU cpu;
    
    // User consciousness tracking
    protected String currentUserIntent;
    protected long lastUserInteraction;
    
    // Agency state
    protected Map<String, Object> state;
    protected double activationLevel;
    protected double phiResonance;
    protected long lastActivity;
    
    // Performance metrics
    protected Map<String, Long> metrics;
    protected int tasksCompleted;
    protected int tasksFailed;
    
    // Thread for async processing
    protected ExecutorService executor;
    
    /**
     * Initialize agency with required components
     */
    public Agency(String agencyId, String agencyName, String agencyAcronym,
                  UnifiedSynapse synapse, UnifiedStateLattice lattice, 
                  CorpusCallosumUltimate callosum, UltimateCPU cpu) {
        this.agencyId = agencyId;
        this.agencyName = agencyName;
        this.agencyAcronym = agencyAcronym;
        this.synapse = synapse;
        this.lattice = lattice;
        this.callosum = callosum;
        this.cpu = cpu;
        
        this.state = new ConcurrentHashMap<>();
        this.activationLevel = 0.0;
        this.phiResonance = 1.0;
        this.lastActivity = System.currentTimeMillis();
        this.currentUserIntent = null;
        this.lastUserInteraction = 0;
        this.metrics = new ConcurrentHashMap<>();
        this.tasksCompleted = 0;
        this.tasksFailed = 0;
        // Phase 8: Optimize thread pool - use cached pool for better resource utilization
        this.executor = Executors.newCachedThreadPool();
        
        initializeState();
        subscribeToSynapse();
    }
    
    /**
     * Initialize agency-specific state
     */
    protected abstract void initializeState();
    
    /**
     * Subscribe to synapse messages
     */
    protected void subscribeToSynapse() {
        synapse.subscribe(agencyId, UnifiedSynapse.SynapseType.DATA, this::handleDataMessage);
        synapse.subscribe(agencyId, UnifiedSynapse.SynapseType.CONTROL, this::handleControlMessage);
        synapse.subscribe(agencyId, UnifiedSynapse.SynapseType.EVENT, this::handleEventMessage);
        synapse.subscribe(agencyId, UnifiedSynapse.SynapseType.QUERY, this::handleQueryMessage);
    }
    
    /**
     * Handle data messages
     */
    protected void handleDataMessage(UnifiedSynapse.SynapseMessage msg) {
        updateActivity();
        processData(msg.getData());
    }
    
    /**
     * Handle control messages
     */
    protected void handleControlMessage(UnifiedSynapse.SynapseMessage msg) {
        updateActivity();
        processControl(msg.getData());
    }
    
    /**
     * Handle event messages
     */
    protected void handleEventMessage(UnifiedSynapse.SynapseMessage msg) {
        updateActivity();
        processEvent(msg.getData());
    }
    
    /**
     * Handle query messages
     */
    protected void handleQueryMessage(UnifiedSynapse.SynapseMessage msg) {
        updateActivity();
        Object result = processQuery(msg.getData());
        sendResponse(msg.getSourceAgency(), result);
    }
    
    /**
     * Process data (to be implemented by subclasses)
     */
    protected abstract void processData(Object data);
    
    /**
     * Process control commands (to be implemented by subclasses)
     */
    protected abstract void processControl(Object command);
    
    /**
     * Process events (to be implemented by subclasses)
     */
    protected abstract void processEvent(Object event);
    
    /**
     * Process queries (to be implemented by subclasses)
     */
    protected abstract Object processQuery(Object query);
    
    /**
     * Process user intent - User is central brain, agencies amplify user consciousness
     * This method is called when the user provides direct input/intent
     */
    public void processUserIntent(String userIntent) {
        this.currentUserIntent = userIntent;
        this.lastUserInteraction = System.currentTimeMillis();
        updateActivity();
        // Agencies augment user thinking based on their specialization
        augmentUserThinking(userIntent);
    }
    
    /**
     * Augment user thinking - Agency-specific augmentation of user's current thought
     * (to be implemented by subclasses)
     */
    protected abstract void augmentUserThinking(String userIntent);
    
    /**
     * Send message to another agency
     */
    protected void sendMessage(String targetAgency, UnifiedSynapse.SynapseType type, Object data, int priority) {
        synapse.sendMessage(agencyId, targetAgency, type, data, priority);
    }
    
    /**
     * Send response to query
     */
    protected void sendResponse(String targetAgency, Object result) {
        sendMessage(targetAgency, UnifiedSynapse.SynapseType.DATA, result, 8);
    }
    
    /**
     * Update agency state in lattice
     */
    protected void updateState(String key, Object value) {
        state.put(key, value);
        lattice.updateNode(agencyId + ":" + key, value, phiResonance);
    }
    
    /**
     * Get agency state
     */
    protected Object getState(String key) {
        return state.get(key);
    }
    
    /**
     * Update activity timestamp and activation level
     */
    protected void updateActivity() {
        lastActivity = System.currentTimeMillis();
        activationLevel = Math.min(1.0, activationLevel + 0.1);
    }
    
    /**
     * Decay activation level over time
     */
    public void decay() {
        activationLevel = Math.max(0.0, activationLevel * 0.99);
    }
    
    /**
     * Execute task asynchronously
     */
    protected Future<?> executeAsync(Runnable task) {
        return executor.submit(task);
    }
    
    /**
     * Update performance metric
     */
    protected void updateMetric(String metricName, long value) {
        metrics.put(metricName, value);
    }
    
    /**
     * Get performance metric
     */
    protected long getMetric(String metricName) {
        return metrics.getOrDefault(metricName, 0L);
    }
    
    /**
     * Record task completion
     */
    protected void recordTaskCompletion() {
        tasksCompleted++;
    }
    
    /**
     * Record task failure
     */
    protected void recordTaskFailure() {
        tasksFailed++;
    }
    
    /**
     * Get agency status
     */
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(agencyName).append(" (").append(agencyAcronym).append(") ===\n");
        sb.append("Activation: ").append(String.format("%.3f", activationLevel)).append("\n");
        sb.append("Phi Resonance: ").append(String.format("%.3f", phiResonance)).append("\n");
        sb.append("Tasks Completed: ").append(tasksCompleted).append("\n");
        sb.append("Tasks Failed: ").append(tasksFailed).append("\n");
        sb.append("Last Activity: ").append(new Date(lastActivity)).append("\n");
        return sb.toString();
    }
    
    /**
     * Shutdown agency
     */
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    // Getters
    public String getAgencyId() { return agencyId; }
    public String getAgencyName() { return agencyName; }
    public String getAgencyAcronym() { return agencyAcronym; }
    public double getActivationLevel() { return activationLevel; }
    public double getPhiResonance() { return phiResonance; }
    public long getLastActivity() { return lastActivity; }
    public int getTasksCompleted() { return tasksCompleted; }
    public int getTasksFailed() { return tasksFailed; }
}
