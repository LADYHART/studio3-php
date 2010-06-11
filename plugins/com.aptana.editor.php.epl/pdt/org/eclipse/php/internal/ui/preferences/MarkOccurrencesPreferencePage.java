/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.internal.ui.preferences;

import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * The page for setting the editor options for occurrences marking.
 */
public final class MarkOccurrencesPreferencePage extends
		AbstractConfigurationBlockPreferencePage {

	/*
	 * @seeorg.eclipse.ui.internal.editors.text.
	 * AbstractConfigureationBlockPreferencePage#getHelpId()
	 */
	protected String getHelpId() {
		return null;
	}

	/*
	 * @see
	 * org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage
	 * #setDescription()
	 */
	protected void setDescription() {
		setDescription(PHPUIMessages
				.getString("MarkOccurrencesConfigurationBlock_title")); //$NON-NLS-1$
	}

	/*
	 * @seeorg.org.eclipse.ui.internal.editors.text.
	 * AbstractConfigurationBlockPreferencePage#setPreferenceStore()
	 */
	protected void setPreferenceStore() {
		setPreferenceStore(PHPEplPlugin.getDefault().getPreferenceStore());
	}

	protected Label createDescriptionLabel(Composite parent) {
		return null; // no description for new look.
	}

	/*
	 * @seeorg.eclipse.ui.internal.editors.text.
	 * AbstractConfigureationBlockPreferencePage
	 * #createConfigurationBlock(org.eclipse
	 * .ui.internal.editors.text.OverlayPreferenceStore)
	 */
	protected IPreferenceConfigurationBlock createConfigurationBlock(
			OverlayPreferenceStore overlayPreferenceStore) {
		return new MarkOccurrencesConfigurationBlock(overlayPreferenceStore);
	}

}
