package net.sourceforge.subsonic.domain;

public class LastFMArtistSimilar {

	private int id;
	private String artistName;
	private String artistMbid;	
	private String similarName;
	private String similarMbid;	

	public LastFMArtistSimilar() {
    }
    
    public LastFMArtistSimilar(int id, String artistName, String artistMbid, String similarName, String similarMbid) {
        this.id = id;
        this.artistName = artistName;
        this.artistMbid = artistMbid;
        this.similarName = similarName;
        this.similarMbid = similarMbid;
    }

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getArtistMbid() {
		return artistMbid;
	}

	public void setArtistMbid(String artistMbid) {
		this.artistMbid = artistMbid;
	}

	public String getSimilarName() {
		return similarName;
	}

	public void setSimilarName(String similarName) {
		this.similarName = similarName;
	}

	public String getSimilarMbid() {
		return similarMbid;
	}

	public void setSimilarMbid(String similarMbid) {
		this.similarMbid = similarMbid;
	}
}
