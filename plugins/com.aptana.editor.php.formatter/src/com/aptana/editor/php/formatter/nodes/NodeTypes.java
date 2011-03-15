/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that holds definitions for arbitrary node types, such as punctuation and operators types.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class NodeTypes
{
	/**
	 * Supported node types for punctuation.<br>
	 * A punctuation type can only have a single char in its name.
	 */
	public enum TypePunctuation
	{
		CASE_COLON(":"), //$NON-NLS-1$
		GOTO_COLON(":"), //$NON-NLS-1$
		SEMICOLON(";"), //$NON-NLS-1$
		FOR_SEMICOLON(";"), //$NON-NLS-1$
		COMMA(","), //$NON-NLS-1$
		ARRAY_COMMA(","), //$NON-NLS-1$
		NAMESPACE_SEPARATOR("\\");//$NON-NLS-1$

		String name;

		TypePunctuation(String name)
		{
			if (name == null || name.length() != 1)
			{
				throw new IllegalArgumentException("Cannot create a TypePunctuation with the name: " + name); //$NON-NLS-1$
			}
			this.name = name;
		}

		public String toString()
		{
			return name;
		}
	};

	/**
	 * Supported node types for operators.
	 */
	public enum TypeOperator
	{
		ASSIGNMENT("="), //$NON-NLS-1$
		DOT("."), //$NON-NLS-1$
		GREATER_THAN(">"), //$NON-NLS-1$
		LESS_THAN("<"), //$NON-NLS-1$
		GREATER_THAN_OR_EQUAL(">="), //$NON-NLS-1$
		LESS_THAN_OR_EQUAL("<="), //$NON-NLS-1$
		DOT_EQUAL(".="), //$NON-NLS-1$
		PLUS_EQUAL("+="), //$NON-NLS-1$
		MINUS_EQUAL("-="), //$NON-NLS-1$
		MULTIPLY_EQUAL("*="), //$NON-NLS-1$
		DIVIDE_EQUAL("/="), //$NON-NLS-1$
		TILDE_EQUAL("~="), //$NON-NLS-1$
		MULTIPLY("*"), //$NON-NLS-1$
		PLUS("+"), //$NON-NLS-1$
		MINUS("-"), //$NON-NLS-1$
		DIVIDE("/"), //$NON-NLS-1$
		MODULUS("%"), //$NON-NLS-1$
		POSTFIX_INCREMENT("++"), //$NON-NLS-1$
		PREFIX_INCREMENT("++"), //$NON-NLS-1$
		POSTFIX_DECREMENT("--"), //$NON-NLS-1$
		PREFIX_DECREMENT("--"), //$NON-NLS-1$
		OR("||"), //$NON-NLS-1$
		OR_LITERAL("or"), //$NON-NLS-1$
		AND("&&"), //$NON-NLS-1$
		AND_LITERAL("and"), //$NON-NLS-1$
		XOR("^"), //$NON-NLS-1$
		XOR_LITERAL("xor"), //$NON-NLS-1$
		BINARY_OR("|"), //$NON-NLS-1$
		BINARY_AND("&"), //$NON-NLS-1$
		OR_EQUAL("|="), //$NON-NLS-1$
		AND_EQUAL("&="), //$NON-NLS-1$
		EQUAL("=="), //$NON-NLS-1$
		TYPE_EQUAL("==="), //$NON-NLS-1$
		TILDE("~"), //$NON-NLS-1$
		NOT("!"), //$NON-NLS-1$
		NOT_EQUAL("!="), //$NON-NLS-1$
		NOT_TYPE_EQUAL("!=="), //$NON-NLS-1$
		ARROW("->"), //$NON-NLS-1$
		STATIC_INVOCATION("::"), //$NON-NLS-1$
		KEY_VALUE("=>"), //$NON-NLS-1$
		CONDITIONAL("?"), //$NON-NLS-1$
		CONDITIONAL_COLON(":"); //$NON-NLS-1$

		String name;

		private static Map<String, TypeOperator> OPERATORS_MAP;

		/**
		 * static initializer
		 */
		static
		{
			OPERATORS_MAP = new HashMap<String, TypeOperator>();
			for (TypeOperator type : EnumSet.allOf(TypeOperator.class))
			{
				OPERATORS_MAP.put(type.toString(), type);
			}
		}

		TypeOperator(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		/**
		 * Returns a {@link TypeOperator} by a string.
		 * 
		 * @param operationString
		 * @return The matching {@link TypeOperator}; Null, if no match was found.
		 */
		public static TypeOperator getTypeOperator(String operationString)
		{
			return OPERATORS_MAP.get(operationString.toLowerCase());
		}
	};
}
