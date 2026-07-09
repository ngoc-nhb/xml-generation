package com.company.xmlgen.template.importing.service;

import com.company.xmlgen.template.importing.domain.XmlImportNode;
import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftFieldResponse;
import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftResponse;
import com.company.xmlgen.template.importing.exception.XmlImportErrorCode;
import com.company.xmlgen.template.importing.exception.XmlImportException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orchestrates XML parsing and template draft generation.
 */
@Service
public class TemplateImportServiceImpl implements TemplateImportService {

    private final XmlImportParser xmlImportParser;
    private final TemplateDraftBuilder templateDraftBuilder;
    private final TemplateImportSampleInputBuilder templateImportSampleInputBuilder;

    public TemplateImportServiceImpl(
            XmlImportParser xmlImportParser,
            TemplateDraftBuilder templateDraftBuilder,
            TemplateImportSampleInputBuilder templateImportSampleInputBuilder) {
        this.xmlImportParser = xmlImportParser;
        this.templateDraftBuilder = templateDraftBuilder;
        this.templateImportSampleInputBuilder = templateImportSampleInputBuilder;
    }

    @Override
    public TemplateImportDraftResponse importXml(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new XmlImportException(XmlImportErrorCode.XML_IMPORT_EMPTY, "XML file is required.");
        }

        try {
            XmlImportNode root = xmlImportParser.parse(file.getBytes());
            String originalFilename = file.getOriginalFilename();
            String sourceFileName = originalFilename == null ? "import.xml" : originalFilename;
            String suggestedName = suggestedNameFromFilename(sourceFileName);
            String suggestedCode = suggestedCodeFromFilename(sourceFileName);
            List<TemplateImportDraftFieldResponse> fields = templateDraftBuilder.build(root);

            return new TemplateImportDraftResponse(
                    suggestedCode,
                    suggestedName,
                    sourceFileName,
                    fields,
                    templateImportSampleInputBuilder.build(root, fields));
        } catch (IOException ex) {
            throw new XmlImportException(XmlImportErrorCode.XML_IMPORT_MALFORMED, "Unable to read XML file.");
        }
    }

    static String suggestedNameFromFilename(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    static String suggestedCodeFromFilename(String filename) {
        String baseName = suggestedNameFromFilename(filename).toUpperCase(Locale.ROOT);
        String code = baseName.replaceAll("[^A-Z0-9]", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
        return code.isBlank() ? "IMPORTED_TEMPLATE" : code;
    }
}
