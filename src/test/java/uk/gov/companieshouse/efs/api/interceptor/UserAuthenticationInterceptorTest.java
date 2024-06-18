package uk.gov.companieshouse.efs.api.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationInterceptorTest {
    public static final String USER_EMAIL_FIELD = "user_email";
    private static final String USER_EMAIL = "qschaden@ch.gov.uk";
    private static final String USER_FORENAME = "Quentin";
    private static final String USER_SURNAME = "Schaden";
    public static final String IDENTITY_FIELD = "identity";
    public static final String OAUTH2_FIELD = "oauth2";
    public static final String USER_ID_FIELD = "user_id";
    public static final String USER_FORENAME_FIELD = "user_forename";
    public static final String USER_SURNAME_FIELD = "user_surname";

    private UserAuthenticationInterceptor testInterceptor;

    @Mock
    private AuthenticationHelper authHelper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        testInterceptor = new UserAuthenticationInterceptor(authHelper, logger);
    }

    @Test
    void preHandleWhenIdentityTypeNull() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(null);

        assertFalse(testInterceptor.preHandle(request, response, null));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeEmpty() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn("");

        assertFalse(testInterceptor.preHandle(request, response, null));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityNull() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn("key");
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(null);

        assertFalse(testInterceptor.preHandle(request, response, null));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityEmpty() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn("key");
        when(authHelper.getAuthorisedIdentity(request)).thenReturn("");

        assertFalse(testInterceptor.preHandle(request, response, null));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeNotOauth2OrApiKey() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn("none");
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(IDENTITY_FIELD);
        when(authHelper.isOauth2IdentityType("none")).thenReturn(false);
        when(authHelper.isApiKeyIdentityType("none")).thenReturn(false);

        assertFalse(testInterceptor.preHandle(request, response, null));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeOauth2AndAuthorisedUserNull() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(OAUTH2_FIELD);
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(IDENTITY_FIELD);
        when(authHelper.isOauth2IdentityType(OAUTH2_FIELD)).thenReturn(true);
        when(authHelper.getAuthorisedUser(request)).thenReturn(null);

        assertFalse(testInterceptor.preHandle(request, response, null));
        verify(request).setAttribute(USER_ID_FIELD, IDENTITY_FIELD);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeOauth2AndAuthorisedUserEmpty() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(OAUTH2_FIELD);
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(IDENTITY_FIELD);
        when(authHelper.isOauth2IdentityType(OAUTH2_FIELD)).thenReturn(true);
        when(authHelper.getAuthorisedUser(request)).thenReturn("");

        assertFalse(testInterceptor.preHandle(request, response, null));
        verify(request).setAttribute(USER_ID_FIELD, IDENTITY_FIELD);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeOauth2() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(OAUTH2_FIELD);
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(IDENTITY_FIELD);
        when(authHelper.isOauth2IdentityType(OAUTH2_FIELD)).thenReturn(true);
        when(authHelper.getAuthorisedUser(request)).thenReturn(
            String.join(";", USER_EMAIL, "user_forename=" + USER_FORENAME, "user_surname=" + USER_SURNAME));
        when(authHelper.getAuthorisedUserEmail(request)).thenReturn(USER_EMAIL);
        when(authHelper.getAuthorisedUserForename(request)).thenReturn(USER_FORENAME);
        when(authHelper.getAuthorisedUserSurname(request)).thenReturn(USER_SURNAME);

        assertTrue(testInterceptor.preHandle(request, response, null));
        verify(request).setAttribute(USER_ID_FIELD, IDENTITY_FIELD);
        verify(request).setAttribute(USER_EMAIL_FIELD, USER_EMAIL);
        verify(request).setAttribute(USER_FORENAME_FIELD, USER_FORENAME);
        verify(request).setAttribute(USER_SURNAME_FIELD, USER_SURNAME);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeOauth2AndUserNameMissing() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(OAUTH2_FIELD);
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(IDENTITY_FIELD);
        when(authHelper.isOauth2IdentityType(OAUTH2_FIELD)).thenReturn(true);
        when(authHelper.getAuthorisedUser(request)).thenReturn(USER_EMAIL);
        when(authHelper.getAuthorisedUserEmail(request)).thenReturn(USER_EMAIL);

        assertTrue(testInterceptor.preHandle(request, response, null));
        verify(request).setAttribute(USER_ID_FIELD, IDENTITY_FIELD);
        verify(request).setAttribute(USER_EMAIL_FIELD, USER_EMAIL);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeOauth2AndUserSurnameMissing() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(OAUTH2_FIELD);
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(IDENTITY_FIELD);
        when(authHelper.isOauth2IdentityType(OAUTH2_FIELD)).thenReturn(true);
        when(authHelper.getAuthorisedUser(request)).thenReturn(
            String.join(";", USER_EMAIL, "user_forename=" + USER_FORENAME));
        when(authHelper.getAuthorisedUserEmail(request)).thenReturn(USER_EMAIL);
        when(authHelper.getAuthorisedUserForename(request)).thenReturn(USER_FORENAME);

        assertTrue(testInterceptor.preHandle(request, response, null));
        verify(request).setAttribute(USER_ID_FIELD, IDENTITY_FIELD);
        verify(request).setAttribute(USER_EMAIL_FIELD, USER_EMAIL);
        verify(request).setAttribute(USER_FORENAME_FIELD, USER_FORENAME);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void preHandleWhenIdentityTypeOauth2AndUserForenameInvalid() {
        when(authHelper.getAuthorisedIdentityType(request)).thenReturn(OAUTH2_FIELD);
        when(authHelper.getAuthorisedIdentity(request)).thenReturn(IDENTITY_FIELD);
        when(authHelper.isOauth2IdentityType(OAUTH2_FIELD)).thenReturn(true);
        when(authHelper.getAuthorisedUser(request)).thenReturn(String.join(";", USER_EMAIL, USER_FORENAME));
        when(authHelper.getAuthorisedUserEmail(request)).thenReturn(USER_EMAIL);

        assertTrue(testInterceptor.preHandle(request, response, null));
        verify(request).setAttribute(USER_ID_FIELD, IDENTITY_FIELD);
        verify(request).setAttribute(USER_EMAIL_FIELD, USER_EMAIL);
        verifyNoMoreInteractions(request, response);
    }

    @Test
    void afterCompletion() {
        testInterceptor.afterCompletion(request, response, null, null);

        verify(request).setAttribute(USER_ID_FIELD, null);
        verify(request).setAttribute(USER_EMAIL_FIELD, null);
        verify(request).setAttribute(USER_FORENAME_FIELD, null);
        verify(request).setAttribute(USER_SURNAME_FIELD, null);
        verifyNoMoreInteractions(request, response);
    }
}