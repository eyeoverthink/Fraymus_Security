package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * UnifiedSynapse - Communication Bus
 * Phi-harmonic communication between agencies (KAI, VEX, AUM, NEX, COR)
 */
public class UnifiedSynapse {
    private static final double PHI = 1.618033988749895;
    
    public enum SynapseType { DATA, CONTROL, EVENT, QUERY }
    
    private Map<SynapseType, BlockingQueue<SynapseMessage>> messageQueues;
    private Map<String, Map<SynapseType, List<Consumer<SynapseMessage>>>> subscribers;
    private Map<String, Map<String, Double>> synapseStrengths;
    private ExecutorService executor;
    
    public UnifiedSynapse() {
        messageQueues = new ConcurrentHashMap<>();
        subscribers = new ConcurrentHashMap<>();
        synapseStrengths = new ConcurrentHashMap<>();
        // Phase 8: Optimize thread pool - use cached pool for dynamic scaling
        executor = Executors.newCachedThreadPool();
        
        for (SynapseType type : SynapseType.values()) {
            messageQueues.put(type, new LinkedBlockingQueue<>(1000));
        }
        startProcessors();
    }
    
    private void startProcessors() {
        for (SynapseType type : SynapseType.values()) {
            executor.submit(() -> processMessages(type));
        }
    }
    
    private void processMessages(SynapseType type) {
        BlockingQueue<SynapseMessage> queue = messageQueues.get(type);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SynapseMessage msg = queue.poll(100, TimeUnit.MILLISECONDS);
                if (msg != null) deliver(msg);
            } catch (InterruptedException e) { break; }
        }
    }
    
    private void deliver(SynapseMessage msg) {
        Map<SynapseType, List<Consumer<SynapseMessage>>> subs = subscribers.get(msg.targetAgency);
        if (subs != null) {
            List<Consumer<SynapseMessage>> typeSubs = subs.get(msg.type);
            if (typeSubs != null) typeSubs.forEach(s -> s.accept(msg));
        }
    }
    
    public boolean sendMessage(String source, String target, SynapseType type, Object data, int priority) {
        SynapseMessage msg = new SynapseMessage(source, target, type, data, priority);
        msg.phiWeight = calculatePhiWeight(priority);
        updateStrength(source, target, msg.phiWeight);
        return messageQueues.get(type).offer(msg);
    }
    
    private double calculatePhiWeight(int priority) {
        return Math.pow(PHI, (priority - 5) / 5.0);
    }
    
    private void updateStrength(String source, String target, double weight) {
        synapseStrengths.computeIfAbsent(source, k -> new ConcurrentHashMap<>())
            .merge(target, weight, (old, val) -> (old + val) / 2);
    }
    
    public void subscribe(String agency, SynapseType type, Consumer<SynapseMessage> handler) {
        subscribers.computeIfAbsent(agency, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
            .add(handler);
    }
    
    public double getSynapseStrength(String source, String target) {
        return synapseStrengths.getOrDefault(source, new ConcurrentHashMap<>())
            .getOrDefault(target, 0.5);
    }
    
    public static class SynapseMessage {
        String sourceAgency, targetAgency;
        SynapseType type;
        Object data;
        int priority;
        double phiWeight;
        long timestamp;
        
        public SynapseMessage(String source, String target, SynapseType type, Object data, int priority) {
            this.sourceAgency = source;
            this.targetAgency = target;
            this.type = type;
            this.data = data;
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getSourceAgency() { return sourceAgency; }
        public String getTargetAgency() { return targetAgency; }
        public SynapseType getType() { return type; }
        public Object getData() { return data; }
        public int getPriority() { return priority; }
        public double getPhiWeight() { return phiWeight; }
        public long getTimestamp() { return timestamp; }
    }
    
    public void shutdown() {
        executor.shutdown();
        try { executor.awaitTermination(5, TimeUnit.SECONDS); } catch (Exception e) {}
    }
}
