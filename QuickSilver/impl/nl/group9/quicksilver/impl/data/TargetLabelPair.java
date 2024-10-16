package nl.group9.quicksilver.impl.data;

/**
 * Record representing the combination of a target vertex and a label.
 * @author Roan
 * @param target The target vertex.
 * @param label The edge label.
 */
public record TargetLabelPair(int target, int label){
}
