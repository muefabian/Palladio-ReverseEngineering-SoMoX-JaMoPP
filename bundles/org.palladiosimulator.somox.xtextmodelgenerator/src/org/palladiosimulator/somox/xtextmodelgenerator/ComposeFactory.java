package org.palladiosimulator.somox.xtextmodelgenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.palladiosimulator.somox.docker.compose.ComposeFileStandaloneSetup;

import com.google.inject.Injector;

public class ComposeFactory {
	
//	final static String OUT = "./dockermodel.xmi";
	
	private static Injector injector;
	private static ResourceSet rs;
	
	private static void setUp() {
		injector = new ComposeFileStandaloneSetup().createInjectorAndDoEMFRegistration();
		rs = injector.getInstance(ResourceSet.class);
	}
	
	public static Resource createXMI(String[] inputfiles, String outputfile) {
		
		setUp();
		
		for(int i = 0; i < inputfiles.length; i++) {
			
			Path tempFile = null;			
			try {
				tempFile = Files.createTempFile("tempfiles", ".compose");
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
		
		Resource xmiResource = BaseXtextFactory.createAndAddResource(outputfile, new String[] {"compose"}, rs);

		for (Resource res : rs.getResources()) {
			xmiResource.getContents().add(res.getContents().get(0));
		}
		
		BaseXtextFactory.saveResource(xmiResource);
		
		return xmiResource;
	}

}
