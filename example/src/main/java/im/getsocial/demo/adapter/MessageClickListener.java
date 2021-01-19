package im.getsocial.demo.adapter;
import im.getsocial.sdk.communities.ChatMessage;

public interface MessageClickListener {
    void onShowMessageDetails(ChatMessage message);
}