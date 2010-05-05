/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Block;
import org.eclipse.php.internal.core.ast.nodes.CatchClause;
import org.eclipse.php.internal.core.ast.nodes.ClassConstantDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ClassInstanceCreation;
import org.eclipse.php.internal.core.ast.nodes.ClassName;
import org.eclipse.php.internal.core.ast.nodes.Comment;
import org.eclipse.php.internal.core.ast.nodes.Dispatch;
import org.eclipse.php.internal.core.ast.nodes.DoStatement;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org.eclipse.php.internal.core.ast.nodes.FieldAccess;
import org.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ForEachStatement;
import org.eclipse.php.internal.core.ast.nodes.ForStatement;
import org.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.FunctionName;
import org.eclipse.php.internal.core.ast.nodes.GlobalStatement;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.IfStatement;
import org.eclipse.php.internal.core.ast.nodes.Include;
import org.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.LambdaFunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org.eclipse.php.internal.core.ast.nodes.MethodInvocation;
import org.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.ast.nodes.Quote;
import org.eclipse.php.internal.core.ast.nodes.ReturnStatement;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.StaticDispatch;
import org.eclipse.php.internal.core.ast.nodes.StaticFieldAccess;
import org.eclipse.php.internal.core.ast.nodes.StaticStatement;
import org.eclipse.php.internal.core.ast.nodes.SwitchStatement;
import org.eclipse.php.internal.core.ast.nodes.TryStatement;
import org.eclipse.php.internal.core.ast.nodes.TypeDeclaration;
import org.eclipse.php.internal.core.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.nodes.VariableBase;
import org.eclipse.php.internal.core.ast.nodes.WhileStatement;
import org.eclipse.php.internal.core.ast.parser.ASTParser;
import org.eclipse.php.internal.core.ast.parser.PhpAstLexer53;
import org.eclipse.php.internal.core.phpModel.javacup.runtime.Symbol;
import org.eclipse.php.internal.core.phpModel.parser.php5.ParserConstants5;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPModifier;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.ASTVisitorRegistry;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IIndexReporter;
import com.aptana.editor.php.indexer.IIndexingASTVisitor;
import com.aptana.editor.php.indexer.IModuleIndexer;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.IProgramIndexer;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.utils.EncodingUtils;
import com.aptana.editor.php.utils.PHPASTVisitorProxy;
import com.aptana.editor.php.utils.PHPASTVisitorStub;

/**
 * PDTPHPModuleIndexer
 * 
 * @author Denis Denisenko
 */
public class PDTPHPModuleIndexer implements IModuleIndexer, IProgramIndexer {
	// private static final TaskTagsUpdater updater = new TaskTagsUpdater();

	/**
	 * This.
	 */
	private static final String THIS = "this"; //$NON-NLS-1$

	/**
	 * Self.
	 */
	private static final String SELF = "self"; //$NON-NLS-1$

	/**
	 * Define function name.
	 */
	private static final String DEFINE = "define"; //$NON-NLS-1$

	/**
	 * Variable info.
	 * 
	 * @author Denis Denisenko
	 */
	private class VariableInfo {
		/**
		 * Variable name.
		 */
		private String variableName;

		/**
		 * Variable types.
		 */
		private Set<Object> variableTypes;

		/**
		 * Variable scope.
		 */
		private Scope scope;

		/**
		 * Node start.
		 */
		private int nodeStart = 0;

		private int modifier = 0;

		/**
		 * VariableInfo constructor.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param variableType
		 *            - variable type.
		 * @param scope
		 * @param pos
		 */
		public VariableInfo(String variableName, Object variableType,
				Scope scope, int pos) {
			this.variableName = variableName;
			variableTypes = new HashSet<Object>(1);
			if (variableType != null) {
				variableTypes.add(variableType);
			}
			this.scope = scope;
			nodeStart = pos;

			grabDockedTypes();
		}

		/**
		 * VariableInfo constructor.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param variableTypes
		 *            - variable types.
		 * @param scope
		 * @param pos
		 */
		public VariableInfo(String variableName, Set<Object> variableTypes,
				Scope scope, int pos) {
			this.variableName = variableName;

			this.variableTypes = variableTypes;

			this.scope = scope;
			nodeStart = pos;

			grabDockedTypes();
		}

		/**
		 * VariableInfo constructor.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param variableTypes
		 *            - variable types.
		 * @param scope
		 *            - variable scope.
		 * @param pos
		 *            - variable declaration position.
		 * @param modifier
		 *            - variable modifier.
		 */
		public VariableInfo(String variableName, Set<Object> variableTypes,
				Scope scope, int pos, int modifier) {
			this.variableName = variableName;

			this.variableTypes = variableTypes;

			this.scope = scope;
			nodeStart = pos;
			this.modifier = modifier;

			grabDockedTypes();
		}

		/**
		 * Gets variable type.
		 * 
		 * @return variable type.
		 */
		public Set<Object> getVariableTypes() {
			if (variableTypes != null) {
				return variableTypes;
			} else {
				return Collections.emptySet();
			}
		}

		/**
		 * Sets variable type.
		 * 
		 * @param variableType
		 *            - type to set.
		 */
		public void setVariableType(Object variableType) {
			variableTypes = new HashSet<Object>(1);
			variableTypes.add(variableType);
		}

		/**
		 * Sets variable types.
		 * 
		 * @param variableType
		 */
		public void setVariableTypes(Collection<Object> variableType) {
			variableTypes = new HashSet<Object>();
			variableTypes.addAll(variableType);
		}

		/**
		 * Adds variable type.
		 * 
		 * @param variableType
		 *            - type to add.
		 */
		public void addVariableType(Object variableType) {
			if (variableTypes == null) {
				variableTypes = new HashSet<Object>();
			}
			variableTypes.add(variableType);
		}

		/**
		 * Gets variable name.
		 * 
		 * @return variable name.
		 */
		public String getName() {
			return variableName;
		}

		/**
		 * Gets scope.
		 * 
		 * @return scope.
		 */
		public Scope getScope() {
			return scope;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(variableName);
			buffer.append(" types:"); //$NON-NLS-1$
			buffer.append(variableTypes.toString());
			return buffer.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((variableName == null) ? 0 : variableName.hashCode());
			return result;
		}

		/**
		 * Gets node start.
		 * 
		 * @return node start.
		 */
		public int getNodeStart() {
			return nodeStart;
		}

		// /**
		// * Sets node start.
		// * @param nodeStart - node start to set.
		// */
		// public void setNodeStart(int nodeStart)
		// {
		// this.nodeStart = nodeStart;
		// }

		/**
		 * Gets node modifier.
		 * 
		 * @return modifier
		 */
		public int getModifier() {
			return modifier;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final VariableInfo other = (VariableInfo) obj;
			if (variableName == null) {
				if (other.variableName != null)
					return false;
			} else if (!variableName.equals(other.variableName))
				return false;
			return true;
		}

		/**
		 * Checks if there are any documented types for a variable.
		 */
		private void grabDockedTypes() {
			String comment = findFunctionPHPDocComment(this.nodeStart);
			/* TODO - Shalom: Hook the PHPDoc Parser
			if (comment != null) {
				FunctionDocumentation documentation = PHPDocUtils
						.parseFunctionPHPDoc(comment);
				if (documentation != null) {
					ArrayList<TypedDescription> vars = documentation.getVars();
					if (vars != null && vars.size() != 0) {
						if (this.variableTypes == null) {
							variableTypes = new HashSet<Object>();
						}
						for (TypedDescription descr : vars) {
							String[] types = descr.getTypes();
							for (String type : types) {
								variableTypes.add(type);
							}
						}
					}
				}
			}
			*/
		}
	}

	/**
	 * Variables scope.
	 * 
	 * @author Denis Denisenko
	 */
	private static class Scope {
		/**
		 * Scope root.
		 */
		private ASTNode root;

		/**
		 * Root entry.
		 */
		private IElementEntry entry;

		/**
		 * Scope variables.
		 */
		private Map<String, VariableInfo> variables;

		/**
		 * Scope parent.
		 */
		private Scope parent;

		/**
		 * Set of variables imported from global scope.
		 */
		private Set<String> globalImports = new HashSet<String>();

		private HashMap<String, String> aliases = new HashMap<String, String>();

		/**
		 * Scope constructor.
		 * 
		 * @param root
		 *            - scope root.
		 * @param parent
		 *            - scope parent.
		 */
		public Scope(ASTNode root, Scope parent) {
			this.root = root;
			this.parent = parent;
			this.entry = null;
			variables = new HashMap<String, VariableInfo>(1);
		}

		/**
		 * Scope constructor.
		 * 
		 * @param root
		 *            - scope root.
		 * @param parent
		 *            - scope parent.
		 * @param entry
		 *            - root entry if exist. null is acceptable.
		 */
		public Scope(ASTNode root, Scope parent, IElementEntry entry) {
			this.root = root;
			this.parent = parent;
			this.entry = entry;
			variables = new HashMap<String, VariableInfo>(1);
		}

		/**
		 * Gets scope global imports.
		 * 
		 * @return scope golobal imports.
		 */
		public Set<String> getGlobalImports() {
			return globalImports;
		}

		/**
		 * Gets scope global imports.
		 * 
		 * @return scope golobal imports.
		 */
		public Map<String, String> getAliases() {
			return aliases;
		}

		/**
		 * Adds variables to import from global scope.
		 * 
		 * @param imports
		 *            - set of imports.
		 */
		public void addGlobalImports(Set<String> imports) {
			globalImports.addAll(imports);
		}

		/**
		 * Adds variable to import from global scope.
		 * 
		 * @param importVariable
		 *            - name of variable to import.
		 */
		public void addGlobalImport(String importVariable) {
			globalImports.add(importVariable);
		}

		/**
		 * Gets whether this scope is global.
		 * 
		 * @return true if global, false otherwise.
		 */
		public boolean isGlobalScope() {
			return root instanceof Program;
		}

		/**
		 * Adds variable to the scope. If variable has a type specified and this
		 * or parent scopes do contain such a variable, current type would be
		 * added to the list of variable types.
		 * 
		 * @param variable
		 *            - variable info.
		 */
		public void addVariable(VariableInfo variable) {
			VariableInfo existingVariable = getVariable(variable.getName());
			if (existingVariable != null) {
				if (variable.getVariableTypes() != null
						&& !variable.getVariableTypes().isEmpty()) {
					if (this.equals(existingVariable.getScope())) {
						existingVariable.setVariableTypes(variable
								.getVariableTypes());
					} else {
						for (Object type : variable.getVariableTypes()) {
							existingVariable.addVariableType(type);
						}
					}
				}
			} else {
				variables.put(variable.getName(), variable);
			}
		}

		/**
		 * Gets variable by name from current scope or nearest of the parent
		 * scopes.
		 * 
		 * @param name
		 *            - variable name.
		 * @return variable info.
		 */
		public VariableInfo getVariable(String name) {
			return getVariable(name, new HashSet<String>());
		}

