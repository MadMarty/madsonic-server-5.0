/*
 This file is part of Madsonic.

 Madsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Madsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License		
 along with Madsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2013 (C) Madevil
 */
package net.sourceforge.subsonic.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.domain.AccessGroup;
import net.sourceforge.subsonic.domain.AccessRight;
import net.sourceforge.subsonic.domain.AccessToken;
import net.sourceforge.subsonic.domain.Group;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * Provides database services for Access Control.
 *
 * @author Madevil
 */
public class AccessRightDao extends AbstractDao {

	private static final String COLUMNS = "user_group_access.music_folder_id, music_folder.enabled, user_group_access.enabled";	
    private final RowMapper rowMapper = new AccessRightMapper();
    private static final Logger LOG = Logger.getLogger(AccessRightDao.class);
    
    public AccessGroup getAllAccessToken(List<Group> allGroup){
    	
    	AccessGroup AG = new AccessGroup();
    	
    	for (Group group : allGroup) {
    		AG.addAccessToken(getAccessTokenByGroup(group));
    	}
		return AG;
    }
    
	public AccessToken getAccessTokenByGroup(Group userGroup) {
		
		AccessToken AT = new AccessToken();
		
	    List<AccessRight> ARList = query("select " + COLUMNS + " from user_group_access, music_folder " + 
	    "where user_group_access.music_folder_id=music_folder.id and user_group_access.user_group_id=? ", rowMapper, userGroup.getId());
	    
	    if (ARList.isEmpty()) {
	        return null;
	    }		
	    for (AccessRight AR : ARList) {
	    //	if (AR.isEnabled() == true) {
		    	AT.addAccessRight(AR);
		    	AT.addAccessRight2(AR);
	    //	}
	    }
	    AT.setUserGroupId(userGroup.getId());
	    AT.setUserGroupName(userGroup.getName());
	    
		return AT;
	}		
	
	public void updateAccessRight(int user_group_id, int music_folder_id, boolean isEnabled) {
        String sql = "update user_group_access set Enabled=? where user_group_id=? and music_folder_id=?";
        update(sql, isEnabled, user_group_id, music_folder_id);
	}
	
	
    private static class AccessRightMapper implements ParameterizedRowMapper<AccessRight> {
        public AccessRight mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AccessRight(	rs.getInt(1), rs.getBoolean(2), rs.getBoolean(3));
        }
    }	
	
}
