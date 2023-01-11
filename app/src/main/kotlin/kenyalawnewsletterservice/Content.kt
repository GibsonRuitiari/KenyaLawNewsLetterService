package kenyalawnewsletterservice

import kotlinx.serialization.Serializable

sealed interface Content
@Serializable
data class Node(val paragraph:String?=null,val paragraphs:List<String>?=null):Content