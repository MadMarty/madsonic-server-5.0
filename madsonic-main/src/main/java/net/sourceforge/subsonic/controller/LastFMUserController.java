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
package net.sourceforge.subsonic.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.subsonic.lastfm.*;

import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;

import net.sourceforge.subsonic.domain.Album;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.UserSettings;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * Controller for the genre radio.
 *
 */
public class LastFMUserController extends ParameterizableViewController {

    private SettingsService settingsService;
    private SecurityService securityService;    
    private MediaFileService mediaFileService;
    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

        User user = securityService.getCurrentUser(request);
        UserSettings userSettings = settingsService.getUserSettings(user.getUsername());
        
        String lastfmUsername = userSettings.getLastFmUsername();
        String api_key = "8b396e869b58f63e65d352d1a71874f2";
      
//      securityService.getCurrentUserGroupId(request);

	    Collection<net.sourceforge.subsonic.domain.Artist> artists2 = new ArrayList<net.sourceforge.subsonic.domain.Artist>(); 

        if(lastfmUsername.length() > 1) {

        // tag --------------------------------------
	    Collection<Tag> tags = net.sourceforge.subsonic.lastfm.User.getTopTags(lastfmUsername, 10, api_key);        
        List<String> userTopTag = new ArrayList<String>();
	    for (Tag t : tags) {
	    	userTopTag.add(t.getName() );
	    }	
        map.put("topTags", userTopTag);
        
        // Artist --------------------------------------
	    Collection<net.sourceforge.subsonic.lastfm.Artist> artists = net.sourceforge.subsonic.lastfm.User.getTopArtists(lastfmUsername, Period.THREE_MONTHS, api_key);
	    
	    for (net.sourceforge.subsonic.lastfm.Artist artist : artists) {

	    	net.sourceforge.subsonic.domain.Artist x = new net.sourceforge.subsonic.domain.Artist();
	        x.setName(artist.getName());	    	
	    	
		    try {
			    Integer ArtistID = mediaFileService.getIDfromArtistname(artist.getName());
		    	if (ArtistID != null) {x.setId(ArtistID);}		    
	        } catch (Throwable ex) {
	        } 
	        
	        artists2.add(x);
    	}
		map.put("topArtists", artists2);
        
        
        // ALBUM --------------------------------------
		Collection<net.sourceforge.subsonic.domain.Album> albumsX = new ArrayList<net.sourceforge.subsonic.domain.Album>();
	    Collection<net.sourceforge.subsonic.lastfm.Album> albums = net.sourceforge.subsonic.lastfm.User.getTopAlbums(lastfmUsername, Period.THREE_MONTHS, api_key);        

	    for (net.sourceforge.subsonic.lastfm.Album album : albums) {

		    Album ali = new net.sourceforge.subsonic.domain.Album();
		    ali.setArtist(album.getArtist());
		    ali.setName(album.getName());

		    try {
			    Integer AlbumID = mediaFileService.getIdsForAlbums(album.getArtist(), album.getName());
		    	if (AlbumID != null) {ali.setMediaFileId(AlbumID);}		    
	        } catch (Throwable x) {
	        } 
		    try {
			    Integer ArtistID = mediaFileService.getIDfromArtistname(album.getArtist());
		    	if (ArtistID != null) {ali.setId(ArtistID);}		    
	        } catch (Throwable x) {
	        } 		    
		    albumsX.add(ali);
	    }
        map.put("topAlbums", albumsX);
        
//        // TRACK Chart --------------------------------------
//	    Chart<Track> track = net.sourceforge.subsonic.lastfm.User.getWeeklyTrackChart(lastfmUsername, 5, api_key);        
//        List<String> userChartTracks = new ArrayList<String>();
//        Collection<Track> tracks = track.getEntries();
//	    for (Track t : tracks) {
//	    	userChartTracks.add(t.getArtist() + " - " + t.getName());
//	    }	    
//        map.put("chartTracks", userChartTracks);
	    
	    
        // --- Loved Tracks --------------------------------------
        List<String> userLovedTracks = new ArrayList<String>();
        Collection<MediaFile> Tracks = new ArrayList<MediaFile>();
        
	    PaginatedResult<Track> loved = net.sourceforge.subsonic.lastfm.User.getLovedTracks(lastfmUsername, 0, api_key);
		    for (Track t : loved) { 
		    	userLovedTracks.add(t.getArtist() + " - " + t.getName());
		    
		    MediaFile f = new MediaFile();
		    
		    f.setArtist(t.getArtist());
		    f.setTitle(t.getName());
		    
		    
		    f.setTrackNumber(0);
//		    try {
//			    Integer TrackID = mediaFileService.getIdForTrack(t.getArtist(), t.getName());
//		    	if (TrackID != null) {f.setTrackNumber(TrackID);}		    
//	        } catch (Throwable x) {
//	        } 
		    
		    try {
			    Integer ArtistID = mediaFileService.getIDfromArtistname(t.getArtist());
		    	if (ArtistID != null) {f.setId(ArtistID);}		    
	        } catch (Throwable x) {
	        } 
		    
	        Tracks.add(f);
    	}	    
	    
        map.put("lovedTracks", Tracks);
        // ---
        
        }
        
        map.put("customScrollbar", userSettings.isCustomScrollbarEnabled()); 		

        ModelAndView result = super.handleRequestInternal(request, response);
        result.addObject("model", map);

        return result;
    }
    
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
    
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }	
}