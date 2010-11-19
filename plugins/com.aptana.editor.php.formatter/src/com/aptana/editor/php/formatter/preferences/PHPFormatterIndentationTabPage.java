/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
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
package com.aptana.editor.php.formatter.preferences;

import java.net.URL;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.formatter.ui.CodeFormatterConstants;
import com.aptana.formatter.ui.FormatterMessages;
import com.aptana.formatter.ui.IFormatterControlManager;
import com.aptana.formatter.ui.IFormatterModifyDialog;
import com.aptana.formatter.ui.preferences.FormatterModifyTabPage;
import com.aptana.formatter.ui.util.SWTFactory;

/**
 * PHP formatter indentation tab.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterIndentationTabPage extends FormatterModifyTabPage
{
	private static final String INDENTATION_PREVIEW_FILE = "indentation-preview.js"; //$NON-NLS-1$
	private final String[] TAB_OPTION_ITEMS = new String[] { CodeFormatterConstants.SPACE, CodeFormatterConstants.TAB,
			CodeFormatterConstants.MIXED };
	private final String[] TAB_OPTION_NAMES = new String[] {
			FormatterMessages.IndentationTabPage_general_group_option_tab_policy_SPACE,
			FormatterMessages.IndentationTabPage_general_group_option_tab_policy_TAB,
			FormatterMessages.IndentationTabPage_general_group_option_tab_policy_MIXED };

	/**
	 * Constructs a new PHPFormatterIndentationTabPage
	 * 
	 * @param dialog
	 */
	public PHPFormatterIndentationTabPage(IFormatterModifyDialog dialog)
	{
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.formatter.ui.FormatterModifyTabPage#createOptions(com.aptana.formatter.ui.IFormatterControlManager,
	 * org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createOptions(IFormatterControlManager manager, Composite parent)
	{
		Group group = SWTFactory.createGroup(parent,
				Messages.PHPFormatterIndentationTabPage_indentationGeneralGroupLabel, 2, 1, GridData.FILL_HORIZONTAL);
		final Combo tabOptions = manager.createCombo(group, PHPFormatterConstants.FORMATTER_TAB_CHAR,
				FormatterMessages.IndentationTabPage_general_group_option_tab_policy, TAB_OPTION_ITEMS,
				TAB_OPTION_NAMES);
		final Text indentationSize = manager.createNumber(group, PHPFormatterConstants.FORMATTER_INDENTATION_SIZE,
				FormatterMessages.IndentationTabPage_general_group_option_indent_size);
		final Text tabSize = manager.createNumber(group, PHPFormatterConstants.FORMATTER_TAB_SIZE,
				FormatterMessages.IndentationTabPage_general_group_option_tab_size);
		tabSize.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				int index = tabOptions.getSelectionIndex();
				if (index >= 0)
				{
					final boolean tabMode = CodeFormatterConstants.TAB.equals(TAB_OPTION_ITEMS[index]);
					if (tabMode)
					{
						indentationSize.setText(tabSize.getText());
					}
				}
			}
		});
		new TabOptionHandler(manager, tabOptions, indentationSize);

		group = SWTFactory.createGroup(parent, Messages.PHPFormatterTabPage_indentGroupLabel, 1, 1,
				GridData.FILL_HORIZONTAL);
		manager.createCheckbox(group, PHPFormatterConstants.INDENT_BLOCKS,
				Messages.PHPFormatterIndentationTabPage_statementsWithinBlocks);
		manager.createCheckbox(group, PHPFormatterConstants.INDENT_NAMESPACE_BLOCKS,
				Messages.PHPFormatterIndentationTabPage_statementsWithinNamespaceBlocks);
		manager.createCheckbox(group, PHPFormatterConstants.INDENT_TYPE_BODY,
				Messages.PHPFormatterIndentationTabPage_statementsWithinTypes);
		manager.createCheckbox(group, PHPFormatterConstants.INDENT_FUNCTION_BODY,
				Messages.PHPFormatterIndentationTabPage_statementsWithinFunctions);
		manager.createCheckbox(group, PHPFormatterConstants.INDENT_SWITCH_BODY,
				Messages.PHPFormatterIndentationTabPage_statementsWithinSwitch);
		manager.createCheckbox(group, PHPFormatterConstants.INDENT_CASE_BODY,
				Messages.PHPFormatterIndentationTabPage_statementsWithinCase);
		manager.createCheckbox(group, PHPFormatterConstants.INDENT_BREAK_IN_CASE,
				Messages.PHPFormatterIndentationTabPage_breakWithinCase);
	}

	/**
	 * Listens to changes in the type of tab selected.
	 */
	private class TabOptionHandler extends SelectionAdapter implements IFormatterControlManager.IInitializeListener
	{

		private IFormatterControlManager manager;
		private Combo tabOptions;
		private Text indentationSize;

		/**
		 * Constructor.
		 * 
		 * @param controlManager
		 */
		public TabOptionHandler(IFormatterControlManager controlManager, Combo tabOptions, Text indentationSize)
		{
			this.manager = controlManager;
			this.tabOptions = tabOptions;
			this.indentationSize = indentationSize;
			tabOptions.addSelectionListener(this);
			manager.addInitializeListener(this);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e)
		{
			int index = tabOptions.getSelectionIndex();
			if (index >= 0)
			{
				final boolean tabMode = CodeFormatterConstants.TAB.equals(TAB_OPTION_ITEMS[index]);
				manager.enableControl(indentationSize, !tabMode);
			}
		}

		public void initialize()
		{
			final boolean tabMode = CodeFormatterConstants.TAB.equals(manager
					.getString(PHPFormatterConstants.FORMATTER_TAB_CHAR));
			manager.enableControl(indentationSize, !tabMode);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.FormatterModifyTabPage#getPreviewContent()
	 */
	protected URL getPreviewContent()
	{
		return getClass().getResource(INDENTATION_PREVIEW_FILE);
	}
}
