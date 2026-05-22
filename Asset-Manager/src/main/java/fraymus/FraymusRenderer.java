package fraymus;

import jade.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;

import java.util.*;

public class FraymusRenderer {

    private static final int MAX_TRAIL_LENGTH = 60;
    private static final float GOLDEN_ANGLE = (float) Math.toRadians(137.50776405003785);
    private static Map<String, List<Vector2f>> trailHistory = new HashMap<>();

    public static void render(PhiWorld world, Camera camera) {
        List<PhiNode> nodes = world.getNodes();

        renderBackgroundGrid();

        for (PhiNode node : nodes) {
            String key = node.name;
            List<Vector2f> trail = trailHistory.computeIfAbsent(key, k -> new ArrayList<>());
            trail.add(new Vector2f(node.x, node.y));
            if (trail.size() > MAX_TRAIL_LENGTH) {
                trail.remove(0);
            }
        }

        Set<String> aliveNames = new HashSet<>();
        for (PhiNode node : nodes) {
            aliveNames.add(node.name);
        }
        trailHistory.keySet().retainAll(aliveNames);

        for (PhiNode node : nodes) {
            List<Vector2f> trail = trailHistory.get(node.name);
            if (trail != null && trail.size() > 1) {
                for (int i = 0; i < trail.size() - 1; i++) {
                    float alpha = (float) i / trail.size();
                    Vector3f trailColor = new Vector3f(
                            node.r * alpha * 0.8f,
                            node.g * alpha * 0.8f,
                            node.b * alpha * 0.8f
                    );
                    DebugDraw.addLine2D(
                            new Vector2f(trail.get(i)),
                            new Vector2f(trail.get(i + 1)),
                            trailColor, 1
                    );
                }
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                PhiNode a = nodes.get(i);
                PhiNode b = nodes.get(j);
                float dx = a.x - b.x;
                float dy = a.y - b.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 100.0f) {
                    float alpha = 1.0f - dist / 100.0f;
                    Vector3f lineColor = getRelationColor(a, b, alpha);
                    DebugDraw.addLine2D(
                            new Vector2f(a.x, a.y),
                            new Vector2f(b.x, b.y),
                            lineColor, 1
                    );
                }
            }
        }

