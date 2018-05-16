package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.MetaboliteDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.CheckTasks;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.tINITConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.tINIT;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;
/**
 * Assumption: ReactionDataMap is the first element and MetaboliteDataMap the second in the omics list.
 * @author Sara
 *
 */
public class tINITAlgorithm extends AbstractReconstructionAlgorithm {
	private tINIT tINITFormulation;
	
	// constructors
	public tINITAlgorithm(tINITConfiguration configuration) {
		super(configuration);
		
	}
	
	// generate tissue model
	public SpecificModelResult generateSpecificModel() throws Exception {
		
		Map<String, Set<String>> requiredReactions = getRequiredReactions();
		
		
		buidSteadyStateModel(); // SGC:change this ...model must be build in the new instance of class
		
		// set required Reactions
		getConfig().setRequiredReactions(requiredReactions);

		tINITFormulation = new tINIT(model, getConfig());
		
		SteadyStateSimulationResult solution = tINITFormulation.simulate();
		
		Set<String> toRemove = getFinalModelReacs(solution);
		return buildTissueSpecificModel(toRemove);
	}


	private Map<String, Set<String>> getRequiredReactions() throws Exception{
		Map<String, Set<String>> requiredReactions = null;;
		
		if (getConfig().geMetabolicTasks() != null) {
			CheckTasks c = new CheckTasks(getConfig().getTemplateContainer(), getConfig().geMetabolicTasks(), true, getConfig().getFluxThreshold(),
					getConfig().getSolverType());

			System.out.println("REQUIRED REACTIONS"+ getConfig().getSolverType());
			requiredReactions = c.getRequiredReactions();
			System.out.println("REQUIRED REACTIONS");
			for (String k : requiredReactions.keySet()) {
				System.out.println("___________________");
				System.out.println(k);
				for (String reac : requiredReactions.get(k))
					System.out.println(reac);
			}
		}
		return requiredReactions;

	}
	private Set<String> getFinalModelReacs(SteadyStateSimulationResult solution) {		
		MapStringNum map = solution.getComplementaryInfoReactions().get("tINIT");
		Set<String> reacs = new HashSet<String>();
		
		
		for(Map.Entry<String, Double>entry: map.entrySet()){
			// entry == 0  impossible entry = 1 reaction active  entry =2 reaction inactive
			System.out.println(entry.getKey() + " " + entry.getValue() + " " + solution.getFluxValues().get(entry.getKey()));
			if(entry.getValue()==1)
				reacs.add(entry.getKey());
		}
		return reacs;
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "tINIT";
	}
	
	private tINITConfiguration getConfig(){
		return (tINITConfiguration)configuration;
	}
}
