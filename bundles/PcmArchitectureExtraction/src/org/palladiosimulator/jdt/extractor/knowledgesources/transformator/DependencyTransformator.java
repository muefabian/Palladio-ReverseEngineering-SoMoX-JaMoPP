package org.palladiosimulator.jdt.extractor.knowledgesources.transformator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.impl.ConcreteClassifierImpl;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.emftext.language.java.imports.Import;
import org.emftext.language.java.members.ClassMethod;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.impl.MethodImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.jdt.extractor.model.ExtractedComponent;
import org.palladiosimulator.jdt.extractor.util.ResolverUtil;
import org.palladiosimulator.jdt.extractor.util.HelperUtil;
import org.palladiosimulator.jdt.extractor.util.ParserType;
import org.palladiosimulator.somox.yaml.yamlFile.impl.YamlModelImpl;

public class DependencyTransformator extends KnowledgeSourceBase {
	
	private boolean logical;
	private String[] logicalMethodNames;
	private ResolverUtil resolverUtil;

	
	public void configure(ParserType type, boolean logical, String[] logicalMethodNames) {
		this.logical = logical;
		this.logicalMethodNames = logicalMethodNames;
		resolverUtil = new ResolverUtil(blackboard, type);
	}

	@Override
	public void execute() {
		
		addManualServiceDiscovery();
		
		addAnnotationDependencies();
		
		addTurbineDependencies();
		
		addZuulDependencies();

		// TODO: change to use blackboard.allClassifiers
		for(EObject obj : blackboard.getResource().getContents()) {
			if(!(obj instanceof CompilationUnitImpl)) continue;
			CompilationUnitImpl compUnit = (CompilationUnitImpl) obj;
			String component = blackboard.getComponentFromClass(HelperUtil.createQualifiedName(compUnit.getNamespaces(), compUnit.getName()));
			for (Import imp: ((CompilationUnitImpl) compUnit).getImports()) {
				
				//TODO: find component by package (substring)
				String importName = HelperUtil.createQualifiedName(imp.getNamespaces(), "");
				String dependency = blackboard.getComponentFromClass(importName);
				if(dependency != null && !(dependency.equals(component))) {
					addDependency(component, dependency);
				}
				
//				if(logical) {
//					if(imp instanceof ClassifierImport) {
//						ClassifierImport classImp = (ClassifierImport) imp;
//						String className = classImp.getClassifier().getName();
//						
//						if(className == null) continue;
//						
//						switch(className) {
//							case "LoadBalancedCRUDOperations":
//								addLogicalDependency(component, "PERSISTENCE");
//								break;
////							case "LoadBalancedImageOperations":
////								addLogicalDependency(component, "IMAGE");
////								break;
////							case "LoadBalancedRecommenderOperations":
////								addLogicalDependency(component, "RECOMMENDER");
////								break;
////							case "LoadBalancedStoreOperations":
////								addLogicalDependency(component, "AUTH");
////								break;
//						}
//					}
//				}
			}
		}
		
		if(logical) {
//			generateLogicalDependencyMap(new String[] {"loadBalanceRESTOperation", "multicastRESTOperation"});
			generateLogicalDependencyMap(logicalMethodNames);
		}
		
		if(blackboard.getMissingComponents().size() > 0) {
			
			//TODO: resolve methods properly
			
//			Map<String, List<MethodImpl>> methodNames = new HashMap<String, List<MethodImpl>>();			
			
			blackboard.getMissingComponents().entrySet().forEach(misComp -> {
//				methodNames.put(misComp.getKey(), new ArrayList<MethodImpl>());
				
				
				// resolve missing dependencies
				for(EObject obj : blackboard.getResource().getContents()) {
					if(!(obj instanceof CompilationUnitImpl)) continue;
					CompilationUnitImpl compUnit = (CompilationUnitImpl) obj;
					
					for(ConcreteClassifier classifier: compUnit.getClassifiers()) {
						for(Member member: classifier.getMembers()) {
							if(member instanceof ClassMethod) {
								ClassMethod method = (ClassMethod) member;
								resolverUtil.resolveStatementsToClass(method, method.getStatements(), misComp.getValue());
							}
						}
					}
				}
				
				
				// add required roles
				blackboard.getMissingDependencies().entrySet().forEach(misDep -> {
//					System.out.println("DEPENDENCY: " + misDep.getKey() + " -> " + misComp.getKey());
					for(String dependency : misDep.getValue()) {						
						if(misComp.getValue().equals(dependency)) {
							addDependency(misDep.getKey(), misComp.getKey());
						}
						
//						String className = method.getName().split("\\.")[0];
////						String methodName = method.getName().split("\\.")[1];
//						if(misComp.getValue().equals(className)) {
//							if(!methodNames.get(misComp.getKey()).contains(method)) {
//								methodNames.get(misComp.getKey()).add(method);
//							}
//							addDependency(misDep.getKey(), misComp.getKey());
//						}
					}
				});				
			});
			
			
			// add method names to extracted interface	
			blackboard.getMissingInterfaceMethods().entrySet().forEach(entry -> {
				blackboard.getMissingComponents().entrySet().forEach(misComp -> {
					if(misComp.getValue().equals(entry.getKey())) {
						for(MethodImpl method : entry.getValue()) {
							blackboard.getInterfaceExtractor().addInterfaceMethod(misComp.getKey(), method);
						}
					}
				});
//				for(MethodImpl method : entry.getValue()) {
//					blackboard.getInterfaceExtractor().addInterfaceMethod(entry.getKey(), method);
//				}				
			});
			
			blackboard.getMissingComponents().clear();
		}		
		
	}
	
