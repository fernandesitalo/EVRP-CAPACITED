package problems;

import solutions.Solution;

import java.util.List;

/**
 * The Evaluator interface gives to a problem the required functionality to
 * obtain a mapping of a solution (n-dimensional array of elements of generic
 * type E (domain)) to a Double (image). It is a useful representation of an
 * objective function for an optimization problem.
 * 
 * @author ccavellucci, fusberti
 * @param <E>
 */
public interface Evaluator<E> {

	public abstract List<Integer> getClients();

	public abstract List<Integer> getChargingStations();

	public abstract Integer getNumberBlocks();

	public abstract Double evaluate(Solution<E> sol);

}
