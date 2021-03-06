
/**
 * SendPushMessageServiceMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4  Built on : Apr 26, 2008 (06:24:30 EDT)
 */
        package vinaphone.org.csapi.www.wsdl.parlayx.wap_push.send.v1_0.service;

        /**
        *  SendPushMessageServiceMessageReceiverInOut message receiver
        */

        public class SendPushMessageServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        SendPushMessageServiceSkeleton skel = (SendPushMessageServiceSkeleton)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava(op.getName().getLocalPart())) != null)){

        

            if("getPushMessageDeliveryStatus".equals(methodName)){
                
                vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE getPushMessageDeliveryStatusResponse5 = null;
	                        vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusE wrappedParam =
                                                             (vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusE)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusE.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               getPushMessageDeliveryStatusResponse5 =
                                                   
                                                   
                                                         skel.getPushMessageDeliveryStatus(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), getPushMessageDeliveryStatusResponse5, false);
                                    } else 

            if("sendPushMessage".equals(methodName)){
                
                vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE sendPushMessageResponse7 = null;
	                        vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageE wrappedParam =
                                                             (vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageE)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageE.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               sendPushMessageResponse7 =
                                                   
                                                   
                                                         skel.sendPushMessage(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), sendPushMessageResponse7, false);
                                    
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        

        newMsgContext.setEnvelope(envelope);
        }
        } catch (PolicyException e) {

            msgContext.setProperty(org.apache.axis2.Constants.FAULT_NAME,"PolicyException");
            org.apache.axis2.AxisFault f = createAxisFault(e);
            if (e.getFaultMessage() != null){
                f.setDetail(toOM(e.getFaultMessage(),false));
            }
            throw f;
            }
         catch (ServiceException e) {

            msgContext.setProperty(org.apache.axis2.Constants.FAULT_NAME,"ServiceException");
            org.apache.axis2.AxisFault f = createAxisFault(e);
            if (e.getFaultMessage() != null){
                f.setDetail(toOM(e.getFaultMessage(),false));
            }
            throw f;
            }
        
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(vinaphone.org.csapi.www.schema.parlayx.common.v2_1.PolicyExceptionE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.common.v2_1.PolicyExceptionE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(vinaphone.org.csapi.www.schema.parlayx.common.v2_1.ServiceExceptionE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.common.v2_1.ServiceExceptionE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE wrapsendPushMessage(){
                                vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE wrappedElement = new vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE wrapgetPushMessageDeliveryStatus(){
                                vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE wrappedElement = new vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE();
                                return wrappedElement;
                         }
                    


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        
                if (vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.GetPushMessageDeliveryStatusResponseE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (vinaphone.org.csapi.www.schema.parlayx.common.v2_1.PolicyExceptionE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.common.v2_1.PolicyExceptionE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (vinaphone.org.csapi.www.schema.parlayx.common.v2_1.ServiceExceptionE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.common.v2_1.ServiceExceptionE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.wap_push.send.v1_0.local.SendPushMessageResponseE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (vinaphone.org.csapi.www.schema.parlayx.common.v2_1.PolicyExceptionE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.common.v2_1.PolicyExceptionE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (vinaphone.org.csapi.www.schema.parlayx.common.v2_1.ServiceExceptionE.class.equals(type)){
                
                           return vinaphone.org.csapi.www.schema.parlayx.common.v2_1.ServiceExceptionE.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    