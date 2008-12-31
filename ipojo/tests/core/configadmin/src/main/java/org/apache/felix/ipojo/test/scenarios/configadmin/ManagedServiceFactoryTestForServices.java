package org.apache.felix.ipojo.test.scenarios.configadmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.PrimitiveInstanceDescription;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.junit4osgi.OSGiTestCase;
import org.apache.felix.ipojo.test.scenarios.configadmin.service.FooService;
import org.apache.felix.ipojo.test.scenarios.util.Utils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ManagedServiceFactoryTestForServices extends OSGiTestCase {
    
    private ComponentFactory factory;
    private ConfigurationAdmin admin;
    
    public void setUp() {
        factory = (ComponentFactory) Utils.getFactoryByName(getContext(), "CA-ConfigurableProvider");
        admin = (ConfigurationAdmin) Utils.getServiceObject(getContext(), ConfigurationAdmin.class.getName(), null);
        assertNotNull("Check configuration admin availability", admin);
        try {
            Configuration[] configurations = admin.listConfigurations("(service.factoryPid=CA-ConfigurableProvider)");
            for (int i = 0; configurations != null && i < configurations.length; i++) {
                configurations[i].delete();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void tearDown() {
        try {
            Configuration[] configurations = admin.listConfigurations("(service.factoryPid=CA-ConfigurableProvider)");
            for (int i = 0; configurations != null && i < configurations.length; i++) {
                configurations[i].delete();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        admin = null;

        
    }
    
    public void testCreationAndReconfiguration() {
        Configuration configuration = null;
        try {
            configuration = admin.createFactoryConfiguration("CA-ConfigurableProvider", null);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        Dictionary props = configuration.getProperties();
        if(props == null) {
            props = new Properties();
        }
        props.put("message", "message");
        
        try {
            configuration.update(props);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        
        String pid = configuration.getPid();
        System.out.println("PID : " + pid);
        
        //  The instance should be created, wait for the architecture service
        Utils.waitForService(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        Architecture architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Check no object", 0, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);        
        
        Utils.waitForService(getContext(), FooService.class.getName(), "(instance.name="+pid+")");       
        
        ServiceReference refx = Utils.getServiceReference(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        assertNotNull("Check refx", refx);
        FooService fs = (FooService) Utils.getServiceObject(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        assertNotNull("Check fs", fs);
        
        Properties p = fs.fooProps();
        String mes = p.getProperty("message");
        int count = ((Integer) p.get("count")).intValue();
        //architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Assert Message", "message", mes);
        assertEquals("Assert count", 1, count);
        assertEquals("Check 1 object", 1, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
        
        // Wait for the processing of the first configuration.
        try {
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (InterruptedException e1) {
            fail(e1.getMessage());
        }
        
        
        Dictionary p2 = configuration.getProperties();
        p2.put("message", "message2");
        try {
            System.err.println("The configuration will be updated with message2");

            configuration.update(p2);
            // Update the configuration ...
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        System.err.println("The configuration should be updated with message2");
        
        fs = (FooService) Utils.getServiceObject(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        p = fs.fooProps();
        mes = p.getProperty("message");
        count = ((Integer) p.get("count")).intValue();
        //architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Assert Message", "message2", mes);
        assertEquals("Assert count", 2, count);
        assertEquals("Check 1 object", 1, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
        try {
            configuration.delete();
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        ServiceReference ref = Utils.getServiceReference(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        assertNull("Check unavailability", ref);
    }
    
    public void testCreationAndReconfiguration2() {
        //The reconfiguration happens before the service invocation
        Configuration configuration = null;
        try {
            configuration = admin.createFactoryConfiguration("CA-ConfigurableProvider", null);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        Dictionary props = configuration.getProperties();
        if(props == null) {
            props = new Properties();
        }
        props.put("message", "message");
        
        try {
            configuration.update(props);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        
        String pid = configuration.getPid();
        
        //  The instance should be created, wait for the architecture service
        Utils.waitForService(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        Architecture architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Check no object", 0, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
        // Wait for the processing of the first configuration.
        try {
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (InterruptedException e1) {
            fail(e1.getMessage());
        }
        
        props.put("message", "message2");
        try {
            configuration.update(props);
            // Update the configuration ...
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
       // architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Check no object -2", 0, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
        //Invoke
        FooService fs = (FooService) Utils.getServiceObject(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        Properties p = fs.fooProps();
        String mes = p.getProperty("message");
        int count = ((Integer) p.get("count")).intValue();
        //architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Assert Message", "message2", mes);
        assertEquals("Assert count", 1, count);
        assertEquals("Check 1 object", 1, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
                
        try {
            configuration.delete();
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        ServiceReference ref = Utils.getServiceReference(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        assertNull("Check unavailability", ref);
    }
    
    public void testDelayedCreationAndReconfiguration() {
        factory.stop();
        Configuration configuration = null;
        try {
            configuration = admin.createFactoryConfiguration("CA-ConfigurableProvider", null);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        Dictionary props = configuration.getProperties();
        if(props == null) {
            props = new Properties();
        }
        props.put("message", "message");
        
        try {
            configuration.update(props);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        
        String pid = configuration.getPid();
        
        assertNull("check no instance", Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")"));
        
        factory.start();
        
        
        //  The instance should be created, wait for the architecture service
        Utils.waitForService(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        Architecture architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Check no object", 0, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
        Utils.waitForService(getContext(), FooService.class.getName(), "(instance.name="+pid+")");       
        FooService fs = (FooService) Utils.getServiceObject(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        
        Properties p = fs.fooProps();
        String mes = p.getProperty("message");
        int count = ((Integer) p.get("count")).intValue();
       // architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Assert Message", "message", mes);
        assertEquals("Assert count", 1, count);
        assertEquals("Check 1 object", 1, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
     // Wait for the processing of the first configuration.
        try {
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (InterruptedException e1) {
            fail(e1.getMessage());
        }
        
        System.out.println("===");
        
        props.put("message", "message2");
        try {
            configuration.update(props);
            // Update the configuration ...
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        System.out.println("===");
        
        fs = (FooService) Utils.getServiceObject(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        p = fs.fooProps();
        mes = p.getProperty("message");
        count = ((Integer) p.get("count")).intValue();
       // architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Assert Message", "message2", mes);
        assertEquals("Assert count", 2, count);
        assertEquals("Check 1 object", 1, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
        try {
            configuration.delete();
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        ServiceReference ref = Utils.getServiceReference(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        assertNull("Check unavailability", ref);
    }
    
    public void testDelayedCreationAndReconfiguration2() {
        factory.stop();
        //The reconfiguration happens before the service invocation
        Configuration configuration = null;
        try {
            configuration = admin.createFactoryConfiguration("CA-ConfigurableProvider", null);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        Dictionary props = configuration.getProperties();
        if(props == null) {
            props = new Properties();
        }
        props.put("message", "message");
        
        try {
            configuration.update(props);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        
        String pid = configuration.getPid();
        System.out.println("PID : " + pid);
        
        assertNull("check no instance", Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")"));
        
        factory.start();
        
        
        //  The instance should be created, wait for the architecture service
        Utils.waitForService(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        Architecture architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Check no object", 0, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
     // Wait for the processing of the first configuration.
        try {
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (InterruptedException e1) {
            fail(e1.getMessage());
        }
        
        props.put("message", "message2");
        try {
            configuration.update(props);
            // Update the configuration ...
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        //architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Check no object -2", 0, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
        
        //Invoke
        FooService fs = (FooService) Utils.getServiceObject(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        Properties p = fs.fooProps();
        String mes = p.getProperty("message");
        int count = ((Integer) p.get("count")).intValue();
       // architecture = (Architecture) Utils.getServiceObject(getContext(), Architecture.class.getName(), "(architecture.instance="+pid+")");
        
        assertEquals("Assert Message", "message2", mes);
        assertEquals("Assert count", 1, count);
        assertEquals("Check 1 object", 1, ((PrimitiveInstanceDescription) architecture.getInstanceDescription()).getCreatedObjects().length);
                
        try {
            configuration.delete();
            Thread.sleep(ConfigurationTestSuite.UPDATE_WAIT_TIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        ServiceReference ref = Utils.getServiceReference(getContext(), FooService.class.getName(), "(instance.name=" + pid + ")");
        assertNull("Check unavailability", ref);
    }
    
    

}
