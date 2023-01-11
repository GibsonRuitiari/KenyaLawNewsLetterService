**KenyaLaw News Letter Service**

**What it is**

This is a command line tool built using kotlin, that enables you to pull news-letters from the kenya law website.
This tool essentially, parses the kenya law website and cleans the data gotten from the website, and churns it into 
a presentable format: json format.

**Why**

The unreliability attributed to the kenya law website is what led to the creation of this tool.
This tool is supposed to be integrated into the original Themis api built by our organization Ocluse.

**Pre-requisite** 

* Gradle - for building the tool
* Kotlin 

**Usage**

* You need to first install the tool on your machine before you can test it out.
* Open the folder containing this project, then on your terminal do : `gradle installDist  "&&"  
* app/build/install/app/bin/app`
* A distributable app will be installed on your machine and it will be ran automatically.
* To test out the commands do: `app/build/install/app/bin/app  caseContent http://kenyalaw.org/caselaw/cases/view/160865/ `
* This will out-put the following:

`[ {
"paragraph": " 1.   Kenya Hotel Properties limited, the Petitioner, filed a petition dated15th October 2015 and amended on 12th November 2015 against
the Attorney General, Judicial Service Commission, and Judges and Magistrates Vetting Board, the respondents, as well as Willesden Investments Limited, Ethics
and Anti-Corruption Commission and Kenya Revenue Authority, the 1st 2nd and 3rd interested parties respectively. According to the petition, the 1st intereste
d party had sued the petitioner in HCCC No 367 of 2000 seeking damages for trespass and mesne profits over a property previously leased to the petitioner by t
he then Nairobi city Council to which the petitioner was paying rent."
},// redacted for brevity`


