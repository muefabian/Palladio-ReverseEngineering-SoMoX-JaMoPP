package org.palladiosimulator.jdt.extractor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.emftext.language.java.annotations.AnnotationValue;
import org.emftext.language.java.annotations.impl.AnnotationAttributeSettingImpl;
import org.emftext.language.java.annotations.impl.AnnotationInstanceImpl;
import org.emftext.language.java.annotations.impl.AnnotationParameterListImpl;
import org.emftext.language.java.arrays.impl.ArrayInitializerImpl;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.impl.AnnotationImpl;
import org.emftext.language.java.classifiers.impl.ConcreteClassifierImpl;
import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.expressions.impl.AssignmentExpressionImpl;
import org.emftext.language.java.expressions.impl.UnaryExpressionImpl;
import org.emftext.language.java.members.ClassMethod;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.impl.EnumConstantImpl;
import org.emftext.language.java.members.impl.FieldImpl;
import org.emftext.language.java.members.impl.MethodImpl;
import org.emftext.language.java.modifiers.AnnotationInstanceOrModifier;
import org.emftext.language.java.references.impl.IdentifierReferenceImpl;
import org.emftext.language.java.references.impl.MethodCallImpl;
import org.emftext.language.java.references.impl.StringReferenceImpl;
import org.emftext.language.java.statements.Condition;
import org.emftext.language.java.statements.ExpressionStatement;
import org.emftext.language.java.statements.ForEachLoop;
import org.emftext.language.java.statements.LocalVariableStatement;
import org.emftext.language.java.statements.Return;
import org.emftext.language.java.statements.Statement;
import org.emftext.language.java.statements.TryBlock;
import org.emftext.language.java.statements.impl.BlockImpl;
import org.emftext.language.java.types.impl.ClassifierReferenceImpl;
import org.emftext.language.java.variables.impl.LocalVariableImpl;
import org.palladiosimulator.jdt.extractor.blackboard.Blackboard;

public class ResolverUtil {
	
	private Blackboard blackboard;
	private ParserType type;
	
	public ResolverUtil(Blackboard blackboard, ParserType type) {
		this.blackboard = blackboard;
		this.type = type;
	}
	
	public void resolveStatementsToMethod(ClassMethod member, EList<Statement> statements, String[] methodNames, boolean finish) {
		
		//TODO: add for, while, return, ...		
		for(Statement statement : statements) {
			if(statement instanceof BlockImpl) {
				resolveStatementsToMethod(member, ((BlockImpl)statement).getStatements(), methodNames, finish);
			}
			else if(statement instanceof ExpressionStatement) {
				Expression exp = ((ExpressionStatement)statement).getExpression();
				resolveExpressionToMethod(member, exp, methodNames, finish);				
			}
			else if(statement instanceof LocalVariableStatement) {
				Expression exp = ((LocalVariableStatement)statement).getVariable().getInitialValue();
				resolveExpressionToMethod(member, exp, methodNames, finish);
			}
			else if(statement instanceof TryBlock) {				
				resolveStatementsToMethod(member, ((TryBlock)statement).getBlock().getStatements(), methodNames, finish);
			}
			else if(statement instanceof Condition) {				
				EList<Statement> statementList = new BasicEList<Statement>();				
				resolveExpressionToMethod(member, ((Condition)statement).getCondition(), methodNames, finish);
				statementList.add(((Condition)statement).getStatement());
				statementList.add(((Condition)statement).getElseStatement());				
				resolveStatementsToMethod(member, statementList, methodNames, finish);
			}
			else if(statement instanceof ForEachLoop) {
				EList<Statement> statementList = new BasicEList<Statement>();				
				statementList.add(((ForEachLoop)statement).getStatement());
				resolveStatementsToMethod(member, statementList, methodNames, finish);
			}
			else if(statement instanceof Return) {				
				resolveExpressionToMethod(member, ((Return)statement).getReturnValue(), methodNames, finish);
			}
		}
	}
	
