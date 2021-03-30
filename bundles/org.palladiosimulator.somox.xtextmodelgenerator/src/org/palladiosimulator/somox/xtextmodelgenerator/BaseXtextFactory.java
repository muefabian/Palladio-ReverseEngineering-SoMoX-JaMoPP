package org.palladiosimulator.somox.xtextmodelgenerator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public abstract class BaseXtextFactory {
	
	public static void writeAndNormalizeEOL(String input, String output) {
		try {
			FileInputStream fis = new FileInputStream(input);
		    FileOutputStream fos = new FileOutputStream(output);
			String content = IOUtils.toString(fis, Charset.defaultCharset());
		    content = content.replaceAll("\\r\\n?", "\n"); // Unix: \n , Windows: \r\n , Mac: \r
		    IOUtils.write(content, new FileOutputStream(output), Charset.defaultCharset());
		    fis.close();
		    fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}
	
	
	public static void main(String[] args) throws IOException {
		
	}
	
	public static Resource createAndAddResource(final String outputFile, final String[] fileextensions,
            final ResourceSet rs) {
        for (final String fileext : fileextensions) {
            rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileext, new XMIResourceFactoryImpl());
        }
        final URI uri = URI.createFileURI(outputFile);
        final Resource resource = rs.createResource(uri);
        ((ResourceImpl) resource).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
        return resource;
    }
	
	public static void saveResource(final Resource resource) {
        final Map<Object, Object> saveOptions = ((XMLResource) resource).getDefaultSaveOptions();
        saveOptions.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<>());
        try {
            resource.save(saveOptions);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }	

}
