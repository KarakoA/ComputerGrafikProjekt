package gnakcg.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * Resource loading utility class.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class ResourceLoader {
    private static ResourceLoader mResourceLoader = new ResourceLoader();
    private boolean inJarFile;

    public static ResourceLoader getInstance() {
        return mResourceLoader;

    }

    public ResourceLoader() {

        inJarFile=true;
        inJarFile = getResourceURL("ResourceLoader.class").toString().startsWith("jar:");
    }

    /**
     * Fully loads a resource and returns it as a string. Useful for loading shaders.
     */
    public String loadResource(String fileName) throws IOException {
        String result;
        try (InputStream in = getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }


    public InputStream getResourceAsStream(String fileName){
        return ResourceLoader.class.getResourceAsStream(fileName);
    }
    /**
     * Returns the url of a given file.
     *
     * @param fileName the name of the file whose url to return.
     * @return the URL
     */
    private URL getResourceURL(String fileName) {
        return ResourceLoader.class.getResource(fileName);
    }


    public String getResourcePath(String fileName) throws NullPointerException {
        if (inJarFile) {
            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }
            return fileName;
        }
        return new File(getResourceURL(fileName).getFile()).getAbsolutePath();
    }
}