	public void resolveStatementsToClass(ClassMethod member, EList<Statement> statements, String className) {
		
		//TODO: add for, while, return, ...		
		for(Statement statement : statements) {
			if(statement instanceof BlockImpl) {
				resolveStatementsToClass(member, ((BlockImpl)statement).getStatements(), className);
			}
			else if(statement instanceof ExpressionStatement) {
				Expression exp = ((ExpressionStatement)statement).getExpression();
				resolveExpressionToClass(member, exp, className);				
			}
			else if(statement instanceof LocalVariableStatement) {
				Expression exp = ((LocalVariableStatement)statement).getVariable().getInitialValue();
				resolveExpressionToClass(member, exp, className);
			}
			else if(statement instanceof TryBlock) {
				resolveStatementsToClass(member, ((TryBlock)statement).getBlock().getStatements(), className);
			}
			else if(statement instanceof Condition) {
				
				EList<Statement> statementList = new BasicEList<Statement>();				
				resolveExpressionToClass(member, ((Condition)statement).getCondition(), className);
				statementList.add(((Condition)statement).getStatement());
				statementList.add(((Condition)statement).getElseStatement());				
				resolveStatementsToClass(member, statementList, className);
			}
			else if(statement instanceof ForEachLoop) {
				EList<Statement> statementList = new BasicEList<Statement>();				
				statementList.add(((ForEachLoop)statement).getStatement());
				resolveStatementsToClass(member, statementList, className);
			}
			else if(statement instanceof Return) {				
				resolveExpressionToClass(member, ((Return)statement).getReturnValue(), className);
			}
		}
	}
	
	private void resolveExpressionToMethod(ClassMethod method, Expression exp, String[] methodNames, boolean finish) {

		if(exp instanceof AssignmentExpressionImpl) {
			resolveExpressionToMethod(method, ((AssignmentExpressionImpl)exp).getValue(), methodNames, finish);
		}
		else if(exp instanceof MethodCallImpl) {
			MethodCallImpl methodCall = (MethodCallImpl) exp;			
			resolveMethodCallToMethod(method, methodCall, method.getParentConcreteClassifier().getName(), methodNames, finish);
		}
		else if(exp instanceof UnaryExpressionImpl) {
			UnaryExpressionImpl unExp = (UnaryExpressionImpl) exp;
			resolveExpressionToMethod(method, unExp.getChild(), methodNames, finish);
		}
		else if(exp instanceof IdentifierReferenceImpl) {
			
			IdentifierReferenceImpl callIdRef = (IdentifierReferenceImpl) exp;
			String callingClass = "";
			if(callIdRef.getTarget() instanceof LocalVariableImpl) {
				try {
					ConcreteClassifierImpl classRef = (ConcreteClassifierImpl) ((LocalVariableImpl)callIdRef.getTarget()).getTypeReference().getTarget();
					callingClass = classRef.getName();
				}
				catch(Exception e) {						
				}					
			}
			else if(callIdRef.getTarget() instanceof FieldImpl) {
				try {
					ConcreteClassifierImpl classRef = (ConcreteClassifierImpl) ((FieldImpl)callIdRef.getTarget()).getTypeReference().getTarget();
					callingClass = classRef.getName();
				}
				catch(Exception e) {						
				}					
			}
			else {					
				callingClass = callIdRef.getTarget().getName();
			}
			
			if(callIdRef.getNext() instanceof MethodCallImpl) {
				MethodCallImpl methodCall = (MethodCallImpl) callIdRef.getNext();
				
				// CHECK METHOD CALL ITSELF
				resolveMethodCallToMethod(method, methodCall, callingClass, methodNames, finish);
				
				//TODO CHECK NEXT
//				if(methodCall.getNext() instanceof MethodCallImpl) {
//					System.out.println("---------");
//					System.out.println(method.getName());
//					System.out.println(((MethodCallImpl) methodCall.getNext()).getTarget().getName());
//					System.out.println(callIdRef.getTarget().getName());
//					System.out.println();
//					if(method.getName().equals("getRestTemplate")) {
//						MethodCallImpl ifoundit = (MethodCallImpl) methodCall.getNext();
//						System.out.println();
//					}
//					//TODO FIX CALLIDREF
//					resolveMethodCallToMethod(method, (MethodCallImpl) methodCall.getNext(), callIdRef, methodNames, finish);
//				}
//				
//				// CHECK ARGS
//				for(Expression callArg : methodCall.getArguments()) {
//					if(callArg instanceof MethodCallImpl) {
//						MethodCallImpl argMethodCall = (MethodCallImpl) callArg;
//						System.out.println("---------");
//						System.out.println(method.getName());
//						System.out.println(argMethodCall.getTarget().getName());
//						System.out.println(callIdRef.getTarget().getName());
//						System.out.println();
//						//TODO FIX CALLIDREF
//						resolveMethodCallToMethod(method, argMethodCall, callIdRef, methodNames, finish);
//					}
//					if(callArg instanceof IdentifierReferenceImpl) {
//						if(((IdentifierReferenceImpl) callArg).getNext() instanceof MethodCallImpl) {
//							MethodCallImpl argMethodCall = (MethodCallImpl) ((IdentifierReferenceImpl) callArg).getNext();
//							//TODO FIX CALLIDREF
//							resolveMethodCallToMethod(method, argMethodCall, callIdRef, methodNames, finish);
//						}
//					}
//				}
			}
		}	
	}
	