	private void addManualServiceDiscovery() {
		
		String registry = "";
		String registerMethod = "";
		
		Properties prop = new Properties();
		try {
			String config = "config.properties";
			InputStream stream = getClass().getClassLoader().getResourceAsStream(config);
					
			if(stream != null) {
				prop.load(stream);
			}
			else {
				System.out.println("Couldn't find properties file: " + config);
			}			
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		if(prop.containsKey("serviceRegistry")) {
			registry = prop.getProperty("serviceRegistry");			
		} 
		else {
			return;
		}
		
		if(prop.containsKey("registerMethod")) {
			registerMethod = prop.getProperty("registerMethod");
		}
		else {
			return;
		}
		
		List<String> discoveryClients = new ArrayList<>();
		
		for(ConcreteClassifierImpl concreteClass : blackboard.getAllClassifiers().values()) {
			for(Member member : concreteClass.getMembers()) {
				if(member instanceof MethodImpl) {
					if(member.getName().equals(registerMethod)) {
						String componentName = blackboard.getComponentFromClass(concreteClass.getQualifiedName());
						if(componentName!=null) {
							discoveryClients.add(componentName);
						}						
					}
				}
			}			
		}
		
		for(String component : discoveryClients) {
			addDependency(component, registry);
		}
	}
	
	
	private void addAnnotationDependencies() {

		List<String> discoveryClients = new ArrayList<>();
		String discoveryServer = null;
		
		for(ExtractedComponent component : blackboard.getExtractedComponents().values()) {
			if(component.getAnnotations().contains("EnableDiscoveryClient") || component.getAnnotations().contains("EnableEurekaClient")) {
				discoveryClients.add(component.getName());
			}
			
			if(component.getAnnotations().contains("EnableEurekaServer")) {
				discoveryServer = component.getName();
			}
		}
		
		if(discoveryServer == null) return;
		
		for(String component : discoveryClients) {
			addDependency(component, discoveryServer);
		}
	}
	
	private void addTurbineDependencies() {
		
		for(ExtractedComponent component : blackboard.getExtractedComponents().values()) {
			if(component.getAnnotations().contains("EnableTurbine")) {
				
				if(blackboard.getSpringConfigBootstrapYaml().containsKey(component.getName())) {
					YamlModelImpl config = blackboard.getSpringConfigBootstrapYaml().get(component.getName());
					
					if(config.getTurbine() != null && config.getTurbine().getAppConfig() != null) {						
						String[] serviceNames = config.getTurbine().getAppConfig().split(",");
						
						for(String service : serviceNames) {						
							String dependency = blackboard.getComponentFromApplicationName(service);
							component.getRequires().put(dependency, blackboard.getExtractedInterface(dependency));
						}
					}
				}
				
				if(blackboard.getSpringConfigApplicationYaml().containsKey(component.getName())) {
					YamlModelImpl config = blackboard.getSpringConfigApplicationYaml().get(component.getName());
					
					if(config.getTurbine() != null && config.getTurbine().getAppConfig() != null) {						
						String[] serviceNames = config.getTurbine().getAppConfig().split(",");
						
						for(String service : serviceNames) {						
							String dependency = blackboard.getComponentFromApplicationName(service);
							component.getRequires().put(dependency, blackboard.getExtractedInterface(dependency));
						}
					}					
				}
				
				if(blackboard.getSpringConfigBootstrapProperties().containsKey(component.getName())) {
					Properties config = blackboard.getSpringConfigBootstrapProperties().get(component.getName());
					String[] serviceNames = config.getProperty("turbine.appConfig").split(",");
					
					if(serviceNames != null) {
						for(String service : serviceNames) {						
							String dependency = blackboard.getComponentFromApplicationName(service);
							component.getRequires().put(dependency, blackboard.getExtractedInterface(dependency));
						}
					}					
				}
				
				if(blackboard.getSpringConfigApplicationProperties().containsKey(component.getName())) {
					Properties config = blackboard.getSpringConfigApplicationProperties().get(component.getName());
					String[] serviceNames = config.getProperty("turbine.appConfig").split(",");
					
					if(serviceNames != null) {
						for(String service : serviceNames) {						
							String dependency = blackboard.getComponentFromApplicationName(service);
							component.getRequires().put(dependency, blackboard.getExtractedInterface(dependency));
						}
					}
				}				
			}
		}
	}
	
	private void addZuulDependencies() {		
	
		for(ExtractedComponent component : blackboard.getExtractedComponents().values()) {
			if(component.getAnnotations().contains("EnableZuulProxy")) {
				
				for(ExtractedComponent dependencyComponent : blackboard.getExtractedComponents().values()) {
					if(blackboard.getSpringConfigBootstrapProperties().containsKey(dependencyComponent.getName())) {
						Properties config = blackboard.getSpringConfigBootstrapProperties().get(dependencyComponent.getName());
						if(config.containsKey("management.endpoints.web.exposure.include")) {
							component.getRequires().put(dependencyComponent.getName(), blackboard.getExtractedInterface(dependencyComponent.getName()));
						}						
					}
					
					if(blackboard.getSpringConfigApplicationProperties().containsKey(dependencyComponent.getName())) {
						Properties config = blackboard.getSpringConfigApplicationProperties().get(dependencyComponent.getName());
						if(config.containsKey("management.endpoints.web.exposure.include")) {
							component.getRequires().put(dependencyComponent.getName(), blackboard.getExtractedInterface(dependencyComponent.getName()));
						}						
					}
				}
			}
		}
	}
	

	public void generateLogicalDependencyMap(String[] methodNames) {
		
		generateMethodDependencyMap(methodNames);
		
		for(EObject obj : blackboard.getResource().getContents()) {
			if(!(obj instanceof CompilationUnitImpl)) continue;
			CompilationUnitImpl compUnit = (CompilationUnitImpl) obj;
			
			for(ConcreteClassifier classifier: compUnit.getClassifiers()) {
				for(Member member: classifier.getMembers()) {
					if(member instanceof ClassMethod) {
						ClassMethod method = (ClassMethod) member;
						resolverUtil.resolveStatementsToMethod(method, method.getStatements(), blackboard.getMethodDependencies().keySet().toArray(new String[blackboard.getMethodDependencies().keySet().size()]), true);
					}
				}
			}
		}
	}
	
	private void generateMethodDependencyMap(String[] methodNames) {
		int length = blackboard.getMethodDependencies().size();
		
		for(EObject obj : blackboard.getResource().getContents()) {
			if(!(obj instanceof CompilationUnitImpl)) continue;
			CompilationUnitImpl compUnit = (CompilationUnitImpl) obj;
			
			for(ConcreteClassifier classifier: compUnit.getClassifiers()) {
				for(Member member: classifier.getMembers()) {
					if(member instanceof ClassMethod) {
						ClassMethod method = (ClassMethod) member;
						resolverUtil.resolveStatementsToMethod(method, method.getStatements(), methodNames, false);
					}
				}
			}
		}
		if(length < blackboard.getMethodDependencies().size()) {
			generateMethodDependencyMap(methodNames);
		}
		
	}
	
	private void addDependency(String component, String dependency) {
		System.out.println();
		if(!blackboard.getExtractedComponent(component).getRequires().containsKey(dependency)) {
			blackboard.getExtractedComponent(component).getRequires().put(dependency, blackboard.getExtractedInterface(dependency));
		}		
	}
	
//	public void resolveStatements(ClassMethod member, EList<Statement> statements, String[] methodNames, boolean finish, String className) {
//		
//		//TODO: add for, while, return, ...		
//		for(Statement statement : statements) {
//			if(statement instanceof BlockImpl) {
//				resolveStatements(member, ((BlockImpl)statement).getStatements(), methodNames, finish, className);
//			}
//			else if(statement instanceof ExpressionStatement) {
//				Expression exp = ((ExpressionStatement)statement).getExpression();
//				resolveExpression(member, exp, methodNames, finish, className);				
//			}
//			else if(statement instanceof LocalVariableStatement) {
//				Expression exp = ((LocalVariableStatement)statement).getVariable().getInitialValue();
//				resolveExpression(member, exp, methodNames, finish, className);
//			}
//			else if(statement instanceof TryBlock) {
//				resolveStatements(member, ((TryBlock)statement).getBlock().getStatements(), methodNames, finish, className);
//			}
//			else if(statement instanceof Condition) {
//				
//				EList<Statement> statementList = new BasicEList<Statement>();				
//				resolveExpression(member, ((Condition)statement).getCondition(), methodNames, finish, className);
//				statementList.add(((Condition)statement).getStatement());
//				statementList.add(((Condition)statement).getElseStatement());				
//				resolveStatements(member, statementList, methodNames, finish, className);
//			}
//			else if(statement instanceof ForEachLoop) {
//				EList<Statement> statementList = new BasicEList<Statement>();				
//				statementList.add(((ForEachLoop)statement).getStatement());
//				resolveStatements(member, statementList, methodNames, finish, className);
//			}
//			else if(statement instanceof Return) {				
//				resolveExpression(member, ((Return)statement).getReturnValue(), methodNames, finish, className);
//			}
//		}
//	}
//	
//	private void resolveExpression(ClassMethod method, Expression exp, String[] methodNames, boolean finish, String className) {
//		if(exp instanceof MethodCallImpl) {
//			resolveMethodCall(method, (MethodCallImpl) exp, null, methodNames, finish, className);
//		}
//		if(exp instanceof UnaryExpressionImpl) {
//			UnaryExpressionImpl unExp = (UnaryExpressionImpl) exp;
//			resolveExpression(method, unExp.getChild(), methodNames, finish, className);
//		}
//		if(exp instanceof IdentifierReferenceImpl) {	
//			IdentifierReferenceImpl callIdRef = (IdentifierReferenceImpl) exp;			
//			
//			if(callIdRef.getNext() instanceof MethodCallImpl) {
//				MethodCallImpl methodCall = (MethodCallImpl) callIdRef.getNext();
//				for(Expression callArg : methodCall.getArguments()) {
//					if(callArg instanceof IdentifierReferenceImpl) {
//						if(((IdentifierReferenceImpl) callArg).getNext() instanceof MethodCallImpl) {
//							MethodCallImpl argMethodCall = (MethodCallImpl) ((IdentifierReferenceImpl) callArg).getNext();
//							resolveMethodCall(method, argMethodCall, callIdRef, methodNames, finish, className);
//						}
//					}
//				}				
//				resolveMethodCall(method, methodCall, callIdRef, methodNames, finish, className);
//				
//				//check for method
////				List<String> methodList = Arrays.asList(methodNames);
////				if(methodList.contains(methodCall.getTarget().getName())) {
////					ConcreteClassifier parentClass = method.getParentConcreteClassifier();
////					addLogicalDependency(Util.getComponentName(parentClass.getQualifiedName()), methodDependencies.get(methodCall.getTarget().getName()));
////					
////					System.out.println("YAAAAY " + Util.getComponentName(parentClass.getQualifiedName()) + " . " + methodCall.getTarget().getName() + " -> " + methodDependencies.get(methodCall.getTarget().getName()));
////				}					
//			}
//		}	
//	}
//	
//	
//	private void resolveMethodCall(ClassMethod method, MethodCallImpl methodCall, IdentifierReferenceImpl callingClass, String[] methodNames, boolean finish, String className) {
//
//		String fullMethodCall = "";
//		if(callingClass == null) {
//			fullMethodCall = method.getParentConcreteClassifier().getName() + "." + methodCall.getTarget().getName();
//		}
//		else {
//			// get name of the calling class from variable
//			if(callingClass.getTarget() instanceof LocalVariableImpl) {
//				try {
//					ConcreteClassifierImpl classRef = (ConcreteClassifierImpl) ((LocalVariableImpl)callingClass.getTarget()).getTypeReference().getTarget();
//					fullMethodCall = classRef.getName() + "." + methodCall.getTarget().getName();
//				}
//				catch(Exception e) {						
//				}					
//			}
//			else {					
//				fullMethodCall = callingClass.getTarget().getName() + "." + methodCall.getTarget().getName();
//			}
//		}
//		
//		if(className != null) {
//			if(fullMethodCall.split("\\.")[0].equalsIgnoreCase(className)) {
//				if(blackboard.getMissingDependencies().containsKey(Util.getComponentName(method.getParentConcreteClassifier().getQualifiedName()))) {
////					System.out.println("BLAAAAAAH "+methodCall.getTarget().getClass() + " (" + methodCall.getTarget().getName() + ")");
//					blackboard.getMissingDependencies().get(Util.getComponentName(method.getParentConcreteClassifier().getQualifiedName())).add(fullMethodCall);
//				}
//				else {
//					List<String> newList = new ArrayList<String>();
//					newList.add(fullMethodCall);
////					System.out.println("BLAAAAAAH "+methodCall.getTarget().getClass() + " (" + methodCall.getTarget().getName() + ")");
//					blackboard.getMissingDependencies().put(Util.getComponentName(method.getParentConcreteClassifier().getQualifiedName()), newList);
//				}
//			}
//			return;
//		}
//		
//		if(!finish) {
//			List<String> methodList = Arrays.asList(methodNames);
//			if(methodList.contains(methodCall.getTarget().getName())) {
//				for(Expression callArg : methodCall.getArguments()) {
//					if(callArg instanceof IdentifierReferenceImpl) {
//						//	check for target component (TODO: lookup class Service)
//						if(((IdentifierReferenceImpl) callArg).getTarget().getName().equals("Service")) {
//							IdentifierReferenceImpl refImpl = (IdentifierReferenceImpl) ((IdentifierReferenceImpl)callArg).getNext();
//							if(refImpl.getTarget() instanceof EnumConstantImpl) {
////								System.out.println(method.getParentConcreteClassifier().getName() + "." + method.getName() + " -> " + refImpl.getTarget().getName());
////								methodDependencies.put(method.getParentConcreteClassifier().getName() + "." + method.getName(), refImpl.getTarget().getName());
//								addMethodDependencyToList(method.getParentConcreteClassifier().getName(), method.getName(), refImpl.getTarget().getName());
//							}
//						}
//						else if(method.getParameters().size() > 0){
//							if(method.getParameters().get(0).getTypeReference() instanceof ClassifierReferenceImpl) {
//								ClassifierReferenceImpl target = (ClassifierReferenceImpl) method.getParameters().get(0).getTypeReference();
//								if(target.getTarget().getName().equals("Service")) {
//									blackboard.getGenericMethodDependencies().add(method.getParentConcreteClassifier().getName() + "." + method.getName());
//								}
//							}
//						}
//					}
//				}
//			}
//			else if(blackboard.getMethodDependencies().containsKey(fullMethodCall)) {
////				System.out.println(method.getParentConcreteClassifier().getName() + " . " + method.getName() + " -> " + methodDependencies.get(methodCall.getTarget().getName()));
////				methodDependencies.put(method.getParentConcreteClassifier().getName() + "." + method.getName(), methodDependencies.get(fullMethodCall));
//				addMethodDependencyToList(method.getParentConcreteClassifier().getName(), method.getName(), blackboard.getMethodDependencies().get(fullMethodCall));
//			}
//		}
//		else {
//			//check for method
//			List<String> methodList = Arrays.asList(methodNames);
//			if(methodList.contains(fullMethodCall)) {
//				ConcreteClassifier parentClass = method.getParentConcreteClassifier();
//				for(String dependency: blackboard.getMethodDependencies().get(fullMethodCall)) {
////					addLogicalDependency(Util.getComponentName(parentClass.getQualifiedName()), dependency);
//					addDependency(Util.getComponentName(parentClass.getQualifiedName()), dependency);
//				}
////				addLogicalDependency(Util.getComponentName(parentClass.getQualifiedName()), methodDependencies.get(fullMethodCall));
//				
////				System.out.println("YAAAAY " + Util.getComponentName(parentClass.getQualifiedName()) + " . " + methodCall.getTarget().getName() + " -> " + methodDependencies.get(methodCall.getTarget().getName()));
//			}
//			else if(blackboard.getGenericMethodDependencies().contains(fullMethodCall)) {
//				System.out.println("YAAAY: " + fullMethodCall + " : " + method.getName() + " (" + method.getParentConcreteClassifier().getName() + ")");
//			}
//		}
//	}
	
//	private void addMethodDependencyToList(String parentClass, String methodName, String dependency) {
//		String fullName = parentClass + "." + methodName;
//		if(blackboard.getMethodDependencies().containsKey(fullName)) {
//			if(!blackboard.getMethodDependencies().get(fullName).contains(dependency)) {
//				blackboard.getMethodDependencies().get(fullName).add(dependency);
//			}
//		}
//		else {
//			List<String> newList = new ArrayList<String>();
//			newList.add(dependency);
//			blackboard.getMethodDependencies().put(fullName, newList);
//		}		
//	}
//	
//	private void addMethodDependencyToList(String parentClass, String methodName, List<String> dependencies) {
//		String fullName = parentClass + "." + methodName;
//		if(blackboard.getMethodDependencies().containsKey(fullName)) {
//			for(String dependency: dependencies) {
//				if(!blackboard.getMethodDependencies().get(fullName).contains(dependency)) {
//					blackboard.getMethodDependencies().get(fullName).add(dependency);
//				}
//			}
//		}
//		else {
//			blackboard.getMethodDependencies().put(fullName, dependencies);
//		}		
//	}
	
//	private void addTechnicalDependency(String component, String dependency) {
//		if(blackboard.getTechnicalDependencies().containsKey(component)) {
//			if(blackboard.getTechnicalDependencies().get(component).contains(dependency)) return;
//			else {
//				blackboard.getTechnicalDependencies().get(component).add(dependency);
//			}
//		}
//		else {
//			List<String> newList = new ArrayList<String>();
//			newList.add(dependency);
//			blackboard.getTechnicalDependencies().put(component, newList);
//		}
//	}
//	
//	private void addLogicalDependency(String component, String dependency) {
//		if(blackboard.getLogicalDependencies().containsKey(component)) {
//			if(blackboard.getLogicalDependencies().get(component).contains(dependency)) return;
//			else {
//				blackboard.getLogicalDependencies().get(component).add(dependency);
//			}
//		}
//		else {
//			List<String> newList = new ArrayList<String>();
//			newList.add(dependency);
//			blackboard.getLogicalDependencies().put(component, newList);
//		}
//	}
	
	

}
