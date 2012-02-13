/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.text;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

import com.aptana.editor.common.text.CommonAutoIndentStrategy;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.ui.editor.PHPSourceConfiguration;

/**
 * Additional tests to ensure that PHP partitions are being correctly reported as comments or not.
 * 
 * @author Ingo Muschenetz
 */
public class CommonAutoIndentStrategyTest extends TestCase
{
	public void testIsComment()
	{
		List<String> validTokenTypes = new ArrayList<String>(Arrays.asList(IPHPConstants.PHP_SLASH_LINE_COMMENT,
				IPHPConstants.PHP_HASH_LINE_COMMENT, IPHPConstants.PHP_MULTI_LINE_COMMENT,
				IPHPConstants.PHP_DOC_COMMENT));

		for (String ct : PHPSourceConfiguration.CONTENT_TYPES)
		{
			TestCommonAutoIndentStrategy strategy = new TestCommonAutoIndentStrategy(ct);
			if (validTokenTypes.contains(ct))
			{
				assertTrue(MessageFormat.format("{0} is a valid comment type", ct), strategy.isComment(0));
			}
			else
			{
				assertFalse(MessageFormat.format("{0} is not a valid comment type", ct), strategy.isComment(0));
			}
		}
	}

	private static class TestCommonAutoIndentStrategy extends CommonAutoIndentStrategy
	{
		TestCommonAutoIndentStrategy(String contentType)
		{
			super(contentType, null, null, null);
		}

		public void customizeDocumentCommand(IDocument document, DocumentCommand command)
		{
		}

		@Override
		protected boolean autoIndent(IDocument d, DocumentCommand c)
		{
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.aptana.editor.common.text.CommonAutoIndentStrategy#isComment(int)
		 */
		@Override
		public boolean isComment(int offset)
		{
			return super.isComment(offset);
		}

	}
}