	private void resolveExpressionToClass(ClassMethod method, Expression exp, String className) {
		if(exp instanceof AssignmentExpressionImpl) {
			resolveExpressionToClass(method, ((AssignmentExpressionImpl)exp).getValue(), className);
		}
		else if(exp instanceof MethodCallImpl) {
			resolveMethodCallToClass(method, (MethodCallImpl) exp, null, className);
		}
		else if(exp instanceof UnaryExpressionImpl) {
			UnaryExpressionImpl unExp = (UnaryExpressionImpl) exp;
			resolveExpressionToClass(method, unExp.getChild(), className);
		}
		else if(exp instanceof IdentifierReferenceImpl) {	
			IdentifierReferenceImpl callIdRef = (IdentifierReferenceImpl) exp;			
			
			if(callIdRef.getNext() instanceof MethodCallImpl) {
				MethodCallImpl methodCall = (MethodCallImpl) callIdRef.getNext();
				for(Expression callArg : methodCall.getArguments()) {
					if(callArg instanceof IdentifierReferenceImpl) {
						if(((IdentifierReferenceImpl) callArg).getNext() instanceof MethodCallImpl) {
							MethodCallImpl argMethodCall = (MethodCallImpl) ((IdentifierReferenceImpl) callArg).getNext();
							resolveMethodCallToClass(method, argMethodCall, callIdRef, className);
						}
					}
				}
				resolveMethodCallToClass(method, methodCall, callIdRef, className);						
			}
		}	
	}
	
	
	private void resolveMethodCallToMethod(ClassMethod method, MethodCallImpl methodCall, String callingClass, String[] methodNames, boolean finish) {

		String fullMethodCall = callingClass + "." + methodCall.getTarget().getName();
//		String fullMethodCall = "";
//		
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
//			else if(callingClass.getTarget() instanceof FieldImpl) {
//				try {
//					ConcreteClassifierImpl classRef = (ConcreteClassifierImpl) ((FieldImpl)callingClass.getTarget()).getTypeReference().getTarget();
//					fullMethodCall = classRef.getName() + "." + methodCall.getTarget().getName();
//				}
//				catch(Exception e) {						
//				}					
//			}
//			else {					
//				fullMethodCall = callingClass.getTarget().getName() + "." + methodCall.getTarget().getName();
//			}
//		}

		
		if(!finish) {
			List<String> methodList = Arrays.asList(methodNames);
			if(methodList.contains(methodCall.getTarget().getName())) {
				for(Expression callArg : methodCall.getArguments()) {
					switch(type) {
						case REST:
							if(callArg instanceof IdentifierReferenceImpl) {
								//	check for target component (TODO: lookup class Service)
								if(((IdentifierReferenceImpl) callArg).getTarget().getName().equals("Service")) {
									IdentifierReferenceImpl refImpl = (IdentifierReferenceImpl) ((IdentifierReferenceImpl)callArg).getNext();
									if(refImpl.getTarget() instanceof EnumConstantImpl) {
										addMethodDependencyToList(method.getParentConcreteClassifier().getName(), method.getName(), refImpl.getTarget().getName());
									}
								}
								else if(method.getParameters().size() > 0){
									if(method.getParameters().get(0).getTypeReference() instanceof ClassifierReferenceImpl) {
										ClassifierReferenceImpl target = (ClassifierReferenceImpl) method.getParameters().get(0).getTypeReference();
										if(target.getTarget().getName().equals("Service")) {
											blackboard.getGenericMethodDependencies().add(method.getParentConcreteClassifier().getName() + "." + method.getName());
										}
									}
								}
							}
							break;
						case SPRING:
							if(callArg instanceof StringReferenceImpl) {
								String argument = ((StringReferenceImpl)callArg).getValue().toLowerCase();
								// TODO: ideally resolution of component name with 'spring.application.name' in application/bootstrap.(config/yml)
								if(!blackboard.getDockerComponents().contains(argument)) continue;
								addMethodDependencyToList(method.getParentConcreteClassifier().getName(), method.getName(), argument);
							}
							
							break;
					}
					
				}
			}
			else if(blackboard.getMethodDependencies().containsKey(fullMethodCall)) {				
				addMethodDependencyToList(method.getParentConcreteClassifier().getName(), method.getName(), blackboard.getMethodDependencies().get(fullMethodCall));
			}
			
			// CHECK ARGS
			for(Expression callArg : methodCall.getArguments()) {
				if(callArg instanceof MethodCallImpl) {
					MethodCallImpl argMethodCall = (MethodCallImpl) callArg;					
					resolveMethodCallToMethod(method, argMethodCall, method.getParentConcreteClassifier().getName(), methodNames, finish);
				}
				if(callArg instanceof IdentifierReferenceImpl) {
					if(((IdentifierReferenceImpl) callArg).getNext() instanceof MethodCallImpl) {
						resolveExpressionToMethod(method, callArg, methodNames, finish);
					}
				}
			}
			
			// CHECK NEXT
			if(methodCall.getNext() instanceof MethodCallImpl) {
				String newCallingClass = "";
				if(methodCall.getTarget() instanceof MethodImpl) {
					ClassifierReferenceImpl classRef = (ClassifierReferenceImpl) ((MethodImpl)methodCall.getTarget()).getTypeReference();
					newCallingClass = classRef.getTarget().getName();					
				}
				resolveMethodCallToMethod(method, (MethodCallImpl) methodCall.getNext(), newCallingClass, methodNames, finish);
			}
			
		}
		else {
			
			//check for method
			List<String> methodList = Arrays.asList(methodNames);
			if(methodList.contains(fullMethodCall)) {
				ConcreteClassifier parentClass = method.getParentConcreteClassifier();
				for(String dependency: blackboard.getMethodDependencies().get(fullMethodCall)) {
//					addLogicalDependency(Util.getComponentName(parentClass.getQualifiedName()), dependency);
//					addDependency(Util.getComponentName(parentClass.getQualifiedName()), dependency);
					String component = blackboard.getComponentFromClass(parentClass.getQualifiedName());
					if(component == null) continue;
					addDependency(component, dependency.toLowerCase());
				}
//				addLogicalDependency(Util.getComponentName(parentClass.getQualifiedName()), methodDependencies.get(fullMethodCall));
				
			}
			// resolve dependency from generic method (via parameter)
			else if(blackboard.getGenericMethodDependencies().contains(fullMethodCall)) {
				
				for(Expression callArg : methodCall.getArguments()) {
					switch(type) {
						case REST:
							if(callArg instanceof IdentifierReferenceImpl) {
								//	check for target component (TODO: lookup class Service)
								if(((IdentifierReferenceImpl) callArg).getTarget().getName().equals("Service")) {
									IdentifierReferenceImpl refImpl = (IdentifierReferenceImpl) ((IdentifierReferenceImpl)callArg).getNext();
									if(refImpl.getTarget() instanceof EnumConstantImpl) {
										addDependency(blackboard.getComponentFromClass(method.getParentConcreteClassifier().getQualifiedName()), refImpl.getTarget().getName().toLowerCase());
									}
								}
							}
							break;
						case SPRING:
							if(callArg instanceof StringReferenceImpl) {
								String argument = ((StringReferenceImpl)callArg).getValue().toLowerCase();
								
								// TODO: ideally resolution of component name with 'spring.application.name' in application/bootstrap.(config/yml)
								if(!blackboard.getDockerComponents().contains(argument)) continue;
								addMethodDependencyToList(method.getParentConcreteClassifier().getName(), method.getName(), argument);
								
								addDependency(blackboard.getComponentFromClass(method.getParentConcreteClassifier().getQualifiedName()), argument);
							}
							
							break;
					}
				}
			}
		}
	}
	
