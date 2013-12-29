package net.sourceforge.subsonic.domain;

public class LastFMAlbumSimilar {

	private String albumName;
	private String albumMbid;	
	private int mediaFileId;	

	public LastFMAlbumSimilar() {
    }
    
    public LastFMAlbumSimilar(String albumName, String albumMbid, int mediaFileId) {
        this.setAlbumName(albumName);
        this.setAlbumMbid(albumMbid);
        this.setMediaFileId(mediaFileId);
    }

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getAlbumMbid() {
		return albumMbid;
	}

	public void setAlbumMbid(String albumMbid) {
		this.albumMbid = albumMbid;
	}

	public int getMediaFileId() {
		return mediaFileId;
	}

	public void setMediaFileId(int mediaFileId) {
		this.mediaFileId = mediaFileId;
	}
}
