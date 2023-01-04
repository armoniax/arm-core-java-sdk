package io.armoniax.abiserializationprovider;

import java.io.*;
import java.net.URL;

public class EmbeddedLibraryTools {

    public static final boolean LOADED_EMBEDDED_LIBRARY;

    static {
        LOADED_EMBEDDED_LIBRARY = loadEmbeddedLibrary();
    }

    private EmbeddedLibraryTools() {
    }

    private static boolean loadEmbeddedLibrary() {

        boolean usingEmbedded = false;

        // attempt to locate embedded native library within JAR at following location:
        String[] allowedExtensions = new String[]{"dylib", "so", "dll"};
        String[] libs = new String[]{"libamaxabi"};
        StringBuilder url = new StringBuilder();
        url.append("/amaxabi/build/lib/main/release/");
        for (String lib : libs) {
            URL nativeLibraryUrl = null;
            // loop through extensions, stopping after finding first one
            for (String ext : allowedExtensions) {
                nativeLibraryUrl = AbiSerializationProviderImpl.class.getResource(url.toString() + lib + "." + ext);
                if (nativeLibraryUrl != null)
                    break;
            }

            if (nativeLibraryUrl != null) {
                // native library found within JAR, extract and load
                try {

                    final File libfile = File.createTempFile(lib, ".lib");
                    libfile.deleteOnExit(); // just in case

                    final InputStream in = nativeLibraryUrl.openStream();
                    final OutputStream out = new BufferedOutputStream(new FileOutputStream(libfile));

                    int len = 0;
                    byte[] buffer = new byte[8192];
                    while ((len = in.read(buffer)) > -1)
                        out.write(buffer, 0, len);
                    out.close();
                    in.close();
                    System.load(libfile.getAbsolutePath());

                    usingEmbedded = true;

                } catch (IOException x) {
                    // mission failed, do nothing
                }

            } // nativeLibraryUrl exists
        }
        return usingEmbedded;
    }
}
