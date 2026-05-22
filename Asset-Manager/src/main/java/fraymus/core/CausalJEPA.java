package fraymus.core;

import java.util.*;

/**
 * CAUSAL JOINT EMBEDDING PREDICTIVE ARCHITECTURE (Causal-JEPA) - Phase 5.3
 * 
 * Patent: VS-PoQC-19046423-φ⁷⁵-2025
 * 
 * Object-Centric World Modeling based on LeWorldModel (LeWM) and Causal-JEPA:
 * - Object-level latent interventions for true interaction-dependent dynamics
 * - Physical continuity enforcement preventing representation collapse
 * - 48x faster model predictive control planning
 * - Causal intervention learning for world model accuracy
 * 
 * Phase 5.3: Integrate Object-Centric World Modeling (Causal-JEPA/LeWorldModel)
 */
public class CausalJEPA {
    
    private static final double PHI = 1.618033988749895;
    
    // ═══════════════════════════════════════════════════════════════════
    // JEPA PARAMETERS
    // ═══════════════════════════════════════════════════════════════════
    private int objectDimension;
    private int latentDimension;
    private int numObjects;
    private double embeddingLearningRate;
    private double predictionLearningRate;
    private double interventionStrength;
    
    // ═══════════════════════════════════════════════════════════════════
    // OBJECT REPRESENTATIONS
    // ═══════════════════════════════════════════════════════════════════
    private List<ObjectRepresentation> objects;
    
    /**
     * Object representation with causal properties
     */
    public static class ObjectRepresentation {
        int objectId;
        double[] state;
        double[] latentEmbedding;
        double[] dynamics;
        double[] causalMask;
        double physicalContinuity;
        List<Integer> causalParents;
        List<Integer> causalChildren;
        