        for (PhiNode node : nodes) {
            float radius = 3.0f + node.energy * 6.0f;

            float[] consColor = node.consciousness.getConsciousnessColor();
            float cr = consColor[0];
            float cg = consColor[1];
            float cb = consColor[2];

            boolean breathing = node.consciousness.isRegressive();
            float breathPulse = breathing ?
                    (float)(Math.sin(System.nanoTime() * 1e-9 * 1.5) * 0.3 + 0.7) : 1.0f;

            Vector3f nodeColor = new Vector3f(cr * breathPulse, cg * breathPulse, cb * breathPulse);
            DebugDraw.addCircle(new Vector2f(node.x, node.y), radius, nodeColor, 1);

            float pulse = (float) (Math.sin(System.nanoTime() * 1e-9 * 2.0 + node.frequency * 0.01) * 0.5 + 0.5);

            renderEvolutionHalo(node, radius, pulse);
            renderVelocityVector(node, radius);

            float glowRadius1 = radius * 1.3f;
            Vector3f glowColor1 = new Vector3f(
                    cr * 0.3f * (0.7f + pulse * 0.3f),
                    cg * 0.3f * (0.7f + pulse * 0.3f),
                    cb * 0.3f * (0.7f + pulse * 0.3f)
            );
            DebugDraw.addCircle(new Vector2f(node.x, node.y), glowRadius1, glowColor1, 1);

            float glowRadius2 = radius * 1.7f;
            Vector3f glowColor2 = new Vector3f(
                    cr * 0.15f * (0.7f + pulse * 0.3f),
                    cg * 0.15f * (0.7f + pulse * 0.3f),
                    cb * 0.15f * (0.7f + pulse * 0.3f)
            );
            DebugDraw.addCircle(new Vector2f(node.x, node.y), glowRadius2, glowColor2, 1);

            float glowRadius3 = radius * 2.2f;
            Vector3f glowColor3 = new Vector3f(
                    cr * 0.07f * (0.7f + pulse * 0.3f),
                    cg * 0.07f * (0.7f + pulse * 0.3f),
                    cb * 0.07f * (0.7f + pulse * 0.3f)
            );
            DebugDraw.addCircle(new Vector2f(node.x, node.y), glowRadius3, glowColor3, 1);

            float[] roleColor = node.getRole().color;
            float roleRadius = radius * 0.4f;
            DebugDraw.addCircle(new Vector2f(node.x, node.y), roleRadius,
                    new Vector3f(roleColor[0], roleColor[1], roleColor[2]), 1);

            renderXorGlyph(node, radius, pulse);
            renderGeneratedConceptMarker(node, radius, pulse);

            if (node.spikeFlash) {
                float spikePulse = (float)(Math.sin(System.nanoTime() * 1e-8) * 0.5 + 0.5);
                float rayLen = radius * 3.0f;
                for (int r = 0; r < 8; r++) {
                    float angle = (float)(r * Math.PI / 4.0);
                    Vector2f rayEnd = new Vector2f(
                            node.x + (float) Math.cos(angle) * rayLen,
                            node.y + (float) Math.sin(angle) * rayLen
                    );
                    Vector3f rayColor = new Vector3f(
                            1.0f,
                            0.6f + spikePulse * 0.4f,
                            0.1f + spikePulse * 0.2f
                    );
                    DebugDraw.addLine2D(new Vector2f(node.x, node.y), rayEnd, rayColor, 1);
                }
                DebugDraw.addCircle(new Vector2f(node.x, node.y), radius * 2.5f,
                        new Vector3f(1.0f, spikePulse * 0.8f, spikePulse * 0.3f), 1);
            }

            int[] outputs = node.brain.getLastOutputs();
            if (outputs.length > 0) {
                if (node.brain.wantsToSeek(outputs)) {
                    float arrowLen = radius * 1.5f;
                    float angle = (float) Math.atan2(node.vy, node.vx);
                    Vector2f arrowEnd = new Vector2f(
                            node.x + (float) Math.cos(angle) * arrowLen,
                            node.y + (float) Math.sin(angle) * arrowLen
                    );
                    DebugDraw.addLine2D(new Vector2f(node.x, node.y), arrowEnd,
                            new Vector3f(0.0f, 1.0f, 0.5f), 1);
                }
                if (node.brain.wantsToFlee(outputs)) {
                    float arrowLen = radius * 1.5f;
                    float angle = (float) Math.atan2(node.vy, node.vx);
                    Vector2f arrowEnd = new Vector2f(
                            node.x + (float) Math.cos(angle) * arrowLen,
                            node.y + (float) Math.sin(angle) * arrowLen
                    );
                    DebugDraw.addLine2D(new Vector2f(node.x, node.y), arrowEnd,
                            new Vector3f(1.0f, 0.2f, 0.2f), 1);
                }
                if (node.brain.wantsToReproduce(outputs)) {
                    float plusSize = radius * 0.5f;
                    float px = node.x + radius + 2.0f;
                    float py = node.y;
                    DebugDraw.addLine2D(new Vector2f(px - plusSize, py), new Vector2f(px + plusSize, py),
                            new Vector3f(0.3f, 1.0f, 0.3f), 1);
                    DebugDraw.addLine2D(new Vector2f(px, py - plusSize), new Vector2f(px, py + plusSize),
                            new Vector3f(0.3f, 1.0f, 0.3f), 1);
                }
                if (node.brain.wantsToMutate(outputs)) {
                    float mutSize = radius * 0.4f;
                    float mx = node.x - radius - 2.0f;
                    float my = node.y;
                    DebugDraw.addLine2D(new Vector2f(mx - mutSize, my - mutSize), new Vector2f(mx + mutSize, my + mutSize),
                            new Vector3f(1.0f, 0.5f, 0.0f), 1);
                    DebugDraw.addLine2D(new Vector2f(mx - mutSize, my + mutSize), new Vector2f(mx + mutSize, my - mutSize),
                            new Vector3f(1.0f, 0.5f, 0.0f), 1);
                }
            }

            DebugDraw.addCircle(new Vector2f(node.x, node.y + radius + 2.0f), 0.8f,
                    new Vector3f(node.r, node.g, node.b), 1);

            float barWidth = 8.0f;
            float barY = node.y - radius - 3.0f;
            float barX = node.x - barWidth * 0.5f;
            float energyWidth = barWidth * node.energy;

            Vector3f barColor = new Vector3f(
                    1.0f - node.energy,
                    node.energy,
                    0.0f
            );
            DebugDraw.addLine2D(
                    new Vector2f(barX, barY),
                    new Vector2f(barX + energyWidth, barY),
                    barColor, 1
            );

            Vector3f barBg = new Vector3f(0.2f, 0.2f, 0.2f);
            DebugDraw.addLine2D(
                    new Vector2f(barX + energyWidth, barY),
                    new Vector2f(barX + barWidth, barY),
                    barBg, 1
            );
        }

