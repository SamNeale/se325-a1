package se325.assignment01.concert.service.domain;

import javax.ws.rs.container.AsyncResponse;

public class Subscriber {

    private final long concertId;
    private final AsyncResponse response;
    private final int percentageThreshold;

    public Subscriber(long concertId, AsyncResponse response, int percentageThreshold){
        this.concertId = concertId;
        this.response = response;
        this.percentageThreshold = percentageThreshold;
    }

    public long getConcertId() { return concertId; }

    public AsyncResponse getResponse() { return response; }

    public int getPercentageThreshold() { return percentageThreshold; }

}