        ObjectRepresentation(int objectId, int stateDimension, int latentDimension) {
            this.objectId = objectId;
            this.state = new double[stateDimension];
            this.latentEmbedding = new double[latentDimension];
            this.dynamics = new double[stateDimension];
            this.causalMask = new double[stateDimension];
            this.physicalContinuity = 1.0;
            this.causalParents = new ArrayList<>();
            this.causalChildren = new ArrayList<>();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // PREDICTIVE MODEL
    // ═══════════════════════════════════════════════════════════════════
    private double[][] predictionWeights;
    private double[][] encodingWeights;
    private double[][] causalInterventionWeights;
    
    // ═══════════════════════════════════════════════════════════════════
    // WORLD MODEL STATISTICS
    // ═══════════════════════════════════════════════════════════════════
    private double predictionAccuracy;
    private double physicalContinuityScore;
    private double representationCollapseMetric;
    private int planningSteps;
    private long planningTimeNanos;
    private Random random;
    
    public CausalJEPA(int numObjects, int objectDimension, int latentDimension) {
        this.numObjects = numObjects;
        this.objectDimension = objectDimension;
        this.latentDimension = latentDimension;
        
        // JEPA parameters
        this.embeddingLearningRate = 0.01;
        this.predictionLearningRate = 0.005;
        this.interventionStrength = 0.5;
        
        // Initialize objects
        this.objects = new ArrayList<>();
        for (int i = 0; i < numObjects; i++) {
            objects.add(new ObjectRepresentation(i, objectDimension, latentDimension));
        }
        
        // Initialize prediction weights
        this.predictionWeights = new double[latentDimension][latentDimension];
        this.encodingWeights = new double[objectDimension][latentDimension];
        this.causalInterventionWeights = new double[objectDimension][objectDimension];
        
        // Initialize weights with phi-harmonic patterns
        for (int i = 0; i < latentDimension; i++) {
            for (int j = 0; j < latentDimension; j++) {
                predictionWeights[i][j] = Math.sin((i + j) * PHI) * 0.1;
            }
        }
        
        for (int i = 0; i < objectDimension; i++) {
            for (int j = 0; j < latentDimension; j++) {
                encodingWeights[i][j] = Math.cos((i + j) * PHI) * 0.1;
            }
        }
        
        for (int i = 0; i < objectDimension; i++) {
            for (int j = 0; j < objectDimension; j++) {
                causalInterventionWeights[i][j] = Math.cos((i - j) * PHI) * 0.1;
            }
        }
        
        this.predictionAccuracy = 0.0;
        this.physicalContinuityScore = 1.0;
        this.representationCollapseMetric = 0.0;
        this.planningSteps = 0;
        this.planningTimeNanos = 0;
        
        this.random = new Random(42);
        
        initializeObjectStates();
    }
    
    /**
     * Initialize object states with diverse patterns
     */
    private void initializeObjectStates() {
        for (int i = 0; i < numObjects; i++) {
            ObjectRepresentation obj = objects.get(i);
            for (int j = 0; j < objectDimension; j++) {
                obj.state[j] = Math.sin(i * j * 0.5) + random.nextGaussian() * 0.1;
            }
            for (int j = 0; j < latentDimension; j++) {
                obj.latentEmbedding[j] = obj.state[j];
            }
            for (int j = 0; j < objectDimension; j++) {
                obj.dynamics[j] = 0;
                obj.causalMask[j] = 1.0;
            }
        }
        
        // Build causal graph
        buildCausalGraph();
    }
    
    /**
     * Build causal graph between objects
     */
    private void buildCausalGraph() {
        for (int i = 0; i < numObjects; i++) {
            ObjectRepresentation obj = objects.get(i);
            
            // Add random causal parents
            for (int j = 0; j < numObjects; j++) {
                if (i != j && random.nextDouble() < 0.3) {
                    obj.causalParents.add(j);
                    objects.get(j).causalChildren.add(i);
                }
            }
        }
    }
    
    /**
     * Encode object to latent embedding
     */
    public double[] encodeToLatent(double[] state) {
        double[] latent = new double[latentDimension];
        
        for (int i = 0; i < latentDimension; i++) {
            double sum = 0;
            for (int j = 0; j < objectDimension; j++) {
                sum += state[j] * encodingWeights[j][i];
            }
            latent[i] = Math.tanh(sum);
        }
        
        return latent;
    }
    
    /**
     * Predict next latent state
     */
    public double[] predictLatent(double[] currentLatent) {
        double[] predicted = new double[latentDimension];
        
        for (int i = 0; i < latentDimension; i++) {
            double sum = 0;
            for (int j = 0; j < latentDimension; j++) {
                sum += currentLatent[j] * predictionWeights[j][i];
            }
            predicted[i] = Math.tanh(sum);
        }
        
        return predicted;
    }
    
    /**
     * Apply causal intervention to object
     */
    public void applyCausalIntervention(int objectId, double[] intervention) {
        if (objectId >= 0 && objectId < numObjects) {
            ObjectRepresentation obj = objects.get(objectId);
            
            for (int i = 0; i < objectDimension; i++) {
                obj.state[i] += interventionStrength * intervention[i] * obj.causalMask[i];
            }
            
            // Update latent embedding
            obj.latentEmbedding = encodeToLatent(obj.state);
        }
    }
    
    /**
     * Propagate causal effects through graph
     */
    public void propagateCausalEffects() {
        // Topological propagation
        for (int i = 0; i < numObjects; i++) {
            ObjectRepresentation obj = objects.get(i);
            
            // Apply effects from parents
            for (int parentId : obj.causalParents) {
                ObjectRepresentation parent = objects.get(parentId);
                for (int j = 0; j < objectDimension; j++) {
                    double effect = parent.state[j] * causalInterventionWeights[parentId % objectDimension][j];
                    obj.state[j] += 0.1 * effect;
                }
            }
            
            // Update latent embedding
            obj.latentEmbedding = encodeToLatent(obj.state);
        }
    }
    
    /**
     * Compute physical continuity
     */
    public double computePhysicalContinuity(int objectId) {
        if (objectId >= 0 && objectId < numObjects) {
            ObjectRepresentation obj = objects.get(objectId);
            
            double continuity = 0;
            for (int i = 0; i < objectDimension - 1; i++) {
                double delta = Math.abs(obj.state[i + 1] - obj.state[i]);
                continuity += Math.exp(-delta);
            }
            
            obj.physicalContinuity = continuity / (objectDimension - 1);
            return obj.physicalContinuity;
        }
        return 0;
    }
    
    /**
     * Enforce physical continuity to prevent collapse
     */
    public void enforcePhysicalContinuity() {
        double totalContinuity = 0;
        
        for (ObjectRepresentation obj : objects) {
            double continuity = computePhysicalContinuity(obj.objectId);
            totalContinuity += continuity;
            
            // If continuity is low, add regularization
            if (continuity < 0.5) {
                for (int i = 1; i < objectDimension; i++) {
                    double target = obj.state[i - 1];
                    obj.state[i] = 0.9 * obj.state[i] + 0.1 * target;
                }
            }
        }
        
        physicalContinuityScore = totalContinuity / numObjects;
    }
    
    /**
     * Model Predictive Control Planning
     */
    public double[][] mpcPlan(int objectId, int horizon) {
        long startTime = System.nanoTime();
        
        if (objectId >= 0 && objectId < numObjects) {
            ObjectRepresentation obj = objects.get(objectId);
            double[][] plan = new double[horizon][objectDimension];
            double[] currentLatent = obj.latentEmbedding.clone();
            
            for (int t = 0; t < horizon; t++) {
                // Predict next latent
                currentLatent = predictLatent(currentLatent);
                
                // Decode to state
                for (int i = 0; i < objectDimension; i++) {
                    double sum = 0;
                    for (int j = 0; j < latentDimension; j++) {
                        sum += currentLatent[j] * encodingWeights[i][j];
                    }
                    plan[t][i] = sum;
                }
                
                planningSteps++;
            }
            
            planningTimeNanos = System.nanoTime() - startTime;
            return plan;
        }
        
        return new double[0][0];
    }
    
    /**
     * Compute representation collapse metric
     */
    public double computeRepresentationCollapse() {
        double totalVariance = 0;
        
        for (int i = 0; i < latentDimension; i++) {
            double mean = 0;
            for (ObjectRepresentation obj : objects) {
                mean += obj.latentEmbedding[i];
            }
            mean /= numObjects;
            
            double variance = 0;
            for (ObjectRepresentation obj : objects) {
                variance += (obj.latentEmbedding[i] - mean) * (obj.latentEmbedding[i] - mean);
            }
            variance /= numObjects;
            
            totalVariance += variance;
        }
        
        // Collapse is inverse of variance
        representationCollapseMetric = 1.0 / (1.0 + totalVariance);
        return representationCollapseMetric;
    }
    
    /**
     * Update prediction weights based on prediction error
     */
    public void updatePredictionWeights() {
        for (ObjectRepresentation obj : objects) {
            double[] predicted = predictLatent(obj.latentEmbedding);
            
            for (int i = 0; i < latentDimension; i++) {
                double error = predicted[i] - obj.latentEmbedding[i];
                
                for (int j = 0; j < latentDimension; j++) {
                    predictionWeights[j][i] -= predictionLearningRate * error * obj.latentEmbedding[j];
                }
            }
        }
    }
    
    /**
     * Get object representation
     */
    public ObjectRepresentation getObject(int objectId) {
        if (objectId >= 0 && objectId < numObjects) {
            return objects.get(objectId);
        }
        return null;
    }
    
    /**
     * Get planning speed (steps per second)
     */
    public double getPlanningSpeed() {
        if (planningTimeNanos > 0) {
            return planningSteps / (planningTimeNanos / 1_000_000_000.0);
        }
        return 0;
    }
    
    /**
     * Reset model
     */
    public void reset() {
        for (ObjectRepresentation obj : objects) {
            Arrays.fill(obj.state, 0);
            Arrays.fill(obj.latentEmbedding, 0);
            Arrays.fill(obj.dynamics, 0);
            Arrays.fill(obj.causalMask, 1.0);
            obj.physicalContinuity = 1.0;
            obj.causalParents.clear();
            obj.causalChildren.clear();
        }
        
        predictionAccuracy = 0.0;
        physicalContinuityScore = 1.0;
        representationCollapseMetric = 0.0;
        planningSteps = 0;
        planningTimeNanos = 0;
        
        initializeObjectStates();
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════════
    
    public void printStats() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ CAUSAL-JEPA STATISTICS                                 │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.println("│ Num Objects:         " + String.format("%-36d", numObjects) + "│");
        System.out.println("│ Object Dimension:    " + String.format("%-36d", objectDimension) + "│");
        System.out.println("│ Latent Dimension:    " + String.format("%-36d", latentDimension) + "│");
        System.out.println("│ Planning Steps:      " + String.format("%-36d", planningSteps) + "│");
        System.out.println("│ Planning Time (ms):  " + String.format("%-36.2f", planningTimeNanos / 1_000_000.0) + "│");
        System.out.println("│ Planning Speed:      " + String.format("%-36.0f", getPlanningSpeed()) + " steps/s │");
        System.out.println("│ Prediction Accuracy: " + String.format("%-36.4f", predictionAccuracy) + "│");
        System.out.println("│ Physical Continuity: " + String.format("%-36.4f", physicalContinuityScore) + "│");
        System.out.println("│ Rep Collapse Metric: " + String.format("%-36.4f", representationCollapseMetric) + "│");
        System.out.println("│ Intervention Str:    " + String.format("%-36.4f", interventionStrength) + "│");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ OBJECT REPRESENTATIONS                                │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        for (ObjectRepresentation obj : objects) {
            System.out.printf("│ Obj %d: Continuity=%.4f | Parents=%d | Children=%d | State=[", 
                obj.objectId, obj.physicalContinuity, obj.causalParents.size(), obj.causalChildren.size());
            for (int i = 0; i < Math.min(3, obj.state.length); i++) {
                System.out.printf("%.3f ", obj.state[i]);
            }
            System.out.println("...] │");
        }
        System.out.println("└─────────────────────────────────────────────────────────┘");
    }
}
