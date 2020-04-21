/**
 * <a href="http://www.openolat.org">
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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.openolat.gatling.setup;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.openolat.gatling.setup.voes.CatalogEntryVO;
import org.openolat.gatling.setup.voes.CourseVO;
import org.openolat.gatling.setup.voes.GroupVO;
import org.openolat.gatling.setup.voes.OrganisationVO;
import org.openolat.gatling.setup.voes.UserVO;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Description:<br>
 * Manage a connection to the grizzly server used by the unit test
 * with some helpers methods.
 * 
 * <P>
 * Initial Date:  20 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RestConnection implements Closeable {
	
	private final int port;
	private final String host;
	private final String protocol;
	private final String contextPath;
	
	private final String username;
	private final String password;

	private final JsonFactory jsonFactory = new JsonFactory();
	private final CloseableHttpClient httpClient;

	
	public RestConnection(URL url, String username, String password) {
		host = url.getHost();
		port = url.getPort();
		protocol = url.getProtocol();
		contextPath = url.getPath();

		this.username = username;
		this.password = password;

		final CookieStore cookieStore = new BasicCookieStore();
		final BasicCredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(username, password));
		final HttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

		httpClient = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(provider)
				.setConnectionManager(connManager)
				.setMaxConnPerRoute(4)
				.build();
	}

	public void close() throws IOException {
		httpClient.close();
	}

	public boolean login()
	throws IOException, URISyntaxException {
		URI uri = getContextURI().path("auth").path(username).queryParam("password", password).build();
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = httpClient.execute(httpget);
		
    	HttpEntity entity = response.getEntity();
    	int code = response.getStatusLine().getStatusCode();
    	EntityUtils.consume(entity);
    	return code == 200;
	}
	
	public <T> T get(URI uri, Class<T> cl) throws IOException, URISyntaxException {
		HttpGet get = createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = execute(get);
		if(200 == response.getStatusLine().getStatusCode()) {
			HttpEntity entity = response.getEntity();
			return parse(entity.getContent(), cl);
		}
		EntityUtils.consumeQuietly(response.getEntity());
		System.err.println("get return: " + response.getStatusLine().getStatusCode());
		return null;
	}
	
	public void addEntity(HttpEntityEnclosingRequestBase put, NameValuePair... pairs)
	throws UnsupportedEncodingException {
		if(pairs == null || pairs.length == 0) return;
		
		List<NameValuePair> pairList = Arrays.asList(pairs);
		HttpEntity myEntity = new UrlEncodedFormEntity(pairList, "UTF-8");
		put.setEntity(myEntity);
	}
	
	/**
	 * Add an object (application/json)
	 * @param put The request
	 * @param obj The object to transmit
	 * @throws java.io.UnsupportedEncodingException
	 */
	public void addJsonEntity(HttpEntityEnclosingRequestBase put, Object obj)
	throws UnsupportedEncodingException {
		if(obj == null) return;
		
		String jsonObject = toJsonString(obj);
		HttpEntity myEntity = new StringEntity(jsonObject, ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), "UTF-8"));
		put.setEntity(myEntity);
	}
	
	public void addMultipart(HttpEntityEnclosingRequestBase post, String filename, File file)
	throws UnsupportedEncodingException {
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("filename", filename)
				.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, filename).build();
		post.setEntity(entity);
	}
	
	public HttpPut createPut(URI uri, String accept) {
		HttpPut put = new HttpPut(uri);
		decorateHttpMessage(put, accept, "en");
		return put;
	}
	
	public HttpPut createPut(URI uri, String accept, String language) {
		HttpPut put = new HttpPut(uri);
		decorateHttpMessage(put,accept, language);
		return put;
	}
	
	public HttpGet createGet(URI uri, String accept) {
		HttpGet get = new HttpGet(uri);
		decorateHttpMessage(get,accept, "en");
		return get;
	}

	public HttpPost createPost(URI uri, String accept) {
		HttpPost get = new HttpPost(uri);
		decorateHttpMessage(get,accept, "en");
		return get;
	}
	
	public HttpDelete createDelete(URI uri, String accept) {
		HttpDelete del = new HttpDelete(uri);
		decorateHttpMessage(del, accept, "en");
		return del;
	}
	
	private void decorateHttpMessage(HttpMessage msg, String accept, String language) {
		if(accept != null && accept.length() > 0) {
			msg.addHeader("Accept", accept);
		}
		if(language != null && language.length() > 0)  {
			msg.addHeader("Accept-Language", language);
		}
	}
	
	public HttpResponse execute(HttpUriRequest request)
	throws IOException, URISyntaxException {
		return httpClient.execute(request);
	}
	
	/**
	 * @return http://localhost:9998
	 */
	public UriBuilder getBaseURI() throws URISyntaxException  {
		URI uri = new URI(protocol, null, host, port, null, null, null);
		return UriBuilder.fromUri(uri);
	}
	
	/**
	 * @return http://localhost:9998/olat/restapi
	 */
	public UriBuilder getContextURI()  throws URISyntaxException {
		return getBaseURI().path(contextPath).path("restapi");
	}
	
	public String toJsonString(Object obj) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			StringWriter w = new StringWriter();
			mapper.writeValue(w, obj);
			return w.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public <U> U parse(HttpResponse response, Class<U> cl) {
		try(InputStream body = response.getEntity().getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, cl);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public <U> U parse(InputStream body, Class<U> cl) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, cl);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public final List<UserVO> parseUserArray(HttpResponse response) {
		try(InputStream body = response.getEntity().getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){ /* */ });
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parse a list of groups
	 * @param body The body of the response stream
	 * @return A list of groups
	 */
	public final List<GroupVO> parseGroupArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, new TypeReference<List<GroupVO>>(){ /* */ });
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Parse an array of catalog entries
	 * @param body The body of the response
	 * @return A list of catalog entries
	 */
	public final List<CatalogEntryVO> parseCatalogEntryArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, new TypeReference<List<CatalogEntryVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public final List<CourseVO> parseCourseArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, new TypeReference<List<CourseVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public final List<OrganisationVO> parseOrganisationArray(HttpEntity entity) {
		try(InputStream body = entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, new TypeReference<List<OrganisationVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}