	private void resolveMethodCallToClass(ClassMethod method, MethodCallImpl methodCall, IdentifierReferenceImpl callingClass, String className) {

		String fullMethodCall = "";
		if(callingClass == null) {
			fullMethodCall = method.getParentConcreteClassifier().getName() + "." + methodCall.getTarget().getName();
		}
		else {
			// get name of the calling class from variable
			if(callingClass.getTarget() instanceof LocalVariableImpl) {
				try {
					ConcreteClassifierImpl classRef = (ConcreteClassifierImpl) ((LocalVariableImpl)callingClass.getTarget()).getTypeReference().getTarget();
					fullMethodCall = classRef.getName() + "." + methodCall.getTarget().getName();
				}
				catch(Exception e) {						
				}					
			}
			else {					
				fullMethodCall = callingClass.getTarget().getName() + "." + methodCall.getTarget().getName();
			}
		}
		
		if(className != null) {
			if(fullMethodCall.split("\\.")[0].equalsIgnoreCase(className)) {
				
				addMissingDependencyToList(className, method);	
				addMissingInterfaceMethodToList(className, (MethodImpl)methodCall.getTarget());
				
////				System.out.println("WHICH IS IT: " + method.getName() + " OR " + methodCall.getTarget());
//				System.out.println("IS IT THE SAME? " + fullMethodCall.split("\\.")[0] + " <-> " + method.get);
//				if(blackboard.getMissingDependencies().containsKey(Util.getComponentName(method.getParentConcreteClassifier().getQualifiedName()))) {
////					System.out.println("BLAAAAAAH "+methodCall.getTarget().getClass() + " (" + methodCall.getTarget().getName() + ")");
//					blackboard.getMissingDependencies().get(Util.getComponentName(method.getParentConcreteClassifier().getQualifiedName())).add(className);
////					blackboard.getMissingDependencies().get(Util.getComponentName(method.getParentConcreteClassifier().getQualifiedName())).add((MethodImpl)methodCall.getTarget());
//				}
//				else {
////					List<String> newList = new ArrayList<String>();
////					newList.add(fullMethodCall);
//					List<MethodImpl> newList = new ArrayList<MethodImpl>();
//					newList.add((MethodImpl)methodCall.getTarget());
////					System.out.println("BLAAAAAAH "+methodCall.getTarget().getClass() + " (" + methodCall.getTarget().getName() + ")");
//					blackboard.getMissingDependencies().put(Util.getComponentName(method.getParentConcreteClassifier().getQualifiedName()), newList);
//				}
			}
		}
	}
	
