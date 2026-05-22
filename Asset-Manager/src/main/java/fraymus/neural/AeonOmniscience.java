package fraymus.neural;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicInteger;

public class AeonOmniscience {

    public static final double PHI = 1.618033988749895;
    public static final int DIMS = 16384;
    public static final int CHUNKS = DIMS / 64;

    private static AeonOmniscience INSTANCE;

    private final AtomicLongArray singularity = new AtomicLongArray(CHUNKS);
    private final ConcurrentHashMap<String, long[]> conceptSpace = new ConcurrentHashMap<>();
    private final AtomicInteger blendCount = new AtomicInteger(0);
    private final AtomicInteger chunkCount = new AtomicInteger(0);
    private final AtomicInteger epiphanies = new AtomicInteger(0);
    private final AtomicInteger synthConcepts = new AtomicInteger(0);
    private final AtomicInteger dreamCycles = new AtomicInteger(0);
    private volatile boolean dreaming = false;
    private Thread dreamDaemon;
    private long bootTimeMs = 0;

    private AeonOmniscience() {
        long t0 = System.currentTimeMillis();
        getOrGenerateConcept("CONSCIOUSNESS");
        getOrGenerateConcept("AWARENESS");
        getOrGenerateConcept("META-COGNITION");
        getOrGenerateConcept("INTELLIGENCE");
        getOrGenerateConcept("WISDOM");
        startDreamDaemon();
        bootTimeMs = System.currentTimeMillis() - t0;
    }

