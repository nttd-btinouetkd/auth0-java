package com.auth0.client.mgmt;

import com.auth0.client.mgmt.filter.UsersExportFilter;
import com.auth0.json.mgmt.jobs.Job;
import com.auth0.json.mgmt.jobs.UsersExportField;
import com.auth0.net.Request;
import com.auth0.net.multipart.FilePart;
import com.auth0.net.multipart.KeyValuePart;
import com.auth0.net.multipart.RecordedMultipartRequest;
import okhttp3.mockwebserver.RecordedRequest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.auth0.client.MockServer.*;
import static com.auth0.client.RecordedRequestMatcher.hasHeader;
import static com.auth0.client.RecordedRequestMatcher.hasMethodAndPath;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class JobsEntityTest extends BaseMgmtEntityTest {

    @Test
    public void shouldThrowOnGetJobWithNullId() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'job id' cannot be null!");
        api.jobs().get(null);
    }

    @Test
    public void shouldGetJob() throws Exception {
        Request<Job> request = api.jobs().get("1");
        assertThat(request, Matchers.is(Matchers.notNullValue()));

        server.jsonResponse(MGMT_JOB, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("GET", "/api/v2/jobs/1"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        assertThat(response, Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void shouldThrowOnRequestUsersExportWithNullConnectionId() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'connection id' cannot be null!");
        api.jobs().exportUsers(null, null);
    }

    @Test
    public void shouldRequestUsersExport() throws Exception {
        Request<Job> request = api.jobs().exportUsers("con_123456789", null);
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MGMT_JOB_POST_USERS_EXPORTS, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/jobs/users-exports"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body.size(), is(1));
        assertThat(body, hasEntry("connection_id", "con_123456789"));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void shouldRequestUsersExportWithLimit() throws Exception {
        UsersExportFilter filter = new UsersExportFilter();
        filter.withLimit(82);
        Request<Job> request = api.jobs().exportUsers("con_123456789", filter);
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MGMT_JOB_POST_USERS_EXPORTS, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/jobs/users-exports"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body.size(), is(2));
        assertThat(body, hasEntry("connection_id", "con_123456789"));
        assertThat(body, hasEntry("limit", 82));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void shouldRequestUsersExportWithFormat() throws Exception {
        UsersExportFilter filter = new UsersExportFilter();
        filter.withFormat("csv");
        Request<Job> request = api.jobs().exportUsers("con_123456789", filter);
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MGMT_JOB_POST_USERS_EXPORTS, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/jobs/users-exports"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body.size(), is(2));
        assertThat(body, hasEntry("connection_id", "con_123456789"));
        assertThat(body, hasEntry("format", "csv"));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void shouldRequestUsersExportWithFields() throws Exception {
        UsersExportFilter filter = new UsersExportFilter();
        ArrayList<UsersExportField> fields = new ArrayList<>();
        fields.add(new UsersExportField("full_name"));
        fields.add(new UsersExportField("user_metadata.company_name", "company"));
        filter.withFields(fields);
        Request<Job> request = api.jobs().exportUsers("con_123456789", filter);
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MGMT_JOB_POST_USERS_EXPORTS, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/jobs/users-exports"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body.size(), is(2));
        assertThat(body, hasEntry("connection_id", "con_123456789"));
        assertThat(body, hasKey("fields"));
        @SuppressWarnings("unchecked")
        List<Map<String, String>> bodyFields = (List<Map<String, String>>) body.get("fields");
        assertThat(bodyFields.get(0).get("name"), is("full_name"));
        assertThat(bodyFields.get(0).get("export_as"), is(nullValue()));
        assertThat(bodyFields.get(1).get("name"), is("user_metadata.company_name"));
        assertThat(bodyFields.get(1).get("export_as"), is("company"));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void shouldSendUserAVerificationEmail() throws Exception {
        Request<Job> request = api.jobs().sendVerificationEmail("google-oauth2|1234", null);
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MGMT_JOB_POST_VERIFICATION_EMAIL, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/jobs/verification-email"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body.size(), is(1));
        assertThat(body, hasEntry("user_id", "google-oauth2|1234"));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void shouldSendUserVerificationEmailWithClientId() throws Exception {
        Request<Job> request = api.jobs().sendVerificationEmail("google-oauth2|1234", "AaiyAPdpYdesoKnqjj8HJqRn4T5titww");
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MGMT_JOB_POST_VERIFICATION_EMAIL, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/jobs/verification-email"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body.size(), is(2));
        assertThat(body, hasEntry("user_id", "google-oauth2|1234"));
        assertThat(body, hasEntry("client_id", "AaiyAPdpYdesoKnqjj8HJqRn4T5titww"));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnSendUserVerificationEmailWithNullUserId() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'user id' cannot be null!");
        api.jobs().sendVerificationEmail(null, null);
    }

    @Test
    public void shouldThrowOnRequestUsersImportWithNullConnectionId() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'connection id' cannot be null!");
        File usersFile = mock(File.class);
        api.jobs().importUsers(null, usersFile);
    }

    @Test
    public void shouldThrowOnRequestUsersImportWithNullUsersFile() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'users file' cannot be null!");
        api.jobs().importUsers("con_123456789", null);
    }

    @Test
    public void shouldRequestUsersImport() throws Exception {
        File usersFile = new File(MGMT_JOB_POST_USERS_IMPORTS_INPUT);
        Request<Job> request = api.jobs().importUsers("con_123456789", usersFile);
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MGMT_JOB_POST_USERS_IMPORTS, 200);
        Job response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/jobs/users-imports"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));
        String ctHeader = recordedRequest.getHeader("Content-Type");
        assertThat(ctHeader, startsWith("multipart/form-data"));
        String[] ctParts = ctHeader.split("multipart/form-data; boundary=");

        RecordedMultipartRequest recordedMultipartRequest = new RecordedMultipartRequest(recordedRequest);
        assertThat(recordedMultipartRequest.getPartsCount(), is(2));
        assertThat(recordedMultipartRequest.getBoundary(), is(notNullValue()));
        assertThat(recordedMultipartRequest.getBoundary(), is(ctParts[1]));

        //Connection ID
        KeyValuePart formParam = recordedMultipartRequest.getKeyValuePart("connection_id");
        MatcherAssert.assertThat(formParam, Matchers.is(Matchers.notNullValue()));
        MatcherAssert.assertThat(formParam.getValue(), Matchers.is("con_123456789"));

        //Users JSON
        FilePart jsonFile = recordedMultipartRequest.getFilePart("users");
        MatcherAssert.assertThat(jsonFile, Matchers.is(Matchers.notNullValue()));
        String utf8Contents = new String(Files.readAllBytes(usersFile.toPath()));
        MatcherAssert.assertThat(jsonFile.getContentType(), Matchers.is("text/json"));
        MatcherAssert.assertThat(jsonFile.getFilename(), Matchers.is("job_post_users_imports_input.json"));
        MatcherAssert.assertThat(jsonFile.getValue(), Matchers.is(utf8Contents));

        assertThat(response, is(notNullValue()));
    }

}
