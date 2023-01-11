package kenyalawnewsletterservice

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.jsoup.nodes.Document


@Serializable
data class CaseLink(val caseLink:String):Content
@Serializable
data class NewsLetterModel(val newsLetterName:String, val newsletters:List<NewsLetterPeriod>)
@Serializable
data class SubPeriod(val periodName:String, val caseLink:String)
@Serializable
data class NewsLetterPeriod(val newsLetterName:String, val newsLetterLink:String, val subPeriod:
List<SubPeriod>?=null, val caseLinks:List<CaseLink>?=null)
interface NewsLettersLinksService {
    suspend fun getPre2011NewsLetters():String?
    suspend fun getPost2014NewsLetters():String?

}
class NewsLettersLinksServiceDelegate:NewsLettersLinksService{
    private val caseLinkCssSelector="a:contains(Download the Decision)[href]"
    private val baseUrl="http://kenyalaw.org/kl/index.php?id=11565"
    private val newsLettersFrom2008To2011 = listOf("2008 Newsletters",
            "2009 Newsletters",
            "2010 Newsletters",
            "2011 Newsletters")
    private val newsLettersFrom2012To2013 = listOf("2012 Newsletters",
        "2013 Newsletters")

    private val logger = DependencyProvider.getLoggerInstance()
    private val json = DependencyProvider.getJsonInstance()
    private var newsLetters = mutableListOf<NewsLetterModel>()
     init {
         getAllNewsLetters()?.let{
           newsLetters.addAll(it)
        }
     }

     private fun getAllNewsLetters():List<NewsLetterModel>?{
       return baseUrl.createHtmlDocumentFromUrl(logger)?.parseNewsLettersPeriodsFromDocument()
    }

    override suspend fun getPre2011NewsLetters():String?{
       return retryIO {
           if (newsLetters.isNotEmpty()){
               val letters=newsLetters.getPre2011NewsLetters()
               val pre2011=getNewsLettersForPre2011Period(letters)
               json.encodeToString(pre2011)
           }else null
        }
    }
    override suspend fun getPost2014NewsLetters():String?{
       return retryIO {
           if (newsLetters.isNotEmpty()){
               val interMediateLetters=newsLetters.minus(newsLetters.getPre2011NewsLetters().toSet())
                   .filterNot { it.newsLetterName.contains("2012 Newsletters") ||
                           it.newsLetterName.contains("2013 Newsletters") || it.newsLetterName.contains("2014 Newsletters")}
               val post2014=getNewsLettersForPost2014Period(interMediateLetters)
               json.encodeToString(post2014)
           }else null

        }
    }
private fun getNewsLettersForPre2011Period(letters: List<NewsLetterModel>):List<NewsLetterModel>{
    return letters.map {nl->
       val newsLetters= nl.newsletters
       val modified=newsLetters.map {ln2->
           val documentToUse=ln2.newsLetterLink.createHtmlDocumentFromUrl(logger)
           val periods= documentToUse?.select("ul.vert-five > li > a")?.map { sub->
               SubPeriod(periodName = sub.text(),caseLink="http://kenyalaw.org/kl/${sub.attr("href")}")}
           ln2.copy(subPeriod = periods)
       }
      nl.copy(newsletters = modified)
    }
}
private fun getNewsLettersForPost2014Period(letters:List<NewsLetterModel>):List<NewsLetterModel>{
    return letters.map { l->
         val modified=l.newsletters.map { nl->
            val caseLinks= nl.newsLetterLink.createHtmlDocumentFromUrl(logger)
                 ?.select(caseLinkCssSelector)
                 ?.map { element->
                     val posteriorLink= element.attr("href").substringAfter("/view/")
                     CaseLink("http://kenyalaw.org/caselaw/cases/view/$posteriorLink")
                 }
             nl.copy(caseLinks = caseLinks)
         }
         l.copy(newsletters = modified)
     }

}

    private fun List<NewsLetterModel>.getPre2011NewsLetters():List<NewsLetterModel>{
       return filter { it.newsLetterName in newsLettersFrom2008To2011 }
    }

    private fun Document.parseNewsLettersPeriodsFromDocument():List<NewsLetterModel>{
        return select("ul.vert-three > li > a").map {
            val newsLettersLinks ="http://kenyalaw.org/kl/${it.attr("href")}"
            val newsLettersDocument=newsLettersLinks.createHtmlDocumentFromUrl(logger)
            val newsLetters=newsLettersDocument?.select("ul.vert-four > li > a")?.map {el->
                val link=" http://kenyalaw.org/kl/${it.attr("href")}"
                NewsLetterPeriod(el.text(),link)
            } ?: emptyList()
            NewsLetterModel(it.text(),newsLetters)
        }.filter { it.newsletters.isNotEmpty() }

    }
}