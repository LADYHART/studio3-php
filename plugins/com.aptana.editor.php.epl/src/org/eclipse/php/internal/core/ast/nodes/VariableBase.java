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

import com.aptana.editor.php.typebinding.ITypeBinding;

/**
 * This interface is base for all the PHP variables
 * including simple variable, function invocation, list, dispatch, etc.
 */
public abstract class VariableBase extends Expression {

	public VariableBase(int start, int end) {
		super(start, end);
	}

	public ITypeBinding resolveTypeBinding() {
		return (ITypeBinding) getBinding();
	}

}
