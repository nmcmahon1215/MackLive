package com.macklive.twitter;

import twitter4j.TwitterStream;

import java.util.List;

public class TwitterStreamWrapper {

    private TwitterStream stream;
    private List<String> handles;

    public TwitterStreamWrapper(TwitterStream stream, List<String> handles) {
        this.stream = stream;
        this.handles = handles;
    }

    public TwitterStream getStream() {
        return stream;
    }

    public List<String> getHandles() {
        return handles;
    }
}
