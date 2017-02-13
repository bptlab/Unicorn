/**
 * EventProcessingPlatformWebserviceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.hpi;

public class EventProcessingPlatformWebserviceLocator extends org.apache.axis.client.Service implements de.hpi.EventProcessingPlatformWebservice {

    public EventProcessingPlatformWebserviceLocator() {
    }


    public EventProcessingPlatformWebserviceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public EventProcessingPlatformWebserviceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for EventProcessingPlatformWebserviceHttpSoap11Endpoint
    private java.lang.String EventProcessingPlatformWebserviceHttpSoap11Endpoint_address = "http://172.16.64.105:8080/SushiWicket-featureMergeBsSemiar/services/EventProcessingPlatformWebservice.EventProcessingPlatformWebserviceHttpSoap11Endpoint/";

    public java.lang.String getEventProcessingPlatformWebserviceHttpSoap11EndpointAddress() {
        return EventProcessingPlatformWebserviceHttpSoap11Endpoint_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String EventProcessingPlatformWebserviceHttpSoap11EndpointWSDDServiceName = "EventProcessingPlatformWebserviceHttpSoap11Endpoint";

    public java.lang.String getEventProcessingPlatformWebserviceHttpSoap11EndpointWSDDServiceName() {
        return EventProcessingPlatformWebserviceHttpSoap11EndpointWSDDServiceName;
    }

    public void setEventProcessingPlatformWebserviceHttpSoap11EndpointWSDDServiceName(java.lang.String name) {
        EventProcessingPlatformWebserviceHttpSoap11EndpointWSDDServiceName = name;
    }

    public de.hpi.EventProcessingPlatformWebservicePortType getEventProcessingPlatformWebserviceHttpSoap11Endpoint() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(EventProcessingPlatformWebserviceHttpSoap11Endpoint_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getEventProcessingPlatformWebserviceHttpSoap11Endpoint(endpoint);
    }

    public de.hpi.EventProcessingPlatformWebservicePortType getEventProcessingPlatformWebserviceHttpSoap11Endpoint(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            de.hpi.EventProcessingPlatformWebserviceSoap11BindingStub _stub = new de.hpi.EventProcessingPlatformWebserviceSoap11BindingStub(portAddress, this);
            _stub.setPortName(getEventProcessingPlatformWebserviceHttpSoap11EndpointWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setEventProcessingPlatformWebserviceHttpSoap11EndpointEndpointAddress(java.lang.String address) {
        EventProcessingPlatformWebserviceHttpSoap11Endpoint_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (de.hpi.EventProcessingPlatformWebservicePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                de.hpi.EventProcessingPlatformWebserviceSoap11BindingStub _stub = new de.hpi.EventProcessingPlatformWebserviceSoap11BindingStub(new java.net.URL(EventProcessingPlatformWebserviceHttpSoap11Endpoint_address), this);
                _stub.setPortName(getEventProcessingPlatformWebserviceHttpSoap11EndpointWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("EventProcessingPlatformWebserviceHttpSoap11Endpoint".equals(inputPortName)) {
            return getEventProcessingPlatformWebserviceHttpSoap11Endpoint();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://hpi.de", "EventProcessingPlatformWebservice");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://hpi.de", "EventProcessingPlatformWebserviceHttpSoap11Endpoint"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("EventProcessingPlatformWebserviceHttpSoap11Endpoint".equals(portName)) {
            setEventProcessingPlatformWebserviceHttpSoap11EndpointEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
