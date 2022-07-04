package problems.ecvrp.solver;

import problems.ecvrp.ECVRP;
import problems.ecvrp.Utils;
import solutions.MyPair;
import solutions.Route;
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
		moveRandom2OptClients();
		moveRandom2OptChargingStations();
		return null;
	}

	protected void moveRandom2OptChargingStations() {
		int posRoute1 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
		int posRoute2 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);

		Route route1 = this.sol.get(posRoute1);
		Route route2 = this.sol.get(posRoute2);

		if(route1.getChargingStations().size() > 0 && route2.getChargingStations().size() > 0) {
			int sizeCS = route1.getChargingStations().size();
			int posCS1 = Utils.getRandomNumber(0, sizeCS - 1);

			sizeCS = route2.getChargingStations().size();
			int posCS2 = Utils.getRandomNumber(0, sizeCS - 1);

			MyPair cs1 = route1.getChargingStations().get(posCS1);
			MyPair cs2 = route1.getChargingStations().get(posCS2);

			route1.getChargingStations().set(posCS1, cs2);
			route2.getChargingStations().set(posCS2, cs1);

			this.sol.set(posRoute1, route1);
			this.sol.set(posRoute2, route2);

			Double oldCost = route1.cost + route2.cost;
			route1.cost = ObjFunction.evaluateRoute(route1);
			route2.cost = ObjFunction.evaluateRoute(route2);
			Double newCost = route1.cost + route2.cost;
			this.cost += newCost - oldCost;
		}
	}

	protected void moveRandom2OptClients(){
		int posRoute1 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
		int posRoute2 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);

		Route route1 = this.sol.get(posRoute1);
		Route route2 = this.sol.get(posRoute2);

		if(route1.getClients().size() > 0 && route2.getClients().size() > 0) {
			int sizeClients = route1.getClients().size();
			int posClient1 = Utils.getRandomNumber(0, sizeClients - 1);

			sizeClients = route2.getClients().size();
			int posClient2 = Utils.getRandomNumber(0, sizeClients - 1);

			int client1 = route1.getClients().get(posClient1);
			int client2 = route1.getClients().get(posClient2);

			route1.getClients().set(posClient1, client2);
			route2.getClients().set(posClient2, client1);

			this.sol.set(posRoute1, route1);
			this.sol.set(posRoute2, route2);

			Double oldCost = route1.cost + route2.cost;
			route1.cost = ObjFunction.evaluateRoute(route1);
			route2.cost = ObjFunction.evaluateRoute(route2);
			Double newCost = route1.cost + route2.cost;
			this.cost += newCost - oldCost;
		}
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
