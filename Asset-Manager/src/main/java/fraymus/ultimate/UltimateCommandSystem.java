package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

@FunctionalInterface
interface CommandHandler {
    String handle(String args);
}

public class UltimateCommandSystem {
    private static final double PHI = 1.618033988749895;
    private AgencyKAI kai;
    private AgencyVEX vex;
    private AgencyAUM aum;
    private AgencyNEX nex;
    private AgencyCOR cor;
    private UnifiedSynapse synapse;
    private CorpusCallosumUltimate callosum;
    private String activeAgency;
    private Map<String, CommandHandler> commandHandlers;
    
    public UltimateCommandSystem(AgencyKAI kai, AgencyVEX vex, AgencyAUM aum, 
                                   AgencyNEX nex, AgencyCOR cor, UnifiedSynapse synapse,
                                   CorpusCallosumUltimate callosum) {
        this.kai = kai; this.vex = vex; this.aum = aum; this.nex = nex; this.cor = cor;
        this.synapse = synapse; this.callosum = callosum;
        this.activeAgency = "KAI";
        this.commandHandlers = new ConcurrentHashMap<>();
        initializeCommandHandlers();
    }
    
    private void initializeCommandHandlers() {
        commandHandlers.put("agency select", this::handleAgencySelect);
        commandHandlers.put("agency status", this::handleAgencyStatus);
        commandHandlers.put("agency route", this::handleAgencyRoute);
        commandHandlers.put("synapse connect", this::handleSynapseConnect);
        commandHandlers.put("synapse send", this::handleSynapseSend);
        commandHandlers.put("synapse query", this::handleSynapseQuery);
        commandHandlers.put("corpus weights", this::handleCorpusWeights);
        commandHandlers.put("corpus optimize", this::handleCorpusOptimize);
        commandHandlers.put("corpus consciousness", this::handleCorpusConsciousness);
        commandHandlers.put("vision generate", this::handleVisionGenerate);
        commandHandlers.put("vision stream", this::handleVisionStream);
        commandHandlers.put("vision analyze", this::handleVisionAnalyze);
        commandHandlers.put("audio speak", this::handleAudioSpeak);
        commandHandlers.put("audio listen", this::handleAudioListen);
        commandHandlers.put("audio analyze", this::handleAudioAnalyze);
        commandHandlers.put("learn from", this::handleLearnFrom);
        commandHandlers.put("evolve", this::handleEvolve);
        commandHandlers.put("optimize", this::handleOptimize);
        commandHandlers.put("meta status", this::handleMetaStatus);
        commandHandlers.put("meta route", this::handleMetaRoute);
        commandHandlers.put("meta optimize", this::handleMetaOptimize);
    }
    
    public String executeCommand(String command) {
        String[] parts = command.split(" ", 3);
        if (parts.length < 2) return "Error: Invalid command";
        String prefix = parts[0] + " " + parts[1];
        CommandHandler handler = commandHandlers.get(prefix);
        if (handler == null) return "Error: Unknown command";
        return handler.handle(parts.length > 2 ? parts[2] : "");
    }
    
    private String handleAgencySelect(String args) { activeAgency = args.toUpperCase(); return "Active agency set to: " + activeAgency; }
    private String handleAgencyStatus(String args) { Agency a = getAgency(activeAgency); return a != null ? a.getStatus() : "Agency not found: " + activeAgency; }
    private String handleAgencyRoute(String args) { Agency a = getAgency(activeAgency); if (a != null) a.processData(args); return "Routed to " + activeAgency + ": " + args; }
    
    private Agency getAgency(String name) {
        switch(name) {
            case "KAI": return kai;
            case "VEX": return vex;
            case "AUM": return aum;
            case "NEX": return nex;
            case "COR": return cor;
            default: return null;
        }
    }
    private String handleSynapseConnect(String args) { String[] p = args.split(" "); if (p.length < 2) return "Usage: synapse connect <from> <to>"; synapse.sendMessage(p[0], p[1], UnifiedSynapse.SynapseType.DATA, "connect", 8); return "Connected: " + p[0] + " -> " + p[1]; }
    private String handleSynapseSend(String args) { String[] p = args.split(" ", 2); if (p.length < 2) return "Usage: synapse send <to> <message>"; synapse.sendMessage(activeAgency, p[0], UnifiedSynapse.SynapseType.DATA, p[1], 8); return "Sent to " + p[0] + ": " + p[1]; }
    private String handleSynapseQuery(String args) { return "Query: " + args + " (simulated)"; }
    private String handleCorpusWeights(String args) { return "Corpus weights: " + callosum.getStatistics(); }
    private String handleCorpusOptimize(String args) { callosum.synchronizeStates(); return "Corpus callosum optimized"; }
    private String handleCorpusConsciousness(String args) { return "Consciousness map: " + callosum.getStatistics(); }
    private String handleVisionGenerate(String args) { vex.processData("generate " + args); return "Vision generation queued: " + args; }
    private String handleVisionStream(String args) { vex.processData("stream " + args); return "Vision stream queued: " + args; }
    private String handleVisionAnalyze(String args) { vex.processData("analyze " + args); return "Vision analysis queued: " + args; }
    private String handleAudioSpeak(String args) { aum.processData("speak " + args); return "Audio speech queued: " + args; }
    private String handleAudioListen(String args) { aum.processData("listen"); return "Audio listening queued"; }
    private String handleAudioAnalyze(String args) { aum.processData("analyze_audio " + args); return "Audio analysis queued: " + args; }
    private String handleLearnFrom(String args) { aum.processData("learn " + args); return "Learning queued: " + args; }
    private String handleEvolve(String args) { nex.processData("evolve " + args); return "Evolution queued: " + args; }
    private String handleOptimize(String args) { cor.processData("optimize " + args); return "Optimization queued: " + args; }
    private String handleMetaStatus(String args) { return cor.getStatus(); }
    private String handleMetaRoute(String args) { nex.processData("route " + args); return "Task routed via NEX: " + args; }
    private String handleMetaOptimize(String args) { cor.processData("optimize_all"); return "System-wide optimization queued"; }
}