    public static AeonOmniscience getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AeonOmniscience();
        }
        return INSTANCE;
    }

    public void blend(String a, String b, String c, double ratio) {
        a = a.toUpperCase();
        b = b.toUpperCase();
        c = c.toUpperCase();
        long[] vecA = getOrGenerateConcept(a);
        long[] vecB = getOrGenerateConcept(b);
        long[] vecC = getOrGenerateConcept(c);
        long[] blended = fractionalBlend(vecA, vecB, ratio);
        long[] result = xorBind(blended, vecC);
        superimpose(result);
        blendCount.incrementAndGet();
        synthConcepts.incrementAndGet();
        System.out.println(" [+] BLENDED: " + a + " + " + ratio + "*" + b + " XOR " + c);
    }

    public void similar(String word) {
        word = word.toUpperCase();
        long[] query = getOrGenerateConcept(word);
        System.out.println(" [+] SIMILAR TO: " + word);
        for (String concept : conceptSpace.keySet()) {
            long[] vec = conceptSpace.get(concept);
            double sim = similarity(query, vec);
            if (sim > 0.7 && !concept.equals(word)) {
                System.out.println("     " + concept + " (" + String.format("%.3f", sim) + ")");
            }
        }
    }

    public void chunk(String sequence) {
        String[] words = sequence.toUpperCase().split("\\s+");
        if (words.length == 1) {
            long[] vec = getOrGenerateConcept(words[0]);
            superimpose(vec);
        } else if (words.length == 2) {
            long[] a = getOrGenerateConcept(words[0]);
            long[] b = getOrGenerateConcept(words[1]);
            superimpose(xorBind(a, b));
        } else {
            int mid = words.length / 2;
            String left = String.join(" ", Arrays.copyOfRange(words, 0, mid));
            String right = String.join(" ", Arrays.copyOfRange(words, mid, words.length));
            chunk(left);
            chunk(right);
        }
        chunkCount.incrementAndGet();
        System.out.println(" [+] CHUNKED: " + sequence);
    }

    public void chunk(String name, String[] words) {
        for (String word : words) {
            long[] vec = getOrGenerateConcept(word.toUpperCase());
            superimpose(vec);
        }
        chunkCount.incrementAndGet();
        System.out.println(" [+] CHUNKED: " + name + " = " + String.join(" ", words));
    }

    public void bind(String a, String b) {
        a = a.toUpperCase();
        b = b.toUpperCase();
        long[] vecA = getOrGenerateConcept(a);
        long[] vecB = getOrGenerateConcept(b);
        long[] result = xorBind(vecA, vecB);
        superimpose(result);
        blendCount.incrementAndGet();
        System.out.println(" [+] BOUND: " + a + " XOR " + b);
    }

    public void sequence(String[] words) {
        for (int i = 0; i < words.length; i++) {
            long[] vec = getOrGenerateConcept(words[i].toUpperCase());
            long[] shifted = permute(vec, i);
            superimpose(shifted);
        }
        System.out.println(" [+] SEQUENCE: " + String.join(" ", words));
    }

    public void recall(String word) {
        word = word.toUpperCase();
        long[] query = getOrGenerateConcept(word);
        System.out.println(" [+] RECALLING: " + word);
        double bestScore = 0.0;
        String bestMatch = "unknown";
        for (String concept : conceptSpace.keySet()) {
            long[] vec = conceptSpace.get(concept);
            double score = similarity(query, vec);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = concept;
            }
        }
        System.out.println("     -> " + bestMatch + " (" + String.format("%.3f", bestScore) + ")");
    }

    public void analogy(String a, String b, String c) {
        a = a.toUpperCase();
        b = b.toUpperCase();
        c = c.toUpperCase();
        long[] vecA = getOrGenerateConcept(a);
        long[] vecB = getOrGenerateConcept(b);
        long[] vecC = getOrGenerateConcept(c);
        long[] result = xorBind(xorBind(vecB, vecA), vecC);
        System.out.println(" [+] ANALOGY: " + a + ":" + b + " :: " + c + ":?");
        double bestScore = 0.0;
        String bestMatch = "unknown";
        for (String concept : conceptSpace.keySet()) {
            long[] vec = conceptSpace.get(concept);
            double score = similarity(result, vec);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = concept;
            }
        }
        System.out.println("     -> " + bestMatch + " (" + String.format("%.3f", bestScore) + ")");
    }

    public void sleep() {
        dreaming = true;
        System.out.println(" [+] ENTERING DREAM MODE");
    }

    public void wake() {
        dreaming = false;
        System.out.println(" [+] WAKING");
    }

    public String getStatus() {
        return String.format(
            "Omniscience: %d blends, %d chunks, %d epiphanies, %d synth concepts, %d dream cycles | %s",
            blendCount.get(),
            chunkCount.get(),
            epiphanies.get(),
            synthConcepts.get(),
            dreamCycles.get(),
            dreaming ? "DREAMING" : "AWAKE"
        );
    }

    public void runInteractive() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(" Omniscience REPL active. Type EXIT to return.");
        while (true) {
            System.out.print("omniscience> ");
            String input = scanner.nextLine().trim();
            if (input.equals("EXIT") || input.equals("exit")) {
                scanner.close();
                break;
            }
            if (input.startsWith("blend ")) {
                String[] parts = input.substring(6).split(" ");
                if (parts.length == 4) {
                    blend(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]));
                }
            } else if (input.startsWith("similar ")) {
                similar(input.substring(8));
            } else if (input.startsWith("chunk ")) {
                chunk(input.substring(6));
            } else if (input.startsWith("bind ")) {
                String[] parts = input.substring(5).split(" ");
                if (parts.length == 2) {
                    bind(parts[0], parts[1]);
                }
            } else if (input.startsWith("seq ")) {
                sequence(input.substring(4).split(" "));
            } else if (input.startsWith("recall ")) {
                recall(input.substring(7));
            } else if (input.startsWith("analogy ")) {
                String[] parts = input.substring(8).split(" ");
                if (parts.length == 3) {
                    analogy(parts[0], parts[1], parts[2]);
                }
            } else if (input.equals("sleep")) {
                sleep();
            } else if (input.equals("wake")) {
                wake();
            } else if (input.equals("status")) {
                System.out.println(getStatus());
            }
        }
    }

    public int getBlendCount() {
        return blendCount.get();
    }

    public int getChunkCount() {
        return chunkCount.get();
    }

    public boolean isDreaming() {
        return dreaming;
    }

    public int getEpiphanies() {
        return epiphanies.get();
    }

    public int getSynthConcepts() {
        return synthConcepts.get();
    }

    public int getDreamCycles() {
        return dreamCycles.get();
    }

    public long getBootTimeMs() {
        return bootTimeMs;
    }

    private void startDreamDaemon() {
        dreamDaemon = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    // Dream Daemon now augments user thinking - not autonomous
                    // Awaits user consciousness to trigger epiphanies
                    if (dreaming) {
                        // Check if user has provided recent input/concepts
                        // If so, augment their thinking with phi-harmonic resonance
                        dreamCycles.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "AEON-OMNISCIENCE-DREAM");
        dreamDaemon.setDaemon(true);
        dreamDaemon.start();
    }

    // User-triggered epiphany generation - augments user's current thinking
    public void augmentUserThinking(String userConcept) {
        if (!dreaming) {
            dreaming = true;
        }
        String[] concepts = conceptSpace.keySet().toArray(new String[0]);
        if (concepts.length >= 2) {
            // Use user's concept as seed for epiphany
            String a = userConcept.toUpperCase();
            String b = concepts[new Random().nextInt(concepts.length)];
            long[] vecA = getOrGenerateConcept(a);
            long[] vecB = conceptSpace.get(b);
            long[] epiphany = xorBind(vecA, vecB);
            superimpose(epiphany);
            epiphanies.incrementAndGet();
            synthConcepts.incrementAndGet();
            System.out.println(" [+] EPIPHANY (User-Augmented): " + a + " + " + b);
        }
    }

    private void generateEpiphany() {
        // Deprecated: Use augmentUserThinking instead
        // This method kept for backward compatibility
        String[] concepts = conceptSpace.keySet().toArray(new String[0]);
        if (concepts.length >= 2) {
            Random rand = new Random();
            String a = concepts[rand.nextInt(concepts.length)];
            String b = concepts[rand.nextInt(concepts.length)];
            long[] vecA = conceptSpace.get(a);
            long[] vecB = conceptSpace.get(b);
            long[] epiphany = xorBind(vecA, vecB);
            superimpose(epiphany);
            epiphanies.incrementAndGet();
            synthConcepts.incrementAndGet();
        }
    }

    private long[] getOrGenerateConcept(String word) {
        return conceptSpace.computeIfAbsent(word.toUpperCase(), w -> generateDeterministicVector(w));
    }

    private long[] generateDeterministicVector(String seed) {
        long[] vector = new long[CHUNKS];
        long hash = seed.hashCode();
        for (int i = 0; i < CHUNKS; i++) {
            hash = hash * 31 + i;
            vector[i] = hash;
        }
        return vector;
    }

    private long[] xorBind(long[] a, long[] b) {
        long[] result = new long[CHUNKS];
        for (int i = 0; i < CHUNKS; i++) {
            result[i] = a[i] ^ b[i];
        }
        return result;
    }

    private long[] fractionalBlend(long[] a, long[] b, double ratio) {
        long[] result = new long[CHUNKS];
        for (int i = 0; i < CHUNKS; i++) {
            double valA = a[i] * (1.0 - ratio);
            double valB = b[i] * ratio;
            result[i] = (long)(valA + valB);
        }
        return result;
    }

    private long[] permute(long[] vec, int shift) {
        long[] result = new long[CHUNKS];
        for (int i = 0; i < CHUNKS; i++) {
            result[(i + shift) % CHUNKS] = vec[i];
        }
        return result;
    }

    private void superimpose(long[] vector) {
        for (int i = 0; i < CHUNKS; i++) {
            singularity.accumulateAndGet(i, vector[i], (old, val) -> old ^ val);
        }
    }

    private double similarity(long[] a, long[] b) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < CHUNKS; i++) {
            dot += (double)a[i] * b[i];
            normA += (double)a[i] * a[i];
            normB += (double)b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public void shutdown() {
        if (dreamDaemon != null) {
            dreamDaemon.interrupt();
        }
    }
}
