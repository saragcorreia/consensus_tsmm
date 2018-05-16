package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;

/* 
 * FBA simulation with molecular crowding constraint
 * 
 * max vbiomass
 * 
 * 	subject to 
 * 		S.v=0
 * 		vmin <= v <= vmax
 * 
 * 		sum(i=0,n) [ MWi . vi / kcati] <= C
 */


public class FBAwMolecularCrowding extends FBA {

	
	public FBAwMolecularCrowding(ISteadyStateModel model, Map<String, Double> molecularWeightsMap, Map<String, Double> turnoverNumberMap, float metabolic_genes_concentration) {
		super(model);
		
		mandatoryProperties.add(SimulationProperties.MOLECULAR_WEIGHTS); //MW
		mandatoryProperties.add(SimulationProperties.TURNOVER_NUMBER); //kcat
		mandatoryProperties.add(SimulationProperties.MET_GENES_CONC); //metabolic genes concentration
		
		this.setMolecularWeights(molecularWeightsMap);
		this.setTurnoverNumber(turnoverNumberMap);
		this.setMetabolicGenesConcentration(metabolic_genes_concentration);
		
	}
	
	public void setIsMaximization(boolean isMaximization){
		properties.put(SimulationProperties.IS_MAXIMIZATION, isMaximization);
	}
	
	public boolean getIsMaximization() throws PropertyCastException, MandatoryPropertyException{
		return ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
	}
	
	public void setMolecularWeights(Map<String, Double> molecularWeightsList){
		properties.put(SimulationProperties.MOLECULAR_WEIGHTS, molecularWeightsList);
	}
	
	public Map<String, Double> getMolecularWeights() throws PropertyCastException, MandatoryPropertyException{
		return ManagerExceptionUtils.testCast(properties, Map.class, SimulationProperties.MOLECULAR_WEIGHTS, false);
	}

	public void setTurnoverNumber(Map<String, Double> turnoverNumberList){
		properties.put(SimulationProperties.TURNOVER_NUMBER, turnoverNumberList);
	}
	
	public Map<String, Double> getTurnoverNumber() throws PropertyCastException, MandatoryPropertyException{
		return ManagerExceptionUtils.testCast(properties, Map.class, SimulationProperties.TURNOVER_NUMBER, false);
	}
	
	public void setMetabolicGenesConcentration(float metabolic_genes_concentration){
		properties.put(SimulationProperties.MET_GENES_CONC, metabolic_genes_concentration);
	}
	
	public float getMetabolicGenesConcentration() throws PropertyCastException, MandatoryPropertyException{
		return ManagerExceptionUtils.testCast(properties, Float.class, SimulationProperties.MET_GENES_CONC, false);
	}
	
	public void setFBAObjSingleFlux (String fluxId, Double objValueCoef)
	{
		Map <String, Double> m = new HashMap<String,Double>();
		m.put(fluxId, 1.0);
		this.setObjectiveFunction(m);
	}


	@Override
	protected void createConstraints()throws WrongFormulationException, PropertyCastException, MandatoryPropertyException{
		super.createConstraints();
		
		int numberVariables = model.getNumberOfReactions();
		int counter = 0;
		
		LPProblemRow row = new LPProblemRow();
		for(int i=0; i < numberVariables; i++)
		{
			String reaction_name = getIdVar(i);
			//value MWi/kcati
			if(this.getMolecularWeights().containsKey(reaction_name) && this.getTurnoverNumber().containsKey(reaction_name)){
				double reaction_mw = this.getMolecularWeights().get(reaction_name);
				double reaction_turnover = this.getTurnoverNumber().get(reaction_name);
				double ratio = reaction_mw/reaction_turnover;							
			
				if (ratio != 0) try {
					row.addTerm(i, ratio);
				} catch (LinearProgrammingTermAlreadyPresentException e) {
					throw new WrongFormulationException("Cannot add term " + i + "to row with value: " + ratio);
				}
			
			}
			else {
				counter++;
			}		
		}
		
		//for(String reaction_name : this.getTurnoverNumber().keySet()){
		//	Reaction reac = model.getReaction(reaction_name);
		//	if(reac == null) System.out.println("ERRO!: "+reaction_name);			
		//}
		
		//System.out.println("Reaction (total):"+numberVariables);
		//System.out.println("Reaction that do not exist:"+counter);
		
		LPConstraint constraint = 
			new LPConstraint(LPConstraintType.LESS_THAN, row, this.getMetabolicGenesConcentration());
		problem.addConstraint(constraint);
		
	}
	

	
}
