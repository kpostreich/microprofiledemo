package application.microprofile;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import application.microprofile.repo.BookManager;
import application.microprofile.web.BookEndpoint;
import application.microprofile.web.BookEndpointConfig;

@ApplicationScoped
@Health 
@Path("health")
public class HealthEndpoint implements HealthCheck {

    @Inject 
    private BookEndpoint be ;
	
	@Inject
    private BookManager bookManager;
    
	@Inject
	@ConfigProperty(name = "io_openliberty_microprofile_inMaintenance")
	 private Provider<Boolean> inMaintenance;

		
	public boolean isHealthy() {
	    if (inMaintenance.get() ) {
	      return false;
	    }  
	   return true;  
	}
	
	
	 @Override
	 public HealthCheckResponse call() {
	        if (bookManager.getAll().size() < be.getMinBookCatalogSizeCheck()
	        	|| !this.isHealthy()
	        )
	        {
	          return HealthCheckResponse.named("READY_FOR_DEMO")
	                   .withData("BookCatalogSizetest", bookManager.getAll().size())
	                   .withData("IsHealthy", this.isHealthy())
	                   .down()
	                   .build();
	        } else {
	          return HealthCheckResponse.named("READY_FOR_DEMO")
	        		  .withData("BookCatalogSizetest", bookManager.getAll().size())
	                  .withData("IsHealthy", this.isHealthy())
	                  .up()
	                  .build();
	        }
	        
	      }
}
