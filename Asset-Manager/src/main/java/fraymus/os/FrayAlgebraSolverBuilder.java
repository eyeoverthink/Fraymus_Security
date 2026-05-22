package fraymus.os;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * FRAY ALGEBRA SOLVER BUILDER
 *
 * Converts the runtime-proposed AlgebraSolverBuilder idea into a real Fraynix
 * module. The shell can call the Java solver directly, while the builder emits a
 * small C solver for the bare-metal image pipeline.
 */
public class FrayAlgebraSolverBuilder {

    private static final String OUTPUT_DIR = "fraynix_src";
    private static final double EPSILON = 1.0e-9;

    public static void main(String[] args) {
        System.out.println("FRAY ALGEBRA SOLVER BUILDER");
        if (args != null && args.length > 0) {
            System.out.println(solveEquation(String.join(" ", args)));
        } else {
            System.out.println(solveEquation("2x + 3 = 5"));
            System.out.println(solveEquation("x^2 + 4x - 3 = 0"));
        }

        try {
            Files.createDirectories(Path.of(OUTPUT_DIR));
            writeFile("algebra_solver.c", buildAlgebraSolverC());
            writeFile("algebra_solver.md", buildAlgebraManifest());
            System.out.println("Algebra solver source generated in " + OUTPUT_DIR);
        } catch (IOException e) {
            System.err.println("Algebra solver build failed: " + e.getMessage());
        }
    }

    public static String solveEquation(String equation) {
        Polynomial polynomial = parseEquation(equation);
        if (Math.abs(polynomial.c2) > EPSILON) {
            return solveQuadraticEquation(equation);
        }
        return solveLinearEquation(equation);
    }

    public static String solveLinearEquation(String equationString) {
        Polynomial polynomial = parseEquation(equationString);
        if (Math.abs(polynomial.c2) > EPSILON) {
            return solveQuadraticEquation(equationString);
        }

        if (Math.abs(polynomial.c1) <= EPSILON) {
            if (Math.abs(polynomial.c0) <= EPSILON) {
                return "identity: all real x satisfy " + equationString;
            }
            return "contradiction: no solution for " + equationString;
        }

        LinearSolution solution = new LinearSolution(-polynomial.c0 / polynomial.c1);
        return "linear: " + equationString + " -> " + solution;
    }

    public static String solveQuadraticEquation(String equationString) {
        Polynomial polynomial = parseEquation(equationString);
        if (Math.abs(polynomial.c2) <= EPSILON) {
            return solveLinearEquation(equationString);
        }

        QuadraticSolution solution = solveQuadratic(polynomial.c2, polynomial.c1, polynomial.c0);
        return "quadratic: " + equationString + " -> " + solution;
    }

    public static QuadraticSolution solveQuadratic(double a, double b, double c) {
        if (Math.abs(a) <= EPSILON) {
            return QuadraticSolution.degenerate(new LinearSolution(Math.abs(b) <= EPSILON ? Double.NaN : -c / b));
        }

        double discriminant = b * b - 4.0 * a * c;
        if (discriminant >= 0.0) {
            double sqrt = Math.sqrt(discriminant);
            double x1 = (-b + sqrt) / (2.0 * a);
            double x2 = (-b - sqrt) / (2.0 * a);
            return QuadraticSolution.real(x1, x2, discriminant);
        }

        double real = -b / (2.0 * a);
        double imaginary = Math.sqrt(-discriminant) / (2.0 * Math.abs(a));
        return QuadraticSolution.complex(real, imaginary, discriminant);
    }

    private static Polynomial parseEquation(String equation) {
        if (equation == null || equation.isBlank()) {
            throw new IllegalArgumentException("Equation is empty.");
        }

        String[] sides = equation.split("=", 2);
        if (sides.length != 2) {
            throw new IllegalArgumentException("Equation must contain '='.");
        }

        Polynomial left = parseSide(sides[0]);
        Polynomial right = parseSide(sides[1]);
        return left.subtract(right);
    }

    private static Polynomial parseSide(String side) {
        String normalized = side.toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("*", "")
                .replace("-", "+-");
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }

        Polynomial result = new Polynomial();
        if (normalized.isBlank()) {
            return result;
        }

