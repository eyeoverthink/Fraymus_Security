package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

/**
 * SystemPerformanceMonitor - System-Wide Performance Monitoring
 * 
 * Monitors performance metrics across all agencies (KAI, VEX, AUM, NEX, COR)
 * and provides phi-harmonic analysis of system health, bottlenecks, and
 * optimization opportunities.
 * 
 * Features:
 * - Real-time performance tracking for all agencies
 * - Phi-harmonic performance scoring
 * - Bottleneck detection and analysis
 * - Resource utilization monitoring
 * - Performance trend analysis
 * - Automated performance reports
 * 
 * @author Vaughn Scott
 * @date May 7, 2026
 * @version 1.0
 */
public class SystemPerformanceMonitor {
    
    private static final double PHI = 1.618033988749895;
    
    // Performance metrics for each agency
    private Map<String, AgencyMetrics> agencyMetrics;
    
    // System-wide metrics
    private double systemThroughput;
    private double systemLatency;
    private double systemEfficiency;
    private double systemHealth;
    
    // Monitoring state
    private boolean isMonitoring;
    private long monitoringInterval;
    private long lastUpdateTime;
    private int monitoringCycles;
    
    // Performance history
    private Queue<SystemSnapshot> performanceHistory;
    private int maxHistorySize;
    
    // Bottleneck detection
    private List<Bottleneck> detectedBottlenecks;
    private double bottleneckThreshold;
    
    /**
     * Initialize system performance monitor
     */
    public SystemPerformanceMonitor() {
        this.agencyMetrics = new ConcurrentHashMap<>();
        this.systemThroughput = 0.0;
        this.systemLatency = 0.0;
        this.systemEfficiency = 0.8;
        this.systemHealth = 1.0;
        
        this.isMonitoring = false;
        this.monitoringInterval = 1000; // 1 second
        this.lastUpdateTime = System.currentTimeMillis();
        this.monitoringCycles = 0;
        
        // Phase 8: Optimize - use ConcurrentLinkedQueue for thread-safe non-blocking operations
        this.performanceHistory = new ConcurrentLinkedQueue<>();
        this.maxHistorySize = 100;
        
        this.detectedBottlenecks = new ArrayList<>();
        this.bottleneckThreshold = 0.7; // 70% threshold
        
        initializeAgencyMetrics();
    }
    
    /**
     * Initialize metrics for all agencies
     */
    private void initializeAgencyMetrics() {
        String[] agencies = {"KAI", "VEX", "AUM", "NEX", "COR"};
        for (String agency : agencies) {
            agencyMetrics.put(agency, new AgencyMetrics(agency));
        }
    }
    
