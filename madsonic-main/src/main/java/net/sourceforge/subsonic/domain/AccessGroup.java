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
import java.util.List;

/**
 * Represents a Security Group.
 *
 */
public class AccessGroup {

    private List<AccessToken> AccessToken = new ArrayList<AccessToken>();
    
    public AccessGroup() {
    }

	/**
	 * @return the accessToken
	 */
	public List<AccessToken> getAccessToken() {
		return AccessToken;
	}

	/**
	 * @param aT the accessToken to set
	 */
	public void addAccessToken(AccessToken accessToken) {
		AccessToken.add(accessToken);
	}

}