		/**
		 * Gets variable by name from current scope or nearest of the parent
		 * scopes.
		 * 
		 * @param name
		 *            - variable name.
		 * @param importsFromGlobal
		 *            - set of variables that might be imported from global
		 *            scope.
		 * @return variable info.
		 */
		public VariableInfo getVariable(String name,
				Set<String> importsFromGlobal) {
			if (globalImports != null && !globalImports.isEmpty()) {
				importsFromGlobal.addAll(globalImports);
			}

			VariableInfo info = variables.get(name);
			if (info != null) {
				return info;
			}

			if (parent != null) {
				if (parent.isGlobalScope()) {
					if (importsFromGlobal.contains(name)
							|| !(root instanceof FunctionDeclaration || root instanceof ClassDeclaration)) {
						return parent.getVariable(name, importsFromGlobal);
					}
				} else {
					return parent.getVariable(name, importsFromGlobal);
				}
				/*
				 * if (!parent.isGlobalScope() ||
				 * importsFromGlobal.contains(name)) { return
				 * parent.getVariable(name, importsFromGlobal); }
				 */
			}

			return null;
		}

		/**
		 * Gets root element entry.
		 * 
		 * @return rooot element entry.
		 */
		public IElementEntry getEntry() {
			return entry;
		}

		/**
		 * Gets scope root.
		 * 
		 * @return scope root node.
		 */
		public ASTNode getRoot() {
			return root;
		}

		/**
		 * Gets scope parent scope or null if parent not exist.
		 * 
		 * @return scope parent.
		 */
		public Scope getParent() {
			return parent;
		}

		/**
		 * Gets unmodifiable variables set including variables from the upper
		 * scope being aware of global statement.
		 * 
		 * @return unmodifiable variables set.
		 */
		public Set<VariableInfo> getVariables() {
			return getVariables(new HashSet<String>());
		}

		/**
		 * Gets variables or nearest of the parent scopes.
		 * 
		 * @param importsFromGlobal
		 *            - set of variables that might be imported from global
		 *            scope.
		 * @return variables.
		 */
		private Set<VariableInfo> getVariables(Set<String> importsFromGlobal) {
			if (globalImports != null && !globalImports.isEmpty()) {
				importsFromGlobal.addAll(globalImports);
			}

			Set<VariableInfo> result = new HashSet<VariableInfo>();

			for (VariableInfo variable : variables.values()) {
				result.add(variable);
			}

			if (parent != null) {
				if (!parent.isGlobalScope()) {
					result.addAll(parent.getVariables(importsFromGlobal));
				} else {
					if (automaticallyDeriveGlobalVariables()) {
						result.addAll(parent
								.getVariables(new HashSet<String>()));
					} else {
						for (String var : importsFromGlobal) {
							VariableInfo varInfo = parent.getVariable(var,
									importsFromGlobal);
							if (varInfo != null) {
								result.add(varInfo);
							}
						}
					}
				}
			}

			return result;
		}

		/**
		 * Checks whether this node must automatically derive variables from the
		 * global scope.
		 * 
		 * @return true if this node must automatically derive variables from
		 *         the global scope, false otherwise.
		 */
		private boolean automaticallyDeriveGlobalVariables() {
			if (!parent.isGlobalScope()) {
				return false;
			}

			if (this.getRoot() instanceof TypeDeclaration
					|| this.getRoot() instanceof FunctionDeclaration
					|| this.getRoot() instanceof MethodDeclaration||root instanceof LambdaFunctionDeclaration) {
				return false;
			}

			return true;
		}
	}

	/**
	 * Class scope information.
	 * 
	 * @author Denis Denisenko
	 */
	private static class ClassScopeInfo {
		/**
		 * Class entry.
		 */
		private IElementEntry classEntry;

		/**
		 * Class fields.
		 */
		private Map<String, IElementEntry> fields = new HashMap<String, IElementEntry>();

		/**
		 * ClassScopeInfo constructor.
		 * 
		 * @param classEntry
		 *            - class entry.
		 */
		public ClassScopeInfo(IElementEntry classEntry) {
			this.classEntry = classEntry;
		}

		/**
		 * Gets class entry.
		 * 
		 * @return class entry.
		 */
		public IElementEntry getClassEntry() {
			return classEntry;
		}

		/**
		 * Gets class fields.
		 * 
		 * @return class fields.
		 */
		public Collection<IElementEntry> getFields() {
			return fields.values();
		}

		/**
		 * Checks whether class has a field with a name specified.
		 * 
		 * @param fieldName
		 *            - field name to check.
		 * @return true if has field, false otherwise.
		 */
		public boolean hasField(String fieldName) {
			return fields.containsKey(fieldName);
		}

		/**
		 * Gets field by name.
		 * 
		 * @param fieldName
		 *            - field name to get.
		 * @return field entry or null.
		 */
		public IElementEntry getField(String fieldName) {
			return fields.get(fieldName);
		}

		/**
		 * Adds new field.
		 * 
		 * @param fieldName
		 *            - field name.
		 * @param field
		 *            - field entry.
		 */
		public void setField(String fieldName, IElementEntry field) {
			fields.put(fieldName, field);
		}

		/**
		 * Adds field types.
		 * 
		 * @param fieldName
		 *            - field name.
		 * @param types
		 *            - types to add.
		 * @return true if types are added, false otherwise.
		 */
		public boolean addFieldTypes(String fieldName, Set<Object> types) {
			IElementEntry fieldEntry = fields.get(fieldName);
			if (fieldEntry == null) {
				return false;
			}

			Object entryValue = fieldEntry.getValue();
			if (!(entryValue instanceof VariablePHPEntryValue)) {
				return false;
			}

			VariablePHPEntryValue value = (VariablePHPEntryValue) entryValue;
			for (Object type : types) {
				value.addType(type);
			}

			return true;
		}
	}

	/**
	 * AST visitor.
	 * 
	 * @author Denis Denisenko
	 */
	private class PHPASTVisitor extends PHPASTVisitorStub {
		/**
		 * Reporter.
		 */
		private IIndexReporter reporter;

		/**
		 * Module.
		 */
		private IModule module;

		private String currentNamespace = "";

		HashMap<String, String> aliases = new HashMap<String, String>();

		/**
		 * Current class.
		 */
		private ClassScopeInfo currentClass;

		/**
		 * Current function.
		 */
		private IElementEntry currentFunction;

		/**
		 * Variable scopes.
		 */
		private Stack<Scope> scopes = new Stack<Scope>();

		// /**
		// * Backuped variable scopes.
		// */
		// private Stack<Scope> backupedScopes = new Stack<Scope>();
		//		
		// /**
		// * Scopes of the previous node.
		// */
		// private Stack<Scope> previousNodeScopes = new Stack<Scope>();

		/**
		 * Whether the local stack was reported (required for local mode).
		 */
		boolean localStackReported = false;

