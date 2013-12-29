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
package net.sourceforge.subsonic.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.wrapper.SecurityContextHolderAwareRequestWrapper;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.dao.DataAccessException;

import net.sf.ehcache.Ehcache;
import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.dao.AccessRightDao;
import net.sourceforge.subsonic.dao.GroupDao;
import net.sourceforge.subsonic.dao.UserDao;
import net.sourceforge.subsonic.domain.AccessGroup;
import net.sourceforge.subsonic.domain.AccessToken;
import net.sourceforge.subsonic.domain.Group;
import net.sourceforge.subsonic.domain.InternetRadio;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.UserSettings;
import net.sourceforge.subsonic.util.FileUtil;


/**
 * Provides security-related services for authentication and authorization.
 *
 * @author Sindre Mehus
 */
public class SecurityService implements UserDetailsService {

    private static final Logger LOG = Logger.getLogger(SecurityService.class);

    private GroupDao groupDao;
    private AccessRightDao accessRightDao;
    private UserDao userDao;
    private SettingsService settingsService;
    private Ehcache userCache;

    /**
     * Locates the user based on the username.
     *
     * @param username The username presented to the {@link DaoAuthenticationProvider}
     * @return A fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no GrantedAuthority.
     * @throws DataAccessException       If user could not be found for a repository-specific reason.
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    	
        boolean unlocked = true;
        User user = getUserByName(username);
        
        if (user == null) {
            throw new UsernameNotFoundException("User \"" + username + "\" was not found.");
        }

        // block disabled user at logon
        if (username.equalsIgnoreCase("default") || user.isLocked()) {
        	unlocked = false;
        }

        String[] roles = userDao.getRolesForUser(username);
        GrantedAuthority[] authorities = new GrantedAuthority[roles.length];
        for (int i = 0; i < roles.length; i++) {
            authorities[i] = new GrantedAuthorityImpl("ROLE_" + roles[i].toUpperCase());
        }

        // If user is LDAP authenticated, disable user. The proper authentication should in that case
        // be done by SubsonicLdapBindAuthenticator.
        boolean enabled = !user.isLdapAuthenticated();

        return new org.acegisecurity.userdetails.User(username, user.getPassword(), enabled, true, true, unlocked, authorities);
    }

    /**
     * Returns the currently logged-in user for the given HTTP request.
     *
     * @param request The HTTP request.
     * @return The logged-in user, or <code>null</code>.
     */
    public User getCurrentUser(HttpServletRequest request) {
        String username = getCurrentUsername(request);
        return username == null ? null : userDao.getUserByName(username);
    }

    /**
     * Returns the name of the currently logged-in user.
     *
     * @param request The HTTP request.
     * @return The name of the logged-in user, or <code>null</code>.
     */
    public String getCurrentUsername(HttpServletRequest request) {
        return new SecurityContextHolderAwareRequestWrapper(request, null).getRemoteUser();
    }

    /**
     * Returns the user with the given username.
     *
     * @param username The username used when logging in.
     * @return The user, or <code>null</code> if not found.
     */
    public User getUserByName(String username) {
        return userDao.getUserByName(username);
    }

    /**
     * Returns the user with the given email address.
     *
     * @param email The email address.
     * @return The user, or <code>null</code> if not found.
     */
    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    /**
     * Returns the GroupId with the given email address.
     *
     * @param user.
     * @return if found integer or <code>null</code> if not found.
     */
	public int getCurrentUserGroupId(HttpServletRequest request) {
		String username = getCurrentUsername(request);
		return username == null ? null : userDao.getUserAccess(username);
	}
    
    public AccessGroup getAccessGroup(){
    	AccessGroup accessGroup = new AccessGroup();
    	List<Group> allGroups = groupDao.getAllGroups();
    	accessGroup = accessRightDao.getAllAccessToken(allGroups);
		return accessGroup;
    }
    
    public List<AccessToken> getAllAccessToken(){
    	AccessGroup accessGroup = new AccessGroup();
    	List<Group> allGroups = groupDao.getAllGroups();
    	accessGroup = accessRightDao.getAllAccessToken(allGroups);
		return accessGroup.getAccessToken();
    }
    
    public void updateAccessRight(int user_group_id, int music_folder_id, boolean isEnabled){
    	accessRightDao.updateAccessRight(user_group_id, music_folder_id, isEnabled);
    }
    
    /**
     * Returns all users.
     *
     * @return Possibly empty array of all users.
     */
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    /**
     * Returns all groups.
     *
     * @return Possibly empty array of all groups.
     */
    public List<Group> GetAllGroups(){
    	return groupDao.getAllGroups();
    }

    /**
     * Returns id from Group.
     *
     * @return Possibly empty array of GroupId.
     */
    public int GetIdfromGroup(Group group){
    	return groupDao.getIdAllGroups(group);
    }
    
