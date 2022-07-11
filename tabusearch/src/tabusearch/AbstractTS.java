/**
 * 
 */
package tabusearch;

import java.util.*;

import problems.Evaluator;
import solutions.Route;
import solutions.Solution;

import static java.lang.Math.abs;

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
//	protected Double bestCost;

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

	protected Integer fleetSize;

	/**
	 * the Tabu List of elements to enter the solution.
	 */
	protected ArrayDeque<List<Integer>> TL;
	
	/**
	 * Creates the Tabu List, which is an ArrayDeque of the Tabu
	 * candidate elements. The number of iterations a candidate
	 * is considered tabu is given by the Tabu Tenure {@link #tenure}
	 * 
	 * @return The Tabu List.
	 */
	public abstract ArrayDeque<List<Integer>> makeTL();

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
	public abstract Solution<E> neighborhoodMove() throws Exception;

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
	public AbstractTS(Evaluator<E> objFunction, Integer tenure, Integer iterations, Integer fleetSize) {
		this.ObjFunction = objFunction;
		this.tenure = tenure;
		this.iterations = iterations;
		this.fleetSize = fleetSize;
	}

	public abstract Solution<E> createARandomSolution() throws Exception;

	public Solution<E> initialSolution() throws Exception {
//		return this.createARandomSolution();
		return this.createGreedSolution();
	}

	/**
	 * The TS mainframe. It consists of a constructive heuristic followed by
	 * a loop, in which each iteration a neighborhood move is performed on
	 * the current solution. The best solution is returned as result.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */

	public Solution<E> solve() throws Exception {

		bestSol = new Solution<E>(this.fleetSize);
		initialSolution();
		TL = makeTL();
		for (int i = 0; i < iterations; i++) {
			neighborhoodMove();
			if (bestSol.cost > sol.cost) {
				bestSol = new Solution<E>(sol);
				bestSol.cost = sol.cost;
				System.out.println("(Iter. " + i + ") BestSol = " + sol.cost + " ,isValid = " + sol.isValid);
			}
		}

		ObjFunction.evaluate(bestSol);
		return bestSol;
	}

	public abstract Solution<E> createGreedSolution() throws Exception;
}
