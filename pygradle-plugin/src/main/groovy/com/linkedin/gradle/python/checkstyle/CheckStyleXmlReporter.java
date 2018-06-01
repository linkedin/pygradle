/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.checkstyle;

import com.linkedin.gradle.python.checkstyle.model.FileStyleViolationsContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class CheckStyleXmlReporter {

    private final FileStyleViolationsContainer violationContainer;

    public CheckStyleXmlReporter(FileStyleViolationsContainer container) {
        this.violationContainer = container;
    }

    public String generateXml() {
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();

        try {
            Document document = icFactory.newDocumentBuilder().newDocument();

            Element rootNode = document.createElement("checkstyle");
            violationContainer.getViolations().forEach(fileStyleViolation -> {
                Element fileNode = document.createElement("file");
                fileNode.setAttribute("name", fileStyleViolation.getFilename());

                fileStyleViolation.getViolations().forEach(violation -> {
                    Element error = document.createElement("error");
                    violation.createCheckstyleMap().forEach((key, value) -> error.setAttribute(key, value.toString()));
                    fileNode.appendChild(error);
                });
                rootNode.appendChild(fileNode);
            });

            document.appendChild(rootNode);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            StreamResult console = new StreamResult(stringWriter);
            transformer.transform(source, console);


            stringWriter.close();
            return stringWriter.toString();
        } catch (Exception e) {
            throw new CheckstyleException(e);
        }
    }

    private static class CheckstyleException extends RuntimeException {
        CheckstyleException(Throwable t) {
            super(t);
        }
    }
}
