package org.palladiosimulator.jdt.extractor.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.jdt.extractor.blackboard.Blackboard;

public class HelperUtil {

//	private static Map<String, String> namedComponents = new HashMap<String, String>();
//	private static List<String> regComponents = new ArrayList<String>();
	
	public static List<Path> lookupFilesWithName(java.net.URI path, String fileName) {
		List<Path> filePaths = new ArrayList<>();
		
		try {			
			Stream<Path> paths = Files.find(Paths.get(path.toString()), Integer.MAX_VALUE, (filePath, fileAttributes) -> fileAttributes.isRegularFile())
			.sorted().filter(filePath -> filePath.getFileName().toString().equals(fileName));
						
			paths.forEach(u -> {
				filePaths.add(u);				
			});
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return filePaths;
	}
	
	public static List<Path> lookupFilesWithExtension(java.net.URI path, String fileExtension) {
		List<Path> filePaths = new ArrayList<>();
		try {
			Stream<Path> paths = Files.find(Paths.get(path), Integer.MAX_VALUE, (filePath, fileAttributes) -> fileAttributes.isRegularFile())
			.sorted().filter(filePath -> filePath.getFileName().toString().endsWith("."+fileExtension));
						
			paths.forEach(u -> {
				filePaths.add(u.getParent());				
			});
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return filePaths;
	}	
	
	public static String createQualifiedName(EList<String> namespaces, String unitName) {
		
		if(namespaces.size()==0) return unitName;
		
		String name = new String();
		for(int i=0; i<namespaces.size(); i++) {
			name += namespaces.get(i);
			name += '.';
		}
		
		name += unitName;
		
		return name;
		
	}
	
//	public static boolean isRestMethod(String annotationName) {
//		if(annotationName.equalsIgnoreCase("post") || annotationName.equalsIgnoreCase("get") || annotationName.equalsIgnoreCase("put") 
//				|| annotationName.equalsIgnoreCase("delete") || annotationName.equalsIgnoreCase("patch") || annotationName.equalsIgnoreCase("requestmapping") 
//				|| annotationName.equalsIgnoreCase("postmapping") || annotationName.equalsIgnoreCase("getmapping") || annotationName.equalsIgnoreCase("putmapping") 
//				|| annotationName.equalsIgnoreCase("deletemapping") || annotationName.equalsIgnoreCase("patchmapping")) {
//			return true;
//		}		
//		return false;
//	}
	
	
//	public static String getExtractedComponentByName(Blackboard blackboard, String name) {
//		
//		List<String> component = new ArrayList<String>();
//		
//		blackboard.getExtractedComponents().keySet().forEach(comp -> {
//			if(comp.toLowerCase().contains(name) || name.contains(comp.toLowerCase())) {
//				component.add(comp);
//			}
//		});
//		if(component.size()>0) {
//			return component.get(0);
//		}
//		return null;
//	}
//	
//	public static String getRegisteredComponentByName(Blackboard blackboard, String name) {
//		
//		List<String> component = new ArrayList<String>();
//		
//		blackboard.getRegisteredComponents().forEach(comp -> {
//			if(comp.toLowerCase().contains(name) || name.contains(comp.toLowerCase())) {
//				component.add(comp);
//			}
//		});
//		if(component.size()>0) {
//			return component.get(0);
//		}
//		return null;
//	}
//	
//	public static String getComponentName(String packageName) {
//		List<String> name = new ArrayList<String>();
//		namedComponents.entrySet().stream().forEach(entry -> {
//			if(packageName.contains(entry.getValue())) {
//				name.add(entry.getKey());
//			}
//		});
//		if(name.size()>0) {
//			return name.get(0);
//		}
//		else {
//			regComponents.forEach(comp -> {
//				if(comp.contains(packageName) || packageName.contains(comp)) {
//					name.add(comp);
//				}
//			});
//			if(name.size()>0) {
//				return name.get(0);
//			}
//		}
//		return null;
//	}
	
//	public static void addComponent(String name, String value) {
//		namedComponents.put(name, value);
//	}
	
//	public static String createQualifiedName(EList<String> namespaces) {
//		String name = new String();
//		for(int i=0; i<namespaces.size()-1; i++) {
//			name += namespaces.get(i);
//			name += '.';
//		}
//		name += namespaces.get(namespaces.size()-1);
//		
//		return name;
//		
//	}
	
	
	
//	public static void printComponents() {
//		namedComponents.entrySet().stream().forEach(c -> {
//			System.out.println("named: " + c.getKey() + " (" + c.getValue() + ")");
//		});
//		ComponentFinder.getComponents().stream().forEach(c -> System.out.println("registered: " + c));
////		technicalDependencies.entrySet().stream().forEach(c -> {
////			System.out.println("dependency: "+c.getKey()+ " --> " + c.getValue());
////		});
//	}
//	
//	public static void printDependencies() {
//		DependencyFinder.getTechnicalDependencies().entrySet().stream().forEach(c -> {
//			System.out.println("dependency (T): "+c.getKey()+ " --> " + c.getValue());
//		});
//		DependencyFinder.getLogicalDependencies().entrySet().stream().forEach(c -> {
//			System.out.println("dependency (L): "+c.getKey()+ " --> " + c.getValue());
//		});
//	}
//	
//	public static void printInterfaces() {
//		InterfaceFinder.getInterfaces().entrySet().stream().forEach(c -> {
//			System.out.println("Interface: "+c.getKey()+ " --> " + c.getValue());
//		});
//	}
	
	
	
//	public static boolean isSpringMethod(String annotationName) {
//		if(annotationName.equalsIgnoreCase("requestmapping")) {
//			return true;
//		}
//		return false;
//	}

//	public static List<String> checkForDuplicates(List<String> registeredComponents) {
//
//		List<String> packageNames = new ArrayList<String>();
//		
////		String[] newComponents = (String[]) registeredComponents.toArray();
////		
////		for(int i = 0; i < newComponents.length; i++) {
////			if(newComponents[i].contains(".")) {
////				
////			}
////		}
//		
//		registeredComponents.forEach(name -> {
//			if(name.contains(".")) {
//				packageNames.add(name);
//			}			
//		});
//		
//		registeredComponents.removeAll(packageNames);		
//		
//		int prefix = getCommonPrefix(packageNames);
//		
//		packageNames.forEach(name -> {
//			String newName = name.substring(prefix);
//			if(newName.contains(".")) {
//				newName = newName.split("\\.")[0];
//			}
//			registeredComponents.add(newName);
//		});		
//		
//		regComponents = registeredComponents;
//		
//		return registeredComponents;
//	}
	
//	public static int getCommonPrefix(List<String> names){
//		
//		if(names.size() < 2) {
//			return 0;
//		}
//		
//		int minCounter = Integer.MAX_VALUE;
//		int length = Integer.MAX_VALUE, i=0, counter=0;
//		
//		for(String str : names){
//			if(length > str.length()){
//				length = str.length();
//				counter=i;
//			}
//			i++;
//		}
//		
//		char [] chars =  names.get(counter).toCharArray();
//		
//		for(i=0;i<names.size();i++){
//			String str = names.get(i);
//			int len = str.length();
//			int j;
//			for(j=0; j<len && j<chars.length && chars[j]==str.charAt(j); j++);
//			if(j < minCounter)
//				minCounter = j;
//		}
//		return minCounter;
////		return names.get(0).substring(0, minCounter);	
//	}
	
//	public static void main(String[] args) {
//		List<String> test = new ArrayList<String>();
//		test.add("com.example.teddy");
//		test.add("com.example.bar.foo");
//		test.add("com.example.auto");
//		checkForDuplicates(test).forEach(e -> {
//			System.out.println(e);
//		});
//	}
}
