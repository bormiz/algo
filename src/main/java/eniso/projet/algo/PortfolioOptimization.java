package eniso.projet.algo;

import java.util.*;

public class PortfolioOptimization {
    // Sample assets data
    double[] returns = {0.08, 0.12, 0.10, 0.15, 0.09}; // Expected returns
    double[] risks = {0.1, 0.2, 0.15, 0.25, 0.18}; // Risks (standard deviation)

    // Objective 1: Maximize portfolio returns
    public double objective1(double[] portfolio) {
        double portfolioReturn = 0;
        for (int i = 0; i < portfolio.length; i++) {
            portfolioReturn += portfolio[i] * returns[i];
        }
        return portfolioReturn;
    }

    // Objective 2: Minimize portfolio risk (variance)
    public double objective2(double[] portfolio) {
        double portfolioRisk = 0;
        for (int i = 0; i < portfolio.length; i++) {
            portfolioRisk += portfolio[i] * risks[i];
        }
        return portfolioRisk;
    }

    // MOGA algorithm for multi-objective optimization
    public static void main(String[] args) {
        PortfolioOptimization optimization = new PortfolioOptimization();
        MOGA moga = new MOGA(optimization);
        moga.run();
    }
}

class MOGA {
    PortfolioOptimization problem;
    int populationSize = 10;
    int maxGenerations = 50;
    Random rand = new Random();

    // Constructor for MOGA
    public MOGA(PortfolioOptimization problem) {
        this.problem = problem;
    }

    // Generate initial population of portfolios
    public List<double[]> generateInitialPopulation() {
        List<double[]> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            double[] portfolio = new double[problem.returns.length];
            double sum = 0;
            for (int j = 0; j < portfolio.length; j++) {
                portfolio[j] = rand.nextDouble();
                sum += portfolio[j];
            }
            // Normalize portfolio so that the sum of weights is 1
            for (int j = 0; j < portfolio.length; j++) {
                portfolio[j] /= sum;
            }
            population.add(portfolio);
        }
        return population;
    }

    // Fitness function: evaluates both objectives
    public List<double[]> evaluatePopulation(List<double[]> population) {
        // Evaluate each individual using both objectives (maximize return, minimize risk)
        // We store both objective values for Pareto comparison later.
        List<double[]> evaluated = new ArrayList<>();
        for (double[] portfolio : population) {
            double returnValue = problem.objective1(portfolio);
            double riskValue = problem.objective2(portfolio);
            evaluated.add(new double[] {returnValue, riskValue});
        }
        return evaluated;
    }

    // Selection: Select individuals that are Pareto-dominant
    public List<double[]> select(List<double[]> population) {
        // Implement Pareto dominance: keep only the non-dominated solutions
        List<double[]> selected = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            boolean isDominated = false;
            for (int j = 0; j < population.size(); j++) {
                if (i != j && dominates(population.get(i), population.get(j))) {
                    isDominated = true;
                    break;
                }
            }
            if (!isDominated) {
                selected.add(population.get(i));
            }
        }
        return selected;
    }

    // Crossover: Combine two portfolios (simple one-point crossover)
    public double[] crossover(double[] parent1, double[] parent2) {
        double[] offspring = new double[parent1.length];
        int crossoverPoint = rand.nextInt(parent1.length);
        for (int i = 0; i < crossoverPoint; i++) {
            offspring[i] = parent1[i];
        }
        for (int i = crossoverPoint; i < parent2.length; i++) {
            offspring[i] = parent2[i];
        }
        return offspring;
    }

    // Mutation: Apply a small random change to a portfolio
    public void mutate(double[] portfolio) {
        int index = rand.nextInt(portfolio.length);
        portfolio[index] = rand.nextDouble();
        // Normalize the portfolio after mutation so that the sum of weights is 1
        double sum = 0;
        for (double weight : portfolio) {
            sum += weight;
        }
        for (int i = 0; i < portfolio.length; i++) {
            portfolio[i] /= sum;
        }
    }

    // Pareto dominance check
    public boolean dominates(double[] p1, double[] p2) {
        return (p1[0] >= p2[0] && p1[1] <= p2[1]) && (p1[0] > p2[0] || p1[1] < p2[1]);
    }

    // Run MOGA for a set number of generations
    public void run() {
        List<double[]> population = generateInitialPopulation();
        for (int generation = 0; generation < maxGenerations; generation++) {
            // Evaluate population
            List<double[]> evaluated = evaluatePopulation(population);

            // Select non-dominated individuals
            population = select(evaluated);

            // Perform crossover and mutation
            List<double[]> nextGeneration = new ArrayList<>();
            while (nextGeneration.size() < populationSize) {
                // Select two parents randomly from the population
                double[] parent1 = population.get(rand.nextInt(population.size()));
                double[] parent2 = population.get(rand.nextInt(population.size()));

                // Crossover to generate offspring
                double[] offspring = crossover(parent1, parent2);

                // Mutate offspring
                mutate(offspring);

                // Add offspring to the next generation
                nextGeneration.add(offspring);
            }

            // Replace the old generation with the new generation
            population = nextGeneration;

            // Print Pareto front after each generation
            System.out.println("Generation " + generation + ": ");
            for (double[] portfolio : population) {
                System.out.println("Return: " + portfolio[0] + ", Risk: " + portfolio[1]);
            }
        }
    }
}
