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
package com.aptana.editor.php.formatter.nodes;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.IFormatterWriter;
import com.aptana.formatter.nodes.FormatterTextNode;

/**
 * A PHP formatter node for HEREDOC and NOWDOC nodes.<br>
 * These nodes should not be formatted when written back.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPHeredocNode extends FormatterTextNode
{

	private IRegion heredocRegion;

	/**
	 * Constructs a new FormatterPHPHeredocNode
	 * 
	 * @param document
	 * @param startOffset
	 * @param endOffset
	 */
	public FormatterPHPHeredocNode(IFormatterDocument document, int startOffset, int endOffset)
	{
		super(document, startOffset, endOffset);
		heredocRegion = new Region(startOffset, endOffset - startOffset);
	}

	/**
	 * Override the default implementation to exclude the content from begin indented/formatted. (non-Javadoc)
	 * 
	 * @see com.aptana.formatter.nodes.FormatterTextNode#accept(com.aptana.formatter.IFormatterContext,
	 *      com.aptana.formatter.IFormatterWriter)
	 */
	public void accept(IFormatterContext context, IFormatterWriter visitor) throws Exception
	{
		IFormatterContext heredocContext = context.copy();
		heredocContext.setIndenting(false);
		visitor.write(heredocContext, getStartOffset(), getEndOffset());
		visitor.excludeRegion(heredocRegion);
	}
}
