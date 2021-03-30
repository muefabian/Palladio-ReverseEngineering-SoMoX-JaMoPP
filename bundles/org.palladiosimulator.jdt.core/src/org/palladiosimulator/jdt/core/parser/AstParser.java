package org.palladiosimulator.jdt.core.parser;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.palladiosimulator.jdt.helper.FileHelper;

public class AstParser {

    private static final String JAVA_FILE_EXTENSION = "java";

    private static final Logger LOG = Logger.getLogger(AstParser.class);

    private static final String[] STANDRAD_ENCODINGS = new String[] { "utf-8" };

    private final AstLevel level;

    private final ASTParser parser;

    private final AstRequestor requestor;

    public AstParser(final AstLevel level) {
        this.level = level;
        requestor = new AstRequestor();
        parser = ASTParser.newParser(level.getConstant());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        
        //TODO: Set options according to level
        Hashtable<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11); //or newer version
        parser.setCompilerOptions(options);
        
        LOG.info("Instantiation of a new parser for Java " + level.getConstant());
    }

    public boolean createAsts(final URI sourcePath, String classPath) {
        try {
            final String[] sourceFilePaths = FileHelper.getRegularFiles(sourcePath, JAVA_FILE_EXTENSION);
            
            final String[] classFilePaths = FileHelper.getRegularFiles(FileHelper.createUri(classPath), "jar");
            
//            for(int i = 0; i < classpathEntries.length; i++) {
//            	classpathEntries[i] = FileHelper.createPath(classpathEntries[i]).toString();
//            }
                        
            parser.setEnvironment(classFilePaths, new String[] { FileHelper.createPath(sourcePath).toString() },
                    STANDRAD_ENCODINGS, true);
            // TODO Determine the encoding of all files
            parser.createASTs(sourceFilePaths, new String[sourceFilePaths.length], new String[0], requestor, null);

        } catch (final IllegalArgumentException e) {
            LOG.error("No files could be parsed for the specified path: " + String.valueOf(sourcePath), e);
            return false;
        }

        return true;
    }

    public Map<String, CompilationUnit> getCompilationUnits() {
        return requestor.getCompilationUnits();
    }

    public AstLevel getLevel() {
        return level;
    }

}
