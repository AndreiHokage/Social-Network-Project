package socialNetwork.domain.models;

import java.util.List;
import java.util.Objects;

public class Chat {

    private List<User> members;
    private List<Message> messageList;
    private List<ReplyMessage> replyMessageList;

    public Chat(List<User> members, List<Message> messageList, List<ReplyMessage> replyMessageList) {
        this.members = sortMembersByID(members);
        this.messageList = sortMessagesByDate(messageList);;
        this.replyMessageList = sortReplyMessagesByDate(replyMessageList);
    }


    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public List<ReplyMessage> getReplyMessageList() {
        return replyMessageList;
    }

    public void setReplyMessageList(List<ReplyMessage> replyMessageList) {
        this.replyMessageList = replyMessageList;
    }

    public void addMessage(Message message){
        messageList.add(message);
    }

    public void addReplyMessage(ReplyMessage replyMessage){
        replyMessageList.add(replyMessage);
    }

    @Override
    public String toString() {
        return "Chat{" +
                "members=" + members +
                ", messageList=" + messageList +
                ", replyMessageList=" + replyMessageList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(members, chat.members) && Objects.equals(messageList, chat.messageList) && Objects.equals(replyMessageList, chat.replyMessageList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(members, messageList, replyMessageList);
    }

    private List<User> sortMembersByID(List<User> userList){
        return userList.stream()
                .sorted((User X,User Y) -> {
                    return X.getId().compareTo(Y.getId());
                }).toList();
    }

    private List<Message> sortMessagesByDate(List<Message> messageList) {
        return  messageList.stream()
                .sorted((Message messageX,Message messageY) -> {
                    return messageX.getDate().compareTo(messageY.getDate());
                })
                .toList();
    }

    private List<ReplyMessage> sortReplyMessagesByDate(List<ReplyMessage> replyMessageList) {
        return  replyMessageList.stream()
                .sorted((ReplyMessage messageX,ReplyMessage messageY) -> {
                    return messageX.getDate().compareTo(messageY.getDate());
                })
                .toList();
    }
}
