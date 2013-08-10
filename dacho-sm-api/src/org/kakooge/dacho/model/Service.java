package org.kakooge.dacho.model;

import java.net.URL;
import java.util.List;

import org.kakooge.dacho.api.ServiceContext;

public class Service {
    private int id;
    private String name;
    private String description;
    private String serviceClass;
    private ServiceContext serviceContext;
    private List<URL> classpath;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public ServiceContext getServiceContext(){
    	return serviceContext;
    }

    public List<URL> getClasspath(){
    	return classpath;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classpath == null) ? 0 : classpath.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((serviceClass == null) ? 0 : serviceClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Service other = (Service) obj;
		if (classpath == null) {
			if (other.classpath != null)
				return false;
		} else if (!classpath.equals(other.classpath))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (serviceClass == null) {
			if (other.serviceClass != null)
				return false;
		} else if (!serviceClass.equals(other.serviceClass))
			return false;
		return true;
	}

	/**
     * Model the database <code>Service</code>
     * @param id    - The service id
     * @param name  - the unique service name
     * @param description - the service description
     * @param serviceClass - the service class
     * @param args  - the service's arguments
     */
    public Service(int id, String name, String description, 
    		String serviceClass, ServiceContext serviceContext,
    		List<URL> classpath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.serviceClass = serviceClass;
        this.serviceContext = serviceContext;
        this.classpath = classpath;
    }
    
        
    /**
     * Model the database <code>Service</code>. Use this mainly for set operations because the {@link Service#hashCode()} is based on only <code>id</code> and <code>name</code>
     * @param id    - The service id
     * @param name  - the unique service name
     */
    public Service(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
