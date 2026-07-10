package com.company.xmlgen.authentication.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.CommonErrorCode;
import com.company.xmlgen.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Ensures the current caller has system administrator privileges.
 */
@Component
public class AdminAuthorizationGuard {

    public void requireAdmin() {
        AuthenticatedUser user = getCurrentUser();
        if (!user.admin()) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }
    }

    AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }
        return user;
    }
}
