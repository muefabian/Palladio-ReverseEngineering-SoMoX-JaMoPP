package org.palladiosimulator.jdt.core;

import java.util.Map;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.palladiosimulator.jdt.core.parser.AstLevel;
import org.palladiosimulator.jdt.core.parser.AstParser;
import org.palladiosimulator.jdt.core.visitor.AnnotationVisitor;
import org.palladiosimulator.jdt.core.visitor.ClassDeclarationVisitor;
import org.palladiosimulator.jdt.core.visitor.FieldDeclarationVisitor;
import org.palladiosimulator.jdt.core.visitor.ImportDeclarationVisitor;
import org.palladiosimulator.jdt.core.visitor.MethodDeclarationVisitor;
import org.palladiosimulator.jdt.core.visitor.PackageDeclarationVisitor;
import org.palladiosimulator.jdt.core.visitor.TypeDeclarationVisitor;
import org.palladiosimulator.jdt.helper.FileHelper;

public class Test {
	@SuppressWarnings("static-access")
	public static void main(final String args[]) {
		final ClassDeclarationVisitor classVisitor = new ClassDeclarationVisitor();
		final PackageDeclarationVisitor packageVisitor = new PackageDeclarationVisitor();
		final MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
		final FieldDeclarationVisitor fieldVisitor = new FieldDeclarationVisitor();
		final ImportDeclarationVisitor importVisitor = new ImportDeclarationVisitor();
		final AnnotationVisitor annoVisitor = new AnnotationVisitor();
//		final String path = "C:\\Users\\Fabian\\git\\masterarbeit\\Workspace\\innereclipse\\demo\\src";
//		final String path = "C:\\Users\\Fabian\\git\\TeaStore";
//		final String path = "C:\\Users\\Fabian\\git\\TeaStore\\utilities\\tools.descartes.teastore.registryclient\\src\\main\\java\\tools\\descartes\\teastore\\registryclient";
		final String path = "C:\\Users\\Fabian\\git\\TeaStore\\interfaces\\tools.descartes.teastore.entities\\src\\main\\java\\tools\\descartes\\teastore\\entities";
		
		//final String classpath = "C:\\Users\\Fabian\\.m2\\repository\\org\\springframework\\spring-web\\5.2.1.RELEASE\\spring-web-5.2.1.RELEASE.jar";
		final AstParser parser = new AstParser(AstLevel.JLS11);
		
//		String[] jarfiles = FileHelper.getRegularFiles(FileHelper.createUri("C:\\Users\\Fabian\\.m2\\repository"), "jar");
//		
//		for(String file : jarfiles) {
//			System.out.println(file);
//		}
		
		parser.createAsts(FileHelper.createUri(path), args[0]);
		
		final Map<String,CompilationUnit> asts = parser.getCompilationUnits();
		
		final TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		asts.values().stream().forEach(a -> a.accept(visitor));
		
		visitor.getVisitedNodes().forEach(e -> {
			// TODO: create enums
//			EnumerationImpl newEnum = createEnum(e.getName());
//			entry.getValue().getClassifiers().add(newEnum);
//			classes.put(e, newEnum);
//			if(e.getNodeType()==e.ENUM_DECLARATION) {
//				System.out.println("I AM ENUM: "+ e.getName());
//			}
		});
		
//		final EnumDeclarationVisitor enumVisitor = new EnumDeclarationVisitor();
//		asts.values().stream().forEach(a -> a.accept(enumVisitor));
//		
//		enumVisitor.getVisitedNodes().forEach(e -> {
//			// TODO: create enums
////			EnumerationImpl newEnum = createEnum(e.getName());
////			entry.getValue().getClassifiers().add(newEnum);
////			classes.put(e, newEnum);
////			if(e.getNodeType()==e.ENUM_DECLARATION) {
//				System.out.println("I AM ENUM: "+ e.getName());
////			}
//		});
		
		
		
//        asts.values().stream().forEach(a -> a.accept(packageVisitor));
//        packageVisitor.getVisitedNodes().stream().forEach(p -> {
//        	System.out.println(p.getName());
//        	System.out.println(p.resolveBinding().isSynthetic());
//        });
		//System.out.println("ASTs: " + asts);
		
		//asts.values().stream().forEach(a -> a.accept(packageVisitor));
		//packageVisitor.getVisitedNodes().stream().map(PackageDeclaration::getName).forEach(System.out::println);
		
		//asts.values().stream().forEach(a -> a.accept(classVisitor));
		//classVisitor.getVisitedNodes().stream().map(TypeDeclaration::getName).forEach(System.out::println);
		
		asts.values().parallelStream().forEach(a -> a.accept(methodVisitor));
		methodVisitor.getVisitedNodes().stream().forEach(m -> {
			
			if(!m.isConstructor()) {
//				System.out.println(m.getName() + ": " + m.modifiers().get(0) +" -> " + m.modifiers().get(0).getClass());
//				System.out.println(m.getName() + ": " + m.modifiers());
				
//				System.out.println(m.parameters());
//				for(int i = 0; i < m.parameters().size(); i++) {
//					SingleVariableDeclaration dec = (SingleVariableDeclaration) m.parameters().get(i);
//					System.out.println(dec.getName());
//					System.out.println(dec.getType().resolveBinding().getQualifiedName());
//				}
//				System.out.println(m.getReturnType2());
//				System.out.println("------------------");
//				System.out.println(m.getName());
			}			
		});
		
		//asts.values().stream().forEach(a -> a.accept(fieldVisitor));
		//fieldVisitor.getVisitedNodes().stream().map(FieldDeclaration::fragments).map(l -> l.get(0)).forEach(System.out::println);
	
		
		asts.values().stream().forEach(a -> a.accept(fieldVisitor));
		fieldVisitor.getVisitedNodes().stream().forEach(f -> {
			
//			System.out.println(f.fragments()+ ": " + f.modifiers());
//			System.out.println(f.fragments());
			for(int i = 0; i < f.modifiers().size(); i++) {
//				if(f.modifiers().get(i) instanceof IExtendedModifier) System.out.println("IT IS A MODIFIER: "+f.modifiers().get(i).toString());
//				IExtendedModifier mod = (IExtendedModifier) f.modifiers().get(i);
			}
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) f.fragments().get(0);
			if(fragment.getInitializer() != null) {
				Expression exp = fragment.getInitializer();
//				System.out.println(exp);
				
//				System.out.println("initializier: "+fragment.getInitializer());
//				System.out.println("node type class: "+exp.nodeClassForType(exp.getNodeType()));
//				System.out.println(exp.resolveTypeBinding().getName()+" -> "+exp.resolveTypeBinding().isParameterizedType());
//				ASTNode.LITERAL
//				System.out.println("node type: "+exp.getNodeType());
				
//				System.out.println("constexpvalue: "+exp.resolveConstantExpressionValue());
//				System.out.println("TypeBinding: "+fragment.getInitializer().resolveTypeBinding().getName());
			}
//			System.out.println(f.modifiers().get(0).toString());
			//if(f.getType().toString().equals("boolean")) {
				//System.out.println("IM A BOOLEAN");
			//}
//			if(f.getType().isPrimitiveType()) {
//				System.out.println("prim type name: "+f.getType().resolveBinding().getName());
//			}
//			else if(f.getType().isSimpleType()) {
//				System.out.println("simple type name: "+f.getType().resolveBinding().getName());
//			}
//			else if(f.getType().isArrayType()) {
//				System.out.println("ARRAY "+f.getType().resolveBinding().getElementType().getName());
//			}
//			else if(f.getType().isParameterizedType()) {
//				VariableDeclarationFragment fragment = (VariableDeclarationFragment) f.fragments().get(0);
//				//System.out.println(f.getType().resolveBinding().getName());
//				
//				if(fragment.getInitializer() != null) {
//					System.out.println("TEST");
//    				//System.out.println(fragment.getInitializer().resolveTypeBinding().getName());
//				}
				//System.out.println("getName: "+f.getType().resolveBinding().getName());
				//System.out.println("TypeDecl.getName: "+f.getType().resolveBinding().getTypeDeclaration().getQualifiedName());
				//System.out.println("TypeArgs: "+ f.getType().resolveBinding().getTypeArguments()[0].getName());
//			}
			
			//System.out.println(f.getType().resolveBinding().getName());
			//System.out.println(f.getType().resolveBinding().getQualifiedName());
			//System.out.println("PACKAGE: "+f.getType().resolveBinding().getPackage().getName());
		});
		
		
		asts.values().stream().forEach(a -> a.accept(annoVisitor));
		annoVisitor.getVisitedNodes().stream().forEach(a -> {
			
			//System.out.println("TypeName: "+a.getTypeName());
			//System.out.println(a.resolveTypeBinding());
			//System.out.println("-------------");
			//System.out.println("TypeNameProperty: "+a.getTypeNameProperty());
			//System.out.println("Properties: "+a.properties());
			
			//System.out.println("ToString: "+a.toString());
		});
		
		asts.values().stream().forEach(a -> a.accept(importVisitor));
		importVisitor.getVisitedNodes().stream().forEach(i -> {
//			System.out.println(i.getName());
			IBinding binding = i.resolveBinding();
			if(binding!=null) {
//				System.out.println(i.getName().getFullyQualifiedName());
//				System.out.println(binding.getKind() + "(Type: "+binding.TYPE+", Package: "+binding.PACKAGE+")");
//				System.out.println(binding.getName());
//				System.out.println("   RESOLVABLE - Kind: "+binding.getKind());
//				if(binding.getKind()==binding.TYPE) {
//					System.out.println("Class: "+((ITypeBinding) binding).isClass()+", Interface: "+((ITypeBinding) binding).isInterface()+", Annotation: "+((ITypeBinding) binding).isAnnotation());
//				}
				//System.out.println(binding);
				//((ITypeBinding) binding).get
			}
			//System.out.println("TypeName: "+a.getTypeName());
			//System.out.println(a.resolveTypeBinding());
			//System.out.println("-------------");
			//System.out.println("TypeNameProperty: "+a.getTypeNameProperty());
			//System.out.println("Properties: "+a.properties());
			
			//System.out.println("ToString: "+a.toString());
		});
	}
}
