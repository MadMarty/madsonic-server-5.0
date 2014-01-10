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

 Copyright 2014 (C) Madevil
 */
package net.sourceforge.subsonic.controller;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.service.PlaylistService;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.LastFMArtist;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.UserSettings;
import net.sourceforge.subsonic.service.LastFMService;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;

import org.jfree.util.Log;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for showing a user's starred items.
 *
 * @author Sindre Mehus
 */
public class WelcomeController extends ParameterizableViewController {

    private static final Logger LOG = Logger.getLogger(WelcomeController.class);
	
    private PlayerService playerService;
    private MediaFileDao mediaFileDao;
    private SecurityService securityService;
    private SettingsService settingsService;
    private MediaFileService mediaFileService;
    private PlaylistService playlistService;
    private LastFMService lastFMService;
    
    private static final String DEFAULT_LIST_TYPE = "albums";

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        User user = securityService.getCurrentUser(request);
        String username = user.getUsername();
        UserSettings userSettings = settingsService.getUserSettings(username);
        int userGroupId = securityService.getCurrentUserGroupId(request);
        
	    String listType = DEFAULT_LIST_TYPE;  
	    
	    List<MediaFile> artists = null;
	    List<MediaFile> albums = null;
	    List<MediaFile> songs = null;
	    List<MediaFile> topPlayedSongs = null;
	    List<MediaFile> lastPlayedSongs = null;
	    
		if (request.getParameter("listType") != null) {
            listType = String.valueOf(request.getParameter("listType"));
        }

	    if (listType.equalsIgnoreCase("artists")){
	        artists = mediaFileDao.getStarredLastArtists(0, 30, userGroupId);
	        
	        for (MediaFile mediaFile : artists) {
	        	LastFMArtist lastFMArtist = new LastFMArtist();
	        	
	        try	{
	        	String requestedArtist = mediaFile.getArtist();
		        lastFMArtist = lastFMService.getArtist(requestedArtist);
		        
		        if ((lastFMArtist == null))  {
		        	
		        	if (requestedArtist.toString().length() > 1) { 
		        	
		        	LOG.debug("#Try search for Data: " + requestedArtist);
		        	
		            List<Artist> artist = new ArrayList<Artist>();
		            Artist a = new Artist();
		            a.setName(requestedArtist);
		            a.setArtistFolder(requestedArtist);
		            artist.add(a);
					lastFMService.getArtistInfo(artist);
		        	lastFMArtist = lastFMService.getArtist(requestedArtist);
		        	
			        if ((lastFMArtist != null)){
			        	LOG.debug("## Data found for: " + requestedArtist);
			        	
			        } else {
			        	LOG.error("## No Data found for: " + requestedArtist);
			        }
	         	  } 
		        }
	        }  catch (SecurityException x) {
	            // xxx
	        }
	     	mediaFile.setComment(lastFMArtist.getSummary());
	        }

	        
	    mediaFileService.populateStarredDate(artists, username);
	        
	    };  
        
	    if (listType.equalsIgnoreCase("albums")){
	        albums = mediaFileDao.getStarredLastAlbums(0, 30, userGroupId);
	        mediaFileService.populateStarredDate(albums, username);
	    };  
        
	    if (listType.equalsIgnoreCase("songs")){
	        songs = mediaFileDao.getStarredLastFiles(0, 30, userGroupId);
	        mediaFileService.populateStarredDate(songs, username);
	        getParent(songs);
	    };  

	    if (listType.equalsIgnoreCase("topplayed")){
	        topPlayedSongs = mediaFileDao.getTopPlayedCountForAllUser(0, 20, userGroupId);
	        mediaFileService.populateStarredDate(topPlayedSongs, username);
	        getParent(topPlayedSongs);
	    };  

	    if (listType.equalsIgnoreCase("lastplayed")){
	        lastPlayedSongs = mediaFileDao.getLastPlayedCountForUser(0, 20, username);
	        mediaFileService.populateStarredDate(lastPlayedSongs, username);
	        getParent(lastPlayedSongs);
	    };  
        
        map.put("listType", listType);
        map.put("user", user);
        
        map.put("partyModeEnabled", userSettings.isPartyModeEnabled());
        map.put("customScrollbar", userSettings.isCustomScrollbarEnabled()); 		
        map.put("player", playerService.getPlayer(request, response));
        
        map.put("artists", artists);
        map.put("albums", albums);
        map.put("songs", songs);
        
        //TODO:!!
        map.put("lastPlayedSongs", lastPlayedSongs);
        map.put("topPlayedSongs", topPlayedSongs); 
        
        ModelAndView result = super.handleRequestInternal(request, response);
        result.addObject("model", map);
        return result;
    }

    private void getParent(List<MediaFile> colection){
        for (MediaFile mediaFile : colection) {
		try {
				int id = mediaFileDao.getIDfromArtistname(mediaFile.getArtist());
				mediaFile.setArtistPath(mediaFileService.getMediaFile(id).getPath());
				} catch (Exception x) {
					Log.error("## no Path found for MediaFileid: " + mediaFile.getId());
				}
        }
    }
    public void setLastFMService(LastFMService lastFMService) {
        this.lastFMService = lastFMService;
    }
    
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setMediaFileDao(MediaFileDao mediaFileDao) {
        this.mediaFileDao = mediaFileDao;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }	
	
    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }
}
