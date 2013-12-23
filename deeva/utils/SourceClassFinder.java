package deeva.utils;

/*
  Adapted from http://dzone.com/snippets/get-all-classes-within-package
 */
import java.io.File;
import java.util.*;

public class SourceClassFinder {
    private final List<File> classPaths;
    private final List<File> sourcePaths;
    private Set<String> possibleSourceNames = new HashSet<String>();
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
     *  Do sym-links need to be dealt with?
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
                String className = file.getName().substring(0,
                        file.getName().length() - 6);

                String fullClassName = packagePrepend + className;
                String classDirectory = file.getParent();
                foundClasses.put(fullClassName, classDirectory);

                /* Add to possible source files - exclude nested classes */
                if (!className.contains("$")) {
                    this.possibleSourceNames.add(className + ".java");
                }
            }
        }
        return foundClasses;
    }

    private Map<String, String> findSources(File directory) {
        Map<String, String> foundSources = new HashMap<String, String>();

        if (!directory.exists()) {
            return foundSources;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                if (this.sourcePaths.contains(file)) {
                    continue;
                }

                /* Find sources in all the folders */
                foundSources.putAll(findSources(file));

            } else if (fileName.endsWith(".java") && this.possibleSourceNames
                    .contains(fileName)) {
                /* We've found a source file that has a corresponding class
                that we're planning to load */

                foundSources.put(fileName, file.getPath());
              }
        }

        return foundSources;
    }

    public Map<String, String> getAllClasses() {
        /* Get the cached result */
        if (this.classes != null) {
            return this.classes;
        }

        this.classes = new HashMap<String, String>();
        /* For each class path we find the classes within it */
        for (File classPath : this.classPaths) {
            this.classes.putAll(findClasses(classPath, ""));
        }

        return this.classes;
    }

    public Map<String, String> getAllSources() {
        /*
         We have a list of classes, generate a list of the class names with
         .java extensions, search the source paths, and if we see any java
         files, then we add their location, and their name iff they are a
         named class

         */

        /* Get the cached result */
        if (this.sources != null) {
            return this.sources;
        }

        /* Get all the classes first */
        this.getAllClasses();

        this.sources = new HashMap<String, String>();

        for (File sourcePath : this.sourcePaths) {
            this.sources.putAll(findSources(sourcePath));
        }

        return this.sources;
    }

    public static void main(String[] args) {
        List<String> cps = new LinkedList<String>();
        cps.add("./examples");
        SourceClassFinder finder = new SourceClassFinder(cps, null);


        Map<String, String> classes = finder.getAllClasses();

        System.out.println("--------");

        for (String sourceName : finder.possibleSourceNames) {
            System.out.println("--------");
            System.out.println("Name: " + sourceName);
        }
        System.out.println("--------");

        Map<String, String> sources = finder.getAllSources();

        for (String source : sources.keySet()) {
            System.out.println("--------");
            System.out.println("Name: " + source);
            System.out.println("Location: " + sources.get(source));
        }
        System.out.println("--------");
    }

}
