package kenyalawnewsletterservice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


interface CaseScraperService {
    /* Can be null if the parser did not get anything scrape worthy */
    suspend fun getCaseDetailsWhenGivenCaseLink(caseUrl:String):String?
}

class CaseScraperServiceDelegate:CaseScraperService{
    private val baseCaseParserCssSelector ="div.judgement > div#case_content div.case_content:nth-child(5)"
    private val documentWithArticleElementCssSelector="$baseCaseParserCssSelector article.akn-judgment"
    private val documentWithParagraphElementCssSelector="$baseCaseParserCssSelector p"
    private val cssSelectorFor2008To2011="div.page-content > div.csc-default > table[cellspacing=0] > tbody > tr:nth-child(7) > td:nth-child(1) > p"
    private val spaceRegex="\\s+".toRegex()
    private val newLineRegex="\\n+".toRegex()
    private val backToTopText="Back toTop"
    private val downloadCaseContentText="Download Full Text of This Judicial Opinion"
    private val logger = DependencyProvider.getLoggerInstance()
    private val json = DependencyProvider.getJsonInstance()
    private fun Document.parseCaseContentWhenCaseDocumentHasArticleElement():String{
        val judgmentBodyNodes=select("$documentWithArticleElementCssSelector > div#judgmentBody div").map { element ->
            element.getNodeWhenGivenElement()
        }
        val headerNodes = select("$documentWithArticleElementCssSelector > div.akn-header div > div").map{element->
            element.getNodeWhenGivenElement()
        }
        val nodes=headerNodes+judgmentBodyNodes.drop(1)
        return json.encodeToString(nodes)
    }
    private fun Document.parseCaseContentWhenCaseDocumentHasParagraphElement():String{
        val nodes=select(documentWithParagraphElementCssSelector).map { element ->
            element.getNodeWhenGivenElement()
        }
        return json.encodeToString(nodes)
    }
    private fun Document.parseCaseOfTheWeekContentFor2008To2011NewsLetter():String{
        val nodes=select(cssSelectorFor2008To2011).map { element->
            element.getNodeWhenGivenElement().takeIf { it.paragraph?.contains(backToTopText) ==false
                    || it.paragraph?.contains(downloadCaseContentText) ==false }
        }
      return json.encodeToString(nodes.takeIf { it.isNotEmpty() })
    }

    override suspend fun getCaseDetailsWhenGivenCaseLink(caseUrl: String): String? {
        return try {
            val document = caseUrl.createHtmlDocumentFromUrl(logger) ?: return null
            val stringfiedCaseContent=withContext(Dispatchers.IO){
               when{
                   document.select(cssSelectorFor2008To2011).isNotEmpty()->{
                       /* 2008 - 2011*/
                       document.parseCaseOfTheWeekContentFor2008To2011NewsLetter()
                   }
                   document.select(documentWithParagraphElementCssSelector).isNotEmpty()->{
                       /* cases with p selector */
                       document.parseCaseContentWhenCaseDocumentHasParagraphElement()
                   }
                   document.select(documentWithArticleElementCssSelector).isNotEmpty()->{
                       /* cases with article selector  mostly 2016-2023 cases */
                       document.parseCaseContentWhenCaseDocumentHasArticleElement()

                   }
                   else-> null
               }
            }
            stringfiedCaseContent
        }catch (ex:Exception){
            null
        }

    }
    private fun Element.getNodeWhenGivenElement():Node{
        val node= if (nextElementSibling()?.tagName() == "ol" || nextElementSibling()?.tagName() == "ul") {
            val nodes = nextElementSibling()?.select("li")
                ?.map { it.wholeText().replace(spaceRegex, " ").replace(newLineRegex, "\n") }
            val paragraph = wholeText()
                .replace(spaceRegex, " ")
                .replace(newLineRegex, "\n")
            Node(paragraph = paragraph, nodes)
        } else {
            val paragraph = wholeText()
                .replace(spaceRegex, " ")
                .replace(newLineRegex, "\n")
            Node(paragraph)
        }
        return node
    }
}