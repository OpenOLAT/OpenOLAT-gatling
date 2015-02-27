/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.openolat.gatling.setup.builder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.openolat.gatling.setup.RestConnection;
import org.openolat.gatling.setup.voes.CatalogEntryVO;
import org.openolat.gatling.setup.voes.CourseVO;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  9 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CatalogUriBuilder {

	public final static int TYPE_NODE = 0;
	public final static int TYPE_LEAF = 1;
	
	private final RestConnection connection;
	
	public CatalogUriBuilder(RestConnection connection) {
		this.connection = connection;
	}
	
	public CatalogEntryVO getCatalogRoot()
	throws IOException, URISyntaxException {
		URI uri = getCatalogUri().build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if(code == 200) {
			InputStream body = response.getEntity().getContent();
			List<CatalogEntryVO> vos = connection.parseCatalogEntryArray(body);
			if(vos.size() == 1) {
				return vos.get(0);
			}
			return null;
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getCatalogRoot HTTP Error code: " + code);
		}
		return null;
	}

	public List<CatalogEntryVO> getCatalogList(CatalogEntryVO entry)
	throws IOException, URISyntaxException {
		URI uri = getCatalogEntryListUri(entry).build();
		HttpGet method = connection.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if(code == 200) {
			InputStream body = response.getEntity().getContent();
			return connection.parseCatalogEntryArray(body);
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("getCatalogList HTTP Error code: " + code);
		}
		return null;
	}
	
	public Catalog readCatalog(CatalogEntryVO entry)
	throws IOException, URISyntaxException  {
		Catalog catalog = new Catalog();
		if(entry != null) {
			readCatalog(entry, catalog);
		}
		return catalog;
	}
	
	private void readCatalog(CatalogEntryVO entry, Catalog catalog)
	throws IOException, URISyntaxException  {
		String entryName = entry.getName();
		if(entryName != null) {
			entryName = entryName.trim();
		}
		
		if(catalog.getCatalogMap().containsKey(entryName)) {
			System.out.println("Error duplicate catalog entry name: " + entryName);
		}
		catalog.getCatalogMap().put(entryName, entry);
		catalog.getKeyToCatalogEntryMap().put(entry.getKey(), entry);
		
		List<CatalogEntryVO> children = getCatalogList(entry);
		if(children == null || children.isEmpty()) {
			return;
		}
		
		for(CatalogEntryVO child:children) {
			catalog.getChildToParentMap().put(child.getKey(), entry.getKey());
			readCatalog(child, catalog);
		}
	}
	
	
	/**
	 * Add a catalog entry under the parentEntry
	 * @param parentEntry
	 * @param entryName
	 * @param entryDescription
	 * @return
	 * @throws java.io.IOException
	 */
	public CatalogEntryVO addCatalogEntry(CatalogEntryVO parentEntry, String entryName, String entryDescription)
	throws IOException, URISyntaxException {
		URI uri = getCatalogEntryUri(parentEntry)
				.queryParam("name", entryName)
				.queryParam("description", entryDescription)
				.queryParam("type", String.valueOf(TYPE_NODE))
				.build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if(code == 200 || code == 201) {
			InputStream body = response.getEntity().getContent();
			CatalogEntryVO vo = connection.parse(body, CatalogEntryVO.class);
			return vo;
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("addCatalogEntry HTTP Error code: " + code);
		}
		return null;
	}
	
	
	public CatalogEntryVO addCatalogEntry(CourseVO course, CatalogEntryVO entry)
	throws IOException, URISyntaxException {
		URI uri = getCatalogEntryUri(entry)
				.queryParam("name", course.getTitle())
				.queryParam("description", course.getTitle())
				.queryParam("type", String.valueOf(TYPE_LEAF))
				.queryParam("repoEntryKey", course.getRepoEntryKey().toString())
				.build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if(code == 200 || code == 201) {
			InputStream body = response.getEntity().getContent();
			CatalogEntryVO vo = connection.parse(body, CatalogEntryVO.class);
			return vo;
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("addCatalogEntry HTTP Error code: " + code);
		}
		return null;
	}
	
	/**
	 * Add a catalog entry under the parentEntry, for this course with specific name
	 * and description.
	 * @param course
	 * @param parentEntry
	 * @param entryName
	 * @param entryDescription
	 * @return
	 * @throws java.io.IOException
	 */
	public CatalogEntryVO addCatalogEntry(CourseVO course, CatalogEntryVO parentEntry, String entryName, String entryDescription)
	throws IOException, URISyntaxException {
		URI uri = getCatalogEntryUri(parentEntry)
				.queryParam("name", entryName)
				.queryParam("description", entryDescription)
				.queryParam("type", String.valueOf(TYPE_LEAF))
				.queryParam("repoEntryKey", course.getRepoEntryKey().toString())
				.build();
		HttpPut method = connection.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = connection.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if(code == 200 || code == 201) {
			InputStream body = response.getEntity().getContent();
			CatalogEntryVO vo = connection.parse(body, CatalogEntryVO.class);
			return vo;
		} else {
			EntityUtils.consume(response.getEntity());
			System.out.println("addCatalogEntry HTTP Error code: " + code);
		}
		return null;
	}
	
	/**
	 * 
	 * @return The URI to catalog of http://localhost:8080/olat/restapi/catalog
	 */
	public UriBuilder getCatalogUri() throws URISyntaxException {
		return connection.getContextURI().path("catalog");
	}
	
	/**
	 * 
	 * @return The URI to catalog of http://localhost:8080/olat/restapi/catalog/{entryKey}
	 */
	public UriBuilder getCatalogEntryUri(CatalogEntryVO entry) throws URISyntaxException {
		return connection.getContextURI().path("catalog").path(entry.getKey().toString());
	}
	
	/**
	 * 
	 * @return The URI to catalog of http://localhost:8080/olat/restapi/catalog/{entryKey}/children
	 */
	public UriBuilder getCatalogEntryListUri(CatalogEntryVO entry) throws URISyntaxException {
		return connection.getContextURI().path("catalog").path(entry.getKey().toString()).path("children");
	}
	
	public static class Catalog {
		private final Map<String,CatalogEntryVO> catalogMap = new HashMap<String,CatalogEntryVO>();
		private final Map<Long,Long> childToParentMap = new HashMap<Long,Long>();
		private final Map<Long,CatalogEntryVO> keyToCatalogEntryMap = new HashMap<Long,CatalogEntryVO>();

		public Map<String, CatalogEntryVO> getCatalogMap() {
			return catalogMap;
		}
		
		public Map<Long, CatalogEntryVO> getKeyToCatalogEntryMap() {
			return keyToCatalogEntryMap;
		}
		
		public Map<Long, Long> getChildToParentMap() {
			return childToParentMap;
		}

		public CatalogEntryVO getEntryByName(String name) {
			return catalogMap.get(name);
		}
		
		public CatalogEntryVO getEntryByKey(Long key) {
			return keyToCatalogEntryMap.get(key);
		}
		
		public int size() {
			return catalogMap.size();
		}
	}
}
