package problems.ecvrp.solver;

import problems.ecvrp.ECVRP;
import solutions.Block;
import solutions.Solution;
import tabusearch.AbstractTS;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class TS_ECVRP extends AbstractTS<Block> {

	private final Integer fake = -1;
	
	public TS_ECVRP(Integer tenure, Integer iterations, String filename) throws IOException {
		super(new ECVRP(filename), tenure, iterations);
	}

	@Override
	public ArrayList<Block> makeCL() {
		ArrayList<Block> _CL = new ArrayList<Block>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = i;
			_CL.add(cand);
		}
		return _CL;
	}


	@Override
	public ArrayList<Block> makeRCL() {
		ArrayList<Block> _RCL = new ArrayList<Block>();
		return _RCL;
	}
	

	@Override
	public ArrayDeque<Block> makeTL() {
		ArrayDeque<Integer> _TS = new ArrayDeque<Integer>(2*tenure);
		for (int i=0; i<2*tenure; i++) {
			_TS.add(fake);
		}
		return _TS;
	}
	
	@Override
	public void updateCL() {
		// do nothing
	}


	@Override
	public Solution<Block> createEmptySol() {
		Solution<Block> sol = new Solution<Block>();
		sol.cost = 0.0;
		return sol;
	}


	@Override
	public Solution<Block> neighborhoodMove() {

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		minDeltaCost = Double.POSITIVE_INFINITY;
		updateCL();
		// Evaluate insertions
		for (Block candIn : CL) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
			if (!TL.contains(candIn) || sol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
		}
		// Evaluate removals
		for (Block candOut : sol) {
			Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
			if (!TL.contains(candOut) || sol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
		}
		// Evaluate exchanges
		for (Block candIn : CL) {
			for (Block candOut : sol) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || sol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
		}
		// Implement the best non-tabu move
		TL.poll();
		if (bestCandOut != null) {
			sol.remove(bestCandOut);
			CL.add(bestCandOut);
			TL.add(bestCandOut);
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (bestCandIn != null) {
			sol.add(bestCandIn);
			CL.remove(bestCandIn);
			TL.add(bestCandIn);
		} else {
			TL.add(fake);
		}
		ObjFunction.evaluate(sol);
		
		return null;
	}

	
	// just to tests
	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();

		TS_ECVRP tabusearch = new TS_ECVRP(20, 1000, "instances/c101_21.txt");

		Solution<Integer> bestSol = tabusearch.solve();

		System.out.println("maxVal = " + bestSol);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");

	}

}
