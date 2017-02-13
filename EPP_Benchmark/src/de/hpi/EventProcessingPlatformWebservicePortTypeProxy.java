package de.hpi;

public class EventProcessingPlatformWebservicePortTypeProxy implements de.hpi.EventProcessingPlatformWebservicePortType {
  private String _endpoint = null;
  private de.hpi.EventProcessingPlatformWebservicePortType eventProcessingPlatformWebservicePortType = null;
  
  public EventProcessingPlatformWebservicePortTypeProxy() {
    _initEventProcessingPlatformWebservicePortTypeProxy();
  }
  
  public EventProcessingPlatformWebservicePortTypeProxy(String endpoint) {
    _endpoint = endpoint;
    _initEventProcessingPlatformWebservicePortTypeProxy();
  }
  
  private void _initEventProcessingPlatformWebservicePortTypeProxy() {
    try {
      eventProcessingPlatformWebservicePortType = (new de.hpi.EventProcessingPlatformWebserviceLocator()).getEventProcessingPlatformWebserviceHttpSoap11Endpoint();
      if (eventProcessingPlatformWebservicePortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)eventProcessingPlatformWebservicePortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)eventProcessingPlatformWebservicePortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (eventProcessingPlatformWebservicePortType != null)
      ((javax.xml.rpc.Stub)eventProcessingPlatformWebservicePortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public de.hpi.EventProcessingPlatformWebservicePortType getEventProcessingPlatformWebservicePortType() {
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    return eventProcessingPlatformWebservicePortType;
  }
  
  public java.lang.Boolean importEvents(java.lang.String xml) throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    return eventProcessingPlatformWebservicePortType.importEvents(xml);
  }
  
  public java.lang.Boolean registerEventType(java.lang.String xsd, java.lang.String schemaName, java.lang.String timestampName) throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    return eventProcessingPlatformWebservicePortType.registerEventType(xsd, schemaName, timestampName);
  }
  
  public java.lang.String importRoute(java.lang.String xml) throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    return eventProcessingPlatformWebservicePortType.importRoute(xml);
  }
  
  public void unregisterQueryFromQueue(java.lang.String uuid) throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    eventProcessingPlatformWebservicePortType.unregisterQueryFromQueue(uuid);
  }
  
  public void addQueryNotification() throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    eventProcessingPlatformWebservicePortType.addQueryNotification();
  }
  
  public java.lang.Boolean importSemanticEventMapping(java.lang.String mappingContent, java.lang.String associatedEventTypeName) throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    return eventProcessingPlatformWebservicePortType.importSemanticEventMapping(mappingContent, associatedEventTypeName);
  }
  
  public java.lang.String registerQueryForQueue(java.lang.String title, java.lang.String queryString, java.lang.String eMail) throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    return eventProcessingPlatformWebservicePortType.registerQueryForQueue(title, queryString, eMail);
  }
  
  public java.lang.Integer unregisterQueriesFromQueue(java.lang.String email) throws java.rmi.RemoteException{
    if (eventProcessingPlatformWebservicePortType == null)
      _initEventProcessingPlatformWebservicePortTypeProxy();
    return eventProcessingPlatformWebservicePortType.unregisterQueriesFromQueue(email);
  }
  
  
}