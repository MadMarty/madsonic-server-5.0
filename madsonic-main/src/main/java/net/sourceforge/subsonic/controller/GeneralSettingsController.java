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
package net.sourceforge.subsonic.controller;

import net.sourceforge.subsonic.command.GeneralSettingsCommand;
import net.sourceforge.subsonic.domain.Theme;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.UserSettings;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Controller for the page used to administrate general settings.
 *
 * @author Sindre Mehus
 */
public class GeneralSettingsController extends SimpleFormController {

    private SettingsService settingsService;
    private SecurityService securityService;
    
    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
    Map<String, Boolean> model = new HashMap<String, Boolean>();
    
    User user = securityService.getCurrentUser(request);
    UserSettings userSettings = settingsService.getUserSettings(user.getUsername());

    model.put("customScrollbar", userSettings.isCustomScrollbarEnabled());     
    return model;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        GeneralSettingsCommand command = new GeneralSettingsCommand();
        command.setCoverArtFileTypes(settingsService.getCoverArtFileTypes());
        command.setIgnoredArticles(settingsService.getIgnoredArticles());
        command.setShortcuts(settingsService.getShortcuts());
        command.setIndex(settingsService.getIndexString());
        command.setIndex2(settingsService.getIndex2String());
        command.setIndex3(settingsService.getIndex3String());		
        command.setIndex4(settingsService.getIndex4String());	
        command.setMusicFileTypes(settingsService.getMusicFileTypes());
        command.setVideoFileTypes(settingsService.getVideoFileTypes());
        command.setPlaylistFolder(settingsService.getPlaylistFolder());
        command.setPlaylistExportFolder(settingsService.getPlaylistExportFolder());
        command.setShowShortcuts(settingsService.isShowShortcuts());
        command.setShowAlbumsYear(settingsService.isShowAlbumsYear());
        command.setShowAlbumsYearApi(settingsService.isShowAlbumsYearApi());
        command.setSortAlbumsByFolder(settingsService.isSortAlbumsByFolder());
        command.setSortFilesByFilename(settingsService.isSortFilesByFilename());
        command.setFolderParsing(settingsService.isFolderParsingEnabled());
        command.setAlbumSetParsing(settingsService.isAlbumSetParsingEnabled());
        command.setLogfileReverse(settingsService.isLogfileReverse());
        command.setLogfileLevel(settingsService.getLogLevel());
        command.setSortMediaFileFolder(settingsService.isSortMediaFileFolder());
        command.setUsePremiumServices(settingsService.isUsePremiumServices());
        command.setShowGenericArtistArt(settingsService.isShowGenericArtistArt());
        command.setGettingStartedEnabled(settingsService.isGettingStartedEnabled());
        command.setWelcomeTitle(settingsService.getWelcomeTitle());
        command.setWelcomeSubtitle(settingsService.getWelcomeSubtitle());
        command.setWelcomeMessage(settingsService.getWelcomeMessage());
        command.setLoginMessage(settingsService.getLoginMessage());
        command.setListType(settingsService.getListType());
        command.setNewAdded(settingsService.getNewaddedTimespan());
        command.setLeftframeSize(settingsService.getLeftframeSize());
        command.setPlayQueueSize(settingsService.getPlayqueueSize());
        command.setShowQuickEdit(settingsService.isShowQuickEdit());
        command.setHTML5Enabled(settingsService.isHTML5PlayerEnabled());
        command.setOwnGenreEnabled(settingsService.isOwnGenreEnabled());
        command.setPlaylistEnabled(settingsService.isPlaylistEnabled());
        command.setUploadFolder(SettingsService.getUploadFolder());
        
        Theme[] themes = settingsService.getAvailableThemes();
        command.setThemes(themes);
        String currentThemeId = settingsService.getThemeId();
        for (int i = 0; i < themes.length; i++) {
            if (currentThemeId.equals(themes[i].getId())) {
                command.setThemeIndex(String.valueOf(i));
                break;
            }
        }

        Locale currentLocale = settingsService.getLocale();
        Locale[] locales = settingsService.getAvailableLocales();
        String[] localeStrings = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            localeStrings[i] = locales[i].getDisplayName(locales[i]);

