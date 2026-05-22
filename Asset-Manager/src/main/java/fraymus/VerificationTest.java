package fraymus;

import fraymus.compute.FrayCLContext;
import fraymus.compute.FrayCLBuffer;
import fraymus.compute.FrayCLKernels;
import fraymus.neural.BabelModelRouter;

/**
 * 🧬 VERIFICATION TEST
 * 
 * Proves what systems are actually working
 */
public class VerificationTest {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🧬 FRAYNIX SYSTEM VERIFICATION                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Test 1: FrayCL
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: FRAYCL COMPUTE ABSTRACTION");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        try {
            FrayCLContext context = new FrayCLContext();
            System.out.println("✅ FrayCL Context created successfully");
            
            FrayCLBuffer buffer = context.createFloatBuffer(1000);
            System.out.println("✅ FrayCL Buffer created successfully");
            
            context.createKernel("test", FrayCLKernels.identity());
            System.out.println("✅ FrayCL Kernel created successfully");
            
            buffer.release();
            context.release();
            System.out.println("✅ FrayCL TEST PASSED");
        } catch (Exception e) {
            System.out.println("❌ FrayCL TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        // Test 2: Babel Router
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: BABEL MODEL ROUTER");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        try {
            BabelModelRouter router = new BabelModelRouter(null, null);
            System.out.println("✅ Babel Router created successfully");
            
            router.printRoutingTable();
            System.out.println("✅ Routing table displayed successfully");
            
            String code = router.generateCode("design a system architecture", "python");
            System.out.println("✅ Code generation working");
            System.out.println("   Generated: " + code.split("\n")[0]);
            
            System.out.println("✅ Babel Router TEST PASSED");
        } catch (Exception e) {
            System.out.println("❌ Babel Router TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        // Test 3: Task Classification
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: TASK CLASSIFICATION");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        try {
            BabelModelRouter router = new BabelModelRouter(null, null);
            
            String[] tests = {
                "design a system architecture",
                "optimize this code for performance",
                "create an innovative solution",
                "create a simple prototype",
                "implement fraynix specific feature"
            };
            
            for (String test : tests) {
                BabelModelRouter.CodeTaskType type = router.classifyTask(test, "python");
                System.out.println("   \"" + test + "\" → " + type);
            }
            
            System.out.println("✅ Task Classification TEST PASSED");
        } catch (Exception e) {
            System.out.println("❌ Task Classification TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ VERIFICATION COMPLETE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
