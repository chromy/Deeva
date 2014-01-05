package deeva.sourceutil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by felixdesouza on 24/12/2013.
 */
public class SourceClassMeta {
    private final String className;
    private final File classFile;
    private File sourceFile;
    private List<String> nestedClasses;

    public SourceClassMeta(String className, File classFile) {
        this.className = className;
        this.classFile = classFile;
        this.nestedClasses = new LinkedList<String>();
    }

    public void setSourceFile(File sourceFile) {
        /* Throw an exception? */
        System.out.println(sourceFile.getName());
        if (sourceFile == null || !sourceFile.getName().endsWith(".java"))
            return;
        this.sourceFile = sourceFile;
    }

    public void addNestedClass(String nestedClass) {
        nestedClasses.add(nestedClass);
    }

    public String getSourceFileName() {
        if (sourceFile == null) {
            return null;
        }
        return this.sourceFile.getPath();
    }
}
