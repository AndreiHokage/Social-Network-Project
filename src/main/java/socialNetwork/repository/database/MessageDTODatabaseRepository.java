package socialNetwork.repository.database;

import socialNetwork.domain.models.Message;
import socialNetwork.domain.models.MessageDTO;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.Paginator;
import socialNetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDTODatabaseRepository implements PagingRepository<Long, MessageDTO> {

    private String url;
    private String user;
    private String password;
    private final boolean IS_EMPTY = false;
    private final int NO_ROWS_AFFECTED_WHEN_INSERTING = 0;
    private final Message NOT_EXISTING = null;

    public MessageDTODatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * returns a user instance by searching after his id
     * @param connection - Connection
     * @param id - Long
     * @return instance of User class
     */
    private User getUserById(Connection connection,Long id){
        try (PreparedStatement selectUserById = connection.prepareStatement(
                "select * from users where id = ?")) {
            selectUserById.setLong(1, id);
            ResultSet resultSetUsers = selectUserById.executeQuery();
            resultSetUsers.next();
            Long idUser = resultSetUsers.getLong("id");
            String firstName = resultSetUsers.getString("first_name");
            String lastname = resultSetUsers.getString("last_name");
            String username = resultSetUsers.getString("username");
            return buildUser(idUser, firstName, lastname,username);
        } catch (SQLException throwable) {
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * creates and returns a user
     * @param idUser - Long
     * @param firstName - String
     * @param lastname - String
     * @return User
     */
    private User buildUser(Long idUser, String firstName, String lastname ,String username) {
        User user = new User(idUser, firstName, lastname ,username);
        user.setIdEntity(idUser);
        return user;
    }

    /**
     * gets a non reply message given the id of the message
     * @param connection - Connection
     * @param idOfMessage - Long
     * @return Optional of Message
     */
    private Optional<Message> getNotReplyMessageById(Connection connection,Long idOfMessage) {
        try (PreparedStatement selectTextDataMessage =
                     connection.prepareStatement("select * from messages where id = ?")) {

            selectTextDataMessage.setLong(1, idOfMessage);
            ResultSet resultSetIdTextDateMessage = selectTextDataMessage.executeQuery();
            if (resultSetIdTextDateMessage.next() == IS_EMPTY)
                return Optional.empty();

            Long idMessage = resultSetIdTextDateMessage.getLong("id");
            String text = resultSetIdTextDateMessage.getString("text");
            LocalDateTime date = resultSetIdTextDateMessage.getTimestamp("date").toLocalDateTime();

            PreparedStatement selectFromToUsers = getIdUsersInvolvedInTheMessage(connection, idMessage);
            ResultSet resultSetSentReceiveUsers = selectFromToUsers.executeQuery();

            return buildNotReplyMessageAfterGettingFromAndTo(connection, idMessage, text,
                    date, resultSetSentReceiveUsers);

        } catch (SQLException throwable) {
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * creates a not reply message after getting the from and to of a message's id
     * @param connection Connection
     * @param idMessage Long
     * @param text body of Message
     * @param date LocalDateTime
     * @param resultSetSentReceiveUsers ResultSet
     * @return an optional of Message
     */
    private Optional<Message> buildNotReplyMessageAfterGettingFromAndTo(Connection connection,
                                                                        Long idMessage, String text,
                                                                        LocalDateTime date,
                                                                        ResultSet resultSetSentReceiveUsers){
        try {
            User from = null;
            List<User> to = new ArrayList<>();
            while (resultSetSentReceiveUsers.next()) {
                User userTo = getUserTo(connection, resultSetSentReceiveUsers);
                to.add(userTo);
                from = getUserFrom(connection, resultSetSentReceiveUsers);
            }
            return buildOptionalNotResponseMessage(idMessage, text, date, from, to);
        }
        catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * @param connection Connection
     * @param idMessage Long
     * @return a PreparedStatement that gives the id of users involved in a message with a certain id
     */
    private PreparedStatement getIdUsersInvolvedInTheMessage(Connection connection, Long idMessage){
        try {
            PreparedStatement selectFromToUsers = connection.prepareStatement(
                    "select id_user_send,id_user_receive from messages_id_correlation where id_message = ?");
            selectFromToUsers.setLong(1, idMessage);
            return selectFromToUsers;
        }catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * @param connection Connection
     * @param resultSetSentReceiveUsers ResultSet
     * @return User that sends the message
     */
    private User getUserFrom(Connection connection, ResultSet resultSetSentReceiveUsers){
        try {
            Long id_user_send = resultSetSentReceiveUsers.getLong("id_user_send");
            return getUserById(connection, id_user_send);
        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * @param connection Connection
     * @param resultSetSentReceiveUsers ResultSet
     * @return User that receives the message
     */
    private User getUserTo(Connection connection, ResultSet resultSetSentReceiveUsers){
        try {
            Long id_user_receive = resultSetSentReceiveUsers.getLong("id_user_receive");
            return getUserById(connection, id_user_receive);
        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * @param idMessage - Long
     * @param text - message's body
     * @param date - LocalDateTime
     * @param from - who sent the message
     * @param to - to whom we send the message
     * @return an optional of Message
     */
    private Optional<Message> buildOptionalNotResponseMessage(Long idMessage, String text, LocalDateTime date, User from, List<User> to) {
        Message responseMessage = new Message(from, to, text);
        responseMessage.setDate(date);
        responseMessage.setIdEntity(idMessage);

        return Optional.of(responseMessage);
    }

    @Override
    public Optional<MessageDTO> find(Long idSearchedEntity) {

        try( Connection connection = DriverManager.getConnection(url,user,password)) {

            Optional<Message> responseMessageOptional = getNotReplyMessageById(connection,idSearchedEntity);
            if(responseMessageOptional.isEmpty())
                return Optional.empty();
            Message responseMessage = responseMessageOptional.get();

            return buildMessageDTOAfterCheckingIfReplyMessage(connection, responseMessage);

        } catch (SQLException throwable) {
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * creates and returns a MessageDTO after checking if the message we search is a simple message
     * or a reply one
     * @param connection Connection
     * @param responseMessage Message
     * @return Optional of MessageDTO
     */
    private Optional<MessageDTO> buildMessageDTOAfterCheckingIfReplyMessage(Connection connection,
                                                                            Message responseMessage){
        try {
            MessageDTO messageDTO = new MessageDTO(responseMessage);
            PreparedStatement isThereReplyMessage = getStatementThatChecksForReplies(connection,
                    responseMessage);
            ResultSet resultSetFindReplyMessage = isThereReplyMessage.executeQuery();
            return setsMessageWeWantToRespondToIfReply(connection, messageDTO, resultSetFindReplyMessage);

        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * checks if the current message is not only a simple message, but a reply one; if so, the function
     * sets the message we want to respond to
     * @param connection Connection
     * @param messageDTO MessageDTO that has only the main message
     * @param resultSetFindReplyMessage ResultSet
     * @return optional of MessageDTO
     */
    private Optional<MessageDTO> setsMessageWeWantToRespondToIfReply(Connection connection, MessageDTO messageDTO,
                                                                     ResultSet resultSetFindReplyMessage){
        try {
            if (resultSetFindReplyMessage.next()) {
                Long idTheMessageWeWantToRespondTo = resultSetFindReplyMessage.getLong("id_message");
                Optional<Message> theMessageWeWantToRespondTo =
                        getNotReplyMessageById(connection, idTheMessageWeWantToRespondTo);
                messageDTO.setMessageToRespondTo(theMessageWeWantToRespondTo.get());
            }

            return Optional.of(messageDTO);
        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * @param connection Connection
     * @param responseMessage Message
     * @return PreparedStatement that searches for reply messages
     */
    private PreparedStatement getStatementThatChecksForReplies(Connection connection, Message responseMessage){
        try {
            Long idMessage = responseMessage.getId();

            PreparedStatement isThereReplyMessage = connection.prepareStatement(
                    "select * from replymessages where id_reply = ?");
            isThereReplyMessage.setLong(1, idMessage);
            return isThereReplyMessage;
        }
        catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    @Override
    public List<MessageDTO> getAll() {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement selectAllIdFromMessages =
                connection.prepareStatement("select id from messages");
            ResultSet resultSetAllId = selectAllIdFromMessages.executeQuery()) {

            return getListOfAllMessageDTO(connection, resultSetAllId);
        } catch (SQLException throwable) {
            throw new DatabaseException(throwable.getMessage());
        }
    }

    @Override
    public Page<MessageDTO> getAll(Pageable pageable) {
        Paginator<MessageDTO> paginator = new Paginator<MessageDTO>(pageable,getAll());
        return paginator.paginate();
    }

    /**
     * creates and returns a list with all MessageDTO in repo
     * @param connection Connection
     * @param resultSetAllId ResultSet
     * @return a list of MessageDTO
     */
    private List<MessageDTO> getListOfAllMessageDTO(Connection connection, ResultSet resultSetAllId){
        try{
            List <MessageDTO> messageDTOList = new ArrayList<>();
            while(resultSetAllId.next()){
                Long idMessage = resultSetAllId.getLong("id");
                Optional<MessageDTO> messageDTO = find(idMessage);
                messageDTO.ifPresent(messageDTOList::add);
            }
            return messageDTOList;
        }catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    @Override
    public Optional<MessageDTO> save(MessageDTO entityToSave) {
        Message responseMessage = entityToSave.getMainMessage();

        try(Connection connection = DriverManager.getConnection(url,user,password)){
            var insertMessage = insertMessageStatement(connection, responseMessage);
            int rowCount = insertMessage.executeUpdate();

            if( rowCount == NO_ROWS_AFFECTED_WHEN_INSERTING )
                return Optional.of(entityToSave);

            Long maxId = getMaximumIdFromMessages(connection);
            responseMessage.setIdEntity(maxId);

            insertIntoMessagesIdCorrelation(connection, responseMessage, maxId);

            Message theMessageWeWantToRespondTo = entityToSave.getMessageToRespondTo();

            if( theMessageWeWantToRespondTo != NOT_EXISTING ){
                insertIntoReplyMessages(responseMessage, connection, theMessageWeWantToRespondTo);
            }
            return Optional.empty();

        } catch (SQLException throwable) {
            throw new DatabaseException(throwable.getMessage());
        }

    }

    /**
     * inserts data into the replymessages table
     * @param responseMessage Message
     * @param connection Connection
     * @param theMessageWeWantToRespondTo Message
     */
    private void insertIntoReplyMessages(Message responseMessage, Connection connection, Message theMessageWeWantToRespondTo){
        try {
            PreparedStatement insertToReplyMessages =
                    connection.prepareStatement("insert into replymessages(id_message,id_reply) values(?,?)");
            insertToReplyMessages.setLong(1, theMessageWeWantToRespondTo.getId());
            insertToReplyMessages.setLong(2, responseMessage.getId());
            insertToReplyMessages.executeUpdate();
        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * function that inserts into the table messages_id_correlation the information from responseMessage
     * @param connection Connection
     * @param responseMessage Message
     * @param maxId Long
     */
    private void insertIntoMessagesIdCorrelation(Connection connection, Message responseMessage, Long maxId){
        try{
            PreparedStatement insertConnectionToChat =
                    connection.prepareStatement(
                            "insert into messages_id_correlation(id_user_send,id_user_receive,id_message) values (?,?,?)");
            responseMessage.getTo().forEach( userTo -> {
                try {
                    insertConnectionToChat.setLong(1,responseMessage.getFrom().getId());
                    insertConnectionToChat.setLong(2,userTo.getId());
                    insertConnectionToChat.setLong(3,maxId);
                    insertConnectionToChat.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } );
        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * gets the id of the recently saved message, which is automatically incremented, so
     * it will be the maximum one
     * @param connection Connection
     * @return maximum id
     */
    private Long getMaximumIdFromMessages(Connection connection){
        try {
            String findIdLastRecord = "select id from messages where id = ( select MAX(id) from messages  ) ";
            PreparedStatement getIdFromLastRecord = connection.prepareStatement(findIdLastRecord);
            ResultSet resultSetMaxId = getIdFromLastRecord.executeQuery();
            resultSetMaxId.next();
            return resultSetMaxId.getLong("id");
        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    /**
     * @param connection Connection
     * @param responseMessage Message
     * @return PreparedStatement that inserts data in the messages table
     */
    private PreparedStatement insertMessageStatement(Connection connection, Message responseMessage){
        try{
            PreparedStatement insertMessage =
                    connection.prepareStatement("insert into messages(text,date) values (?,?)");
            insertMessage.setString(1,responseMessage.getText());
            insertMessage.setTimestamp(2, Timestamp.valueOf(responseMessage.getDate()));
            return insertMessage;
        } catch (SQLException throwable){
            throw new DatabaseException(throwable.getMessage());
        }
    }

    @Override
    public Optional<MessageDTO> remove(Long idMessageToRemove) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var oldMessage = find(idMessageToRemove);
            PreparedStatement deleteFromMessages = connection.prepareStatement
                    ("delete from messages where id = ?");
            deleteFromMessages.setLong(1, idMessageToRemove);
            int rowsAffected = deleteFromMessages.executeUpdate();
            if(rowsAffected == 0)
                return Optional.empty();
            return oldMessage;
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Optional<MessageDTO> update(MessageDTO entityToUpdate) {
        return Optional.empty();
    }
}
