package dev.roanh.gmark.data;

/**
 * Record representing the combination of a source vertex and a label.
 * @author Roan
 * @param source The source vertex.
 * @param label The edge label.
 */
public record SourceLabelPair(int source, int label){
}
