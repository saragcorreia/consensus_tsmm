package pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class MetabolicTask implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id;
	private EnvironmentalConditions reacConstraints;
	private Map<String, Pair<Double, Double>> metaConstraints;
	private String ObjectiveReaction; // maximize a reaction
	private String ObjectiveMetabolite; // when we want maximize the production
										// of an internal metabolite. the
										// associated reaction are created
										// during the task validation
	private Set<ReactionCI> addReactions; //extra reactions that should be appended to container to satisfy task. (not the drains)
	private boolean isMaximization = true;
	private boolean fail;
	
	
	public MetabolicTask(String id) {
		this.id = id;
		this.reacConstraints = new EnvironmentalConditions();
		this.metaConstraints = new HashMap<String, Pair<Double, Double>>();
		this.addReactions = new HashSet<ReactionCI>();
		this.fail = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public EnvironmentalConditions getReacConstraints() {
		return reacConstraints;
	}

	public Map<String, Pair<Double, Double>> getInternalMetaConstraints() {
		return metaConstraints;
	}

	public void setInternalMetaConstraints(Map<String, Pair<Double, Double>> intMetaCons) {
		this.metaConstraints = intMetaCons;
	}

	public void setInternalMetaConstraint(String metaId, Pair<Double, Double> bounds) {
		this.metaConstraints.put(metaId, bounds);
	}

	public void setReacConstraints(EnvironmentalConditions env) {
		this.reacConstraints = env;
	}

	public boolean isFail() {
		return fail;
	}

	public void setFail(boolean fail) {
		this.fail = fail;
	}

	public Set<ReactionCI> getAddicionalReactions() {
		if (addReactions == null)
			addReactions = new HashSet<ReactionCI>();
		return addReactions;
	}

	public void setAddicionalReactions(Set<ReactionCI> addReactions) {
		this.addReactions = addReactions;
	}

	public String getObjectiveReaction() {
		return ObjectiveReaction;
	}

	public void setObjectiveReaction(String objectiveReaction) {
		if (objectiveReaction.startsWith("-")) {
			isMaximization = false;
			ObjectiveReaction = objectiveReaction.substring(1).trim();
		} else
			ObjectiveReaction = objectiveReaction;

	}

	public String getObjectiveMetabolite() {
		return ObjectiveMetabolite;
	}

	public void setObjectiveMetabolite(String objectiveMetabolite) {
		ObjectiveMetabolite = objectiveMetabolite;
	}

	public boolean isMaximization() {
		return isMaximization;
	}

	public void setMaximization(boolean isMaximization) {
		this.isMaximization = isMaximization;
	}

	// Verify in the external reactions that will be added, if already exists in
	// the
	// container, if exist change the id for the id used in container.
	// return the name of reactions that not exist in the orig container
	public Set<String> verifyExistenceInContainer(Container container) {
		Set<String> extraReactions = new HashSet<String>();
		String sameReacId = "";
		if (addReactions != null) {
			for (ReactionCI reaction : addReactions) {
				boolean equal = false;
				for (String rc : container.getReactions().keySet()) {
					equal = reaction.hasSameStoichiometry(container.getReaction(rc), true);
					if (equal) {
						sameReacId = rc;
						break;
					}
				}
				if (equal) {
					reaction.setId(sameReacId);
				} else {
					extraReactions.add(reaction.getId());
				}
			}
		}
		return extraReactions;
	}

}