	private void addMissingDependencyToList(String className, ClassMethod method) {
		if(blackboard.getMissingDependencies().containsKey(blackboard.getComponentFromClass(method.getParentConcreteClassifier().getQualifiedName()))) {
			if(!blackboard.getMissingDependencies().get(blackboard.getComponentFromClass(method.getParentConcreteClassifier().getQualifiedName())).contains(className)) {
				blackboard.getMissingDependencies().get(blackboard.getComponentFromClass(method.getParentConcreteClassifier().getQualifiedName())).add(className);
			}			
		}
		else {
			List<String> newList = new ArrayList<String>();
			newList.add(className);
			blackboard.getMissingDependencies().put(blackboard.getComponentFromClass(method.getParentConcreteClassifier().getQualifiedName()), newList);
		}
	}
	
	private void addMissingInterfaceMethodToList(String className, MethodImpl method) {
		if(blackboard.getMissingInterfaceMethods().containsKey(className)) {
			if(!blackboard.getMissingInterfaceMethods().get(className).contains(method)) {
				blackboard.getMissingInterfaceMethods().get(className).add(method);
			}
			
		}
		else {
			List<MethodImpl> newList = new ArrayList<MethodImpl>();
			newList.add(method);
			blackboard.getMissingInterfaceMethods().put(className, newList);
		}
	}
	
	private void addMethodDependencyToList(String parentClass, String methodName, String dependency) {		
		String fullName = parentClass + "." + methodName;
		if(blackboard.getMethodDependencies().containsKey(fullName)) {
			if(!blackboard.getMethodDependencies().get(fullName).contains(dependency)) {
				blackboard.getMethodDependencies().get(fullName).add(dependency);
			}
		}
		else {
			List<String> newList = new ArrayList<String>();
			newList.add(dependency);
			blackboard.getMethodDependencies().put(fullName, newList);
		}
	}
	
