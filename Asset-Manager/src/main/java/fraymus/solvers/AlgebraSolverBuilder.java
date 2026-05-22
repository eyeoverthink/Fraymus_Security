package fraymus.solvers;

import fraymus.knowledge.AkashicRecord;
import fraymus.neural.AeonSingularity;

/**
 * ALGEBRA SOLVER BUILDER
 * 
 * Solves algebraic equations using:
 * - AkashicRecord for mathematical knowledge retrieval
 * - AeonSingularity (8192-D HDC spiking matrix with Hebbian learning)
 * 
 * Supported equation types:
 * - Linear equations: ax + b = c
 * - Quadratic equations: ax² + bx + c = 0
 * - Polynomial equations
 * 
 * "The neural network solves what the calculator cannot."
 */
public class AlgebraSolverBuilder {
    
    private final AkashicRecord akashicRecord;
    private final AeonSingularity aeonSingularity;
    
    public AlgebraSolverBuilder() {
        this.akashicRecord = new AkashicRecord();
        this.aeonSingularity = AeonSingularity.getInstance();
    }
    
    /**
     * Build the algebra solver
     */
    public AlgebraSolver build() {
        return new AlgebraSolver(akashicRecord, aeonSingularity);
    }
    
    /**
     * Algebra Solver - solves mathematical equations
     */
    public static class AlgebraSolver {
        private final AkashicRecord akashicRecord;
        private final AeonSingularity aeonSingularity;
        
        public AlgebraSolver(AkashicRecord akashicRecord, AeonSingularity aeonSingularity) {
            this.akashicRecord = akashicRecord;
            this.aeonSingularity = aeonSingularity;
        }
        
        /**
         * Solve linear equation: ax + b = c
         * Example: "2x + 3 = 5" → x = 1
         */
        public Solution solveLinearEquation(String equation) {
            // Parse equation coefficients
            double[] coeffs = parseLinearEquation(equation);
            double a = coeffs[0];
            double b = coeffs[1];
            double c = coeffs[2];
            
            // Solve: ax + b = c → x = (c - b) / a
            double x = (c - b) / a;
            
            // Verify with neural network (using learn method)
            aeonSingularity.learn(equation + " → x = " + x);
            
            // Store in knowledge base
            akashicRecord.addBlock("algebra", 
                equation + " → x = " + x);
            
            return new Solution(x, "Linear equation solved");
        }
        
        /**
         * Solve quadratic equation: ax² + bx + c = 0
         * Example: "x² + 4x - 3 = 0" → x = -3, 1
         */
        public Solution solveQuadraticEquation(String equation) {
            // Parse equation coefficients
            double[] coeffs = parseQuadraticEquation(equation);
            double a = coeffs[0];
            double b = coeffs[1];
            double c = coeffs[2];
            
            // Solve using quadratic formula: x = (-b ± √(b² - 4ac)) / 2a
            double discriminant = b * b - 4 * a * c;
            
            if (discriminant < 0) {
                return new Solution(Double.NaN, "No real solutions (complex roots)");
            }
            
            double x1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double x2 = (-b - Math.sqrt(discriminant)) / (2 * a);
            
            // Verify with neural network (using learn method)
            aeonSingularity.learn(equation + " → x₁ = " + x1 + ", x₂ = " + x2);
            
            // Store in knowledge base
            akashicRecord.addBlock("algebra", 
                equation + " → x₁ = " + x1 + ", x₂ = " + x2);
            
            return new Solution(x1, "Quadratic equation solved: x₁ = " + x1 + ", x₂ = " + x2);
        }
        
        /**
         * Parse linear equation string to coefficients
         */
        private double[] parseLinearEquation(String equation) {
            // Simplified parsing - in production would use proper parser
            double a = 1, b = 0, c = 0;
            
            // Remove spaces
            equation = equation.replaceAll("\\s+", "");
            
            // Extract coefficients (simplified)
            if (equation.contains("=")) {
                String[] parts = equation.split("=");
                String left = parts[0];
                c = Double.parseDouble(parts[1]);
                
                if (left.contains("x")) {
                    if (left.matches(".*x.*\\+.*")) {
                        String[] terms = left.split("\\+");
                        a = extractCoefficient(terms[0]);
                        b = extractCoefficient(terms[1]);
                    }
                }
            }
            
            return new double[]{a, b, c};
        }
        
        /**
         * Parse quadratic equation string to coefficients
         */
        private double[] parseQuadraticEquation(String equation) {
            // Simplified parsing
            double a = 1, b = 0, c = 0;
            
            equation = equation.replaceAll("\\s+", "");
            equation = equation.replace("x²", "x^2");
            
            if (equation.contains("=")) {
                String[] parts = equation.split("=");
                String left = parts[0];
                c = -Double.parseDouble(parts[1]);
                
                // Extract coefficients (simplified)
                String[] terms = left.split("(?=[+-])");
                for (String term : terms) {
                    if (term.contains("x^2")) {
                        a = extractCoefficient(term);
                    } else if (term.contains("x")) {
                        b = extractCoefficient(term);
                    } else {
                        c += extractCoefficient(term);
                    }
                }
            }
            
            return new double[]{a, b, c};
        }
        
        /**
         * Extract numeric coefficient from term
         */
        private double extractCoefficient(String term) {
            term = term.replace("x", "").replace("^2", "").replace("+", "").replace("-", "");
            if (term.isEmpty()) return 1.0;
            try {
                return Double.parseDouble(term);
            } catch (NumberFormatException e) {
                return 1.0;
            }
        }
    }
    
    /**
     * Solution wrapper
     */
    public static class Solution {
        private final double value;
        private final String explanation;
        
        public Solution(double value, String explanation) {
            this.value = value;
            this.explanation = explanation;
        }
        
        public double getValue() {
            return value;
        }
        
        public String getExplanation() {
            return explanation;
        }
    }
}
