/**
 * EventProcessingPlatformWebservicePortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.hpi;

public interface EventProcessingPlatformWebservicePortType extends java.rmi.Remote {
    public java.lang.Boolean importEvents(java.lang.String xml) throws java.rmi.RemoteException;
    public java.lang.Boolean registerEventType(java.lang.String xsd, java.lang.String schemaName, java.lang.String timestampName) throws java.rmi.RemoteException;
    public java.lang.String importRoute(java.lang.String xml) throws java.rmi.RemoteException;
    public void unregisterQueryFromQueue(java.lang.String uuid) throws java.rmi.RemoteException;
    public void addQueryNotification() throws java.rmi.RemoteException;
    public java.lang.Boolean importSemanticEventMapping(java.lang.String mappingContent, java.lang.String associatedEventTypeName) throws java.rmi.RemoteException;
    public java.lang.String registerQueryForQueue(java.lang.String title, java.lang.String queryString, java.lang.String eMail) throws java.rmi.RemoteException;
    public java.lang.Integer unregisterQueriesFromQueue(java.lang.String email) throws java.rmi.RemoteException;
}
