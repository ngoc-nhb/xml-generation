package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.xmlgeneration.dto.ExportRequest;
import com.company.xmlgen.xmlgeneration.dto.ExportResponse;

/**
 * Application service for XML export.
 */
public interface ExportService {

    ExportResponse export(ExportRequest request);
}
