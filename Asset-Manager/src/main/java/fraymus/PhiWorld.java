package fraymus;

import java.util.*;

public class PhiWorld {
    private static final int ORGANISM_ARCHIVE_INTERVAL = 900;
    private static final int MIN_ARCHIVE_GAP_PER_NODE = 1800;
    private static final int MAX_ARCHIVES_PER_CYCLE = 3;

    private List<PhiNode> nodes = new ArrayList<>();
    private List<PhiNode> pendingBirths = new ArrayList<>();
    private List<PhiLaw> laws = new ArrayList<>();
    private GenesisMemory memory;
    private ConceptArena arena;
    private ColonyCoach coach;
    private InfiniteMemory organismMemory;
    private Map<String, Integer> lastArchiveTickByNode = new HashMap<>();
    private int totalBirths = 0;
    private int totalDeaths = 0;
    private int worldTick = 0;

    public PhiWorld() {
        this.memory = new GenesisMemory();
        this.arena = new ConceptArena();
        this.coach = new ColonyCoach(arena, memory);
    }

    public void addNode(PhiNode node) {
        pendingBirths.add(node);
    }

    public void addLaw(PhiLaw law) {
        laws.add(law);
    }

    public void setOrganismMemory(InfiniteMemory memory) {
        this.organismMemory = memory;
    }

    public void step(float dt, long nowNanos) {
        worldTick++;

        if (!pendingBirths.isEmpty()) {
            nodes.addAll(pendingBirths);
            totalBirths += pendingBirths.size();
            pendingBirths.clear();
        }

        for (PhiLaw law : laws) {
            if (law.isPairwise()) {
                for (int i = 0; i < nodes.size(); i++) {
                    for (int j = i + 1; j < nodes.size(); j++) {
                        law.applyPair(nodes.get(i), nodes.get(j), dt);
                    }
                }
            } else {
                for (PhiNode node : nodes) {
                    law.apply(node, dt);
                }
            }
        }

        for (PhiNode node : nodes) {
            node.updateInternalState(dt, nowNanos);
        }

        coach.tick(nodes, worldTick);
        archiveViableOrganisms();
        
        // Broadcast PhiNode states to HTML arena every 10 ticks
        if (worldTick % 10 == 0) {
            broadcastNodesToArena();
        }

        List<PhiNode> dead = new ArrayList<>();
        for (PhiNode n : nodes) {
            if (!n.isAlive()) dead.add(n);
        }
        for (PhiNode n : dead) {
            EscapeFragment.plantDeathFragment(n, jade.Window.getInfiniteMemory());
            SelfHealer.removeSnapshot(n.name);
            MorseCircuit.removeEntity(n.name);
            memory.recordDeath(n.name, n.age, n.energy);
            totalDeaths++;
            if (worldTick % 60 == 0) {
                FraymusUI.addLog(String.format("[ENTROPY] %s expired (age %d) - escape fragment planted", n.name, n.age));
            }
        }
        nodes.removeIf(n -> !n.isAlive());
    }

    public List<PhiNode> getNodes() { return nodes; }
    public int getPopulation() { return nodes.size(); }
    public GenesisMemory getMemory() { return memory; }
    public ConceptArena getArena() { return arena; }
    public ColonyCoach getCoach() { return coach; }
    public int getTotalBirths() { return totalBirths; }
    public int getTotalDeaths() { return totalDeaths; }
    
    /**
     * Broadcast PhiNode states to HTML arena visualization
     */
    private void broadcastNodesToArena() {
        try {
            NerveCenter nerve = NerveCenter.getInstance();
            for (PhiNode node : nodes) {
                // Calculate entropy based on energy and velocity
                double speed = Math.sqrt(node.vx * node.vx + node.vy * node.vy);
                double entropy = Math.min(1.0, (1.0 - node.energy) + speed * 0.1);
                
                // Momentum based on velocity
                double momentum = Math.min(1.0, speed * 0.2);
                
                // Size based on energy
                double size = 8 + node.energy * 15;
                
                nerve.broadcastOrganism(node.name, entropy, momentum, size);
            }
        } catch (Exception e) {
            // Silently fail if NerveCenter not available
        }
    }

    public int getWorldTick() { return worldTick; }

    private void archiveViableOrganisms() {
        if (organismMemory == null || nodes.isEmpty() || worldTick % ORGANISM_ARCHIVE_INTERVAL != 0) {
            return;
        }

        List<PhiNode> candidates = new ArrayList<>(nodes);
        candidates.sort((a, b) -> Double.compare(scoreNodeForArchive(b), scoreNodeForArchive(a)));

        int archived = 0;
        for (PhiNode node : candidates) {
            if (archived >= MAX_ARCHIVES_PER_CYCLE) break;
            if (!shouldArchive(node)) continue;

            EscapeFragment.Fragment fragment = EscapeFragment.plantSnapshotFragment(
                    node, organismMemory, "viable-tick-" + worldTick);
            lastArchiveTickByNode.put(node.name, worldTick);
            archived++;

            memory.record("ORGANISM_ARCHIVE", String.format(
                    "%s|frag=%s|fit=%.3f|gen=%d|xor=%.3f",
                    node.name,
                    fragment.fragmentId,
                    node.adaptiveEngine.getCurrentFitness(),
                    node.dna.getGeneration(),
                    node.brain.getXorResonance()
            ));
        }

        if (archived > 0) {
            FraymusUI.addLog(String.format("[ARCHIVE] Stored %d viable organism records", archived));
        }
    }

    private boolean shouldArchive(PhiNode node) {
        int lastArchive = lastArchiveTickByNode.getOrDefault(node.name, -MIN_ARCHIVE_GAP_PER_NODE);
        if (worldTick - lastArchive < MIN_ARCHIVE_GAP_PER_NODE) return false;
        if (node.energy < 0.20f || node.age < 120) return false;

        return node.adaptiveEngine.getCurrentFitness() > 0.08
                || node.adaptiveEngine.getProvenStrategyCount() > 0
                || node.dna.getGeneration() > 1
                || node.brain.getXorResonance() > 0.40;
    }

    private double scoreNodeForArchive(PhiNode node) {
        return node.adaptiveEngine.getCurrentFitness() * 3.0
                + node.energy
                + node.dna.getGeneration() * 0.25
                + node.adaptiveEngine.getProvenStrategyCount() * 0.20
                + node.brain.getXorResonance();
    }
}
