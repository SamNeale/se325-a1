package se325.assignment01.concert.service.services;

import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.common.types.BookingStatus;
import se325.assignment01.concert.service.domain.*;
import se325.assignment01.concert.service.jaxrs.LocalDateTimeParam;
import se325.assignment01.concert.service.mapper.BookingMapper;
import se325.assignment01.concert.service.mapper.ConcertMapper;
import se325.assignment01.concert.service.mapper.PerformerMapper;
import se325.assignment01.concert.service.mapper.SeatMapper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;

@Path("/concert-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConcertResource {

    @PersistenceContext(name = "se325.assignment01.concert")
    private EntityManager em;


    private final static Set<Subscriber> subscribers = new HashSet<>();


    /***
     * This method gets a single concert specified by the URI of the incoming HTTP Get request. The concert is
     * then returned as a concert dto within the body of an HTTP response message.
     *
     * In the case where no concert exists for the given id 404 is returned.
     *
     * @param id - the id of the conceret to be returned
     * @return Response - an HTTP response
     */
    @GET
    @Path("/concerts/{id}")
    public Response getConcert(@PathParam("id") long id) {

        em = PersistenceManager.instance().createEntityManager();
        Concert concert;
        try {
            em.getTransaction().begin();
            concert = em.find(Concert.class, id);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        if (concert == null) {
            return Response.status(404).build();
        } else {
            return Response.ok().entity(ConcertMapper.toDto(concert)).build();
        }
    }

    /***
     * This method returns every concert that is known by the database in concert dto format within an HTTP response
     * message.
     *
     * @return Response - the HTTP response message.
     */

    @GET
    @Path("/concerts")
    public Response getAllConcerts() {

        em = PersistenceManager.instance().createEntityManager();
        List<Concert> concerts;

        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            concerts = concertQuery.getResultList();
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        if (concerts.size() == 0) {
            return Response.status(404).build();
        } else {

            List<ConcertDTO> concertDTOs = new ArrayList<>();
            for (Concert c : concerts) {
                ConcertDTO dto = ConcertMapper.toDto(c);
                concertDTOs.add(dto);
            }

            return Response.ok().entity(concertDTOs).build();
        }
    }

    /***
     * This method returns the concert summaries for all concerts that are known by the web service.
     * The concert summaries are returned in their dto form.
     *
     * @return Respone - and HTTP response containing concert summaries
     */
    @GET
    @Path("/concerts/summaries")
    public Response getConcertSummaries() {

        em = PersistenceManager.instance().createEntityManager();
        List<Concert> concerts;

        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            concerts = concertQuery.getResultList();
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        if (concerts.size() == 0) {
            Response response = Response.status(404).build();
            return response;
        } else {

            List<ConcertSummaryDTO> concertSummaries = new ArrayList<>();
            for (Concert c : concerts) {
                ConcertSummaryDTO summary = new ConcertSummaryDTO(c.getId(), c.getTitle(), c.getImage_name());
                concertSummaries.add(summary);
            }

            return Response.ok().entity(concertSummaries).build();
        }
    }

    /***
     * This method gets a single performer from the datebase and returns it as a performer DTO if it exists inside the body
     * of an HTTP response message. If it does not exist a 404 status code is returned
     *
     * @param id the performer id
     * @return Response - the HTTP response message
     */
    @GET
    @Path("/performers/{id}")
    public Response getPerformer(@PathParam("id") long id) {

        em = PersistenceManager.instance().createEntityManager();
        Performer performer;

        try {
            em.getTransaction().begin();
            performer = em.find(Performer.class, id);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        if (performer == null) {
            return Response.status(404).build();
        } else {
            return Response.ok().entity(PerformerMapper.toDto(performer)).build();
        }
    }

    /***
     * This method returns all performers known by the web service.
     *
     * @return Response - and HTTP response containing all performers in the body.
      */
    @GET
    @Path("/performers")
    public Response getAllPerformers() {
        em = PersistenceManager.instance().createEntityManager();
        List<Performer> performers;

        try {
            em.getTransaction().begin();
            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
            performers = performerQuery.getResultList();
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        if (performers.size() == 0) {
            return Response.status(404).build();
        } else {

            List<PerformerDTO> performerDTOs = new ArrayList<PerformerDTO>();
            for (Performer p : performers) {
                PerformerDTO dto = PerformerMapper.toDto(p);
                performerDTOs.add(dto);
            }

            return Response.ok().entity(performerDTOs).build();
        }
    }

    /***
     * This method attempts to authenticate a user credentials with those known by the web service in the database.
     * If the user is known and credentials correct an authorization token (as random UUID) is returned in a cookie.
     *
     * Modified from:  https://santoshbandage.wordpress.com/2017/06/13/how-token-based-authentication-works-in-rest-api/a
     *
     * @param user - the user (as username and password) to be logged in
     * @return Respone- an HTTP response with a cookie containing auth token for client to provide.
     */
    @POST
    @Path("/login")
    public Response authenticateUser(UserDTO user) {
        try {

            String token = authenticate(user.getUsername(), user.getPassword());

            NewCookie cookie = new NewCookie("auth", token);
            return Response.ok().cookie(cookie).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /***
     * A helper method to authenticate that checks user credentials against those stored within the database.
     *
     * @param username
     * @param password
     * @return the UUID token if credentials are correct
     * @throws Exception if there is no user the database knows about - thrown by getSingleResult()
     */
    private String authenticate(String username, String password) throws Exception {

        em = PersistenceManager.instance().createEntityManager();
        User user;
        String token;
        try {

            em.getTransaction().begin();
            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :un and u.password = :pw", User.class)
                    .setParameter("un", username)
                    .setParameter("pw", password);
            user = userQuery.getSingleResult();

            token = UUID.randomUUID().toString();
            user.setToken(token);

            em.merge(user);
            em.getTransaction().commit();

        } finally {
            em.close();
        }
        return token;

    }

    /***
     * This method attempts to satisfy the booking specified in the bookingRequestDTO. If the user is logged in and the
     * booking is valid (e.g the seats are avaialble, etc) the booking is created and persisted to the database.
     *
     * @param bookingRequestDTO - the requested booking
     * @param cookie - contains auth token to verify user is logged in
     * @param uri - of the incoming request relative to web service
     * @return Response - an HTTP response indicating the out come of attemping to statisfy the booking request.
     */
    @POST
    @Path("/bookings")
    public Response attemptBooking(BookingRequestDTO bookingRequestDTO, @CookieParam("auth") Cookie cookie, @Context UriInfo uri) {

        // Checking client autorization - that is the booking comes from someone logged in
        if (cookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String token = cookie.getValue();

        // Then we have a UUID token
        if (token.length() == UUID.randomUUID().toString().length()) {

            // Check there is a user for this token
            em = PersistenceManager.instance().createEntityManager();
            Booking booking;
            try {
                em.getTransaction().begin();

                // Checking the token matches
                TypedQuery<User> userQuery = em.createQuery("select u from User u where token = :authToken", User.class).setParameter("authToken", token);
                User user = userQuery.getSingleResult();

                //Checking that a concert exists for given date
                Concert concert = em.find(Concert.class, bookingRequestDTO.getConcertId());

                if (concert == null|| !concert.getDates().contains(bookingRequestDTO.getDate())){
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }

                // Attempting to process the booking.
                TypedQuery<Seat> seatsQuery = em.createQuery("select s from Seat s where s.label in :seatLabels and s.date = :concertDate", Seat.class)
                        .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                        .setParameter("seatLabels", bookingRequestDTO.getSeatLabels())
                        .setParameter("concertDate", bookingRequestDTO.getDate());


                List<Seat> seats = seatsQuery.getResultList();
                List<Seat> seatsToBook = new ArrayList<Seat>();

                boolean allSeatsAvailable = true;
                for (Seat s : seats) {
                    if (s.isBooked()) {
                        allSeatsAvailable = false;
                        break;
                    } else {
                        s.setBooked(true);
                        seatsToBook.add(s);
                    }
                }

                if (allSeatsAvailable) {
                    // Create a booking
                    booking = new Booking(bookingRequestDTO.getConcertId(), seatsToBook, bookingRequestDTO.getDate(), user.getId());

                    for (Seat s : seatsToBook) {
                        em.merge(s);
                    }
                    em.persist(booking);
                } else {
                    // Return error code resource unavailable
                    return Response.status(Response.Status.FORBIDDEN).build();
                }

                em.getTransaction().commit();
            } catch (Exception e) {
                // This token isn't valid so return unauthroized.
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } finally {
                em.close();
            }

            // Notifying our subscribers that concert has been booked.

            notifySubscribers(bookingRequestDTO.getDate(), bookingRequestDTO.getConcertId());
            return Response.status(Response.Status.CREATED).location(URI.create("/concert-service/bookings/" + booking.getId())).build();
        } else {
            //We dont have a UUID token - so not authorized.
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /***
     * This method gets all seats for a given booking status (eg booked, free, any).
     *
     * @param status - status of seats in question
     * @param dateTime - date of seats in question
     * @return Response - an HTTP response containing the seats matching query
     */
    @GET
    @Path("/seats/{date}")
    public Response getSeatsForStatus(@QueryParam("status") BookingStatus status, @PathParam("date") String dateTime) {

        // Converting string to LocalDatetime from pathparam
        LocalDateTimeParam ldtp = new LocalDateTimeParam(dateTime);
        LocalDateTime datetime = ldtp.getLocalDateTime();

        em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            // Status is booked
            if (status == BookingStatus.Booked) {
                TypedQuery<Seat> seatsQuery = em.createQuery("select s from Seat s where s.isBooked = :isBooked and s.date = :date", Seat.class)
                        .setParameter("isBooked", true)
                        .setParameter("date", datetime);
                List<Seat> seatListBooked = seatsQuery.getResultList();

                List<SeatDTO> seatDTOList = new ArrayList<>();

                for (Seat s : seatListBooked) {
                    SeatDTO seatDTO = SeatMapper.toDto(s);
                    seatDTOList.add(seatDTO);
                }
                return Response.ok().entity(seatDTOList).build();
            } else if (status == BookingStatus.Unbooked) {
                TypedQuery<Seat> seatsQuery = em.createQuery("select s from Seat s where s.isBooked = :isBooked and s.date = :date", Seat.class)
                        .setParameter("isBooked", false)
                        .setParameter("date", datetime);
                List<Seat> seatListUnbooked = seatsQuery.getResultList();

                List<SeatDTO> seatDTOList = new ArrayList<>();

                for (Seat s : seatListUnbooked) {
                    SeatDTO seatDTO = SeatMapper.toDto(s);
                    seatDTOList.add(seatDTO);
                }
                return Response.ok().entity(seatDTOList).build();
            } else {
                TypedQuery<Seat> seatsQuery = em.createQuery("select s from Seat s where s.date = :date", Seat.class)
                        .setParameter("date", datetime);
                List<Seat> seatListAny = seatsQuery.getResultList();

                List<SeatDTO> seatDTOList = new ArrayList<>();

                for (Seat s : seatListAny) {
                    SeatDTO seatDTO = SeatMapper.toDto(s);
                    seatDTOList.add(seatDTO);
                }
                return Response.ok().entity(seatDTOList).build();
            }
        }
        finally{
            em.close();
        }

    }

    /***
     * This method gets all bookings associated with a particular user.
     * The client must provide an auth-token which verifies them as a logged in user
     *
     * @return Response - an HTTP response message containing all bookings for a user.
     *
     */
    @GET
    @Path("/bookings")
    public Response getBookingForUser(@CookieParam("auth") Cookie cookie) {

        // Checking that the client is authenticated
        if (cookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String token = cookie.getValue();

        // Then we have a UUID token
        List<Booking> bookings;
        if (token.length() == UUID.randomUUID().toString().length()) {

            // Check there is a user for this token
            em = PersistenceManager.instance().createEntityManager();
            try {
                em.getTransaction().begin();

                // Checking the token matches
                TypedQuery<User> userQuery = em.createQuery("select u from User u where token = :authToken", User.class).setParameter("authToken", token);
                User user = userQuery.getSingleResult();

                // Getting all bookings for this user.
                TypedQuery<Booking> bookingQuery = em.createQuery("select b from Booking b where b.userId = :userId", Booking.class)
                        .setParameter("userId", user.getId());

                bookings = bookingQuery.getResultList();

                em.getTransaction().commit();

            } catch (NoResultException  e) {
                // This token isn't valid so return unauthroized.
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } finally {
                em.close();
            }

            // Converting bookings to DTOs
            List<BookingDTO> bookingsListDTO = new ArrayList<BookingDTO>();
            for (Booking b : bookings) {
                BookingDTO dto = BookingMapper.toDto(b);
                bookingsListDTO.add(dto);
            }

            return Response.ok().entity(bookingsListDTO).build();

        }
        else {
        //We dont have a UUID token - so not authorized.
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    }

    /***
     * This method attempts to get a booking by providing a valid booking ID - only succedes if the correct booking
     * owner is logged in
     *
     */
    @GET
    @Path("/bookings/{id}")
    public Response getBooking(@CookieParam("auth") Cookie cookie, @PathParam("id") long id){

        User user = validateToken(cookie);
        if (user == null){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Getting the booking.
        em = PersistenceManager.instance().createEntityManager();
        Booking booking;
        try{
            em.getTransaction().begin();
            booking = em.find(Booking.class, id);
            em.getTransaction().commit();
        }
        finally{
            em.close();
        }

        if (booking == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        else if( booking.getUserId() != user.getId()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        else{
            return Response.ok().entity(BookingMapper.toDto(booking)).build();
        }


    }

    /***
     * This is a helper function that checks user authroization by validating the provided UUID auth-token.
     *
     * @param cookie - http cookie containing the auth-token
     * @return User if the user exists and the token is valid - null otherwise
     */
    public User validateToken(Cookie cookie){

        if (cookie == null) {
            return null;
        }

        String authToken = cookie.getValue();
        User user = null;
        if (authToken.length() == UUID.randomUUID().toString().length()){

            //Checking user authorization
            em = PersistenceManager.instance().createEntityManager();

            try{
                em.getTransaction().begin();
                // Checking the token matches
                TypedQuery<User> userQuery = em.createQuery("select u from User u where token = :authToken", User.class).setParameter("authToken", authToken);
                 user = userQuery.getSingleResult(); // throws exception if no user for token
                em.getTransaction().commit();

            }
            catch(Exception e){
                return null;
            }
            finally{
                em.close();
            }

        }
        return user;
    }

    // Handling publish-subscribe for concert notifications
    @POST
    @Path("subscribe/concertInfo")
    public void subscribeForConcertInfo(@Suspended AsyncResponse sub, @CookieParam("auth") Cookie cookie, ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO) throws NotAuthorizedException{
        em = PersistenceManager.instance().createEntityManager();
        Concert concert;
        try{
            em.getTransaction().begin();
            concert = em.find(Concert.class, concertInfoSubscriptionDTO.getConcertId());
            em.getTransaction().commit();
        }
        finally {
            em.close();
        }

        // Check if request comes from a logged in user.
        if (validateToken(cookie) == null){
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        else if( concert == null || !concert.getDates().contains(concertInfoSubscriptionDTO.getDate())){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        else{
            // Otherwise we are authenticated and can create a subscriber
            synchronized (subscribers){
                subscribers.add(new Subscriber(concertInfoSubscriptionDTO.getConcertId(), sub, concertInfoSubscriptionDTO.getPercentageBooked()));
            }

        }
    }


    public void notifySubscribers(LocalDateTime date, long concertId) {

        synchronized (subscribers) {
            for (Subscriber sub : subscribers){
                if (sub.getConcertId() == concertId){
                    int percentageSeatsRemaining = calculatePercentageSeatsReaming(date, concertId);

                    // Checking concert validity
                    if (percentageSeatsRemaining == -1 ){
                        break;
                    }
                    else if(percentageSeatsRemaining <= sub.getPercentageThreshold()){
                        sub.getResponse().resume(new ConcertInfoNotificationDTO(120 * (percentageSeatsRemaining)/100));
                    }

                }
            }
        }
    }

    /***
     * Helper function that returns the percentage of remaining seats as a int between 0-100.
     * Returns -1 if the concert is not found or does not occur on the specified date.
     *
     * @param date to calculate for
     * @param concertId to calcualte for
     * @return percentage of seats reaminging as int
     */
    public int calculatePercentageSeatsReaming(LocalDateTime date, long concertId){

        em = PersistenceManager.instance().createEntityManager();
        int percentage;
        try{
            em.getTransaction().begin();

            Concert concert = em.find(Concert.class, concertId);

            // Checking concertInfo validity
            if (concert == null || !concert.getDates().contains(date)){
                return -1;
            }

            TypedQuery<Seat> unbookedSeatsQuery = em.createQuery("select s from Seat s where s.date = :concertDate and s.isBooked = :bookingStatus", Seat.class)
                    .setParameter("concertDate", date)
                    .setParameter("bookingStatus", false);

            TypedQuery<Seat> bookedSeatsQuery = em.createQuery("select s from Seat s where s.date = :concertDate and s.isBooked = :bookingStatus", Seat.class)
                    .setParameter("concertDate", date)
                    .setParameter("bookingStatus", true);

            percentage =(unbookedSeatsQuery.getResultList().size() *100)/(unbookedSeatsQuery.getResultList().size() + bookedSeatsQuery.getResultList().size() );

            em.getTransaction().commit();
        }
        finally {
            em.close();
        }

        return percentage;
    }
}