    /**
     * Start monitoring
     */
    public void startMonitoring() {
        this.isMonitoring = true;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * Stop monitoring
     */
    public void stopMonitoring() {
        this.isMonitoring = false;
    }
    
    /**
     * Update performance metrics for an agency
     */
    public void updateAgencyMetrics(String agency, double throughput, 
                                    double latency, double efficiency) {
        AgencyMetrics metrics = agencyMetrics.get(agency);
        if (metrics != null) {
            metrics.updateMetrics(throughput, latency, efficiency);
            recalculateSystemMetrics();
        }
    }
    
    /**
     * Recalculate system-wide metrics
     */
    private void recalculateSystemMetrics() {
        double totalThroughput = 0.0;
        double totalLatency = 0.0;
        double totalEfficiency = 0.0;
        
        for (AgencyMetrics metrics : agencyMetrics.values()) {
            totalThroughput += metrics.throughput;
            totalLatency += metrics.latency;
            totalEfficiency += metrics.efficiency;
        }
        
        int numAgencies = agencyMetrics.size();
        systemThroughput = totalThroughput / numAgencies;
        systemLatency = totalLatency / numAgencies;
        systemEfficiency = totalEfficiency / numAgencies;
        
        // Calculate phi-harmonic system health
        systemHealth = calculatePhiHealth(systemEfficiency, systemLatency);
        
        // Detect bottlenecks
        detectBottlenecks();
        
        // Record snapshot
        recordSnapshot();
    }
    
    /**
     * Calculate phi-harmonic health score
     */
    private double calculatePhiHealth(double efficiency, double latency) {
        // Health = efficiency * PHI^(-latency_factor)
        double latencyFactor = Math.min(latency / 1000.0, 2.0); // Normalize latency
        return efficiency * Math.pow(PHI, -latencyFactor);
    }
    
    /**
     * Detect performance bottlenecks
     */
    private void detectBottlenecks() {
        detectedBottlenecks.clear();
        
        for (Map.Entry<String, AgencyMetrics> entry : agencyMetrics.entrySet()) {
            String agency = entry.getKey();
            AgencyMetrics metrics = entry.getValue();
            
            // Check for high latency bottleneck
            if (metrics.latency > systemLatency * 1.5) {
                detectedBottlenecks.add(new Bottleneck(
                    agency, "HIGH_LATENCY", metrics.latency, 
                    metrics.latency / systemLatency
                ));
            }
            
            // Check for low efficiency bottleneck
            if (metrics.efficiency < bottleneckThreshold) {
                detectedBottlenecks.add(new Bottleneck(
                    agency, "LOW_EFFICIENCY", metrics.efficiency,
                    systemEfficiency / metrics.efficiency
                ));
            }
        }
    }
    
    /**
     * Record system snapshot
     */
    private void recordSnapshot() {
        SystemSnapshot snapshot = new SystemSnapshot(
            System.currentTimeMillis(),
            new HashMap<>(agencyMetrics),
            systemThroughput,
            systemLatency,
            systemEfficiency,
            systemHealth
        );
        
        performanceHistory.offer(snapshot);
        if (performanceHistory.size() > maxHistorySize) {
            performanceHistory.poll();
        }
        
        monitoringCycles++;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * Get performance report
     */
    public String getPerformanceReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SYSTEM PERFORMANCE REPORT ===\n");
        sb.append("Monitoring Cycles: ").append(monitoringCycles).append("\n");
        sb.append("Is Monitoring: ").append(isMonitoring).append("\n");
        sb.append("\n=== SYSTEM-WIDE METRICS ===\n");
        sb.append("System Throughput: ").append(String.format("%.3f", systemThroughput)).append(" ops/sec\n");
        sb.append("System Latency: ").append(String.format("%.3f", systemLatency)).append(" ms\n");
        sb.append("System Efficiency: ").append(String.format("%.3f", systemEfficiency)).append("\n");
        sb.append("System Health: ").append(String.format("%.3f", systemHealth)).append(" (phi-harmonic)\n");
        
        sb.append("\n=== AGENCY METRICS ===\n");
        for (Map.Entry<String, AgencyMetrics> entry : agencyMetrics.entrySet()) {
            sb.append(entry.getValue().toString()).append("\n");
        }
        
        sb.append("\n=== BOTTLENECKS ===\n");
        if (detectedBottlenecks.isEmpty()) {
            sb.append("No bottlenecks detected\n");
        } else {
            for (Bottleneck bottleneck : detectedBottlenecks) {
                sb.append(bottleneck.toString()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Get performance trend analysis
     */
    public String getTrendAnalysis() {
        if (performanceHistory.size() < 2) {
            return "Insufficient data for trend analysis";
        }
        
        double efficiencyTrend = 0.0;
        double latencyTrend = 0.0;
        
        SystemSnapshot[] snapshots = performanceHistory.toArray(new SystemSnapshot[0]);
        SystemSnapshot oldest = snapshots[0];
        SystemSnapshot newest = snapshots[snapshots.length - 1];
        
        efficiencyTrend = newest.systemEfficiency - oldest.systemEfficiency;
        latencyTrend = newest.systemLatency - oldest.systemLatency;
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFORMANCE TREND ANALYSIS ===\n");
        sb.append("Analysis Window: ").append(snapshots.length).append(" snapshots\n");
        sb.append("Efficiency Trend: ").append(String.format("%+.3f", efficiencyTrend));
        sb.append(efficiencyTrend > 0 ? " (improving)" : " (degrading)").append("\n");
        sb.append("Latency Trend: ").append(String.format("%+.3f", latencyTrend));
        sb.append(latencyTrend < 0 ? " (improving)" : " (degrading)").append("\n");
        
        return sb.toString();
    }
    
    // Getters
    public double getSystemHealth() { return systemHealth; }
    public double getSystemEfficiency() { return systemEfficiency; }
    public double getSystemLatency() { return systemLatency; }
    public double getSystemThroughput() { return systemThroughput; }
    public List<Bottleneck> getBottlenecks() { return new ArrayList<>(detectedBottlenecks); }
    public boolean isMonitoring() { return isMonitoring; }
    
    /**
     * AgencyMetrics class
     */
    public static class AgencyMetrics {
        String agency;
        double throughput;
        double latency;
        double efficiency;
        int taskCount;
        long lastUpdate;
        
        public AgencyMetrics(String agency) {
            this.agency = agency;
            this.throughput = 0.0;
            this.latency = 0.0;
            this.efficiency = 1.0;
            this.taskCount = 0;
            this.lastUpdate = System.currentTimeMillis();
        }
        
        public void updateMetrics(double throughput, double latency, double efficiency) {
            // Exponential moving average
            double alpha = 0.2;
            this.throughput = alpha * throughput + (1 - alpha) * this.throughput;
            this.latency = alpha * latency + (1 - alpha) * this.latency;
            this.efficiency = alpha * efficiency + (1 - alpha) * this.efficiency;
            this.taskCount++;
            this.lastUpdate = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format("%s: throughput=%.3f ops/sec, latency=%.3f ms, efficiency=%.3f, tasks=%d",
                agency, throughput, latency, efficiency, taskCount);
        }
    }
    
    /**
     * SystemSnapshot class
     */
    private static class SystemSnapshot {
        long timestamp;
        Map<String, AgencyMetrics> agencyMetrics;
        double systemThroughput;
        double systemLatency;
        double systemEfficiency;
        double systemHealth;
        
        public SystemSnapshot(long timestamp, Map<String, AgencyMetrics> agencyMetrics,
                            double systemThroughput, double systemLatency,
                            double systemEfficiency, double systemHealth) {
            this.timestamp = timestamp;
            this.agencyMetrics = new HashMap<>(agencyMetrics);
            this.systemThroughput = systemThroughput;
            this.systemLatency = systemLatency;
            this.systemEfficiency = systemEfficiency;
            this.systemHealth = systemHealth;
        }
    }
    
    /**
     * Bottleneck class
     */
    public static class Bottleneck {
        String agency;
        String type;
        double value;
        double severity;
        
        public Bottleneck(String agency, String type, double value, double severity) {
            this.agency = agency;
            this.type = type;
            this.value = value;
            this.severity = severity;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s (value=%.3f, severity=%.2fx)", 
                agency, type, value, severity);
        }
    }
}
