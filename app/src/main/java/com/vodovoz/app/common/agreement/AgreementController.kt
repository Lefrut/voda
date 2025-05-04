package com.vodovoz.app.common.agreement

object AgreementController {

    private val linkRegex = """<a\s+(?:[^>]*?\s+)?href=["']([^"']+)["']""".toRegex()
    private var agreementText: String = ""
    private val titles: ArrayList<String> = ArrayList()

    fun setAgreement(text: String?, titles: List<String>?) {
        agreementText = text ?: ""
        this.titles.clear()
        if(titles != null) {
            this.titles.addAll(titles)
        }
    }

    fun getTitle(index: Int): String?  {
        if(index >= titles.size) return null
        return titles[index]
    }

    fun getText(): String  {
        return agreementText
    }


    fun extractLinks(): List<String> {
        return linkRegex.findAll(agreementText)
            .map { it.groupValues[1] }
            .toList()
    }
}