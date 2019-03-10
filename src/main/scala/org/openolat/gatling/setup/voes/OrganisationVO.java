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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.openolat.gatling.setup.voes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "organisationVO")
public class OrganisationVO {

	private Long key;
	
	private String identifier;
	private String displayName;
	private String description;
	private String cssClass;
	private String status;
	
	private String externalId;
	private String managedFlagsString;
	
	private Long rootOrganisationKey;
	private Long parentOrganisationKey;

	private Long organisationTypeKey;
	
	public OrganisationVO() {
		//
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	public Long getRootOrganisationKey() {
		return rootOrganisationKey;
	}

	public void setRootOrganisationKey(Long rootOrganisationKey) {
		this.rootOrganisationKey = rootOrganisationKey;
	}

	public Long getParentOrganisationKey() {
		return parentOrganisationKey;
	}

	public void setParentOrganisationKey(Long parentOrganisationKey) {
		this.parentOrganisationKey = parentOrganisationKey;
	}

	public Long getOrganisationTypeKey() {
		return organisationTypeKey;
	}

	public void setOrganisationTypeKey(Long organisationTypeKey) {
		this.organisationTypeKey = organisationTypeKey;
	}
}