        renderBoundary();
    }

    private static void renderBackgroundGrid() {
        float minX = -180.0f, maxX = 180.0f, minY = -100.0f, maxY = 100.0f;
        Vector3f gridColor = new Vector3f(0.08f, 0.08f, 0.15f);
        Vector3f phiColor = new Vector3f(0.11f, 0.09f, 0.03f);
        float dotSize = 0.3f;

        for (float x = minX; x <= maxX; x += 20.0f) {
            for (float y = minY; y <= maxY; y += 20.0f) {
                DebugDraw.addLine2D(
                        new Vector2f(x - dotSize, y),
                        new Vector2f(x + dotSize, y),
                        gridColor, 1
                );
            }
        }

        for (int i = 1; i <= 18; i++) {
            float radius = i * 8.0f;
            float angle = i * GOLDEN_ANGLE;
            Vector2f a = new Vector2f(
                    (float) Math.cos(angle) * radius,
                    (float) Math.sin(angle) * radius
            );
            Vector2f b = new Vector2f(
                    (float) Math.cos(angle + GOLDEN_ANGLE) * Math.min(radius + 10.0f, maxX),
                    (float) Math.sin(angle + GOLDEN_ANGLE) * Math.min(radius + 10.0f, maxY)
            );
            DebugDraw.addLine2D(a, b, phiColor, 1);
        }
    }

    private static Vector3f getRelationColor(PhiNode a, PhiNode b, float alpha) {
        float xorResonance = (float) ((a.brain.getXorResonance() + b.brain.getXorResonance()) * 0.5);

        if (a.isEntangledWith(b)) {
            return new Vector3f(0.15f * alpha, 0.95f * alpha, 1.0f * alpha);
        }
        if (xorResonance > 0.45f) {
            return new Vector3f(1.0f * alpha, 0.25f * alpha, 0.85f * alpha);
        }
        if (a.adaptiveEngine.isInTrial() || b.adaptiveEngine.isInTrial()) {
            return new Vector3f(1.0f * alpha, 0.55f * alpha, 0.12f * alpha);
        }
        return new Vector3f(0.2f * alpha, 0.8f * alpha, 0.6f * alpha);
    }

    private static void renderEvolutionHalo(PhiNode node, float radius, float pulse) {
        if (node.adaptiveEngine.isInTrial()) {
            DebugDraw.addCircle(new Vector2f(node.x, node.y), radius * (2.6f + pulse * 0.5f),
                    new Vector3f(1.0f, 0.45f + pulse * 0.2f, 0.05f), 1);
        }

        int provenStrategies = node.adaptiveEngine.getProvenStrategyCount();
        if (provenStrategies > 0) {
            float[] roleColor = node.getRole().color;
            float strategyPulse = Math.min(1.0f, provenStrategies / 8.0f);
            DebugDraw.addCircle(new Vector2f(node.x, node.y), radius * (2.1f + strategyPulse),
                    new Vector3f(roleColor[0] * 0.45f, roleColor[1] * 0.45f, roleColor[2] * 0.45f), 1);
        }
    }

    private static void renderVelocityVector(PhiNode node, float radius) {
        float speed = (float) Math.sqrt(node.vx * node.vx + node.vy * node.vy);
        if (speed < 0.01f) return;

        float scale = Math.min(radius * 2.4f, radius + speed * 0.8f);
        float invSpeed = 1.0f / speed;
        Vector2f start = new Vector2f(node.x, node.y);
        Vector2f end = new Vector2f(node.x + node.vx * invSpeed * scale, node.y + node.vy * invSpeed * scale);
        DebugDraw.addLine2D(start, end, new Vector3f(0.35f, 0.75f, 1.0f), 1);
    }

    private static void renderXorGlyph(PhiNode node, float radius, float pulse) {
        int activeXor = node.brain.getActiveXorCount();
        if (activeXor <= 0) return;

        float glyphRadius = radius * (1.9f + 0.25f * pulse);
        Vector3f xorColor = new Vector3f(1.0f, 0.15f + pulse * 0.25f, 0.95f);
        Vector2f top = new Vector2f(node.x, node.y + glyphRadius);
        Vector2f right = new Vector2f(node.x + glyphRadius, node.y);
        Vector2f bottom = new Vector2f(node.x, node.y - glyphRadius);
        Vector2f left = new Vector2f(node.x - glyphRadius, node.y);

        DebugDraw.addLine2D(top, right, xorColor, 1);
        DebugDraw.addLine2D(right, bottom, xorColor, 1);
        DebugDraw.addLine2D(bottom, left, xorColor, 1);
        DebugDraw.addLine2D(left, top, xorColor, 1);
        DebugDraw.addLine2D(left, right, xorColor, 1);
        DebugDraw.addLine2D(top, bottom, xorColor, 1);
    }

    private static void renderGeneratedConceptMarker(PhiNode node, float radius, float pulse) {
        if (node.getLastGeneratedConcept() == null) return;

        float markerRadius = radius * (2.8f + pulse * 0.3f);
        Vector3f codeColor = new Vector3f(0.4f, 1.0f, 0.75f);
        for (int i = 0; i < 3; i++) {
            float a = (float) (i * Math.PI * 2.0 / 3.0 + pulse * 0.4);
            float b = (float) (((i + 1) % 3) * Math.PI * 2.0 / 3.0 + pulse * 0.4);
            DebugDraw.addLine2D(
                    new Vector2f(node.x + (float) Math.cos(a) * markerRadius, node.y + (float) Math.sin(a) * markerRadius),
                    new Vector2f(node.x + (float) Math.cos(b) * markerRadius, node.y + (float) Math.sin(b) * markerRadius),
                    codeColor, 1
            );
        }
    }

    private static void renderBoundary() {
        float minX = -180.0f, maxX = 180.0f, minY = -100.0f, maxY = 100.0f;
        Vector3f boundaryColor = new Vector3f(0.15f, 0.15f, 0.25f);

        DebugDraw.addLine2D(new Vector2f(minX, minY), new Vector2f(maxX, minY), boundaryColor, 1);
        DebugDraw.addLine2D(new Vector2f(maxX, minY), new Vector2f(maxX, maxY), boundaryColor, 1);
        DebugDraw.addLine2D(new Vector2f(maxX, maxY), new Vector2f(minX, maxY), boundaryColor, 1);
        DebugDraw.addLine2D(new Vector2f(minX, maxY), new Vector2f(minX, minY), boundaryColor, 1);
    }
}