    public void createGroup(Group group) {
        groupDao.createGroup(group);
        LOG.info("## Created Access Group: " + group.getName());
        
        group.setId(GetIdfromGroup(group));
        
        groupDao.insertGroupAccess(group.getId());
        LOG.info("## Created Default Access for Group: " + group.getName());
    }
    
    public int getUserGroupVideoDefault(int id){
        return groupDao.getUserGroupVideoDefault(id);
    }
    
    public void updateGroup(Group group) {
        groupDao.updateGroup(group);
        LOG.debug("## Updated Access Group: " + group.getName());
    }

	public void deleteGroup(Group group) {
        groupDao.deleteGroup(group);
        LOG.info("## Deleted Access Group: " + group.getName());
	}

	public void resetControl() {
		groupDao.resetGroup();	
	    LOG.info("## All Access Groups reseted!!!");
	}	
	
    /**
     * Returns whether the given user has administrative rights.
     */
    public boolean isAdmin(String username) {
        if (User.USERNAME_ADMIN.equals(username)) {
            return true;
        }
        User user = getUserByName(username);
        return user != null && user.isAdminRole();
    }

    /**
     * check/creates a default/guest user.
     */
    public void checkAccounts(){
    	
        User defaultUser = userDao.getUserByName(User.USERNAME_DEFAULT);
        User guestUser = userDao.getUserByName(User.USERNAME_GUEST);
        
        // check default User
        if (defaultUser == null){
	        userDao.createUser(new User(User.USERNAME_DEFAULT, RandomStringUtils.randomAlphanumeric(30), null, false, 0, 0, 0, 0, true));
	        defaultUser = userDao.getUserByName(User.USERNAME_DEFAULT);
	        defaultUser.setLdapAuthenticated(false);
	        defaultUser.setStreamRole(false);
	        userDao.updateUser(defaultUser);
	        userDao.updateUserSettings(settingsService.getUserSettings(User.USERNAME_DEFAULT));
	        LOG.debug("## Created user " + User.USERNAME_DEFAULT);
        } else {
	        UserSettings userSettings = userDao.getUserSettings(User.USERNAME_DEFAULT);
	        defaultUser.setLdapAuthenticated(false);
	        defaultUser.setStreamRole(false);
	        defaultUser.setLocked(true);
	        userDao.updateUser(defaultUser);
	        userDao.updateUserSettings(userSettings);
	        LOG.debug("## Updated user " + User.USERNAME_DEFAULT);
        }
        
        // check gueset User
        if (guestUser == null){
            userDao.createUser(new User(User.USERNAME_GUEST, RandomStringUtils.randomAlphanumeric(30), null, false, 0, 0, 0, 1, false));
	        guestUser = userDao.getUserByName(User.USERNAME_GUEST);
	        guestUser.setLdapAuthenticated(false);
	        guestUser.setStreamRole(true);
	        userDao.updateUser(guestUser);
	        userDao.updateUserSettings(settingsService.getUserSettings(User.USERNAME_GUEST));
	        LOG.debug("## Created user " + User.USERNAME_GUEST);
        } else {
	        UserSettings userSettings = userDao.getUserSettings(User.USERNAME_GUEST);
	        guestUser.setLdapAuthenticated(false);
	        guestUser.setStreamRole(true);
	        userDao.updateUserSettings(userSettings);
	        LOG.debug("## Updated user " + User.USERNAME_GUEST);
        }
    }
    
    /**
     * Creates a new user.
     *
     * @param user The user to create.
     */
    public void createUser(User user) {
        userDao.createUser(user);
        LOG.info("## Created user " + user.getUsername());
    }
    
    public void cloneUser(User user) {
        userDao.createUser(user);
        userDao.updateUserSettings(settingsService.getDefaultUserSettings(user.getUsername()));
        LOG.info("## Cloned from default user: " + user.getUsername());
    }    
    
    /**
     * Deletes the user with the given username.
     *
     * @param username The username.
     */
    public void deleteUser(String username) {
        userDao.deleteUser(username);
        LOG.info("## Deleted user " + username);
        userCache.remove(username);
    }

    /**
     * Updates the given user.
     *
     * @param user The user to update.
     */
    public void updateUser(User user) {
        userDao.updateUser(user);
        userCache.remove(user.getUsername());
    }

    /**
     * Updates the byte counts for given user.
     *
     * @param user                 The user to update, may be <code>null</code>.
     * @param bytesStreamedDelta   Increment bytes streamed count with this value.
     * @param bytesDownloadedDelta Increment bytes downloaded count with this value.
     * @param bytesUploadedDelta   Increment bytes uploaded count with this value.
     */
    public void updateUserByteCounts(User user, long bytesStreamedDelta, long bytesDownloadedDelta, long bytesUploadedDelta) {
        if (user == null) {
            return;
        }

        user.setBytesStreamed(user.getBytesStreamed() + bytesStreamedDelta);
        user.setBytesDownloaded(user.getBytesDownloaded() + bytesDownloadedDelta);
        user.setBytesUploaded(user.getBytesUploaded() + bytesUploadedDelta);

        userDao.updateUser(user);
    }
    