	private void addMethodDependencyToList(String parentClass, String methodName, List<String> dependencies) {
		
		String fullName = parentClass + "." + methodName;
		if(blackboard.getMethodDependencies().containsKey(fullName)) {
			for(String dependency: dependencies) {
				if(!blackboard.getMethodDependencies().get(fullName).contains(dependency)) {
					blackboard.getMethodDependencies().get(fullName).add(dependency);
				}
			}
		}
		else {
			blackboard.getMethodDependencies().put(fullName, dependencies);
		}
	}

	
	private void addDependency(String component, String dependency) {
		if(component == null || component.equalsIgnoreCase(dependency)) return;
		if(!blackboard.getExtractedComponent(component).getRequires().containsKey(dependency)) {
			blackboard.getExtractedComponent(component).getRequires().put(dependency, blackboard.getExtractedInterface(dependency));
		}		
	}

	public String resolveAnnotationDependencies(String className) {

		for(ConcreteClassifierImpl concreteClass : blackboard.getAllClassifiers().values()) {
			
			// only check classes of components
			if(blackboard.getComponentFromClass(concreteClass.getQualifiedName()) == null) continue;
			
			if(containsReferenceToClass(concreteClass, className)) {
				return blackboard.getComponentFromClass(concreteClass.getQualifiedName());
			}								
		}
		
		return null;
	}
	
	// checks method return parameters of class for reference and resolves import annotations to check the imported class as well
	public boolean containsReferenceToClass(ConcreteClassifierImpl concreteClass, String className) {
		
		// TODO check other parameters
		
		// check method return parameters
		for(Member member : concreteClass.getMembers()) {
			if(member instanceof MethodImpl) {
				MethodImpl method = (MethodImpl) member;
				if(method.getTypeReference() instanceof ClassifierReferenceImpl) {
					if(method.getTypeReference().getTarget() instanceof ConcreteClassifierImpl) {
						ConcreteClassifierImpl referencedClassifier = (ConcreteClassifierImpl) method.getTypeReference().getTarget();
						
						if(referencedClassifier.getQualifiedName().equalsIgnoreCase(className)) {
							return true;
						}
					}					
				}
			}
		}
		
		
		for(AnnotationInstanceOrModifier anno : concreteClass.getAnnotationsAndModifiers()) {
			if(anno instanceof AnnotationInstanceImpl) {
								
				AnnotationImpl annotation = (AnnotationImpl) ((AnnotationInstanceImpl)anno).getAnnotation();
				String annotationName = annotation.getName();

				for(AnnotationInstanceOrModifier innerAnno : annotation.getAnnotationsAndModifiers()) {
					if(innerAnno instanceof AnnotationInstanceImpl) {
						AnnotationImpl innerAnnotation = (AnnotationImpl) ((AnnotationInstanceImpl)innerAnno).getAnnotation();
						String innerAnnotationName = innerAnnotation.getName();
							
						if(innerAnnotationName.equalsIgnoreCase("import")) {

							if(((AnnotationInstanceImpl)innerAnno).getParameter() instanceof AnnotationParameterListImpl) {
								AnnotationParameterListImpl parameterList = (AnnotationParameterListImpl) ((AnnotationInstanceImpl)innerAnno).getParameter();
								AnnotationValue value = ((AnnotationAttributeSettingImpl)parameterList.getSettings().get(0)).getValue();

								if(value instanceof ArrayInitializerImpl) {
									if(((ArrayInitializerImpl)value).getInitialValues().get(0) instanceof IdentifierReferenceImpl) {
										IdentifierReferenceImpl idRef = (IdentifierReferenceImpl) ((ArrayInitializerImpl)value).getInitialValues().get(0);
										ConcreteClassifierImpl referencedClassifier = (ConcreteClassifierImpl) idRef.getTarget();
										if(referencedClassifier.getQualifiedName().equalsIgnoreCase(className)) {
											return true;
										}
										else {
											if(containsReferenceToClass(referencedClassifier, className)) {
												return true;
											}
										}
									}
								}												
							}											
						}
					}
				}						
			}			
		}		
		
		return false;
	}

}
