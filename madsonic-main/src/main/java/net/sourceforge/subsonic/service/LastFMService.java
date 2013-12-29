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
 package net.sourceforge.subsonic.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.dao.LastFMArtistDao;
import net.sourceforge.subsonic.dao.LastFMArtistSimilarDao;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.LastFMArtist;
import net.sourceforge.subsonic.domain.LastFMArtistSimilar;
import net.sourceforge.subsonic.domain.MediaFile; 
import net.sourceforge.subsonic.lastfm.Album;
import net.sourceforge.subsonic.lastfm.Image;
import net.sourceforge.subsonic.lastfm.ImageSize;
import net.sourceforge.subsonic.lastfm.PaginatedResult;
import net.sourceforge.subsonic.util.StringUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.jfree.util.Log;

public class LastFMService {
	
    private static final Logger LOG = Logger.getLogger(LastFMService.class);
	
    private SecurityService securityService;
    private SettingsService settingsService;
    private MediaFileService mediaFileService;	
    private LastFMArtistDao lastFMArtistDao;
    
    // Use here your own Last.FM API key
    private static String api_key = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    
    private static LastFMArtistSimilarDao lastFMArtistSimilarDao;   
    
    public LastFMArtist getArtist(String artistname) {
    	
try {
	 	if (artistname != null || artistname.length() > 0 ){
			String tmpArtistName = net.sourceforge.subsonic.lastfm.Artist.getCorrection(artistname, api_key).getName();
			if (artistname != tmpArtistName) {
				LOG.debug("## ArtistAutoCorrect: " + artistname + " -> " + tmpArtistName);
			}
	    	return lastFMArtistDao.getArtist(tmpArtistName);
	 	}
    } catch (Exception x) {
    	// Ignore Exception
    	return null;
    }
	return null;
    }
    
    public List<String> getSimilarArtist(String ArtistName){
    	return lastFMArtistSimilarDao.getSimilarArtist(ArtistName);
    }
    
    public void CleanupArtist(){
    	lastFMArtistDao.CleanupArtist();
    }
    
	public void getArtistImages(List<Artist> artistList) {
	   	
		LOG.info("## ArtistCount: " + artistList.size());
	 	MediaFile mediaFileArtist;
	 	
	 	for (Artist artist : artistList) {
	 		try{
	 			int id = mediaFileService.getIDfromArtistname(artist.getName()) == null ? -1 : mediaFileService.getIDfromArtistname(artist.getName());
			 	mediaFileArtist = mediaFileService.getMediaFile(id);
			 	
			 	if (mediaFileArtist == null){
			       continue;
			 	}
			 	LOG.debug("## Scan for Artist: " + artist.getName() );
		 		getArtistImage(mediaFileArtist, api_key);

		        } catch (NullPointerException ex) {
			        System.out.println("## ERROR: " + artist.getName());
		        }
	 	}
		LOG.info("## LastFM Scan Finished");
	}
    
	public void getArtistBio(List<Artist> artistList) {
	   	
		LOG.info("## ArtistCount: " + artistList.size());
	 	
	 	MediaFile mediaFileArtist;
	 	
	 	//LastFMArtist 
	 	for (Artist artist : artistList) {
	 		try{
	 			int id = mediaFileService.getIDfromArtistname(artist.getName()) == null ? -1 : mediaFileService.getIDfromArtistname(artist.getName());
			 	mediaFileArtist = mediaFileService.getMediaFile(id);
			 	
			 	if (mediaFileArtist == null){
			       continue;
			 	}
			 	LOG.debug("## Scan for ArtistBio: " + artist.getName() );
			 	
//		 		getArtistBio(mediaFileArtist, api_key);
//		 		mediaFileService.createOrUpdateMediaFile(mediaFileArtist);

		        } catch (NullPointerException ex) {
			        System.out.println("## ERROR: " + artist.getName());
		        }
	 	}
		LOG.info("## LastFM Scan Finished");
	}	
	
