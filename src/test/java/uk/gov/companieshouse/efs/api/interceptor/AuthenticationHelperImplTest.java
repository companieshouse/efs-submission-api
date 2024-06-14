package uk.gov.companieshouse.efs.api.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import jakarta.servlet.http.HttpServletRequest;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperImplTest {
    private static final String USER_EMAIL = "qschaden@somewhere.email.com";
    private static final String USER_FORENAME = "Quentin";
    private static final String USER_SURNAME = "Schaden";
    public static final String AUTHORISED_ROLES = "ERIC-Authorised-Roles";
    public static final String AUTHORISED_USER = "ERIC-Authorised-User";
    public static final String USER_FORMAT = "{0};forename={1};surname={2}";
    public static final String AUTHORISED_KEY_ROLES = "ERIC-Authorised-Key-Roles";
    public static final String ROLE_1_ROLE_2 = "role-1 role-2";
    public static final String ROLE_1 = "role-1";
    public static final String ROLE_2 = "role-2";

    private AuthenticationHelper testHelper;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        testHelper = new AuthenticationHelperImpl();
    }

    @Test
    void getAuthorisedIdentityWhenRequestNull() {

        MatcherAssert.assertThat(testHelper.getAuthorisedIdentity(null), is(nullValue()));
    }

    @Test
    void getAuthorisedIdentityWhenRequestNotNull() {
        String expected = "identity";

        when(request.getHeader("ERIC-Identity")).thenReturn(expected);

        Assertions.assertEquals(testHelper.getAuthorisedIdentity(request), expected);
    }

    @Test
    void getAuthorisedIdentityType() {
        String expected = "identity-type";

        when(request.getHeader("ERIC-Identity-Type")).thenReturn(expected);

        Assertions.assertEquals(testHelper.getAuthorisedIdentityType(request), expected);
    }

    @Test
    void isApiKeyIdentityTypeWhenItIs() {
        assertTrue(testHelper.isApiKeyIdentityType("key"));
    }

    @Test
    void isApiKeyIdentityTypeWhenItIsNot() {
        assertFalse(testHelper.isApiKeyIdentityType("KEY"));
    }

    @Test
    void isOauth2IdentityTypeWhenItIs() {
        assertTrue(testHelper.isOauth2IdentityType("oauth2"));
    }

    @Test
    void isOauth2IdentityTypeWhenItIsNot() {
        assertFalse(testHelper.isOauth2IdentityType("Oauth2"));
    }

    @Test
    void getAuthorisedUser() {
        String expected = "authorised-user";

        when(request.getHeader(AUTHORISED_USER)).thenReturn(expected);

        assertEquals(testHelper.getAuthorisedUser(request), expected);
    }

    @Test
    void getAuthorisedUserEmail() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
            MessageFormat.format(USER_FORMAT, USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertEquals(USER_EMAIL, testHelper.getAuthorisedUserEmail(request));
    }

    @Test
    void getAuthorisedUserEmailWhenUserNul() {
        assertNull(testHelper.getAuthorisedUserEmail(request));
    }

    @Test
    void getAuthorisedUserEmailWhenUserMissing() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn("");

        assertNull(testHelper.getAuthorisedUserEmail(request));
    }

    @Test
    void getAuthorisedUserEmailWhenEmpty() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(";");

        assertNull(testHelper.getAuthorisedUserEmail(request));
    }

    @Test
    void getAuthorisedUserEmailWhenNull() {
        assertNull(testHelper.getAuthorisedUserEmail(request));
    }

    @Test
    void getAuthorisedUserForename() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
            MessageFormat.format(USER_FORMAT, USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertEquals(USER_FORENAME, testHelper.getAuthorisedUserForename(request));
    }

    @Test
    void getAuthorisedUserForenameWhenUserNull() {
        assertNull(testHelper.getAuthorisedUserForename(request));
    }

    @Test
    void getAuthorisedUserForenameWhenUserEmpty() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn("");

        assertNull(testHelper.getAuthorisedUserForename(request));
    }

    @Test
    void getAuthorisedUserForenameWhenMissing() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(MessageFormat.format("{0}", USER_EMAIL));

        assertNull(testHelper.getAuthorisedUserForename(request));
    }

    @Test
    void getAuthorisedUserForenameWhenUnnamed() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
            MessageFormat.format("{0};{1}", USER_EMAIL, USER_FORENAME));

        assertNull(testHelper.getAuthorisedUserForename(request));
    }

    @Test
    void getAuthorisedUserSurname() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
            MessageFormat.format(USER_FORMAT, USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertEquals(USER_SURNAME, testHelper.getAuthorisedUserSurname(request));
    }

    @Test
    void getAuthorisedUserSurnameWhenMissing() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
            MessageFormat.format("{0};forename={1}", USER_EMAIL, USER_FORENAME));

        assertNull(testHelper.getAuthorisedUserSurname(request));
    }

    @Test
    void getAuthorisedUserSurnameWhenUnnamed() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
            MessageFormat.format("{0};forename={1};{2}", USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertNull(testHelper.getAuthorisedUserSurname(request));
    }

    @Test
    void getAuthorisedScope() {
        String expected = "authorised-scope";

        when(request.getHeader("ERIC-Authorised-Scope")).thenReturn(expected);

        assertEquals(testHelper.getAuthorisedScope(request), expected);
    }

    @Test
    void getAuthorisedRoles() {
        String expected = "authorised-roles";

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(expected);

        assertEquals(testHelper.getAuthorisedRoles(request), expected);
    }

    @Test
    void getAuthorisedRolesArray() {
        String[] expected = new String[]{ROLE_1, ROLE_2};

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertArrayEquals(testHelper.getAuthorisedRolesArray(request), expected);
    }

    @Test
    void getAuthorisedRolesArrayWhenRolesNull() {
        String[] expected = new String[]{};

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(null);

        assertArrayEquals(testHelper.getAuthorisedRolesArray(request), expected);
    }

    @Test
    void getAuthorisedRolesArrayWhenRolesEmpty() {
        String[] expected = new String[]{};

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn("");

        assertArrayEquals(testHelper.getAuthorisedRolesArray(request), expected);
    }

    @Test
    void isRoleAuthorisedWhenItIs() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertTrue(testHelper.isRoleAuthorised(request, ROLE_1));
    }

    @Test
    void isRoleAuthorisedWhenItIsNot() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertFalse(testHelper.isRoleAuthorised(request, "role-0"));
    }

    @Test
    void isRoleAuthorisedWhenItIsNull() {
        assertFalse(testHelper.isRoleAuthorised(request, null));
    }

    @Test
    void isRoleAuthorisedWhenItIsEmpty() {
        assertFalse(testHelper.isRoleAuthorised(request, ""));
    }

    @Test
    void isRoleAuthorisedWhenRolesNull() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(null);

        assertFalse(testHelper.isRoleAuthorised(request, ROLE_1));
    }

    @Test
    void isRoleAuthorisedWhenRolesEmpty() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn("");

        assertFalse(testHelper.isRoleAuthorised(request, ROLE_1));
    }

    @Test
    void getAuthorisedKeyRoles() {
        String expected = "authorised-key-roles";

        when(request.getHeader(AUTHORISED_KEY_ROLES)).thenReturn(expected);

        assertEquals(testHelper.getAuthorisedKeyRoles(request), expected);

    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIs() {
        when(request.getHeader(AUTHORISED_KEY_ROLES)).thenReturn("*");

        assertTrue(testHelper.isKeyElevatedPrivilegesAuthorised(request));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsNot() {
        when(request.getHeader(AUTHORISED_KEY_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertFalse(testHelper.isKeyElevatedPrivilegesAuthorised(request));
    }
}