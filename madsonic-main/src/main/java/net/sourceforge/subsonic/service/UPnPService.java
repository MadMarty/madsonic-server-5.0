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

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.fourthline.cling.support.model.container.MusicArtist;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.dao.AlbumDao;
import net.sourceforge.subsonic.dao.ArtistDao;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.Album;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.CoverArtScheme;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MediaLibraryStatistics;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.Version;
import net.sourceforge.subsonic.service.upnp.ApacheUpnpServiceConfiguration;
import net.sourceforge.subsonic.service.upnp.MSMediaReceiverRegistrarService;
import net.sourceforge.subsonic.util.StringUtil;
import net.sourceforge.subsonic.util.Util;

import static net.sourceforge.subsonic.controller.CoverArtController.ALBUM_COVERART_PREFIX;
import static net.sourceforge.subsonic.controller.CoverArtController.ARTIST_COVERART_PREFIX;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class UPnPService {

    private static final Logger LOG = Logger.getLogger(UPnPService.class);

    private SettingsService settingsService;
    private PlayerService playerService;
    private VersionService versionService;
    private TranscodingService transcodingService;
    private MediaFileDao mediaFileDao;
    private ArtistDao artistDao;
    private AlbumDao albumDao;
    private UpnpService upnpService;

    public void init() {
        startService();
    }

    public void startService() {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    LOG.info("Checking UPnP service...");
                    createService();
                    LOG.info("Checking UPnP service - Done!");
                } catch (Throwable x) {
                    LOG.error("Failed to check UPnP service: " + x, x);
                }
            }
        };
        new Thread(runnable).start();
    }

    private synchronized void createService() throws Exception {
    	
        upnpService = new UpnpServiceImpl(new ApacheUpnpServiceConfiguration());

        // Asynch search for other devices (most importantly UPnP-enabled routers for port-mapping)
        upnpService.getControlPoint().search();

        // Start DLNA media server?
        setMediaServerEnabled(settingsService.isDlnaEnabled());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Shutting down UPnP service...");
                upnpService.shutdown();
                System.err.println("Shutting down UPnP service - Done!");
            }
        });

    }

    public void setMediaServerEnabled(boolean enabled) {
        if (enabled) {
            try {
                upnpService.getRegistry().addDevice(createMediaServerDevice());
                LOG.info("Enabling UPnP/DLNA media server");
            } catch (Exception x) {
                LOG.error("Failed to start UPnP/DLNA media server: " + x, x);
            }
        } else {
            upnpService.getRegistry().removeAllLocalDevices();
            LOG.info("Disabling UPnP/DLNA media server");
        }
    }

    private LocalDevice createMediaServerDevice() throws Exception {

        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("Madsonic"));
        DeviceType type = new UDADeviceType("MediaServer", 1);

        // TODO: DLNADoc, DLNACaps
        Version version = versionService.getLocalVersion();
        String versionString = version == null ? null : version.toString();
        String licenseEmail = "you@localhost"; //settingsService.getLicenseEmail();
        String licenseString = licenseEmail == null ? "Unlicensed" : ("Licensed to " + licenseEmail);

        DeviceDetails details = new DeviceDetails("Madsonic Media Streamer", new ManufacturerDetails("Madsonic"),
                new ModelDetails("Madsonic", licenseString, versionString),
                new DLNADoc[]{new DLNADoc("DMS", DLNADoc.Version.V1_5)}, null);

        // TODO: add smaller icon as well
        Icon icon = new Icon("image/png", 512, 512, 32, getClass().getResource("madsonic-512.png"));

        LocalService<ContentDirectory> contentDirectoryservice = new AnnotationLocalServiceBinder().read(ContentDirectory.class);
        contentDirectoryservice.setManager(new DefaultServiceManager<ContentDirectory>(contentDirectoryservice) {
            @Override
            protected ContentDirectory createServiceInstance() throws Exception {
                return new ContentDirectory();
            }
        });


        // TODO: This is returned from Windows Media Player
        // "http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L4_SO_G726,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_PRO,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAFULL,http-get:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM,http-get:*:audio/L16;rate=44100;channels=2:DLNA.ORG_PN=LPCM,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMABASE,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL_XAC3,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED,http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_SP_G726,http-get:*:audio/L16;rate=48000;channels=2:DLNA.ORG_PN=LPCM,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_PRO,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3X,http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L5_SO_G726,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPLL_BASE,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG1,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAPRO,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_LRG,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC_XAC3,http-get:*:audio/L16;rate=44100;channels=1:DLNA.ORG_PN=LPCM,http-get:*:image/x-ycbcr-yuv420:*,http-get:*:video/x-ms-wmv:*,http-get:*:audio/x-ms-wma:*,http-get:*:video/wtv:*,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_SP_G726,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_PRO,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L5_SO_G726,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L4_SO_G726,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPLL_BASE,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_PRO,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3,rtsp-rtp-udp:*:video/x-ms-wmv:*";

        // TODO: Provide protocol info
        final ProtocolInfos sourceProtocols = new ProtocolInfos(
                new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio/mpeg", "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01"),
                new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0"));

        LocalService<ConnectionManagerService> connetionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        connetionManagerService.setManager(new DefaultServiceManager<ConnectionManagerService>(connetionManagerService) {
            @Override
            protected ConnectionManagerService createServiceInstance() throws Exception {
                return new ConnectionManagerService(sourceProtocols, null);
            }
        });

        // For compatibility with Microsoft
        LocalService<MSMediaReceiverRegistrarService> receiverService = new AnnotationLocalServiceBinder().read(MSMediaReceiverRegistrarService.class);
        receiverService.setManager(new DefaultServiceManager<MSMediaReceiverRegistrarService>(receiverService, MSMediaReceiverRegistrarService.class));
//        System.err.println(receiverService); // TODO
//
        return new LocalDevice(identity, type, details, new Icon[]{icon}, new LocalService[]{contentDirectoryservice, connetionManagerService, receiverService});
//        return new LocalDevice(identity, type, details, new Icon[] {icon}, new LocalService[] {contentDirectoryservice, connetionManagerService});
    }


    public UpnpService getUpnpService() {
        return upnpService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setArtistDao(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    public void setAlbumDao(AlbumDao albumDao) {
        this.albumDao = albumDao;
    }

    public void setMediaFileDao(MediaFileDao mediaFileDao) {
        this.mediaFileDao = mediaFileDao;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setTranscodingService(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }


    private class ContentDirectory extends AbstractContentDirectoryService {

        public static final String ROOT_CONTAINER_ID = "0";

        @Override
        public BrowseResult browse(String objectId, BrowseFlag browseFlag, String filter, long firstResult,
                long maxResults, SortCriterion[] orderby) throws ContentDirectoryException {

            // TODO
            System.out.println("objectId    : " + objectId);
            System.out.println("browseFlag  : " + browseFlag);
            System.out.println("filter      : " + filter);
            System.out.println("firstResult : " + firstResult);
            System.out.println("maxResults  : " + maxResults);
            System.out.println("orderBy     : " + Arrays.toString(orderby));
            System.out.println();

            // maxResult == 0 means all.
            if (maxResults == 0) {
                maxResults = Integer.MAX_VALUE;
            }

            try {

                if (ROOT_CONTAINER_ID.equals(objectId)) {
                    return browseRoot(browseFlag, firstResult, maxResults);
                }

                if (objectId.startsWith(ARTIST_COVERART_PREFIX)) {
                    return browseArtist(objectId, browseFlag, firstResult, maxResults);
                }

                if (objectId.startsWith(ALBUM_COVERART_PREFIX)) {
                    return browseAlbum(objectId, browseFlag, firstResult, maxResults);
                }

                // TODO: else {...}

                throw new Exception("Not implemented");
                // TODO: Container update ID.

            } catch (Exception ex) {
                // TODO: Use different error codes.
                throw new ContentDirectoryException(
                        ContentDirectoryErrorCode.CANNOT_PROCESS,
                        ex.toString()
                );
            }
        }

        private BrowseResult browseRoot(BrowseFlag browseFlag, long firstResult, long maxResults) throws Exception {
            return browseFlag == BrowseFlag.METADATA ? browseRootMetadata() : browseArtists(firstResult, maxResults);
        }

        private BrowseResult browseRootMetadata() throws Exception {
            StorageFolder root = new StorageFolder();
            root.setId(ROOT_CONTAINER_ID);
            root.setParentID("-1");

            MediaLibraryStatistics statistics = settingsService.getMediaLibraryStatistics();
            root.setStorageUsed(statistics == null ? 0 : statistics.getTotalLengthInBytes());
            root.setTitle("Subsonic Media");
            root.setRestricted(true);
            root.setSearchable(false);
            root.setWriteStatus(WriteStatus.NOT_WRITABLE);
            // TODO: Support videos
            root.setChildCount(artistDao.getAlphabetialArtists(0, Integer.MAX_VALUE).size());

            DIDLContent didl = new DIDLContent();
            didl.addContainer(root);
            return createBrowseResult(didl, 1, 1);
        }

        private BrowseResult browseArtists(long firstResult, long maxResults) throws Exception {
            DIDLContent didl = new DIDLContent();
            for (Artist artist : artistDao.getAlphabetialArtists((int) firstResult, (int) maxResults)) {
                didl.addContainer(createArtistContainer(artist));
            }
            int artistCount = artistDao.getAlphabetialArtists(0, Integer.MAX_VALUE).size();
            return createBrowseResult(didl, didl.getContainers().size(), artistCount);
        }

        private BrowseResult browseArtist(String objectId, BrowseFlag browseFlag, long firstResult, long maxResults) throws Exception {
            Artist artist = getArtistByObjectId(objectId);
            return browseFlag == BrowseFlag.METADATA ? browseArtistMetadata(artist) : browseAlbums(artist, firstResult, maxResults);
        }

        private BrowseResult browseArtistMetadata(Artist artist) throws Exception {
            DIDLContent didl = new DIDLContent();
            didl.addContainer(createArtistContainer(artist));
            return createBrowseResult(didl, 1, 1);
        }

        private BrowseResult browseAlbums(Artist artist, long firstResult, long maxResults) throws Exception {
            DIDLContent didl = new DIDLContent();
            List<Album> albums = albumDao.getAlbumsForArtist(artist.getName());
            for (int i = (int) firstResult; i < Math.min(albums.size(), firstResult + maxResults); i++) {
                didl.addContainer(createAlbumContainer(artist, albums.get(i)));
            }
            return createBrowseResult(didl, didl.getContainers().size(), albums.size());
        }

        private BrowseResult browseAlbum(String objectId, BrowseFlag browseFlag, long firstResult, long maxResults) throws Exception {
            Album album = getAlbumByObjectId(objectId);
            Artist artist = artistDao.getArtist(album.getArtist());
            return browseFlag == BrowseFlag.METADATA ? browseAlbumMetadata(artist, album) : browseSongs(album, firstResult, maxResults);
        }

        private BrowseResult browseAlbumMetadata(Artist artist, Album album) throws Exception {
            DIDLContent didl = new DIDLContent();
            didl.addContainer(createAlbumContainer(artist, album));
            return createBrowseResult(didl, 1, 1);
        }

        private BrowseResult browseSongs(Album album, long firstResult, long maxResults) throws Exception {
            DIDLContent didl = new DIDLContent();
            List<MediaFile> songs = mediaFileDao.getSongsForAlbum(album.getArtist(), album.getName());
            // TODO: Create util method for extracting sublist.
            for (int i = (int) firstResult; i < Math.min(songs.size(), firstResult + maxResults); i++) {
                didl.addItem(createSongItem(album, songs.get(i)));
            }
            return createBrowseResult(didl, didl.getItems().size(), songs.size());
        }

        private Container createArtistContainer(Artist artist) {
            MusicArtist container = new MusicArtist();
            container.setId(ARTIST_COVERART_PREFIX + artist.getId());
            container.setParentID(ROOT_CONTAINER_ID);
            container.setTitle(artist.getName());
            container.setChildCount(albumDao.getAlbumsForArtist(artist.getName()).size());
            return container;
        }

        private Container createAlbumContainer(Artist artist, Album album) throws Exception {
            MusicAlbum container = new MusicAlbum();
            container.setId(ALBUM_COVERART_PREFIX + album.getId());
            container.setParentID(ARTIST_COVERART_PREFIX + artist.getId());
            container.setTitle(album.getName());

            String albumArtUrl = getBaseUrl() + "coverArt.view?id=" + container.getId() + "&size=" + CoverArtScheme.LARGE.getSize();
            container.setAlbumArtURIs(new URI[]{new URI(albumArtUrl)});
            container.setArtists(new PersonWithRole[]{new PersonWithRole(artist.getName())});
            container.setDescription(album.getComment());
            container.setChildCount(album.getSongCount());
            return container;
        }

        private Item createSongItem(Album album, MediaFile song) {
            MusicTrack item = new MusicTrack();
            item.setId(String.valueOf(song.getId()));
            item.setParentID(ALBUM_COVERART_PREFIX + album.getId());
            item.setTitle(song.getTitle());
            item.setAlbum(song.getAlbumName());
            if (song.getArtist() != null) {
                item.setArtists(new PersonWithRole[]{new PersonWithRole(song.getArtist())});
            }
            Integer year = song.getYear();
            if (year != null) {
                item.setDate(year + "-01-01");
            }
            item.setOriginalTrackNumber(song.getTrackNumber());
            if (song.getGenre() != null) {
                item.setGenres(new String[]{song.getGenre()});
            }
            item.setResources(Arrays.asList(createResourceForsong(song)));
            item.setDescription(song.getComment());
            return item;
        }

        private Res createResourceForsong(MediaFile song) {
            Player player = playerService.getGuestPlayer(null);
            String suffix = transcodingService.getSuffix(player, song, null);
            String mimeTypeString = StringUtil.getMimeType(suffix);
            MimeType mimeType = mimeTypeString == null ? null : MimeType.valueOf(mimeTypeString);
            String url = getBaseUrl() + "stream?id=" + song.getId() + "&player=" + player.getId();
            System.err.println(url);

            Res res = new Res(mimeType, null, url);
            res.setDuration(song.getDurationString());
            return res;
        }

        private String getBaseUrl() {
            int port = settingsService.getPort();
            String contextPath = settingsService.getUrlRedirectContextPath();

            StringBuilder url = new StringBuilder("http://").append(Util.getLocalIpAddress())
                    .append(":").append(port).append("/");
            if (StringUtils.isNotEmpty(contextPath)) {
                url.append(contextPath).append("/");
            }
            return url.toString();
        }

        private Artist getArtistByObjectId(String objectId) {
            return artistDao.getArtist(Integer.parseInt(objectId.replace(ARTIST_COVERART_PREFIX, "")));
        }

        private Album getAlbumByObjectId(String objectId) {
            return albumDao.getAlbum(Integer.parseInt(objectId.replace(ALBUM_COVERART_PREFIX, "")));
        }

        private BrowseResult createBrowseResult(DIDLContent didl, int count, int totalMatches) throws Exception {
            return new BrowseResult(new DIDLParser().generate(didl), count, totalMatches);
        }

        @Override
        public BrowseResult search(String containerId,
                String searchCriteria, String filter,
                long firstResult, long maxResults,
                SortCriterion[] orderBy) throws ContentDirectoryException {
            // You can override this method to implement searching!
            return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
        }
    }

}
