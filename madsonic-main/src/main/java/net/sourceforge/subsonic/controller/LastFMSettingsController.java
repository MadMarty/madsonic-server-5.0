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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.subsonic.dao.ArtistDao;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.service.LastFMService;
import net.sourceforge.subsonic.service.MediaScannerService;
import net.sourceforge.subsonic.service.SecurityService;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Madevil
 */
public class LastFMSettingsController extends ParameterizableViewController {

    private ArtistDao artistDao;

    private LastFMService lastFMService;
    private SecurityService securityService;
    private MediaScannerService mediaScannerService;
   
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        if (request.getParameter("ScanNow") != null) {
        	map.put("warn", true);
			map.put("warnInfo", "Artist-Coverscan <br>This take some time!");	
            List<Artist> allArtists = artistDao.getAllArtists();
            lastFMService.getArtistImages(allArtists);
			map.put("done", true);
            }		
        
        if (request.getParameter("ScanInfo") != null) {
			map.put("warn", true);
			map.put("warnInfo", "Artist-Infoscan <br>This take some time!");	        	
            List<Artist> allArtists = artistDao.getGroupedAlbumArtists();
            lastFMService.getArtistInfo(allArtists);
			map.put("done", true);
            }	        

        if (request.getParameter("ScanNewInfo") != null) {
			map.put("warn", true);
			map.put("warnInfo", "Artist-Infoscan <br>This take some time!");	        	
            List<Artist> allArtists = artistDao.getNewGroupedAlbumArtists();
            lastFMService.getArtistInfo(allArtists);
			map.put("done", true);
            }	        

        if (request.getParameter("CleanupArtist") != null) {
            lastFMService.CleanupArtist();
			map.put("done", true);
            }	        

        if (request.getParameter("ScanBio") != null) {
            List<Artist> allArtists = artistDao.getGroupedAlbumArtists();
            lastFMService.getArtistBio(allArtists);
    		map.put("done", true);
            }        
			map.put("scanning", mediaScannerService.isScanning());	
			
        ModelAndView result = super.handleRequestInternal(request, response);
        
        result.addObject("model", map);
        return result;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }    
 
     public void setMediaScannerService(MediaScannerService mediaScannerService) {
        this.mediaScannerService = mediaScannerService;
    }
     
     public void setlastFMService(LastFMService lastFMService) {
         this.lastFMService = lastFMService;
     }
     
     public void setArtistDao(ArtistDao artistDao) {
         this.artistDao = artistDao;
     }
}