    public String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xB) ||                
                (current == 0xD) ||
                (current == 0x1f) ||                
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }
	
    //----------------- Import ------------------
	public void getArtistInfo(List<Artist> artistList) {

		LOG.debug("## ArtistCount: " + artistList.size());
	 	Locale LastFMLocale = new Locale(settingsService.getLocale().toString()) ; 	
		LOG.debug("## LastFM Locale: " + LastFMLocale.toString());	 	
		
	 	for (Artist artist : artistList) {
	 		try{
			 	if (artist.getArtistFolder() != null) {

			 	LastFMArtist lastFMartist = new LastFMArtist();
			 	net.sourceforge.subsonic.lastfm.Artist tmpArtist = null;
			 	
			 	//String escapedArtist = stripNonValidXMLCharacters(StringEscapeUtils.escapeXml(artist.getArtistFolder()) );
			 	String stripedArtist = stripNonValidXMLCharacters( artist.getArtistFolder() );
			 	String RequestedArtist = (net.sourceforge.subsonic.lastfm.Artist.getCorrection(stripedArtist, api_key)).getName();

				//todo:error
			 	try {
			 			tmpArtist = net.sourceforge.subsonic.lastfm.Artist.getInfo(RequestedArtist, LastFMLocale, null, api_key);
			 		}
			 		catch (Exception e) {
				 		Log.error("## FATAL Error! Artist Fetch! " + tmpArtist.getName());
				}
				
			 	lastFMartist.setArtistname(tmpArtist.getName());
			 	lastFMartist.setMbid(tmpArtist.getMbid());
			 	lastFMartist.setUrl(tmpArtist.getUrl());
			 	lastFMartist.setSince(tmpArtist.getSince());
			 	lastFMartist.setPlayCount(tmpArtist.getPlaycount());

			 	Collection<Album> TopAlbum = net.sourceforge.subsonic.lastfm.Artist.getTopAlbums(RequestedArtist, api_key, 3);

			 	String CollAlbum = null;
			 	for (Album album : TopAlbum) {
			 		if (album != null) {
			 			if (CollAlbum == null) {
			 				CollAlbum = album.getName();
			 			}else {
			 				CollAlbum = CollAlbum + "|" + album.getName();
			 			}
		 			}			 		
			 	}
			 	lastFMartist.setTopalbum(CollAlbum);

			 	
			 	Collection<String> GenreTags =	tmpArtist.getTags();
			 	String CollTag = null; 
			 	for(String TopTag : GenreTags) {
			 		if (TopTag != null) {
			 			if (CollTag == null) {
			 				CollTag = TopTag;
			 			}else {
			 				CollTag = CollTag + "|" + TopTag;
			 			}
		 			}
			 	}
			 	lastFMartist.setToptag(CollTag);

			 	
			 	for(String TopTag : GenreTags) {
			 		if (TopTag != null) {
			 			lastFMartist.setGenre(TopTag); break; }
			 	}
//			 	String[] sep = CollTag.split("\\|");
//			 	List list = Arrays.asList(sep);
			 	
			 	String tmpSum = tmpArtist.getWikiSummary();
			 	tmpSum = StringUtil.removeMarkup(tmpSum);
			 	
//			 	String tmpText = tmpArtist.getWikiText();
//			 	tmpText = StringUtil.removeMarkup(tmpText);
//			 	lastFMartist.setBio(tmpText);
			 	lastFMartist.setSummary(tmpSum);
			 	
//			 	Collection<Tag> TopTags =	 net.sourceforge.subsonic.lastfm.Artist.getTopTags(tmpArtist.getName(), api_key, 1);
			 	Collection<net.sourceforge.subsonic.lastfm.Artist> Similar = net.sourceforge.subsonic.lastfm.Artist.getSimilar(tmpArtist.getName(), 6, api_key);
			 	
			 	for (net.sourceforge.subsonic.lastfm.Artist x : Similar) {
			 		
			 		LastFMArtistSimilar s = new LastFMArtistSimilar();
			 		
			 		s.setArtistName(tmpArtist.getName());
			 		s.setArtistMbid(tmpArtist.getMbid());
			 		s.setSimilarName(x.getName());
			 		s.setSimilarMbid(x.getMbid());
			 		
			 		lastFMArtistSimilarDao.createOrUpdateLastFMArtistSimilar(s);
			 	}

//				/**
//				 * new Artist image importer workaround
//				 */
//			 	if (tmpArtist != null) {
//			 		lastFMartist.setCoverart1(tmpArtist.getImageURL(ImageSize.EXTRALARGE));
//			 	}
			 	
			 	// deprecated
			 	
//			 	PaginatedResult <Image> artistImage = net.sourceforge.subsonic.lastfm.Artist.getImages(RequestedArtist, 1, 5, api_key);
//			 	Collection <Image> Imgs = artistImage.getPageResults();
//
//			 	int counter = 0;
//			 	for (Image Img : Imgs)
//			 	{	 switch(counter)
//		             { case 0: lastFMartist.setCoverart1(Img.getImageURL(ImageSize.LARGESQUARE));break;
//		               case 1: lastFMartist.setCoverart2(Img.getImageURL(ImageSize.LARGESQUARE));break;
//		               case 2: lastFMartist.setCoverart3(Img.getImageURL(ImageSize.LARGESQUARE));break;
//		               case 3: lastFMartist.setCoverart4(Img.getImageURL(ImageSize.LARGESQUARE));break;
//		               case 4: lastFMartist.setCoverart5(Img.getImageURL(ImageSize.LARGESQUARE));break;
//		               }
//			 		counter++;
//			 	}
		 	
			 	
			 	if (lastFMartist.getArtistname() != null) {

			 		LOG.info("## LastFM ArtistInfo Update: " + lastFMartist.getArtistname());			 		
				 	lastFMArtistDao.createOrUpdateLastFMArtist(lastFMartist);
			 	}
			 	}

		        } catch (NullPointerException ex) {
			        System.out.println("## ERROR: " + artist.getName());
		        }
	 	}
		LOG.info("## LastFM ArtistScan Finished");	 	
	 	
	}

	public void getArtistInfo(LastFMArtist lastFMartist, String api_key){
	 	
		try {
						
			/// LastFM API
			net.sourceforge.subsonic.lastfm.Artist Artist = net.sourceforge.subsonic.lastfm.Artist.getCorrection(lastFMartist.getArtistname(), api_key);
			
		 	lastFMartist.setArtistname(Artist.getName());
		 	lastFMartist.setMbid(Artist.getMbid());
			
	        } catch (Exception x) {
	            LOG.warn("## Failed to Update ArtistCover: " + lastFMartist.getArtistname(), x);
	        }

 	}
	
	/**
	 * Artist image importer
	 */
	public void getArtistImage(MediaFile mediaFileArtist, String api_key){
	 	
		if (mediaFileArtist.getCoverArtPath() == null || mediaFileArtist == null) {
			try {
				/// LastFM API
				String artistName = net.sourceforge.subsonic.lastfm.Artist.getCorrection(mediaFileArtist.getArtist(), api_key).getName();
		    	net.sourceforge.subsonic.lastfm.Artist artist = net.sourceforge.subsonic.lastfm.Artist.getInfo(artistName, api_key);
		    	setCoverArtImage (mediaFileArtist.getId(), artist.getImageURL(ImageSize.MEGA), true);
				LOG.info("## Update ArtistCover: " + mediaFileArtist.getArtist());
	        } catch (Exception x) {
	            LOG.warn("## Failed to Update ArtistCover: " + mediaFileArtist.getArtist(), x);
	        }
		}
 	}

	public void getArtistBio(LastFMArtist lastFMartist, String api_key){
	 	
		try {
			/// LastFM API
			String artist = net.sourceforge.subsonic.lastfm.Artist.getCorrection(lastFMartist.getArtistname(), api_key).getName();
		 	String summary = getSummary(artist, api_key);
		 	lastFMartist.setSummary(summary);
		 	System.out.println("summary: ");
	        System.out.println(summary);

	        } catch (Exception x) {
	            LOG.warn("## Failed to Update ArtistCover: " + lastFMartist.getArtistname(), x);
	        }

 	}
	
    public static String getInfo(String artistName, String apiKey){
//    	net.sourceforge.subsonic.lastfm.Artist ArtistTest1 = net.sourceforge.subsonic.lastfm.Artist.getInfo(net.sourceforge.subsonic.lastfm.Artist.getCorrection("AC-DC", apiKey).getName(), apiKey);
    	net.sourceforge.subsonic.lastfm.Artist temp = net.sourceforge.subsonic.lastfm.Artist.getInfo(artistName, apiKey);
        return temp.getWikiSummary(); //.getWikiSummary(); //getWikiText(); //Also .getWikiSummary(), .getWikiLastChanged(), etc...
    }	
	
    public static String getSummary(String artistName, String apiKey){
    	net.sourceforge.subsonic.lastfm.Artist temp = net.sourceforge.subsonic.lastfm.Artist.getInfo(artistName, apiKey);
        return temp.getWikiSummary(); //.getWikiSummary(); //getWikiText(); //Also .getWikiSummary(), .getWikiLastChanged(), etc...
    }		

    public String setCoverArtImage(int id, String url, boolean isArtist) {
        try {
            MediaFile mediaFile = mediaFileService.getMediaFile(id);
            
            if (mediaFile.isAlbum() || mediaFile.isAlbumSet() ){
            	isArtist = false;
            }
            
            saveCoverArt(mediaFile.getPath(), url , isArtist);
            return null;
        } catch (Exception x) {
            LOG.warn("Failed to save cover art for media " + id, x);
            return x.toString();
        }
    }

    private void saveCoverArt(String path, String url, boolean isArtist) throws Exception {
        InputStream input = null;
        HttpClient client = new DefaultHttpClient();

        try {
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 20 * 1000); // 20 seconds
            HttpConnectionParams.setSoTimeout(client.getParams(), 20 * 1000); // 20 seconds
            HttpGet method = new HttpGet(url);

            org.apache.http.HttpResponse response = client.execute(method);
            input = response.getEntity().getContent();

            // Attempt to resolve proper suffix.
            String suffix = "jpg";
            if (url.toLowerCase().endsWith(".gif")) {
                suffix = "gif";
            } else if (url.toLowerCase().endsWith(".png")) {
                suffix = "png";
            }

            String coverName = "cover.";
            
            if (isArtist == true) {
            	coverName = "artist.";
            }
            
            // Check permissions.         
            File newCoverFile = new File(path, coverName + suffix);
            
            
            if (!securityService.isWriteAllowed(newCoverFile)) {
                throw new Exception("Permission denied: " + StringUtil.toHtml(newCoverFile.getPath()));
            }

            // If file exists, create a backup.
            backup(newCoverFile, new File(path, coverName + "backup." + suffix));

            // Write file.
            IOUtils.copy(input, new FileOutputStream(newCoverFile));

            MediaFile mediaFile = mediaFileService.getMediaFile(path);

            // Rename existing cover file if new cover file is not the preferred.
            try {
                File coverFile = mediaFileService.getCoverArt(mediaFile);
                if (coverFile != null) {
                    if (!newCoverFile.equals(coverFile)) {
                        coverFile.renameTo(new File(coverFile.getCanonicalPath() + ".old"));
                        LOG.info("Renamed old image file " + coverFile);
                    }
                }
            } catch (Exception x) {
                LOG.warn("Failed to rename existing cover file.", x);
            }

            mediaFileService.refreshMediaFile(mediaFile);

        } finally {
            IOUtils.closeQuietly(input);
            client.getConnectionManager().shutdown();
        }
    }

    private void backup(File newCoverFile, File backup) {
        if (newCoverFile.exists()) {
            if (backup.exists()) {
                backup.delete();
            }
            if (newCoverFile.renameTo(backup)) {
                LOG.info("Backed up old image file to " + backup);
            } else {
                LOG.warn("Failed to create image file backup " + backup);
            }
        }
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
    
    public void setLastFMArtistDao(LastFMArtistDao lastFMArtistDao) {
        this.lastFMArtistDao = lastFMArtistDao;
    }
    
    public void setLastFMArtistSimilarDao(LastFMArtistSimilarDao lastFMArtistSimilarDao) {
        this.lastFMArtistSimilarDao = lastFMArtistSimilarDao;
    }    
}
