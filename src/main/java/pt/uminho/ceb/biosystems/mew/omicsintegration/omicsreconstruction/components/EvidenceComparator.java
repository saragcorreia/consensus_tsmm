package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.components;

import java.util.Comparator;

public class EvidenceComparator implements Comparator<ReactionEvidence> {

	private static final EvidenceComparator instance = new EvidenceComparator();

	public EvidenceComparator() {
	}

	public static EvidenceComparator getInstance() {
		return instance;
	}

	@Override
	public int compare(ReactionEvidence re1, ReactionEvidence re2) {
		int res = 0;
		if (re1.getExpression() != re2.getExpression()) {
			res = Double.compare(re1.getExpression(), re2.getExpression());
		}
		// test the second level
		else {
			if (re1.getConnectivity() != re2.getConnectivity()) {
				res = Double.compare(re1.getConnectivity(), re2.getConnectivity());
			}
			// test the third level
			else {
				if (re1.getConfidence() != re2.getConfidence()) {
					res = Double.compare(re1.getConfidence(), re2.getConfidence());

				}
			}
		}
		return res;
	}

	// public static void main(String[] args) {
	// ReactionEvidence e1 = new ReactionEvidence("r1");
	// e1.setExpression(0.0);
	// ReactionEvidence e2 = new ReactionEvidence("r2");
	// e2.setExpression(1.0);
	// e2.setCoreReaction(true);
	// ReactionEvidence e3 = new ReactionEvidence("r3");
	// e3.setExpression(1.0);
	// e3.setCoreReaction(true);
	// ReactionEvidence e4 = new ReactionEvidence("r4");
	// e4.setExpression(1.0);
	// e4.setConnectivity(1.0);
	// e4.setCoreReaction(true);
	// ReactionEvidence e5 = new ReactionEvidence("r5");
	// e5.setExpression(0.03);
	// ReactionEvidence e6 = new ReactionEvidence("r6");
	// e6.setExpression(0.02);
	// ReactionEvidence e7 = new ReactionEvidence("r7");
	// e7.setExpression(0.20);
	// ReactionEvidence e8 = new ReactionEvidence("r8");
	// e8.setExpression(0.20);
	// e8.setConnectivity(1.0);
	// ReactionEvidence e9 = new ReactionEvidence("r9");
	// e9.setExpression(-0.01);
	// ReactionEvidence e10 = new ReactionEvidence("r10");
	// e10.setExpression(0.0);
	//
	// List<ReactionEvidence> reactionEvidences = new
	// ArrayList<ReactionEvidence>();
	// reactionEvidences.add(e1);
	// reactionEvidences.add(e2);
	// reactionEvidences.add(e3);
	// reactionEvidences.add(e4);
	// reactionEvidences.add(e5);
	// reactionEvidences.add(e6);
	// reactionEvidences.add(e7);
	// reactionEvidences.add(e8);
	// reactionEvidences.add(e9);
	// reactionEvidences.add(e10);
	//
	// Collections.sort(reactionEvidences, EvidenceComparator.getInstance());
	//
	// for (int i = 0; !reactionEvidences.get(i).isCoreReactrion(); i++) {
	// System.out.println(reactionEvidences.get(i).getReactionId());
	// }
	//
	// }

}
