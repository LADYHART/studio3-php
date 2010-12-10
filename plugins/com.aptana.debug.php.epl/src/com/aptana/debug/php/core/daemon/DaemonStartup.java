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
package com.aptana.debug.php.core.daemon;

import org.eclipse.ui.IStartup;

/**
 * This class is loaded when the UI is loading and starts the daemon that is defined as 'best match'.
 */
public class DaemonStartup implements IStartup
{

	public void earlyStartup()
	{
		// Do nothing. Let the plugin startup do the daemon loading.
	}
}
