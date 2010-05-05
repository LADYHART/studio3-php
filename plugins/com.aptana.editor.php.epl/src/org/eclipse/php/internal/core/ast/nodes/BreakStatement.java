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
 * Represent a break statement
 * <pre>e.g.<pre> break;
 * break $a;
 */
public class BreakStatement extends Statement {

	private final Expression expr;

	public BreakStatement(int start, int end) {
		this(start, end, null);
	}

	public BreakStatement(int start, int end, Expression expr) {
		super(start, end);
		this.expr = expr;

		if (expr != null) {
			expr.setParent(this);
		}
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public void childrenAccept(Visitor visitor) {
		if (expr != null) {
			expr.accept(visitor);
		}
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		if (expr != null) {
			expr.traverseTopDown(visitor);
		}
	}

	public void traverseBottomUp(Visitor visitor) {
		if (expr != null) {
			expr.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<BreakStatement"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$
		if (expr != null) {
			expr.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(tab).append("</BreakStatement>"); //$NON-NLS-1$
	}

	public int getType() {
		return ASTNode.BREAK_STATEMENT;
	}

	public Expression getExpr() {
		return expr;
	}
}
