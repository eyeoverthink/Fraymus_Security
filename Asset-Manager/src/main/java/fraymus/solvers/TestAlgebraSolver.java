package fraymus.solvers;

/**
 * Test Algebra Solver
 */
public class TestAlgebraSolver {
    public static void main(String[] args) {
        System.out.println("Testing AlgebraSolverBuilder...");
        
        try {
            AlgebraSolverBuilder builder = new AlgebraSolverBuilder();
            AlgebraSolverBuilder.AlgebraSolver solver = builder.build();
            
            // Test linear equation: 2x + 3 = 5
            AlgebraSolverBuilder.Solution linear = solver.solveLinearEquation("2x + 3 = 5");
            System.out.println("Linear equation (2x + 3 = 5):");
            System.out.println("  x = " + linear.getValue());
            System.out.println("  " + linear.getExplanation());
            
            // Test quadratic equation: x^2 + 4x - 3 = 0
            AlgebraSolverBuilder.Solution quadratic = solver.solveQuadraticEquation("x^2 + 4x - 3 = 0");
            System.out.println("\nQuadratic equation (x^2 + 4x - 3 = 0):");
            System.out.println("  " + quadratic.getExplanation());
            
            System.out.println("\n✅ AlgebraSolver test PASSED");
        } catch (Exception e) {
            System.err.println("❌ AlgebraSolver test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
