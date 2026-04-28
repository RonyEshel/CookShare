package com.cookshare.data.model

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
) {
    fun getOtherUserId(myId: String): String =
        participants.firstOrNull { it != myId } ?: ""

    fun getOtherUserName(myId: String): String =
        participantNames[getOtherUserId(myId)] ?: "User"
}
