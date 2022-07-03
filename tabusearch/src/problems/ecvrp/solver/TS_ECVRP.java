package problems.ecvrp.solver;

import problems.ecvrp.ECVRP;
import solutions.Route;
import solutions.MyPair;
import solutions.Solution;
import tabusearch.AbstractTS;

import java.io.IOException;
import java.util.ArrayDeque;

public class TS_ECVRP extends AbstractTS<Route> {

	private final Route fake = Route.builder().build();
	
	public TS_ECVRP(Integer tenure, Integer iterations, String filename) throws IOException {
		super(new ECVRP(filename), tenure, iterations);
	}


	/**
	 * Creates the Tabu List, which is an ArrayDeque of the Tabu
	 * candidate elements. The number of iterations a candidate
	 * is considered tabu is given by the Tabu Tenure {@link #tenure}
	 *
	 * @return The Tabu List.
	 */
	@Override
	public ArrayDeque<Route> makeTL() {
		ArrayDeque<Route> _TS = new ArrayDeque<Route>(2*tenure);
		for (int i=0; i<2*tenure; i++) {
			_TS.add(fake);
		}
		return _TS;
	}


	@Override
	public Solution<Route> createEmptySol() {
		Solution<Route> sol = new Solution<Route>();
		sol.cost = 0.0;
		return sol;
	}


	@Override
	public Solution<Route> neighborhoodMove() {
		return null;
	}

	
	// just to tests
	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();

		TS_ECVRP tabusearch = new TS_ECVRP(20, 1000, "instances/c101_21.txt");

		Solution<Route> bestSol = tabusearch.solve();

		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");

	}

}
