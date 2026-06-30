package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.xmlgeneration.dto.PreviewRequest;
import com.company.xmlgen.xmlgeneration.dto.PreviewResponse;

/**
 * Application service for XML preview.
 */
public interface PreviewService {

    PreviewResponse preview(PreviewRequest request);
}