    public boolean isAccessAllowed(File file, int user_group_id){
        return isInMusicFolder(file, user_group_id) || isInPodcastFolder(file);    	
    	//TODO:isAccessAllowed
    }

    public boolean isReadAllowed(File file) {
        // Allowed to read from both music folder and podcast folder.
        return isInMusicFolder(file) || isInPodcastFolder(file);
    }    
    
    /**
     * Returns whether the given file may be read.
     *
     * @return Whether the given file may be read.
     */
    public boolean isReadAllowed(File file, int user_group_id) {
        // Allowed to read from both music folder and podcast folder.
        return isInMusicFolder(file, user_group_id) || isInPodcastFolder(file);
    }

    /**
     * Returns whether the given file may be written, created or deleted.
     *
     * @return Whether the given file may be written, created or deleted.
     */
    public boolean isWriteAllowed(File file) {
        // Only allowed to write podcasts, artist art or cover art.
        boolean isPodcast = isInPodcastFolder(file);
        boolean isCoverArt = isInMusicFolder(file) && file.getName().startsWith("cover.");
        boolean isArtistArt = isInMusicFolder(file) && file.getName().startsWith("artist.");
        
        return isPodcast || isCoverArt || isArtistArt;
    }

    /**
     * Returns whether the given file may be uploaded.
     *
     * @return Whether the given file may be uploaded.
     */
    public boolean isUploadAllowed(File file) {
        return isInUploadFolder(file) || isInMusicFolder(file) && !FileUtil.exists(file);
    }
    
    /**
     * Returns whether the given file is located in the upload folders.
     *
     * @param file The file in question.
     * @return Whether the given file is located in the upload folders.
     */
    private boolean isInUploadFolder(File file) {
        if (isFileInFolder(file.getPath(), settingsService.getUploadFolder())) {
            return true;
        }
    return false;
    }

	/**
     * Returns whether the given file is located in one of the music folders (or any of their sub-folders).
     *
     * @param file The file in question.
     * @return Whether the given file is located in one of the music folders.
     */
    private boolean isInMusicFolder(File file, int user_group_id) {
        return getMusicFolderForFile(file, user_group_id ) != null;
    }
    
    private MusicFolder getMusicFolderForFile(File file, int user_group_id) {
        List<MusicFolder> folders = settingsService.getAllMusicFolders(false, true, user_group_id);
        String path = file.getPath();
        for (MusicFolder folder : folders) {
            if (isFileInFolder(path, folder.getPath().getPath())) {
                return folder;
            }
        }
        return null;
    }

    private boolean isInMusicFolder(File file) {
        return getMusicFolderForFile(file) != null;
    }
    
    private MusicFolder getMusicFolderForFile(File file) {
    	if (file == null) {
    		return null;
    	}
    	
        List<MusicFolder> folders = settingsService.getAllMusicFolders(false, true);
        String path = file.getPath();
        for (MusicFolder folder : folders) {
            if (isFileInFolder(path, folder.getPath().getPath())) {
                return folder;
            }
        }
        return null;
    }
    
    /**
     * Returns whether the given file is located in the Podcast folder (or any of its sub-folders).
     *
     * @param file The file in question.
     * @return Whether the given file is located in the Podcast folder.
     */
    private boolean isInPodcastFolder(File file) {
    	if (file == null) {
    		return false;
    	}
    	
        String podcastFolder = settingsService.getPodcastFolder();
        return isFileInFolder(file.getPath(), podcastFolder);
    }

    public String getRootFolderForFile(File file) {
        MusicFolder folder = getMusicFolderForFile(file, 0);
        if (folder != null) {
            return folder.getPath().getPath();
        }

        if (isInPodcastFolder(file)) {
            return settingsService.getPodcastFolder();
        }
        return null;
    }

    /**
     * Returns whether the given file is located in the given folder (or any of its sub-folders).
     * If the given file contains the expression ".." (indicating a reference to the parent directory),
     * this method will return <code>false</code>.
     *
     * @param file   The file in question.
     * @param folder The folder in question.
     * @return Whether the given file is located in the given folder.
     */
    protected boolean isFileInFolder(String file, String folder) {
        // Deny access if file contains ".." surrounded by slashes (or end of line).
        if (file.matches(".*(/|\\\\)\\.\\.(/|\\\\|$).*")) {
            return false;
        }

        // Convert slashes.
        file = file.replace('\\', '/');
        folder = folder.replace('\\', '/');

        return file.toUpperCase().startsWith(folder.toUpperCase());
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

	public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

	public void setAccessRightDao(AccessRightDao accessRightDao) {
        this.accessRightDao = accessRightDao;
    }
	
    public void setUserCache(Ehcache userCache) {
        this.userCache = userCache;
    }
}