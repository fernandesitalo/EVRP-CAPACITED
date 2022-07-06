/**
 * 
 */
package tabusearch;

import java.util.*;

import problems.Evaluator;
import problems.ecvrp.Movement;
import problems.ecvrp.Utils;
import solutions.Route;
import solutions.Solution;

/**
 * Abstract class for metaheuristic Tabu Search. It consider a minimization problem.
 * 
 * @author ccavellucci, fusberti
 * @param <E>
 *            Generic type of the candidate to enter the solution.
 */
public abstract class AbstractTS<E> {

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = true;

	/**
	 * a random number generator
	 */
	static Random rng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<E> ObjFunction;

	/**
	 * the best solution cost
	 */
	protected Double bestCost;

	/**
	 * the incumbent solution cost
	 */
	protected Double cost;

	/**
	 * the best solution
	 */
	protected Solution<E> bestSol;

	/**
	 * the incumbent solution
	 */
	protected Solution<E> sol;

	/**
	 * the number of iterations the TS main loop executes.
	 */
	protected Integer iterations;
	
	/**
	 * the tabu tenure.
	 */
	protected Integer tenure;


	/**
	 * the Tabu List of elements to enter the solution.
	 */
	protected ArrayDeque<Movement> TL;
	
	/**
	 * Creates the Tabu List, which is an ArrayDeque of the Tabu
	 * candidate elements. The number of iterations a candidate
	 * is considered tabu is given by the Tabu Tenure {@link #tenure}
	 * 
	 * @return The Tabu List.
	 */
	public abstract ArrayDeque<Movement> makeTL();

	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * candidate solution element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<E> createEmptySol();

	/**
	 * The TS local search phase is responsible for repeatedly applying a
	 * neighborhood operation while the solution is getting improved, i.e.,
	 * until a local optimum is attained. When a local optimum is attained
	 * the search continues by exploring moves which can make the current 
	 * solution worse. Cycling is prevented by not allowing forbidden
	 * (tabu) moves that would otherwise backtrack to a previous solution.
	 * 
	 * @return An local optimum solution.
	 */
	public abstract Solution<E> neighborhoodMove();

	/**
	 * Constructor for the AbstractTS class.
	 * 
	 * @param objFunction
	 *            The objective function being minimized.
	 * @param tenure
	 *            The Tabu tenure parameter. 
	 * @param iterations
	 *            The number of iterations which the TS will be executed.
	 */
	public AbstractTS(Evaluator<E> objFunction, Integer tenure, Integer iterations) {
		this.ObjFunction = objFunction;
		this.tenure = tenure;
		this.iterations = iterations;
	}

	public abstract Solution<E> createARandomSolution();

	public Solution<E> initialSolution() {
		return this.createARandomSolution();
	}
	public abstract void createInitialSolution();

	/**
	 * The TS mainframe. It consists of a constructive heuristic followed by
	 * a loop, in which each iteration a neighborhood move is performed on
	 * the current solution. The best solution is returned as result.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<E> solve() {

		bestSol = createEmptySol();
//		initialSolution();
		createInitialSolution();

		TL = makeTL();
		for (int i = 0; i < iterations; i++) {
			neighborhoodMove();
			if (bestSol.cost > sol.cost) {
				bestSol = new Solution<E>(sol);
				if (verbose)
					System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
			}
		}

		return bestSol;
	}

	/**
	 * A standard stopping criteria for the constructive heuristic is to repeat
	 * until the incumbent solution improves by inserting a new candidate
	 * element.
	 * 
	 * @return true if the criteria is met.
	 */
	public Boolean constructiveStopCriteria() {
		return cost <= sol.cost	;
	}
}
