package com.brandwatch.robots;

import com.brandwatch.robots.domain.Robots;
import com.brandwatch.robots.parser.ParseException;
import com.brandwatch.robots.parser.RobotsTxtParser;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

class RobotsDownloaderImpl implements RobotsDownloader {

    private static final Logger log = LoggerFactory.getLogger(RobotsDownloaderImpl.class);
    private final RobotsUtilities utilities;

    public RobotsDownloaderImpl(@Nonnull RobotsUtilities utilities) {
        this.utilities = checkNotNull(utilities, "utilities is null");
    }

    @Nonnull
    @Override
    public Robots load(@Nonnull URI robotsResource) {
        checkNotNull(robotsResource, "robotsResource");

        final RobotsBuildingHandler handler = new RobotsBuildingHandler(utilities);
        final Closer closer = Closer.create();

        try {
            try {
                log.debug("Downloading and parsing robot.txt: {}", robotsResource);

                final Reader reader = closer.register(
                        utilities.createCharSourceFor(robotsResource).openBufferedStream());


                final RobotsTxtParser parser = new RobotsTxtParser(reader);
                parser.parse(handler);

            } catch (ParseException e) {
                log.warn("Failed to parse robots.txt: ", e);
                throw closer.rethrow(e);
            } catch (Throwable t) {
                log.warn("Failed to download robots.txt: ", t);
                throw closer.rethrow(t);
            } finally {
                closer.close();
            }
        } catch (IOException ex) {
            log.warn("Failed to download robots.txt: ", ex);
            throw new RuntimeException(ex);
        }

        return handler.get();
    }

}