        for (String term : normalized.split("\\+")) {
            if (term == null || term.isBlank()) {
                continue;
            }
            addTerm(result, term);
        }
        return result;
    }

    private static void addTerm(Polynomial polynomial, String term) {
        if (term.contains("x^2")) {
            polynomial.c2 += parseCoefficient(term.substring(0, term.indexOf("x^2")));
        } else if (term.contains("x")) {
            polynomial.c1 += parseCoefficient(term.substring(0, term.indexOf("x")));
        } else {
            polynomial.c0 += Double.parseDouble(term);
        }
    }

    private static double parseCoefficient(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("+")) {
            return 1.0;
        }
        if (raw.equals("-")) {
            return -1.0;
        }
        return Double.parseDouble(raw);
    }

    private static String buildAlgebraSolverC() {
        return """
/* FRAY ALGEBRA SOLVER
 * Generated by FrayAlgebraSolverBuilder.
 * Linear and quadratic equation primitives for bare-metal Fraynix.
 */

#ifndef FRAY_ALGEBRA_SOLVER_C
#define FRAY_ALGEBRA_SOLVER_C

typedef struct {
    int has_solution;
    int infinite_solutions;
    double x;
} fray_linear_solution;

typedef struct {
    int complex_roots;
    double x1;
    double x2;
    double real;
    double imaginary;
    double discriminant;
} fray_quadratic_solution;

static double fray_abs_double(double x) {
    return x < 0.0 ? -x : x;
}

static double fray_sqrt(double x) {
    if (x <= 0.0) {
        return 0.0;
    }
    double guess = x > 1.0 ? x : 1.0;
    for (int i = 0; i < 32; i++) {
        guess = 0.5 * (guess + x / guess);
    }
    return guess;
}

fray_linear_solution fray_solve_linear(double a, double b) {
    fray_linear_solution out;
    out.has_solution = 1;
    out.infinite_solutions = 0;
    out.x = 0.0;

    if (fray_abs_double(a) < 0.000000001) {
        out.has_solution = fray_abs_double(b) < 0.000000001;
        out.infinite_solutions = out.has_solution;
        return out;
    }

    out.x = -b / a;
    return out;
}

fray_quadratic_solution fray_solve_quadratic(double a, double b, double c) {
    fray_quadratic_solution out;
    out.complex_roots = 0;
    out.x1 = 0.0;
    out.x2 = 0.0;
    out.real = 0.0;
    out.imaginary = 0.0;
    out.discriminant = b * b - 4.0 * a * c;

    if (out.discriminant >= 0.0) {
        double root = fray_sqrt(out.discriminant);
        out.x1 = (-b + root) / (2.0 * a);
        out.x2 = (-b - root) / (2.0 * a);
    } else {
        out.complex_roots = 1;
        out.real = -b / (2.0 * a);
        out.imaginary = fray_sqrt(-out.discriminant) / (2.0 * fray_abs_double(a));
    }
    return out;
}

#endif
""";
    }

    private static String buildAlgebraManifest() {
        return """
# Fray Algebra Solver

Generated by `FrayAlgebraSolverBuilder`.

Purpose:
- Make the Fraynix-proposed algebra builder real and runnable.
- Support single-variable linear equations such as `2x + 3 = 5`.
- Support quadratic equations such as `x^2 + 4x - 3 = 0`.
- Emit `algebra_solver.c` for the bare-metal image pipeline.

Shell:
- `solve 2x + 3 = 5`
- `solve x^2 + 4x - 3 = 0`
- `builders run solver`
""";
    }

    private static void writeFile(String fileName, String content) throws IOException {
        Path path = Path.of(OUTPUT_DIR, fileName);
        Files.writeString(path, content, StandardCharsets.UTF_8);
        System.out.println("  -> " + path + " (" + content.length() + " bytes)");
    }

    private static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        String text = String.format(Locale.ROOT, "%.10f", value);
        while (text.contains(".") && text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static final class Polynomial {
        private double c2;
        private double c1;
        private double c0;

        private Polynomial subtract(Polynomial other) {
            Polynomial result = new Polynomial();
            result.c2 = c2 - other.c2;
            result.c1 = c1 - other.c1;
            result.c0 = c0 - other.c0;
            return result;
        }
    }

    public static final class LinearSolution {
        private final double x;

        private LinearSolution(double x) {
            this.x = x;
        }

        public double x() {
            return x;
        }

        @Override
        public String toString() {
            return "x = " + format(x);
        }
    }

    public static final class QuadraticSolution {
        private final boolean degenerate;
        private final boolean complex;
        private final LinearSolution linearSolution;
        private final double x1;
        private final double x2;
        private final double real;
        private final double imaginary;
        private final double discriminant;

        private QuadraticSolution(
                boolean degenerate,
                boolean complex,
                LinearSolution linearSolution,
                double x1,
                double x2,
                double real,
                double imaginary,
                double discriminant) {
            this.degenerate = degenerate;
            this.complex = complex;
            this.linearSolution = linearSolution;
            this.x1 = x1;
            this.x2 = x2;
            this.real = real;
            this.imaginary = imaginary;
            this.discriminant = discriminant;
        }

        private static QuadraticSolution degenerate(LinearSolution solution) {
            return new QuadraticSolution(true, false, solution, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }

        private static QuadraticSolution real(double x1, double x2, double discriminant) {
            return new QuadraticSolution(false, false, null, x1, x2, Double.NaN, Double.NaN, discriminant);
        }

        private static QuadraticSolution complex(double real, double imaginary, double discriminant) {
            return new QuadraticSolution(false, true, null, Double.NaN, Double.NaN, real, imaginary, discriminant);
        }

        @Override
        public String toString() {
            if (degenerate) {
                return "degenerate -> " + linearSolution;
            }
            if (complex) {
                return "x = " + format(real) + " +/- " + format(imaginary) + "i"
                        + " (discriminant=" + format(discriminant) + ")";
            }
            return "x1 = " + format(x1) + ", x2 = " + format(x2)
                    + " (discriminant=" + format(discriminant) + ")";
        }
    }
}