		/**
		 * PHPASTVisitor constructor.
		 * 
		 * @param reporter
		 *            - reporter to use.
		 * @param module
		 *            - current module.
		 */
		public PHPASTVisitor(IIndexReporter reporter, IModule module) {
			this.reporter = reporter;
			this.module = module;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean startVisitNode(ASTNode node) {
			// if we start visiting node that is after the parser offset
			// and we did not report the stack yet and local mode is on
			// we should report the whole stack
			// if (!globalMode && !localStackReported
			// && node.getStart() > currentOffset)
			// {
			// while (previousNodeScopes.size() != 0)
			// {
			// Scope lastScope =
			// previousNodeScopes.get(previousNodeScopes.size() - 1);
			// ASTNode lastScopeRoot = lastScope.getRoot();
			// //if we found the scope that end is further then current pos,
			// reporting it
			// if (lastScopeRoot.getEnd() > currentOffset)
			// {
			// reportStack(previousNodeScopes);
			// localStackReported = true;
			// break;
			// }
			// else
			// {
			// //skipping this scope.
			// previousNodeScopes.pop();
			// }
			// }
			// }
			return true;
		}

		@Override
		public boolean endVisit(NamespaceDeclaration node) {
			currentNamespace = "";
			return super.endVisit(node);
		}

		@Override
		public boolean visit(NamespaceDeclaration node) {
			StringBuilder nameBuilder = new StringBuilder();
			List<Identifier> segments = node.getName().segments();
			int a = 0;
			for (Identifier i : segments) {
				nameBuilder.append(i.getName());
				a++;
				if (a != segments.size()) {
					nameBuilder.append('\\');
				}
			}
			String name = nameBuilder.toString();
			reporter.reportEntry(IPHPIndexConstants.NAMESPACE_CATEGORY, name,
					new NamespacePHPEntryValue(0, name), module);
			currentNamespace = name;
			_namespace = currentNamespace;
			return super.visit(node);
		}

		@Override
		public boolean visit(UseStatement node) {
			// TODO Auto-generated method stub
			return super.visit(node);
		}

		@Override
		public boolean visit(UseStatementPart node) {
			NamespaceName name = node.getName();
			String fullName = name.getFullName();
			Identifier alias = node.getAlias();
			if (alias != null) {
				String aliasName = alias.getName();
				// this is alias for class name
				aliases.put(aliasName, fullName);
				getCurrentScope().aliases.put(aliasName, fullName);
			} else {
				int lastIndexOf = fullName.lastIndexOf('\\');
				String aliasName = fullName.substring(lastIndexOf + 1);
				aliases.put(aliasName, fullName);
				getCurrentScope().aliases.put(aliasName, fullName);
			}
			return super.visit(node);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ClassDeclaration classDeclaration) {
			int category = IPHPIndexConstants.CLASS_CATEGORY;

			Identifier[] interfaces = classDeclaration.getInterfaces();
			List<String> interfaceNames = new ArrayList<String>(
					interfaces.length);
			for (int i = 0; i < interfaces.length; i++) {
				interfaceNames.add(interfaces[i].getName());
			}

			Identifier superClassIdentifier = classDeclaration.getSuperClass();
			String superClassName = null;
			if (superClassIdentifier != null) {
				superClassName = superClassIdentifier.getName();
			}

			ClassPHPEntryValue value = new ClassPHPEntryValue(classDeclaration
					.getModifier(), superClassName, interfaceNames,
					currentNamespace);

			value.setStartOffset(classDeclaration.getStart());
			value.setEndOffset(classDeclaration.getEnd());

			IElementEntry currentClassEntry = reporter.reportEntry(category,
					classDeclaration.getName().getName(), value, module);
			currentClass = new ClassScopeInfo(currentClassEntry);

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(InterfaceDeclaration interfaceDeclaration) {
			int category = IPHPIndexConstants.CLASS_CATEGORY;

			Identifier[] interfaces = interfaceDeclaration.getInterfaces();
			List<String> interfaceNames = new ArrayList<String>(
					interfaces.length);
			for (int i = 0; i < interfaces.length; i++) {
				interfaceNames.add(interfaces[i].getName());
			}

			ClassPHPEntryValue value = new ClassPHPEntryValue(
					PHPModifier.INTERFACE, null, interfaceNames,
					currentNamespace);

			value.setStartOffset(interfaceDeclaration.getStart());
			value.setEndOffset(interfaceDeclaration.getEnd());

			IElementEntry currentClassEntry = reporter.reportEntry(category,
					interfaceDeclaration.getName().getName(), value, module);

			currentClass = new ClassScopeInfo(currentClassEntry);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(FunctionInvocation functionInvocation) {
			FunctionName funcName = functionInvocation.getFunctionName();
			if (funcName == null) {
				return true;
			}

			Expression functionName = funcName.getFunctionName();
			if (functionName instanceof Identifier) {
				if (!DEFINE.equals(((Identifier) functionName).getName())) {
					return true;
				}
			}
			if (functionName instanceof Variable) {
				Variable vr = (Variable) functionName;
				Expression name = vr.getVariableName();
				if (name instanceof Identifier) {
					if (!DEFINE.equals(((Identifier) name).getName())) {
						return true;
					}
				}
			}

			Expression[] parameters = functionInvocation.getParameters();
			if (parameters.length < 2) {
				return true;
			}

			if (!(parameters[0] instanceof Scalar)) {
				return true;
			}

			if (Scalar.TYPE_STRING != ((Scalar) parameters[0]).getScalarType()) {
				return true;
			}

			// getting define name
			String defineName = ((Scalar) parameters[0]).getStringValue();
			if (defineName.startsWith("\"")) //$NON-NLS-1$
			{
				defineName = defineName.substring(1);
			}

			if (defineName.endsWith("\"")) //$NON-NLS-1$
			{
				defineName = defineName.substring(0, defineName.length() - 1);
			}
			if (defineName.startsWith("\'")) //$NON-NLS-1$;
			{
				defineName = defineName.substring(1);
			}

			if (defineName.endsWith("\'")) //$NON-NLS-1$
			{
				defineName = defineName.substring(0, defineName.length() - 1);
			}
			Set<Object> defineTypes = countExpressionTypes(parameters[1]);
			if (defineTypes == null) {
				return true;
			}

			VariableInfo info = new VariableInfo(defineName, defineTypes,
					getGlobalScope(), functionInvocation.getStart(),
					PHPModifier.NAMED_CONSTANT);
			getGlobalScope().addVariable(info);
			VariablePHPEntryValue entryValue = new VariablePHPEntryValue(0,
					false, false, true, defineTypes, functionInvocation
							.getStart(), currentNamespace);
			reporter.reportEntry(IPHPIndexConstants.CONST_CATEGORY, defineName,
					entryValue, module);
			return true;
		}

		@Override
		public boolean visit(FunctionDeclaration functionDeclaration) {
			// methods are handled in other type.
			if (functionDeclaration.getParent() != null
					&& functionDeclaration.getParent() instanceof MethodDeclaration) {
				return true;
			}

			String comment = findFunctionPHPDocComment(functionDeclaration
					.getStart());

			// getting function name
			Identifier functionNameIdentifier = functionDeclaration
					.getFunctionName();
			if (functionNameIdentifier == null) {
				return true;
			}

			String functionName = functionNameIdentifier.getName();

			// getting function parameters
			FormalParameter[] parameters = functionDeclaration
					.getFormalParameters();

			LinkedHashMap<String, Set<Object>> parametersMap = null;
			int[] parameterPositions = parameters == null
					|| parameters.length == 0 ? null
					: new int[parameters.length];

			if (parameters.length > 0) {
				parametersMap = new LinkedHashMap<String, Set<Object>>(
						parameters.length);
			}
			ArrayList<Boolean> mandatoryParams = new ArrayList<Boolean>();
			if (parameters != null) {
				int parCount = 0;
				for (FormalParameter parameter : parameters) {
					Identifier nameIdentifier = parameter
							.getParameterNameIdentifier();
					if (nameIdentifier == null) {
						continue;
					}
					String parameterName = nameIdentifier.getName();
					parameterPositions[parCount] = parameter.getStart();
					String parameterType = null;
					Expression parameterTypeIdentifier = parameter
							.getParameterType();
					if (parameterTypeIdentifier != null) {
						parameterType = parameterTypeIdentifier.getName();
					}

					Set<Object> types = null;
					if (parameterType != null) {
						types = new HashSet<Object>(1);
						types.add(parameterType);
					}

					if (parameter.getDefaultValue() != null) {
						Expression rightPartExpr = parameter.getDefaultValue();
						Set<Object> expressionTypes = countExpressionTypes(rightPartExpr);
						if (expressionTypes != null
								&& expressionTypes.size() != 0) {
							if (types == null) {
								types = new HashSet<Object>();
								types.addAll(expressionTypes);
							}
						}
					}

					parametersMap.put(parameterName, types);
					mandatoryParams.add(parameter.getDefaultValue() == null
							|| parameter.isMandatory());
					parCount++;
				}
			}

			// parsing PHP doc and adding types from it to the parameters
			String[] returnTypes = null;
			if (comment != null) {
				returnTypes = applyComment(comment, parametersMap);
			}
			boolean[] mandatories = new boolean[mandatoryParams.size()];
			for (int j = 0; j < mandatoryParams.size(); j++) {
				mandatories[j] = mandatoryParams.get(j);
			}
			FunctionPHPEntryValue entryValue = new FunctionPHPEntryValue(0,
					false, parametersMap, parameterPositions, mandatories,
					functionDeclaration.getStart(), currentNamespace);
			if (returnTypes != null) {
				HashSet<Object> returnTypesSet = new HashSet<Object>();
				for (String returnType : returnTypes) {
					returnTypesSet.add(returnType);
				}
				entryValue.setReturnTypes(returnTypesSet);
			}
			String entryPath = ""; //$NON-NLS-1$
			if (currentClass != null) {
				entryPath = currentClass.getClassEntry().getEntryPath()
						+ IElementsIndex.DELIMITER;
			}

			entryPath += functionName;

			currentFunction = reporter.reportEntry(
					IPHPIndexConstants.FUNCTION_CATEGORY, entryPath,
					entryValue, module);

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(MethodDeclaration methodDeclaration) {
			String comment = findFunctionPHPDocComment(methodDeclaration
					.getStart());

			FunctionDeclaration functionDeclaration = methodDeclaration
					.getFunction();
			if (functionDeclaration == null) {
				return true;
			}

			// getting function name
			Identifier functionNameIdentifier = functionDeclaration
					.getFunctionName();
			if (functionNameIdentifier == null) {
				return true;
			}

			String functionName = functionNameIdentifier.getName();

			// getting function parameters
			FormalParameter[] parameters = functionDeclaration
					.getFormalParameters();
			int[] parameterPositions = parameters == null
					|| parameters.length == 0 ? null
					: new int[parameters.length];

			LinkedHashMap<String, Set<Object>> parametersMap = null;
			if (parameters.length > 0) {
				parametersMap = new LinkedHashMap<String, Set<Object>>(
						parameters.length);
			}
			ArrayList<Boolean> mandatoryParams = new ArrayList<Boolean>();
			if (parameters != null) {
				int parCount = 0;
				for (FormalParameter parameter : parameters) {
					Identifier nameIdentifier = parameter
							.getParameterNameIdentifier();
					if (nameIdentifier == null) {
						continue;
					}
					String parameterName = nameIdentifier.getName();
					parameterPositions[parCount] = parameter.getStart();

					String parameterType = null;
					Expression parameterTypeIdentifier = parameter
							.getParameterType();
					if (parameterTypeIdentifier != null) {
						parameterType = parameterTypeIdentifier.getName();
					}

					Set<Object> types = null;
					if (parameterType != null) {
						types = new HashSet<Object>(1);
						types.add(parameterType);
					}

					parametersMap.put(parameterName, types);
					mandatoryParams.add(parameter.getDefaultValue() == null
							|| parameter.isMandatory());
					parCount++;
				}
			}

			// parsing PHP doc and adding types from it to the parameters
			String[] returnTypes = null;
			if (comment != null) {
				returnTypes = applyComment(comment, parametersMap);
			}

			int modifier = methodDeclaration.getModifier();
			// if access modifier is unspecified, making it public.
			if (!PHPModifier.isPublic(modifier)
					&& !PHPModifier.isProtected(modifier)
					&& !PHPModifier.isPrivate(modifier)) {
				modifier |= PHPModifier.PUBLIC;
			}

			boolean[] mandatories = new boolean[mandatoryParams.size()];
			for (int j = 0; j < mandatoryParams.size(); j++) {
				mandatories[j] = mandatoryParams.get(j);
			}
			FunctionPHPEntryValue entryValue = new FunctionPHPEntryValue(
					modifier, true, parametersMap, parameterPositions,
					mandatories, methodDeclaration.getStart(), currentNamespace);

			if (returnTypes != null) {
				HashSet<Object> returnTypesSet = new HashSet<Object>();
				for (String returnType : returnTypes) {
					returnTypesSet.add(returnType);
				}
				entryValue.setReturnTypes(returnTypesSet);
			}

			String entryPath = ""; //$NON-NLS-1$
			if (currentClass != null) {
				entryPath = currentClass.getClassEntry().getEntryPath()
						+ IElementsIndex.DELIMITER;
			}

			entryPath += functionName;

			currentFunction = reporter.reportEntry(
					IPHPIndexConstants.FUNCTION_CATEGORY, entryPath,
					entryValue, module);

			// backuping old scopes
			// backupedScopes = scopes;
			// scopes = new Stack<Scope>();

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Variable variable) {
			ASTNode parent = variable.getParent();

			// handling variables assigned to some expression
			if (parent instanceof Assignment) {
				Assignment assignment = (Assignment) parent;
				if (variable.equals(assignment.getVariable())) {
					boolean staticDeclaration = parent.getParent() instanceof StaticStatement;

					return handleAssigment(variable, assignment,
							staticDeclaration);
				}
			}
			// handling simple variable definitions
			else if (parent instanceof ExpressionStatement) {
				String variableName = getVariableName(variable);
				if (variableName == null) {
					return true;
				}
				VariableInfo variableInfo = new VariableInfo(variableName,
						null, getCurrentScope(), variable.getStart());
				getCurrentScope().addVariable(variableInfo);
			}
			// handling infix expressions, variable might take part in
			else if (parent instanceof InfixExpression) {
				String variableName = getVariableName(variable);
				if (variableName == null) {
					return true;
				}
				InfixExpression expr = (InfixExpression) parent;
				Set<Object> types = countInfixExpressionTypes(expr);
				VariableInfo variableInfo = new VariableInfo(variableName,
						types, getCurrentScope(), variable.getStart());
				getCurrentScope().addVariable(variableInfo);
			}

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(StaticFieldAccess fieldAccess) {
			if (currentClass == null) {
				return true;
			}

			if (fieldAccess.getParent() == null
					|| !(fieldAccess.getParent() instanceof Assignment)) {
				return true;
			}

			String className = fieldAccess.getClassName().getName();
			if (className == null || !SELF.equals(className)) {
				return true;
			}

			String fieldName = getVariableName(fieldAccess.getField());
			if (fieldName == null) {
				return true;
			}

			Expression value = ((Assignment) fieldAccess.getParent())
					.getValue();
			Set<Object> valueTypes = countExpressionTypes(value);
			if (valueTypes != null && valueTypes.size() > 0) {
				if (currentClass.hasField(fieldName)) {
					currentClass.addFieldTypes(fieldName, valueTypes);
				} else {
					reportField(PHPModifier.PUBLIC | PHPModifier.STATIC,
							fieldName, valueTypes, fieldAccess.getStart());
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ClassConstantDeclaration constantDeclaration) {
			if (currentClass == null) {
				return true;
			}
			Identifier[] variableNames = constantDeclaration.getVariableNames();
			Expression[] values = constantDeclaration.getConstantValues();

			for (int i = 0; i < variableNames.length; i++) {
				String variableName = variableNames[i].getName();

				Expression value = values[i];
				Set<Object> valueTypes = countExpressionTypes(value);
				if (valueTypes != null && valueTypes.size() > 0) {
					// if (currentClass.hasField(variableName))
					// {
					// currentClass.addFieldTypes(variableName, valueTypes);
					// }
					// else
					// {
					reportClassConst(PHPModifier.PUBLIC, variableName,
							valueTypes, constantDeclaration.getStart());
					// }
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(FieldAccess fieldAccess) {
			if (fieldAccess == null) {
				return false;
			}

			if (currentClass == null) {
				return true;
			}

			if (fieldAccess.getParent() == null
					|| !(fieldAccess.getParent() instanceof Assignment)) {
				return true;
			}

			if (fieldAccess != ((Assignment) fieldAccess.getParent())
					.getVariable()) {
				return true;
			}

			VariableBase leftSide = fieldAccess.getDispatcher();
			if (!(leftSide instanceof Variable)) {
				return true;
			}

			String variableName = getVariableName((Variable) leftSide);
			if (variableName == null || !THIS.equals(variableName)) {
				return true;
			}

			VariableBase rightSide = fieldAccess.getMember();
			if (!(rightSide instanceof Variable)) {
				return true;
			}

			String fieldName = getVariableName((Variable) rightSide);
			if (fieldName == null) {
				return true;
			}

			Expression value = ((Assignment) fieldAccess.getParent())
					.getValue();
			Set<Object> valueTypes = countExpressionTypes(value);
			if (valueTypes != null && valueTypes.size() > 0) {
				if (currentClass.hasField(fieldName)) {
					currentClass.addFieldTypes(fieldName, valueTypes);
				} else {
					reportField(PHPModifier.PUBLIC, fieldName, valueTypes,
							fieldAccess.getStart());
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(FieldsDeclaration fieldsDeclaration) {
			Variable[] variables = fieldsDeclaration.getVariableNames();
			Expression[] initialValues = fieldsDeclaration.getInitialValues();
			int modifier = fieldsDeclaration.getModifier();

			// if access modifier is unspecified, making it public.
			if (!PHPModifier.isPublic(modifier)
					&& !PHPModifier.isProtected(modifier)
					&& !PHPModifier.isPrivate(modifier)) {
				modifier |= PHPModifier.PUBLIC;
			}

			for (int i = 0; i < variables.length; i++) {
				Variable fieldVariable = variables[i];
				Expression initialValue = initialValues[i];

				String fieldName = getVariableName(fieldVariable);
				if (fieldName == null) {
					continue;
				}

				Set<Object> fieldTypes = null;
				if (initialValue != null) {
					fieldTypes = countExpressionTypes(initialValue);
				}

				String comment = findFunctionPHPDocComment(fieldsDeclaration
						.getStart());
				if (comment != null) {
					/* TODO: Shalom - Hook the PHPDoc parser
					FunctionDocumentation documentation = PHPDocUtils
							.parseFunctionPHPDoc(comment);
					if (documentation != null) {
						ArrayList<TypedDescription> vars = documentation
								.getVars();
						if (vars != null && vars.size() != 0) {
							if (fieldTypes == null) {
								fieldTypes = new HashSet<Object>();
							}
							for (TypedDescription descr : vars) {
								String[] types = descr.getTypes();
								for (String type : types) {
									fieldTypes.add(type);
								}
							}
						}
					}
					*/
				}

				reportField(modifier, fieldName, fieldTypes, fieldVariable
						.getStart());
			}

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(GlobalStatement globalStatement) {
			Variable[] variables = globalStatement.getVariables();
			Set<String> imports = new HashSet<String>();
			for (Variable variable : variables) {
				String varName = getVariableName(variable);
				if (varName != null) {
					imports.add(varName);

					getGlobalScope().addVariable(
							new VariableInfo(varName, null, getGlobalScope(),
									globalStatement.getStart()));
				}
			}

			getCurrentScope().addGlobalImports(imports);

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ReturnStatement returnStatement) {
			// ignoring out of function returns
			if (currentFunction == null) {
				return true;
			}

			Expression expression = returnStatement.getExpr();
			Set<Object> types = countExpressionTypes(expression);

			if (!(currentFunction.getValue() instanceof FunctionPHPEntryValue)) {
				return true;
			}

			FunctionPHPEntryValue value = (FunctionPHPEntryValue) currentFunction
					.getValue();
			if (types != null) {
				Set<Object> currentTypes = value.getReturnTypes();
				if (currentTypes == null || currentTypes.size() == 0) {
					currentTypes = new HashSet<Object>();
				}
				currentTypes.addAll(types);
				value.setReturnTypes(currentTypes);
			}

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(ClassDeclaration classDeclaration) {
			currentClass = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(FunctionDeclaration functionDeclaration) {
			currentFunction = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(MethodDeclaration methodDeclaration) {
			// restoring backuped scopes
			// scopes = backupedScopes;

			currentFunction = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(Program program) {
			endVisitScopeNode(program);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Program program) {
			startVisitScopeNode(program);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Block block) {
			if (block.getParent() instanceof NamespaceDeclaration) {
				return true;
			}
			startVisitScopeNode(block.getParent());
			addBlockVariables(block);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(CatchClause catchClause) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(DoStatement doStatement) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ForEachStatement forEachStatement) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ForStatement forStatement) {
			startVisitScopeNode(forStatement);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(IfStatement ifStatement) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(SwitchStatement switchStatement) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(TryStatement tryStatement) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(WhileStatement whileStatement) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Include include) {
			Expression expr = include.getExpr();
			if (expr != null
					&& (expr instanceof ParenthesisExpression || expr instanceof Scalar)) {
				Expression subExpr = null;
				if (expr instanceof Scalar) {
					subExpr = expr;
				} else {
					subExpr = ((ParenthesisExpression) expr).getExpr();
				}
				if (subExpr == null) {
					return true;
				}
				if (subExpr instanceof Scalar) {
					String includePath = ((Scalar) subExpr).getStringValue();
					if (includePath == null || includePath.length() == 0) {
						return true;
					}

					int pathStartOffset = subExpr.getStart();

					if (includePath.startsWith("\"") || includePath.startsWith("'")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						includePath = includePath.substring(1);
						pathStartOffset++;
					}

					if (includePath.endsWith("\"") || includePath.endsWith("'")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						includePath = includePath.substring(0, includePath
								.length() - 1);
					}

					int pdtIncludeType = include.getIncludeType();
					int includeType = -1;
					switch (pdtIncludeType) {
					case Include.IT_INCLUDE:
						includeType = IncludePHPEntryValue.INCLUDE_TYPE;
						break;
					case Include.IT_INCLUDE_ONCE:
						includeType = IncludePHPEntryValue.INCLUDE_ONCE_TYPE;
						break;
					case Include.IT_REQUIRE:
						includeType = IncludePHPEntryValue.REQUIRE_TYPE;
						break;
					case Include.IT_REQUIRE_ONCE:
						includeType = IncludePHPEntryValue.REQUIRE_ONCE_TYPE;
						break;
					}

					IncludePHPEntryValue value = new IncludePHPEntryValue(
							includePath, include.getStart(), include.getEnd(),
							pathStartOffset, includeType);
					reporter.reportEntry(IPHPIndexConstants.IMPORT_CATEGORY,
							"", value, module); //$NON-NLS-1$
				}
			}

			return true;
		}

		// ///
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(Block block) {
			if (block.getParent() instanceof NamespaceDeclaration) {
				return;
			}
			endVisitScopeNode(block);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(CatchClause catchClause) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(DoStatement doStatement) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(ForEachStatement forEachStatement) {

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(ForStatement forStatement) {
			endVisitScopeNode(forStatement);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(IfStatement ifStatement) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(SwitchStatement switchStatement) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(TryStatement tryStatement) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(WhileStatement whileStatement) {
		}

		/**
		 * Handles variable assignment.
		 * 
		 * @param variable
		 *            - variable.
		 * @param assigment
		 *            - assignment node.
		 * @return
		 */
		private boolean handleAssigment(Variable variable,
				Assignment assigment, boolean staticDeclaration) {
			Expression value = assigment.getValue();

			String variableName = getVariableName(variable);
			if (variableName == null) {
				return true;
			}

			Set<Object> rightSideTypes = countExpressionTypes(value);

			VariableInfo variableInfo = new VariableInfo(variableName,
					rightSideTypes, getCurrentScope(), variable.getStart(),
					staticDeclaration ? PHPModifier.STATIC : 0);
			getCurrentScope().addVariable(variableInfo);

			return true;
		}

		/**
		 * Gets variable entry path.
		 * 
		 * @param variable
		 *            - variable.
		 * @return entry path.
		 */
		String getVariableEntryPath(Variable variable) {
			String entryPath = ""; //$NON-NLS-1$
			if (currentFunction != null) {
				entryPath = currentFunction.getEntryPath()
						+ IElementsIndex.DELIMITER;
			} else if (currentClass != null) {
				entryPath = currentClass.getClassEntry().getEntryPath()
						+ IElementsIndex.DELIMITER;
			}

			String varName = getVariableName(variable);
			if (varName == null) {
				return null;
			}

			entryPath += varName;

			return entryPath;
		}

		/**
		 * Gets variable name.
		 * 
		 * @param variable
		 *            - variable.
		 * @return variable name.
		 */
		private String getVariableName(Expression variable) {
			ASTNode variableNameExpr = variable.getVariableName();
			if (variableNameExpr instanceof Identifier) {
				return ((Identifier) variableNameExpr).getName();
			} else {
				return null;
			}
		}

		/**
		 * Handles the start of visiting the node that provides variables scope.
		 * 
		 * @param node
		 *            - node.
		 */
		private void startVisitScopeNode(ASTNode node) {
			Scope parent = null;
			if (!scopes.isEmpty()) {
				parent = scopes.peek();
			}

			IElementEntry currentEntry = null;
			if (node instanceof FunctionDeclaration) {
				currentEntry = currentFunction;
			} else if (node instanceof ClassDeclaration) {
				currentEntry = currentClass.getClassEntry();
			}

			Scope scope = new Scope(node, parent, currentEntry);
			
			scopes.push(scope);
		}

		/**
		 * Handles the end of visiting the node that provides variables scope.
		 * 
		 * @param node
		 *            - node.
		 */
		private void endVisitScopeNode(ASTNode node) {
			if (!scopes.isEmpty()) {

				// if local mode is on and we did not report the local stack
				// yet,
				// backuping the stack.
				if (!globalMode && !localStackReported) {
					if (node.getEnd() > currentOffset) {
						reportStack(scopes);
						localStackReported = true;
					}
				}

				Scope scope = scopes.pop();
				reportGlobalScopeVariables(scope);
			}
		}

		/**
		 * Gets current scope. Should never be null.
		 * 
		 * @return current scope.
		 */
		private Scope getCurrentScope() {
			return scopes.peek();
		}

		/**
		 * Gets current scope. Should never be null.
		 * 
		 * @return current scope.
		 */
		private Scope getGlobalScope() {
			return scopes.get(0);
		}

		/**
		 * Reports global scope variables.
		 * 
		 * @param scope
		 *            - scope, which variables to report.
		 */
		private void reportGlobalScopeVariables(Scope scope) {
			if (globalMode) {
				if (scope.getRoot() instanceof Program) {
					for (VariableInfo info : scope.getVariables()) {
						VariablePHPEntryValue entryValue = new VariablePHPEntryValue(
								0, false, false, false,
								info.getVariableTypes(), info.getNodeStart(),
								currentNamespace);

						String entryPath = info.getName();
						int category = PHPModifier.isNamedConstant(info
								.getModifier()) ? IPHPIndexConstants.CONST_CATEGORY
								: IPHPIndexConstants.VAR_CATEGORY;
						reporter.reportEntry(category, entryPath, entryValue,
								module);
					}
				}
			}
		}

		/**
		 * Count variables types by name and scope.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param currentScope
		 *            - current scope.
		 * @return
		 */
		private Set<Object> countVariableTypes(String variableName,
				Scope currentScope) {
			Set<Object> result = new HashSet<Object>();

			// checking for variable with this name.
			VariableInfo variable = currentScope.getVariable(variableName);
			if (variable != null) {
				result.addAll(variable.getVariableTypes());
			}

			// checking for function or method parameter with this name
			IElementEntry entry = findFunctionOrMethodParent(currentScope);
			if (entry != null) {
				FunctionPHPEntryValue entryValue = (FunctionPHPEntryValue) entry
						.getValue();
				Map<String, Set<Object>> parameters = entryValue
						.getParameters();
				if (parameters != null) {
					Set<Object> types = parameters.get(variableName);
					if (types != null) {
						result.addAll(types);
					}
				}
			}

			return result;
		}

		/**
		 * Finds function or method parent scope and returns its element entry.
		 * 
		 * @param scope
		 *            - current scope.
		 * @return entry or null if not found.
		 */
		private IElementEntry findFunctionOrMethodParent(Scope scope) {
			Scope currentScope = scope;
			while (currentScope != null) {
				IElementEntry entry = currentScope.getEntry();
				if (entry != null) {
					if (entry.getValue() instanceof FunctionPHPEntryValue) {
						return entry;
					}
				}

				currentScope = currentScope.getParent();
			}

			return null;
		}

		/**
		 * Counts infix expression types.
		 * 
		 * @param expr
		 *            - expression.
		 * @return expression type or null if undefined
		 */
		private Set<Object> countInfixExpressionTypes(InfixExpression expr) {
			int operator = expr.getOperator();
			String type = null;
			switch (operator) {
			case InfixExpression.OP_CONCAT:
				type = IPHPIndexConstants.STRING_TYPE;
				break;
			case InfixExpression.OP_IS_IDENTICAL:
			case InfixExpression.OP_IS_NOT_IDENTICAL:
			case InfixExpression.OP_IS_EQUAL:
			case InfixExpression.OP_IS_NOT_EQUAL:
			case InfixExpression.OP_RGREATER:
			case InfixExpression.OP_IS_SMALLER_OR_EQUAL:
			case InfixExpression.OP_LGREATER:
			case InfixExpression.OP_IS_GREATER_OR_EQUAL:
			case InfixExpression.OP_BOOL_OR:
				type = IPHPIndexConstants.BOOLEAN_TYPE;
				break;
			case InfixExpression.OP_STRING_OR:
			case InfixExpression.OP_STRING_AND:
			case InfixExpression.OP_STRING_XOR:
				type = IPHPIndexConstants.STRING_TYPE;
				break;
			case InfixExpression.OP_PLUS:
			case InfixExpression.OP_MINUS:
			case InfixExpression.OP_MUL:
			case InfixExpression.OP_DIV:
			case InfixExpression.OP_MOD:
				type = IPHPIndexConstants.REAL_TYPE;
				break;
			case InfixExpression.OP_SL:
			case InfixExpression.OP_SR:
				type = IPHPIndexConstants.INTEGER_TYPE;
				break;
			}

			Set<Object> result = new HashSet<Object>(1);
			result.add(type);
			return result;
		}

		/**
		 * Converts static access to the plain path. In example ClassName::$b
		 * would be [ClassName,b] path.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return path or null indicating parsing failure
		 */
		private CallPath getPathByStaticDispatch(StaticDispatch dispatch) {
			CallPath result = new CallPath();

			// counting first path entry
			Expression classNameIdentifier = dispatch.getClassName();
			String className = classNameIdentifier.getName();
			result.setClassEntry(className);

			// counting second entry
			ASTNode member = dispatch.getMember();

			// getting member name and type
			String memberName = null;
			boolean field = true;

			if (member instanceof Variable) {
				Variable currentField = (Variable) member;
				memberName = getVariableName(currentField);
				if (memberName == null) {
					return null;
				}
				field = true;
			} else if (member instanceof FunctionInvocation) {
				FunctionInvocation funcInvocation = (FunctionInvocation) member;
				memberName = getFunctionNameByInvocation(funcInvocation);
				if (memberName == null) {
					return null;
				}
				field = false;
			} else {
				return null;
			}

			// inserting member to the path
			if (field) {
				result.addVariableEntry(memberName);
			} else {
				result.addMethodEntry(memberName);
			}

			return result;
		}

		/**
		 * Converts field access to the plain path. In example $a->$b->$method()
		 * would be [a,b,method] path.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return path or null indicating parsing failure
		 */
		private CallPath getPathByDispatch(Dispatch dispatch) {
			CallPath result = new CallPath();
			Dispatch currentDispatch = dispatch;

			while (currentDispatch != null) {
				VariableBase currentDispatcher = currentDispatch
						.getDispatcher();

				// getting member name and type
				String memberName = null;
				boolean field = true;
				VariableBase member = currentDispatch.getMember();
				if (member instanceof Variable) {
					Variable currentField = (Variable) member;
					memberName = getVariableName(currentField);
					if (memberName == null) {
						return null;
					}
					field = true;
				} else if (member instanceof FunctionInvocation) {
					FunctionInvocation funcInvocation = (FunctionInvocation) member;
					memberName = getFunctionNameByInvocation(funcInvocation);
					if (memberName == null) {
						return null;
					}
					field = false;
				} else {
					return null;
				}

				// inserting member to the path
				if (field) {
					result.insertVariableEntry(memberName);
				} else {
					result.insertMethodEntry(memberName);
				}

				// if left side is still the dispatch, continue to parse it
				if (currentDispatcher instanceof Dispatch) {
					currentDispatch = (Dispatch) currentDispatcher;
				} else if (currentDispatcher instanceof StaticDispatch) {
					// getting the path of the static dispatch
					CallPath staticDispatchCallPath = getPathByStaticDispatch((StaticDispatch) currentDispatcher);
					if (staticDispatchCallPath == null) {
						return null;
					}

					// adding current path to the static dispatch type
					staticDispatchCallPath.addPath(result);
					return staticDispatchCallPath;
				} else if (currentDispatcher instanceof Variable) {
					// if left side is variable, inserting it and stopping
					// parsing
					String dispatecherName = getVariableName((Variable) currentDispatcher);
					if (dispatecherName == null) {
						return null;
					}

					result.insertVariableEntry(dispatecherName);
					break;
				} else if (currentDispatcher instanceof FunctionInvocation) {
					// if left side is method, inserting it and stopping parsing
					String dispatecherName = getFunctionNameByInvocation((FunctionInvocation) currentDispatcher);
					if (dispatecherName == null) {
						return null;
					}

					result.insertMethodEntry(dispatecherName);
					break;
				} else {
					// unrecognized construction
					return null;
				}
			}

			return result;
		}

		/**
		 * Counts the all possible types of expression.
		 * 
		 * @param expression
		 *            - expression to count.
		 * @return types set or null if expression types connot be counted.
		 */
		private Set<Object> countExpressionTypes(Expression expression) {
			Set<Object> result = null;
			// handling scalar values
			if (expression instanceof Quote) {
				if (((Quote) expression).getQuoteType() == Quote.QT_HEREDOC) {
					result = new HashSet<Object>();
					result.add(IPHPIndexConstants.STRING_TYPE);
				}
			}
			if (expression instanceof LambdaFunctionDeclaration) {
				result = new HashSet<Object>(1);
				StringBuilder type = new StringBuilder();
				type.append(IPHPIndexConstants.LAMBDA_TYPE);
				type.append('(');
				LambdaFunctionDeclaration lf = (LambdaFunctionDeclaration) expression;
				List<FormalParameter> formalParameters = lf.formalParameters();
				int i = 0;
				for (FormalParameter p : formalParameters) {
					String name = p.getParameterName().getName();
					if (name.startsWith("$")) {
						type.append(name.substring(1));
						i++;
						if (i != formalParameters.size()) {
							type.append(',');
						}
					}
				}
				type.append(')');
				result.add(type.toString());
				return result;
			}
			if (expression instanceof Scalar) {
				Scalar scalar = (Scalar) expression;
				result = countScalarTypes(scalar);
			}
			// handling variable values
			else if (expression instanceof Variable) {
				Variable rightSideVar = (Variable) expression;
				String rightSideVarName = getVariableName(rightSideVar);
				if (rightSideVarName == null) {
					return null;
				}

				result = countVariableTypes(rightSideVarName, getCurrentScope());
			}
			// handling instance creation
			else if (expression instanceof ClassInstanceCreation) {
				ClassInstanceCreation creation = (ClassInstanceCreation) expression;
				result = countInstanceCreationTypes(creation);
			}
			// handling call paths
			else if (expression instanceof FieldAccess
					|| expression instanceof MethodInvocation) {
				Dispatch dispatch = (Dispatch) expression;
				result = countDispatchTypes(dispatch);
			} else if (expression instanceof StaticDispatch) {
				StaticDispatch staticDispatch = (StaticDispatch) expression;
				result = countStaticDispatchTypes(staticDispatch);
			}
			// handling function invocation
			else if (expression instanceof FunctionInvocation) {
				FunctionInvocation invocation = (FunctionInvocation) expression;
				result = countFunctionInvocationTypes(invocation);
			}
			// handling infix expressions
			else if (expression instanceof InfixExpression) {
				InfixExpression infix = (InfixExpression) expression;
				result = countInfixExpressionTypes(infix);
			}

			return result;
		}

		/**
		 * Counts function invocation types.
		 * 
		 * @param invocation
		 *            - invocation.
		 * @return set of types or null if cannot count.
		 */
		private Set<Object> countFunctionInvocationTypes(
				FunctionInvocation invocation) {
			String functionName = getFunctionNameByInvocation(invocation);
			if (functionName == null) {
				return null;
			}

			FunctionPathReference reference = new FunctionPathReference(
					functionName, null);

			Set<Object> result = new HashSet<Object>(1);
			result.add(reference);

			return result;
		}

		/**
		 * Counts scalar expression types.
		 * 
		 * @param scalar
		 *            - scalar.
		 * @return set of types or null if cannot count.
		 */
		private Set<Object> countScalarTypes(Scalar scalar) {
			Set<Object> result;
			int intType = scalar.getScalarType();
			String type = typeToString(intType);

			result = new HashSet<Object>(1);
			result.add(type);
			return result;
		}

		/**
		 * Counts class instance creation expression types.
		 * 
		 * @param creation
		 *            - instance creation.
		 * @return set of types or null if cannot count.
		 */
		private Set<Object> countInstanceCreationTypes(
				ClassInstanceCreation creation) {
			ClassName className = creation.getClassName();
			if (className == null) {
				return null;
			}
			Expression classNameExpr = className.getClassName();

			if (classNameExpr == null
					|| !((classNameExpr instanceof Identifier) || (classNameExpr instanceof NamespaceName))) {
				return null;
			}

			String clName = null;
			if (classNameExpr instanceof Identifier) {
				clName = ((Identifier) classNameExpr).getName();
				String string = aliases.get(clName);
				if (string != null) {
					clName = string;
				}
				if (currentNamespace != null && currentNamespace.length() > 0) {
					clName = currentNamespace + '\\' + clName;
				}
			} else if (classNameExpr instanceof NamespaceName) {
				NamespaceName na = (NamespaceName) classNameExpr;

				if (na.isGlobal()) {
					clName = na.getFullName();
				} else if (na.isCurrent()) {
					if (currentNamespace != null
							&& currentNamespace.length() > 0) {
						clName = currentNamespace + '\\' + na.getFullName();
					}
				} else {
					Identifier identifier = na.segments().get(0);
					String name = identifier.getName();
					String alias = aliases.get(name);
					if (alias != null) {
						clName = na.getFullName();
						clName = alias + clName.substring(name.length());
					} else {
						clName = currentNamespace + '\\' + na.getFullName();
					}
				}
			}
			if (clName == null) {
				return null;
			}

			Set<Object> result = new HashSet<Object>(1);

			if (SELF.equals(clName)) {
				if (currentClass != null) {
					result.add(currentClass.getClassEntry().getEntryPath());
					return result;
				}

				return null;
			}

			result.add(clName);

			return result;
		}

		/**
		 * Counts dispatch expression types.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return types set or null if cannot count.
		 */
		private Set<Object> countStaticDispatchTypes(StaticDispatch dispatch) {
			Set<Object> result = null;

			CallPath path = getPathByStaticDispatch(dispatch);
			if (path == null || path.getSize() < 2) {
				return null;
			}

			CallPath remainingPath = path.subPath(1);

			List<CallPath.Entry> pathEntries = path.getEntries();
			CallPath.Entry dispatchEntry = pathEntries.get(0);

			// handling "self::"
			if (dispatchEntry instanceof CallPath.ClassEntry
					&& SELF.equals(dispatchEntry.getName())) {
				if (currentClass != null) {
					Set<Object> dispatcherTypes = new HashSet<Object>(1);
					dispatcherTypes.add(ElementsIndexingUtils
							.getFirstNameInPath(currentClass.getClassEntry()
									.getEntryPath()));
					StaticPathReference reference = new StaticPathReference(
							dispatcherTypes, remainingPath);
					result = new HashSet<Object>(1);
					result.add(reference);
				} else {
					return null;
				}
			}
			// handling "ClassName::"
			else if (dispatchEntry instanceof CallPath.ClassEntry) {
				Set<Object> dispatcherTypes = new HashSet<Object>(1);
				dispatcherTypes.add(dispatchEntry.getName());
				StaticPathReference reference = new StaticPathReference(
						dispatcherTypes, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			} else {
				return null;
			}

			return result;
		}

		/**
		 * Counts dispatch expression types.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return types set or null if cannot count.
		 */
		private Set<Object> countDispatchTypes(Dispatch dispatch) {
			Set<Object> result = null;

			CallPath path = getPathByDispatch(dispatch);
			if (path == null || path.getSize() < 2) {
				return null;
			}

			CallPath remainingPath = path.subPath(1);

			List<CallPath.Entry> pathEntries = path.getEntries();
			CallPath.Entry dispatchEntry = pathEntries.get(0);

			// handling "this->"
			if (dispatchEntry instanceof CallPath.VariableEntry
					&& THIS.equals(dispatchEntry.getName())) {
				if (currentClass != null) {
					Set<Object> dispatcherTypes = new HashSet<Object>(1);
					dispatcherTypes.add(ElementsIndexingUtils
							.getFirstNameInPath(currentClass.getClassEntry()
									.getEntryPath()));
					VariablePathReference reference = new VariablePathReference(
							dispatcherTypes, remainingPath);
					result = new HashSet<Object>(1);
					result.add(reference);
				} else {
					return null;
				}
			}
			// handling "self::"
			else if (dispatchEntry instanceof CallPath.ClassEntry
					&& SELF.equals(dispatchEntry.getName())) {
				if (currentClass != null) {
					Set<Object> dispatcherTypes = new HashSet<Object>(1);
					dispatcherTypes.add(ElementsIndexingUtils
							.getFirstNameInPath(currentClass.getClassEntry()
									.getEntryPath()));
					StaticPathReference reference = new StaticPathReference(
							dispatcherTypes, remainingPath);
					result = new HashSet<Object>(1);
					result.add(reference);
				} else {
					return null;
				}
			}
			// handling "ClassName::"
			else if (dispatchEntry instanceof CallPath.ClassEntry) {
				Set<Object> dispatcherTypes = new HashSet<Object>(1);
				dispatcherTypes.add(dispatchEntry.getName());
				StaticPathReference reference = new StaticPathReference(
						dispatcherTypes, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			}
			// handling "$var->"
			else if (dispatchEntry instanceof CallPath.VariableEntry) {
				Set<Object> dispatcherTypes = countVariableTypes(dispatchEntry
						.getName(), getCurrentScope());
				if (dispatcherTypes == null) {
					return null;
				}

				VariablePathReference reference = new VariablePathReference(
						dispatcherTypes, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			}
			// handling "method()->"
			else if (dispatchEntry instanceof CallPath.MethodEntry) {
				String methodEntryPath = ((CallPath.MethodEntry) dispatchEntry)
						.getName();

				FunctionPathReference reference = new FunctionPathReference(
						methodEntryPath, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			} else {
				return null;
			}

			return result;
		}

		/**
		 * Gets function name by function invocation.
		 * 
		 * @param invocation
		 *            - function invocation.
		 * @return function name or null if not recognized.
		 */
		private String getFunctionNameByInvocation(FunctionInvocation invocation) {
			FunctionName funcName = invocation.getFunctionName();
			if (funcName == null) {
				return null;
			}

			Expression funcNameExpression = funcName.getFunctionName();
			if (funcNameExpression == null) {
				return null;
			}

			if (funcNameExpression instanceof Variable) {
				return getVariableName((Variable) funcNameExpression);
			} else if (funcNameExpression instanceof Identifier) {
				return ((Identifier) funcNameExpression).getName();
			}

			return null;
		}

		/**
		 * Reports the stack. Needed for local mode.
		 * 
		 * @param stack
		 *            - stack to report.
		 */
		private void reportStack(Stack<Scope> stack) {
			if (stack.isEmpty()) {
				return;
			}

			int currentScopeDepth = 0;

			// collecting global imports
			for (int i = 0; i < stack.size(); i++) {
				Scope currentScope = stack.get(i);
				Set<String> currentGlobalImports = currentScope
						.getGlobalImports();
				if (currentGlobalImports != null) {
					_overallGlobalImports.addAll(currentGlobalImports);

				}
			}
			_overallAliases.putAll(aliases);
			// _namespace=currentNamespace;
			// searching for a first scope that start earlier then current
			// offset
			Scope currentScope = null;
			for (int i = stack.size() - 1; i >= 0; i--) {
				currentScopeDepth = i;
				currentScope = stack.get(i);
				ASTNode root = currentScope.getRoot();
				if (root == null) {
					continue;
				}
				if (root.getStart() < currentOffset) {
					break;
				}
			}

			// nothing to report
			if (currentScope == null) {
				return;
			}

			isReportedStackGlobal = currentScope.isGlobalScope();

			isReportedScopeUnderClassOrFunction = checkIfScopeIsUnderClassOrFunction(
					stack, currentScopeDepth);

			// String scopePath = getScopePath(stack, i);

			Set<VariableInfo> variables = currentScope.getVariables();

			boolean localVariables = !currentScope.isGlobalScope();

			// reporting variables
			for (VariableInfo varInfo : variables) {
				if (varInfo.getNodeStart() > currentOffset) {
					continue;
				}
				String entryPath = varInfo.getName();
				VariablePHPEntryValue value = new VariablePHPEntryValue(varInfo
						.getModifier(), false, localVariables, false, varInfo
						.getVariableTypes(), varInfo.getNodeStart(),
						currentNamespace);
				int category = PHPModifier.isNamedConstant(varInfo
						.getModifier()) ? IPHPIndexConstants.CONST_CATEGORY
						: IPHPIndexConstants.VAR_CATEGORY;
				reporter.reportEntry(category, entryPath, value, module);
			}

			// reporting function/method parameters if needed, going up in the
			// stack searching for the function
			for (int i = stack.size() - 1; i >= 0; i--) {
				Scope scope = stack.get(i);
				if (scope.getEntry() != null) {
					IElementEntry entry = scope.getEntry();
					if (entry.getCategory() == IPHPIndexConstants.FUNCTION_CATEGORY
							&& entry.getValue() instanceof FunctionPHPEntryValue) {
						FunctionPHPEntryValue val = (FunctionPHPEntryValue) entry
								.getValue();
						Map<String, Set<Object>> parameters = val
								.getParameters();
						int[] parameterStartPositions = val
								.getParameterStartPositions();
						if (parameters != null && !parameters.isEmpty()) {
							int parCount = 0;
							for (Map.Entry<String, Set<Object>> parEntry : parameters
									.entrySet()) {
								String entryPath = parEntry.getKey();
								VariablePHPEntryValue value = new VariablePHPEntryValue(
										0,
										true,
										false,
										false,
										parEntry.getValue(),
										parameterStartPositions == null
												|| parameterStartPositions.length == 0 ? val
												.getStartOffset()
												: parameterStartPositions[parCount],
										currentNamespace);
								reporter.reportEntry(
										IPHPIndexConstants.VAR_CATEGORY,
										entryPath, value, module);
								parCount++;
							}
						}
					}
				}
			}
		}

		/**
		 * Checks if the scope specified is under class or function/method
		 * scope.
		 * 
		 * @param stack
		 *            - scopes stack.
		 * @param currentScopeDepth
		 *            - scope depth.
		 * @return true if the scope specified is under class or function/method
		 *         scope, false otherwise.
		 */
		private boolean checkIfScopeIsUnderClassOrFunction(Stack<Scope> stack,
				int currentScopeDepth) {
			for (int i = currentScopeDepth; i <= 0; i--) {
				Scope scope = scopes.get(i);
				IElementEntry entry = scope.getEntry();
				if (entry == null
						|| entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY
						|| entry.getCategory() == IPHPIndexConstants.FUNCTION_CATEGORY) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Gets scope entries path (including the ending delimiter).
		 * 
		 * @param stack
		 *            - scopes stack.
		 * @param pos
		 *            - position of the scope in stack.
		 * @param clazz
		 *            - class active for the scope.
		 * @return scope entries path
		 */
		String getScopePath(Stack<Scope> stack, int pos) {
			StringBuffer result = new StringBuffer();

			for (int i = pos; i >= 0; i--) {
				Scope currentScope = stack.get(i);
				ASTNode root = currentScope.getRoot();

				if (root instanceof FunctionDeclaration) {
					Identifier funcNameIdentifier = ((FunctionDeclaration) root)
							.getFunctionName();
					if (funcNameIdentifier == null) {
						continue;
					}
					String functionName = funcNameIdentifier.getName();
					if (functionName == null) {
						continue;
					}
					result.insert(0, functionName + IElementsIndex.DELIMITER);
				} else if (root instanceof MethodDeclaration) {
					Identifier funcNameIdentifier = ((MethodDeclaration) root)
							.getFunction().getFunctionName();
					if (funcNameIdentifier == null) {
						continue;
					}
					String functionName = funcNameIdentifier.getName();
					if (functionName == null) {
						continue;
					}
					result.insert(0, functionName + IElementsIndex.DELIMITER);
				} else if (root instanceof ClassDeclaration) {
					Identifier classNameIdentifier = ((ClassDeclaration) root)
							.getName();
					if (classNameIdentifier == null) {
						continue;
					}

					String className = classNameIdentifier.getName();
					if (className == null) {
						continue;
					}

					result.insert(0, className + IElementsIndex.DELIMITER);
				}
			}

			return result.toString();
		}

		/**
		 * Parses comment and adds parameter types if availavle.
		 * 
		 * @param comment
		 * @param parametersMap
		 * @return possible return types.
		 */
		private String[] applyComment(String comment,
				Map<String, Set<Object>> parametersMap) {
			/* TODO: Shalom - Hook the PHPDoc parser
			try {
				FunctionDocumentation doc = PHPDocUtils
						.parseFunctionPHPDoc(comment);
				if (doc == null) {
					return null;
				}

				TypedDescription[] params = doc.getParams();
				if (params != null && params.length != 0) {
					for (TypedDescription param : params) {
						String paramName = param.getName();
						if (paramName == null || paramName.length() == 0) {
							continue;
						}

						if (paramName.startsWith("$")) //$NON-NLS-1$
						{
							paramName = paramName.substring(1);
						}
						String[] types = param.getTypes();
						if (parametersMap != null) {
							Set<Object> toSetParams = parametersMap
									.get(paramName);
							if (parametersMap.containsKey(paramName)) {
								if (toSetParams == null && types != null) {
									toSetParams = new HashSet<Object>();
									parametersMap.put(paramName, toSetParams);
								}

								if (types != null) {
									for (String type : types) {
										toSetParams.add(type);
									}
								}
							}
						}
					}
				}

				TypedDescription returnDescr = doc.getReturn();
				if (returnDescr == null) {
					return null;
				}

				return returnDescr.getTypes();
			} catch (Throwable th) {
				// ignore
			}
			*/

			return null;
		}

		/**
		 * Reports field and adds it to the current class fields list.
		 * 
		 * @param modifier
		 *            - modifier.
		 * @param fieldName
		 *            - field name.
		 * @param fieldTypes
		 *            - field types.
		 * @param pos
		 *            - position.
		 * @return reported entry or null.
		 */
		private IElementEntry reportField(int modifier, String fieldName,
				Set<Object> fieldTypes, int pos) {
			VariablePHPEntryValue entryValue = new VariablePHPEntryValue(
					modifier, false, false, true, fieldTypes, pos,
					currentNamespace);

			if (currentClass != null) {
				String entryPath = currentClass.getClassEntry().getEntryPath()
						+ IElementsIndex.DELIMITER;

				entryPath += fieldName;

				IElementEntry result = reporter.reportEntry(
						IPHPIndexConstants.VAR_CATEGORY, entryPath, entryValue,
						module);
				currentClass.setField(fieldName, result);
				return result;
			}

			return null;
		}

		/**
		 * Reports field and adds it to the current class fields list.
		 * 
		 * @param modifier
		 *            - modifier.
		 * @param fieldName
		 *            - field name.
		 * @param fieldTypes
		 *            - field types.
		 * @param pos
		 *            - position.
		 * @return reported entry or null.
		 */
		private IElementEntry reportClassConst(int modifier, String fieldName,
				Set<Object> fieldTypes, int pos) {
			modifier |= PHPModifier.STATIC;
			modifier |= PHPModifier.FINAL;

			VariablePHPEntryValue entryValue = new VariablePHPEntryValue(
					modifier, false, false, true, fieldTypes, pos,
					currentNamespace);

			if (currentClass != null) {
				String entryPath = currentClass.getClassEntry().getEntryPath()
						+ IElementsIndex.DELIMITER;

				entryPath += fieldName;

				IElementEntry result = reporter.reportEntry(
						IPHPIndexConstants.CONST_CATEGORY, entryPath,
						entryValue, module);
				// currentClass.setField(fieldName, result);
				return result;
			}

			return null;
		}

		/**
		 * Adds block variables for try, for and other non-function expressions
		 * that can define local block variables.
		 * 
		 * @param block
		 *            - block.
		 */
		private void addBlockVariables(Block block) {
			ASTNode blockParent = block.getParent();
			if (blockParent instanceof CatchClause) {
				addCatchClauseVariables((CatchClause) blockParent);
			} else if (blockParent instanceof ForStatement) {
				addForVariables((ForStatement) blockParent);
			} else if (blockParent instanceof ForEachStatement) {
				addForEachVariables((ForEachStatement) blockParent);
			}
			else if (blockParent instanceof LambdaFunctionDeclaration) {
				LambdaFunctionDeclaration lambdaFunctionDeclaration=(LambdaFunctionDeclaration) blockParent;
				List<FormalParameter> formalParameters = lambdaFunctionDeclaration.formalParameters();
				for (FormalParameter p:formalParameters){
					String varName = p.getParameterName().getName();
					if (varName != null&&varName.startsWith("$")) {
						VariableInfo info = new VariableInfo(varName.substring(1), null,
								getCurrentScope(), lambdaFunctionDeclaration.getStart());
						getCurrentScope().addVariable(info);
					}
				}
				List<Expression> lexicalVariables = lambdaFunctionDeclaration.lexicalVariables();
				for (Expression p:lexicalVariables){
					String varName = p.getName();
					if (varName != null&&varName.startsWith("$")) {
						VariableInfo info = new VariableInfo(varName.substring(1), null,
								getCurrentScope(), lambdaFunctionDeclaration.getStart());
						getCurrentScope().addVariable(info);
					}
				}
			}
		}

		/**
		 * Adds "for each" local variables.
		 * 
		 * @param foreachStatement
		 *            - "for each" statement.
		 */
		private void addForEachVariables(ForEachStatement foreachStatement) {
			Expression key = foreachStatement.getKey();
			Expression value = foreachStatement.getValue();

			if (key != null && key instanceof Variable) {
				String varName = getVariableName((Variable) key);
				if (varName != null) {
					VariableInfo info = new VariableInfo(varName, null,
							getCurrentScope(), key.getStart());
					getCurrentScope().addVariable(info);
				}
			}

			if (value != null && value instanceof Variable) {
				String varName = getVariableName((Variable) value);
				if (varName != null) {
					VariableInfo info = new VariableInfo(varName, null,
							getCurrentScope(), value.getStart());
					getCurrentScope().addVariable(info);
				}
			}
		}

		/**
		 * Adds "for" local variables.
		 * 
		 * @param forStatement
		 *            - "for" statement.
		 */
		private void addForVariables(ForStatement forStatement) {
			Expression[] initializations = forStatement.getInitializations();
			if (initializations == null || initializations.length == 0) {
				return;
			}

			for (Expression initialization : initializations) {
				if (initialization instanceof Assignment) {
					Assignment assigment = (Assignment) initialization;
					VariableBase var = assigment.getVariable();
					if (var instanceof Variable) {
						String varName = getVariableName((Variable) var);
						if (varName == null) {
							continue;
						}

						Set<Object> types = countExpressionTypes(assigment
								.getValue());
						if (types != null && types.size() != 0) {
							VariableInfo info = new VariableInfo(varName,
									types, getCurrentScope(), var.getStart());
							getCurrentScope().addVariable(info);
						}
					}
				}
			}
		}

		/**
		 * Adds catch clause local variables.
		 * 
		 * @param clause
		 *            - catch clause.
		 */
		private void addCatchClauseVariables(CatchClause clause) {
			Expression var = clause.getVariable();
			String varName = getVariableName(var);
			if (varName != null) {
				VariableInfo info = new VariableInfo(varName,
						"Exception", getCurrentScope(), var.getStart()); //$NON-NLS-1$
				getCurrentScope().addVariable(info);
			}
		}
	}

	/**
	 * Mode.
	 */
	private boolean globalMode = true;

	/**
	 * Current offset.
	 */
	private int currentOffset = 0;

	/**
	 * Contents to index.
	 */
	private String _contents;

	/**
	 * Comments.
	 */
	private List<Comment> _comments;

	/**
	 * Overall global imports in the reported stack.
	 */
	private Set<String> _overallGlobalImports = new HashSet<String>();

	/**
	 * Overall global imports in the reported stack.
	 */
	private Map<String, String> _overallAliases = new HashMap<String, String>();

	/**
	 * Whether reported stack is global one.
	 */
	private boolean isReportedStackGlobal = true;

	/**
	 * Whether reported stack is under class or function/method.
	 */
	private boolean isReportedScopeUnderClassOrFunction = false;

	private boolean updateTaskTags = true;

	private String _namespace;

	/**
	 * @return is update task tags turned on
	 */
	public boolean isUpdateTaskTags() {
		return updateTaskTags;
	}

	/**
	 * @param updateTaskTags
	 */
	public void setUpdateTaskTags(boolean updateTaskTags) {
		this.updateTaskTags = updateTaskTags;
	}

	/**
	 * PDTPHPModuleIndexer constructor. Creates indexer in global mode.
	 */
	public PDTPHPModuleIndexer() {
	}

	/**
	 * PDTPHPModuleIndexer constructor. Indexer might be created in one of two
	 * modes: global mode and local mode. In global mode indexer reports
	 * includes, classes, functions, methods, fields and only global variables.
	 * In local mode indexer reports includes, classes, functions, methods,
	 * fields, global variables and local variables that are visible from the
	 * offset specified.
	 * 
	 * @param globalMode
	 *            - true if global mode is on, false if local mode is on.
	 * @param currentOffset
	 *            - offset used for local mode counting.
	 */
	public PDTPHPModuleIndexer(boolean globalMode, int currentOffset) {
		this.globalMode = globalMode;
		this.currentOffset = currentOffset;
	}

	/**
	 * Indexes contents of the the module.
	 * 
	 * @param contents
	 *            - module contents.
	 * @param module
	 *            - module.
	 * @param reporter
	 *            - reporter to report to.
	 */
	public synchronized void indexModule(String contents, IModule module,
			IIndexReporter reporter) {
		_contents = contents;
		try {
			Program program;

			try {
				if (!globalMode) {
					StringBuffer cutContents = new StringBuffer();
					String prevLine = ""; //$NON-NLS-1$
					int lineStartPos = 0;
					while (true) {
						String currentLine = readLine(contents, lineStartPos);
						if (currentLine == null) {
							break;
						}
						int lineEndPos = lineStartPos + currentLine.length();

						// if we are in local mode and current offset is in this
						// line,
						// replacing the line with the spaces, otherwise just
						// adding contents
						if (currentOffset > lineEndPos
								|| currentOffset < lineStartPos) {
							cutContents.append(currentLine);
						} else {
							StringBuilder bld = new StringBuilder();
							HashSet<String> variables = new HashSet<String>();

							bld.append("<? "); //$NON-NLS-1$
							if (currentLine.indexOf('{') != -1) {
								bld.append(prevLine);
							}
							bld.append(currentLine);
							bld.append(" ?>"); //$NON-NLS-1$
							PhpAstLexer53 lexer = new PhpAstLexer53(
									new StringReader(bld.toString()));

							// IsInCommentChecker isInCommentChecker = new
							// IsInCommentChecker(offset);
							// lexer.setCommentListener(isInCommentChecker);
							// lexer.setTasksPatterns(new Pattern[0]);
							Symbol prev = null;
							try {
								while (true) {

									Symbol next_token = lexer.next_token();

									if (next_token.sym == ParserConstants5.T_EQUAL) {
										if (prev != null) {
											if (prev.sym == ParserConstants5.T_VARIABLE) {
												String text = (String) prev.value;
												variables.add(text);
											}
										}
									}

									// System.out.println(next_token);

									if (next_token.sym == 0) {
										break;
									}
									prev = next_token;
								}

							} catch (IOException e) {

							}
							for (String s : variables) {
								VariablePHPEntryValue value = new VariablePHPEntryValue(
										0, false, false, false, Collections
												.emptySet(), lineStartPos, "");
								reporter.reportEntry(
										IPHPIndexConstants.VAR_CATEGORY, s
												.substring(1), value, module);
							}
							StringBuffer replaceBuffer = new StringBuffer();
							for (int i = 0; i < currentLine.length(); i++) {
								char ch = currentLine.charAt(i);
								if (ch == '\r') {
									replaceBuffer.append(ch);
								} else if (ch == '\r') {
									replaceBuffer.append(ch);
								} else if (Character.isWhitespace(ch)) {
									replaceBuffer.append(ch);
								} else {
									if (ch == '{') {
										replaceBuffer.append('{');
									} else if (ch == '}') {
										replaceBuffer.append('}');
									} else {
										replaceBuffer.append(' ');
									}
								}
							}

							cutContents.append(replaceBuffer);
						}
						prevLine = currentLine;
						lineStartPos += currentLine.length();
					}
					cutContents.append("\r\n;"); //$NON-NLS-1$

					program = parse(cutContents.toString(), module);
				} else {
					program = parse(contents, module);
					if (program == null) {
						return;
					}
				}

			} catch (Throwable th) {
				return;
			}

			if (program == null) {
				return;
			}

			// collecting comments
			CommentsVisitor commentsVisitor = new CommentsVisitor();
			PHPASTVisitorProxy commentsProxy = new PHPASTVisitorProxy(
					commentsVisitor);
			commentsProxy.visit(program);
			_comments = commentsVisitor.getComments();

			// indexing
			PHPASTVisitorProxy proxy = new PHPASTVisitorProxy(
					new PHPASTVisitor(reporter, module));
			proxy.visit(program);
			for (IIndexingASTVisitor v : ASTVisitorRegistry.getInstance()
					.getVisitors()) {
				v.process(program, reporter, module);
			}
		} catch (Throwable th) {
			PHPEditorPlugin.logError(
							"Unexpected exception while indexing module " + module.toString(), //$NON-NLS-1$
							th);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void indexModule(IModule module, IIndexReporter reporter) {
		try {
			Program program;

			try {

				setContents(module);
				program = parse(_contents, module);
				if (program == null) {
					return;
				}

			} catch (Throwable th) {
				return;
			}

			if (program == null) {
				return;
			}

			// collecting comments
			CommentsVisitor commentsVisitor = new CommentsVisitor();
			PHPASTVisitorProxy commentsProxy = new PHPASTVisitorProxy(
					commentsVisitor);
			commentsProxy.visit(program);
			_comments = commentsVisitor.getComments();

			// indexing
			PHPASTVisitorProxy proxy = new PHPASTVisitorProxy(
					new PHPASTVisitor(reporter, module));
			proxy.visit(program);
			for (IIndexingASTVisitor v : ASTVisitorRegistry.getInstance()
					.getVisitors()) {
				v.process(program, reporter, module);
			}
		} catch (Throwable th) {
			PHPEditorPlugin.logError("Unexpected exception while indexing module " + module.toString(), th); //$NON-NLS-1$
		}
	}

	private void setContents(IModule module) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(module
				.getContents(), EncodingUtils.getModuleEncoding(module)));

		StringBuffer moduleData = new StringBuffer();
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			moduleData.append(readData);
			buf = new char[1024];
		}
		reader.close();

		String contents = moduleData.toString();

		StringBuffer cutContents = new StringBuffer();

		int lineStartPos = 0;
		while (true) {
			String currentLine = readLine(contents, lineStartPos);
			if (currentLine == null) {
				break;
			}
			int lineEndPos = lineStartPos + currentLine.length();

			// if we are in local mode and current offset is in this line,
			// replacing the line with the spaces, otherwise just adding
			// contents
			if (globalMode || currentOffset > lineEndPos
					|| currentOffset < lineStartPos) {
				cutContents.append(currentLine);
			} else {
				StringBuffer replaceBuffer = new StringBuffer();
				for (int i = 0; i < currentLine.length(); i++) {
					char ch = currentLine.charAt(i);
					if (ch == '\r') {
						replaceBuffer.append(ch);
					} else if (ch == '\r') {
						replaceBuffer.append(ch);
					} else {
						replaceBuffer.append(' ');
					}
				}

				cutContents.append(replaceBuffer);
			}

			lineStartPos += currentLine.length();
		}

		_contents = cutContents.toString();
	}

	/**
	 * Gets global imports got while parsing in a local mode in the scopes
	 * stack.
	 * 
	 * @return global imports.
	 */
	public Set<String> getGlobalImports() {
		return _overallGlobalImports;
	}

	/**
	 * Gets whether reported scope is global.
	 * 
	 * @return whether reported scope is global.
	 */
	public boolean isReportedScopeGlobal() {
		return isReportedStackGlobal;
	}

	/**
	 * Gets whether reported scope is under class or function/method.
	 * 
	 * @return true if whether reported scope is under class or function/method,
	 *         false otherwise.
	 */
	public boolean isReportedScopeUnderClassOrFunction() {
		return isReportedStackGlobal;
	}

	/**
	 * Reads line from contents starting with position specified.
	 * 
	 * @param contents
	 *            - contents.
	 * @param lineStartPos
	 *            - line start position.
	 * @return line
	 */
	private String readLine(String contents, int lineStartPos) {
		StringBuffer result = new StringBuffer();

		for (int i = lineStartPos; i < contents.length(); i++) {
			if (i == contents.length() - 1) {
				result.append(contents.substring(lineStartPos, contents
						.length()));
				break;
			}
			char ch = contents.charAt(i);
			switch (ch) {
			case '\r':
				if (i < contents.length() - 1 && contents.charAt(i + 1) == '\n') {
					i++;
				}
			case '\n':
				result.append(contents.substring(lineStartPos, i + 1));
				return result.toString();
			}
		}

		if (result.length() == 0) {
			return null;
		}

		return result.toString();
	}

	/**
	 * Performs the parsing.
	 * 
	 * @param string
	 *            - contents.
	 * @param module
	 * @return parse results
	 * @throws Exception
	 *             IF an exception occurs
	 */
	private Program parse(String contents, IModule module) throws Exception {
		/* TODO: Shalom - Hook the Task-Tags updater
		try {
			if (isUpdateTaskTags()) {
				Reader reader = new StringReader(contents);
				updater.update(reader, module);
			}
		} catch (Throwable th) {
			String message = th.getMessage();
			if (message != null
					&& message.contains("Can't recover from previous error(s)")) { //$NON-NLS-1$
				return null;
			}
			PHPEditorPlugin.logError("Unexpected exception while updating task tags", th); //$NON-NLS-1$
		}
		*/
		try {
			Reader reader = new StringReader(contents);
			return ASTParser.parse(reader);
		} catch (Throwable th) {
			// 99% of the parsing errors should be skipped
			// String message = th.getMessage();
			// if
			// (message!=null&&message.contains("Can't recover from previous error(s)")){
			// return null;
			// }
			//			IdeLog.logError(PHPPlugin.getDefault(), "Unexpected exception while parsing module contents", //$NON-NLS-1$
			// th);
			return null;
		}
	}

	/**
	 * Converts encoded type to its string representation.
	 * 
	 * @param intType
	 *            - interger-encoded type.
	 * @return string representation or null if not found.
	 */
	private static String typeToString(int intType) {
		switch (intType) {
		case Scalar.TYPE_INT:
			return IPHPIndexConstants.INTEGER_TYPE;
		case Scalar.TYPE_REAL:
			return IPHPIndexConstants.REAL_TYPE;
		case Scalar.TYPE_STRING:
			return IPHPIndexConstants.STRING_TYPE;
		case Scalar.TYPE_SYSTEM:
			return IPHPIndexConstants.SYSTEM_TYPE;
		case Scalar.TYPE_UNKNOWN:
			return IPHPIndexConstants.UNKNOWN_TYPE;
		}

		return null;
	}

	/**
	 * Finds function or method PHPDoc comment above.
	 * 
	 * @param offset
	 *            - offset to start search from.
	 * @return comment contents or null.
	 */
	private String findFunctionPHPDocComment(int offset) {
		if (_comments == null || _comments.isEmpty()) {
			return null;
		}

		Comment nearestComment = null;
		for (Comment comment : _comments) {
			if (comment.getStart() > offset) {
				break;
			}

			nearestComment = comment;
		}

		if (nearestComment == null) {
			return null;
		}

		if (nearestComment.getCommentType() != Comment.TYPE_PHPDOC) {
			return null;
		}

		// checking if we have anything but whitespaces between comment end and
		// offset
		if (offset - 2 < 0 || nearestComment.getEnd() >= _contents.length()
				|| offset - 2 >= _contents.length()) {
			return null;
		}

		for (int i = nearestComment.getEnd() + 1; i < offset - 1; i++) {
			char ch = _contents.charAt(i);
			if (!Character.isWhitespace(ch)) {
				return null;
			}
		}

		return _contents.substring(nearestComment.getStart(), nearestComment
				.getEnd());
	}

	public void indexModule(Program program, IModule module,
			IIndexReporter reporter) {
		try {
			setContents(module);
		} catch (IOException e) {
			PHPEditorPlugin.logError("Exception while getting module contents", e); //$NON-NLS-1$
		}
		// collecting comments
		CommentsVisitor commentsVisitor = new CommentsVisitor();
		PHPASTVisitorProxy commentsProxy = new PHPASTVisitorProxy(
				commentsVisitor);
		commentsProxy.visit(program);
		_comments = commentsVisitor.getComments();

		// indexing
		PHPASTVisitorProxy proxy = new PHPASTVisitorProxy(new PHPASTVisitor(
				reporter, module));
		proxy.visit(program);
		for (IIndexingASTVisitor v : ASTVisitorRegistry.getInstance()
				.getVisitors()) {
			v.process(program, reporter, module);
		}

	}

	public Map<String, String> getAliases() {
		return _overallAliases;
	}

	public String getNamespace() {
		return _namespace;
	}
}
