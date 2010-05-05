/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.core.ast.nodes;

import org.eclipse.php.internal.core.ast.visitor.Visitor;

/**
 * Represents a function formal parameter
 * <pre>e.g.<pre> $a,
 * MyClass $a,
 * $a = 3,
 * int $a = 3		
 */
public class FormalParameter extends ASTNode {

	public static final StructuralPropertyDescriptor PARAMETER_TYPE_PROPERTY = new StructuralPropertyDescriptor();
	private final Expression parameterType;
	private final Expression parameterName;
	private final Expression defaultValue;
	private final boolean isMandatory;

	private FormalParameter(int start, int end, Expression type, final Expression parameterName, Expression defaultValue, boolean isMandatory) {
		super(start, end);

		assert parameterName != null;
		this.parameterType = type;
		this.parameterName = parameterName;
		this.defaultValue = defaultValue;
		this.isMandatory = isMandatory;

		if (type != null) {
			type.setParent(this);
			type.setDescriptor(FormalParameter.PARAMETER_TYPE_PROPERTY);
		}
		parameterName.setParent(this);
		if (defaultValue != null) {
			defaultValue.setParent(this);
		}
	}

	public FormalParameter(int start, int end, Identifier type, final Variable parameterName, Expression defaultValue) {
		this(start, end, type, (Expression) parameterName, defaultValue, false);
	}

	public FormalParameter(int start, int end, Expression type, final Reference parameterName, Expression defaultValue) {
		this(start, end, type, (Expression) parameterName, defaultValue, false);
	}

	public FormalParameter(int start, int end, Expression type, final Variable parameterName) {
		this(start, end, type, (Expression) parameterName, null, false);
	}

	public FormalParameter(int start, int end, Expression type, final Variable parameterName, boolean isMandatory) {
		this(start, end, type, (Expression) parameterName, null, isMandatory);
	}

	public FormalParameter(int start, int end, Expression type, final Reference parameterName) {
		this(start, end, type, (Expression) parameterName, null, false);
	}

	public FormalParameter(int s, int scalarright, Expression classType,
			Variable v, Expression scalar) {
		this(s,scalarright,classType,v,scalar,true);
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public void childrenAccept(Visitor visitor) {
		if (parameterType != null) {
			parameterType.accept(visitor);
		}
		parameterName.accept(visitor);
		if (defaultValue != null) {
			defaultValue.accept(visitor);
		}
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		if (parameterType != null) {
			parameterType.traverseTopDown(visitor);
		}
		parameterName.traverseTopDown(visitor);
		if (defaultValue != null) {
			defaultValue.traverseTopDown(visitor);
		}
	}

	public void traverseBottomUp(Visitor visitor) {
		if (parameterType != null) {
			parameterType.traverseBottomUp(visitor);
		}
		parameterName.traverseBottomUp(visitor);
		if (defaultValue != null) {
			defaultValue.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<FormalParameter"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" isMandatory='").append(isMandatory).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(TAB).append(tab).append("<Type>\n"); //$NON-NLS-1$
		if (parameterType != null) {
			parameterType.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</Type>\n"); //$NON-NLS-1$
		buffer.append(TAB).append(tab).append("<ParameterName>\n"); //$NON-NLS-1$
		parameterName.toString(buffer, TAB + TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(TAB).append(tab).append("</ParameterName>\n"); //$NON-NLS-1$
		buffer.append(TAB).append(tab).append("<DefaultValue>\n"); //$NON-NLS-1$
		if (defaultValue != null) {
			defaultValue.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</DefaultValue>\n"); //$NON-NLS-1$
		buffer.append(tab).append("</FormalParameter>"); //$NON-NLS-1$
	}

	public int getType() {
		return ASTNode.FORMAL_PARAMETER;
	}

	public Expression getDefaultValue() {
		return defaultValue;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public Expression getParameterName() {
		return parameterName;
	}

	public Expression getParameterType() {
		return parameterType;
	}

	/**
	 * @return Identifier name of the formal parameter name  
	 */
	public Identifier getParameterNameIdentifier() {
		Expression expression = parameterName;
		switch (parameterName.getType()) {
			case ASTNode.REFERENCE:
				expression = ((Reference) expression).getExpression();
				if (expression.getType() != ASTNode.VARIABLE) {
					throw new IllegalStateException();
				}
			case ASTNode.VARIABLE:
				final Identifier variableName = (Identifier) ((Variable) expression).getVariableName();
				return variableName;
		}
		throw new IllegalStateException();
	}
}
