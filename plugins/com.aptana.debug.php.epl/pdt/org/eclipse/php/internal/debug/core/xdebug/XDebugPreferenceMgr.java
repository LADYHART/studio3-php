/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.core.xdebug;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpPreferences;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

public class XDebugPreferenceMgr {
	
	public static Preferences getPreferences() {
		return IDELayerFactory.getIDELayer().getPrefs();
	}

	// general
	public static final String XDEBUG_PREF_PORT = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_port";
	public static final String XDEBUG_PREF_SHOWSUPERGLOBALS = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_showSuperGlobals";
	public static final String XDEBUG_PREF_ARRAYDEPTH = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_arrayDepth";
	public static final String XDEBUG_PREF_CHILDREN = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_children";
	public static final String XDEBUG_PREF_MULTISESSION = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_multisession";
	public static final String XDEBUG_PREF_REMOTESESSION = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_remotesession";
	//capture output
	public static final String XDEBUG_PREF_CAPTURESTDOUT = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_capturestdout";
	public static final String XDEBUG_PREF_CAPTURESTDERR = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_capturestderr";
	//proxy
	public static final String XDEBUG_PREF_USEPROXY = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_useproxy";
	public static final String XDEBUG_PREF_IDEKEY = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_idekey";
	public static final String XDEBUG_PREF_PROXY = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_proxy";

	public static enum AcceptRemoteSession {
		off, localhost, any, prompt
	}
	
	public static final String[] remoteSessionOptions = {PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_off, 
		PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_localhost, 
		PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_any,
		PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_prompt};		

	
	
	//just happens to match DBGp values so no conversion to DBGp Preferences required.
	public static enum CaptureOutput {
		off, copy, redirect
	}
	
	public static final String[] captureOutputOptions =  {PHPDebugCoreMessages.XDebugConfigurationDialog_capture_off,
		PHPDebugCoreMessages.XDebugConfigurationDialog_capture_copy, 
		PHPDebugCoreMessages.XDebugConfigurationDialog_capture_redirect};
	

	public static void setDefaults() {
		Preferences prefs = getPreferences();
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_PORT, getPortDefault());
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS, showSuperGlobalsDefault());
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH, getDepthDefault());
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION, useMultiSessionDefault());	
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN, getChildrenDefault());
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION, getAcceptRemoteSessionDefault());
		
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT, getCaptureDefault());
		prefs.setDefault(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR, getCaptureDefault());

		//Proxy config doesn't need its default values set here.
	}
	
	public static void applyDefaults(Preferences preferences) {
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_PORT, preferences.getDefaultInt(XDebugPreferenceMgr.XDEBUG_PREF_PORT));
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS, preferences.getDefaultBoolean(XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS));
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH, preferences.getDefaultInt(XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH));
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN, preferences.getDefaultInt(XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN));		
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION, preferences.getDefaultBoolean(XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION));
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION, preferences.getDefaultInt(XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION));		

		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT, preferences.getDefaultInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT));		
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR, preferences.getDefaultInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR));		
		
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY, preferences.getDefaultBoolean(XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY));		
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_IDEKEY, preferences.getDefaultBoolean(XDebugPreferenceMgr.XDEBUG_PREF_IDEKEY));		
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_PROXY, preferences.getDefaultBoolean(XDebugPreferenceMgr.XDEBUG_PREF_PROXY));				
	}
	
	/**
	 * create the DBGp preferences from the UI preferences.
	 * <ul>
	 * <li>Array Depth
	 * <li>Children
	 * <li>show super globals
	 * @return
	 */
	public static DBGpPreferences createSessionPreferences() {
		DBGpPreferences sessionPrefs = new DBGpPreferences();
		Preferences uiPrefs = getPreferences();
		int maxDepth = uiPrefs.getInt(XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH);
		if (1 == maxDepth) {
			XDebugPreferenceMgr.setDefaults();
			maxDepth = XDebugPreferenceMgr.getDepthDefault();
		}
		sessionPrefs.setValue(DBGpPreferences.DBGP_MAX_DEPTH_PROPERTY, maxDepth);

		int maxChildren = uiPrefs.getInt(XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN);		
		sessionPrefs.setValue(DBGpPreferences.DBGP_MAX_CHILDREN_PROPERTY, maxChildren);
		
		boolean getSuperGlobals = uiPrefs.getBoolean(XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS);
		sessionPrefs.setValue(DBGpPreferences.DBGP_SHOW_GLOBALS_PROPERTY, getSuperGlobals);
		
		//ui stored values are identical to DBGp expected values so no need to convert
		int captureStdout = uiPrefs.getInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT);
		sessionPrefs.setValue(DBGpPreferences.DBGP_CAPTURE_STDOUT_PROPERTY, captureStdout);
		
		int captureStderr = uiPrefs.getInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR);		
		sessionPrefs.setValue(DBGpPreferences.DBGP_CAPTURE_STDERR_PROPERTY, captureStderr);
		
		return sessionPrefs;
		
		
	}
	
	// provide easy access to the preferences which are not DBGp Session preferences.
	public static int getPort() {
		Preferences prefs = getPreferences();
		return getPort(prefs);
	}


	public static void setPort(int port) {
		Preferences prefs = getPreferences();
		setPort(prefs, port);
	}	
	
	
	public static int getPort(Preferences preferences) {
		return preferences.getInt(XDebugPreferenceMgr.XDEBUG_PREF_PORT);
	}

	public static void setPort(Preferences preferences, int port) {
		preferences.setValue(XDebugPreferenceMgr.XDEBUG_PREF_PORT, port);
	}	
	
	public static boolean useMultiSession() {
		Preferences prefs = getPreferences();
		return prefs.getBoolean(XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION); 
	}
	
	public static boolean useProxy() {
		Preferences prefs = getPreferences();
		return prefs.getBoolean(XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY); 
	}
	
	public static void setUseProxy(boolean newState) {
		Preferences prefs = getPreferences();
		prefs.setValue(XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY, newState); 
	}
	
	public static AcceptRemoteSession getAcceptRemoteSession() {
		Preferences prefs = getPreferences();
		int rSess = prefs.getInt(XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION); 
		return AcceptRemoteSession.values()[rSess];
	}
	
	//the defaults for the UI preferences
	private static int getDepthDefault() {
		return DBGpPreferences.DBGP_MAX_DEPTH_DEFAULT;
	}
	
	private static int getChildrenDefault() {
		return DBGpPreferences.DBGP_MAX_CHILDREN_DEFAULT;
	}

	private static int getPortDefault() {
		return DBGpPreferences.DBGP_PORT_DEFAULT;
	}

	private static boolean showSuperGlobalsDefault() {
		return DBGpPreferences.DBGP_SHOW_GLOBALS_DEFAULT;
	}
	
	private static int getCaptureDefault() {
		// we use the UI definition here as it would be mapped
		// if required to the appropriate DBGp Value.
		return CaptureOutput.copy.ordinal();
	}
	
	
	private static boolean useMultiSessionDefault() {
		// this is not a DBGp property.
		return false;
	}
	
	private static int getAcceptRemoteSessionDefault() {
		// this is not a DBGp property
		return AcceptRemoteSession.localhost.ordinal();
	}
	


}
