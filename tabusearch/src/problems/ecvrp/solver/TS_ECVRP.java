package problems.ecvrp.solver;

import problems.ecvrp.ECVRP;
import problems.ecvrp.Movement;
import problems.ecvrp.Pair;
import problems.ecvrp.Utils;
import solutions.MyPair;
import solutions.Route;
import solutions.Solution;
import tabusearch.AbstractTS;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TS_ECVRP extends AbstractTS<Route> {

	public TS_ECVRP(Integer tenure, Integer iterations, String filename) throws IOException {
		super(new ECVRP(filename), tenure, iterations);
	}


	@Override
	public ArrayDeque<Movement> makeTL() {
		ArrayDeque<Movement> _TS = new ArrayDeque<>();
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
		List<Pair> listPair = new ArrayList<>();

		for(int i = 0 ; i < 20 ; ++i) {
			Pair c = moveRandom2OptClients(this.sol);
			Pair a = insertAChargingStationInRandomRoute(this.sol);
			Pair b = removeAChargingStationInRandomRoute(this.sol);
			listPair.add(c);
			listPair.add(a);
			listPair.add(b);
		}

		listPair.sort((pair1, pair2) -> {
			if(pair1.cost == pair2.cost) return 0;
			if(pair1.cost > pair2.cost) return 1;
			return -1;
		});

		for(Pair p : listPair){
			if(!this.TL.contains(p.mov) || p.cost <= this.bestCost) {
				applyMove(this.sol, p.mov);
				TL.add(p.mov);
				if (TL.size() > this.tenure*2) {
					TL.pop();
				}
				break;
			}
		}

		return null;
	}

	private void applyMove(Solution<Route> sol, Movement mov) {

	}

	protected Pair insertAChargingStationInRandomRoute(Solution<Route> currentSolution){
		List<Integer> freeChargingStations = getFreeChargingStations().stream().collect(Collectors.toList());

		if (freeChargingStations.size() == 0) {
			return null;
		}

		int pos = Utils.getRandomNumber(0, freeChargingStations.size() - 1);

		int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);

		// TODO: optimize
		int index = Utils.getRandomNumber(0, currentSolution.get(posRoute).getClients().size());

		currentSolution.get(posRoute).getChargingStations().add(new MyPair(freeChargingStations.get(pos), index));

		currentSolution.cost -= currentSolution.get(posRoute).cost;
		Double costRoute = ObjFunction.evaluateRoute(currentSolution.get(posRoute));
		currentSolution.cost += costRoute;
		Movement mov = Movement.builder().type(Utils.MOVE_INSERT_CS).indexes(List.of(posRoute, index)).build();
		return new Pair(currentSolution.cost, mov);
	}

	protected Pair removeAChargingStationInRandomRoute(Solution<Route> currentSolution){
		int posRoute = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
		int index = Utils.getRandomNumber(0, currentSolution.get(posRoute).getClients().size());
		currentSolution.get(posRoute).getChargingStations().remove(index);

		currentSolution.cost -= currentSolution.get(posRoute).cost;
		Double costRoute = ObjFunction.evaluateRoute( currentSolution.get(posRoute));
		currentSolution.cost += costRoute;

		Movement mov = Movement.builder().type(Utils.MOVE_REMOVE_CS).indexes(List.of(posRoute, index)).build();

		return new Pair(currentSolution.cost,mov);
	}

	protected Set<Integer> getFreeChargingStations() {
		Set<Integer> allChargingStations = new HashSet<>(ObjFunction.getChargingStations());

		for(int i = 0; i < this.sol.size() ; ++i) {
			for(int j = 0; j < sol.get(i).getChargingStations().size() ; ++j){
				allChargingStations.remove(sol.get(i).getChargingStations().get(j).chargingStation);
			}
		}
		return allChargingStations;
	}


	protected Pair moveRandom2OptClients(Solution<Route> currentSolution){
		int posRoute1 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);
		int posRoute2 = Utils.getRandomNumber(0, ObjFunction.getNumberRoutes() - 1);

		Route route1 = currentSolution.get(posRoute1);
		Route route2 = currentSolution.get(posRoute2);

		if(route1.getClients().size() > 0 && route2.getClients().size() > 0) {
			int sizeClients = route1.getClients().size();
			int posClient1 = Utils.getRandomNumber(0, sizeClients - 1);

			sizeClients = route2.getClients().size();
			int posClient2 = Utils.getRandomNumber(0, sizeClients - 1);

			int client1 = route1.getClients().get(posClient1);
			int client2 = route1.getClients().get(posClient2);

			route1.getClients().set(posClient1, client2);
			route2.getClients().set(posClient2, client1);

			currentSolution.set(posRoute1, route1);
			currentSolution.set(posRoute2, route2);

			Double oldCost = route1.cost + route2.cost;
			route1.cost = ObjFunction.evaluateRoute(route1);
			route2.cost = ObjFunction.evaluateRoute(route2);
			Double newCost = route1.cost + route2.cost;
			currentSolution.cost += newCost - oldCost;

			Movement mov = Movement.builder().type(Utils.MOVE_CLIENT_NEIGHBORHOOD).indexes(List.of(posRoute1, posRoute2, posClient1, posClient2)).build();

			return new Pair(currentSolution.cost, mov);
		}
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
