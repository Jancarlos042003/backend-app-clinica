package com.proyecto.appclinica.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@RequiredArgsConstructor
public class MultipartFileResource implements Resource {
    private final MultipartFile multipartFile;

    @Override
    public boolean exists() {
        return multipartFile != null && multipartFile.isEmpty();
    }

    @Override
    @Nullable
    public URL getURL() throws IOException {
        return null;
    }

    @Override
    @Nullable
    public URI getURI() throws IOException {
        return null;
    }

    @Override
    @Nullable
    public File getFile() throws IOException {
        return null;
    }

    @Override
    public long contentLength() throws IOException {
        return multipartFile.getSize();
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }

    @Override
    @Nullable
    public Resource createRelative(String relativePath) throws IOException {
        return null;
    }

    @Override
    public String getFilename() {
        return multipartFile.getOriginalFilename();
    }

    @Override
    public String getDescription() {
        return "MultipartFileResource for " + multipartFile.getOriginalFilename();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return multipartFile.getInputStream();
    }
}
