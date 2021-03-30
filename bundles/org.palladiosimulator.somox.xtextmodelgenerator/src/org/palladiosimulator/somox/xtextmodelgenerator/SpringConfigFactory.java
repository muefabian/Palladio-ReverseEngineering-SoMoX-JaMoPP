package org.palladiosimulator.somox.xtextmodelgenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.palladiosimulator.docker.DockerFileStandaloneSetup;

import com.google.inject.Injector;

public class SpringConfigFactory {

	//	final static String OUT = "./springconfig.xmi";
	
	private static Injector injector;
	private static ResourceSet rs;
	
	private static void setUp() {
		injector = new DockerFileStandaloneSetup().createInjectorAndDoEMFRegistration();
		rs = injector.getInstance(ResourceSet.class);
	}
	
	public static Resource createXMI(String[] inputfiles, String outputfile) {
		
		setUp();
		
		for(int i = 0; i < inputfiles.length; i++) {
			
			Path tempFile = null;			
			try {
				tempFile = Files.createTempFile("tempfiles", ".yamlFile");
				System.out.println(tempFile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				BaseXtextFactory.writeAndNormalizeEOL(inputfiles[i], tempFile.toString());
				rs.getResource(URI.createFileURI(tempFile.toString()), true);
//				Files.delete(tempFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Resource xmiResource = BaseXtextFactory.createAndAddResource(outputfile, new String[] {"yamlFile"}, rs);

		for (Resource res : rs.getResources()) {
			xmiResource.getContents().add(res.getContents().get(0));
		}
		
		BaseXtextFactory.saveResource(xmiResource);
		
		return xmiResource;
	}
}
