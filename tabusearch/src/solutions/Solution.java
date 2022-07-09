package solutions;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Solution<E> {
	
	public double cost;
	public List<Route> routes;
	
	public Solution(int fleetSize) {
		this.routes = new ArrayList<>();
		for (int i = 0 ; i < fleetSize; i++) {
			this.routes.add(new Route());
		}
		this.cost = Double.MAX_VALUE;
	}
	
	public Solution(Solution<E> sol) {
		ArrayList<Route> copyRoutes = new ArrayList<>();
		this.cost = 0;
		for (Route r : sol.routes) {
			copyRoutes.add(r.getCopy());
			this.cost += r.cost;
		}

		this.routes = copyRoutes;
	}

	public Route getRouteCopy(int routeIdx) {
		Route r = routes.get(routeIdx);
		return new Route(r.clients, r.chargingStations, r.cost);
	}

	public Route getRoute(int routeIdx) {
		return routes.get(routeIdx);
	}

	@Override
	public String toString() {
		return "Solution: cost=[" + cost + "], fleetSize=[" + this.routes.size() + "], elements=" + super.toString();
	}

	public void addClient(int randomRoutIdx, int client) {
		this.routes.get(randomRoutIdx).addClient(client);
	}

	public void addCS(int routeIdx, int clientIndex, int cs) {
		RechargePoint rp = new RechargePoint(cs, clientIndex);
		this.routes.get(routeIdx).addCS(rp);
	}

	public void removeCS(int routeIdx, int indexToRemove) {
		this.routes.get(routeIdx).removeCS(indexToRemove);
	}
}