            if (currentLocale.equals(locales[i])) {
                command.setLocaleIndex(String.valueOf(i));
            }
        }
        command.setLocales(localeStrings);
        return command;

    }

    protected void doSubmitAction(Object comm) throws Exception {
        GeneralSettingsCommand command = (GeneralSettingsCommand) comm;

        int themeIndex = Integer.parseInt(command.getThemeIndex());
        Theme theme = settingsService.getAvailableThemes()[themeIndex];

        int localeIndex = Integer.parseInt(command.getLocaleIndex());
        Locale locale = settingsService.getAvailableLocales()[localeIndex];

        command.setToast(true);
        
        command.setStatusPlayerChanged(!settingsService.isHTML5PlayerEnabled() == command.isHTML5Enabled());
        
        command.setReloadNeeded(!settingsService.getIndexString().equals(command.getIndex()) ||
								!settingsService.getIndex2String().equals(command.getIndex2()) ||
								!settingsService.getIndex3String().equals(command.getIndex3()) ||
								!settingsService.getIndex4String().equals(command.getIndex4()) ||
								!settingsService.isSortMediaFileFolder() == command.isSortMediaFileFolder() ||
                                !settingsService.getIgnoredArticles().equals(command.getIgnoredArticles()) ||
                                !settingsService.getShortcuts().equals(command.getShortcuts()) ||
                                !settingsService.getThemeId().equals(theme.getId()) ||
                                !settingsService.getLocale().equals(locale));

        command.setFullReloadNeeded(!settingsService.isShowShortcuts() == command.isShowShortcuts() ||
        						   !(settingsService.getLeftframeSize()==(command.getLeftframeSize())) ||
        						   !(settingsService.isPlaylistEnabled()==(command.isPlaylistEnabled())) ||
        						   !(settingsService.getPlayqueueSize()==(command.getPlayQueueSize())) );
        
        settingsService.setIndexString(command.getIndex());
        settingsService.setIndex2String(command.getIndex2());
        settingsService.setIndex3String(command.getIndex3());
        settingsService.setIndex4String(command.getIndex4());
        settingsService.setIgnoredArticles(command.getIgnoredArticles());
        settingsService.setShortcuts(command.getShortcuts());
        settingsService.setPlaylistFolder(command.getPlaylistFolder());
        settingsService.setPlaylistExportFolder(command.getPlaylistExportFolder());
        settingsService.setMusicFileTypes(command.getMusicFileTypes());
        settingsService.setVideoFileTypes(command.getVideoFileTypes());
        settingsService.setCoverArtFileTypes(command.getCoverArtFileTypes());
        settingsService.setShowAlbumsYear(command.isShowAlbumsYear());
        settingsService.setShowShortcuts(command.isShowShortcuts());
        settingsService.setShowAlbumsYearApi(command.isShowAlbumsYearApi());
        settingsService.setSortAlbumsByFolder(command.isSortAlbumsByFolder());
        settingsService.setSortFilesByFilename(command.isSortFilesByFilename());
        settingsService.setSortMediaFileFolder(command.isSortMediaFileFolder());
        settingsService.setFolderParsingEnabled(command.isFolderParsing());
        settingsService.setAlbumSetParsingEnabled(command.isAlbumSetParsing());
        settingsService.setLogfileReverse(command.isLogfileReverse());
        settingsService.setLogfileLevel(command.getLogfileLevel());
        settingsService.setUsePremiumServices(command.isUsePremiumServices());
        settingsService.setShowGenericArtistArt(command.isShowGenericArtistArt());
        settingsService.setGettingStartedEnabled(command.isGettingStartedEnabled());
        settingsService.setWelcomeTitle(command.getWelcomeTitle());
        settingsService.setWelcomeSubtitle(command.getWelcomeSubtitle());
        settingsService.setWelcomeMessage(command.getWelcomeMessage());
        settingsService.setLoginMessage(command.getLoginMessage());
        settingsService.setThemeId(theme.getId());
        settingsService.setLocale(locale);
        settingsService.setListType(command.getListType());
        settingsService.setNewaddedTimespan(command.getNewAdded());
        settingsService.setLeftframeSize(command.getLeftframeSize());
        settingsService.setPlayqueueSize(command.getPlayQueueSize());
        settingsService.setShowQuickEdit(command.isShowQuickEdit());
        settingsService.setHTML5PlayerEnabled(command.isHTML5Enabled());
        settingsService.setOwnGenreEnabled(command.isOwnGenreEnabled());
        settingsService.setPlaylistEnabled(command.isPlaylistEnabled());
        SettingsService.setUploadFolder(command.getUploadFolder());
        
        settingsService.save();
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
    
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }    
}
