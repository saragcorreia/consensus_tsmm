package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.components;

//calculation of the 3 types of reaction evidences, used in mCADRE algorithm to sort the reactions order to be removed  
public class ReactionEvidence {
	private String reactionId;
	private double expression;
	private double connectivity;
	private double confidence;
	private boolean isCore;
	
	public ReactionEvidence(String id){
		reactionId = id;
		isCore = false;
	}

	public String getReactionId() {
		return reactionId;
	}

	public void setReactionId(String reactionId) {
		this.reactionId = reactionId;
	}

	public double getExpression() {
		return expression;
	}

	public void setExpression(double expression) {
		this.expression = expression;
	}

	public double getConnectivity() {
		return connectivity;
	}

	public void setConnectivity(double connectivity) {
		this.connectivity = connectivity;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public boolean isCoreReactrion(){
		return isCore;
	}
	
	public void setCoreReaction(boolean isCore){
		this.isCore = isCore;
	}

}
