/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simplification.model.FVAZeroValueFluxes;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;



public class FBAwMolecularCrowdingFVA {

	ISteadyStateModel model; 
	
	FBAwMolecularCrowding simulCenter;
	
	FluxValueMap wildTypeFluxes=null;
		
	
	public FBAwMolecularCrowdingFVA (ISteadyStateModel model, 
			EnvironmentalConditions envConditions,
			GeneticConditions geneticConditions,
			SolverType solverType,
			Map<String, Double> molecularWeightsMap,
			Map<String, Double> turnoverNumberMap,
			float metabolic_genes_concentration) throws Exception
	{
		this.model = model;
		
		simulCenter = new FBAwMolecularCrowding(model,molecularWeightsMap,turnoverNumberMap,metabolic_genes_concentration);
		simulCenter.setSolverType(solverType);
		simulCenter.setIsMaximization(true);
		simulCenter.setEnvironmentalConditions(envConditions);
		
		SteadyStateSimulationResult res = simulCenter.simulate();
		wildTypeFluxes = res.getFluxValues();
	
	}
	
	// gives maximum or minimum value for a flux (no constraints)
	public double optimumFlux (String fluxId, boolean maximize)
	{
		simulCenter.setFBAObjSingleFlux(fluxId,1.0);
		simulCenter.setIsMaximization(maximize);
		
		double value = Double.NaN;
		try {
			SteadyStateSimulationResult res = simulCenter.simulate();
			value = res.getFluxValues().get(fluxId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return value;
	}

	public double[] limitsFlux (String fluxId)
	{
		double[] res = new double[2];
		
		res[0] = optimumFlux (fluxId, false);
		res[1] = optimumFlux (fluxId, true);
		
		return res;
	}
	

	// gives maximum or minimum value for a flux given a constraint on the biomass
	// (biomass given as percentage of wild type)
	public double optimumFlux (String fluxId, double minimumPercBiomass, boolean maximize) throws Exception
	{
		String biomassId = model.getBiomassFlux();
		double wtBiomass = wildTypeFluxes.get(biomassId);
		double biomassLowerLimit = minimumPercBiomass*wtBiomass;
		
		ReactionConstraint oldConstraints = model.getReactionConstraint(biomassId);
		ReactionConstraint newConstraints = new ReactionConstraint(biomassLowerLimit, oldConstraints.getUpperLimit());
		
		EnvironmentalConditions conditions;
		
		if (this.simulCenter.getEnvironmentalConditions() != null) conditions = this.simulCenter.getEnvironmentalConditions().copy();
		else conditions = new EnvironmentalConditions();
		
		conditions.addReactionConstraint(biomassId, newConstraints);
		
		simulCenter.setFBAObjSingleFlux(fluxId, 1.0);
		simulCenter.setEnvironmentalConditions(conditions);
		simulCenter.setIsMaximization(maximize);
		
		SteadyStateSimulationResult res = simulCenter.simulate();
		
		if (res == null) return Double.NaN;
		else return res.getFluxValues().get(fluxId);
	}

	public double[] limitsFlux (String fluxId, double minPercBiomass) throws Exception
	{
		double[] res = new double[2];
		
		res[0] = optimumFlux (fluxId, minPercBiomass,false);
		res[1] = optimumFlux (fluxId, minPercBiomass,true);
		
		return res;
	}

	
	public Map<String, double[]> limitsAllFluxes(double minimumPercBiomass) throws Exception
	{
		Map<String, double[]> res = new HashMap<String, double[]>();


		for(int i =0; i < model.getNumberOfReactions(); i++){
			
			String reactionId = model.getReactionId(i);

			double r[] = limitsFlux(reactionId, minimumPercBiomass);


			res.put(reactionId, r);
			
		}
		return res;
	}
	
	
	public double[] tightBounds (String fluxId) throws Exception
	{
		return limitsFlux(fluxId, 1.0);
	}
	
	
	/** For a given flux calculates maximum value, for each value of biomass, from
	 * 0% to 100% using interval defined by 2nd argument */	
	public double[] maxValuesFluxAllBiomasses (String fluxId, double interval) 
	throws Exception
	{
		double[] res = new double[(int)(1.0/interval)+1];
		
		double biomassPerc = 0.0;
		
		for(int i=0; i < res.length; i++){
			res[i] = optimumFlux (fluxId, biomassPerc, true);
			biomassPerc += interval;
		}
		
		return res;
	}

	
	public FluxValueMap getWildTypeFluxes() {
		return wildTypeFluxes;
	}


	public void setWildTypeFluxes(FluxValueMap wildTypeFluxes) {
		this.wildTypeFluxes = wildTypeFluxes;
	}
	
	public EnvironmentalConditions getEnvConditions() throws PropertyCastException, MandatoryPropertyException{
		return simulCenter.getEnvironmentalConditions();
	}


	public void setEnvConditions(EnvironmentalConditions envConditions) throws Exception  {
		simulCenter.setEnvironmentalConditions(envConditions);

		SteadyStateSimulationResult res = simulCenter.simulate();
		wildTypeFluxes = res.getFluxValues();

	}


	public GeneticConditions getGeneticConditions () throws PropertyCastException, MandatoryPropertyException
	{
		return this.simulCenter.getGeneticConditions();
	}
	
	public void setGeneticConditions (GeneticConditions gc) throws Exception
	{
		simulCenter.setGeneticConditions(gc);
		
		SteadyStateSimulationResult res = simulCenter.simulate();
		wildTypeFluxes = res.getFluxValues();
	}
	
	public FVAZeroValueFluxes identifyFVAZeroFluxes() throws Exception{
		Map<String, double[]> fluxLimits = this.limitsAllFluxes(0.0);
		List<String> zeroF = new ArrayList<String>();
		
		for(String fId : fluxLimits.keySet()){
			if(fluxLimits.get(fId)[0]==0 && fluxLimits.get(fId)[1]==0)
				zeroF.add(fId);
		}
		
		FVAZeroValueFluxes zerovalues = new FVAZeroValueFluxes(zeroF, this.getEnvConditions());
		return zerovalues;
	}
	
}
