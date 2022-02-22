package socialNetwork.service;


import socialNetwork.domain.models.DTOEventPublicUser;
import socialNetwork.domain.models.EventNotification;
import socialNetwork.domain.models.EventPublic;
import socialNetwork.domain.validators.EventPublicValidator;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.PageableImplementation;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;
import socialNetwork.utilitaries.events.*;
import socialNetwork.utilitaries.observer.Observable;
import socialNetwork.utilitaries.observer.Observer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventPublicService implements Observable< Event > {

    PagingRepository<Long, EventPublic> eventPublicPagingRepository;
    PagingRepository<UnorderedPair<Long,Long>, DTOEventPublicUser> eventPublicUserPagingRepository;
    EventPublicValidator eventPublicValidator;
    public List< Observer<socialNetwork.utilitaries.events.Event> > observerList = new ArrayList<>();

    public EventPublicService(PagingRepository<Long, EventPublic> eventPublicPagingRepository,
                              PagingRepository<UnorderedPair<Long, Long>, DTOEventPublicUser> eventPublicUserPagingRepository,
                              EventPublicValidator eventPublicValidator) {
        this.eventPublicPagingRepository = eventPublicPagingRepository;
        this.eventPublicUserPagingRepository = eventPublicUserPagingRepository;
        this.eventPublicValidator = eventPublicValidator;
    }

    public Optional<EventPublic> addEventPublicService(String name, String description, LocalDateTime date){
        EventPublic eventPublicToSave = new EventPublic(name,description,date);
        eventPublicValidator.validate(eventPublicToSave);
        Optional<EventPublic> eventPublicSave = eventPublicPagingRepository.save(eventPublicToSave);
        notifyObservers(new EventPublicChangeEvent(
                EventPublicChangeEventType.ADD, eventPublicToSave));
        return eventPublicSave;
    }

    public Optional<DTOEventPublicUser> subscribeEventPublicService(Long idUser, Long idEventPublic){
        EventPublic eventPublicForSubscribe = eventPublicPagingRepository.find(idEventPublic).get();
        DTOEventPublicUser dtoEventPublicUserToSave = new DTOEventPublicUser(idUser,idEventPublic);
        Optional<DTOEventPublicUser> dtoEventPublicUserSave = eventPublicUserPagingRepository
                .save(dtoEventPublicUserToSave);
        notifyObservers(new EventPublicChangeEvent(
                EventPublicChangeEventType.SUBSCRIBE,eventPublicForSubscribe));
        return dtoEventPublicUserSave;
    }

    public Optional<DTOEventPublicUser> stopNotificationEventPublicService(Long idUser,Long idEventPublic){
        EventPublic eventPublicForStop = eventPublicPagingRepository.find(idEventPublic).get();
        DTOEventPublicUser dtoEventPublicUserToUpdate = new DTOEventPublicUser
                (idUser,idEventPublic, EventNotification.REJECT);
        Optional<DTOEventPublicUser> dtoEventPublicUserUpdate = eventPublicUserPagingRepository
                .update(dtoEventPublicUserToUpdate);
        notifyObservers(new EventPublicChangeEvent(
                EventPublicChangeEventType.STOPNOTIFICATION,eventPublicForStop));
        return dtoEventPublicUserUpdate;
    }

    public Optional<DTOEventPublicUser> turnOnNotificationEventPublicService(Long idUser, Long idEventPublic){
        EventPublic eventPublicToTurnNotificationsOn = eventPublicPagingRepository.find(idEventPublic).get();
        DTOEventPublicUser dtoEventPublicUser = new DTOEventPublicUser
                (idUser, idEventPublic, EventNotification.APPROVE);
        Optional<DTOEventPublicUser> dtoEventPublicUserUpdate = eventPublicUserPagingRepository
                .update(dtoEventPublicUser);
        notifyObservers(new EventPublicChangeEvent(
                EventPublicChangeEventType.ADD, eventPublicToTurnNotificationsOn)
        );
        return dtoEventPublicUserUpdate;
    }

    public Optional<EventPublic> findPublicEvent(Long idEvent){
        return eventPublicPagingRepository.find(idEvent);
    }

    /*
    return all events for which the user is subscribed and accept notifications from them
    Also return that event whose deadline is less than a given period of time
     */
    public List<EventPublic> filterAllEventPublicForNotificationService(Long idUser, Long days){
        LocalDateTime thisMoment = LocalDateTime.now();
        Predicate<DTOEventPublicUser> predicate = dtoEventPublicUser -> {
            return dtoEventPublicUser.getIdUser().equals(idUser) &&
                    dtoEventPublicUser.getReceivedNotification().equals(EventNotification.APPROVE);
        };

        List<EventPublic> eventPublicList = eventPublicUserPagingRepository.getAll()
                .stream()
                .filter(predicate)
                .map(dtoEventPublicUser -> {
                    Long idEventPublic = dtoEventPublicUser.getIdEventPublic();
                    EventPublic eventPublic = eventPublicPagingRepository.find(idEventPublic).get();
                    return eventPublic;
                })
                .filter(eventPublic -> {
                    LocalDateTime dateOfEvent = eventPublic.getDate();
                    Long years = ChronoUnit.YEARS.between(dateOfEvent,thisMoment);
                    Long months = ChronoUnit.MONTHS.between(dateOfEvent,thisMoment);
                    Long daysUntil = ChronoUnit.DAYS.between(dateOfEvent,thisMoment);
                    daysUntil += months*30 + years*365;
                    return  ( daysUntil.compareTo(days) <= 0 );
                })
                .toList();
        return eventPublicList;
    }

    public List<EventPublic> getAllEventPublicService(){
        return eventPublicPagingRepository.getAll();
    }

    public List<EventPublic> getAllEventPublicForSpecifiedUserService(Long idUser){
        Predicate<DTOEventPublicUser> predicate = dtoEventPublicUser -> {
            return dtoEventPublicUser.getIdUser().equals(idUser);
        };

        List<EventPublic> eventPublicList = eventPublicUserPagingRepository.getAll()
                .stream()
                .filter(predicate)
                .map(dtoEventPublicUser -> {
                    Long idEventPublic = dtoEventPublicUser.getIdEventPublic();
                    EventPublic eventPublic = eventPublicPagingRepository.find(idEventPublic).get();
                    return eventPublic;
                }).toList();
        return  eventPublicList;
    }

    public List<DTOEventPublicUser> getAllEventsWithNotificationStatus(Long idUser){
        Predicate<DTOEventPublicUser> predicate = dtoEventPublicUser -> {
            return dtoEventPublicUser.getIdUser().equals(idUser);
        };
        return eventPublicUserPagingRepository.getAll()
                .stream()
                .filter(predicate)
                .toList();
    }

    @Override
    public void addObserver(Observer<Event> observer) {
        observerList.add(observer);
    }

    @Override
    public void removeObserver(Observer<Event> observer) {
        observerList.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        observerList.forEach(x -> x.update(event));
    }

    private int pageNumber = 0;
    private int pageSize = 1;

    private Pageable pageable;

    private void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }

    private void setPageable(Pageable pageable){
        this.pageable = pageable;
    }

    public Set<EventPublic> getNextEventPublic(){
        this.pageNumber++;
        return getEventPublicOnPage(this.pageNumber);
    }

    public Set<EventPublic> getEventPublicOnPage(int pageNumber){
        this.pageNumber = pageNumber;
        Pageable pageable = new PageableImplementation(pageNumber,this.pageSize);
        Page<EventPublic> eventPublicPage = eventPublicPagingRepository.getAll(pageable);
        return eventPublicPage.getContent().collect(Collectors.toSet());
    }

}
