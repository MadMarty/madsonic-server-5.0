package net.sourceforge.subsonic.service;

import net.sourceforge.subsonic.Logger;

public class UserService {

	   private static final Logger LOG = Logger.getLogger(UserService.class);

	    private SecurityService securityService;
	    
	    public void init() {
	        CheckUser();
	    }
	    
	    public void CheckUser() {
	        Runnable runnable = new Runnable() {
	            public void run() {
	                try {
	                    LOG.info("Checking User accounts ...");
	                    
	                    securityService.checkAccounts();
	                    LOG.info("Checking Done");
	                    
	                } catch (Throwable x) {
	                    LOG.error("Failed to check User service: " + x, x);
	                }
	            }
	        };
	        new Thread(runnable).start();
	    }
	    
	    public void setSecurityService(SecurityService securityService) {
	        this.securityService = securityService;
	    }    
}
