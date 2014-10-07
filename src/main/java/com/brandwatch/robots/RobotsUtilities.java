package com.brandwatch.robots;

import com.brandwatch.robots.domain.Group;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

public class RobotsUtilities {

    private static final Logger log = LoggerFactory.getLogger(RobotsUtilities.class);

    @Nonnull
    public Optional<Group> getBestMatchingGroup(@Nonnull List<Group> groups, @Nonnull String agentString) {
        checkNotNull(groups, "groups is null");
        checkNotNull(agentString, "agentString is null");
        checkArgument(!groups.isEmpty(), "groups is empty");

        Optional<Group> bestGroup = Optional.absent();
        int longestMatch = -1;

        for (Group group : groups) {
            int matchLength = getLongestAgentMatchLength(group, agentString);
            if (longestMatch < matchLength) {
                longestMatch = matchLength;
                bestGroup = Optional.of(group);
            }
        }

        return bestGroup;
    }


    public int getLongestAgentMatchLength(@Nonnull Group group, @Nonnull String agentString) {
        checkNotNull(group, "group is null");
        checkNotNull(agentString, "agentString is null");

        int longestMatch = -1;
        for (String agentPattern : group.getUserAgents()) {
            int length = getAgentMatchLength(agentPattern, agentString);
            if (longestMatch < length) {
                longestMatch = length;
            }
        }
        return longestMatch;
    }


    public int getAgentMatchLength(@Nonnull String agentPattern, @Nonnull String agentString) {
        checkNotNull(agentPattern, "agentPattern is null");
        checkNotNull(agentString, "agentString is null");

        if (agentPattern.isEmpty() || agentPattern.equals("*")) {
            return 0;
        } else if (agentString.toLowerCase().contains(agentPattern.toLowerCase())) {
            return agentPattern.length();
        } else {
            return -1;
        }
    }

    public boolean isPathExpressionMatched(@Nonnull String pathExpression, @Nonnull URI uri) {
        return isPathExpressionMatched(pathExpression, uri.getPath());
    }

    boolean isPathExpressionMatched(@Nonnull String pathExpression, @Nonnull String absolutePath) {
        checkArgument(absolutePath.startsWith("/"), "path is not absolute");
        return compilePathExpression(pathExpression).matcher(absolutePath).matches();
    }

    @Nonnull
    Pattern compilePathExpression(@Nonnull String pathExpression) {
        final StringBuilder regex = new StringBuilder().append("^");

        switch (pathExpression.charAt(0)) {
            default:
                regex.append('/');
            case '/':
            case '*':
        }

        final int len = pathExpression.length();
        int start = 0;
        int end;
        while ((end = pathExpression.indexOf('*', start)) != -1) {
            regex.append(quote(pathExpression.substring(start, end)))
                    .append(".*?");
            start = end + 1;
        }

        if (pathExpression.charAt(len - 1) == '$') {
            regex.append(quote(pathExpression.substring(start, len - 1)))
                    .append('$');
        } else {
            regex.append(quote(pathExpression.substring(start, len)))
                    .append(".*?");
        }

        return compile(regex.toString());
    }

}
