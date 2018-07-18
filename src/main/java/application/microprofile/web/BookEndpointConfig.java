package application.microprofile.web;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@RequestScoped
public class BookEndpointConfig {

  
  @Inject
  @ConfigProperty(name = "io_openliberty_microprofile_inMaintenance")
  private Provider<Boolean> inMaintenance;

  
  public boolean isInMaintenance() {
    return inMaintenance.get();
  }
}
