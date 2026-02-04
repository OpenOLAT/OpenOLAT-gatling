/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.gatling.setup.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.gatling.setup.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;


/**
 * 
 * Wrapper to use the HttpClient of Apache version 4.x
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RestConnection {
	private final Logger log = LoggerFactory.getLogger(RestConnection.class);
	private final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
	
	public static final String ACCEPT_LANGUAGE = "Accept-Language";
	public static final String COOKIE = "Cookie";
	public static final String CONTENT_LANGUAGE = "Content-Language";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String EXPIRES = "Expires";
	public static final String HEADER_ACCEPT = "Accept";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HOST = "Host";
	public static final String LOCATION = "Location";
	public static final String SET_COOKIE = "Set-Cookie";
	
	public static final String CONTEXT_PATH = "restapi";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
	
	private static final JsonFactory jsonFactory = new JsonFactory();
	private final ObjectMapper mapper = new ObjectMapper(jsonFactory);

	private final URL baseUrl;
	private final String context;
	private final String encoding;
	
	private final HttpClient httpclient;

	public RestConnection(URL baseUrl, String context, String username, String password, String encoding) {
		this(baseUrl, context, username, password, encoding, 300);
	}

	public RestConnection(URL baseUrl, String context, String username, String password, String encoding, int timeoutInSeconds) {
		this.context = (context == null ? "" : context);
		this.baseUrl = baseUrl;
		this.encoding = encoding;
		
		httpclient = HttpClient
				.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
	            .connectTimeout(Duration.ofSeconds(timeoutInSeconds))
	            .cookieHandler(new CookieManager())
				.authenticator(new BasicAuthenticator(username, password))
				.build();
	}
	
	public String getEncoding() {
		return encoding;
	}

	public boolean ping() {
		try {
			URL url = forgeURL("ping");
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(url.toURI())
					.version(HttpClient.Version.HTTP_1_1)
					.GET()
					.header(HEADER_ACCEPT, TEXT_PLAIN)
					.build();
			HttpResponse<InputStream> response = httpclient.send(httpRequest, BodyHandlers.ofInputStream());
			int returnCode = response.statusCode();
			ConnectionUtilities.consume(response);
			if(returnCode == 200) {
				return true;
			}
			log.error("Ping failed with status code: {}", returnCode);
			return false;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	public void logResponseError(HttpResponse<InputStream> response)
	throws IOException {
		int status = response.statusCode();
		if(status == 503) {
			String reason = ConnectionUtilities.toString(response);
			log.error("Server not found: {}", reason);
		} else {
			ConnectionUtilities.consume(response);
		}
	}
	
	public HttpResponse<InputStream> getAll(String request, NameValuePair... queryParameters)
			throws URISyntaxException, IOException, InterruptedException {
		return createGet(request, "*/*", queryParameters);
	}
	
	public HttpResponse<InputStream> getJson(String request, NameValuePair... queryParameters)
			throws URISyntaxException, IOException, InterruptedException {
		return createGet(request, RestConnection.APPLICATION_JSON, queryParameters);
	}

	public HttpResponse<InputStream> getXml(String request, NameValuePair... queryParameters)
			throws URISyntaxException, IOException, InterruptedException {
		return createGet(request, RestConnection.APPLICATION_XML, queryParameters);
	}

	public HttpResponse<InputStream> getPlainText(String request, NameValuePair... queryParameters)
			throws URISyntaxException, IOException, InterruptedException {
		return createGet(request, RestConnection.TEXT_PLAIN, queryParameters);
	}

	public HttpResponse<InputStream> createHead(String request, String mimeType, NameValuePair... queryParameters)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request, queryParameters);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.method("HEAD", BodyPublishers.noBody())
				.header(HEADER_ACCEPT, mimeType)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<InputStream> createGet(String request, String mimeType, NameValuePair... queryParameters)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request, queryParameters);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.GET()
				.header(HEADER_ACCEPT, mimeType)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<InputStream> put(String request)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.PUT(BodyPublishers.noBody())
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<InputStream> putRetry(String request)
	throws URISyntaxException, IOException, InterruptedException {
		HttpResponse<InputStream> response = null;
		try {
			response = put(request);
		} catch(IOException e) {
			ConnectionUtilities.consume(response);
			log.warn("Retry: {}", request);
			response = put(request);
		}
		return response;
	}

	public HttpResponse<InputStream> put(String request, NameValuePair... queryParameters)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request, queryParameters);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.PUT(BodyPublishers.noBody())
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<Void> putNoResponse(String request)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.PUT(BodyPublishers.noBody())
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.discarding());
	}

	public HttpResponse<Void> putNoResponseRetry(String request)
	throws URISyntaxException, IOException, InterruptedException {
		HttpResponse<Void> response = null;
		try {
			response = putNoResponse(request);
		} catch(IOException e) {
			log.warn("Retry: {}", request);
			pauseBeforeRetry();
			response = putNoResponse(request);
		}
		return response;
	}

	public HttpResponse<InputStream> put(String request, Object entity, NameValuePair... queryParameters)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request, queryParameters);
		String jsonEntity = stringuified(entity);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.PUT(BodyPublishers.ofString(jsonEntity, StandardCharsets.UTF_8))
				.header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<InputStream> putRetry(String request, Object entity, NameValuePair... queryParameters)
	throws URISyntaxException, IOException, InterruptedException {
		HttpResponse<InputStream> response = null;
		try {
			response = put(request, entity, queryParameters);
		} catch(IOException e) {
			ConnectionUtilities.consume(response);
			log.warn("Retry: {}", request);
			response = put(request, entity, queryParameters);
		}
		return response;
	}

	public HttpResponse<InputStream> post(String request)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.POST(BodyPublishers.noBody())
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<InputStream> postRetry(String request, Object entity)
	throws URISyntaxException, IOException, InterruptedException {
		HttpResponse<InputStream> response = null;
		try {
			response = post(request, entity);
		} catch(IOException e) {
			ConnectionUtilities.consume(response);
			log.warn("Retry: {}", request);
			response = post(request, entity);
		}
		return response;
	}

	public HttpResponse<InputStream> post(String request, Object entity)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request);
		String jsonEntity = stringuified(entity);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.POST(BodyPublishers.ofString(jsonEntity, StandardCharsets.UTF_8))
				.header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}
	
	/**
	 * Send a POST request with parameters encoded as form-urlencoded. And retry once if failed
	 * 
	 * @param request The request
	 * @param formParameters The parameters
	 * @return The response
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public HttpResponse<InputStream> postFormRetry(String request, List<NameValuePair> formParameters)
	throws URISyntaxException, IOException, InterruptedException {
		HttpResponse<InputStream> response = null;
		try {
			response = postForm(request, formParameters);
		} catch(IOException e) {
			ConnectionUtilities.consume(response);
			log.warn("Retry: {}", request);
			response = postForm(request, formParameters);
		}
		return response;
	}
	
	/**
	 * Send a POST request with parameters encoded as form-urlencoded.
	 * 
	 * @param request The request
	 * @param formParameters The parameters
	 * @return The response
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public HttpResponse<InputStream> postForm(String request, List<NameValuePair> formParameters)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request);
		String form = ConnectionUtilities.urlEncode(formParameters, StandardCharsets.UTF_8);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.header(HEADER_CONTENT_TYPE, APPLICATION_URL_ENCODED)
				.POST(BodyPublishers.ofString(form, StandardCharsets.UTF_8))
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<InputStream> post(String request, String filename, String mimeType, List<NameValuePair> nameValues, byte[] data)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request);
		String boundary = ConnectionUtilities.getBoundary();
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.header(HEADER_CONTENT_TYPE, "multipart/form-data;boundary=" + boundary)
				.POST(ConnectionUtilities.ofMimeMultipartData(filename, mimeType, nameValues, data, boundary))
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}

	public HttpResponse<InputStream> deleteRetry(String request)
	throws URISyntaxException, IOException, InterruptedException {
		HttpResponse<InputStream> response = null;
		try {
			response = delete(request);
		} catch(IOException e) {
			ConnectionUtilities.consume(response);
			log.warn("Retry: {}", request);
			response = delete(request);
		}
		return response;
	}

	public HttpResponse<InputStream> delete(String request)
	throws URISyntaxException, IOException, InterruptedException {
		URL url = forgeURL(request);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(url.toURI())
				.version(HttpClient.Version.HTTP_1_1)
				.DELETE()
				.header(HEADER_ACCEPT, APPLICATION_JSON)
				.build();
		return httpclient.send(httpRequest, BodyHandlers.ofInputStream());
	}
	
	public URL forgeURL(String request, NameValuePair... query)
	throws MalformedURLException {
		if(query != null && query.length > 0) {
			List<NameValuePair> parameters = new ArrayList<>(query.length);
			for(NameValuePair queryParam:query) {
				if(queryParam != null) {
					parameters.add(queryParam);
				}
			}
			String queryStr = ConnectionUtilities.urlEncode(parameters, StandardCharsets.UTF_8);
			return new URL(baseUrl, context + request + "?" + queryStr);
		}
		return new URL(baseUrl, context + request);
	}
	
	private void pauseBeforeRetry() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			//
		}
	}
	
	protected String stringuified(Object obj) {
		try {
			StringWriter w = new StringWriter();
			mapper.writeValue(w, obj);
			return w.toString();
		} catch (Exception e) {
			log.error("Cannot stringuifie " + obj, e);
			return null;
		}
	}
	
	protected byte[] stringuifiedBytes(Object obj) {
		try {
			StringWriter w = new StringWriter();
			mapper.writeValue(w, obj);
			return w.toString().getBytes(StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Cannot stringuifie " + obj, e);
			return null;
		}
	}
	
	public Date lastModified(HttpResponse<?> response) {
		String lastModifiedHeader = response.headers()
				.firstValue("Last-Modified")
				.orElse(null);
		if(StringHelper.containsNonWhitespace(lastModifiedHeader)) {
			try {
				return format.parse(lastModifiedHeader);
			} catch (ParseException e) {
				log.error("Cannot parse last modified date header: {}", lastModifiedHeader, e);
			}
		}
		return null;
	}

	public <U> U parse(HttpResponse<InputStream> response, Class<U> cl) {
		try(InputStream body = response.body()) {
			return mapper.readValue(body, cl);
		} catch (Exception e) {
			log.error("Cannot parse a response", e);
			return null;
		}
	}

	public <U> U parse(InputStream body, Class<U> cl) {
		try {
			return mapper.readValue(body, cl);
		} catch (Exception e) {
			log.error("Cannot parse a stream", e);
			return null;
		}
	}

	public <U> List<U> parseList(HttpResponse<InputStream> response, Class<U> cl) {
		CollectionType javaType = mapper.getTypeFactory()
			      .constructCollectionType(List.class, cl);
		try(InputStream in=response.body()) {
			return mapper.readValue(in, javaType);
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("restConnection[url=").append(baseUrl == null ? "NULL" : baseUrl.toString())
		  .append(";context=").append(context == null ? "NULL" : context);
		return sb.toString();
	}
}