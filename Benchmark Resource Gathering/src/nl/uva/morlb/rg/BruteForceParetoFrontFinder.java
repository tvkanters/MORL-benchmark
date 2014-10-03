package nl.uva.morlb.rg;

import java.util.LinkedList;
import java.util.List;

import nl.uva.morlb.rg.agent.model.BenchmarkReward;
import nl.uva.morlb.rg.environment.ResourceGatheringEnv;
import nl.uva.morlb.rg.environment.SdpCollection;
import nl.uva.morlb.rg.environment.model.DiscreteAction;
import nl.uva.morlb.rg.environment.model.Parameters;
import nl.uva.morlb.rg.environment.model.State;
import nl.uva.morlb.rg.experiment.Judge;
import nl.uva.morlb.rg.experiment.model.Solution;
import nl.uva.morlb.rg.experiment.model.SolutionSet;

/**
 * Runs recursively through all possible state action pairs until MAX_DEPTH and finds the pareto front
 */
public class BruteForceParetoFrontFinder {

    private final static int MAX_DEPTH = 10;

    public static void main(final String[] args) {

        Parameters problem = SdpCollection.getSimpleProblem();
        ResourceGatheringEnv environment = new ResourceGatheringEnv(problem);
        environment.env_init();
        environment.env_start();

        List<DiscreteAction> availableActions = new LinkedList<DiscreteAction>();
        for(int action = (problem.finiteHorizon ? 1 : 0); action <= problem.actionMax; ++action) {
            availableActions.add(DiscreteAction.values()[action]);
        }

        int numOfObjectives = problem.numResourceTypes +1;
        SolutionSet paretoFront = new SolutionSet(numOfObjectives);
        for(DiscreteAction action : availableActions) {
            performAction(MAX_DEPTH, environment.getCurrentState(), new BenchmarkReward(new double[numOfObjectives]), action, availableActions, environment, paretoFront);
        }

    }

    /**
     * Perform a given action on a state and add the reward to the pareto front if the next state is terminal and the reward belongs in it
     * @param currentDepth The current depth we are in
     * @param currentState The current state we are in
     * @param rewardSoFar The reward we observed so far within this run
     * @param action The next action to take
     * @param availableActions All available actions in this environment
     * @param environment The current environment
     * @param paretoFront The current pareto front
     */
    private static void performAction(final int currentDepth, final State currentState,final BenchmarkReward rewardSoFar,final DiscreteAction action,final List<DiscreteAction> availableActions, final ResourceGatheringEnv environment, final SolutionSet paretoFront) {
        if(currentDepth == 0) {
            return;
        }

        State nextState = environment.getPossibleTransitions(currentState, action).keySet().iterator().next();
        BenchmarkReward nextReward = new BenchmarkReward(environment.getRewardRanges(currentState, nextState));
        BenchmarkReward currentReward = rewardSoFar.add(nextReward);

        if(environment.isTerminal(nextState)) {
            Solution currentSolution = new Solution(currentReward.getRewardVector());

            if(!paretoFront.isDominated(currentSolution) && paretoFront.addSolution(currentSolution)) {

                System.out.println( paretoFront +" -> ");
                paretoFront.pruneDominatedSolutions();
                System.out.println(paretoFront);
                System.out.println(" Hypervolume: " +Judge.hypervolume(paretoFront));
            }

            return;
        }

        for(DiscreteAction nextAction : availableActions) {
            performAction(currentDepth -1, nextState, currentReward, nextAction, availableActions, environment, paretoFront);
        }
    }
}
