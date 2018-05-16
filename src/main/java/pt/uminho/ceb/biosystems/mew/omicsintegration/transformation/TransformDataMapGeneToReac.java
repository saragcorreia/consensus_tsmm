package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.GeneDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TransformException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.Plus;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTreeNode;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.Environment;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.IEnvironment;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DoubleValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.And;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Maximum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Mean;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Minimum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Or;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Variable;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.VariableDouble;

public class TransformDataMapGeneToReac implements ITransformDataMap {

	private Container container;
	private String opAnd;
	private String opOr;
	
	public static String VAR_CONTAINER = "Container";
	public static String VAR_OPERATION_AND = "And operation";
	public static String VAR_OPERATION_OR = "Or operation";
	
		
	
static Map<String, Class<? extends  AbstractSyntaxTreeNode<DataTypeEnum, IValue>>> map = new HashMap<>();
	
	static void putTransformData(String id, Class<? extends AbstractSyntaxTreeNode<DataTypeEnum, IValue>> klass) throws TransformException{
		try {
			klass.getConstructor(AbstractSyntaxTreeNode.class, AbstractSyntaxTreeNode.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TransformException("Class " +klass.getName()+" don't have contructor with correct parameters!");
		}
		map.put(id, klass);
	}
	
	static{
		try {
			putTransformData("PLUS", Plus.class);
			putTransformData("MEAN", Mean.class);
			putTransformData("MAX", Maximum.class);
			putTransformData("MIN", Minimum.class);
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public TransformDataMapGeneToReac(Container container ) {
		this.container = container;
		opOr = "MAX";
		opAnd = "MIN";	
	}
	
	public TransformDataMapGeneToReac(Map<String,Object> properties ) {
		this.container = (Container) properties.get(VAR_CONTAINER);
		opOr = properties.containsKey(VAR_OPERATION_OR)?(String)properties.get(VAR_OPERATION_OR):"MAX";
		opAnd = properties.containsKey(VAR_OPERATION_AND)?(String)properties.get(VAR_OPERATION_AND):"MIN";	
	}

	@Override
	public IOmicsDataMap transform(IOmicsDataMap omics) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException { 
		// calculate the reaction value considering the gene rule
		Map<String, Double> geneValues = omics.getMapValues();
		IEnvironment<IValue> environment = new Environment<IValue>();
		Map<String, Double> reacValues = new HashMap<String, Double>();

		for (String gene : container.getGenes().keySet()) {
			// when gene don't have weight put NAN
			if (geneValues.containsKey(gene))
				environment.associate(gene, new DoubleValue(geneValues.get(gene)));
			else
				environment.associate(gene, new DoubleValue(Double.NaN));
		}
		for (Map.Entry<String, ReactionCI> e : container.getReactions().entrySet()) {
			if (e.getValue().getGeneRule() != null && !e.getValue().getGeneRuleString().equals("")) {
				double value = convertGeneRule(e.getValue().getGeneRule().getRootNode(), environment)
						.evaluate(environment).getNumericValue();
				reacValues.put(e.getKey(), value);

				}
			}
		
		return new ReactionDataMap(reacValues, container,((GeneDataMap) omics).getCondition());
	}

	private AbstractSyntaxTreeNode<DataTypeEnum, IValue> convertGeneRule(
			AbstractSyntaxTreeNode<DataTypeEnum, IValue> geneRule, IEnvironment<IValue> environment) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		AbstractSyntaxTreeNode<DataTypeEnum, IValue> geneRegulationRule = null;
		if (geneRule instanceof And) {
			geneRegulationRule = map.get(opAnd).getConstructor(AbstractSyntaxTreeNode.class, AbstractSyntaxTreeNode.class).newInstance(convertGeneRule(geneRule.getChildAt(0), environment),convertGeneRule(geneRule.getChildAt(1), environment));
//			geneRegulationRule = new Minimum(convertGeneRule(geneRule.getChildAt(0), environment),
//					convertGeneRule(geneRule.getChildAt(1), environment));
		}
		if (geneRule instanceof Or) {
			geneRegulationRule = map.get(opOr).getConstructor(AbstractSyntaxTreeNode.class, AbstractSyntaxTreeNode.class).newInstance(convertGeneRule(geneRule.getChildAt(0), environment),convertGeneRule(geneRule.getChildAt(1), environment));

//			geneRegulationRule = new Maximum(convertGeneRule(geneRule.getChildAt(0), environment),
//					convertGeneRule(geneRule.getChildAt(1), environment));
		}
		if (geneRule instanceof Variable) {
			String geneID = ((Variable) geneRule).toString();
			double geneExpressionValue = environment.find(geneID).getNumericValue();
			geneRegulationRule = new VariableDouble(geneID, geneExpressionValue);
		}

		return geneRegulationRule;
	}

}
