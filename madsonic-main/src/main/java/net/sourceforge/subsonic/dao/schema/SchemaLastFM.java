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
package net.sourceforge.subsonic.dao.schema;

import net.sourceforge.subsonic.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Used for creating and evolving the database schema.
 * This class implements the database schema for Madsonic version 4.8.
 *
 * @author Madevil
 */
public class SchemaLastFM extends Schema {

    private static final Logger LOG = Logger.getLogger(SchemaLastFM.class);

    @Override
    public void execute(JdbcTemplate template) {

		////////////////////////////////////////

        if (template.queryForInt("select count(*) from version where version = 122") == 0) {
            LOG.info("Updating database schema to version 122.");
            template.execute("insert into version values (122)");
			
		
			// Add LastFM Artist Table
	        if (!tableExists(template, "lastfm_artist")) {
	            LOG.info("Database table 'lastfm_artist' not found. Creating it.");
	            template.execute("create cached table lastfm_artist (" +
	                    "id identity," +
	                    "artistname varchar," +
	                    "mbid varchar," +
	                    "since varchar," +
	                    "genre varchar," +
	                    "url varchar," +
	                    "fanart varchar," +
	                    "coverart1 varchar," +
	                    "coverart2 varchar," +
	                    "coverart3 varchar," +
	                    "coverart4 varchar," +
	                    "coverart5 varchar," +
	                    "toptag varchar," +
	                    "topalbum varchar," +
	                    "bio varchar," +
	                    "summary varchar," +
	                    "play_count int default 0)");
						
	            template.execute("create index idx_lastfm_artist_artistname on lastfm_artist(artistname)");
	            template.execute("create index idx_lastfm_artist_mbid on lastfm_artist(mbid)");

	            LOG.info("Database table 'lastfm_artist' was created successfully.");
	        }


			// Add LastFM Artist Similar Table
	        if (!tableExists(template, "lastfm_artist_similar")) {
	            LOG.info("Database table 'lastfm_artist_similar' not found. Creating it.");
	            template.execute("create cached table lastfm_artist_similar (" +
	                    "id identity," +
	                    "artist_name varchar," +
	                    "artist_mbid varchar," +
	                    "similar_name varchar," +
	                    "similar_mbid varchar)");
						
	            template.execute("create index idx_lastfm_artist_similar_artist_name  on lastfm_artist_similar(artist_name)");
	            template.execute("create index idx_lastfm_artist_similar_artist_mbid on lastfm_artist_similar(artist_mbid)");
	            template.execute("create index idx_lastfm_artist_similar_similar_name on lastfm_artist_similar(similar_name)");
	            template.execute("create index idx_lastfm_artist_similar_similar_mbid on lastfm_artist_similar(similar_mbid)");


	            LOG.info("Database table 'lastfm_artist' was created successfully.");
	        }	        
	        
			////////////////

			
			
        }
		////////////////////////////////////////
		
	}
	
}