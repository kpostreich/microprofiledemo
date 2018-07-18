package application.microprofile.web;

import application.microprofile.model.Book;
import application.microprofile.repo.BookManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("books")
@ApplicationScoped
public class BookEndpoint  {
	
	private final long TIMEOUT = 500;
    private final long SLEEPTIME = 5000;
    private static final int requestVolumeThresholdValue = 2;
    private int counterForInvokingBookService = 0;
    private int minBookCatalogSizeCheck = 2;
    
 
	//set the test type (retry, timeout, circuitBreaker, none
    private String testType = "none";
   
    @Inject
    BookEndpointConfig bookEndpointConfig;
 
    @Inject
    private BookManager bookManager;
    
    
    @GET
    //@Retry(maxRetries = 4, retryOn = {RuntimeException.class})
    //@Timeout(TIMEOUT)
    //@CircuitBreaker(requestVolumeThreshold=requestVolumeThresholdValue,failureRatio=0.5,successThreshold=2, delay=10000)
    //@Fallback(fallbackMethod = "fallbackGetAllBooks")
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBook(@PathParam("id") String id)  {
        Book book = bookManager.get(id);
   
        
        if (testType == "timeout") {
        	
          counterForInvokingBookService++;
          System.out.println("counter for book service: " + counterForInvokingBookService);
          
          try {
			if (isSlow()) {
				System.out.println("Slow response retrieving book");			
				return Response.ok(book).build();
			 }
		 } catch (InterruptedException e) {
			System.out.println("SLOW RESPONSE retreiving book");
		 } 
       } 
        
        else if (testType == "retry") {
            
        	counterForInvokingBookService++;
        	System.out.println("counter for book service: " + counterForInvokingBookService);
        	
        	if (isDown())throw new RuntimeException(); 
				return Response.ok(book).build();
    	} 
        
       else if (testType == "circuitBreaker") {
    	   
    	   counterForInvokingBookService++;
    	   System.out.println("counter for book service: " + counterForInvokingBookService);
    	  
    	   //The Exception will result in the Fallback method being triggered
       		if (isDown())throw new RuntimeException(); 
  				return Response.ok(book).build();
       }  
       
       else 
        System.out.println("Successfully retrieved book");
        return Response.ok(book).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBooks() {
    	
    	if (!bookEndpointConfig.isInMaintenance()) {
    	  
    	  return Response.ok(bookManager.getAll()).build();
    	 
    	} else {
    		  System.out.println("Service is down for Maintenance");
    		  String message = "Service is currently in maintenance.";    
    	      //String json = "{\"ERROR\":\"" + message + "\"}";
    	      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(bookManager.sendErrorMessage(message)).build();
    	                  
    	  }
      }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(Book book) {
        String bookId = bookManager.add(book);
        return Response.created(
                UriBuilder.fromResource(this.getClass()).path(bookId).build())
                .build();
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fallbackGetAllBooks(@PathParam("id") String id) {
       
        System.out.println("Fallback service called - list all books.....");
        
        return Response.ok(bookManager.getAll()).build();
    }


    private boolean isSlow() throws InterruptedException {
        // Simulating 2 failures to trip the circuit.  
        if (counterForInvokingBookService <= requestVolumeThresholdValue) {
         
        	return false;
        }	
        else {
        	if (counterForInvokingBookService > requestVolumeThresholdValue) {
          	  counterForInvokingBookService = 0;
          	  System.out.println("Simulating Slow Response.....");
              Thread.sleep(SLEEPTIME);
              System.out.println("Service took tooooo long to respond!!!");
        	}   
        	return true; 
        }   
    }    
  
    
    private boolean isDown() {
        // Simulating 2 failures to trip the circuit. 
        if (counterForInvokingBookService <= requestVolumeThresholdValue) {
           	System.out.println("Service is DOWN!");
        	return true;
        }	
        else {
        	if (counterForInvokingBookService > requestVolumeThresholdValue+1) {
          	   counterForInvokingBookService = 0;
           	}   
        	return false; 
        }   
    }    
        
    public int getMinBookCatalogSizeCheck() {
 		return minBookCatalogSizeCheck;
 	}

 
    
    /*
    private boolean isSlow1() throws InterruptedException {
        if (Math.random() > 0.6) {
            // approx 40% chance that will be slow. 0.4 = 60% chance of slow
            System.out.println("Simulating Slow Response.....");
            Thread.sleep(SLEEPTIME);
            System.out.println("Service took tooooo long to respond!!!");
            return true;
        }
        return false;
    }
*/

}	
