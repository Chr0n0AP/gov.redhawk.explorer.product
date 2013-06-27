/*******************************************************************************
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package gov.redhawk.explorer.tests;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests the ide.product file to ensure custom changes are not overwritten by
 * the Product Editor.
 */
public class SCAExplorerProductDefinitionTest {

	private DocumentBuilder builder;
	private List<Node> configurationElements;

	/**
	 * Parse the sca_explorer.product file and build list of items under the
	 * configurations node.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		this.builder = factory.newDocumentBuilder();
		final Document document = this.parseSCAExplorerProduct();
		this.buildConfigElementList(document);

	}

	/**
	 * Tear down the builder and configElements list between tests.
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.builder = null;
		this.configurationElements = null;
	}

	/**
	 * Tests for presence of a property element in the configuration section of
	 * the ide.product file.
	 */
	@Test
	public void isConfigurationPropertyElementPresent() {
		Assert.assertNotNull("The ide.product file must contain a configuration/property element", this.configurationElements);
	}

	/**
	 * Tests for presence and correct value of the osgi.instance.area property.
	 */
	@Test
	public void containsOSGIInstanceAreaProperty() {
		Element buildIdElement = null;
		for (final Node node : this.configurationElements) {
			if (node instanceof Element) {
				final Element temp = (Element) node;
				if (temp.getNodeName().equals("property") && temp.getAttribute("name").equals("osgi.instance.area")) {
					buildIdElement = temp;
					break;
				}
			}
		}

		Assert.assertNotNull("The sca_explorer.product file must contain an osgi.instance.area property", buildIdElement);
		Assert.assertEquals("The osgi.instance.area property must have a value of '@user.home/.sca_explorer'", "@user.home/.sca_explorer",
			buildIdElement.getAttribute("value"));
	}

	@Test
	public void containsBuildIDProperty() {
		Element buildIdElement = null;
		for (final Node node : this.configurationElements) {
			if (node instanceof Element) {
				final Element temp = (Element) node;
				if (temp.getNodeName().equals("property") && temp.getAttribute("name").equals("eclipse.buildId")) {
					buildIdElement = temp;
					break;
				}
			}
		}
		Assert.assertNotNull("The ide.product file must contain an eclipse.buildId property", buildIdElement);
		Assert.assertEquals("The eclipse.buildId property must have a value of '@buildId@'", "@buildId@", buildIdElement.getAttribute("value"));
	}

	/**
	 * Parses the ide.product file.
	 * 
	 * @return the {@link Document} resulting from parsing the ide.product file
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document parseSCAExplorerProduct() throws SAXException, IOException {
		Bundle bundle = Platform.getBundle("gov.redhawk.product.sca_explorer");
		Assert.assertNotNull("Can not find Explorer Bundle", bundle);
		URL fileUrl = FileLocator.find(bundle, new Path("sca_explorer.product"), null);
		Assert.assertNotNull("Can not find product in bundle", fileUrl);
		fileUrl = FileLocator.toFileURL(fileUrl);
		Assert.assertNotNull("Can not find product in bundle", fileUrl);
		return this.builder.parse(fileUrl.toString());
	}

	/**
	 * Builds a list of {@link Node} under the "configurations" element.
	 * 
	 * @param document the {@link Document} associated with the ide.product file
	 */
	private void buildConfigElementList(final Document document) {
		final Element root = document.getDocumentElement();
		final NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);
			if (node instanceof Element) {
				final Element child = (Element) node;
				if (child.getNodeName().equals("configurations")) {
					final NodeList configurationsNodes = child.getChildNodes();
					for (int j = 0; j < configurationsNodes.getLength(); j++) {
						if (configurationsNodes.item(j).getNodeName().equals("property")) {
							if (this.configurationElements == null) {
								this.configurationElements = new ArrayList<Node>();
							}
							this.configurationElements.add(configurationsNodes.item(j));
						}
					}
				}
			}
		}
		if (this.configurationElements == null) {
			this.configurationElements = Collections.emptyList();
		}
	}
}
