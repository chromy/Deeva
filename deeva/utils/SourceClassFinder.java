package deeva.utils;

/*
  Adapted from http://dzone.com/snippets/get-all-classes-within-package
 */
import java.io.File;
import java.util.*;

public class SourceClassFinder {
    private final List<File> classPaths;
    private final List<File> sourcePaths;
    private Map<String, String> classes = null;
    private Map<String, String> sources = null;

    /* Maybe abstract out? who knows :S :S definitely in need of refactor at
    some point */
    public SourceClassFinder(List<String> classPathStrings,
                             List<String> sourcePathStrings) {
        if (classPathStrings == null) {
            classPathStrings = new LinkedList<String>();
        }

        if (sourcePathStrings == null) {
            sourcePathStrings = new LinkedList<String>();
        }

        /* Add the current directory as the default if no classpaths are
        specified */
        if (classPathStrings.isEmpty()) {
            classPathStrings.add(".");
        }

        if (sourcePathStrings.isEmpty()) {
            sourcePathStrings.add(".");
        }

        this.classPaths = new LinkedList<File>();
        this.sourcePaths = new LinkedList<File>();

        for (String cpString : classPathStrings) {
            this.classPaths.add(new File(cpString));
        }

        for (String srcString : sourcePathStrings) {
            this.sourcePaths.add(new File(srcString));
        }

    }

    /**
     *  Finds all the classes within the given directory, however,
     *  it may not produce the right package names, since we're also not
     *  loading the class either, so it is up to the user to provide valid
     *  source paths.
     *
     */
    private Map<String, String> findClasses(File directory, String packageName) {
        Map<String, String> foundClasses = new HashMap<String, String>();

        if (!directory.exists()) {
            return foundClasses;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            String packagePrepend = packageName.equals("") ? "" : packageName + '.';
            if (file.isDirectory()) {
                /* If the directory is a explicit classpath,
                we'll skip it as it will be dealt with later */
                if (this.classPaths.contains(file)) {
                    continue;
                }

                assert !file.getName().contains(".");

                String newPackageName = packagePrepend + file.getName();
                foundClasses.putAll(findClasses(file, newPackageName));
            } else if (file.getName().endsWith(".class")) {
                /* We've found a class */
                String className = packagePrepend + file.getName().substring(0, file.getName().length() - 6);
                String classDirectory = file.getParent();
                foundClasses.put(className, classDirectory);
            }
        }
        return foundClasses;
    }

    public Map<String, String> getAllClasses() {
        /* Get the cached result */
        if (this.classes != null) {
            return this.classes;
        }

        /* For each class path we find the classes within it */
        for (File classPath : this.classPaths) {
            this.classes.putAll(findClasses(classPath, ""));
        }

        return this.classes;
    }

    public List<String> getAllSources() {
        /*
         We have a list of classes, generate a list of the class names with
         .java extensions, since the source has to match the

         */
        return null;
    }

    public static void main(String[] args) {
        SourceClassFinder finder = new SourceClassFinder(null, null);

        Map<String, String> classes = finder.findClasses(new File("" +
                "./examples"), "examples");

        for (String className : classes.keySet()) {
            System.out.println("--------");
            System.out.println("Name: " + className);
            System.out.println("Location: " + classes.get(className));
        }
        System.out.println("--------");
    }

}
