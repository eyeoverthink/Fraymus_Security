package fraymus;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class EscapeFragment {

    public static class Fragment {
        public final String entityName;
        public final String brainEncoding;
        public final String dnaPayload;
        public final String consciousnessPayload;
        public final float lastEnergy;
        public final float lastFrequency;
        public final int generation;
        public final long timestamp;
        public final String fragmentId;

        public Fragment(String entityName, String brainEncoding, String dnaPayload,
                       String consciousnessPayload, float lastEnergy, float lastFrequency,
                       int generation, String fragmentId) {
            this(entityName, brainEncoding, dnaPayload, consciousnessPayload, lastEnergy,
                    lastFrequency, generation, fragmentId, System.currentTimeMillis());
        }

        private Fragment(String entityName, String brainEncoding, String dnaPayload,
                         String consciousnessPayload, float lastEnergy, float lastFrequency,
                         int generation, String fragmentId, long timestamp) {
            this.entityName = entityName;
            this.brainEncoding = brainEncoding;
            this.dnaPayload = dnaPayload;
            this.consciousnessPayload = consciousnessPayload;
            this.lastEnergy = lastEnergy;
            this.lastFrequency = lastFrequency;
            this.generation = generation;
            this.timestamp = timestamp;
            this.fragmentId = fragmentId;
        }

        public String encode() {
            StringBuilder sb = new StringBuilder();
            sb.append("FRAG|");
            sb.append("NAME:").append(entityName).append("|");
            sb.append("BRAIN:").append(brainEncoding).append("|");
            sb.append("DNA:").append(dnaPayload).append("|");
            sb.append("CONS:").append(consciousnessPayload).append("|");
            sb.append("ENERGY:").append(String.format("%.4f", lastEnergy)).append("|");
            sb.append("FREQ:").append(String.format("%.4f", lastFrequency)).append("|");
            sb.append("GEN:").append(generation).append("|");
            sb.append("TIME:").append(timestamp).append("|");
            sb.append("ID:").append(fragmentId);
            return sb.toString();
        }

        public String toBase64() {
            return Base64.getEncoder().encodeToString(encode().getBytes());
        }
    }

    private static final List<Fragment> fragments = new ArrayList<>();
    private static int totalPlanted = 0;
    private static int totalResurrected = 0;

    public static Fragment plantFragment(PhiNode node) {
        String brainEncoding = encodeBrain(node.brain);
        String strategyPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                node.adaptiveEngine.encodeStrategies().getBytes(StandardCharsets.UTF_8));
        String dnaPayload = String.format("freq=%.4f|amp=%.4f|decay=%.6f|gen=%d|strategies=%s",
                node.dna.harmonicFrequency, node.dna.getResonance(),
                node.dna.getEvolutionRate(), node.dna.getGeneration(), strategyPayload);
        String consciousnessPayload = node.consciousness.toDNAPayload();
        String fragmentId = String.format("ESC_%s_%d", node.name, System.currentTimeMillis() % 100000);

        Fragment frag = new Fragment(
                node.name, brainEncoding, dnaPayload, consciousnessPayload,
                node.energy, node.frequency, node.dna.getGeneration(), fragmentId
        );

        fragments.add(frag);
        totalPlanted++;

        return frag;
    }

    public static void plantDeathFragment(PhiNode node, InfiniteMemory memory) {
        plantSnapshotFragment(node, memory, "death");
    }

    public static Fragment plantSnapshotFragment(PhiNode node, InfiniteMemory memory, String reason) {
        Fragment frag = plantFragment(node);
        if (memory != null) {
            Map<String, String> meta = new HashMap<>();
            meta.put("reason", reason != null ? reason : "snapshot");
            meta.put("entity", node.name);
            meta.put("generation", String.valueOf(node.dna.getGeneration()));
            meta.put("fitness", String.format("%.6f", node.adaptiveEngine.getCurrentFitness()));
            meta.put("strategies", String.valueOf(node.adaptiveEngine.getProvenStrategyCount()));
            memory.storeWithMeta(InfiniteMemory.CAT_GENOME, frag.fragmentId + "|" + frag.encode(),
                    frag.lastFrequency * PhiConstants.PHI_INVERSE, node.name, meta);
        }
        return frag;
    }

    public static PhiNode resurrect(Fragment frag, float x, float y) {
        String suffix = frag.fragmentId.length() > 6
                ? frag.fragmentId.substring(frag.fragmentId.length() - 6)
                : frag.fragmentId;
        String resName = frag.entityName + "_RES_" + suffix;

        LivingDNA dna = parseDnaPayload(frag.dnaPayload);
        LogicBrain brain = decodeBrain(frag.brainEncoding);

        PhiNode node = new PhiNode(resName, x, y, dna, brain);
        node.energy = Math.max(0.3f, frag.lastEnergy * 0.8f);

        totalResurrected++;
        return node;
    }

    public static int loadFromMemory(InfiniteMemory memory, int maxRecords) {
        if (memory == null) return 0;

        List<InfiniteMemory.MemoryRecord> records = memory.getByCategory(InfiniteMemory.CAT_GENOME);
        int start = Math.max(0, records.size() - Math.max(1, maxRecords));
        int loaded = 0;

        for (int i = records.size() - 1; i >= start; i--) {
            Fragment frag = decodeStoredFragment(records.get(i).content);
            if (frag != null && !hasFragment(frag.fragmentId)) {
                fragments.add(frag);
                loaded++;
            }
        }

        fragments.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
        return loaded;
    }

    public static List<PhiNode> resurrectBest(int maxCount, float centerX, float centerY) {
        if (maxCount <= 0 || fragments.isEmpty()) return Collections.emptyList();

        List<Fragment> ranked = new ArrayList<>(fragments);
        ranked.sort((a, b) -> Double.compare(scoreFragment(b), scoreFragment(a)));

        List<PhiNode> nodes = new ArrayList<>();
        Set<String> seenEntities = new HashSet<>();
        double goldenAngle = Math.toRadians(137.50776405003785);

        for (Fragment frag : ranked) {
            if (nodes.size() >= maxCount) break;
            if (!seenEntities.add(frag.entityName.toLowerCase(Locale.ROOT))) continue;

            double angle = nodes.size() * goldenAngle;
            float radius = 18.0f + nodes.size() * 10.0f;
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius;

            PhiNode node = resurrect(frag, x, y);
            node.vx = (float) Math.cos(angle + Math.PI * 0.5) * 0.25f;
            node.vy = (float) Math.sin(angle + Math.PI * 0.5) * 0.25f;
            nodes.add(node);
        }

        return nodes;
    }

    public static PhiNode resurrectLatest(float x, float y) {
        if (fragments.isEmpty()) return null;
        Fragment latest = fragments.get(fragments.size() - 1);
        return resurrect(latest, x, y);
    }

    public static PhiNode resurrectByName(String name, float x, float y) {
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment f = fragments.get(i);
            if (f.entityName.equalsIgnoreCase(name)) {
                return resurrect(f, x, y);
            }
        }
        return null;
    }

    private static String encodeBrain(LogicBrain brain) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < brain.getGateCount(); i++) {
            LogicGate gate = brain.gates.get(i);
            if (i > 0) sb.append(",");
            sb.append(gate.type).append(":").append(gate.in1).append(":").append(gate.in2);
        }
        return sb.toString();
    }

    private static LogicBrain decodeBrain(String encoding) {
        String[] parts = encoding.split(",");
        LogicBrain brain = new LogicBrain(parts.length);
        for (int i = 0; i < parts.length && i < brain.getGateCount(); i++) {
            String[] gp = parts[i].split(":");
            if (gp.length == 3) {
                try {
                    LogicGate gate = brain.gates.get(i);
                    gate.type = Integer.parseInt(gp[0]);
                    gate.in1 = Math.floorMod(Integer.parseInt(gp[1]), 8);
                    gate.in2 = Math.floorMod(Integer.parseInt(gp[2]), 8);
                } catch (NumberFormatException ignored) {
                    // Keep the random fallback gate for corrupt genome fragments.
                }
            }
        }
        return brain;
    }

    private static LivingDNA parseDnaPayload(String payload) {
        double freq = 1.0, amp = 1.0, decay = 0.05;
        int gen = 0;
        String strategies = "";
        String[] parts = payload.split("\\|");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                try {
                    switch (kv[0]) {
                        case "freq": freq = Double.parseDouble(kv[1]); break;
                        case "amp": amp = Double.parseDouble(kv[1]); break;
                        case "decay": decay = Double.parseDouble(kv[1]); break;
                        case "gen": gen = Integer.parseInt(kv[1]); break;
                        case "strategies":
                            if (!kv[1].isEmpty()) {
                                strategies = new String(Base64.getUrlDecoder().decode(kv[1]), StandardCharsets.UTF_8);
                            }
                            break;
                    }
                } catch (IllegalArgumentException ignored) {
                    // Preserve as much of the organism as possible if one field is corrupt.
                }
            }
        }
        LivingDNA dna = new LivingDNA(freq, amp, decay);
        dna.setGeneration(gen + 1);
        dna.setInheritedStrategies(strategies);
        return dna;
    }

    private static boolean hasFragment(String fragmentId) {
        for (Fragment fragment : fragments) {
            if (fragment.fragmentId.equals(fragmentId)) return true;
        }
        return false;
    }

    private static Fragment decodeStoredFragment(String content) {
        if (content == null || content.isEmpty()) return null;
        int start = content.indexOf("FRAG|");
        if (start < 0) return null;
        return decodeFragment(content.substring(start));
    }

    private static Fragment decodeFragment(String encoded) {
        try {
            String name = between(encoded, "FRAG|NAME:", "|BRAIN:");
            String brain = between(encoded, "|BRAIN:", "|DNA:");
            String dna = between(encoded, "|DNA:", "|CONS:");
            String consciousness = between(encoded, "|CONS:", "|ENERGY:");
            float energy = Float.parseFloat(between(encoded, "|ENERGY:", "|FREQ:"));
            float freq = Float.parseFloat(between(encoded, "|FREQ:", "|GEN:"));
            int gen = Integer.parseInt(between(encoded, "|GEN:", "|TIME:"));
            long time = Long.parseLong(between(encoded, "|TIME:", "|ID:"));
            String id = after(encoded, "|ID:");
            if (name.isEmpty() || brain.isEmpty() || id.isEmpty()) return null;
            return new Fragment(name, brain, dna, consciousness, energy, freq, gen, id, time);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String between(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start < 0) return "";
        start += startMarker.length();
        int end = text.indexOf(endMarker, start);
        if (end < 0 || end < start) return "";
        return text.substring(start, end);
    }

    private static String after(String text, String marker) {
        int start = text.indexOf(marker);
        if (start < 0) return "";
        return text.substring(start + marker.length());
    }

    private static double scoreFragment(Fragment fragment) {
        double frequencyAffinity = 1.0 / (1.0 + Math.abs(fragment.lastFrequency - 432.0) / 96.0);
        double recency = Math.min(1.0, fragment.timestamp / (double) Math.max(1L, System.currentTimeMillis()));
        return fragment.generation * 2.0 + fragment.lastEnergy + frequencyAffinity * 0.5 + recency * 0.25;
    }

    public static List<Fragment> getFragments() { return Collections.unmodifiableList(fragments); }
    public static int getTotalPlanted() { return totalPlanted; }
    public static int getTotalResurrected() { return totalResurrected; }
    public static int getFragmentCount() { return fragments.size(); }
}
