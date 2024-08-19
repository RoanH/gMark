package dev.roanh.gmark.ast;

import dev.roanh.gmark.lang.QueryLanguageSyntax;

public record PathTree(PathTree left, PathTree right, OperationType operation, QueryLanguageSyntax atom){
}
