/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.subsonic.domain.AccessRight;

/**
 * Represents a Security Group.
 *
 */
public class AccessToken {

    private Integer id;
    private String name;
  
    private ArrayList<AccessRight> AccessRight = new ArrayList<AccessRight>();
    private List<AccessRight> accessRights = new LinkedList<AccessRight>(); 

    public AccessToken(Integer id, String name, ArrayList<AccessRight> accessRight) {
        this.id = id;
        this.name = name;
        this.AccessRight = accessRight;
    }

	public AccessToken() {
	}

	public Integer getId() {
        return id;
    }	
	
	public Integer getUserGroupId() {
        return id;
    }

	public void setUserGroupId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}	
	
	public String getUserGroupName() {
		return name;
	}

	public void setUserGroupName(String name) {
		this.name = name;
	}    
	
	public List<AccessRight> getAccessRight() {
		return AccessRight;
	}
  
    public void addAccessRight(AccessRight accessRight) {
    	AccessRight.add(accessRight);
    }

    public void addAccessRight2(AccessRight accessRight) {
    	accessRights.add(accessRight);
    }    
    
	/**
	 * @return the accessRights
	 */
	public List<AccessRight> getAccessRights() {
		return accessRights;
	}

	/**
	 * @param accessRights the accessRights to set
	 */
	public void setAccessRights(List<AccessRight> accessRights) {
		this.accessRights = accessRights;
	}
}