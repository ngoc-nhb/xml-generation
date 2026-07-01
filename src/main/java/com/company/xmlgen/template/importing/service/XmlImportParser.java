package com.company.xmlgen.template.importing.service;

import com.company.xmlgen.template.importing.domain.XmlImportNode;

/**
 * Parses XML samples into a generic {@link XmlImportNode} tree.
 */
public interface XmlImportParser {

    XmlImportNode parse(byte[] xmlBytes);
}
