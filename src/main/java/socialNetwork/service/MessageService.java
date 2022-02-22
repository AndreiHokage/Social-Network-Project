package socialNetwork.service;

import socialNetwork.domain.models.*;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.PageableImplementation;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.MessageChangeEvent;
import socialNetwork.utilitaries.events.MessageChangeEventType;
import socialNetwork.utilitaries.observer.Observable;
import socialNetwork.utilitaries.observer.Observer;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MessageService implements Observable<Event> {
    PagingRepository<Long, User> repoUser;
    PagingRepository<Long, MessageDTO> repoMessagesDTO;
    private List< Observer<Event> > observersMessage = new ArrayList<>();
    private static final int DATES_ARE_IDENTICAL = 0;
    private static final int MESSAGES_ARE_IN_CORRECT_ORDER = -1;
    private static final int REVERSE_ORDER_OF_MESSAGES = 1;

    public MessageService(PagingRepository<Long, User> repoUser, PagingRepository<Long, MessageDTO> repoMessagesDTO) {
        this.repoUser = repoUser;
        this.repoMessagesDTO = repoMessagesDTO;
    }

    /**
     * one user sends a text to another
     * @param idUserFrom Long - id of the user that sends the message
     * @param to List of Long - id's of users that will receive the message
     * @param text String
     * @return Optional containing the message built from the given data if repository save failed,
     * empty Optional otherwise
     */
    public Optional<Message> sendMessagesService(Long idUserFrom, List<Long> to, String text){
        User userFrom = getUserById(idUserFrom);
        List<User> usersTo = setListOfReceiversForMessageToSend(to);

        MessageDTO messageDTO = buildMessageToSendDto(userFrom, usersTo, text);

        return saveMessageThatIsNotReplyInRepo(messageDTO);
    }

    /**
     * one user responds to a message that was previously sent by another
     * @param idUserFrom Long - id of the user that responds to a message
     * @param idMessageAggregate - id of message that the users responds to
     * @param text - String
     * @return Optional containing the reply message built from the given data if repository save failed,
     * empty Optional otherwise
     */
    public Optional<ReplyMessage> respondMessageService(Long idUserFrom, Long idMessageAggregate, String text){
        User userFrom = getUserById(idUserFrom);
        //checkIfUserIsSupposedToRespondToMessage(idUserFrom, idMessageAggregate);

        MessageDTO messageDTO = findMessageWithSpecifiedId(idMessageAggregate);
        Message messageWeWantToRespondTo = messageDTO.getMainMessage();

        if( messageWeWantToRespondTo.getFrom().getId().equals(idUserFrom) )
            throw new CorruptedDataException("Stop talking alone......");

        List<User> to = setListOfReceiversForResponseMessage(userFrom, messageWeWantToRespondTo);
        MessageDTO messageDTOToSave = buildResponseMessageDTO(userFrom, to, text, messageWeWantToRespondTo);

        return saveReplyMessageDTOInRepo(userFrom, to, text, messageWeWantToRespondTo, messageDTOToSave);
    }


    /**
     * creates a list of HistoryConversationDTO (all messages shared by 2 users)
     * messages are returned chronologically
     * @param idFirstUser Long
     * @param idSecondUser Long
     * @return a list of HistoryConversationDTO
     */
    public List< List< HistoryConversationDTO > > historyConversationService(Long idFirstUser, Long idSecondUser){
        User firstUser = getUserById(idFirstUser);
        User secondUser = getUserById(idSecondUser);
        List < Message > messageList = getAllMainMessagesFromRepository();

        List<Message> filteredMessages = getMessagesThatBothUsersAreInvolvedIntoAndSortByDate(firstUser,
                secondUser, messageList);

        Map<List<Long> , List<Message> > usersGroupByChat = filteredMessages.stream()
                .collect(Collectors.groupingBy(message -> {
                    List<Long> listIdUser = new ArrayList<>();
                    listIdUser.add(message.getFrom().getId());
                    message.getTo().forEach(user -> listIdUser.add(user.getId()));
                    List<Long> sortedListByIdUser = listIdUser.stream()
                            .sorted((x,y) -> x.compareTo(y))
                            .toList();
                    return sortedListByIdUser;
                }));

        return usersGroupByChat.entrySet()
                .stream()
                .map(x ->{
                    return getHistoryConversationDTOListFromFilteredMessages(firstUser,secondUser,x.getValue());
                })
                .sorted((List<HistoryConversationDTO> x,List<HistoryConversationDTO> y) ->{
                    return (int)(x.size()-y.size());
                })
                .toList();
    }

    /**
     * gets a list of MessagesToRespondDTO - all messages that the given user can respond to
     * from which we remove the ones he already responded to
     * @param idUser Long
     * @return a list of MessagesToRespondDTO
     */
    public List<MessagesToRespondDTO> getAllMessagesToRespondForUserService(Long idUser){
        User user = getUserById(idUser);
        List<Message> messageListUserAlreadyRespondTo =
                getMessagesUserAlreadyRespondedTo(idUser);

        Predicate<MessageDTO> notReplyMessageUser = messageDTO -> {
            return (messageDTO.getMessageToRespondTo() == null) &&
                    userInList(idUser, messageDTO.getMainMessage().getTo()) &&
                    !messageInList(messageDTO.getMainMessage(),messageListUserAlreadyRespondTo);
        };

        return repoMessagesDTO.getAll()
                .stream()
                .filter( notReplyMessageUser )
                .map( messageDTO -> {
                    return new MessagesToRespondDTO(
                            messageDTO.getMainMessage().getId(),messageDTO.getMainMessage().getText());
                } )
                .toList();
    }

    /**
     * remove a certain message from the repository of dto messages
     * @return - Optional containing entity if exists, empty Optional otherwise
     */
    public Optional < MessageDTO > removeMessageService(Long idMessage) {
        return repoMessagesDTO.remove(idMessage);
    }

    /**
     * @return - a list of all messages that a user is taking part of - either as one who sends or
     * one who receives
     */
    public List<Message> allMessagesUserAppearsIn(Long idUser){
        Predicate<MessageDTO> allUserMessage = messageDTO -> {
            return messageDTO.getMainMessage().getFrom().getId().equals(idUser) ||
                    userInList(idUser,messageDTO.getMainMessage().getTo());
        };

        return repoMessagesDTO.getAll()
                .stream()
                .filter(allUserMessage)
                .map(MessageDTO::getMainMessage)
                .toList();
    }


    public List<Chat> getAllChatsSpecifiedUserMessageService(Long idUser){
        Predicate<MessageDTO> messageHasSpecifiedUser = messageDTO -> {
            return messageDTO.getMainMessage().getFrom().getId().equals(idUser) ||
                    userInList(idUser,messageDTO.getMainMessage().getTo());
        };
        //I group all messages by the users involved and the result chats have User with idUser ID
        Map< List<User>,List<MessageDTO> > messagesChats = repoMessagesDTO.getAll()
                        .stream()
                        .filter(messageHasSpecifiedUser)
                        .collect(Collectors.groupingBy(messageDTO -> {
                            Message mainMessage = messageDTO.getMainMessage();
                            List<User> members = mainMessage.getTo();
                            members.add(mainMessage.getFrom());

                            List<User> sortedMembersFromChat = members
                                    .stream()
                                    .sorted((User userX,User userY) -> {
                                        return userX.getId().compareTo(userY.getId());
                                    })
                                    .toList();
                            return  sortedMembersFromChat;
                        }));

        return messagesChats.entrySet()
                .stream()
                .map(allMessageChat -> {
                    List<User> members = allMessageChat.getKey();
                    List<Message> messageList = allMessageChat.getValue()
                            .stream()
                            .filter(messageDTO -> messageDTO.getMessageToRespondTo() == null)
                            .map(messageDTO -> messageDTO.getMainMessage())
                            .toList();
                    List<ReplyMessage> replyMessageList = allMessageChat.getValue()
                            .stream()
                            .filter(messageDTO -> messageDTO.getMessageToRespondTo() != null)
                            .map(messageDTO -> {
                                Message mainMessage = messageDTO.getMainMessage();
                                ReplyMessage replyMessage = new ReplyMessage(mainMessage.getFrom(),
                                        mainMessage.getTo(), mainMessage.getText(), messageDTO.getMessageToRespondTo());
                                replyMessage.setDate(mainMessage.getDate());
                                return replyMessage;
                            })
                            .toList();
                    return new Chat(members,messageList,replyMessageList);
                })
                .toList();
    }

    /**
     * checks if a certain message is part of a list of messages
     * @return - true if message is part of the list, false otherwise
     */
    private boolean messageInList(Message message,List<Message> messageList){
        for(Message messageFromTheList : messageList)
            if(message.getId().equals(messageFromTheList.getId()))
                return true;
        return false;
    }

    /**
     * @return the list of messages the user with idUser already responded to
     */
    private List<Message> getMessagesUserAlreadyRespondedTo(Long idUser){
        Predicate<MessageDTO> userReplyMessage = messageDTO -> {
            return ( messageDTO.getMessageToRespondTo() != null ) &&
                    messageDTO.getMainMessage().getFrom().getId().equals(idUser);
        };
        return repoMessagesDTO.getAll()
                .stream()
                .filter(userReplyMessage)
                .map(MessageDTO::getMessageToRespondTo)
                .toList();
    }

    /**
     * @return user knowing his identifier
     * throws exception if user does not exist
     */
    private User getUserById(Long id){
        Optional<User> findUserFrom = repoUser.find(id);
        if( findUserFrom.isEmpty())
            throw new EntityMissingValidationException("The user doesn't exist");
        return findUserFrom.get();
    }

    /**
     * gets all the messages from the repo, but only the main messages
     * @return List of messages
     */
    private List<Message> getAllMainMessagesFromRepository(){
        List < Message > messageList = new ArrayList<>();
        repoMessagesDTO.getAll().forEach(messageDTO -> {
            messageList.add(messageDTO.getMainMessage());
        });
        return messageList;
    }

    /**
     * check if a certain user is part of a list of users
     * @return - true if user is part of the list, false otherwise
     */
    private boolean userInList(Long idUser,List<User> list){
        for(User userTo: list)
            if( userTo.getId().equals(idUser) )
                return true;
        return false;
    }

    /**
     * check if user is supposed to respond to the message that has the id idMessageAggregate
     * @param idUserFrom Long
     * @param idMessageAggregate Long
     */
    private void checkIfUserIsSupposedToRespondToMessage(Long idUserFrom, Long idMessageAggregate) {
        boolean findAnyMessageThatUserHasToRespond = false;
        List<MessagesToRespondDTO> messageListUserHasToRespond = getAllMessagesToRespondForUserService(idUserFrom);
        for(MessagesToRespondDTO messagesToRespondDTO : messageListUserHasToRespond)
            if( messagesToRespondDTO.getId().equals(idMessageAggregate) ) {
                findAnyMessageThatUserHasToRespond = true;
                break;
            }
        if( !findAnyMessageThatUserHasToRespond )
            throw new CorruptedDataException("This is not a message for the specified user");
    }

    /**
     * finds MessageDTO in the repository with the specified id
     * @param idMessageAggregate Long
     * @return the message dto if found, throws EntityMissingValidationException exception if there are not
     * any messages with the given id
     */
    private MessageDTO findMessageWithSpecifiedId(Long idMessageAggregate) {
        Optional<MessageDTO> findMessageDTO = repoMessagesDTO.find(idMessageAggregate);
        if (findMessageDTO.isEmpty())
            throw new EntityMissingValidationException("The message doesn't exist");
        return findMessageDTO.get();
    }

    /**
     * sets the list of receivers for the response message - gets all the receivers from the message
     * to respond to, adds the user that sent that message and from that list we delete the user that
     * is currently replying
     * @param userFrom Long
     * @param message Message
     * @return a list of users
     */
    private List<User> setListOfReceiversForResponseMessage(User userFrom, Message message) {
        List<User> to = new ArrayList<>();
        to.add(message.getFrom());
        message.getTo().forEach(userTo -> {
            if (!userTo.getId().equals(userFrom.getId()))
                to.add(userTo);
        });
        return to;
    }

    /**
     * creates a list of User entities knowing their id's
     * @param to - List of identifiers
     * @return List of Users
     */
    private List<User> setListOfReceiversForMessageToSend(List<Long> to){
        List<User> usersTo = new ArrayList<>();
        to.forEach( idUserTo -> {
            User userTo = getUserById(idUserTo);
            usersTo.add(userTo);
        } );
        return usersTo;
    }

    /**
     * builds a DTO for a response message that will be saved in the repository
     * @param userFrom User
     * @param to List of users
     * @param text String
     * @param messageWeWantToRespondTo Message
     * @return MessageDTO
     */
    private MessageDTO buildResponseMessageDTO(User userFrom, List<User> to, String text,
                                            Message messageWeWantToRespondTo){
        Message responseMessage = new Message(userFrom,to,text);
        MessageDTO messageDTOToSave = new MessageDTO(responseMessage);
        messageDTOToSave.setMessageToRespondTo(messageWeWantToRespondTo);
        return  messageDTOToSave;
    }

    /**
     * build a MessageDTO that contains a message (which is not a reply)
     * @param userFrom User
     * @param to List of users
     * @param text String
     * @return MessageDTO
     */
    private MessageDTO buildMessageToSendDto(User userFrom, List<User> to, String text){
        Message message = new Message(userFrom,to,text);
        return new MessageDTO(message);
    }

    /**
     * gets the ReplyMessage entity from the DTO returned by the save method from repository
     * @param userFrom User
     * @param to List of users
     * @param text String
     * @param messageWeWantToRespondTo Message
     * @param messageDTOAfterSave MessageDTO
     * @return ReplyMessage
     */
    private ReplyMessage getReplyMessageFromDTOAfterSave(User userFrom, List<User> to, String text,
                                                         Message messageWeWantToRespondTo,
                                                         MessageDTO messageDTOAfterSave) {
        ReplyMessage replyMessage = new ReplyMessage(userFrom, to, text, messageWeWantToRespondTo);
        replyMessage.setDate(messageDTOAfterSave.getMainMessage().getDate());
        replyMessage.setIdEntity(messageDTOAfterSave.getMainMessage().getId());
        return replyMessage;
    }

    /**
     * @param userFrom User
     * @param to List of users
     * @param text String
     * @param messageWeWantToRespondTo Message
     * @param messageDTOToSave MessageDTO
     * @return Optional containing the reply message if the reply message dto could not be saved in repo,
     * empty Optional otherwise
     */
    private Optional<ReplyMessage> saveReplyMessageDTOInRepo(User userFrom, List<User> to, String text,
                                                             Message messageWeWantToRespondTo,
                                                             MessageDTO messageDTOToSave) {
        Optional<MessageDTO> messageDTOAfterSaveOptional = repoMessagesDTO.save(messageDTOToSave);
        if (messageDTOAfterSaveOptional.isEmpty()) {
            notifyObservers(new MessageChangeEvent(MessageChangeEventType.RESPOND,messageDTOToSave));
            return Optional.empty();
        }
        MessageDTO messageDTOAfterSave = messageDTOAfterSaveOptional.get();

        ReplyMessage replyMessage = getReplyMessageFromDTOAfterSave(userFrom, to, text,
                messageWeWantToRespondTo, messageDTOAfterSave);
        return Optional.of(replyMessage);
    }

    /**
     * saves a MessageDTO that contains a message (which is not a reply) in repo
     * @param messageDTO MessageDTO
     * @return Optional containing the  message if the message dto could not be saved in repo,
     * empty Optional otherwise
     */
    private Optional<Message> saveMessageThatIsNotReplyInRepo(MessageDTO messageDTO){
        Optional<MessageDTO> saveMessageDTO = repoMessagesDTO.save(messageDTO);
        if(saveMessageDTO.isEmpty()) {
            notifyObservers(new MessageChangeEvent(MessageChangeEventType.SEND,messageDTO));
            return Optional.empty();
        }
        return Optional.of(saveMessageDTO.get().getMainMessage());
    }

    /**
     * gets all the messages that both users are involved into(one as a sender and the other as a receiver
     * and vice-versa) and sort them by date
     * @param firstUser User
     * @param secondUser User
     * @param messageList List of messages
     * @return List of messages
     */
    private List<Message> getMessagesThatBothUsersAreInvolvedIntoAndSortByDate(User firstUser, User secondUser,
                                                                               List<Message> messageList) {
        Predicate<Message> isThere = message -> {
            return message.getFrom().getId().equals(firstUser.getId()) &&
                    userInList(secondUser.getId(), message.getTo()) ||
                    message.getFrom().getId().equals(secondUser.getId()) &&
                            userInList(firstUser.getId(), message.getTo());
        };

        return messageList.stream()
                .filter(isThere)
                .sorted((firstMessage, secondMessage) -> {
                    if (firstMessage.getDate().isEqual(secondMessage.getDate()))
                        return DATES_ARE_IDENTICAL;
                    if (firstMessage.getDate().isBefore(secondMessage.getDate()))
                        return MESSAGES_ARE_IN_CORRECT_ORDER;
                    return REVERSE_ORDER_OF_MESSAGES;
                })
                .toList();

    }

    /**
     * creates a list of history conversation DTO from the list of filtered messages
     * @param firstUser User
     * @param secondUser User
     * @param filteredMessages List of messages
     * @return a list of HistoryConversationDTO
     */
    private List<HistoryConversationDTO> getHistoryConversationDTOListFromFilteredMessages(User firstUser,
                                                                                           User secondUser,
                                                                                           List<Message>
                                                                                                   filteredMessages
                                                                                           ){
        List <HistoryConversationDTO> listHistoryConversation = new ArrayList<>();
        filteredMessages.forEach( message -> {
            if (message.getFrom().getId().equals(firstUser.getId()))
                listHistoryConversation.add(
                        new HistoryConversationDTO(firstUser.getFirstName(), firstUser.getLastName(),
                                message.getText(), message.getDate()));

            if (message.getFrom().getId().equals(secondUser.getId()))
                listHistoryConversation.add(
                        new HistoryConversationDTO(secondUser.getFirstName(), secondUser.getLastName(),
                                message.getText(), message.getDate()));
            });
        return listHistoryConversation;
        }

    @Override
    public void addObserver(Observer<Event> observer) {
        observersMessage.add(observer);
    }

    @Override
    public void removeObserver(Observer<Event> observer) {
        observersMessage.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        observersMessage.forEach(obs -> obs.update(event));
    }

    private int pageNumber = 0;
    private int pageSize = 1;

    private Pageable pageable;

    public void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }

    public void setPageable(Pageable pageable){
        this.pageable = pageable;
    }

    public Set<MessageDTO> getNextMessagesDTO(){
        this.pageNumber++;
        return getMessagesDTOnPage(this.pageNumber);
    }

    public Set<MessageDTO> getMessagesDTOnPage(int pageNumber){
        this.pageNumber = pageNumber;
        Pageable pageable = new PageableImplementation(pageNumber,this.pageSize);
        Page<MessageDTO> messagesDTOPage = repoMessagesDTO.getAll(pageable);
        return messagesDTOPage.getContent().collect(Collectors.toSet());
    }

}
